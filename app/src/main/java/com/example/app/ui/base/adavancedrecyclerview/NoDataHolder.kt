package com.example.app.ui.base.adavancedrecyclerview

import com.example.app.databinding.AdvancedRecyclerViewNoDataHolderBinding


class NoDataHolder<E>(private val binding: AdvancedRecyclerViewNoDataHolderBinding) :
    BaseHolder<E>(binding.root) {

    /*var errorTextView: TextView? = null

    init {
        errorTextView = itemView as TextView
    }*/

    fun setErrorText(errorText: String) = with(binding) {
        textViewErrorMessage.text = errorText
    }

    override fun bind(item: E) {

    }
}

/*
class NoDataHolder<E>(itemView: View) : BaseHolder<E>(itemView) {
    var errorTextView: TextView? = null

    init {
        errorTextView = itemView as TextView
    }

    fun setErrorText(errorText: String) {
        if (errorTextView != null) {
            errorTextView!!.text = errorText
        }
    }

    override fun bind(item: E) {

    }
}*/
