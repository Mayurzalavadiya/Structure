package com.starter.app.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.starter.app.R
import com.starter.app.data.pojo.dataclass.BluetoothDevices
import com.starter.app.databinding.ItemBluetoothDeviceBinding

class BluetoothDeviceAdapter :
    ListAdapter<BluetoothDevices, BluetoothDeviceAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemBluetoothDeviceBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemBluetoothDeviceBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: BluetoothDevices) = with(binding) {
            textViewDeviceName.text = item.name
            textViewDeviceAddress.text = item.address

            // Connected badge
            textViewConnected.isVisible = item.isConnected

            // Paired badge
            textViewPaired.isVisible = item.isPaired && !item.isConnected

            // Icon tint — blue if connected, gray if not
            imageViewBluetooth.setColorFilter(
                ContextCompat.getColor(
                    root.context,
                    if (item.isConnected) R.color.colorAccent else R.color.colorGray
                )
            )
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<BluetoothDevices>() {
        override fun areItemsTheSame(a: BluetoothDevices, b: BluetoothDevices) =
            a.address == b.address

        override fun areContentsTheSame(a: BluetoothDevices, b: BluetoothDevices) =
            a == b
    }
}