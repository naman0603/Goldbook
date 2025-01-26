package com.goldbookapp.ui.activity.group

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.blankj.utilcode.util.NetworkUtils
import com.goldbookapp.R
import com.goldbookapp.api.ApiHelper
import com.goldbookapp.api.RetrofitBuilder
import com.goldbookapp.databinding.ActivityGroupDetailsBinding
import com.goldbookapp.model.LedgerSubGroupDetailModel
import com.goldbookapp.model.LoginModel
import com.goldbookapp.model.SearchListGroupModel
import com.goldbookapp.model.UserWiseRestrictionModel
import com.goldbookapp.ui.activity.viewmodel.SubGroupDetailsViewModal
import com.goldbookapp.ui.activity.viewmodel.ViewModelFactory
import com.goldbookapp.utils.CommonUtils
import com.goldbookapp.utils.CommonUtils.Companion.clickWithDebounce
import com.goldbookapp.utils.CommonUtils.Companion.isValidClickPressed
import com.goldbookapp.utils.Constants
import com.goldbookapp.utils.PreferenceHelper
import com.goldbookapp.utils.PreferenceHelper.get
import com.goldbookapp.utils.Status
import com.google.gson.Gson
import com.hamzasharuf.networkmonitor.ConnectivityStateHolder
import com.hamzasharuf.networkmonitor.Event
import com.hamzasharuf.networkmonitor.NetworkEvents
import kotlinx.android.synthetic.main.activity_group_details.*
import kotlinx.android.synthetic.main.toolbar_layout.*



class ParentGroupDetailsActivity : AppCompatActivity() {
    var is_delete: Boolean = false
    var is_add_edit: Boolean = false

    lateinit var binding: ActivityGroupDetailsBinding
    lateinit var loginModel: LoginModel
    var subGroupId: String? = ""
    private lateinit var viewModel: SubGroupDetailsViewModal
    lateinit var subGroupDetailModel: LedgerSubGroupDetailModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_group_details)
        setupViewModel()
        setupUIandListner()
    }

    private fun setupViewModel() {
        viewModel =
            ViewModelProvider(this, ViewModelFactory(ApiHelper(RetrofitBuilder.apiService))).get(
                SubGroupDetailsViewModal::class.java
            )
        binding.setLifecycleOwner(this)

    }

    private fun setupUIandListner() {

        val prefs = PreferenceHelper.defaultPrefs(this)
        loginModel = Gson().fromJson(
            prefs[Constants.PREF_LOGIN_DETAIL_KEY, ""],
            LoginModel::class.java
        ) //getter

        imgLeft.setImageResource(R.drawable.ic_back)
        imgRight2.setImageResource(R.drawable.ic_edit)
        imgRight.setImageResource(R.drawable.ic_delete_icon)
        tvTitle.setText("Sub Group Details")

        if (intent.extras?.containsKey(Constants.GroupID)!!) {
            subGroupId = intent.getStringExtra(Constants.GroupID)
        }
        if (intent.extras?.containsKey(Constants.GROUP_DETAIL_KEY)!!) {
            var group_str: String? = intent.getStringExtra(Constants.GROUP_DETAIL_KEY)
            var subGroupDetailModel: SearchListGroupModel.DataGroup = Gson().fromJson(
                group_str,
                SearchListGroupModel.DataGroup::class.java
            )

            subGroupId = subGroupDetailModel.sub_group_id
        }

        imgRight2?.clickWithDebounce {
            if (this::subGroupDetailModel.isInitialized) {
                startActivity(
                    Intent(this, EditSubGroupActivity::class.java)
                        .putExtra(Constants.GROUP_DETAIL_KEY, Gson().toJson(subGroupDetailModel))
                )
                finish()
            }
        }

        imgRight?.clickWithDebounce {

            if (this::subGroupDetailModel.isInitialized) {
                // delete category api call
                ensureDeleteDialog(subGroupId.toString())
            }
        }

        imgLeft?.clickWithDebounce{
            onBackPressed()
        }
    }

    private fun ensureDeleteDialog(sub_group_id: String) {
        val builder = AlertDialog.Builder(this)
        val DeleteClick = { dialog: DialogInterface, which: Int ->
            //itemCategory delete api call
            deleteSubGroup(loginModel?.data?.bearer_access_token,subGroupId)
        }
        val dialogdismiss = { dialog: DialogInterface, which: Int ->
            dialog.dismiss()
        }
        with(builder)
        {
            setTitle(context.getString(R.string.delSubGroupDialog2Title))
            setMessage(context.getString(R.string.subgroupDialog2Message))
            setPositiveButton(context.getString(R.string.Cancel),dialogdismiss)
            setNeutralButton(context.getString(R.string.Delete), DialogInterface.OnClickListener(function = DeleteClick))
            show()
        }
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
                }
                false->{
                    defaultEnableAllButtonnUI()
                }

            }
            subGroupDetailAPI(loginModel?.data?.bearer_access_token, subGroupId)
        }

        if (!ConnectivityStateHolder.isConnected) {
            // Network is not available
            CommonUtils.showDialog(this, getString(R.string.please_check_internet_msg))

        }
    }

    private fun defaultEnableAllButtonnUI() {
        imgRight2.visibility = View.VISIBLE
        imgRight.visibility = View.VISIBLE
        imgEnd.visibility = View.GONE
    }

    private fun defaultDisableAllButtonnUI() {
        imgRight2.visibility = View.GONE
        imgRight.visibility = View.GONE
        imgEnd.visibility = View.GONE
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
            if (data.permission!!.get(i).startsWith(getString(R.string.ledger_Group))) {
                // Restriction check for SubGroup
                when (data.permission!!.get(i)
                    .endsWith(getString(R.string.ledger_subgroup_delete), true)) {
                    true -> {
                        is_delete = true
                        if(is_add_edit){
                            imgRight2.visibility = View.VISIBLE
                            imgEnd.visibility = View.GONE
                            imgRight.visibility = View.VISIBLE
                        }else{
                            //  Log.v("groupdelete", "true")
                            imgRight.visibility = View.VISIBLE
                            imgEnd.visibility = View.GONE
                            imgRight2.visibility = View.GONE
                        }

                    }
                    else->{

                    }
                }

                when (data.permission!!.get(i)
                    .endsWith(getString(R.string.ledger_group_add_edit), true)) {
                    true -> {
                        is_add_edit = true
                        if (is_delete) {
                            // Log.v("checkdelete", "true")
                            imgRight2.visibility = View.VISIBLE
                            imgEnd.visibility = View.GONE
                            imgRight.visibility = View.VISIBLE
                        } else {
                            //  Log.v("groupaddedit", "true")
                            imgRight2.visibility = View.GONE
                            imgEnd.visibility = View.VISIBLE
                            imgRight.visibility = View.GONE
                        }
                    }
                    else->{

                    }
                }

            }
        }
    }


    fun subGroupDetailAPI(
        token: String?,
        sub_group_id: String?
    ) {

        if (NetworkUtils.isConnected()) {
            viewModel.subGroupDetail(token, sub_group_id).observe(this, Observer {
                it?.let { resource ->
                    when (resource.status) {
                        Status.SUCCESS -> {
                            if (it.data?.status == true) {
                                llGroupDetail_root.visibility = View.VISIBLE
                                subGroupDetailModel = it.data
                                tv_groupName.text = subGroupDetailModel.data.sub_group_name
                                tv_parentgroup_name.text = "Parent Group Name"
                                tv_natureGroupName.text =  subGroupDetailModel.data.group_name
                                tv_affectGP.visibility = View.GONE
                                tv_gp.visibility = View.GONE
                                if(subGroupDetailModel.data.is_bank_account == 0){
                                    tv_isBankAcc.text = "No"
                                }else{
                                    tv_isBankAcc.text = "Yes"
                                }

                            } else {

                            }
                            CommonUtils.hideProgress()

                        }
                        Status.ERROR -> {
                            CommonUtils.hideProgress()

                        }
                        else->{

                        }

                    }
                }
            })
        }
    }

    private fun deleteSubGroup(token: String?, sub_group_id: String?) {
        if (NetworkUtils.isConnected()) {
            if (CommonUtils.isValidClickPressed()) {
                viewModel.deleteSubGroup(token, sub_group_id).observe(this, Observer {
                    it?.let { resource ->
                        when (resource.status) {
                            Status.SUCCESS -> {
                                if (it.data?.status == true) {

                                    Toast.makeText(
                                        this,
                                        it.data?.message,
                                        Toast.LENGTH_LONG
                                    )
                                        .show()
                                    onBackPressed()

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
                                Toast.makeText(
                                    this,
                                    it.data?.errormessage?.message,
                                    Toast.LENGTH_LONG
                                )
                                    .show()
                            }
                            Status.LOADING -> {
                                CommonUtils.showProgress(this)
                            }
                        }
                    }
                })
            } else {
                CommonUtils.showDialog(this, getString(R.string.please_check_internet_msg))
            }

        }
    }
}