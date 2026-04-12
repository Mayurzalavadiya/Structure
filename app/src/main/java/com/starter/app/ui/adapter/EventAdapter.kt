package com.starter.app.ui.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.starter.app.data.pojo.dataclass.Event
import com.starter.app.data.pojo.response.UsersResponse
import com.starter.app.databinding.ItemUserListBinding
import com.starter.app.utils.Extensions.showView

class EventAdapter(
    private val onClick: (Event) -> Unit,
    private val onDelete: (Event) -> Unit
) : RecyclerView.Adapter<EventAdapter.ViewHolder>() {

    private var list = mutableListOf<Event>()


    @SuppressLint("NotifyDataSetChanged")
    fun addItem(items: List<Event>) {
        list.clear()
        this.list.addAll(items.reversed())
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun removeItem(items: Event) {
        this.list.remove(items)
    }

    inner class ViewHolder(private val binding: ItemUserListBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(item: Event) = with(binding) {
            Glide.with(imageViewImage).load(item.imageUri).diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imageViewImage)
            textViewUserName.text = item.title
            textViewUserEmail.text = item.description
            textViewUserId.text = item.dateTime
            showView(imageViewDelete, imageViewEdit)

            imageViewEdit.setOnClickListener {
                onClick(item)
            }

            imageViewDelete.setOnClickListener {
                onDelete(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemUserListBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position])
    }
}