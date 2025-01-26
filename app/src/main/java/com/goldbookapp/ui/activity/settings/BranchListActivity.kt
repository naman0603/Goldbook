package com.goldbookapp.ui.activity.settings

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
import com.goldbookapp.databinding.ActivityBranchlistBinding
import com.goldbookapp.model.BranchListModel
import com.goldbookapp.model.LoginModel
import com.goldbookapp.model.UserWiseRestrictionModel
import com.goldbookapp.ui.AccessDeniedActivity
import com.goldbookapp.ui.activity.branch.BranchDetailActivity
import com.goldbookapp.ui.MainActivity
import com.goldbookapp.ui.activity.BranchViewModel
import com.goldbookapp.ui.activity.branch.NewCompanyBranch
import com.goldbookapp.ui.activity.viewmodel.ViewModelFactory
import com.goldbookapp.ui.adapter.BranchListAdapter
import com.goldbookapp.utils.CommonUtils
import com.goldbookapp.utils.CommonUtils.Companion.clickWithDebounce
import com.goldbookapp.utils.Constants
import com.goldbookapp.utils.PreferenceHelper
import com.goldbookapp.utils.PreferenceHelper.get
import com.goldbookapp.utils.Status
import com.google.gson.Gson
import com.hamzasharuf.networkmonitor.ConnectivityStateHolder
import com.hamzasharuf.networkmonitor.Event
import com.hamzasharuf.networkmonitor.NetworkEvents
import kotlinx.android.synthetic.main.activity_branchlist.*
import kotlinx.android.synthetic.main.toolbar_layout.view.*

class BranchListActivity : AppCompatActivity() {

    private lateinit var viewModel: BranchViewModel

    lateinit var binding: ActivityBranchlistBinding

    private lateinit var adapter: BranchListAdapter

    lateinit var loginModel: LoginModel
    private var viewDetail: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_branchlist)


        val view = binding.root

        setupViewModel()
        setupUIandListner(view)


    }

    private fun setupViewModel() {
        viewModel =
            ViewModelProvider(this, ViewModelFactory(ApiHelper(RetrofitBuilder.apiService))).get(
                BranchViewModel::class.java
            )
        binding.setLifecycleOwner(this)

    }

    fun setupUIandListner(root: View) {

        val prefs = PreferenceHelper.defaultPrefs(this)
        loginModel = Gson().fromJson(
            prefs[Constants.PREF_LOGIN_DETAIL_KEY, ""],
            LoginModel::class.java
        ) //getter

        binding.root.imgLeft.setImageResource(R.drawable.ic_back)
        binding.root.tvTitle.setText(getString(R.string.branches))


        root.imgLeft.clickWithDebounce { onBackPressed() }

        binding.recyclerViewBranchList.layoutManager = LinearLayoutManager(this)
        adapter = BranchListAdapter(arrayListOf(), viewDetail)
        binding.recyclerViewBranchList.adapter = adapter


        //hides keyboard on focus change from editext(search) to tap anywhere in the screen
        root.isClickable = true
        root.isFocusable = true
        root.isFocusableInTouchMode = true
        root.setOnFocusChangeListener { v, hasFocus -> CommonUtils.hideKeyboardnew(this) }

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
                    userWiseRestriction(loginModel.data?.bearer_access_token)

                    addBranch.clickWithDebounce {
                        startActivity(
                            Intent(
                                this,
                                NewCompanyBranch::class.java
                            )
                        )
                    }

                }
                // user_type -> admin or super_admin or any other
                false -> {
                    defaultEnableAllButtonnUI()
                    userLimitAccess(loginModel?.data?.bearer_access_token)
                    //recyclerviewsetup
                }
            }



            getCompanyBranches(
                loginModel.data?.bearer_access_token,
                loginModel.data?.company_info?.id
            )
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
            if (data.permission!!.get(i).startsWith(getString(R.string.branch))) {
                // Restriction check for Customerl
                when (data.permission!!.get(i).endsWith(getString(R.string.add_edit), true)) {
                    true -> {
                       // binding.addBranch.visibility = View.VISIBLE
                    }
                    else->{

                    }
                }
            }
            if (data.permission!!.get(i).startsWith(getString(R.string.branch))) {
                // Restriction check for Customerl
                when (data.permission!!.get(i).endsWith(getString(R.string.list), true)) {
                    true -> {
                        viewDetail = true
                    }else->{

                }
                }
            }

        }
    }

    private fun defaultEnableAllButtonnUI() {
        addBranch.visibility = View.VISIBLE
        viewDetail = true
    }

    private fun defaultDisableAllButtonnUI() {
        addBranch.visibility = View.GONE
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
                                    //restrictUsertoAddBranch(it.data.data!!.can_add_branch)
                                    val message_branch = it.data.data!!.message_branch
                                    if (it.data.data!!.can_add_branch.equals("0")) {
                                        addBranch.clickWithDebounce {

                                            startActivity(
                                                Intent(
                                                    this,
                                                    AccessDeniedActivity::class.java
                                                ).setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_NEW_TASK)
                                                    .putExtra(
                                                        Constants.isFromListRestrict,
                                                        Constants.isFromListRestrict
                                                    )
                                                    .putExtra(
                                                        Constants.restrict_msg,
                                                        message_branch
                                                    )
                                            )

                                        }
                                    } else {
                                        addBranch.clickWithDebounce {
                                            startActivity(
                                                Intent(
                                                    this,
                                                    NewCompanyBranch::class.java
                                                )
                                            )
                                        }
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


    fun getCompanyBranches(token: String?, companyID: String?) {

        if (NetworkUtils.isConnected()) {

            viewModel.getCompanyBranches(token, companyID)
                .observe(this, Observer {
                    it?.let { resource ->
                        when (resource.status) {
                            Status.SUCCESS -> {
                                if (it.data?.status == true) {

                                    if (it.data.data?.branch?.size != 0) {
                                        binding.recyclerViewBranchList.visibility = View.VISIBLE
                                        tvNoRecordBranchList.visibility = View.GONE

                                    } else {

                                        tvNoRecordBranchList.text = it.data.message

                                        recyclerViewBranchList.visibility = View.GONE
                                        tvNoRecordBranchList.visibility = View.VISIBLE
                                    }
                                    retrieveList(it.data.data?.branch)

                                    //val nums = listOf(it.data?.data)
                                    //val listSize: Int? = it.data?.data?.size



                                } else {
                                    when (it.data!!.code == Constants.ErrorCode) {
                                        true -> {
                                            Toast.makeText(
                                                this,
                                                it.data.errormessage?.message,
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

    private fun retrieveList(branchesList: List<BranchListModel.Data.Branches>?) {

        adapter.apply {
            addBranches(branchesList as ArrayList<BranchListModel.Data.Branches>?, viewDetail)
            notifyDataSetChanged()
        }
    }

    //click from branchlist adapter
    fun switchBranch(id: String?, branchName: String?) {
        if (id?.equals(loginModel?.data?.branch_info?.id)!!) {
            Toast.makeText(
                this,
                getString(R.string.select_another_bramch_to_switch_msg),
                Toast.LENGTH_SHORT
            ).show()
        } else {
            startActivity(
                Intent(
                    this,
                    MainActivity::class.java
                ).putExtra("RoundingMode.UNNECESSARY", id).putExtra("branch_name", branchName)
            )
        }
    }

    fun branchDetails(id: String?) {
        startActivity(Intent(this, BranchDetailActivity::class.java).putExtra("branch_id", id))
    }


}