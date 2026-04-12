package com.starter.app.ui.base.adavancedrecyclerview

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

class DiffUtilCallback<T>(
    private val oldList: List<T>,
    private val newList: List<T>,
    private val areItemsTheSame: (oldItem: T, newItem: T) -> Boolean
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return areItemsTheSame(oldList[oldItemPosition], newList[newItemPosition])
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }
}

abstract class DiffUtilAdapter<VH : BaseHolder<Item>, Item>(
    private val areItemsTheSame: (oldItem: Item, newItem: Item) -> Boolean
) : RecyclerView.Adapter<BaseHolder<Item>>() {

    var items = emptyList<Item>()
        private set

    fun submitList(newList: List<Item>, page: Int = 1) {
        val diffResult = DiffUtil.calculateDiff(
            DiffUtilCallback(
                oldList = items,
                newList = newList,
                areItemsTheSame = areItemsTheSame
            )
        )
        if (page == 1) {
            items = emptyList()
        }
        items = newList
        diffResult.dispatchUpdatesTo(this)
    }

    fun clearAllItem() {
        if (items.isNotEmpty()) {
            items = emptyList()
            // Commented
            // isLoading = true
            notifyDataSetChanged()
        }
    }

    protected var onClickListener: ((item: Item) -> Unit)? = null
    fun setOnItemClickListener(onItemClickListener: (item: Item) -> Unit) {
        this.onClickListener = onItemClickListener
    }

    fun updateItem(predicate: (Item) -> Boolean, itemToUpdate: (Item) -> Item) {
        val updatedList = items.map { item ->
            if (predicate(item)) itemToUpdate(item) else item
        }
        submitList(updatedList)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseHolder<Item> {
        return createDataHolder(parent, viewType)
    }

    override fun onBindViewHolder(holder: BaseHolder<Item>, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    abstract fun createDataHolder(parent: ViewGroup, viewType: Int): VH
}
