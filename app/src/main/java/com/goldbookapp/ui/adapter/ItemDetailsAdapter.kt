package com.goldbookapp.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.Filterable
import androidx.annotation.LayoutRes
import com.goldbookapp.R
import com.goldbookapp.model.ItemSearchModel
import com.goldbookapp.model.SearchItemModule
import com.goldbookapp.utils.Constants
import kotlinx.android.synthetic.main.search_item_popup.view.*


class ItemDetailsAdapter(
    context: Context,
    private val isFromPayRec: Boolean,
    @LayoutRes private val layoutResource: Int,
    private val allitems: List<ItemSearchModel.ItemSearch>
):
    ArrayAdapter<ItemSearchModel.ItemSearch>(context, layoutResource, allitems),
    Filterable {

    private lateinit var fileterditems:ArrayList<ItemSearchModel.ItemSearch> /*ArrayList<SearchItemModule.Data61082640>()*/

    override fun getCount() = if(this::fileterditems.isInitialized) fileterditems.size else 0
    override fun getItem(position: Int) = fileterditems[position]


    override fun getView(
        position: Int,
        view: View?,
        parent: ViewGroup
    ): View {
        var convertview = view
        if (view == null) {
            convertview = LayoutInflater.from(context).inflate(R.layout.search_item_popup, parent, false)

        }

        convertview?.tv_searchitem_right_series?.text
        convertview?.tv_searchitem_left_item_name?.text = "${fileterditems[position].item_name}"
        if(isFromPayRec){
            convertview?.tv_seachitem_left_category?.visibility = View.VISIBLE
            convertview?.tv_seachitem_left_category?.text = "Category: ${fileterditems[position].category_name}"
        }
        else{
            convertview?.tv_seachitem_left_category?.visibility = View.GONE
        }

        return convertview!!
    }

    override fun getFilter() = filter
    private var filter: Filter = object : Filter() {

        override fun performFiltering(constraint: CharSequence?): Filter.FilterResults {
            val results = FilterResults()

            val query =
                if (constraint != null && constraint.isNotEmpty()) autocomplete(constraint.toString())
                else arrayListOf()

            results.values = query
            results.count = query.size

            return results
        }

        private fun autocomplete(input: String): ArrayList<ItemSearchModel.ItemSearch> {
            val results = arrayListOf<ItemSearchModel.ItemSearch>()
            for (item in allitems) {
                if (item.item_name?.toLowerCase()
                        ?.contains(input.toLowerCase())!!
                ) results.add(item)
            }

            return results
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            fileterditems = results?.values as ArrayList<ItemSearchModel.ItemSearch>
            if (results?.count!! > 0) {
                notifyDataSetChanged()
            }
            else notifyDataSetInvalidated()
        }

    }


}
