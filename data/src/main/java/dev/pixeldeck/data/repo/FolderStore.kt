package dev.pixeldeck.data.repo

import android.content.Context
import android.net.Uri

class FolderStore(context: Context) {

    private val prefs = context.getSharedPreferences("game_folders", Context.MODE_PRIVATE)

    fun add(uri: Uri) {
        val current = prefs.getStringSet(KEY, emptySet()).orEmpty()
        prefs.edit().putStringSet(KEY, current + uri.toString()).apply()
    }

    fun remove(uri: Uri) {
        val current = prefs.getStringSet(KEY, emptySet()).orEmpty()
        prefs.edit().putStringSet(KEY, current - uri.toString()).apply()
    }

    fun all(): List<Uri> =
        prefs.getStringSet(KEY, emptySet()).orEmpty().map(Uri::parse)

    private companion object {
        const val KEY = "trees"
    }
}
