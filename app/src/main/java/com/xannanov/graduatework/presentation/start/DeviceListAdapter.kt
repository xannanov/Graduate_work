package com.xannanov.graduatework.presentation.start

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.xannanov.graduatework.databinding.ItemDeviceBinding
import com.xannanov.graduatework.domain.repository.device.model.DeviceModel

class DeviceListAdapter(
    private val onItemClickListener: (model: DeviceModel) -> Unit
): ListAdapter<DeviceModel, DeviceListAdapter.DeviceListHolder>(ItemComparator()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceListHolder =
        DeviceListHolder(
            binding = ItemDeviceBinding.inflate(LayoutInflater.from(parent.context), parent, false),
            onItemClickListener = onItemClickListener
        )

    override fun onBindViewHolder(holder: DeviceListHolder, position: Int) =
        holder.bind(getItem(position))

    class DeviceListHolder(
        private val binding: ItemDeviceBinding,
        private val onItemClickListener: (model: DeviceModel) -> Unit
    ): ViewHolder(binding.root) {

        fun bind(model: DeviceModel) {
            with(binding) {
                tvDeviceName.text = model.name
                tvRoomName.text = model.room

                root.setOnClickListener {
                    onItemClickListener(model)
                }
            }
        }
    }

    class ItemComparator : DiffUtil.ItemCallback<DeviceModel>() {

        override fun areItemsTheSame(oldItem: DeviceModel, newItem: DeviceModel): Boolean =
            oldItem.uuid == newItem.uuid

        override fun areContentsTheSame(oldItem: DeviceModel, newItem: DeviceModel): Boolean =
            oldItem == newItem
    }
}