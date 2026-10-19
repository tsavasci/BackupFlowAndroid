package com.example.backupflow.data.provider

import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.core.content.ContextCompat
import com.example.backupflow.domain.model.MediaType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

data class LocalFileItem(
    val relativePath: String,
    val uri: Uri,
    val size: Long,
    val lastModified: Long
)

@Singleton
class PhotoLibraryManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun hasFullPhotoAccess(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val imagesGranted = ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED
            val videosGranted = ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_MEDIA_VIDEO) == PackageManager.PERMISSION_GRANTED
            val userSelectedGranted = ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED) == PackageManager.PERMISSION_GRANTED
            
            (imagesGranted || videosGranted) && !userSelectedGranted
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val imagesGranted = ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED
            val videosGranted = ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_MEDIA_VIDEO) == PackageManager.PERMISSION_GRANTED
            imagesGranted || videosGranted
        } else {
            ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        }
    }

    suspend fun fetchMediaAssets(mediaType: MediaType): List<LocalFileItem> = withContext(Dispatchers.IO) {
        val items = mutableListOf<LocalFileItem>()

        val projection = arrayOf(
            MediaStore.MediaColumns._ID,
            MediaStore.MediaColumns.DISPLAY_NAME,
            MediaStore.MediaColumns.SIZE,
            MediaStore.MediaColumns.DATE_MODIFIED,
            MediaStore.MediaColumns.RELATIVE_PATH
        )

        fun queryUri(contentUri: Uri) {
            val cursor = context.contentResolver.query(
                contentUri,
                projection,
                null,
                null,
                "${MediaStore.MediaColumns.DATE_MODIFIED} DESC"
            )

            cursor?.use {
                val idColumn = it.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
                val nameColumn = it.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
                val sizeColumn = it.getColumnIndexOrThrow(MediaStore.MediaColumns.SIZE)
                val dateColumn = it.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_MODIFIED)
                val relPathColumn = it.getColumnIndexOrThrow(MediaStore.MediaColumns.RELATIVE_PATH)

                while (it.moveToNext()) {
                    val id = it.getLong(idColumn)
                    val name = it.getString(nameColumn) ?: "unknown_$id"
                    val size = it.getLong(sizeColumn)
                    val dateModifiedSec = it.getLong(dateColumn)
                    val relPath = it.getString(relPathColumn) ?: ""

                    val fileUri = Uri.withAppendedPath(contentUri, id.toString())
                    val cleanRelativePath = if (relPath.isNotEmpty()) "$relPath$name" else name

                    items.add(
                        LocalFileItem(
                            relativePath = cleanRelativePath.trimStart('/'),
                            uri = fileUri,
                            size = size,
                            lastModified = dateModifiedSec * 1000L
                        )
                    )
                }
            }
        }

        if (mediaType == MediaType.PHOTOS_ONLY || mediaType == MediaType.PHOTOS_AND_VIDEOS) {
            queryUri(MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        }
        if (mediaType == MediaType.VIDEOS_ONLY || mediaType == MediaType.PHOTOS_AND_VIDEOS) {
            queryUri(MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
        }

        return@withContext items
    }
}
