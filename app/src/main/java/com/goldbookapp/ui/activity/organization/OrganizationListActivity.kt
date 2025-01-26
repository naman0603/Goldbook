package com.goldbookapp.ui.activity.organization

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.NetworkUtils
import com.goldbookapp.R
import com.goldbookapp.api.ApiHelper
import com.goldbookapp.api.RetrofitBuilder
import com.goldbookapp.databinding.ActivityOrganizationListBinding
import com.goldbookapp.model.GetUserCompaniesModel
import com.goldbookapp.model.LoginModel
import com.goldbookapp.model.UserWiseRestrictionModel
import com.goldbookapp.ui.AccessDeniedActivity
import com.goldbookapp.ui.activity.viewmodel.OrganizationViewModel
import com.goldbookapp.ui.activity.viewmodel.ViewModelFactory
import com.goldbookapp.ui.adapter.OrganizationsListAdapter
import com.goldbookapp.utils.*
import com.goldbookapp.utils.CommonUtils.Companion.clickWithDebounce
import com.goldbookapp.utils.PreferenceHelper.get
import com.google.gson.Gson
import com.hamzasharuf.networkmonitor.ConnectivityStateHolder
import com.hamzasharuf.networkmonitor.Event
import com.hamzasharuf.networkmonitor.NetworkEvents
import kotlinx.android.synthetic.main.activity_organization_list.*
import kotlinx.android.synthetic.main.toolbar_layout.view.*

class OrganizationListActivity : AppCompatActivity() {

    private lateinit var viewModel: OrganizationViewModel

    lateinit var binding: ActivityOrganizationListBinding
    private var viewDetail: Boolean = false

    private lateinit var adapter: OrganizationsListAdapter

    lateinit var loginModel: LoginModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_organization_list)

        val view = binding.root

        setupViewModel()
        setupUIandListner(view)

    }
    private fun setupViewModel() {
        viewModel =
            ViewModelProvider(this, ViewModelFactory(ApiHelper(RetrofitBuilder.apiService))).get(
                OrganizationViewModel::class.java
            )
        binding.setLifecycleOwner(this)

    }
    fun setupUIandListner(root: View){

        val prefs = PreferenceHelper.defaultPrefs(this)
        loginModel = Gson().fromJson(
            prefs[Constants.PREF_LOGIN_DETAIL_KEY, ""],
            LoginModel::class.java
        ) //getter

        binding.root.imgLeft.setImageResource(R.drawable.ic_back)
        binding.root.tvTitle.setText(getString(R.string.organization))


        root.imgLeft.clickWithDebounce {
            onBackPressed()
        }
        binding.recyclerViewOrgs.layoutManager = LinearLayoutManager(this)
        adapter = OrganizationsListAdapter(arrayListOf(),viewDetail)
        binding.recyclerViewOrgs.adapter = adapter



        //hides keyboard on focus change from editext(search) to tap anywhere in the screen
        root.isClickable = true
        root.isFocusable = true
        root.isFocusableInTouchMode = true
        root.setOnFocusChangeListener { v, hasFocus -> CommonUtils.hideKeyboardnew(this)  }

    }


    override fun onDestroy() {
        super.onDestroy()
        CommonUtils.hideProgress()
    }
    override fun onResume() {
        super.onResume()

        NetworkEvents.observe(this, Observer {
            if (it is Event.ConnectivityEvent) {
                handleConnectivityChange()
            }
        })

    }

    private fun handleConnectivityChange() {
        if (ConnectivityStateHolder.isConnected) {
            // Network is available
            CommonUtils.hideInternetDialog()

            when(loginModel.data!!.user_info!!.user_type.equals("user",true)){
                // user type user
                true -> {
                    // apply restriciton
                    defaultDisableAllButtonnUI()
                    addOrg.clickWithDebounce {
                        startActivity(
                            Intent(
                                this,
                                NewOrganizationActivity::class.java
                            )
                        )
                    }

                    userWiseRestriction(loginModel.data?.bearer_access_token)
                }
                // user_type -> admin or super_admin or any other
                false -> {
                    defaultEnableAllButtonnUI()
                    userLimitAccess(loginModel?.data?.bearer_access_token)
                    //recyclerviewsetup
                }
            }



            getUserCompaniesAPI( loginModel.data?.bearer_access_token,
                loginModel.data?.company_info?.id)
        }

        if (!ConnectivityStateHolder.isConnected) {
            // Network is not available
            CommonUtils.showDialog(this, getString(R.string.please_check_internet_msg))

        }
    }
    private fun userWiseRestriction(token: String?) {
        if (NetworkUtils.isConnected()) {
            viewModel.userWiseRestriction(token)
                .observe(this, Observer {
                    it?.let { resource ->
                        when (resource.status) {

                            Status.SUCCESS -> {
                                if (it.data?.status == true) {
                                    applyUserWiseRestriction(it.data.data)
                                } else {
                                    when (it.data!!.code == Constants.ErrorCode) {
                                        true -> {
                                            Toast.makeText(
                                                this,
                                                it.data.errormessage.message,
                                                Toast.LENGTH_LONG
                                            )
                                                .show()
                                        }
                                        false -> {
                                            CommonUtils.somethingWentWrong(this)
                                        }

                                    }
                                }
                                CommonUtils.hideProgress()
                            }
                            Status.ERROR -> {
                                CommonUtils.hideProgress()
                            }
                            Status.LOADING -> {
                                CommonUtils.showProgress(this)
                            }
                        }
                    }
                })
        }
    }

    private fun applyUserWiseRestriction(data: UserWiseRestrictionModel.Data) {
        for (i in 0 until data.permission!!.size) {
            if (data.permission!!.get(i).startsWith(getString(R.string.org))) {
                // Restriction check for Customerl
                when (data.permission!!.get(i).endsWith(getString(R.string.add), true)) {
                    true -> {
                        //binding.addOrg.visibility = View.VISIBLE
                    }
                    else->{

                    }
                }
            }
            if (data.permission!!.get(i).startsWith(getString(R.string.org))) {
                // Restriction check for Customerl
                when (data.permission!!.get(i).endsWith(getString(R.string.details), true)) {
                    true -> {
                        viewDetail = true
                    }
                    else->{

                    }
                }
            }
        }
    }

    private fun defaultEnableAllButtonnUI() {
        addOrg.visibility = View.VISIBLE
        viewDetail = true
    }

    private fun defaultDisableAllButtonnUI() {
        addOrg.visibility = View.GONE
        viewDetail = false
    }

    private fun userLimitAccess(token: String?) {
        if (NetworkUtils.isConnected()) {
            viewModel.userLimitAccess(token)
                .observe(this, Observer {
                    it?.let { resource ->
                        when (resource.status) {
                            Status.SUCCESS -> {

                                if (it.data?.status == true) {
                                    val message_company = it.data.data!!.message_company
                                    //restrictUsertoAddOrg(it.data.data!!.can_add_company)
                                    if(it.data.data!!.can_add_company.equals("0")){
                                        addOrg.clickWithDebounce {

                                            startActivity(
                                            Intent(
                                                this,
                                                AccessDeniedActivity::class.java
                                            ).putExtra(Constants.isFromListRestrict, Constants.isFromListRestrict).putExtra(Constants.restrict_msg,message_company)
                                        )

                                        }
                                    }
                                    else{
                                        addOrg.clickWithDebounce {

                                            startActivity(Intent(this, NewOrganizationActivity::class.java).setFlags(
                                            Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_NEW_TASK))}
                                    }

                                } else {

                                }


                            }
                            Status.ERROR -> {

                            }
                            Status.LOADING -> {

                            }
                        }
                    }
                })
        }
    }


    fun getUserCompaniesAPI( token: String?, companyID: String?){

        if(NetworkUtils.isConnected()) {

            viewModel.getUserCompanies(token, companyID)
                .observe(this, Observer {
                    it?.let { resource ->
                        when (resource.status) {
                            Status.SUCCESS -> {
                                if (it.data?.status == true) {



                                    //val nums = listOf(it.data?.data)
                                    //val listSize: Int? = it.data?.data?.size

                                    if(it.data.data?.company?.size != 0){
                                        recyclerViewOrgs.visibility = View.VISIBLE
                                        tvNoRecordOrgs.visibility = View.GONE
                                    }else{
                                        tvNoRecordOrgs.text = it.data.message

                                        recyclerViewOrgs.visibility = View.GONE
                                        tvNoRecordOrgs.visibility = View.VISIBLE
                                    }
                                    retrieveList(it.data.data?.company)

                                } else {

                                    when(it.data!!.code == Constants.ErrorCode){
                                        true-> {
                                            Toast.makeText(
                                                this,
                                                it.data.errormessage?.message,
                                                Toast.LENGTH_LONG
                                            )
                                                .show()
                                        }
                                        false->{
                                            CommonUtils.somethingWentWrong(this)
                                        }

                                    }
                                }
                                CommonUtils.hideProgress()

                            }
                            Status.ERROR -> {

                                CommonUtils.hideProgress()

                            }
                            Status.LOADING -> {
                                    CommonUtils.showProgress(this)
                            }
                        }
                    }
                })
        }
    }
    private fun retrieveList(orgsList: List<GetUserCompaniesModel.Data.Companies>?) {
        adapter.apply {
            addOrg(orgsList as ArrayList<GetUserCompaniesModel.Data.Companies>?, viewDetail)
            notifyDataSetChanged()
        }
    }


    fun orgDetails(id: String?){
        startActivity(Intent(this, OrganizationDetailActivity::class.java).putExtra("company_id",id ))
    }


}