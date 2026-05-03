package com.example.app.utils

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

abstract class PaginationScrollListener(private val layoutManager: LinearLayoutManager) :
    RecyclerView.OnScrollListener() {


    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        if (dy > 0) {

            val visibleItemCount: Int = layoutManager.childCount
            val totalItemCount: Int = layoutManager.itemCount
            val pastVisibleItem: Int = layoutManager.findFirstVisibleItemPosition()
            if (!isLoading() && !isLastPage()) {
                if ((visibleItemCount + pastVisibleItem) >= totalItemCount
                    && pastVisibleItem >= 0
                ) {
                    loadMoreItems()
                }
            }
            super.onScrolled(recyclerView, dx, dy)
        }

    }

    protected abstract fun loadMoreItems()
    abstract fun isLastPage(): Boolean
    abstract fun isLoading(): Boolean

}