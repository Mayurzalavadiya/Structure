package com.example.app.ui.fragment

import android.Manifest
import android.app.Activity
import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.example.app.data.pojo.dataclass.Event
import com.example.app.exception.ApplicationException
import com.example.app.databinding.FragmentAddEventBinding
import com.example.app.ui.base.BaseActivity
import com.example.app.ui.base.BaseFragment
import com.example.app.ui.base.EventNotificationReceiver
import com.example.app.ui.viewmodel.EventViewModel
import com.example.app.utils.Validator
import com.example.app.utils.imagepicker.MediaPickerHelper
import com.example.app.utils.imagepicker.PickedMedia
import com.example.app.utils.load
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

/**
 * Base Fragment has T type class, pass viewbinding name on this T type,
 */
@AndroidEntryPoint
class AddEventFragment : BaseFragment<FragmentAddEventBinding>() {


    private val eventViewModel: EventViewModel by viewModels()


    private var currentEvent: Event? = null

    private var imageUri: Uri? = null

    private var eventId: Int? = null

    private lateinit var mediaPicker: MediaPickerHelper

    /**
     * Inject fragmentComponent for dagger
     */
    @Inject
    lateinit var validator: Validator

    private val isValid: Boolean
        get() {
            return try {

                if (imageUri == null) {
                    throw ApplicationException("Please select image")
                }

                validator.submit(binding.editTextTitle)
                    .checkEmpty().errorMessage("Please enter title")
                    .check()

                validator.submit(binding.editTextDescription)
                    .checkEmpty().errorMessage("Please enter description")
                    .check()
                validator.submit(binding.editTextDateTime)
                    .checkEmpty().errorMessage("Please enter date and time")
                    .check()

                true
            } catch (e: ApplicationException) {
                showMessage(e)
                false
            }
        }

    /**
     * Create view binding object and return this object for set layout on fragment.
     */
    override fun createViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        attachToRoot: Boolean
    ): FragmentAddEventBinding {
        return FragmentAddEventBinding.inflate(inflater, container, attachToRoot)
    }

    /**
     * This method is call when on onViewCreated call from life cycle
     * THis one is used for bind data to control
     */
    override fun bindData() {
        eventId = arguments?.getInt("id", -1)

        if (eventId != null) {
            observeData()
        }

        binding.toolbar.imageviewBack.isVisible = true

        mediaPicker = MediaPickerHelper(
            activity = requireActivity() as BaseActivity,
            callback = initialMediaPicker()
        )

        setClickListener()
    }

    private fun initialMediaPicker(): MediaPickerHelper.MediaPickerCallback {
        return object : MediaPickerHelper.MediaPickerCallback {

            override fun onMediaSelected(media: List<PickedMedia>) {
                imageUri = media.first().uri
                binding.imageViewImage.load(imageUri.toString(), true)
            }


            override fun onCameraImageCaptured(uri: Uri, path: String) {
                Log.d("MediaPicker", "Camera Image Uri: $uri")
                Log.d("MediaPicker", "Camera Image Path: $path")
            }

            override fun onCameraVideoCaptured(uri: Uri, path: String) {
                Log.d("MediaPicker", "Camera Video Uri: $uri")
                Log.d("MediaPicker", "Camera Video Path: $path")
            }

            override fun onError(error: String) {
                showMessage(error)
            }
        }
    }


    private fun setClickListener() = with(binding) {

        binding.toolbar.imageviewBack.setOnClickListener {
            navigator.goBack()
        }

        buttonSave.setOnClickListener {
            if (isValid) {
                val alarmManager =
                    requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                    !alarmManager.canScheduleExactAlarms()
                ) {
                    // 🔸 Ask user to grant permission for exact alarms
                    val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                        data = Uri.parse("package:${requireContext().packageName}")
                    }
                    startActivity(intent)

                    return@setOnClickListener
                }

                // ✅ If permission is granted, proceed to save and schedule
                val title = binding.editTextTitle.text.toString().trim()
                val description = binding.editTextDescription.text.toString().trim()
                val dateTime = binding.editTextDateTime.text.toString().trim()
                val image = imageUri.toString()

                val event = if (eventId == -1) {
                    Event(
                        title = title,
                        description = description,
                        dateTime = dateTime,
                        imageUri = image
                    )
                } else {
                    currentEvent?.copy(
                        title = title,
                        description = description,
                        dateTime = dateTime,
                        imageUri = image
                    )
                }

                event?.let {
                    if (eventId == -1) {
                        eventViewModel.insertEvent(it)
                    } else {
                        eventViewModel.updateEvent(it)
                    }

                    scheduleNotification(it)
                    navigator.goBack()
                }
            }
        }



        imageViewEdit.setOnClickListener {
            mediaPicker.pickSingleImage()

            /* if (hasStoragePermission()) {
                 openGallery()
             } else {
                 requestPermission()
             }*/

        }

        editTextDateTime.setOnClickListener {
            hideKeyBoard()
            editTextDateTime.clearFocus()
            openDateTimePicker()
        }
    }

    private fun scheduleNotification(event: Event) {
        val calendar = Calendar.getInstance().apply {
            val formatter = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault())
            time = formatter.parse(event.dateTime) ?: return
        }

        val intent = Intent(requireContext(), EventNotificationReceiver::class.java).apply {
            putExtra("image", event.imageUri)
            putExtra("title", event.title)
            putExtra("description", event.description)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            event.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            val settingsIntent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                data = Uri.parse("package:${requireContext().packageName}")
            }
            startActivity(settingsIntent)
            return
        }

        alarmManager.setExact(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )
    }


    private fun openDateTimePicker() {
        val calendar = Calendar.getInstance()

        val datePicker = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                val timePicker = TimePickerDialog(
                    requireContext(),
                    { _, hourOfDay, minute ->
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                        calendar.set(Calendar.MINUTE, minute)

                        val formatter = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault())
                        val formattedDateTime = formatter.format(calendar.time)

                        binding.editTextDateTime.setText(formattedDateTime)
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    false // is24HourView
                )

                timePicker.show()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        datePicker.show()
    }


    private fun hasStoragePermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestPermission() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        permissionLauncher.launch(permission)
    }


    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            openGallery()
        } else {
            showMessage("Permission Denied")
        }
    }

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            imageUri = result.data?.data
            binding.imageViewImage.load(imageUri.toString(), true)
        }
    }


    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImageLauncher.launch(intent)
    }


    override fun onBackActionPerform(): Boolean {
        return true
    }


    private fun observeData() {
        eventId?.let {
            eventViewModel.getEventById(it).observe(viewLifecycleOwner) { event ->
                event?.let {
                    currentEvent = it
                    binding.editTextDateTime.setText(it.dateTime)
                    binding.editTextTitle.setText(it.title)
                    binding.editTextDescription.setText(it.description)
                    imageUri = it.imageUri.toUri()
                    Glide.with(requireActivity()).load(it.imageUri).into(binding.imageViewImage)
                }
            }
        }
    }

}