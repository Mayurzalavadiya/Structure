package com.starter.app.ui.base.adavancedrecyclerview

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.starter.app.utils.Extensions.toBinding

abstract class AdvanceRecycleViewAdapter<H : BaseHolder<E>, E> :
    RecyclerView.Adapter<BaseHolder<*>>, HasItem<E> {

    var items: MutableList<E>? = null
        set(value) {
            if (field == null)
                field = mutableListOf()
            else
                field!!.clear()

            if (field != null)
                field!!.addAll(value!!)

            isLoading = false
            notifyDataSetChanged()
        }

    protected var isNoData = false
    protected var isLoading = true
    var loaderVisibility = View.GONE

    var errorMessage = ""
        set(value) {
            field = value
            isLoading = false
            isNoData = true
            notifyDataSetChanged()
        }

    protected var onClickListener: ((item: E) -> Unit)? = null
    fun setOnItemClickListener(onItemClickListener: (item: E) -> Unit) {
        this.onClickListener = onItemClickListener
    }

    protected var onClickPositionListener: ((item: E, position: Int) -> Unit)? = null
    fun setOnItemClickPositionListener(onClickListenerPosition: ((item: E, position: Int) -> Unit)) {
        this.onClickPositionListener = onClickListenerPosition
    }


    protected var onClickViewListener: ((position: Int) -> Unit)? = null
    fun setOnItemClickViewListener(onClickViewListener: (position: Int) -> Unit) {
        this.onClickViewListener = onClickViewListener
    }

    protected var onClickPollListener: ((position: Int) -> Unit)? = null
    fun setOnPollClickViewListener(onClickPollListener: (position: Int) -> Unit) {
        this.onClickPollListener = onClickPollListener
    }

    protected var onClickInvestListener: ((position: Int) -> Unit)? = null
    fun setOnInvestClickViewListener(onClickInvestListener: (position: Int) -> Unit) {
        this.onClickInvestListener = onClickInvestListener
    }


    constructor() {
        this.items = arrayListOf()
    }

    constructor(items: MutableList<E>) : this() {
        this.items = items
    }

    fun updateItems(items: List<E>?) {
        isLoading = true

        if (this.items == null) {
            this.items = mutableListOf()
            this.items!!.addAll(items!!)

            if (this.items!!.size < MAX_ITEM_PER_PAGE)
                isLoading = false
        } else {

            this.items!!.retainAll(items!!)

            isLoading = false

            notifyDataSetChanged()
        }
    }

    /**
     * Update each item in the items childList
     */
    fun updateEachItem(items: List<E>?) {

        isLoading = true

        if (this.items == null) {
            this.items = mutableListOf()
            this.items!!.addAll(items!!)

            if (this.items!!.size < MAX_ITEM_PER_PAGE)
                isLoading = false
        } else {

            items?.forEach {
                updateItem(it)
            }

            isLoading = false
        }
    }

    fun setItems(itemsToAdd: List<E>, page: Int) {
        if (page == 0) {
            this.items = ArrayList()
            this.items!!.addAll(itemsToAdd)
            notifyItemRangeInserted(0, items!!.size)
        } else {
            this.items?.let { items ->
                items.addAll(itemsToAdd)
                notifyItemRangeInserted(items.size - itemsToAdd.size, items.size)
            }
        }
    }

    fun setItemsAtTop(itemsToAdd: List<E>, page: Int, recyclerView: RecyclerView) {
        if (page == 0) {
            // Clear items for the first page
            this.items = ArrayList()
            this.items!!.addAll(itemsToAdd)
            notifyDataSetChanged()
        } else {
            // Add new items at the top for subsequent pages
            val layoutManager = recyclerView.layoutManager as LinearLayoutManager
            val firstVisiblePosition = layoutManager.findFirstVisibleItemPosition()
            val offset = recyclerView.getChildAt(0)?.top ?: 0

            this.items?.let { currentItems ->
                currentItems.addAll(0, itemsToAdd)
                notifyItemRangeInserted(0, itemsToAdd.size)

                // Maintain the scroll position after adding new items
                layoutManager.scrollToPositionWithOffset(
                    firstVisiblePosition + itemsToAdd.size,
                    offset
                )
            }
        }
    }

    /*fun setItems(items: List<E>?, page: Int) {
        isLoading = true

        if (page == 1) {
            if (this.items == null)
                this.items = mutableListOf()
            else
                this.items!!.clear()
            this.items!!.addAll(items!!)

            if (this.items!!.size < MAX_ITEM_PER_PAGE)
                isLoading = false

            notifyDataSetChanged()
        } else {

            if (this.items == null)
                this.items = mutableListOf()

            if (this.items != null) {
                if (items != null && !items.isEmpty())
                    this.items!!.addAll(items)
                else
                    isLoading = false

                notifyDataSetChanged()
            }
        }
    }*/

    fun setItemsWithPage(items: List<E>?, page: Int) {

        isLoading = true

        if (page == 1) {
            if (this.items == null)
                this.items = mutableListOf()
            else
                this.items!!.clear()
            this.items!!.addAll(items!!)

            // item is less then per page items
            if (this.items!!.size < MAX_ITEM_PER_PAGE)
                isLoading = false

            notifyDataSetChanged()
        } else {

            if (this.items != null) {

                if (items != null && !items.isEmpty()) {

                    if (this.items!!.size >= page * MAX_ITEM_PER_PAGE) {

                        val top = (page - 1) * MAX_ITEM_PER_PAGE
                        val bottom = items.size
                        var i = top
                        var j = 0
                        while (i < bottom) {
                            this.items!![i] = items[j]
                            i++
                            j++
                        }

                    } else
                        this.items!!.addAll(items)

                } else
                    isLoading = false

                notifyDataSetChanged()
            }
        }
    }

    fun removeItem(e: E) {
        val i = items!!.indexOf(e)
        if (i > -1 && i < items!!.size) {
            items!!.removeAt(i)
            notifyItemRemoved(i)
        }
    }

    fun removeItemAt(postition: Int) {
        if (postition > -1 && postition < items!!.size) {
            items!!.removeAt(postition)
            notifyItemRemoved(postition)
        }
    }

    fun addItem(e: E) {
        if (items == null) {
            items = mutableListOf()
            isLoading = false
        }

        items!!.add(e)
        notifyItemInserted(items!!.size)
    }

    fun addItem(e: E, position: Int) {

        if (items == null) {
            items = mutableListOf()
            isLoading = false
            items!!.add(e)
            notifyDataSetChanged()
        } else {
            items!!.add(position, e)
            if (items!!.size == 1)
                notifyDataSetChanged()
            else
                notifyItemInserted(position)
        }

    }

    fun itemChanged(e: E) {
        if (items != null) {
            val index = items!!.indexOf(e)
            if (index > -1) {
                items!![index] = e
                notifyItemChanged(index)
            }
        }
    }

    fun updateItem(e: E) {
        if (items != null) {
            val index = items!!.indexOf(e)
            if (index > -1) {
                items!![index] = e
                notifyDataSetChanged()
            }
        }
    }

    fun updateItemAt(newItem: E, position: Int) {
        if (position in items!!.indices) { // Check if position is valid
            items!![position] = newItem // Replace the item
            notifyItemChanged(position) // Notify adapter about the change
        }
    }


    fun updateItem(item: E, predicate: (E) -> Boolean) {
        val index = items!!.indexOfFirst(predicate)
        if (index != -1) {
            items!![index] = item
            notifyItemChanged(index)
        }
    }

    fun updateItem(predicate: (E) -> Boolean, itemToUpdate: (E) -> Unit) {
        val index = items!!.indexOfFirst(predicate)
        if (index != -1) {
            itemToUpdate(items!![index])
            notifyItemChanged(index)
        }
    }

    fun removeItem(predicate: (E) -> Boolean) {
        val index = items!!.indexOfFirst(predicate)
        if (index != -1) {
            this.items!!.removeAt(index)
            notifyItemRemoved(index)
        }
    }

    fun clearAllItem() {
        if (items != null) {
            items!!.clear()
            // Commented
            // isLoading = true

            // Written
            isNoData = true
            notifyDataSetChanged()
        }
    }

    /*fun getItems(): List<E>? {
        return items
    }*/

    /*fun setItems(items: List<E>?) {

        if (this.items == null)
            this.items = ArrayList()
        else
            this.items!!.clear()

        if (items != null)
            this.items!!.addAll(items)

        isLoading = false
        notifyDataSetChanged()
    }*/

    /*fun getErrorMessage(): String {
        return errorMessage
    }*/

    /*fun setErrorMessage(errorMessage: String) {
        this.errorMessage = errorMessage
        isLoading = false
        isNoData = true
        notifyDataSetChanged()
    }*/

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseHolder<E> {
        val baseHolder: BaseHolder<E> = when (viewType) {
            333 -> createLoadingHolder(parent, viewType)
            111 -> createNoDataHolder(parent)
            else -> createDataHolder(parent, viewType)
        }

        baseHolder.hasItem = this

        return baseHolder
    }

    fun createNoDataHolder(parent: ViewGroup): NoDataHolder<E> {
        val textView = TextView(parent.context)
        textView.gravity = Gravity.CENTER
        val dimensionPixelSizeHorizontal = parent.resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._15sdp)
        val dimensionPixelSizeVertical = parent.resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._38sdp)
        textView.setPaddingRelative(
            dimensionPixelSizeHorizontal,
            dimensionPixelSizeVertical,
            dimensionPixelSizeHorizontal,
            dimensionPixelSizeVertical
        )
        val layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )

        textView.layoutParams = layoutParams

        //View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_no_data, parent, false);
//        return NoDataHolder(textView)
        return NoDataHolder(parent.toBinding())
    }

    fun createLoadingHolder(parent: ViewGroup, viewType: Int): LoadingHolder<E> {
        val progressBar = ProgressBar(parent.context, null, android.R.attr.progressBarStyleSmall)

        val layoutParams = parent.layoutParams
        layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
        layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
        progressBar.layoutParams = layoutParams

        if (layoutParams is LinearLayout.LayoutParams) {
            layoutParams.gravity = Gravity.CENTER
        }

        //Hide progress bar for now
        progressBar.visibility = loaderVisibility

        return LoadingHolder(progressBar)
    }

    abstract fun createDataHolder(parent: ViewGroup, viewType: Int): H

    override fun getItemViewType(position: Int): Int {
        if (isNoData && isLoading)
            return 333
        else if (!isLoading && isNoData)
            return 111
        else if (isLoading && items?.size == position)
            return 333

        return getViewType(position)
    }


    override fun onBindViewHolder(
        holder: BaseHolder<*>,
        position: Int,
        payloads: MutableList<Any>
    ) {
        val itemViewType = getItemViewType(position)
        if (itemViewType != 111 && itemViewType != 333) {
            val item = getItem(position)
            // actual view
            if (payloads.size != 0) {
                onBindDataHolder(holder as H, position, item, payloads)
            } else {
                onBindDataHolder(holder as H, position, item)
            }
        }

        if (itemViewType == 111 && holder is NoDataHolder) {
            holder.setErrorText(errorMessage)
        }
        super.onBindViewHolder(holder, position, payloads)
    }

    open fun getViewType(position: Int): Int {
        return 222
    }

    override fun onBindViewHolder(holder: BaseHolder<*>, position: Int) {

        /*val itemViewType = getItemViewType(position)

        if (itemViewType != 111 && itemViewType != 333) {
            val item = getItem(position)
            // actual view
            onBindDataHolder(holder as H, position, item)
        }

        if (itemViewType == 111 && holder is NoDataHolder) {
            holder.setErrorText(errorMessage)
        }*/
    }

    open fun onBindDataHolder(holder: H, position: Int, item: E) {
        holder.bind(item)
    }

    open fun onBindDataHolder(holder: H, position: Int, item: E, payloads: MutableList<Any>) {
    }

    fun getItemIndex(item: E?): Int {
        return items?.indexOf(item) ?: run { -1 }
    }

    override fun getItem(index: Int): E {
        if (isLoading && items!!.size == index) {
            var i = index - 1
            i = if (i < 0) 0 else i
            return items!![i]
        }

        if (index == items?.size) {
            return items!![0]
        }

        if (index == items?.size!! + 1) {
            return items!![0]
        }
        return items!![index]
    }

    override fun getItemCount(): Int {
        isNoData = items == null || items!!.isEmpty()
        return when {
            isNoData -> 1
            isLoading -> items!!.size + 1
            else -> items!!.size
        }
    }

    protected fun makeView(parent: ViewGroup, @LayoutRes layout: Int): View {
        return LayoutInflater.from(parent.context).inflate(layout, parent, false)
    }

    fun displayErrorMessageInCentre(gridLayoutManager: GridLayoutManager) {
        if (items!!.isEmpty()) {
            gridLayoutManager.spanSizeLookup =
                object : GridLayoutManager.SpanSizeLookup() {
                    override fun getSpanSize(position: Int): Int {
                        return gridLayoutManager.spanCount
                    }
                }
        }
    }

    // Single selection
    protected var selectedItemPos = -1
    protected var lastItemSelectedPos = -1

    protected fun selectSingleItem(
        adapterPosition: Int,
        onSingleItemSelected: (prevSelectedItem: E) -> Unit
    ) {
        selectedItemPos = adapterPosition
        if (lastItemSelectedPos == -1) {
            lastItemSelectedPos = selectedItemPos
        } else if (lastItemSelectedPos != selectedItemPos) {
            onSingleItemSelected(items!![lastItemSelectedPos])
            notifyItemChanged(lastItemSelectedPos)
            lastItemSelectedPos = selectedItemPos
        }
    }

    companion object {
        protected var MAX_ITEM_PER_PAGE = 10
    }
}