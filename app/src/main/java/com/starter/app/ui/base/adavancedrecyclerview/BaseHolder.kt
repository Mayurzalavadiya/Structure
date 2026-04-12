package com.starter.app.ui.base.adavancedrecyclerview

import android.content.Context
import android.view.View
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.FontRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView

abstract class BaseHolder<E> : RecyclerView.ViewHolder, View.OnClickListener {

    private var onRecycleItemClick: OnRecycleItemClick<E>? = null
    var hasItem: HasItem<E>? = null

    val current: E
        get() = hasItem!!.getItem(layoutPosition)

    constructor(itemView: View) : super(itemView) {
        itemView.setOnClickListener(this)
    }

    constructor(itemView: View, onRecycleItemClick: OnRecycleItemClick<E>) : super(itemView) {
        this.onRecycleItemClick = onRecycleItemClick
        itemView.setOnClickListener(this@BaseHolder)
    }

    override fun onClick(v: View) {
        onRecycleItemClick?.onClick(current, v)
    }

    /**
     * It will return context
     */
    protected val context: Context
        get() = itemView.context
//    protected fun getContext(): Context = itemView.context

    /**
     * It is use to get string
     */
    protected fun getString(@StringRes resId: Int) = context.getString(resId)

    /**
     * It is use to get color
     */
    protected fun getColor(@ColorRes resId: Int) = ContextCompat.getColor(context, resId)

    /**
     * It is use to get drawable
     */
    protected fun getDrawable(@DrawableRes resId: Int) =
        ContextCompat.getDrawable(context, resId)

    protected fun getFont(@FontRes resId: Int) = ResourcesCompat.getFont(context, resId)

    /**
     * Implement this method
     */
    abstract fun bind(item: E)
}