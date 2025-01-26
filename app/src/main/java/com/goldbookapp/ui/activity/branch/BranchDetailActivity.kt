package com.goldbookapp.ui.activity.branch

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.View
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
import com.goldbookapp.databinding.BranchDetailActivityBinding
import com.goldbookapp.model.*
import com.goldbookapp.ui.activity.*
import com.goldbookapp.ui.activity.viewmodel.ViewModelFactory
import com.goldbookapp.utils.*
import com.goldbookapp.utils.CommonUtils.Companion.clickWithDebounce
import com.goldbookapp.utils.CommonUtils.Companion.isValidClickPressed
import com.goldbookapp.utils.PreferenceHelper.get
import com.google.gson.Gson
import com.hamzasharuf.networkmonitor.ConnectivityStateHolder
import com.hamzasharuf.networkmonitor.Event
import com.hamzasharuf.networkmonitor.NetworkEvents
import kotlinx.android.synthetic.main.branch_detail_activity.*
import kotlinx.android.synthetic.main.toolbar_layout.*
import java.lang.StringBuilder

class BranchDetailActivity : AppCompatActivity() {
    var is_delete: Boolean = false
    var is_add_edit: Boolean = false
    private lateinit var viewModel: BranchDetailViewModel
    lateinit var branchDetailModel: BranchDetailModel.Data
    lateinit var binding: BranchDetailActivityBinding
    var selectedBranchID: String? = null
    private var status: String = "2"
    lateinit var branchAddressModel: BranchAddressModel
    private lateinit var changeStatusBranchModel: ChangeStatusBranchModel

    lateinit var loginModel: LoginModel
    lateinit var popupMenu: PopupMenu

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.branch_detail_activity)
        //binding.loginModel = LoginModel()

        setupViewModel()
        setupUIandListner()

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

        imgRight.clickWithDebounce {

            popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
                if (this::branchDetailModel.isInitialized) {
                    when (item.itemId) {

                        R.id.actionInactiveItemData ->
                            when (status) {
                                "1" -> { // active to inactive case
                                    // 2 inactive
                                    status = "2"
                                    popupMenu.menu.getItem(0).title =
                                        getString(R.string.mark_as_active)
                                        changeStatusBranchAPI(
                                            loginModel?.data?.bearer_access_token,
                                            selectedBranchID,
                                            status
                                        )
                                }
                                "2" -> {//inactive to active case
                                    //1 active
                                    status = "1"
                                    popupMenu.menu.getItem(0).title =
                                        getString(R.string.mark_as_inactive)

                                        changeStatusBranchAPI(
                                            loginModel?.data?.bearer_access_token,
                                            selectedBranchID,
                                            status
                                        )
                                }
                            }

                        R.id.actionDeleteItemData ->
                            ensureDeleteDialog(branchDetailModel.branch_name.toString())
                    }
                }
                true
            })
            popupMenu.show()
        }

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
                   // userWiseRestriction(loginModel.data?.bearer_access_token)
                }
                // user_type -> admin or super_admin or any other
                false -> {
                    defaultEnableAllButtonnUI()
                }
            }
            if (selectedBranchID?.isNotEmpty()!!) {
                getBranchDetailsAPI(
                    loginModel.data?.bearer_access_token,
                    /*loginModel.data?.branch_info?.id*/selectedBranchID
                )

            }
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
                when (data.permission!!.get(i).endsWith(getString(R.string.del), true)) {
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
                    when (data.permission!!.get(i).endsWith(getString(R.string.add_edit), true)) {
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

    private fun defaultDisableAllButtonnUI() {
        imgRight.visibility = View.GONE
        imgRight2.visibility = View.GONE
        imgEnd.visibility = View.GONE
    }
    private fun defaultEnableAllButtonnUI() {
        imgRight.visibility = View.VISIBLE
        imgRight2.visibility = View.VISIBLE
        imgEnd.visibility = View.GONE
    }
    fun getBranchDetailsAPI(token: String?, branchID: String?) {

        if (NetworkUtils.isConnected()) {

            viewModel.getBranchDetails(token, branchID)
                .observe(this, Observer {
                    it?.let { resource ->
                        when (resource.status) {
                            Status.SUCCESS -> {
                                if (it.data?.status == true) {

                                    branchDetailModel = it.data.data!!
                                    when (branchDetailModel.status?.equals("1", true)) {
                                        true -> {
                                            status = "1"
                                            popupMenu.menu.getItem(0).title =
                                                getString(R.string.mark_as_inactive)
                                        }
                                        false -> {
                                            status = "2"
                                            popupMenu.menu.getItem(0).title =
                                                getString(R.string.mark_as_active)
                                        }
                                        else -> {

                                        }
                                    }
                                    fillBranchDetailData(branchDetailModel)


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

    private fun setupViewModel() {
        viewModel =
            ViewModelProvider(this, ViewModelFactory(ApiHelper(RetrofitBuilder.apiService))).get(
                BranchDetailViewModel::class.java
            )
        binding.setLifecycleOwner(this)

    }

    private fun setupUIandListner() {

        val prefs = PreferenceHelper.defaultPrefs(this)
        loginModel = Gson().fromJson(
            prefs[Constants.PREF_LOGIN_DETAIL_KEY, ""],
            LoginModel::class.java
        ) //getter


        binding.toolbar.imgLeft.setImageResource(R.drawable.ic_back)
        binding.toolbar.imgRight2.setImageResource(R.drawable.ic_edit)
        binding.toolbar.imgEnd.setImageResource(R.drawable.ic_edit)
        binding.toolbar.imgRight.setImageResource(R.drawable.ic_more)
        binding.toolbar.imgEnd.visibility = View.GONE


        binding.toolbar.imgLeft.clickWithDebounce {

            onBackPressed()
        }

        binding.toolbar.imgRight2.clickWithDebounce {

            if (NetworkUtils.isConnected())
                startActivity(
                    Intent(this, EditBranchActivity::class.java)
                        .putExtra(Constants.BRANCH_DETAIL_KEY, Gson().toJson(branchDetailModel))
                )
        }

        if (intent.extras != null && intent.extras!!.containsKey("branch_id")) {
            selectedBranchID = intent.getStringExtra("branch_id")
            popupMenu = PopupMenu(this, imgRight)
            popupMenu.menuInflater.inflate(R.menu.popup_menu_customer_detail, popupMenu.menu)
            //sent statement invisible
            popupMenu.menu.getItem(0).isVisible = true
            popupMenu.menu.getItem(1).isVisible = true

            when (loginModel?.data?.branch_info?.id.equals(selectedBranchID)) {
                true -> {
                    popupMenu.menu.getItem(1).isEnabled = false
                    popupMenu.menu.getItem(0).isEnabled = false
                    //binding.toolbar.imgRight.visibility = View.GONE
                }
                false -> {
                    popupMenu.menu.getItem(1).isEnabled = true
                    popupMenu.menu.getItem(0).isEnabled = true
                    // binding.toolbar.imgRight.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun ensureDeleteDialog(item_name: String) {
        val builder = AlertDialog.Builder(this)
        val DeleteClick = { dialog: DialogInterface, which: Int ->
            //branch delete api call
                deleteBranch(loginModel?.data?.bearer_access_token, selectedBranchID)
        }
        val dialogdismiss = { dialog: DialogInterface, which: Int ->
            dialog.dismiss()
        }
        with(builder)
        {
            setTitle(context.getString(R.string.branchDelDialogTitle))
            setMessage(context.getString(R.string.branchDelDialogMessage))
            setPositiveButton(context.getString(R.string.branchDialogCancel), dialogdismiss)
            setNeutralButton(
                context.getString(R.string.branchDialogDelete),
                DialogInterface.OnClickListener(function = DeleteClick)
            )
            show()
        }
    }

    private fun deleteItemDialog(customer: String) {
        val builder = AlertDialog.Builder(this)
        val InActiveClick = { dialog: DialogInterface, which: Int ->
                changeStatusBranchAPI(loginModel?.data?.bearer_access_token, selectedBranchID, "2")

        }
        val dialogdismiss = { dialog: DialogInterface, which: Int ->
            dialog.dismiss()
        }
        with(builder)
        {
            setTitle(context.getString(R.string.delBranchDialog1Title))
            setMessage(context.getString(R.string.branchDialog1Message))
            setPositiveButton(context.getString(R.string.Cancel), dialogdismiss)
            setNeutralButton(
                context.getString(R.string.mark_as_inactive),
                DialogInterface.OnClickListener(function = InActiveClick)
            )
            show()
        }
    }

    private fun deleteBranch(token: String?, branchID: String?) {
        if (isValidClickPressed()) {
            if (NetworkUtils.isConnected()) {

                viewModel.deleteBranch(token, branchID).observe(this, Observer {
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
                                    finish()

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
                                    if (it.data?.data?.status.equals("1")) {
                                        deleteItemDialog(branchDetailModel.branch_name.toString())
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
    }

    fun changeStatusBranchAPI(
        token: String?,
        item_id: String?,
        status: String?
    ) {

        if (NetworkUtils.isConnected()) {
            if (isValidClickPressed()) {
                viewModel.changeStatusBranch(token, item_id, status).observe(this, Observer {
                    it?.let { resource ->
                        when (resource.status) {
                            Status.SUCCESS -> {
                                if (it.data?.status == true) {

                                    changeStatusBranchModel = it.data
                                    Toast.makeText(
                                        this,
                                        it.data?.message,
                                        Toast.LENGTH_LONG
                                    )
                                        .show()

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
                                this.finish()
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
    }

    private fun fillBranchDetailData(branchDetailModel: BranchDetailModel.Data) {
        binding.toolbar.tvTitle.text = branchDetailModel.branch_name
        tvBranchDetailBranchName.text = branchDetailModel.branch_name
        tvBranchDetailBranchCode.text = branchDetailModel.branch_code
        if (branchDetailModel.contact_person_fname == null) {
            tvBranchDetailName.text = ""
        } else {
            val sb = StringBuilder()
            sb.append(branchDetailModel.contact_person_fname)
            sb.append(' ')
            sb.append(branchDetailModel.contact_person_lname)

            tvBranchDetailName.text = sb
        }


        tvBranchDetailMobile.text = branchDetailModel.branch_contact_no
        tvBranchDetailPhone.text = branchDetailModel.secondary_contact
        tvBranchDetailEmail.text = branchDetailModel.branch_email
        if (branchDetailModel.branch_type?.equals("0")!!) {
            tvBranchDetailRegistration.text = "Non-GST"
        } else {
            tvBranchDetailRegistration.text = "GST"
        }


        var addressStringBuilder: java.lang.StringBuilder = java.lang.StringBuilder()


        when (branchDetailModel.branch_address.isNullOrBlank()) {
            false -> addressStringBuilder
                .append(branchDetailModel.branch_address?.trim()).append(",").append("\n")
else->{

}
        }
        when (branchDetailModel.area.isNullOrBlank()) {
            false -> addressStringBuilder
                .append(branchDetailModel.area?.trim()).append(",").append("\n")
            else->{

            }
        }
        when (branchDetailModel.landmark.isNullOrBlank()) {
            false -> addressStringBuilder
                .append(branchDetailModel.landmark?.trim())
            else->{

            }
        }

        tvBranchDetailAddress.text = /*CommonUtils.removeUnwantedComma(*/
            addressStringBuilder.toString()/*)*/

        tvBranchDetailCountry.text = branchDetailModel.country_name
        tvBranchDetailState.text = branchDetailModel.state_name
        tvBranchDetailCity.text = branchDetailModel.city_name
        tvBranchDetailPincode.text = branchDetailModel.pincode

    }

}