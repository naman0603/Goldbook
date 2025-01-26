package com.goldbookapp.searchablespinner

import android.widget.Filter

class SearchableFilter constructor(
    val mAdapter: SearchableAdapter,
    val itemList: List<Any>
) : Filter() {

    override fun performFiltering(constraint: CharSequence?): FilterResults {
        val results = FilterResults()
        var filteredList = itemList

        if (constraint?.length == 0) {
            results.values = itemList
        } else {
            filteredList = itemList.filter {
                it.toString().toLowerCase().contains(constraint.toString().toLowerCase())
            }
            results.values = filteredList
        }
        results.count = filteredList.size
        return results
    }

    override fun publishResults(
        constraint: CharSequence?,
        results: FilterResults?
    ) {
        mAdapter.setFilteredList(results?.values as List<Any>)
        mAdapter.notifyDataSetChanged()
    }
}
