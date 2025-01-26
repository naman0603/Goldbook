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
import com.goldbookapp.model.LedgerGroupDetailModel
import com.goldbookapp.model.LoginModel
import com.goldbookapp.model.SearchListGroupModel
import com.goldbookapp.model.UserWiseRestrictionModel
import com.goldbookapp.ui.activity.viewmodel.GroupDetailsViewModal
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
import kotlinx.android.synthetic.main.toolbar_layout.*
import kotlinx.android.synthetic.main.toolbar_layout.view.*

class GroupDetailsActivity : AppCompatActivity() {

    lateinit var binding: ActivityGroupDetailsBinding
    lateinit var loginModel: LoginModel
    var groupId: String? = ""
    lateinit var groupDetailModel: LedgerGroupDetailModel
    private lateinit var viewModel: GroupDetailsViewModal

    var is_delete: Boolean = false
    var is_add_edit: Boolean = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_group_details)
        setupViewModel()
        setupUIandListner()

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
            when (loginModel.data!!.user_info!!.user_type.equals("user", true)) {
                // user type user
                true -> {
                    // apply restriciton
                    defaultDisableAllButtonnUI()
                    userWiseRestriction(loginModel.data?.bearer_access_token)
                }
                false -> {
                    defaultEnableAllButtonnUI()
                }

            }
            groupDetailAPI(loginModel?.data?.bearer_access_token, groupId)
        }

        if (!ConnectivityStateHolder.isConnected) {
            // Network is not available
            CommonUtils.showDialog(this, getString(R.string.please_check_internet_msg))

        }
    }

    private fun defaultEnableAllButtonnUI() {
        binding.root.imgRight2.visibility = View.VISIBLE
        binding.root.imgRight.visibility = View.VISIBLE
        binding.root.imgEnd.visibility = View.GONE
    }

    private fun defaultDisableAllButtonnUI() {
        binding.root.imgRight2.visibility = View.GONE
        binding.root.imgRight.visibility = View.GONE
        binding.root.imgEnd.visibility = View.GONE
    }


    private fun setupViewModel() {
        viewModel =
            ViewModelProvider(this, ViewModelFactory(ApiHelper(RetrofitBuilder.apiService))).get(
                GroupDetailsViewModal::class.java
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
        imgEnd.setImageResource(R.drawable.ic_edit)
        imgRight.setImageResource(R.drawable.ic_delete_icon)
        tvTitle.setText("Group Details")

        if (intent.extras?.containsKey(Constants.GroupID)!!) {
            groupId = intent.getStringExtra(Constants.GroupID)
        }
        if (intent.extras?.containsKey(Constants.GROUP_DETAIL_KEY)!!) {
            var group_str: String? = intent.getStringExtra(Constants.GROUP_DETAIL_KEY)
            var groupDetailModel: SearchListGroupModel.DataGroup = Gson().fromJson(
                group_str,
                SearchListGroupModel.DataGroup::class.java
            )

            groupId = groupDetailModel.ledger_group_id.toString()

        }
        imgEnd?.clickWithDebounce {
            if (this::groupDetailModel.isInitialized) {
                startActivity(
                    Intent(this, EditLedgerGroupActivity::class.java)
                        .putExtra(Constants.GROUP_DETAIL_KEY, Gson().toJson(groupDetailModel))
                )
                finish()
            }

        }

        imgRight2?.clickWithDebounce {
            if (this::groupDetailModel.isInitialized) {
                startActivity(
                    Intent(this, EditLedgerGroupActivity::class.java)
                        .putExtra(Constants.GROUP_DETAIL_KEY, Gson().toJson(groupDetailModel))
                )
                finish()
            }

        }

        imgRight?.clickWithDebounce {
            if (this::groupDetailModel.isInitialized) {
                // delete category api call
                ensureDeleteDialog(groupId.toString())
            }
        }

        imgLeft?.clickWithDebounce {
            onBackPressed()
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
            if (data.permission!!.get(i).startsWith(getString(R.string.ledger_Group))) {
                // Restriction check for Group

                when (data.permission!!.get(i)
                    .endsWith(getString(R.string.ledger_group_delete), true)) {
                    true -> {
                        is_delete = true
                        if(is_add_edit){
                            binding.root.imgRight2.visibility = View.VISIBLE
                            binding.root.imgEnd.visibility = View.GONE
                            binding.root.imgRight.visibility = VISIBLE
                        }else{
                          //  Log.v("groupdelete", "true")
                            binding.root.imgRight.visibility = View.VISIBLE
                            binding.root.imgEnd.visibility = View.GONE
                            binding.root.imgRight2.visibility = View.GONE
                        }

                        // imgRight2.visibility = View.GONE
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
                            binding.root.imgRight2.visibility = View.VISIBLE
                            binding.root.imgEnd.visibility = View.GONE
                            binding.root.imgRight.visibility = VISIBLE
                        } else {
                          //  Log.v("groupaddedit", "true")
                            binding.root.imgRight2.visibility = View.GONE
                            binding.root.imgEnd.visibility = View.VISIBLE
                            binding.root.imgRight.visibility = GONE
                        }
                    }
                    else->{

                    }
                }

            }

        }
    }


    private fun ensureDeleteDialog(group_id: String) {
        val builder = AlertDialog.Builder(this)
        val DeleteClick = { dialog: DialogInterface, which: Int ->
            //itemCategory delete api call
            deleteGroup(loginModel?.data?.bearer_access_token, group_id)
        }
        val dialogdismiss = { dialog: DialogInterface, which: Int ->
            dialog.dismiss()
        }
        with(builder)
        {
            setTitle(context.getString(R.string.delGroupDialog2Title))
            setMessage(context.getString(R.string.groupDialog2Message))
            setPositiveButton(context.getString(R.string.Cancel), dialogdismiss)
            setNeutralButton(
                context.getString(R.string.Delete),
                DialogInterface.OnClickListener(function = DeleteClick)
            )
            show()
        }
    }


    fun groupDetailAPI(
        token: String?,
        group_id: String?
    ) {

        if (NetworkUtils.isConnected()) {
            viewModel.groupDetail(token, group_id).observe(this, Observer {
                it?.let { resource ->
                    when (resource.status) {
                        Status.SUCCESS -> {
                            if (it.data?.status == true) {
                                binding.llGroupDetailRoot.visibility = VISIBLE
                                groupDetailModel = it.data

                                binding.tvGroupName.text = groupDetailModel.data.group_name
                                binding.tvNatureGroupName.text = groupDetailModel.data.nature_name

                                if (groupDetailModel.data.affect_gross_profit == "0") {
                                    binding.tvAffectGP.text = "No"
                                } else {
                                    binding.tvAffectGP.text = "Yes"
                                }
                                if (groupDetailModel.data.is_bank_account == 0) {
                                    binding.tvIsBankAcc.text = "No"
                                } else {
                                    binding.tvIsBankAcc.text = "Yes"
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

    private fun deleteGroup(token: String?, group_id: String?) {
        if (NetworkUtils.isConnected()) {
            if (isValidClickPressed()) {
                viewModel.deleteGroup(token, group_id).observe(this, Observer {
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
                                    Toast.makeText(
                                        this,
                                        it.data?.errormessage?.message,
                                        Toast.LENGTH_LONG
                                    )
                                        .show()
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