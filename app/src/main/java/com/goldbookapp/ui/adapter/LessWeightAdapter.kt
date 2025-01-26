package com.goldbookapp.ui.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.goldbookapp.R
import com.goldbookapp.model.AddLessWeightModel
import com.goldbookapp.ui.activity.additem.AddLessWeightActivity
import com.goldbookapp.ui.activity.additem.LessWeightDetailsActivity
import com.goldbookapp.utils.CommonUtils.Companion.clickWithDebounce
import com.goldbookapp.utils.Constants
import com.google.gson.Gson
import kotlinx.android.synthetic.main.row_lessweight_list.view.*

class LessWeightAdapter(
    private val mList: List<AddLessWeightModel.AddLessWeightModelItem>,
    private val isFromEdit: Boolean
) :
    RecyclerView.Adapter<LessWeightAdapter.ViewHolder>() {

    // create new views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // inflates the card_view_design view
        // that is used to hold list item
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_lessweight_list, parent, false)

        return ViewHolder(view)
    }

    // binds the list items to a view
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {


        val ItemsViewModel = mList[position]


        holder.bind(mList[position], position, isFromEdit)

    }

    // return the number of the items in the list
    override fun getItemCount(): Int {
        return mList.size
    }

    // Holds the views for adding it to image and text
    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        fun bind(
            itemViewModel: AddLessWeightModel.AddLessWeightModelItem,
            position: Int,
            isFromEdit: Boolean

        ) {
            itemView.apply {


                tvItemNameLessWeight.setText(itemViewModel.less_wt_item_name)
                tvWeightLessWeight.setText("Weight: "+itemViewModel.less_wt_less_wt)
                tvFinalWtLessWeight.setText("Final Wt: "+itemViewModel.less_wt_final_wt)
                tvTotalAmtLessWeight.setText("Total Amt: "+itemViewModel.less_wt_total_amount)


                when (isFromEdit) {
                    true -> {
                            //Edit Less Weight from edit
                        imgRight2editlessWeight.clickWithDebounce {
                            imgRight2editlessWeight.context.startActivity(
                                Intent(
                                    imgRight2editlessWeight.context,
                                    AddLessWeightActivity::class.java
                                )
                                    .putExtra(Constants.PREF_LESS_WEIGHT_POS, position)
                                    //2->Edit LessWeight From New
                                    .putExtra(Constants.LESS_WEIGHT_SAVE_TYPE, "2")
                                    .putExtra(
                                        Constants.PREF_LESS_WEIGHT_INFO_EDITKEY,
                                        Gson().toJson(itemViewModel)
                                    )
                            )
                        }

                        imgRightdellessWeight.clickWithDebounce {
                            (context as LessWeightDetailsActivity).deleteLessWeight(position, false)
                        }


                    }
                    false -> {
                        //Edit Less Weight from new
                        imgRight2editlessWeight.clickWithDebounce {
                            imgRight2editlessWeight.context.startActivity(
                                Intent(
                                    imgRight2editlessWeight.context,
                                    AddLessWeightActivity::class.java
                                )
                                    .putExtra(Constants.PREF_LESS_WEIGHT_POS, position)
                                    //2->Edit LessWeight From New
                                    .putExtra(Constants.LESS_WEIGHT_SAVE_TYPE, "3")
                                    .putExtra(
                                        Constants.PREF_LESS_WEIGHT_INFO_KEY,
                                        Gson().toJson(itemViewModel)
                                    )
                            )
                        }

                        imgRightdellessWeight.clickWithDebounce {
                            (context as LessWeightDetailsActivity).deleteLessWeight(position, true)
                        }
                    }
                }
            }

        }

    }
}



