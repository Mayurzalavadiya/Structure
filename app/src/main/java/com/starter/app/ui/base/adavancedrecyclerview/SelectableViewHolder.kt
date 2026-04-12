package com.starter.app.ui.base.adavancedrecyclerview

import android.view.View

open class SelectableViewHolder<E : AbstractSelectableAdapter.Selectable> : BaseHolder<E> {

    internal var actionPerformer: SelectionActionPerformer<E>

    val currentItem: E
        get() = actionPerformer.getItem(layoutPosition)

    val selectionMode: Int
        @AbstractSelectableAdapter.SelectionMode
        get() = actionPerformer.selectionMode


    constructor(itemView: View, actionPerformer: SelectionActionPerformer<E>) : super(itemView) {
        this.actionPerformer = actionPerformer
    }

    constructor(itemView: View, actionPerformer: SelectionActionPerformer<E>, onRecycleItemClick: OnRecycleItemClick<E>) : super(itemView, onRecycleItemClick) {
        this.actionPerformer = actionPerformer
    }

    fun select() {
        actionPerformer.select(layoutPosition)
    }

    fun deSelect() {
        actionPerformer.deSelect(layoutPosition)
    }

    override fun bind(item: E) {

    }
}