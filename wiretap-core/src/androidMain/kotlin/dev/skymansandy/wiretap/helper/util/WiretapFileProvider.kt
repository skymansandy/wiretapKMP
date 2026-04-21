/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.helper.util

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.OpenableColumns
import java.io.File

/**
 * Minimal ContentProvider to serve shared HTTP log files to external apps
 * without requiring XML resources (which aren't supported by androidMultiplatformLibrary plugin).
 *
 * Authority: `${applicationId}.wiretap.fileprovider`
 */
internal class WiretapFileProvider : ContentProvider() {

    private fun getShareDir(): File {
        val dir = File(context!!.cacheDir, SHARE_DIR_NAME)
        dir.mkdirs()
        return dir
    }

    private fun resolveFile(uri: Uri): File? {
        val fileName = uri.lastPathSegment ?: return null
        val file = File(getShareDir(), fileName)
        // Prevent path traversal
        if (!file.canonicalPath.startsWith(getShareDir().canonicalPath)) return null
        return file.takeIf { it.exists() }
    }

    override fun onCreate(): Boolean = true

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?,
    ): Cursor? {
        val file = resolveFile(uri) ?: return null
        val columns = projection ?: arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE)
        val cursor = MatrixCursor(columns)
        val row = columns.map { column ->
            when (column) {
                OpenableColumns.DISPLAY_NAME -> file.name
                OpenableColumns.SIZE -> file.length()
                else -> null
            }
        }
        cursor.addRow(row)
        return cursor
    }

    override fun getType(uri: Uri): String = "text/plain"

    override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor? {
        val file = resolveFile(uri) ?: return null
        return ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0
    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int = 0
}
