package com.example.app.ui.fragment

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.app.data.pojo.dataclass.BluetoothDevices
import com.example.app.databinding.FragmentBluetoothBinding
import com.example.app.ui.base.BaseFragment
import com.example.app.ui.adapter.BluetoothDeviceAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BluetoothFragment : BaseFragment<FragmentBluetoothBinding>() {

    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var deviceAdapter: BluetoothDeviceAdapter

    // Tracks all discovered + paired devices
    private val deviceList = mutableListOf<BluetoothDevices>()

    // Connected device addresses (can be multiple across profiles)
    private val connectedAddresses = mutableSetOf<String>()

    private var isSettingsDialogShown = false

    // ─── Permissions ────────────────────────────────────────────────────────────

    // Android 12+ needs BLUETOOTH_SCAN + BLUETOOTH_CONNECT
    // Android <12 needs BLUETOOTH + ACCESS_FINE_LOCATION (for discovery)
    private val requiredPermissions: Array<String>
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT
            )
        } else {
            arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        if (results.values.all { it }) {
            initBluetooth()
        } else {
            val anyPermanentlyDenied = requiredPermissions.any { permission ->
                !shouldShowRequestPermissionRationale(permission)
                        && results[permission] == false
            }
            if (anyPermanentlyDenied) {
                showSettingsDialog()
            } else {
                showMessage("Bluetooth permissions are required")
            }
        }
    }

    // ─── BroadcastReceiver ───────────────────────────────────────────────────────

    private val bluetoothReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {

                BluetoothDevice.ACTION_FOUND -> {
                    val device: BluetoothDevice? =
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            intent.getParcelableExtra(
                                BluetoothDevice.EXTRA_DEVICE,
                                BluetoothDevice::class.java
                            )
                        } else {
                            @Suppress("DEPRECATION")
                            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                        }
                    device?.let { addOrUpdateDevice(it, isConnected = false) }
                }

                BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                    binding.buttonScan.text = "Stop Scan"
                    binding.progressBar.progress.isVisible = true   // show
                }

                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    binding.buttonScan.text = "Scan Devices"
                    binding.progressBar.progress.isVisible= false  // hide                    binding.textViewStatus.text = "Scanning..."
                    binding.textViewStatus.text = "${deviceList.size} device(s) found"
                }

                BluetoothDevice.ACTION_ACL_CONNECTED -> {
                    val device: BluetoothDevice? =
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            intent.getParcelableExtra(
                                BluetoothDevice.EXTRA_DEVICE,
                                BluetoothDevice::class.java
                            )
                        } else {
                            @Suppress("DEPRECATION")
                            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                        }
                    device?.address?.let {
                        connectedAddresses.add(it)
                        refreshConnectionStatus()
                    }
                }

                BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                    val device: BluetoothDevice? =
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            intent.getParcelableExtra(
                                BluetoothDevice.EXTRA_DEVICE,
                                BluetoothDevice::class.java
                            )
                        } else {
                            @Suppress("DEPRECATION")
                            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                        }
                    device?.address?.let {
                        connectedAddresses.remove(it)
                        refreshConnectionStatus()
                    }
                }
            }
        }
    }

    // ─── Lifecycle ───────────────────────────────────────────────────────────────

    override fun createViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        attachToRoot: Boolean
    ): FragmentBluetoothBinding {
        return FragmentBluetoothBinding.inflate(inflater, container, attachToRoot)
    }

    override fun bindData() {
        setUpAdapter()
        setToolBar()
        setClickListeners()
        checkPermissionsAndInit()
        registerBluetoothReceiver()
    }

    private fun setToolBar() {
        toolbar.setToolbarTitle("Bluetooth")
        toolbar.showToolbar(true)
    }

    override fun onResume() {
        super.onResume()
        if (isSettingsDialogShown) {
            isSettingsDialogShown = false
            when {
                hasAllPermissions() -> initBluetooth()
                requiredPermissions.any { shouldShowRequestPermissionRationale(it) } -> {
                    // "Ask every time" selected → re-request normally
                    permissionLauncher.launch(requiredPermissions)
                }

                else -> showSettingsDialog()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stopScanSafely()
        requireContext().unregisterReceiver(bluetoothReceiver)
    }

    // ─── Setup ───────────────────────────────────────────────────────────────────

    private fun setUpAdapter() = with(binding) {
        deviceAdapter = BluetoothDeviceAdapter()
        recyclerViewDevices.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = deviceAdapter
        }
    }

    private fun setClickListeners() = with(binding) {

        buttonScan.setOnClickListener {
            if (bluetoothAdapter.isDiscovering) {
                stopScanSafely()
            } else {
                startScanSafely()
            }
        }
    }

    private fun registerBluetoothReceiver() {
        val filter = IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_FOUND)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
            addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
            addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
        }
        requireContext().registerReceiver(bluetoothReceiver, filter)
    }

    // ─── Permission helpers ──────────────────────────────────────────────────────

    private fun hasAllPermissions(): Boolean {
        return requiredPermissions.all {
            ContextCompat.checkSelfPermission(requireContext(), it) ==
                    PackageManager.PERMISSION_GRANTED
        }
    }

    private fun checkPermissionsAndInit() {
        if (hasAllPermissions()) {
            initBluetooth()
        } else {
            permissionLauncher.launch(requiredPermissions)
        }
    }

    private fun showSettingsDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Permission Required")
            .setMessage("Bluetooth permissions are required to scan and connect devices. Please enable them in Settings.")
            .setPositiveButton("Go to Settings") { _, _ ->
                isSettingsDialogShown = true
                openAppSettings()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(false)
            .show()
    }

    private fun openAppSettings() {
        val intent = android.content.Intent(
            android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        ).apply {
            data = android.net.Uri.fromParts("package", requireActivity().packageName, null)
        }
        startActivity(intent)
    }

    // ─── Bluetooth logic ─────────────────────────────────────────────────────────

    private fun initBluetooth() {
        val bluetoothManager =
            requireContext().getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter

        if (!bluetoothAdapter.isEnabled) {
            binding.textViewStatus.text = "Bluetooth is off. Please enable it."
            binding.buttonScan.isEnabled = false
            return
        }

        binding.buttonScan.isEnabled = true
        loadPairedDevices()
    }

    private fun loadPairedDevices() {
        if (!hasBluetoothConnectPermission()) return

        connectedAddresses.clear()

        // Step 1: Load paired devices immediately
        try {
            bluetoothAdapter.bondedDevices?.forEach { device ->
                addOrUpdateDevice(device, isConnected = false)
            }
            binding.textViewStatus.text = "Paired devices: ${deviceList.size}"
        } catch (e: SecurityException) {
            e.printStackTrace()
        }

        // Step 2: Check connected devices via profile proxy (async)
        checkConnectedViaProfile(BluetoothProfile.A2DP)
        checkConnectedViaProfile(BluetoothProfile.HEADSET)
    }

    private fun checkConnectedViaProfile(profile: Int) {
        bluetoothAdapter.getProfileProxy(
            requireContext(),
            object : BluetoothProfile.ServiceListener {
                override fun onServiceConnected(profile: Int, proxy: BluetoothProfile) {
                    try {
                        proxy.connectedDevices.forEach { device ->
                            connectedAddresses.add(device.address)
                        }
                        refreshConnectionStatus()
                    } catch (e: SecurityException) {
                        e.printStackTrace()
                    } finally {
                        // Always close proxy after use to avoid leaks
                        bluetoothAdapter.closeProfileProxy(profile, proxy)
                    }
                }

                override fun onServiceDisconnected(profile: Int) {}
            },
            profile
        )
    }

    private fun startScanSafely() {
        if (!hasBluetoothScanPermission()) {
            permissionLauncher.launch(requiredPermissions)
            return
        }
        try {
            deviceList.clear()
            deviceAdapter.submitList(emptyList())
            loadPairedDevices() // Keep paired devices at top
            bluetoothAdapter.startDiscovery()
        } catch (e: SecurityException) {
            showMessage("Permission error during scan")
        }
    }

    private fun stopScanSafely() {
        try {
            if (::bluetoothAdapter.isInitialized && bluetoothAdapter.isDiscovering) {
                bluetoothAdapter.cancelDiscovery()
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    private fun addOrUpdateDevice(device: BluetoothDevice, isConnected: Boolean) {
        try {
            val name = device.name ?: "Unknown Device"
            val address = device.address
            val existing = deviceList.indexOfFirst { it.address == address }
            val item = BluetoothDevices(
                name = name,
                address = address,
                isConnected = isConnected || connectedAddresses.contains(address),
                isPaired = device.bondState == BluetoothDevice.BOND_BONDED
            )
            if (existing >= 0) {
                deviceList[existing] = item
            } else {
                deviceList.add(item)
            }
            // Always show connected/paired first
            val sorted = deviceList.sortedWith(
                compareByDescending<BluetoothDevices> { it.isConnected }
                    .thenByDescending { it.isPaired }
            )
            requireActivity().runOnUiThread {
                deviceAdapter.submitList(sorted.toList())
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    private fun refreshConnectionStatus() {
        val updated = deviceList.map {
            it.copy(isConnected = connectedAddresses.contains(it.address))
        }
        deviceList.clear()
        deviceList.addAll(updated)
        requireActivity().runOnUiThread {
            deviceAdapter.submitList(updated.toList())
        }
    }

    // ─── Permission shorthand ─────────────────────────────────────────────────────

    private fun hasBluetoothConnectPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        } else true
    }

    private fun hasBluetoothScanPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.BLUETOOTH_SCAN
            ) == PackageManager.PERMISSION_GRANTED
        } else true
    }
}