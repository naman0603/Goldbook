package com.goldbookapp.ui.activity.group

import android.os.Bundle
import android.view.Menu
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.PopupMenu
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.blankj.utilcode.util.NetworkUtils
import com.goldbookapp.R
import com.goldbookapp.api.ApiHelper
import com.goldbookapp.api.RetrofitBuilder
import com.goldbookapp.databinding.ActivityEditLedgerGroupBinding
import com.goldbookapp.model.LedgerGroupDetailModel
import com.goldbookapp.model.LoginModel
import com.goldbookapp.model.NatureGroupModel
import com.goldbookapp.ui.activity.viewmodel.EditGroupViewModel
import com.goldbookapp.ui.activity.viewmodel.ViewModelFactory
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
import kotlinx.android.synthetic.main.activity_edit_ledger_group.*
import kotlinx.android.synthetic.main.toolbar_layout.*

class EditLedgerGroupActivity : AppCompatActivity() {

    lateinit var binding: ActivityEditLedgerGroupBinding
    lateinit var loginModel: LoginModel
    var groupId: String? = ""

    var natureGroupList: List<NatureGroupModel.DataNatureGroup>? = null
    var natureGroupNameList: List<String>? = null
    var selectedNatureGroupID: String? = null
    private lateinit var viewModel: EditGroupViewModel
    lateinit var popupMenu: PopupMenu
    var IsAffectGP: String? = "0"
    var is_bank_account: String? = ""
    lateinit var groupDetailModel: LedgerGroupDetailModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_edit_ledger_group)
        setupViewModel()
        setupUIandListner()
    }

    private fun setupViewModel() {
        viewModel =
            ViewModelProvider(this, ViewModelFactory(ApiHelper(RetrofitBuilder.apiService))).get(
                EditGroupViewModel::class.java
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
        tvTitle.setText("Edit Group Details")




        if (intent.extras?.containsKey(Constants.GROUP_DETAIL_KEY)!!) {
            var group_str: String? = intent.getStringExtra(Constants.GROUP_DETAIL_KEY)
            groupDetailModel = Gson().fromJson(
                group_str,
                LedgerGroupDetailModel::class.java
            )

            groupId = groupDetailModel.data.group_id.toString()
            txtNameEditGroup.setText(groupDetailModel.data.group_name)
            txtEditGroupName.setText(groupDetailModel.data.nature_name)
            txtRemarksEditGroup.setText(groupDetailModel.data.description)
            if (groupDetailModel.data.affect_gross_profit.equals("1")) {
                radioGpEditGroupY.isChecked = true
            } else {
                radioGpEditGroupN.isChecked = true
            }
            selectedNatureGroupID = groupDetailModel.data.nature_id.toString()
            if (groupDetailModel.data.nature_name == "Income") {
                lyEditGp.visibility = VISIBLE
            } else if (groupDetailModel.data.nature_name == "Expenses")
                lyEditGp.visibility = VISIBLE
            else
                lyEditGp.visibility = GONE
        }
        imgLeft?.clickWithDebounce {
            onBackPressed()
        }

        radiogroupGpEditGroup.setOnCheckedChangeListener(RadioGroup.OnCheckedChangeListener { radioGroup, checkedId ->
            when (checkedId) {
                radioGpEditGroupN.id -> {
                    IsAffectGP = "0"
                }
                radioGpEditGroupY.id -> {
                    IsAffectGP = "1"
                }
            }
        })
        txtEditGroupName.clickWithDebounce{

            openNatureMenu(natureGroupNameList)
        }

        txtEditGroupName.doAfterTextChanged { selectedNatureGroupID = "" }

        btnSave_EditGroup?.clickWithDebounce {
            if (performValidation()) {
                if (NetworkUtils.isConnected()) {
                    EditGroupAPI(
                        loginModel?.data?.bearer_access_token,
                        txtNameEditGroup.text.toString().trim(),
                        selectedNatureGroupID,
                        IsAffectGP,
                        is_bank_account,
                        txtRemarksEditGroup.text.toString().trim(),
                        groupId
                    )
                }
            }

        }
    }


    fun performValidation(): Boolean {
        if (txtNameEditGroup.text.toString().isBlank()) {
            CommonUtils.showDialog(this, getString(R.string.group_name_msg)/*"Please Enter Code"*/)
            txtNameEditGroup.requestFocus()
            return false
        } else if (selectedNatureGroupID.isNullOrBlank()) {
            CommonUtils.showDialog(this, getString(R.string.nature_group_name_msg))
            txtEditGroupName.requestFocus()
            return false
        } /*else if (txtRemarksEditGroup.text.toString().isBlank()) {
            CommonUtils.showDialog(this, getString(R.string.group_remarks_msg))
            txtRemarksEditGroup.requestFocus()
            return false
        }*/
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
            getNatureGroup()
        }

        if (!ConnectivityStateHolder.isConnected) {
            // Network is not available
            CommonUtils.showDialog(this, getString(R.string.please_check_internet_msg))

        }
    }

    private fun openNatureMenu(natureGroupNameList: List<String>?) {
        popupMenu = PopupMenu(this, txtEditGroupName)
        for (i in 0 until natureGroupNameList!!.size) {
            popupMenu.menu.add(
                Menu.NONE,
                i,
                i,
                natureGroupNameList.get(i)
            ) //add(groupId, itemId, order, title);
        }

        popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
            txtEditGroupName.setText(item.title)
            val selected: String = item.title.toString()
            if (selected == "Income") {
                lyEditGp.visibility = VISIBLE
            } else if (selected == "Expenses")
                lyEditGp.visibility = VISIBLE
            else
                lyEditGp.visibility = GONE
            val pos: Int? = natureGroupNameList.indexOf(selected)

            selectedNatureGroupID =
                pos?.let { it1 -> natureGroupList?.get(it1)?.nature_group_id }.toString()

            // prefs[Constants.PREF_MULTIPLE_OPENINGSTOCK] = Gson().toJson(multipleOpeningStockList) //setter
            true
        })

        popupMenu.show()
    }


    fun getNatureGroup() {
        if (NetworkUtils.isConnected()) {
            viewModel.getNatureGroup(loginModel?.data?.bearer_access_token).observe(this, Observer {
                it?.let { resource ->
                    when (resource.status) {
                        Status.SUCCESS -> {

                            if (it.data?.status == true) {
                                natureGroupList = it.data.data

                                natureGroupNameList = natureGroupList?.map { it.name }


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


    fun EditGroupAPI(
        token: String?,
        group_name: String?,
        nature_group_id: String?,
        affect_gross_profit: String?,
        is_bank_account: String?,
        description: String?,
        group_id: String?
    ) {

        viewModel.editGroup(
            token, group_name,
            nature_group_id,
            affect_gross_profit,
            is_bank_account,
            description,
            group_id
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