package com.goldbookapp.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.goldbookapp.R
import com.goldbookapp.ui.QuickSetupActivity
import com.goldbookapp.utils.CommonUtils.Companion.clickWithDebounce
import kotlinx.android.synthetic.main.row_suggestion_layout.view.*

class QuickSetupSuggestionAdapter(private val suggestions: ArrayList<String>) :
    RecyclerView.Adapter<QuickSetupSuggestionAdapter.DataViewHolder>() {

    class DataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(name: String) {
            itemView.apply {
                tvSuggestionName.text = name
                itemView.clickWithDebounce {

                    (context as QuickSetupActivity).setSuggestedUsername(name)
                }
            }


        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataViewHolder =
        DataViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.row_suggestion_layout, parent, false)
        )

    override fun getItemCount(): Int = suggestions.size

    override fun onBindViewHolder(holder: DataViewHolder, position: Int) {
        holder.bind(suggestions[position])
    }

    fun addSuggesstion(names: List<String>?) {
        this.suggestions.apply {
            clear()
            if (names != null) {
                addAll(names)
            }
        }

    }
}