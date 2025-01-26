package com.goldbookapp.ui.activity.group

import android.content.SharedPreferences
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.PopupMenu
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.blankj.utilcode.util.NetworkUtils
import com.goldbookapp.R
import com.goldbookapp.api.ApiHelper
import com.goldbookapp.api.RetrofitBuilder
import com.goldbookapp.databinding.ActivityNewGroupBinding
import com.goldbookapp.model.LoginModel
import com.goldbookapp.model.NatureGroupModel
import com.goldbookapp.model.ParentGroupModel
import com.goldbookapp.searchablespinner.SearchableSpinner
import com.goldbookapp.ui.activity.viewmodel.NewGroupViewModel
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
import kotlinx.android.synthetic.main.activity_new_group.*
import kotlinx.android.synthetic.main.toolbar_layout.*


class NewGroupActivity : AppCompatActivity() {

    private lateinit var viewModel: NewGroupViewModel
    lateinit var binding: ActivityNewGroupBinding
    lateinit var popupMenu: PopupMenu
    var selectedGroupNameType: String? = null
    lateinit var loginModel: LoginModel
    lateinit var prefs: SharedPreferences

    var is_bank_account: String? = "0"
    var selectedSubGroup: String? = "1"
    var IsAffectGP: String? = "0"

    var natureGroupList: List<NatureGroupModel.DataNatureGroup>? = null
    var natureGroupNameList: List<String>? = null
    var selectedNatureGroupID: String? = null


    var parentGroupList: List<ParentGroupModel.DataParentGroup>? = null
    var parentGroupNameList: List<String>? = null
    var selectedParentGroupID: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_new_group)
        setupViewModel()
        setupUIandListner()
    }

    private fun setupViewModel() {
        viewModel =
            ViewModelProvider(this, ViewModelFactory(ApiHelper(RetrofitBuilder.apiService))).get(
                NewGroupViewModel::class.java
            )
        binding.setLifecycleOwner(this)
    }

    private fun setupUIandListner() {
        prefs = PreferenceHelper.defaultPrefs(this)
        loginModel = Gson().fromJson(
            prefs[Constants.PREF_LOGIN_DETAIL_KEY, ""],
            LoginModel::class.java
        )

        imgLeft.setImageResource(R.drawable.ic_back)
        tvTitle.setText(R.string.new_group)


        checkBankAcc.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                is_bank_account = "1"
            } else {
                is_bank_account = "0"
            }
        }


        radiogroupSubNewGroup.setOnCheckedChangeListener(RadioGroup.OnCheckedChangeListener { radioGroup, checkedId ->
            when (checkedId) {
                radioSubNewGroupN.id -> {
                    selectedSubGroup = "0"
                    checkBankAcc.visibility = GONE
                    lySubGroup.visibility = GONE
                    tVGroupName.visibility = VISIBLE
                    lyGroup.visibility = VISIBLE

                }
                radioSubNewGroupY.id -> {
                    selectedSubGroup = "1"
                    lyGp.visibility = GONE
                    checkBankAcc.visibility = VISIBLE
                    tVGroupName.visibility = GONE
                    lySubGroup.visibility = VISIBLE
                    lyGroup.visibility = GONE
                }
            }
        })

        radiogroupGpNewGroup.setOnCheckedChangeListener(RadioGroup.OnCheckedChangeListener { radioGroup, checkedId ->
            when (checkedId) {
                radioGpNewGroupN.id -> {
                    IsAffectGP = "0"
                }
                radioGpNewGroupY.id -> {
                    IsAffectGP = "1"
                }
            }
        })

        imgLeft?.clickWithDebounce {
            onBackPressed()
        }


        txtGroupName.clickWithDebounce {
            openNatureMenu(natureGroupNameList)
        }


        btnSaveAdd_AddGroup?.clickWithDebounce {

            if (performValidation()) {
                if (NetworkUtils.isConnected()) {
                    addGroupAPI(
                        loginModel?.data?.bearer_access_token,
                        txtNameNewGroup.text.toString().trim(),
                        selectedParentGroupID,
                        selectedNatureGroupID,
                        IsAffectGP,
                        is_bank_account,
                        txtRemarksNewGroup.text.toString().trim(),
                        selectedSubGroup
                    )
                }
            }

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
            getNatureGroup()
            getParentGroup()
        }

        if (!ConnectivityStateHolder.isConnected) {
            // Network is not available
            CommonUtils.showDialog(this, getString(R.string.please_check_internet_msg))

        }
    }

    fun performValidation(): Boolean {
        if (txtNameNewGroup.text.toString().isBlank()) {
            CommonUtils.showDialog(this, getString(R.string.group_name_msg)/*"Please Enter Code"*/)
            txtNameNewGroup.requestFocus()
            return false
        } else if (selectedSubGroup == "0" && selectedNatureGroupID.isNullOrBlank()) {
            CommonUtils.showDialog(this, getString(R.string.nature_group_name_msg))
            txtGroupName.requestFocus()
            return false
        } else if (selectedSubGroup == "1" && selectedParentGroupID.isNullOrBlank()) {
            CommonUtils.showDialog(this, getString(R.string.parent_group_name_msg))
            txtSubGroup.requestFocus()
            return false
        } /*else if (txtRemarksNewGroup.text.toString().isBlank()) {
            CommonUtils.showDialog(this, getString(R.string.group_remarks_msg))
            txtRemarksNewGroup.requestFocus()
            return false
        }*/
        return true
    }


    private fun openNatureMenu(natureGroupNameList: List<String>?) {
        popupMenu = PopupMenu(this, txtGroupName)
        for (i in 0 until natureGroupNameList!!.size) {
            popupMenu.menu.add(
                Menu.NONE,
                i,
                i,
                natureGroupNameList.get(i)
            ) //add(groupId, itemId, order, title);
        }

        popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
            txtGroupName.setText(item.title)
            when (item.title) {
                "Income" -> {
                    lyGp.visibility = VISIBLE
                }
                "Expenses" -> {
                    lyGp.visibility = VISIBLE
                }
                else -> {
                    lyGp.visibility = GONE
                }
            }
            val selected: String = item.title.toString()
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


    fun getParentGroup() {
        if (NetworkUtils.isConnected()) {
            viewModel.getParentGroup(loginModel?.data?.bearer_access_token).observe(this, Observer {
                it?.let { resource ->
                    when (resource.status) {
                        Status.SUCCESS -> {

                            if (it.data?.status == true) {
                                parentGroupList = it.data.data

                                parentGroupNameList = parentGroupList?.map { it.ledger_group_name }

                                txtSubGroup.setItems(parentGroupNameList)
                                txtSubGroup.setOnItemSelectListener(object :
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


    fun addGroupAPI(
        token: String?,
        group_name: String?,
        ledger_group_id: String?,
        nature_group_id: String?,
        affect_gross_profit: String?,
        is_bank_account: String?,
        description: String?,
        make_this_sub_group: String?
    ) {

        viewModel.addGroup(
            token, group_name,
            ledger_group_id,
            nature_group_id,
            affect_gross_profit,
            is_bank_account,
            description,
            make_this_sub_group

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
