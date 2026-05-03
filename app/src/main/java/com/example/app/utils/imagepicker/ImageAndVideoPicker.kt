package com.example.app.utils.imagepicker

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.*
import android.media.ExifInterface
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.example.app.BuildConfig
import com.example.app.R
import com.example.app.databinding.DialogSelectImageBinding
import com.example.app.di.App
import com.example.app.ui.base.BaseActivity
import com.example.app.utils.DialogUtils
import com.example.app.utils.imagepicker.Utils.APP_DIRECTORY
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

class ImageAndVideoPicker : BottomSheetDialogFragment() {

    /**
     * *****************
     */
    internal var isVideo = false
    internal var isImage = false
    internal var isDocument = false
    internal var isCustomCrop = false
    internal var isMultipleSelection = false
    internal var imagePickerResult: ImageVideoPickerResult? = null
    lateinit var binding: DialogSelectImageBinding

    fun pickImage(isSelect: Boolean): ImageAndVideoPicker {
        isImage = isSelect
        return this
    }

    fun pickVideo(isSelect: Boolean): ImageAndVideoPicker {
        isVideo = isSelect
        return this
    }

    fun pickDocument(isSelect: Boolean): ImageAndVideoPicker {
        isDocument = isSelect
        return this
    }

    fun allowMultiple(): ImageAndVideoPicker {
        isMultipleSelection = true
        return this
    }

    fun setResult(imagePickerResult: ImageVideoPickerResult?): ImageAndVideoPicker {
        this.imagePickerResult = imagePickerResult
        return this
    }

    /**
     * *****************
     */

    internal var selectedImage: Uri? = null
    private var isclicked: Boolean = false
    private var selectedImagePath: String? = null
    internal lateinit var imageUri: Uri
    private var seconds = 0
    internal var mCurrentPhotoPath: String? = null

    // length of the random string.
    private val randomFileName: String
        get() {
            val SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890"
            val salt = StringBuilder()
            val rnd = Random()
            while (salt.length < 10) {
                val index = (rnd.nextFloat() * SALTCHARS.length).toInt()
                salt.append(SALTCHARS[index])
            }

            try {
                salt.append(SimpleDateFormat("yyMMddhhmmssMs", Locale.US).format(Date()))
            } catch (e: Exception) {
            }

            return salt.toString()

        }

    private val filename: String
        get() {
            val file = File(
                context?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)?.path,
                "$APP_DIRECTORY"
            )
            if (!file.exists()) {
                file.mkdirs()
            }
            return file.absolutePath + "/" + System.currentTimeMillis() + ".jpg"
        }

    fun setImageCallBack(imageVideoPickerResult: ImageVideoPickerResult?) {
        /*this.imageCallBack = imageCallBack*/
        this.imagePickerResult = imageVideoPickerResult
    }

    fun setCustomCrop(customCrop: Boolean) {
        isCustomCrop = customCrop
    }

    //camera pick image
    lateinit var takePhoto: ActivityResultLauncher<Uri?>
    val cameraImageFileName: String
        get() = UUID.randomUUID().toString() + ".jpg"
    var cameraImageFilePath: String = ""

    //permission  ===============================
    private var readPermissionGranted = false
    private var writePermissionGranted = false
    private var cameraPermissionGranted = false
    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>

    val hasReadPermission: Boolean
        get() {
            return if (Build.VERSION.SDK_INT <= 32) {
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.READ_MEDIA_IMAGES
                ) == PackageManager.PERMISSION_GRANTED
            }
        }

    val hasWritePermission: Boolean
        get() {
            return ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }

    val minSdk29: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

    val hasCameraPermission: Boolean
        get() {
            return ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        }

    //==========================================

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme)

        takePhoto = registerForActivityResult(ActivityResultContracts.TakePicture()) { result ->
            if (result) {
                imagePickerResult?.onImagesSelected(arrayListOf(cameraImageFilePath))
                dismiss()
            } else {
                dismiss()
                imagePickerResult?.onFail("")
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = DialogSelectImageBinding.inflate(inflater, container, false)
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()

        permissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                readPermissionGranted = if (Build.VERSION.SDK_INT <= 32) {
                    permissions[Manifest.permission.READ_EXTERNAL_STORAGE]
                        ?: readPermissionGranted
                } else {
                    permissions[Manifest.permission.READ_MEDIA_IMAGES]
                        ?: readPermissionGranted
                }

                writePermissionGranted = permissions[Manifest.permission.WRITE_EXTERNAL_STORAGE]
                    ?: writePermissionGranted
                cameraPermissionGranted = permissions[Manifest.permission.CAMERA]
                    ?: cameraPermissionGranted

                val showStorageSettings =
                    (!readPermissionGranted && !shouldShowRequestPermissionRationale(
                        if (Build.VERSION.SDK_INT <= 32)
                            Manifest.permission.READ_EXTERNAL_STORAGE
                        else
                            Manifest.permission.READ_MEDIA_IMAGES
                    )) || (!writePermissionGranted && !shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE))

                val showCameraSettings = !cameraPermissionGranted && !shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)

                when {
                    showCameraSettings -> openSettingsDialogForPermission(isCamera = true)
                    showStorageSettings -> openSettingsDialogForPermission()
                }
            }
        updateOrRequestPermission()

        // view listeners
        binding.apply {
            imageViewCamera.setOnClickListener { this@ImageAndVideoPicker.onClick(it) }
            imageViewGallery.setOnClickListener { this@ImageAndVideoPicker.onClick(it) }
            imageViewVideoCamera.setOnClickListener { this@ImageAndVideoPicker.onClick(it) }
            imageViewVideoGallery.setOnClickListener { this@ImageAndVideoPicker.onClick(it) }
            textViewUploadDocument.setOnClickListener { this@ImageAndVideoPicker.onClick(it) }
        }
    }

    private fun updateOrRequestPermission() {
        /*val hasReadPermission = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED

        val hasWritePermission = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED

        val minSdk29 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

        val hasCameraPermission = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED*/

        readPermissionGranted = hasReadPermission
        writePermissionGranted = hasWritePermission || minSdk29
        cameraPermissionGranted = hasCameraPermission

        val permissionsToRequest = mutableListOf<String>()
        if (!writePermissionGranted) {
            permissionsToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        if (!readPermissionGranted) {

            if (Build.VERSION.SDK_INT <= 32) {
                permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            } else {
                permissionsToRequest.add(Manifest.permission.READ_MEDIA_IMAGES)
            }
        }
        if (!cameraPermissionGranted) {
            permissionsToRequest.add(Manifest.permission.CAMERA)
        }
        if (permissionsToRequest.isNotEmpty()) {
            permissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }

    /*private fun savePhotoToExternalStorage(displayName: String, bmp: Bitmap): Boolean {
        val imageCollection = sdk29AndUp {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } ?: MediaStore.Images.Media.EXTERNAL_CONTENT_URI

        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "$displayName.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.WIDTH, bmp.width)
            put(MediaStore.Images.Media.HEIGHT, bmp.height)
        }
        return try {
            contentResolver.insert(imageCollection, contentValues)?.also { uri ->
                contentResolver.openOutputStream(uri).use { outputStream ->
                    if (!bmp.compress(Bitmap.CompressFormat.JPEG, 95, outputStream)) {
                        throw IOException("Couldn't save bitmap")
                    }
                }
            } ?: throw IOException("Couldn't create MediaStore entry")
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }*/

    private fun initUI() = with(binding) {
        if (isImage) {
            textViewLabelImage.visibility = View.VISIBLE
            constrainLayoutImage.visibility = View.VISIBLE
        } else {
            textViewLabelImage.visibility = View.GONE
            constrainLayoutImage.visibility = View.GONE
        }

        if (isVideo) {
            textViewLabelVideo.visibility = View.VISIBLE
            constrainLayoutVideo.visibility = View.VISIBLE
        } else {
            textViewLabelVideo.visibility = View.GONE
            constrainLayoutVideo.visibility = View.GONE
        }

        if (isDocument) {
            textViewUploadDocument.visibility = View.VISIBLE
        } else {
            textViewUploadDocument.visibility = View.GONE
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        isclicked = false

        when (requestCode) {
            RequestCode.REQUEST_PICK_DOC -> {
                if (data != null) {
                    val selectedDocUri = data.data
                    val docPath = Utils.getFileFromStorage(requireContext(), selectedDocUri!!)
                    imagePickerResult?.onDocumentSelected(arrayListOf(docPath ?: ""))
                    dismiss()
                }
            }
            RequestCode.REQUEST_PICK_DOC_MULTIPLE -> {
                if (data != null) {
                    imagePickerResult?.onDocumentSelected(getSelectedDocs(data) as ArrayList<String>)
                    dismiss()
                }
            }
            RequestCode.REQUEST_TAKE_PHOTO -> {
                if (mCurrentPhotoPath != null && resultCode == Activity.RESULT_OK) {
                    cropImageFromPath(mCurrentPhotoPath)
                }
            }
            RequestCode.SELECT_SINGLE_IMAGE -> {
                if (data != null) {
                    val selectedImage = data.data
                    if (selectedImage != null) {

                        if (selectedImage.toString()
                                .startsWith("content://com.google.android.apps.photos.content")
                        ) {

                            /**
                             * when Uri is from google photos which are not in local device then Uri is not from local device then
                             * pick image from as per that
                             */

                            var bitmap: Bitmap? = null
                            try {
                                bitmap = getBitmapFromUri(selectedImage)
                            } catch (e: IOException) {
                                e.printStackTrace()
                            }

                            if (bitmap != null) {
                                dismiss()

                                mCurrentPhotoPath = storeImageToCache(bitmap)
                                imagePickerResult?.onImagesSelected(
                                    arrayListOf(
                                        mCurrentPhotoPath
                                            ?: ""
                                    )
                                )
                                /*cropImageFromPath(mCurrentPhotoPath)*/
                            } else {
                                imagePickerResult!!.onFail("Ooops!")
                            }

                        } else {

                            val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
                            val cursor = requireActivity().contentResolver.query(
                                selectedImage,
                                filePathColumn,
                                null,
                                null,
                                null
                            )
                            if (cursor != null) {
                                cursor.moveToFirst()
                                val columnIndex = cursor.getColumnIndex(filePathColumn[0])
                                mCurrentPhotoPath = cursor.getString(columnIndex)
                                cursor.close()

                                if (mCurrentPhotoPath != null) {

                                    if (mCurrentPhotoPath!!.contains(".gif")) {
                                        dismiss()
                                        imagePickerResult!!.onFail("Oops!")
                                    } else {
                                        dismiss()
                                        imagePickerResult?.onImagesSelected(
                                            arrayListOf(
                                                mCurrentPhotoPath
                                                    ?: ""
                                            )
                                        )
                                    }
                                    /*cropImageFromPath(mCurrentPhotoPath)*/
                                }

                            }
                        }


                    }

                } else{
                    dismiss()
                }

            }

            RequestCode.CROP_IMAGE_ACTIVITY_REQUEST_CODE -> {
                if (data != null) {
                    if (data.extras != null) {
                        if (data.extras!!.getString("ImageData") != null
                            && !data.extras!!.getString("ImageData")!!.isEmpty()
                        ) {
                            if (imagePickerResult != null) {
                                val imagePath = data.extras!!.getString("ImageData")
                                val compressedImagePath = compressImage(imagePath!!)
                                if (compressedImagePath != null)
                                    imagePickerResult!!.onImagesSelected(
                                        arrayListOf(
                                            compressedImagePath
                                        )
                                    )
                                /*imagePickerResult!!.onResult(compressedImagePath)*/
                            }
                            dismiss()
                        }
                    }
                }
            }

            RequestCode.TAKE_CAMERA_VIDEO -> {
                try {
                    selectedImage = imageUri /*data!!.data*/
                    val path = FileUtils.getFilePathFromURI(activity, selectedImage)
                    /*val imagePicker = ImagePickerPath()
                    imagePicker.setPick(true)
                    imagePicker.setVideo(true)
                    imagePicker.setImagePath(path)
                    if (imageCallBack != null)
                        imageCallBack!!.sendImage(imagePicker)*/

                    imagePickerResult!!.onVideoSelected(arrayListOf(path))
                    /*imagePickerResult!!.onResult(path)*/
                    dismiss()

                    /*final boolean isNoget = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N;
                                File sdImageMainDirectory = new File(selectedImagePath);
                                if (isNoget) {
                                    selectedImage = FileProvider.getUriForFile(getActivity(),APP_PROVIDER, sdImageMainDirectory);
                                } else {
                                    selectedImage = Uri.fromFile(sdImageMainDirectory);
                                }
                                startActivityForResult(new Intent(getActivity(), TrimmerActivity.class)
                                        .putExtra(TrimmerActivity.EXTRA_VIDEO_PATH, selectedImagePath).putExtra(TrimmerActivity.VIDEO_LENGTH, toCheckVideoLength(selectedImage)), TRIIM_VIDEO);
                                        */

                } catch (e: Exception) {
                    Log.d("ERROR", ":::" + e.message)
                }

            }

            RequestCode.SELECT_SINGLE_VIDEO -> {

                try {
                    val uri = data!!.data
                    val path = FileUtils.getPath(activity, uri)

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            TRIIM_VIDEO -> {

                /* if (data != null) {
                     val uri = Uri.parse(data.getStringExtra(TrimmerActivity.VIDEO_URI))
                     val path = FileUtils.getPath(activity, uri)

                     val imagePicker = ImagePickerPath()
                     imagePicker.setPick(true)
                     imagePicker.setVideo(true)
                     imagePicker.setImagePath(path)
                     if (imageCallBack != null)
                         imageCallBack!!.sendImage(imagePicker)
                     dismiss()
                 }*/
            }

            RequestCode.SELECT_VIDEOS_KITKAT_MULTIPLE, RequestCode.SELECT_VIDEOS_MULTIPLE -> {
                // when single video selected then crop
                if (data != null) {
                    handleSelectedVideos(data)
                }
            }

            RequestCode.SELECT_IMAGES_KITKAT_MULTIPLE, RequestCode.SELECT_IMAGES_MULTIPLE -> {
                if (data != null) {
                    handleSelectedImages(data)
                    /*imagePickerResult?.onMultipleImagesSelected(
                            getSelectedImages(requestCode, data) as ArrayList<String>)
                    dismiss()*/
                }
            }

            else -> {
                Log.d("ERROR", "ImageAndVideoPicker :: Invalid request code")
                dismiss()
            }

        }
    }

    private fun getSelectedDocs(data: Intent): List<String> {

        val result = ArrayList<String>()

        val clipData = data.clipData
        if (clipData != null) {
            for (i in 0 until clipData.itemCount/*int i=0;i<clipData.getItemCount();i++*/) {
                val videoItem = clipData.getItemAt(i)
                val docUri = videoItem.uri
                docUri?.let {
                    val filePath = Utils.getFileFromStorage(requireContext(), it)
                    /*FileUtils.getPath(context, it)*/
                    /*MultipleMediaSelectionUtils.getPath(context, videoURI)*/
                    result.add(filePath ?: "")
                }

            }
        } else {
            val docUri = data.data
            docUri?.let {
                val filePath = Utils.getFileFromStorage(requireContext(), it)
                /*FileUtils.getPath(context, it)*/
                /*MultipleMediaSelectionUtils.getPath(context, videoURI)*/
                result.add(filePath ?: "")
            }
        }

        return result
    }


    private fun getSelectedVideos(requestCode: Int, data: Intent): List<String> {
        val result = ArrayList<String>()
        val clipData = data.clipData
        if (clipData != null) {
            for (i in 0 until clipData.itemCount/*int i=0;i<clipData.getItemCount();i++*/) {
                val videoItem = clipData.getItemAt(i)
                val videoURI = videoItem.uri
                videoURI?.let {
                    val filePath = Utils.getFileFromStorage(requireContext(), it)
                    /*FileUtils.getPath(context, it)*/
                    /*MultipleMediaSelectionUtils.getPath(context, videoURI)*/
                    result.add(filePath ?: "")
                }
            }
        } else {
            val videoURI = data.data
            videoURI?.let {
                val filePath = Utils.getFileFromStorage(requireContext(), it)
                /*FileUtils.getPath(context, it)*/
                /*MultipleMediaSelectionUtils.getPath(context, videoURI)*/
                result.add(filePath ?: "")
            }
        }
        return result
    }

    private fun getSelectedImages(requestCode: Int, data: Intent): List<String> {
        val result = ArrayList<String>()
        val clipData = data.clipData
        if (clipData != null) {
            for (i in 0 until clipData.itemCount/*int i=0;i<clipData.getItemCount();i++*/) {
                val imageItem = clipData.getItemAt(i)
                val imageURI = imageItem.uri
                imageURI?.let {
                    val filePath = Utils.getFileFromStorage(requireContext(), it)
                    /*FileUtils.getPath(context, it)*/
                    /*MultipleMediaSelectionUtils.getPath(context, imageURI)*/
                    result.add(filePath ?: "")
                }
            }
        } else {
            val imageURI = data.data
            imageURI?.let {
                val filePath = Utils.getFileFromStorage(requireContext(), it)
                /*FileUtils.getPath(context, imageURI)*/
                /*MultipleMediaSelectionUtils.getPath(context, videoURI)*/
                result.add(filePath ?: "")
            }
        }
        return result
    }


    private fun handleSelectedImages(data: Intent) {
        val result = ArrayList<String>()
        val resultUri = ArrayList<Uri>()
        val clipData = data.clipData
        if (clipData != null) {
            for (i in 0 until clipData.itemCount/*int i=0;i<clipData.getItemCount();i++*/) {

                val imageItem = clipData.getItemAt(i)
                val imageURI = imageItem.uri
                /*resultUri.add(imageURI)
                imagePickerResult?.onImagesUriSelected(resultUri)
                dismiss()*/
                imageURI?.let {
                    (activity as BaseActivity).toggleLoader(true)
                    Utils.getFileFromStorage(requireActivity(), it) { path ->
                        result.add(path)
                        if (i == clipData.itemCount - 1) {
                            (activity as BaseActivity).toggleLoader(false)
                            imagePickerResult?.onImagesSelected(result)
                            dismiss()
                        }
                    }
                }
            }
        } else {
            val imageURI = data.data
            /*imageURI?.let { resultUri.add(it) }
            imagePickerResult?.onImagesUriSelected(resultUri)
            dismiss()*/
            imageURI?.let {
                Utils.getFileFromStorage(requireActivity(), it) { path ->
                    result.add(path)
                    imagePickerResult?.onImagesSelected(result)
                    dismiss()
                }
            }
        }
    }

    private fun handleSelectedVideos(data: Intent) {
        val result = ArrayList<String>()
        val clipData = data.clipData
        if (clipData != null) {
            for (i in 0 until clipData.itemCount/*int i=0;i<clipData.getItemCount();i++*/) {
                val videoUri = clipData.getItemAt(i)
                val imageURI = videoUri.uri
                imageURI?.let {
                    Utils.getFileFromStorage(requireContext(), it) { path ->
                        result.add(path)
                        if (i == clipData.itemCount - 1) {
                            imagePickerResult?.onVideoSelected(result)
                            dismiss()
                        }
                    }
                }
            }
        } else {
            val videoUri = data.data
            videoUri?.let {
                Utils.getFileFromStorage(requireContext(), it) { path ->
                    result.add(path)
                    imagePickerResult?.onVideoSelected(result)
                    dismiss()
                }
            }
        }
    }

    fun toCheckVideoLength(data: Uri): Int {
        try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(requireActivity().application, data)
            val mp = MediaPlayer.create(requireActivity().baseContext, data)
            val millis = mp.duration
            val video = FileUtils.getPath(activity, data)
            seconds = millis / 1000
            return if (seconds > 60) {
                60
            } else {
                seconds
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return 0
        }
    }


    @Throws(IOException::class)
    private fun getBitmapFromUri(uri: Uri): Bitmap {
        val parcelFileDescriptor = requireContext().contentResolver.openFileDescriptor(uri, "r")
        val fileDescriptor = parcelFileDescriptor!!.fileDescriptor
        val image = BitmapFactory.decodeFileDescriptor(fileDescriptor)
        parcelFileDescriptor.close()
        return image
    }

    fun getCompressedBitmapFromPath(imagePath: String?): Bitmap {
        val bmOptions = BitmapFactory.Options()
        var bitmap = BitmapFactory.decodeFile(imagePath, bmOptions)
//       bitmap = Bitmap.createScaledBitmap(bitmap, 360, 360, true)
        var out = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        var compressedBitmap = BitmapFactory.decodeStream(ByteArrayInputStream(out.toByteArray()))
        return compressedBitmap
    }

    fun storeImageToCache(data: Bitmap): String? {
        var thumbnail: Bitmap? = null
        try {
            val dateTime = Date()
            thumbnail = data
            val bytes = ByteArrayOutputStream()
            val filenamePath = /*"tmp2" + dateTime.toString()*/ randomFileName + ".jpg"
            thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, bytes)
            val outputDir = requireContext().cacheDir
            val file = File(outputDir.path + "/" + filenamePath)
            file.createNewFile()
            val fo = FileOutputStream(file)
            fo.write(bytes.toByteArray())
            fo.close()
            return file.absolutePath.toString()
        } catch (e: Exception) {
            // TODO Auto-generated catch block
            e.printStackTrace()
            return null
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray,
    ) {
        /*super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            STORAGE_PERMISSION -> if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // selectImage();
                return
            } else {
                dismiss()
            }
        }*/

        if (requestCode == REQUEST_CAMERA_PERMISSION) {

            if (PermissionUtils.verifyPermissions(grantResults)) {
                dispatchTakePictureIntent()
            } else {
                setUpAlertDialog("Allow camera permission")
            }

        } else if (requestCode == REQUEST_GALLERY_PERMISSION) {

            if (PermissionUtils.verifyPermissions(grantResults)) {
                openGallory()
            } else {
                setUpAlertDialog("Allow storage permission")
            }

        } else if (requestCode == REQUEST_CAMERA_VIDEO_PERMISSION) {

            if (PermissionUtils.verifyPermissions(grantResults)) {
                takeCameraVideo()
            } else {
                setUpAlertDialog("Allow camera permission")
            }

        } else if (requestCode == REQUEST_GALLERY_VIDEO_PERMISSION) {

            if (PermissionUtils.verifyPermissions(grantResults)) {
                openVideoGallery()
            } else {
                setUpAlertDialog("Allow storage permission")
            }

        }

    }

    private fun setUpAlertDialog(message: String) {
        val alert = AlertDialog.Builder(requireActivity())
            .setPositiveButton("ok", DialogInterface.OnClickListener { dialogInterface, i ->
                dialogInterface.dismiss()
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.data = Uri.parse("package:${BuildConfig.APPLICATION_ID}")
                startActivity(intent)
            })
            .setMessage(message)
            .setTitle(R.string.app_name)
            .create()

        alert.show()
    }

    fun selectImage() {
        if (!isclicked) {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, SELECT_FILE1)
        }
        isclicked = true
    }

    override fun show(manager: FragmentManager, tag: String?) {
        if (manager.findFragmentByTag(tag) == null) {
            super.show(manager, tag)
        }
    }

    override fun dismiss() {
        super.dismissAllowingStateLoss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    fun onClick(view: View) {
        when (view.id) {
            R.id.textViewUploadDocument -> {
                if (Build.VERSION.SDK_INT >= 23) {

                    if (ActivityCompat.checkSelfPermission(
                            requireActivity(),
                            Manifest.permission.READ_EXTERNAL_STORAGE
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        requestPermissions(PERMISSIONS_DOCUMENT, REQUEST_CAMERA_PERMISSION)
                    } else {
                        pickDocument()
                    }

                } else
                    pickDocument()
            }

            R.id.imageViewCamera -> {

                if (Build.VERSION.SDK_INT >= 23) {

                    if (!cameraPermissionGranted) {
                        updateOrRequestPermission()
//                        openSettingsDialogForPermission(isCamera = true, isVideo = false)
                    } else {
                        dispatchTakePictureIntent()
                    }

                    /*if (ActivityCompat.checkSelfPermission(
                            requireActivity(),
                            Manifest.permission.CAMERA
                        ) != PackageManager.PERMISSION_GRANTED
                        || ActivityCompat.checkSelfPermission(
                            requireActivity(),
                            Manifest.permission.READ_EXTERNAL_STORAGE
                        ) != PackageManager.PERMISSION_GRANTED
                    *//*|| ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED*//*) {
                        requestPermissions(PERMISSIONS_CAMERA, REQUEST_CAMERA_PERMISSION)
                    } else {
                        dispatchTakePictureIntent()
                    }*/

                } else
                    dispatchTakePictureIntent()
            }

            R.id.imageViewVideoCamera -> {
                if (Build.VERSION.SDK_INT >= 23) {

                    if (!cameraPermissionGranted) {
                        updateOrRequestPermission()
                    } else {
                        takeCameraVideo()
                    }

                    /*if (ActivityCompat.checkSelfPermission(
                            requireActivity(),
                            Manifest.permission.CAMERA
                        ) != PackageManager.PERMISSION_GRANTED
                        || ActivityCompat.checkSelfPermission(
                            requireActivity(),
                            Manifest.permission.READ_EXTERNAL_STORAGE
                        ) != PackageManager.PERMISSION_GRANTED
                        *//*|| ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED*//*
                        || ActivityCompat.checkSelfPermission(
                            requireActivity(),
                            Manifest.permission.RECORD_AUDIO
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {

                        requestPermissions(
                            PERMISSIONS_RECORD_VIDEO,
                            REQUEST_CAMERA_VIDEO_PERMISSION
                        )

                    } else {

                        takeCameraVideo()

                    }*/

                } else
                    takeCameraVideo()
            }


            R.id.imageViewGallery -> {

                if (Build.VERSION.SDK_INT >= 23) {

                    if (!readPermissionGranted || !writePermissionGranted) {
                        updateOrRequestPermission()
//                        openSettingsDialogForPermission(isCamera = false, isVideo = false)

                    } else {
                        openGallory()
                    }

                    /*if (ActivityCompat.checkSelfPermission(
                            requireActivity(),
                            Manifest.permission.READ_EXTERNAL_STORAGE
                        ) != PackageManager.PERMISSION_GRANTED
                    *//*|| ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED*//*) {
                        requestPermissions(PERMISSIONS_STORAGE, REQUEST_GALLERY_PERMISSION)
                    } else {
                        openGallory()
                    }*/

                } else {
                    openGallory()
                }
            }

            R.id.imageViewVideoGallery -> {

                if (!readPermissionGranted || !writePermissionGranted) {
                    updateOrRequestPermission()
                } else {
                    openVideoGallery()
                }

                /*if (ActivityCompat.checkSelfPermission(
                    requireActivity(),
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            *//*|| ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED*//*) {
                requestPermissions(PERMISSIONS_STORAGE, REQUEST_GALLERY_VIDEO_PERMISSION)
            } else {
                openVideoGallery()
            }*/

            }
        }
    }

    private fun pickDocument() {
        val allSupportedDocumentsTypesToExtensions = mapOf(
            "application/msword" to ".doc",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document" to ".docx",
            "application/pdf" to ".pdf",
            "text/rtf" to ".rtf",
            "application/rtf" to ".rtf",
            "application/x-rtf" to ".rtf",
            "text/richtext" to ".rtf",
            "text/plain" to ".txt"
        )
        val supportedMimeTypes = allSupportedDocumentsTypesToExtensions.keys.toTypedArray()
        val openDocumentIntent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
            putExtra(Intent.EXTRA_MIME_TYPES, supportedMimeTypes)
            if (isMultipleSelection) {
                putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            }
        }

        if (isMultipleSelection) {
            startActivityForResult(openDocumentIntent, RequestCode.REQUEST_PICK_DOC_MULTIPLE)
        } else {
            startActivityForResult(openDocumentIntent, RequestCode.REQUEST_PICK_DOC)
        }
    }

    private fun openVideoGallery() {
        if (!isMultipleSelection) {
            // When single selection only
            val type = "video/*"
            val cameraVideoIntent =
                Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            cameraVideoIntent.type = type
            cameraVideoIntent.putExtra("return-data", true)
            try {
                startActivityForResult(cameraVideoIntent, RequestCode.SELECT_SINGLE_VIDEO)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            // when multiple selection enabled
            if (Build.VERSION.SDK_INT < 19) {
                val intent = Intent()
                intent.type = "video/mp4"
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                intent.action = Intent.ACTION_GET_CONTENT
                startActivityForResult(
                    Intent.createChooser(intent, "Select videos"),
                    RequestCode.SELECT_VIDEOS_MULTIPLE
                )
            } else {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                intent.addCategory(Intent.CATEGORY_OPENABLE)
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                intent.type = "video/mp4"
                startActivityForResult(intent, RequestCode.SELECT_VIDEOS_KITKAT_MULTIPLE)
            }
        }
    }

    private fun takeCameraVideo() {
        getSaveVideoUri()
        // start default camera
        Log.d(":::::", "" + selectedImagePath!!)
        val cameraIntent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        cameraIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 60)
        startActivityForResult(cameraIntent, RequestCode.TAKE_CAMERA_VIDEO)
    }

    /*    private fun openGallory() {
            if (!isMultipleSelection) {
                try {
                    val i = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    startActivityForResult(i, RequestCode.SELECT_SINGLE_IMAGE)
                } catch (e: Exception) {
                    //e.printStackTrace();
                    dismiss()
                    imagePickerResult!!.onFail("Fail to open gallery")
                }
            } else {
                // when multiple selection enabled
                if (Build.VERSION.SDK_INT < 19) {
                    val intent = Intent()
                    intent.type = FileUtils.MIME_TYPE_IMAGE
                    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                    intent.action = Intent.ACTION_GET_CONTENT
                    startActivityForResult(
                            Intent.createChooser(intent, "Select images"),
                            RequestCode.SELECT_IMAGES_MULTIPLE
                    )
                } else {
                    val intent =
                            Intent(Intent.ACTION_OPEN_DOCUMENT)*//*Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)*//*
                intent.addCategory(Intent.CATEGORY_OPENABLE)
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                intent.type = FileUtils.MIME_TYPE_IMAGE
                startActivityForResult(intent, RequestCode.SELECT_IMAGES_KITKAT_MULTIPLE)
            }
        }
    }*/

    private fun openGallory() {
        val intent = Intent()

        if (!isMultipleSelection) {
            try {
                // For single selection
                intent.action = Intent.ACTION_PICK
                intent.type = "image/*"  // Ensure only images are shown
                startActivityForResult(intent, RequestCode.SELECT_SINGLE_IMAGE)
            } catch (e: Exception) {
                dismiss()
                imagePickerResult?.onFail("Failed to open gallery")
            }
        } else {
            // For multiple selection
            if (Build.VERSION.SDK_INT < 19) {
                intent.action = Intent.ACTION_GET_CONTENT
                intent.type = "image/*"  // Ensure only images are shown
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                startActivityForResult(Intent.createChooser(intent, "Select images"),
                    RequestCode.SELECT_IMAGES_MULTIPLE
                )
            } else {
                intent.action = Intent.ACTION_OPEN_DOCUMENT
                intent.addCategory(Intent.CATEGORY_OPENABLE)
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                intent.type = "image/*"  // Ensure only images are shown

                // Extra MIME types to make sure only images are shown
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    val mimeTypes = arrayOf("image/jpeg", "image/png", "image/gif", "image/webp")
                    intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
                }

                startActivityForResult(intent, RequestCode.SELECT_IMAGES_KITKAT_MULTIPLE)
            }
        }
    }


    abstract class ImageVideoPickerResult {
        abstract fun onFail(message: String)
        open fun onVideoSelected(list: ArrayList<String>) {}
        open fun onImagesSelected(list: ArrayList<String>) {}
        open fun onDocumentSelected(list: ArrayList<String>) {}
        open fun onImagesUriSelected(list: ArrayList<Uri>) {}
    }

    private fun dispatchTakePictureIntent() {
        val file = File(activity?.filesDir, cameraImageFileName)
        val uri = FileProvider.getUriForFile(
            requireActivity(),
            App.FILE_PROVIDER_AUTHORITY,
            file
        )
        cameraImageFilePath = file.path
        takePhoto.launch(uri)
    }

    private fun savePhotoToInternalStorage(
        filename: String,
        bmp: Bitmap,
        callback: (path: String) -> Unit,
    ) {
        return try {
            activity?.openFileOutput("$filename.jpg", MODE_PRIVATE).use { stream ->
                if (!stream?.let { bmp.compress(Bitmap.CompressFormat.JPEG, 100, it) }!!) {
                    throw IOException("Couldn't save bitmap.")
                }
                callback.invoke(context?.filesDir?.absolutePath + "/" + filename + ".jpg")
            }
        } catch (e: IOException) {
            e.printStackTrace()
            callback.invoke("")
        }
    }


    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        val storageDir = context?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile(
            imageFileName, /* prefix */
            ".jpg", /* suffix */
            storageDir      /* directory */
        )

        mCurrentPhotoPath = image.absolutePath
        return image
    }

    fun getSaveImageUri() {
        try {
            val root = File(
                requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                    .toString() + "/${APP_DIRECTORY}/"
            )
            if (!root.exists()) {
                root.mkdirs()
            }
            val imageName = "image_" + System.currentTimeMillis() + ".png"
            val sdImageMainDirectory = File(root, imageName)
            val isNoget = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
            if (isNoget) {
                imageUri = FileProvider.getUriForFile(
                    requireActivity(),
                    App.FILE_PROVIDER_AUTHORITY,
                    sdImageMainDirectory
                )
                selectedImagePath = sdImageMainDirectory.absolutePath
            } else {
                imageUri = Uri.fromFile(sdImageMainDirectory)
                selectedImagePath = FileUtils.getPath(activity, imageUri)
            }
        } catch (e: Exception) {
            //  DebugLog.d("Incident Photo" + "Error occurred. Please try again later.");
        }
    }


    fun getSaveVideoUri() {
        try {
            val root = File(
                App.mContext?.getExternalFilesDir(Environment.DIRECTORY_MOVIES)
                    .toString() + "/${APP_DIRECTORY}/"
            )
            if (!root.exists()) {
                root.mkdirs()
            }
            val imageName = "video_" + System.currentTimeMillis() + ".mp4"
            val sdImageMainDirectory = File(root, imageName)
            val isNoget = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
            if (isNoget) {
                imageUri = FileProvider.getUriForFile(
                    requireActivity(),
                    App.FILE_PROVIDER_AUTHORITY,
                    sdImageMainDirectory
                )
                selectedImagePath = sdImageMainDirectory.absolutePath
            } else {
                imageUri = Uri.fromFile(sdImageMainDirectory)
                selectedImagePath = FileUtils.getPath(activity, imageUri)
            }
        } catch (e: Exception) {
            //  DebugLog.d("Incident Photo" + "Error occurred. Please try again later.");
        }
    }

    fun getSaveImageUriDestination(): Uri {
        val destin: Uri
        val root = File(
            App.mContext?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                .toString() + "/${APP_DIRECTORY}/"
        )
        if (!root.exists()) {
            root.mkdirs()
        }
        val imageName = System.currentTimeMillis().toString() + ".jpg"
        val sdImageMainDirectory = File(root, imageName)
        destin = Uri.fromFile(sdImageMainDirectory)
        return destin
    }


    /**
     * cropImage
     * This method use UCrop for cropping*/
    /*private fun cropImage(uri: Uri*//*mCurrentPhotoPath: String?*//*) {

        var options: UCrop.Options = UCrop.Options()
        options.setToolbarColor(resources.getColor(R.color.colorPrimary))
        options.setStatusBarColor(resources.getColor(R.color.colorwhite_f6f6f6))
        options.setActiveWidgetColor(resources.getColor(R.color.colorPrimary))
        activity?.let {
            UCrop.of(uri, getSaveImageUriDestination())
                    .withOptions(options)
                    .start(it, this)
        }

        *//*if (mCurrentPhotoPath != null && !mCurrentPhotoPath.isEmpty()) {
            //Log.d("File::",mCurrentPhotoPath);
            val cropImage = CropImage()
            cropImage.setFileName(mCurrentPhotoPath)
                    .setRequestCode(Common.RequestCode.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
                    .start(requireContext(), this)
        }*//*
    }*/

    /** cropImageFromPath
     * This method user crop activity
     */
    private fun cropImageFromPath(mCurrentPhotoPath: String?) {
        if (!mCurrentPhotoPath.isNullOrEmpty()) {
            dismiss()
            /*val bitmap = getCompressedBitmapFromPath(mCurrentPhotoPath)*/
            val compressedImagePath = compressImage(mCurrentPhotoPath!!)
            if (compressedImagePath != null)
                imagePickerResult!!.onImagesSelected(arrayListOf(compressedImagePath))
            /*imagePickerResult!!.onResult(compressedImagePath)*/
        }
    }

    /* private void cropImage(Uri selectUri) {
        UCrop.Options options = new UCrop.Options();
        options.setToolbarColor(getResources().getColor(R.color.colorPrimary));
        options.setStatusBarColor(getResources().getColor(R.color.colorwhite_f6f6f6));


        options.setActiveWidgetColor(getResources().getColor(R.color.colorPrimary));

        UCrop.of(selectUri, getSaveImageUriDestin())
                .withOptions(options)
                .start(getActivity(), this);


      *//*  if (mCurrentPhotoPath != null && !(mCurrentPhotoPath.isEmpty())) {
            //Log.d("File::",mCurrentPhotoPath);
            CropImage cropImage = new CropImage();
            cropImage.setCustomeCrop(true);
            cropImage.setFileName(mCurrentPhotoPath)
                    .setRequestCode(Common.RequestCode.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
                    .start(getContext(), this);
        }*//*

    }*/


    fun getRealPathFromURI(context: Context, contentUri: Uri): String {
        var cursor: Cursor? = null
        try {
            val proj = arrayOf(MediaStore.Images.Media.DATA)
            cursor = context.contentResolver.query(contentUri, proj, null, null, null)
            val column_index = cursor!!.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
            cursor.moveToFirst()
            return cursor.getString(column_index)
        } finally {
            cursor?.close()
        }
    }

    fun compressImage(filePath: String): String {
        /**
         * method requires EXTERNAL_STORAGE permission
         */

        var scaledBitmap: Bitmap? = null

        val options = BitmapFactory.Options()

        //      by setting this field as true, the actual bitmap pixels are not loaded in the memory. Just the bounds are loaded. If
        //      you try the use the bitmap here, you will get null.
        options.inJustDecodeBounds = true
        var bmp = BitmapFactory.decodeFile(filePath, options)

        var actualHeight = options.outHeight
        var actualWidth = options.outWidth

        //      max Height and width values of the compressed image is taken as 816x612

        val maxHeight = 1920.0f//1280.0f;//816.0f;
        val maxWidth = 1080.0f//852.0f;//612.0f;

        var imgRatio = (actualWidth / actualHeight).toFloat()
        val maxRatio = maxWidth / maxHeight

        //      width and height values are set maintaining the aspect ratio of the image

        if (actualHeight > maxHeight || actualWidth > maxWidth) {
            if (imgRatio < maxRatio) {
                imgRatio = maxHeight / actualHeight
                actualWidth = (imgRatio * actualWidth).toInt()
                actualHeight = maxHeight.toInt()
            } else if (imgRatio > maxRatio) {
                imgRatio = maxWidth / actualWidth
                actualHeight = (imgRatio * actualHeight).toInt()
                actualWidth = maxWidth.toInt()
            } else {
                actualHeight = maxHeight.toInt()
                actualWidth = maxWidth.toInt()

            }
        }

        //      setting inSampleSize value allows to load a scaled down version of the original image

        options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight)

        //      inJustDecodeBounds set to false to load the actual bitmap
        options.inJustDecodeBounds = false

        //      this options allow android to claim the bitmap memory if it runs low on memory
        options.inPurgeable = true
        options.inInputShareable = true
        options.inTempStorage = ByteArray(16 * 1024)

        try {
            //          load the bitmap from its path
            bmp = BitmapFactory.decodeFile(filePath, options)
        } catch (exception: OutOfMemoryError) {
            exception.printStackTrace()

        }

        try {
            scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.ARGB_8888)
        } catch (exception: OutOfMemoryError) {
            exception.printStackTrace()
        }

        val ratioX = actualWidth / options.outWidth.toFloat()
        val ratioY = actualHeight / options.outHeight.toFloat()
        val middleX = actualWidth / 2.0f
        val middleY = actualHeight / 2.0f

        val scaleMatrix = Matrix()
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY)

        val canvas = Canvas(scaledBitmap!!)
        canvas.setMatrix(scaleMatrix)
        canvas.drawBitmap(
            bmp,
            middleX - bmp.width / 2,
            middleY - bmp.height / 2,
            Paint(Paint.FILTER_BITMAP_FLAG)
        )

        //      check the rotation of the image and display it properly
        val exif: ExifInterface
        try {
            exif = ExifInterface(filePath)

            val orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION, 0
            )
            Log.d("EXIF", "Exif: $orientation")
            val matrix = Matrix()
            when (orientation) {
                6 -> {
                    matrix.postRotate(90f)
                    Log.d("EXIF", "Exif: $orientation")
                }
                3 -> {
                    matrix.postRotate(180f)
                    Log.d("EXIF", "Exif: $orientation")
                }
                8 -> {
                    matrix.postRotate(270f)
                    Log.d("EXIF", "Exif: $orientation")
                }
            }
            scaledBitmap = Bitmap.createBitmap(
                scaledBitmap, 0, 0,
                scaledBitmap.width, scaledBitmap.height, matrix,
                true
            )
        } catch (e: IOException) {
            e.printStackTrace()
        }

        var out: FileOutputStream? = null
        val filename = filename
        try {
            out = FileOutputStream(filename)

            //          write the compressed bitmap at the destination specified by filename.
            scaledBitmap!!.compress(Bitmap.CompressFormat.JPEG, 85, out)


        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }

        return filename

    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val heightRatio = Math.round(height.toFloat() / reqHeight.toFloat())
            val widthRatio = Math.round(width.toFloat() / reqWidth.toFloat())
            inSampleSize = if (heightRatio < widthRatio) heightRatio else widthRatio
        }
        val totalPixels = (width * height).toFloat()
        val totalReqPixelsCap = (reqWidth * reqHeight * 2).toFloat()
        while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
            inSampleSize++
        }

        return inSampleSize
    }

    fun getSaveVideoFileExt() {
        try {
            val root = File(
                App.mContext?.getExternalFilesDir(Environment.DIRECTORY_MOVIES)
                    .toString() + "/.${APP_DIRECTORY}/"
            )
            if (!root.exists()) {
                root.mkdirs()
            }
            val imageName = "video_" + System.currentTimeMillis() + ".mp4"
            val sdImageMainDirectory = File(root, imageName)
        } catch (e: Exception) {
            //  DebugLog.d("Incident Photo" + "Error occurred. Please try again later.");
        }
    }

    companion object {


        private val TRIIM_VIDEO = 45
        private val PICK_Camera_IMAGE = 2
        private val SELECT_FILE1 = 1
        private val STORAGE_PERMISSION = 3
        val CROP_IMAGE_ACTIVITY_REQUEST_CODE = 203

        fun newInstance(): ImageAndVideoPicker {
            val args = Bundle()
            val fragment = ImageAndVideoPicker()
            fragment.arguments = args
            return fragment
        }
    }

    val REQUEST_CAMERA_PERMISSION = 1
    val REQUEST_GALLERY_PERMISSION = 2
    val REQUEST_CAMERA_VIDEO_PERMISSION = 3
    val REQUEST_GALLERY_VIDEO_PERMISSION = 4

    val PERMISSIONS_STORAGE = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE/*,
            Manifest.permission.WRITE_EXTERNAL_STORAGE*/
    )
    val PERMISSIONS_DOCUMENT = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.READ_EXTERNAL_STORAGE
    )
    val PERMISSIONS_CAMERA = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.READ_EXTERNAL_STORAGE/*,
            Manifest.permission.WRITE_EXTERNAL_STORAGE*/
    )
    val PERMISSIONS_RECORD_VIDEO = arrayOf<String>(
        Manifest.permission.CAMERA,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.RECORD_AUDIO/*,
            Manifest.permission.WRITE_EXTERNAL_STORAGE*/
    )

    object RequestCode {
        const val REQUEST_TAKE_PHOTO = 1
        const val SELECT_SINGLE_IMAGE = 2
        const val SELECT_IMAGES_MULTIPLE = 13
        const val SELECT_IMAGES_KITKAT_MULTIPLE = 14

        const val TAKE_CAMERA_VIDEO = 4
        const val SELECT_SINGLE_VIDEO = 5
        const val SELECT_VIDEOS_MULTIPLE = 11
        const val SELECT_VIDEOS_KITKAT_MULTIPLE = 12

        const val REQUEST_PICK_DOC = 12
        const val REQUEST_PICK_DOC_MULTIPLE = 16

        const val CROP_IMAGE_ACTIVITY_REQUEST_CODE = 203
    }

    private fun openSettingsDialogForPermission(isCamera: Boolean = false, isVideo: Boolean = false) {
        DialogUtils.showAlertDialog(
            context = requireContext(),
            message = if (isCamera) {
                getString(R.string.dialog_open_camera_settings_message)
            } else if (isVideo) {
                getString(R.string.dialog_open_video_settings_message)
            } else {
                getString(R.string.dialog_storage_gallery_open_settings_message)
            },
            positiveButtonText = getString(R.string.dialog_location_open_settings_positive_button),
            negativeButtonText = getString(R.string.dialog_location_open_settings_negative_button),
            onPositiveButtonClicked = {
                it.dismiss()
                //dismiss()
                openSettingsScreen()
            },
            onNegativeButtonClicked = {
                it.dismiss()
                //dismiss()

            })
    }

    private fun openSettingsScreen() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", requireActivity().packageName, null)
        intent.data = uri
        requireActivity().startActivity(intent)
    }
}