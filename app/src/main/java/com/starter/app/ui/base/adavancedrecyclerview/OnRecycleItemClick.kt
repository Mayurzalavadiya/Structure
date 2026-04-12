package com.starter.app.ui.base.adavancedrecyclerview

import android.view.View

interface OnRecycleItemClick<T> {
    fun onClick(t: T?, view: View)
}
