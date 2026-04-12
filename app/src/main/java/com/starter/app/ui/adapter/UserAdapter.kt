package com.starter.app.ui.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.starter.app.data.pojo.response.UsersResponse
import com.starter.app.databinding.ItemUserListBinding
import com.starter.app.utils.load

class UserAdapter : RecyclerView.Adapter<UserAdapter.ViewHolder>() {

    private var list = mutableListOf<UsersResponse.User>()


    @SuppressLint("NotifyDataSetChanged")
    fun addItem(items: List<UsersResponse.User>?) {
        if (items != null) {
            this.list.addAll(items)
        }
        notifyDataSetChanged()
    }

    fun clear() {
        list.clear()
        notifyDataSetChanged()
    }

    inner class ViewHolder(private val binding: ItemUserListBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(item: UsersResponse.User) = with(binding) {
            item.image?.let { binding.imageViewImage.load(it,true) }
//            Glide.with(imageViewImage).load(item.image).diskCacheStrategy(DiskCacheStrategy.ALL) .into(imageViewImage)
            textViewUserName.text = "${item.firstName} ${item.lastName}"
            textViewUserEmail.text = item.email
            textViewUserId.text = item.id.toString()
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