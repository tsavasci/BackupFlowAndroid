package com.example.backupflow.data.provider

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FileAccessManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun takePersistableUriPermission(treeUri: Uri) {
        val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION
        context.contentResolver.takePersistableUriPermission(treeUri, takeFlags)
    }

    suspend fun listFilesFromTreeUri(treeUri: Uri): List<LocalFileItem> = withContext(Dispatchers.IO) {
        val items = mutableListOf<LocalFileItem>()
        val docId = DocumentsContract.getTreeDocumentId(treeUri)
        val rootUri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, docId)

        traverseDirectory(treeUri, rootUri, "", items)
        return@withContext items
    }

    private fun traverseDirectory(
        treeUri: Uri,
        parentChildUri: Uri,
        currentRelativePath: String,
        resultList: MutableList<LocalFileItem>
    ) {
        val projection = arrayOf(
            DocumentsContract.Document.COLUMN_DOCUMENT_ID,
            DocumentsContract.Document.COLUMN_DISPLAY_NAME,
            DocumentsContract.Document.COLUMN_MIME_TYPE,
            DocumentsContract.Document.COLUMN_SIZE,
            DocumentsContract.Document.COLUMN_LAST_MODIFIED
        )

        val cursor = context.contentResolver.query(parentChildUri, projection, null, null, null)

        cursor?.use {
            val idCol = it.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
            val nameCol = it.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
            val mimeCol = it.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_MIME_TYPE)
            val sizeCol = it.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_SIZE)
            val modCol = it.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_LAST_MODIFIED)

            while (it.moveToNext()) {
                val docId = it.getString(idCol)
                val displayName = it.getString(nameCol) ?: continue
                val mimeType = it.getString(mimeCol)
                val size = it.getLong(sizeCol)
                val lastModified = it.getLong(modCol)

                val itemRelativePath = if (currentRelativePath.isEmpty()) displayName else "$currentRelativePath/$displayName"

                if (DocumentsContract.Document.MIME_TYPE_DIR == mimeType) {
                    val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, docId)
                    traverseDirectory(treeUri, childrenUri, itemRelativePath, resultList)
                } else {
                    val fileUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, docId)
                    resultList.add(
                        LocalFileItem(
                            relativePath = itemRelativePath,
                            uri = fileUri,
                            size = size,
                            lastModified = lastModified
                        )
                    )
                }
            }
        }
    }
}
