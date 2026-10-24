package app.purebit

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.OpenableColumns
import java.io.File

/** Only app-created diagnostic .txt files may be shared; no arbitrary cache/source access. */
class DiagnosticReportProvider : ContentProvider() {
    override fun onCreate() = true
    private fun report(uri: Uri): File {
        val parts = uri.pathSegments
        require(parts.size == 1 && Regex("PureBit-[0-9a-f-]{36}\\.txt").matches(parts[0])) { "Invalid report URI" }
        val root = File(requireNotNull(context).filesDir, "diagnostic-reports")
        val file = File(root, parts[0])
        require(file.isFile && file.canonicalFile.parentFile == root.canonicalFile) { "Report unavailable" }
        return file
    }
    override fun getType(uri: Uri) = "text/plain"
    override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor {
        require(mode == "r") { "Read-only diagnostic provider" }
        return ParcelFileDescriptor.open(report(uri), ParcelFileDescriptor.MODE_READ_ONLY)
    }
    override fun query(uri: Uri, projection: Array<out String>?, selection: String?, selectionArgs: Array<out String>?, sortOrder: String?): Cursor {
        val file = report(uri)
        val columns = projection ?: arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE)
        return MatrixCursor(columns).apply {
            addRow(columns.map { when (it) { OpenableColumns.DISPLAY_NAME -> file.name; OpenableColumns.SIZE -> file.length(); else -> null } }.toTypedArray<Any?>())
        }
    }
    override fun insert(uri: Uri, values: ContentValues?): Uri? = throw UnsupportedOperationException("Read-only")
    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int = throw UnsupportedOperationException("Read-only")
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = throw UnsupportedOperationException("Read-only")
}
