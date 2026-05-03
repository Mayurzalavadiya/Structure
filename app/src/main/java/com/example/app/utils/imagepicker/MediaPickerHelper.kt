package com.example.app.utils.imagepicker

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import com.example.app.BuildConfig
import com.example.app.ui.base.BaseActivity
import java.io.File
import java.io.FileOutputStream

data class PickedMedia(
    val uri: Uri,
    val path: String
)


class MediaPickerHelper(
    private val activity: BaseActivity,
    private val callback: MediaPickerCallback
) {

    interface MediaPickerCallback {
        fun onMediaSelected(media: List<PickedMedia>)
        fun onCameraImageCaptured(uri: Uri, path: String)
        fun onCameraVideoCaptured(uri: Uri, path: String)
        fun onError(error: String)
    }

    private lateinit var singleImagePicker: ActivityResultLauncher<PickVisualMediaRequest>
    private lateinit var multipleImageVideoPicker: ActivityResultLauncher<PickVisualMediaRequest>
    private lateinit var singleVideoPicker: ActivityResultLauncher<PickVisualMediaRequest>
    private lateinit var cameraImageLauncher: ActivityResultLauncher<Uri>
    private lateinit var cameraVideoLauncher: ActivityResultLauncher<Uri>

    private var cameraImageUri: Uri? = null
    private var cameraVideoUri: Uri? = null
    private var cameraImagePath: String? = null
    private var cameraVideoPath: String? = null

    init {
        initializePickers()
    }

    private fun initializePickers() {

        singleImagePicker =
            activity.registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
                if (uri != null) {
                    val path = copyUriToCache(activity, uri)
                    if (path != null) {
                        callback.onMediaSelected(listOf(PickedMedia(uri, path)))
                    } else {
                        callback.onError("Failed to read selected image")
                    }
                } else callback.onError("No image selected")
            }


        multipleImageVideoPicker =
            activity.registerForActivityResult(
                ActivityResultContracts.PickMultipleVisualMedia(10)
            ) { uris ->
                if (!uris.isNullOrEmpty()) {
                    val mediaList = mutableListOf<PickedMedia>()

                    for (uri in uris) {
                        val path = copyUriToCache(activity, uri)
                        if (path != null) {
                            mediaList.add(PickedMedia(uri, path))
                        }
                    }

                    if (mediaList.isNotEmpty()) {
                        callback.onMediaSelected(mediaList)
                    } else {
                        callback.onError("Failed to read selected media")
                    }
                } else callback.onError("No media selected")
            }


        singleVideoPicker =
            activity.registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
                if (uri != null) {
                    val path = copyUriToCache(activity, uri)
                    if (path != null) {
                        callback.onMediaSelected(listOf(PickedMedia(uri, path)))
                    } else callback.onError("Failed to read selected video")
                } else callback.onError("No video selected")
            }


        cameraImageLauncher =
            activity.registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
                if (success && cameraImageUri != null) {
                    val path = copyUriToCache(activity, cameraImageUri!!)
                    if (path != null)
                        callback.onCameraImageCaptured(cameraImageUri!!, path)
                    else callback.onError("Failed to read captured image")
                } else callback.onError("Image capture failed")
            }

        cameraVideoLauncher =
            activity.registerForActivityResult(ActivityResultContracts.CaptureVideo()) { success ->
                if (success && cameraVideoUri != null) {
                    val path = copyUriToCache(activity, cameraVideoUri!!)
                    if (path != null)
                        callback.onCameraVideoCaptured(cameraVideoUri!!, path)
                    else callback.onError("Failed to read captured video")
                } else callback.onError("Video capture failed")
            }
    }

    /* ---------------- Gallery Pickers ---------------- */

    fun pickSingleImage() {
        singleImagePicker.launch(
            PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly)
                .build()
        )
    }

    fun pickMultipleImages() {
        multipleImageVideoPicker.launch(
            PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly)
                .build()
        )
    }

    fun pickSingleVideo() {
        singleVideoPicker.launch(
            PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.VideoOnly)
                .build()
        )
    }

    fun pickMultipleImagesAndVideos() {
        multipleImageVideoPicker.launch(
            PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                .build()
        )
    }

    /* ---------------- Camera ---------------- */

    fun openCameraForImage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, "IMG_${System.currentTimeMillis()}.jpg")
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Android_Starter")
            }

            cameraImageUri =
                activity.contentResolver.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    values
                )

            if (cameraImageUri != null)
                cameraImageLauncher.launch(cameraImageUri)
            else callback.onError("Failed to create image uri")

        } else {
            val file = createLegacyImageFile() ?: run {
                callback.onError("Failed to create image file")
                return
            }

            cameraImagePath = file.absolutePath
            cameraImageUri = FileProvider.getUriForFile(
                activity,
                "${BuildConfig.APPLICATION_ID}.provider",
                file
            )

            cameraImageLauncher.launch(cameraImageUri)
        }
    }

    fun openCameraForVideo() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

            val values = ContentValues().apply {
                put(MediaStore.Video.Media.DISPLAY_NAME, "VID_${System.currentTimeMillis()}.mp4")
                put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
                put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/Android_Starter")
            }

            cameraVideoUri =
                activity.contentResolver.insert(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    values
                )

            if (cameraVideoUri != null)
                cameraVideoLauncher.launch(cameraVideoUri)
            else callback.onError("Failed to create video uri")

        } else {
            val file = createLegacyVideoFile() ?: run {
                callback.onError("Failed to create video file")
                return
            }

            cameraVideoPath = file.absolutePath
            cameraVideoUri = FileProvider.getUriForFile(
                activity,
                "${BuildConfig.APPLICATION_ID}.provider",
                file
            )

            cameraVideoLauncher.launch(cameraVideoUri)
        }
    }

    /* ---------------- Legacy Files ---------------- */

    private fun createLegacyImageFile(): File? = try {
        val dir = File(
            activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
            "Android_Starter"
        )
        if (!dir.exists()) dir.mkdirs()
        File(dir, "IMG_${System.currentTimeMillis()}.jpg").apply { createNewFile() }
    } catch (e: Exception) {
        null
    }

    private fun createLegacyVideoFile(): File? = try {
        val dir = File(
            activity.getExternalFilesDir(Environment.DIRECTORY_MOVIES),
            "Android_Starter"
        )
        if (!dir.exists()) dir.mkdirs()
        File(dir, "VID_${System.currentTimeMillis()}.mp4").apply { createNewFile() }
    } catch (e: Exception) {
        null
    }

    /* ---------------- Utilities ---------------- */

    fun getRealPathFromUri(uri: Uri): String? =
        FilePath.getPath(activity, uri)

    companion object {
        fun copyUriToCache(context: Context, uri: Uri): String? = try {
            val mime = context.contentResolver.getType(uri)
            val ext = mime?.substringAfter("/") ?: "tmp"
            val file = File(context.cacheDir, "media_${System.currentTimeMillis()}.$ext")

            context.contentResolver.openInputStream(uri).use { input ->
                FileOutputStream(file).use { output ->
                    input?.copyTo(output)
                }
            }
            file.absolutePath
        } catch (e: Exception) {
            null
        }
    }
}


/// Uses
/*
<provider
android:name="androidx.core.content.FileProvider"
android:authorities="${applicationId}.provider"
android:exported="false"
android:grantUriPermissions="true">
<meta-data
android:name="android.support.FILE_PROVIDER_PATHS"
android:resource="@xml/file_paths" />
</provider>


<?xml version="1.0" encoding="utf-8"?>
<paths>
<external -files-path name="external_files" path="." />
<cache-path name="cache" path="." />
</paths>


class CreatePostActivity : AppCompatActivity(),
    MediaPickerHelper.MediaPickerCallback {

    private lateinit var mediaPicker: MediaPickerHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_post)

        mediaPicker = MediaPickerHelper(this, this)

        findViewById<View>(R.id.btnGalleryImage).setOnClickListener {
            mediaPicker.pickSingleImage()
        }

        findViewById<View>(R.id.btnGalleryVideo).setOnClickListener {
            mediaPicker.pickSingleVideo()
        }

        findViewById<View>(R.id.btnCameraImage).setOnClickListener {
            mediaPicker.openCameraForImage()
        }

        findViewById<View>(R.id.btnCameraVideo).setOnClickListener {
            mediaPicker.openCameraForVideo()
        }
    }

    // ---------------- CALLBACKS ----------------

    override fun onMediaSelected(uris: List<Uri>) {
        // Gallery image / video
        val uri = uris.first()
        Log.d("MediaPicker", "Gallery URI = $uri")
    }

    override fun onCameraImageCaptured(uri: Uri, path: String) {
        Log.d("MediaPicker", "Camera image path = $path")
        val file = File(path)
    }

    override fun onCameraVideoCaptured(uri: Uri, path: String) {
        Log.d("MediaPicker", "Camera video path = $path")
        val file = File(path)
    }

    override fun onError(error: String) {
        Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
    }
}

*/
