package com.example.app.ui.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.app.data.pojo.response.UsersResponse
import com.example.app.databinding.ItemUserListBinding
import com.example.app.utils.load

class UserAdapter(private val onItemClick: (item: UsersResponse.User) -> Unit
) : RecyclerView.Adapter<UserAdapter.ViewHolder>() {

    private var list = mutableListOf<UsersResponse.User>()
    private var searchList = mutableListOf<UsersResponse.User>()


    fun getList(): List<UsersResponse.User> = list
    @SuppressLint("NotifyDataSetChanged")
    fun addItem(items: List<UsersResponse.User>?) {
        if (items != null) {
            this.list.addAll(items)
            this.searchList.addAll(items)
        }
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun filter(query: String) {
        searchList = if (query.isBlank()) {
            list.toMutableList()
        } else {
            list.filter { user ->
                val fullName = "${user.firstName} ${user.lastName}"
                fullName.contains(query, ignoreCase = true) ||
                        user.email?.contains(query, ignoreCase = true) == true ||
                        user.id?.toString()?.contains(query) == true
            }.toMutableList()
        }
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun clear() {
        list.clear()
        searchList.clear()
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

            itemView.setOnClickListener {
                onItemClick(item)
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

    override fun getItemCount(): Int = searchList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(searchList[position])
    }
}