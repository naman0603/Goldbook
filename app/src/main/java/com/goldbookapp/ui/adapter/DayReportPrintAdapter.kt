package com.goldbookapp.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.goldbookapp.R
import com.goldbookapp.model.DayPrintModel
import kotlinx.android.synthetic.main.row_day_report.view.*

class DayReportPrintAdapter(private  val transactionsList: List<DayPrintModel.Data.Line_enries>?) : RecyclerView.Adapter<DayReportPrintAdapter.DataViewHolder>(){

    class DataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private lateinit var adapter: ContactReportPrintAdapter

        fun bind(
            itemModel: DayPrintModel.Data.Line_enries,
            isLastItem: Boolean
        ) {
            itemView.apply {

                day_tv_date.text = itemModel.date
                rv_day_trans.layoutManager = LinearLayoutManager(context)
                 adapter = ContactReportPrintAdapter(null,itemModel.entries,null,null, "2")
                rv_day_trans.adapter = adapter
                when(isLastItem){
                    true -> bottomline_dayreport.visibility = View.GONE
                    else->{

                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataViewHolder =
        DataViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.row_day_report, parent, false))

    override fun getItemCount(): Int = transactionsList!!.size

    override fun onBindViewHolder(holder: DataViewHolder, position: Int) {
        if(itemCount == position+1){
            holder.bind(this.transactionsList!![position],true)
        }
        else{
            holder.bind(this.transactionsList!![position],false)
        }

    }

}

