package com.starter.app.ui.base.adavancedrecyclerview

import androidx.annotation.IntDef

abstract class AbstractSelectableAdapter<H : SelectableViewHolder<E>, E : AbstractSelectableAdapter.Selectable> : AdvanceRecycleViewAdapter<H, E>,
    SelectionActionPerformer<E> {

    var previousSelectedItem: E? = null
        private set
    var previousSelectedItemIndex = -1
        private set

    @SelectionMode
    @get:SelectionMode
    var selectionModes = SINGLE
        private set

    private var onSelectionChangeListener: OnSelectionChangeListener<E>? = null

    val selectedItems: ArrayList<E>
        get() {
            val selectedElements = ArrayList<E>()
            if (items != null) {
                for (e in items!!) {
                    if (e.isSelected)
                        selectedElements.add(e)
                }
            }
            return selectedElements
        }

    fun getSelectedItems(listItems: List<E>?): ArrayList<E> {
        val selectedElements = ArrayList<E>()
        if (listItems != null) {
            for (e in listItems) {
                if (e.isSelected)
                    selectedElements.add(e)
            }
        }
        return selectedElements
    }

    @Retention(AnnotationRetention.SOURCE)
    @IntDef(NONE, SINGLE, MULTI)
    annotation class SelectionMode {
    }

    constructor(@SelectionMode selectionModes: Int) {
        this.selectionModes = selectionModes
    }

    constructor(items: MutableList<E>, @SelectionMode selectionModes: Int) : super(items) {
        this.selectionModes = selectionModes
    }

    fun addSelectionChangeListener(onSelectionChangeListener: OnSelectionChangeListener<E>) {
        this.onSelectionChangeListener = onSelectionChangeListener
    }

    override fun select(index: Int) {
        var index = index

        if (index < 0)
            index = 0

        val item = getItem(index)

        if (selectionModes == SINGLE) {
            if (previousSelectedItem != null) {
                previousSelectedItem?.isSelected = false
                notifyItemChanged(previousSelectedItemIndex)
            }
        }

        item.isSelected = true


        onSelectionChangeListener?.onSelectionChange(item, true)

        notifyDataSetChanged()

        previousSelectedItem = item
        previousSelectedItemIndex = index
    }

    fun select(e: E) {
        val i = items!!.indexOf(e)
        select(i)
    }

    override fun deSelect(index: Int) {
        val item = getItem(index)
        item.isSelected = false

        onSelectionChangeListener?.onSelectionChange(item, false)

        //notifyItemChanged(index)
        notifyDataSetChanged()
    }

    override fun onBindDataHolder(holder: H, position: Int, item: E) {

        onBindSelectedViewHolder(holder, position, item)
        if (previousSelectedItem == null && item.isSelected) {
            previousSelectedItem = item
            previousSelectedItemIndex = position
        }
    }

    abstract fun onBindSelectedViewHolder(holder: H, position: Int, item: E)

    interface Selectable {
        var isSelected: Boolean
    }

    companion object {
        const val NONE = -1
        const val SINGLE = 0
        const val MULTI = 1
    }
}
