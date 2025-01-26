import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.goldbookapp.R
import com.goldbookapp.model.UserCompanyListModel
import com.goldbookapp.ui.MainActivity
import com.goldbookapp.utils.CommonUtils.Companion.clickWithDebounce
import com.goldbookapp.utils.Constants
import hk.ids.gws.android.sclick.SClick
import kotlinx.android.synthetic.main.row_drawer_companylist.view.*

class DrawerCompanyListAdapter(
    private val companyList: ArrayList<UserCompanyListModel.Company968753762>,
    var currentCompName: String
) : RecyclerView.Adapter<DrawerCompanyListAdapter.DataViewHolder>(){

    class DataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(
            companyinfo: UserCompanyListModel.Company968753762,
            position: Int,
            currentCompName: String
        ) {
            itemView.apply {


                when(currentCompName.equals(companyinfo.company_name,ignoreCase = true)){
                    true->{
                        lldrawercompanylist.setBackgroundResource(R.drawable.round_rect)
                    }
                    false->{
                        lldrawercompanylist.setBackgroundResource(R.drawable.md_transparent)
                    }
                }
                tvCompanyNAmeDrawer.text = companyinfo.company_name
                tvLAstEntryDateDrawer.text = companyinfo.created_at
                cardCompanyNameDrawer.clickWithDebounce {

                    (context as MainActivity).setSelectedCompanyName(position)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataViewHolder =
        DataViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.row_drawer_companylist, parent, false))

    override fun getItemCount(): Int = companyList.size

    override fun onBindViewHolder(holder: DataViewHolder, position: Int) {
        holder.bind(companyList[position],position,currentCompName)
    }
    fun addCompanies(
        companyList: ArrayList<UserCompanyListModel.Company968753762>?, isFromDemoCompany: Boolean
    ) {
        this.companyList.apply {
            clear()
            if(isFromDemoCompany){
                for (company in companyList!!){
                    if(company.id == Constants.DemoCompanyID){

                    }
                    else companyList.remove(company)
                }
            }
            else{
                if (companyList != null) {
                    for (company in companyList!!){
                        if(company.id == Constants.DemoCompanyID){
                            companyList.remove(company)
                        }
                    }
                    addAll(companyList)

                }
            }

        }

    }

}

