package com.starter.app.ui.fragment


import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.starter.app.utils.imagepicker.ImageAndVideoPicker
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.starter.app.databinding.FragmentQRScannerBinding
import com.starter.app.ui.base.BaseActivity
import com.starter.app.ui.base.BaseFragment
import com.starter.app.utils.imagepicker.MediaPickerHelper
import com.starter.app.utils.imagepicker.PickedMedia
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@AndroidEntryPoint
class QRScannerFragment : BaseFragment<FragmentQRScannerBinding>(),
    MediaPickerHelper.MediaPickerCallback {

    private lateinit var cameraExecutor: ExecutorService
    private var lastScannedValue: String? = null

    private lateinit var mediaPicker: MediaPickerHelper

    private var isSettingsDialogShown = false

    override fun onResume() {
        super.onResume()

        if (isSettingsDialogShown) {
            isSettingsDialogShown = false // always reset

            when {
                ContextCompat.checkSelfPermission(
                    requireContext(), Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // Fully granted → start camera
                    startCamera()
                }

                shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
                    // "Ask Every Time" was selected → re-request normally, system shows prompt
                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                }

                else -> {
                    // Still permanently denied → show settings dialog again
                    showSettingsDialog()
                }
            }
        }
    }

    // Camera permission launcher
    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startCamera()
        } else {
            if (!shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                // Permission denied permanently
                showSettingsDialog()

            } else {
                // Permission denied temporarily
                showMessage("Camera permission is required")
            }
        }
    }

    private fun showSettingsDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Permission Required")
            .setMessage("Camera access is required to scan QR codes. Please enable it in settings.")
            .setPositiveButton("Go to Settings") { _, _ ->
                openAppSettings()
                isSettingsDialogShown = true

            }
            .setCancelable(false)
            .show()
    }

    override fun createViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        attachToRoot: Boolean
    ): FragmentQRScannerBinding {
        return FragmentQRScannerBinding.inflate(layoutInflater, container, attachToRoot)
    }

    override fun bindData() {
        mediaPicker = MediaPickerHelper(requireActivity() as BaseActivity, this)


        cameraExecutor = Executors.newSingleThreadExecutor()


        checkCameraPermissionAndStart()


        binding.toolbar.imageviewBack.isVisible = true
        setClickListener()

        /*    binding.previewView.postDelayed({
                lastScannedValue = null
            }, 3000) // 3 seconds cooldown*/

    }


    private fun checkCameraPermissionAndStart() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ) {
            startCamera()
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun setClickListener() = with(binding) {

        toolbar.imageviewBack.setOnClickListener {
            cameraExecutor.shutdown()
            navigator.goBack()
        }

        buttonSelectImage.setOnClickListener {
            mediaPicker.pickSingleImage()
            /*ImageAndVideoPicker.newInstance()
                .pickImage(true) // set true to pick image, default false
                .setResult(imagePickerResult = object :
                    ImageAndVideoPicker.ImageVideoPickerResult() {
                    override fun onFail(message: String) {
                        Log.d("ImagePickerFailed", "onFail: $message")
                    }

                    override fun onImagesSelected(list: ArrayList<String>) {
                        *//*    selectedImagePath = list.first()
                            Log.d("ImagePickerSelected", "onImagesSelected: $selectedImagePath")
                            ivProfile.load(selectedImagePath ?: "", false)*//*


                    }
                }).show(childFragmentManager, ImageAndVideoPicker::class.java.name)*/
        }
    }

    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", requireActivity().packageName, null)
        }
        startActivity(intent)
    }


    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireActivity())

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.previewView.surfaceProvider)
            }

            val options = BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
                .build()

            val barcodeScanner = BarcodeScanning.getClient(options)

            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor) { imageProxy ->
                        processImageProxy(barcodeScanner, imageProxy)
                    }
                }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                requireActivity(),
                cameraSelector,
                preview,
                imageAnalyzer
            )

        }, ContextCompat.getMainExecutor(requireActivity()))
    }

    @OptIn(ExperimentalGetImage::class)
    private fun processImageProxy(
        scanner: com.google.mlkit.vision.barcode.BarcodeScanner,
        imageProxy: ImageProxy
    ) {
        val mediaImage = imageProxy.image ?: run {
            imageProxy.close()
            return
        }

        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                if (barcodes.isNotEmpty()) {
                    val barcode = barcodes.first()
                    val value = barcode.rawValue

                    if (!value.isNullOrEmpty() && value != lastScannedValue) {
                        lastScannedValue = value
                        Log.d("Scanner", "Scanned new: $value")

                        requireActivity().runOnUiThread {
                            binding.textViewQR.text = value
                        }

                        // Optional: perform any update or insert here
                    }
                }
            }
            .addOnFailureListener {
                it.printStackTrace()
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }


    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    override fun onMediaSelected(media: List<PickedMedia>) {
        binding.textViewQR.text = media.first().path
    }


    override fun onCameraImageCaptured(uri: Uri, path: String) {
        TODO("Not yet implemented")
    }

    override fun onCameraVideoCaptured(uri: Uri, path: String) {
        TODO("Not yet implemented")
    }

    override fun onError(error: String) {
        Log.d("ImagePickerFailed", "onFail: $error")
    }

}
