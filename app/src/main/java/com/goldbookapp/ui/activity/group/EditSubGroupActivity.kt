package com.goldbookapp.ui.activity.group

import android.os.Bundle
import android.view.View
import android.view.View.GONE
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.blankj.utilcode.util.NetworkUtils
import com.goldbookapp.R
import com.goldbookapp.api.ApiHelper
import com.goldbookapp.api.RetrofitBuilder
import com.goldbookapp.databinding.ActivityEditSubGroupBinding
import com.goldbookapp.model.LedgerSubGroupDetailModel
import com.goldbookapp.model.LoginModel
import com.goldbookapp.model.ParentGroupModel
import com.goldbookapp.searchablespinner.SearchableSpinner
import com.goldbookapp.ui.activity.viewmodel.EditSubGroupViewModel
import com.goldbookapp.ui.activity.viewmodel.ViewModelFactory
import com.goldbookapp.utils.*
import com.goldbookapp.utils.CommonUtils.Companion.clickWithDebounce
import com.goldbookapp.utils.PreferenceHelper.get
import com.google.gson.Gson
import com.hamzasharuf.networkmonitor.ConnectivityStateHolder
import com.hamzasharuf.networkmonitor.Event
import com.hamzasharuf.networkmonitor.NetworkEvents
import kotlinx.android.synthetic.main.activity_edit_ledger_group.lyEditGp
import kotlinx.android.synthetic.main.activity_edit_sub_group.*
import kotlinx.android.synthetic.main.toolbar_layout.*

class EditSubGroupActivity : AppCompatActivity() {

    lateinit var binding: ActivityEditSubGroupBinding
    lateinit var loginModel: LoginModel
    var subGroupId: String? = ""
    lateinit var subGroupDetailModel: LedgerSubGroupDetailModel
    private lateinit var viewModel: EditSubGroupViewModel
    var is_bank_account: String? = "0"
    var parentGroupList: List<ParentGroupModel.DataParentGroup>? = null
    var parentGroupNameList: List<String>? = null
    var selectedParentGroupID: String? = null

    lateinit var popupMenu: PopupMenu

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_edit_sub_group)
        setupViewModel()
        setupUIandListner()
    }

    private fun setupViewModel() {
        viewModel =
            ViewModelProvider(this, ViewModelFactory(ApiHelper(RetrofitBuilder.apiService))).get(
                EditSubGroupViewModel::class.java
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
        tvTitle.setText("Edit Sub Group Details")

        lyEditGp.visibility = GONE


        checkEditBankAcc.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                is_bank_account = "1"
            } else {
                is_bank_account = "0"
            }
        }


        if (intent.extras?.containsKey(Constants.GROUP_DETAIL_KEY)!!) {
            var group_str: String? = intent.getStringExtra(Constants.GROUP_DETAIL_KEY)
            subGroupDetailModel = Gson().fromJson(
                group_str,
                LedgerSubGroupDetailModel::class.java
            )

            subGroupId = subGroupDetailModel.data.sub_group_id.toString()
            txtNameEditSubGroup.setText(subGroupDetailModel.data.sub_group_name)
            txtEditSubGroupName.is_from_edit = true
            txtEditSubGroupName.mLabelView?.setText(subGroupDetailModel.data.group_name)
            if (subGroupDetailModel.data.is_bank_account == 1) {
                checkEditBankAcc.isChecked = true
            } else {
                checkEditBankAcc.isChecked = false
            }
            txtRemarksEditSubGroup.setText(subGroupDetailModel.data.description)
            selectedParentGroupID = subGroupDetailModel.data.group_id.toString()

            imgLeft?.clickWithDebounce {
                onBackPressed()
            }


            btnSaveAdd_EditSubGroup?.clickWithDebounce {
                if (performValidation()) {
                    if (NetworkUtils.isConnected()) {
                        EditSubGroupAPI(
                            loginModel?.data?.bearer_access_token,
                            txtNameEditSubGroup.text.toString().trim(),
                            selectedParentGroupID,
                            is_bank_account,
                            txtRemarksEditSubGroup.text.toString().trim(),
                            subGroupId
                        )
                    }
                }

            }
        }
    }

    fun performValidation(): Boolean {
        if (txtNameEditSubGroup.text.toString().isBlank()) {
            CommonUtils.showDialog(this, getString(R.string.group_name_msg)/*"Please Enter Code"*/)
            txtNameEditSubGroup.requestFocus()
            return false
        } else if (selectedParentGroupID.isNullOrBlank()) {
            CommonUtils.showDialog(this, getString(R.string.parent_group_name_msg))
            txtEditSubGroupName.requestFocus()
            return false
        } else if (txtRemarksEditSubGroup.text.toString().isBlank()) {
            CommonUtils.showDialog(this, getString(R.string.group_remarks_msg))
            txtRemarksEditSubGroup.requestFocus()
            return false
        }
        return true
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
            getParentGroup()
        }

        if (!ConnectivityStateHolder.isConnected) {
            // Network is not available
            CommonUtils.showDialog(this, getString(R.string.please_check_internet_msg))

        }
    }

    fun getParentGroup() {
        if (NetworkUtils.isConnected()) {
            viewModel.getParentGroup(loginModel?.data?.bearer_access_token).observe(this, Observer {
                it?.let { resource ->
                    when (resource.status) {
                        Status.SUCCESS -> {

                            if (it.data?.status == true) {
                                parentGroupList = it.data.data

                                parentGroupNameList = parentGroupList?.map { it.ledger_group_name }

                                txtEditSubGroupName.setItems(parentGroupNameList)
                                txtEditSubGroupName.setOnItemSelectListener(object :
                                    SearchableSpinner.SearchableItemListener {
                                    override fun onItemSelected(view: View?, position: Int) {
                                        selectedParentGroupID =
                                            position?.let { it1 -> parentGroupList?.get(it1)?.ledger_group_id }
                                                .toString()

                                    }

                                    override fun onSelectionClear() {

                                    }
                                })


                            } else {

                                when (it.data!!.code.toString() == Constants.ErrorCode) {
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

    fun EditSubGroupAPI(
        token: String?,
        group_name: String?,
        ledger_group_id: String?,
        is_bank_account: String?,
        description: String?,
        sub_group_id: String?
    ) {

        viewModel.editSubGroup(
            token, group_name,
            ledger_group_id,
            is_bank_account,
            description,
            sub_group_id
        ).observe(this, Observer {
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

                            when (it.data!!.code.toString() == Constants.ErrorCode) {
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