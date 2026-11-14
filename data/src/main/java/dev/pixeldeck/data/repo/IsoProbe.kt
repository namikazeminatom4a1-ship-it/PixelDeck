package dev.pixeldeck.data.repo

import android.content.Context
import android.net.Uri
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.nio.charset.StandardCharsets

object IsoProbe {

    private const val SECTOR = 2048
    private const val PVD_LBA = 16L
    private const val MAX_DIRECTORY_BYTES = 256 * 1024

    fun detect(context: Context, uri: Uri): String? = runCatching {
        context.contentResolver.openFileDescriptor(uri, "r")?.use { descriptor ->
            FileInputStream(descriptor.fileDescriptor).use { stream ->
                stream.channel.use { channel -> detect(channel) }
            }
        }
    }.getOrNull()

    private fun detect(channel: FileChannel): String? {
        val pvd = read(channel, PVD_LBA * SECTOR, SECTOR) ?: return null
        if (pvd.size < 190 || pvd[0].toInt() != 1) return null
        if (String(pvd, 1, 5, StandardCharsets.US_ASCII) != "CD001") return null

        val root = recordAt(pvd, 156) ?: return null
        val rootBytes = read(
            channel,
            root.extent * SECTOR,
            root.length.coerceAtMost(MAX_DIRECTORY_BYTES.toLong()).toInt(),
        ) ?: return null

        val entries = parseEntries(rootBytes)
        if (entries.containsKey("PSP_GAME") || entries.containsKey("UMD_DATA.BIN")) return "psp"

        val systemCnf = entries["SYSTEM.CNF"]
        if (systemCnf != null) {
            val bytes = read(
                channel,
                systemCnf.extent * SECTOR,
                systemCnf.length.coerceAtMost(1024L).toInt(),
            ) ?: return null
            val text = String(bytes, StandardCharsets.US_ASCII).uppercase()
            if (text.contains("BOOT2")) return "ps2"
            if (text.contains("BOOT")) return "psx"
        }

        if (entries.containsKey("PSX.EXE")) return "psx"
        return null
    }

    private fun parseEntries(data: ByteArray): Map<String, Record> {
        val entries = mutableMapOf<String, Record>()
        var offset = 0
        while (offset + 34 <= data.size) {
            val length = data[offset].toInt() and 0xFF
            if (length == 0) {
                offset = ((offset / SECTOR) + 1) * SECTOR
                continue
            }
            if (length < 34 || offset + length > data.size) break
            val record = recordAt(data, offset) ?: break
            val name = record.name.substringBefore(';').trimEnd('.').uppercase()
            if (name.isNotEmpty() && name != "." && name != "..") {
                entries[name] = record
            }
            offset += length
        }
        return entries
    }

    private fun recordAt(data: ByteArray, offset: Int): Record? {
        if (offset + 34 > data.size) return null
        val length = data[offset].toInt() and 0xFF
        if (length < 34 || offset + length > data.size) return null
        val nameLength = data[offset + 32].toInt() and 0xFF
        if (nameLength == 0 || offset + 33 + nameLength > data.size) return null
        return Record(
            extent = le32(data, offset + 2),
            length = le32(data, offset + 10),
            name = String(data, offset + 33, nameLength, StandardCharsets.US_ASCII),
        )
    }

    private fun read(channel: FileChannel, offset: Long, size: Int): ByteArray? {
        if (size <= 0) return null
        val buffer = ByteBuffer.allocate(size)
        var position = offset
        while (buffer.hasRemaining()) {
            val read = channel.read(buffer, position)
            if (read <= 0) break
            position += read
        }
        if (buffer.position() == 0) return null
        return buffer.array().copyOf(buffer.position())
    }

    private fun le32(bytes: ByteArray, offset: Int): Long =
        (bytes[offset].toLong() and 0xFF) or
            ((bytes[offset + 1].toLong() and 0xFF) shl 8) or
            ((bytes[offset + 2].toLong() and 0xFF) shl 16) or
            ((bytes[offset + 3].toLong() and 0xFF) shl 24)

    private data class Record(val extent: Long, val length: Long, val name: String)
}
