package com.example.app.utils.imagepicker

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.graphics.*
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import android.text.TextUtils
import android.util.Log
import com.example.app.di.App
import kotlinx.coroutines.*
import java.io.*
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

object Utils {
    var thumbColumns = arrayOf(MediaStore.Video.Thumbnails.DATA)
    var mediaColumns = arrayOf(MediaStore.Video.Media._ID)
    val DOCUMENTS_DIR = "documents"
    const val APP_DIRECTORY = "App"
    private var screenWidth = 0
    private var screenHeight = 0

    object RequestCode {
        const val REQUEST_TAKE_PHOTO = 1
        const val RESULT_LOAD_IMAGE = 2
        const val REQUEST_IMAGE_AND_VIDEO = 3
        const val REQUEST_FROM_CAMERA = 4
        const val REQUEST_TO_FINISH = 5
        const val REQUEST = 10
        const val REQUEST_TRIM_VIDEO = 11
        const val CROP_IMAGE_ACTIVITY_REQUEST_CODE = 203
        const val VIEW_ADVERTISEMENT = 213
        const val REQUEST_PICK_DOC = 12
        const val REQUEST_PICK_DOC_MULTIPLE = 16
        const val REQUEST_STORAGE_PERMISSION = 13
        const val REQUEST_SELECT_COMPANY = 14
        const val REQUEST_SELECT_SKILL = 15
    }

    val isAndroid5: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP

    val filename: String
        get() {
            val file =
                File(
                    App.Companion.mContext?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)?.path,
                    "$APP_DIRECTORY/Images"
                )
            if (!file.exists()) {
                file.mkdirs()
            }
            return file.absolutePath + "/" + System.currentTimeMillis() + ".jpg"
        }


    fun saveFile(data: ByteArray): File? {
        var file: File? = null
        try {


            file = File(
                App.Companion.mContext?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                    .toString() + "/" + APP_DIRECTORY,
                "img_" + System.currentTimeMillis() + "_.jpeg"
            )
            file.parentFile.mkdirs()
            val imageFileOS = FileOutputStream(file)
            imageFileOS.write(data)
            imageFileOS.flush()
            imageFileOS.close()

        } catch (e: IOException) {
            e.printStackTrace()
        }

        return file
    }

    fun createNewVideoFile(): String {
        val file = File(
            App.Companion.mContext?.getExternalFilesDir(Environment.DIRECTORY_MOVIES)
                .toString() + File.separator + APP_DIRECTORY,
            "snap_" + System.currentTimeMillis() + ".mp4"
        )
        file.parentFile.mkdirs()
        return file.path
    }

    fun saveImage(bitmap: Bitmap): File? {
        try {

            val file = File(
                App.Companion.mContext?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                    .toString() + "/" + APP_DIRECTORY,
                "thumb_" + System.currentTimeMillis() + "_.jpeg"
            )
            file.parentFile.mkdirs()
            val outputStream = BufferedOutputStream(FileOutputStream(file))
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
            return file

        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }

        return null
    }

    fun resizeBitMapImage(filePath: String, targetWidth: Int, targetHeight: Int): Bitmap? {
        var bitMapImage: Bitmap? = null
        try {
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            BitmapFactory.decodeFile(filePath, options)
            var sampleSize = 0.0
            val scaleByHeight =
                Math.abs(options.outHeight - targetHeight) >= Math.abs(options.outWidth - targetWidth)
            if (options.outHeight * options.outWidth * 2 >= 1638) {
                sampleSize =
                    (if (scaleByHeight) options.outHeight / targetHeight else options.outWidth / targetWidth).toDouble()
                sampleSize = Math.pow(2.0, Math.floor(Math.log(sampleSize) / Math.log(2.0))).toInt()
                    .toDouble()
            }
            options.inJustDecodeBounds = false
            options.inTempStorage = ByteArray(128)
            while (true) {
                try {
                    options.inSampleSize = sampleSize.toInt()
                    bitMapImage = BitmapFactory.decodeFile(filePath, options)
                    break
                } catch (ex: Exception) {
                    ex.printStackTrace()
                    try {
                        sampleSize = sampleSize * 2
                    } catch (ex1: Exception) {
                        ex1.printStackTrace()
                    }
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }

        return bitMapImage
    }


    fun compressImage(filePath: String): String {

        var scaledBitmap: Bitmap? = null

        val options = BitmapFactory.Options()

        //      by setting this field as true, the actual bitmap pixels are not loaded in the memory. Just the bounds are loaded. If
        //      you try the use the bitmap here, you will get null.
        options.inJustDecodeBounds = true
        var bmp = BitmapFactory.decodeFile(filePath, options)

        /*try {
            val inputStream = MingoApplication.appContext?.contentResolver?.openInputStream(
                Uri.parse(filename)
            )
            bmp = BitmapFactory.decodeStream(inputStream, null, options)
        } catch (e: FileNotFoundException) {
            // do something
        }*/

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
            if (orientation == 6) {
                matrix.postRotate(90f)
                Log.d("EXIF", "Exif: $orientation")
            } else if (orientation == 3) {
                matrix.postRotate(180f)
                Log.d("EXIF", "Exif: $orientation")
            } else if (orientation == 8) {
                matrix.postRotate(270f)
                Log.d("EXIF", "Exif: $orientation")
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
            scaledBitmap!!.compress(Bitmap.CompressFormat.JPEG, 80, out)

        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }

        return filename
    }

    fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
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


    /**
     * Get a file path from a Uri. This will get the the path for Storage Access
     * Framework Documents, as well as the _data field for the MediaStore and
     * other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri     The Uri to query.
     * @author paulburke
     */
    fun getPath(context: Context, uri: Uri): String? {

        val isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val type = split[0]

                if ("primary".equals(type, ignoreCase = true)) {
                    return App.Companion.mContext?.getExternalFilesDir(
                        Environment.DIRECTORY_PICTURES
                    ).toString() + "/" + split[1]
                }

                // TODO handle non-primary volumes
            } else if (isDownloadsDocument(uri)) {

                val id = DocumentsContract.getDocumentId(uri)
                val contentUri = ContentUris.withAppendedId(
                    Uri.parse("content://downloads/public_downloads"), java.lang.Long.valueOf(id)
                )

                return getDataColumn(context, contentUri, null, null)
            } else if (isMediaDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val type = split[0]

                var contentUri: Uri? = null
                if ("image" == type) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                } else if ("video" == type) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                } else if ("audio" == type) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                }

                val selection = "_id=?"
                val selectionArgs = arrayOf(split[1])

                return getDataColumn(context, contentUri, selection, selectionArgs)
            }// MediaProvider
            // DownloadsProvider
        } else if ("content".equals(uri.scheme, ignoreCase = true)) {
            return getDataColumn(context, uri, null, null)
        } else if ("file".equals(uri.scheme, ignoreCase = true)) {
            return uri.path
        }// File
        // MediaStore (and general)

        return null
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    fun getDataColumn(
        context: Context, uri: Uri?, selection: String?,
        selectionArgs: Array<String>?
    ): String? {

        var cursor: Cursor? = null
        val column = "_data"
        val projection = arrayOf(column)

        try {
            cursor =
                context.contentResolver.query(uri!!, projection, selection, selectionArgs, null)
            if (cursor != null && cursor.moveToFirst()) {
                val column_index = cursor.getColumnIndexOrThrow(column)
                return cursor.getString(column_index)
            }
        } finally {
            if (cursor != null)
                cursor.close()
        }
        return null
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
    }


    fun getFileFromStorage(uri: Uri, context: Context): String? {

        var isImageFromGoogleDrive = false
        var imgPath: String? = null
        val isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT

        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            if ("com.android.externalstorage.documents" == uri.authority) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val type = split[0]

                if ("primary".equals(type, ignoreCase = true)) {
                    imgPath = /*Environment.getExternalStorageDirectory()*/
                        App.Companion.mContext?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                            .toString() + "/" + split[1]
                } else {
                    val DIR_SEPORATOR = Pattern.compile("/")
                    val rv = HashSet<String>()
                    val rawExternalStorage = System.getenv("EXTERNAL_STORAGE")
                    val rawSecondaryStoragesStr = System.getenv("SECONDARY_STORAGE")
                    val rawEmulatedStorageTarget = System.getenv("EMULATED_STORAGE_TARGET")
                    if (TextUtils.isEmpty(rawEmulatedStorageTarget)) {
                        if (TextUtils.isEmpty(rawExternalStorage)) {
                            rv.add("/storage/sdcard0")
                        } else {
                            if (rawExternalStorage != null) {
                                rv.add(rawExternalStorage)
                            }
                        }
                    } else {
                        val rawUserId: String
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
                            rawUserId = ""
                        } else {
                            val path = /*Environment.getExternalStorageDirectory()*/
                                App.Companion.mContext?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!.absolutePath
                            val folders = DIR_SEPORATOR.split(path)
                            val lastFolder = folders[folders.size - 1]
                            var isDigit = false
                            try {
                                Integer.valueOf(lastFolder)
                                isDigit = true
                            } catch (ignored: NumberFormatException) {
                            }

                            rawUserId = if (isDigit) lastFolder else ""
                        }
                        if (TextUtils.isEmpty(rawUserId)) {
                            if (rawEmulatedStorageTarget != null) {
                                rv.add(rawEmulatedStorageTarget)
                            }
                        } else {
                            rv.add(rawEmulatedStorageTarget + File.separator + rawUserId)
                        }
                    }
                    if (!TextUtils.isEmpty(rawSecondaryStoragesStr)) {
                        val rawSecondaryStorages =
                            rawSecondaryStoragesStr?.split(File.pathSeparator.toRegex())
                                ?.dropLastWhile { it.isEmpty() }?.toTypedArray()
                        if (rawSecondaryStorages != null)
                            Collections.addAll(rv, *rawSecondaryStorages)
                    }
                    val temp = rv.toTypedArray()
                    for (i in temp.indices) {
                        val tempf = File(temp[i] + "/" + split[1])
                        if (tempf.exists()) {
                            imgPath = temp[i] + "/" + split[1]
                        }
                    }
                }
            } else if ("com.android.providers.downloads.documents" == uri.authority) {
                val id = DocumentsContract.getDocumentId(uri)
                val contentUri = ContentUris.withAppendedId(
                    Uri.parse("content://downloads/public_downloads"),
                    java.lang.Long.valueOf(id)
                )

                var cursor: Cursor? = null
                val column = "_data"
                val projection = arrayOf(column)
                try {
                    cursor = context.contentResolver.query(contentUri, projection, null, null, null)
                    if (cursor != null && cursor.moveToFirst()) {
                        val column_index = cursor.getColumnIndexOrThrow(column)
                        imgPath = cursor.getString(column_index)
                    }
                } finally {
                    if (cursor != null)
                        cursor.close()
                }
            } else if ("com.android.providers.media.documents" == uri.authority) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val type = split[0]

                var contentUri: Uri? = null
                if ("image" == type) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                } else if ("video" == type) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                } else if ("audio" == type) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                }

                val selection = "_id=?"
                val selectionArgs = arrayOf(split[1])

                var cursor: Cursor? = null
                val column = "_data"
                val projection = arrayOf(column)

                try {
                    cursor = context.contentResolver.query(
                        contentUri!!,
                        projection,
                        selection,
                        selectionArgs,
                        null
                    )
                    if (cursor != null && cursor.moveToFirst()) {
                        val column_index = cursor.getColumnIndexOrThrow(column)
                        imgPath = cursor.getString(column_index)
                    }
                } finally {
                    if (cursor != null)
                        cursor.close()
                }
            } else if ("com.google.android.apps.docs.storage" == uri.authority) {
                isImageFromGoogleDrive = true
            }
        } else if ("content".equals(uri.scheme, ignoreCase = true)) {
            var cursor: Cursor? = null
            val column = "_data"
            val projection = arrayOf(column)

            try {
                cursor = context.contentResolver.query(uri, projection, null, null, null)
                if (cursor != null && cursor.moveToFirst()) {
                    val column_index = cursor.getColumnIndexOrThrow(column)
                    imgPath = cursor.getString(column_index)
                }
            } finally {
                if (cursor != null)
                    cursor.close()
            }

            if ("com.google.android.apps.docs.storage" == uri.authority) {
                isImageFromGoogleDrive = true
            }

        } else if ("file".equals(uri.scheme, ignoreCase = true)) {
            imgPath = uri.path
        }

        if (isImageFromGoogleDrive) {
            try {

                val bitmap =
                    BitmapFactory.decodeStream(context.contentResolver.openInputStream(uri))
                return saveImage(bitmap)!!.absolutePath

            } catch (e: Exception) {
                e.printStackTrace()
            }

        }

        return imgPath
    }

    /*fun getFileFromStorage(context: Context, uri: Uri): String? {

        var isImageFromGoogleDrive = false
        var imgPath: String? = null
        val isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT

        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            if ("com.android.externalstorage.documents" == uri.authority) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val type = split[0]

                if ("primary".equals(type, ignoreCase = true)) {
                    imgPath = BayiseBaseApplication.appContext!!.getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString() + "/" + split[1]
                } else {
                    val DIR_SEPORATOR = Pattern.compile("/")
                    val rv = HashSet<String>()
                    val rawExternalStorage = System.getenv("EXTERNAL_STORAGE")
                    val rawSecondaryStoragesStr = System.getenv("SECONDARY_STORAGE")
                    val rawEmulatedStorageTarget = System.getenv("EMULATED_STORAGE_TARGET")
                    if (TextUtils.isEmpty(rawEmulatedStorageTarget)) {
                        if (TextUtils.isEmpty(rawExternalStorage)) {
                            rv.add("/storage/sdcard0")
                        } else {
                            rv.add(rawExternalStorage ?: "")
                        }
                    } else {
                        val rawUserId: String
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
                            rawUserId = ""
                        } else {
                            val path = BayiseMingoApplication.appContext!!.getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!.absolutePath
                            val folders = DIR_SEPORATOR.split(path)
                            val lastFolder = folders[folders.size - 1]
                            var isDigit = false
                            try {
                                Integer.valueOf(lastFolder)
                                isDigit = true
                            } catch (ignored: NumberFormatException) {
                            }

                            rawUserId = if (isDigit) lastFolder else ""
                        }
                        if (TextUtils.isEmpty(rawUserId)) {
                            rv.add(rawEmulatedStorageTarget ?: "")
                        } else {
                            rv.add(rawEmulatedStorageTarget + File.separator + rawUserId)
                        }
                    }
                    if (!TextUtils.isEmpty(rawSecondaryStoragesStr)) {
                        val rawSecondaryStorages =
                                rawSecondaryStoragesStr!!.split(File.pathSeparator.toRegex())
                                        .dropLastWhile { it.isEmpty() }.toTypedArray()
                        Collections.addAll(rv, *rawSecondaryStorages)
                    }
                    val temp = rv.toTypedArray()
                    for (i in temp.indices) {
                        val tempf = File(temp[i] + "/" + split[1])
                        if (tempf.exists()) {
                            imgPath = temp[i] + "/" + split[1]
                        }
                    }
                }
            } else if ("com.android.providers.downloads.documents" == uri.authority) {
                val id = DocumentsContract.getDocumentId(uri)
                Log.d("document_id", id)

                if (id != null && id.startsWith("raw:")) {
                    return id.substring(4);
                }
                var cursor: Cursor? = null
                val column = "_data"
                val contentUriPrefixesToTry = arrayOf(
                        "content://downloads/public_downloads",
                        "content://downloads/my_downloads",
                        "content://downloads/all_downloads"
                )

                for (contentUriPrefix in contentUriPrefixesToTry) {
                    try {
                        val contentUri = ContentUris.withAppendedId(
                                Uri.parse(contentUriPrefix),
                                java.lang.Long.valueOf(id)
                        )
                        val path = getDataColumn(context, contentUri, null, null);
                        if (path != null) {
                            return path;
                        }
                    } catch (e: java.lang.Exception) {

                    }
                }
                val fileName = getFileName(context, uri);
                val cacheDir = getDocumentCacheDir(context)
                val file = generateFileName(fileName, cacheDir)
                var destinationPath: String? = null
                if (file != null) {
                    destinationPath = file.getAbsolutePath();
                    saveFileFromUri(context, uri, destinationPath);
                }
                return destinationPath;


            } else if ("com.android.providers.media.documents" == uri.authority) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val type = split[0]

                var contentUri: Uri? = null
                if ("image" == type) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                } else if ("video" == type) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                } else if ("audio" == type) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                }

                val selection = "_id=?"
                val selectionArgs = arrayOf(split[1])

                var cursor: Cursor? = null
                val column = "_data"
                val projection = arrayOf(column)

                try {
                    cursor = context.contentResolver.query(
                            contentUri!!,
                            projection,
                            selection,
                            selectionArgs,
                            null
                    )
                    if (cursor != null && cursor.moveToFirst()) {
                        val column_index = cursor.getColumnIndexOrThrow(column)
                        imgPath = cursor.getString(column_index)
                    }
                } finally {
                    if (cursor != null)
                        cursor.close()
                }
            } else if ("com.google.android.apps.docs.storage" == uri.authority) {
                isImageFromGoogleDrive = true
            }
        } else if ("content".equals(uri.scheme, ignoreCase = true)) {
            var cursor: Cursor? = null
            val column = "_data"
            val projection = arrayOf(column)

            try {
                cursor = context.contentResolver.query(uri, projection, null, null, null)
                if (cursor != null && cursor.moveToFirst()) {
                    val column_index = cursor.getColumnIndexOrThrow(column)
                    imgPath = cursor.getString(column_index)
                }
            } finally {
                if (cursor != null)
                    cursor.close()
            }

            if ("com.google.android.apps.docs.storage" == uri.authority) {
                isImageFromGoogleDrive = true
            }

        } else if ("file".equals(uri.scheme, ignoreCase = true)) {
            imgPath = uri.path
        }

        if (isImageFromGoogleDrive) {

            imgPath = getFilePathFromDrive(uri, context)

        }

        return imgPath
    }*/

    private var job: Job? = null

    fun getFileFromStorage(context: Context, uri: Uri): String? {

        var isImageFromGoogleDrive = false
        var imgPath: String? = null
        val isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT

        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            if ("com.android.externalstorage.documents" == uri.authority) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val type = split[0]

                if ("primary".equals(type, ignoreCase = true)) {
                    imgPath = Environment.getExternalStorageDirectory().toString() + "/" + split[1]
                } else {
                    val DIR_SEPORATOR = Pattern.compile("/")
                    val rv = HashSet<String>()
                    val rawExternalStorage = System.getenv("EXTERNAL_STORAGE")
                    val rawSecondaryStoragesStr = System.getenv("SECONDARY_STORAGE")
                    val rawEmulatedStorageTarget = System.getenv("EMULATED_STORAGE_TARGET")
                    if (TextUtils.isEmpty(rawEmulatedStorageTarget)) {
                        if (TextUtils.isEmpty(rawExternalStorage)) {
                            rv.add("/storage/sdcard0")
                        } else {
                            rv.add(rawExternalStorage!!)
                        }
                    } else {
                        val rawUserId: String
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
                            rawUserId = ""
                        } else {
                            val path = Environment.getExternalStorageDirectory().absolutePath
                            val folders = DIR_SEPORATOR.split(path)
                            val lastFolder = folders[folders.size - 1]
                            var isDigit = false
                            try {
                                Integer.valueOf(lastFolder)
                                isDigit = true
                            } catch (ignored: NumberFormatException) {
                            }

                            rawUserId = if (isDigit) lastFolder else ""
                        }
                        if (TextUtils.isEmpty(rawUserId)) {
                            rv.add(rawEmulatedStorageTarget!!)
                        } else {
                            rv.add(rawEmulatedStorageTarget + File.separator + rawUserId)
                        }
                    }
                    if (!TextUtils.isEmpty(rawSecondaryStoragesStr)) {
                        val rawSecondaryStorages =
                            rawSecondaryStoragesStr!!.split(File.pathSeparator.toRegex())
                                .dropLastWhile { it.isEmpty() }
                                .toTypedArray()
                        Collections.addAll(rv, *rawSecondaryStorages)
                    }
                    val temp = rv.toTypedArray()
                    for (i in temp.indices) {
                        val tempf = File(temp[i] + "/" + split[1])
                        if (tempf.exists()) {
                            imgPath = temp[i] + "/" + split[1]
                        }
                    }
                }
            } else if ("com.android.providers.downloads.documents" == uri.authority) {
                val id = DocumentsContract.getDocumentId(uri)
                Log.d("document_id", id)
                if (id != null && id.startsWith("raw:")) {
                    return id.substring(4);
                }
                var cursor: Cursor? = null
                val column = "_data"
                val contentUriPrefixesToTry = arrayOf(
                    "content://downloads/public_downloads",
                    "content://downloads/my_downloads",
                    "content://downloads/all_downloads"
                )

                for (contentUriPrefix in contentUriPrefixesToTry) {
                    try {
                        val contentUri = ContentUris.withAppendedId(
                            Uri.parse(contentUriPrefix),
                            java.lang.Long.valueOf(id)
                        )
                        val path = getDataColumn(context, contentUri, null, null);
                        if (path != null) {
                            return path;
                        }
                    } catch (e: java.lang.Exception) {

                    }
                }
                val fileName = getFileName(context, uri);
                val cacheDir = getDocumentCacheDir(context)
                val file = generateFileName(fileName, cacheDir)
                var destinationPath: String? = null
                if (file != null) {
                    destinationPath = file.getAbsolutePath();
                    saveFileFromUri(context, uri, destinationPath);
                }
                return destinationPath;


//                val projection = arrayOf(column)
                /* val projection = arrayOf(MediaStore.Images.Media.DATA)
                 try {
                     cursor = context.contentResolver.query(contentUri, projection, null, null, null)
                     if (cursor != null && cursor.moveToFirst()) {
                         val column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                         imgPath = cursor.getString(column_index)
                     }
                 } finally {
                     if (cursor != null)
                         cursor.close()
                 }*/
            } else if ("com.android.providers.media.documents" == uri.authority) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val type = split[0]

                var contentUri: Uri? = null
                if ("image" == type) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                } else if ("video" == type) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                } else if ("audio" == type) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                }

                val selection = "_id=?"
                val selectionArgs = arrayOf(split[1])

                var cursor: Cursor? = null
                val column = "_data"
                val projection = arrayOf(column)

                try {
                    cursor = context.contentResolver.query(
                        contentUri!!,
                        projection,
                        selection,
                        selectionArgs,
                        null
                    )
                    if (cursor != null && cursor.moveToFirst()) {
                        val column_index = cursor.getColumnIndexOrThrow(column)
                        imgPath = cursor.getString(column_index)
                    }
                } finally {
                    if (cursor != null)
                        cursor.close()
                }
            } else if ("com.google.android.apps.docs.storage" == uri.authority
                || "com.google.android.apps.photos.content" == uri.authority
            ) {
                isImageFromGoogleDrive = true
            }
        } else if ("content".equals(uri.scheme, ignoreCase = true)) {
            var cursor: Cursor? = null
            val column = "_data"
            val projection = arrayOf(column)

            try {
                cursor = context.contentResolver.query(uri, projection, null, null, null)
                if (cursor != null && cursor.moveToFirst()) {
                    val column_index = cursor.getColumnIndexOrThrow(column)
                    imgPath = cursor.getString(column_index)
                }
            } finally {
                if (cursor != null)
                    cursor.close()
            }

            if ("com.google.android.apps.docs.storage" == uri.authority
                || "com.google.android.apps.photos.content" == uri.authority
            ) {
                isImageFromGoogleDrive = true
            }

        } else if ("file".equals(uri.scheme, ignoreCase = true)) {
            imgPath = uri.path
        }

        if (isImageFromGoogleDrive) {
            imgPath = getFilePathFromDrive(uri, context)
        }

        return imgPath
    }

    fun getFileFromStorage(
        context: Context,
        uri: Uri,
        callback: (path: String) -> Unit
    )/*: String?*/ {

        var isImageFromGoogleDrive = false
        var imgPath: String? = null
        val isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT

        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            if ("com.android.externalstorage.documents" == uri.authority) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val type = split[0]

                if ("primary".equals(type, ignoreCase = true)) {
                    imgPath = Environment.getExternalStorageDirectory().toString() + "/" + split[1]
                } else {
                    val DIR_SEPORATOR = Pattern.compile("/")
                    val rv = HashSet<String>()
                    val rawExternalStorage = System.getenv("EXTERNAL_STORAGE")
                    val rawSecondaryStoragesStr = System.getenv("SECONDARY_STORAGE")
                    val rawEmulatedStorageTarget = System.getenv("EMULATED_STORAGE_TARGET")
                    if (TextUtils.isEmpty(rawEmulatedStorageTarget)) {
                        if (TextUtils.isEmpty(rawExternalStorage)) {
                            rv.add("/storage/sdcard0")
                        } else {
                            rv.add(rawExternalStorage!!)
                        }
                    } else {
                        val rawUserId: String
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
                            rawUserId = ""
                        } else {
                            val path = Environment.getExternalStorageDirectory().absolutePath
                            val folders = DIR_SEPORATOR.split(path)
                            val lastFolder = folders[folders.size - 1]
                            var isDigit = false
                            try {
                                Integer.valueOf(lastFolder)
                                isDigit = true
                            } catch (ignored: NumberFormatException) {
                            }

                            rawUserId = if (isDigit) lastFolder else ""
                        }
                        if (TextUtils.isEmpty(rawUserId)) {
                            rv.add(rawEmulatedStorageTarget!!)
                        } else {
                            rv.add(rawEmulatedStorageTarget + File.separator + rawUserId)
                        }
                    }
                    if (!TextUtils.isEmpty(rawSecondaryStoragesStr)) {
                        val rawSecondaryStorages =
                            rawSecondaryStoragesStr!!.split(File.pathSeparator.toRegex())
                                .dropLastWhile { it.isEmpty() }
                                .toTypedArray()
                        Collections.addAll(rv, *rawSecondaryStorages)
                    }
                    val temp = rv.toTypedArray()
                    for (i in temp.indices) {
                        val tempf = File(temp[i] + "/" + split[1])
                        if (tempf.exists()) {
                            imgPath = temp[i] + "/" + split[1]
                        }
                    }
                }
            } else if ("com.android.providers.downloads.documents" == uri.authority) {
                val id = DocumentsContract.getDocumentId(uri)
                Log.d("document_id", id)
                if (id != null && id.startsWith("raw:")) {
                    return callback.invoke(id.substring(4))
                }
                var cursor: Cursor? = null
                val column = "_data"
                val contentUriPrefixesToTry = arrayOf(
                    "content://downloads/public_downloads",
                    "content://downloads/my_downloads",
                    "content://downloads/all_downloads"
                )

                for (contentUriPrefix in contentUriPrefixesToTry) {
                    try {
                        val contentUri = ContentUris.withAppendedId(
                            Uri.parse(contentUriPrefix),
                            java.lang.Long.valueOf(id)
                        )
                        val path = getDataColumn(context, contentUri, null, null);
                        if (path != null) {
                            return callback.invoke(path)
                        }
                    } catch (e: java.lang.Exception) {

                    }
                }
                val fileName = getFileName(context, uri);
                val cacheDir = getDocumentCacheDir(context)
                val file = generateFileName(fileName, cacheDir)
                var destinationPath: String? = null
                if (file != null) {
                    destinationPath = file.getAbsolutePath();
                    saveFileFromUri(context, uri, destinationPath);
                }
                return callback.invoke(destinationPath!!)

            } else if ("com.android.providers.media.documents" == uri.authority) {

                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val type = split[0]

                var contentUri: Uri? = null
                if ("image" == type) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                } else if ("video" == type) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                } else if ("audio" == type) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                }

                val selection = "_id=?"
                val selectionArgs = arrayOf(split[1])

                var cursor: Cursor? = null
                val column = "_data"
                val projection = arrayOf(column)

                try {
                    cursor = context.contentResolver.query(
                        contentUri!!,
                        projection,
                        selection,
                        selectionArgs,
                        null
                    )
                    if (cursor != null && cursor.moveToFirst()) {
                        val column_index = cursor.getColumnIndexOrThrow(column)
                        imgPath = cursor.getString(column_index)
                    }
                } finally {
                    if (cursor != null)
                        cursor.close()
                }
            } else if ("com.google.android.apps.docs.storage" == uri.authority
                || "com.google.android.apps.photos.content" == uri.authority
            ) {
                isImageFromGoogleDrive = true
            }
        } else if ("content".equals(uri.scheme, ignoreCase = true)) {
            var cursor: Cursor? = null
            val column = "_data"
            val projection = arrayOf(column)

            try {
                cursor = context.contentResolver.query(uri, projection, null, null, null)
                if (cursor != null && cursor.moveToFirst()) {
                    val column_index = cursor.getColumnIndexOrThrow(column)
                    imgPath = cursor.getString(column_index)
                }
            } finally {
                if (cursor != null)
                    cursor.close()
            }

            if ("com.google.android.apps.docs.storage" == uri.authority
                || "com.google.android.apps.photos.content" == uri.authority
            ) {
                isImageFromGoogleDrive = true
            }

        } else if ("file".equals(uri.scheme, ignoreCase = true)) {
            imgPath = uri.path
        }

        if (isImageFromGoogleDrive) {

            /*val bitmap = getBitmapFromUri(context,uri)
            imgPath= storeImageToCache(context,bitmap)*/
            /*imgPath = getFilePathFromDrive(uri, context)*/

            job = GlobalScope.launch(Dispatchers.Main) {
                val result = async(Dispatchers.IO) {
                    imgPath = saveFileFromUriAndGetPath(uri, context)
                }
                result.await()
                imgPath?.let { callback.invoke(it) }
            }
        } else {
            imgPath?.let { callback.invoke(it) }
        }
    }

    @Throws(IOException::class)
    private fun getBitmapFromUri(context: Context, uri: Uri): Bitmap {
        val parcelFileDescriptor = context.contentResolver.openFileDescriptor(uri, "r")
        val fileDescriptor = parcelFileDescriptor!!.fileDescriptor
        val image = BitmapFactory.decodeFileDescriptor(fileDescriptor)
        parcelFileDescriptor.close()
        return image
    }

    fun storeImageToCache(context: Context, data: Bitmap): String? {
        var thumbnail: Bitmap? = null
        try {
            val dateTime = Date()
            thumbnail = data
            val bytes = ByteArrayOutputStream()
            val filenamePath = System.currentTimeMillis().toString() + ".jpg"
            thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, bytes)
            val outputDir = context!!.cacheDir
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

    private fun saveFileFromUri(context: Context, uri: Uri, destinationPath: String) {
        var `is`: InputStream? = null
        var bos: BufferedOutputStream? = null
        try {
            `is` = context.contentResolver.openInputStream(uri)
            bos = BufferedOutputStream(FileOutputStream(destinationPath, false))
            val buf = ByteArray(1024)
            `is`!!.read(buf)
            do {
                bos.write(buf)
            } while (`is`.read(buf) !== -1)
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                `is`?.close()
                bos?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
    }

    fun generateFileName(name: String?, directory: File): File? {
        if (name == null) {
            return null;
        }
        var file = File(directory, name)
        if (file.exists()) {
            var fileName = name
            var extension = ""
            val dotIndex = name.lastIndexOf('.')
            if (dotIndex > 0) {
                fileName = name.substring(0, dotIndex);
                extension = name.substring(dotIndex);
            }
            var index = 0;
            while (file.exists()) {
                index++
                val name = fileName + '(' + index + ')' + extension;
                file = File(directory, name)
            }
        }
        try {
            if (!file.createNewFile()) {
                return null
            }
        } catch (e: IOException) {
            return null
        }
        return file
    }

    fun getDocumentCacheDir(context: Context): File {
        val dir = File(context.cacheDir, DOCUMENTS_DIR)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    fun getFileName(context: Context, uri: Uri): String? {
        val mimeType = context.contentResolver.getType(uri)
        var filename: String? = null

        if (mimeType == null && context != null) {
            val path = getPath(context, uri)
            if (path == null) {
                filename = getName(uri.toString())
            } else {
                val file = File(path)
                filename = file.name
            }
        } else {
            val returnCursor = context.contentResolver.query(uri, null, null, null, null)
            if (returnCursor != null) {
                val nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                returnCursor.moveToFirst()
                filename = returnCursor.getString(nameIndex)
                returnCursor.close()
            }
        }

        return filename
    }

    fun getName(filename: String?): String? {
        if (filename == null) {
            return null
        }
        val index = filename.lastIndexOf('/')
        return filename.substring(index + 1)
    }

    fun getFilePathFromDrive(uri: Uri, context: Context): String? {
        val returnCursor = context.contentResolver.query(uri, null, null, null, null)
        /*
         * Get the column indexes of the data in the Cursor,
         *     * move to the first row in the Cursor, get the data,
         *     * and display it.
         * */
        if (returnCursor != null) {
            val nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            val sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE)
            returnCursor.moveToFirst();
            val size = returnCursor.getLong(sizeIndex).toString()
            val name = (returnCursor.getString(nameIndex))
            val file = File(context.cacheDir, name)
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val outputStream = FileOutputStream(file)
                val read = 0
                val maxBufferSize = 1 * 1024 * 1024
                val bytesAvailable = inputStream?.available()

                //int bufferSize = 1024;
                val bufferSize = Math.min(bytesAvailable ?: 0, maxBufferSize)

                val buffers = ByteArray(bufferSize)
                while ((inputStream?.read(buffers)) != -1) {
                    outputStream.write(buffers, 0, inputStream!!.read(buffers))
                }
                Log.d("File Size", "Size " + file.length())
                inputStream.close()
                outputStream.close()
                Log.d("File Path", "Path " + file.path)
                Log.d("File Size", "Size " + file.length())
            } catch (e: Exception) {
                Log.d("Exception", e.message.toString())
            }
            return file.path
        } else {
            return null
        }
    }

    suspend fun saveFileFromUriAndGetPath(uri: Uri, context: Context): String? {
        val inputStream = context.contentResolver.openInputStream(uri)
        val originalSize = inputStream!!.available()

        var bis: BufferedInputStream? = null
        var bos: BufferedOutputStream? = null

        val returnCursor = context.contentResolver.query(uri, null, null, null, null)

        if (returnCursor != null) {
            val nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            val sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE)
            returnCursor.moveToFirst();
            val size = returnCursor.getLong(sizeIndex).toString()
            val name = (returnCursor.getString(nameIndex))

            val file = File(context.cacheDir, name)

            bis = BufferedInputStream(inputStream)
            bos = BufferedOutputStream(FileOutputStream(file, false))

            val buf = ByteArray(originalSize)
            bis.read(buf)
            do {
                bos.write(buf)
            } while (bis.read(buf) != -1)
            bos.flush()
            bos.close()
            bis.close()
            return file.path
        } else {
            return ""
        }
    }


    fun isVideoFile(url: String?): Boolean {

        if (url == null) return false

        var extention = ""
        val i = url.lastIndexOf('.')

        if (i > 1) {
            extention = url.substring(i)
            if (extention.length > 5)
                extention = extention.substring(0, 5)
        }

        return extention.toLowerCase().contains("mp4")
    }

    fun isImageFile(url: String?): Boolean {

        if (url == null) return false

        var extention = ""
        val i = url.lastIndexOf('.')

        if (i > 1) {
            extention = url.substring(i)
            if (extention.length > 5)
                extention = extention.substring(0, 5)
        }

        return extention.toLowerCase().contains("jpg") ||
                extention.toLowerCase().contains("jpeg") || extention.toLowerCase().contains("png")
    }

    fun convertTOLocal(utcDate: String): String {
        try {
            val df = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            df.timeZone = TimeZone.getTimeZone("UTC")
            val date = df.parse(utcDate)
            df.timeZone = TimeZone.getDefault()
            df.applyPattern("dd MMM yy, h:mm a")
            return df.format(date)
        } catch (e: ParseException) {
            e.printStackTrace()
            return ""
        }

    }

    fun convertTOLocal(utcDate: String, fromPattern: String, toPattern: String): String {
        try {
            val df = SimpleDateFormat(fromPattern)
            df.timeZone = TimeZone.getTimeZone("UTC")
            val date = df.parse(utcDate)
            df.timeZone = TimeZone.getDefault()
            df.applyPattern(toPattern)
            return df.format(date)
        } catch (e: ParseException) {
            e.printStackTrace()
            return ""
        }

    }


    fun saveImage(context: Context, bitmap: Bitmap, orientation: Int): File? {
        var bitmap = bitmap
        try {

            val file = File(
                /*Environment.getExternalStorageDirectory()*/context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                    .toString() + "/" + APP_DIRECTORY,
                System.currentTimeMillis().toString() + ".jpeg"
            )
            file.parentFile.mkdirs()
            val outputStream = BufferedOutputStream(FileOutputStream(file))

            if (orientation != -1 && orientation != 0) {

                val matrix = Matrix()
                matrix.postRotate(orientation.toFloat())
                bitmap = Bitmap.createBitmap(
                    bitmap, 0, 0,
                    bitmap.width, bitmap.height, matrix,
                    true
                )
            }

            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
            return file

        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }

        return null
    }


    fun blurBitmapWithRenderscript(rs: RenderScript, bitmap2: Bitmap) {
        // this will blur the bitmapOriginal with a radius of 25
        // and save it in bitmapOriginal
        // use this constructor for best performance, because it uses
        // USAGE_SHARED mode which reuses memory
        val input = Allocation.createFromBitmap(rs, bitmap2)
        val output = Allocation.createTyped(
            rs,
            input.type
        )
        val script = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs))
        // must be >0 and <= 25
        script.setRadius(25f)
        script.setInput(input)
        script.forEach(output)
        output.copyTo(bitmap2)
    }

}