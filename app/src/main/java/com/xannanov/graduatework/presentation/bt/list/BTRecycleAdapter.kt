package com.xannanov.graduatework.presentation.bt.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.xannanov.graduatework.databinding.BtItemBinding
import com.xannanov.graduatework.domain.bt.model.BTModel

class BTRecycleAdapter(
    private val onItemClickListener: (BTModel) -> Unit
) : ListAdapter<BTModel, BTRecycleAdapter.BTViewHolder>(ItemComparator()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BTViewHolder =
        BTViewHolder(
            binding = BtItemBinding.inflate(LayoutInflater.from(parent.context), parent, false),
            onItemClickListener = onItemClickListener
        )

    override fun onBindViewHolder(holder: BTViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class BTViewHolder(
        private val binding: BtItemBinding,
        private val onItemClickListener: (BTModel) -> Unit
    ) : ViewHolder(binding.root) {

        fun bind(model: BTModel) {
            with(binding) {
                tvName.text = model.name
                tvMac.text = model.mac

                root.setOnClickListener {
                    onItemClickListener(model)
                }
            }
        }
    }

    class ItemComparator : DiffUtil.ItemCallback<BTModel>() {

        override fun areItemsTheSame(oldItem: BTModel, newItem: BTModel): Boolean =
            oldItem.mac == newItem.mac

        override fun areContentsTheSame(oldItem: BTModel, newItem: BTModel): Boolean =
            oldItem == newItem
    }
}