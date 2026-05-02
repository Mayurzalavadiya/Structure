package com.starter.app.data.pojo.dataclass

data class BluetoothDevices(
    val name: String,
    val address: String,
    val isConnected: Boolean,
    val isPaired: Boolean
)