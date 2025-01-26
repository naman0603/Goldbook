package com.goldbookapp.ui.activity.organization

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.text.bold
import androidx.core.text.color
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.blankj.utilcode.util.NetworkUtils
import com.goldbookapp.R
import com.goldbookapp.api.ApiHelper
import com.goldbookapp.api.RetrofitBuilder
import com.goldbookapp.databinding.ActivityOrganizationdetailBinding
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
import kotlinx.android.synthetic.main.activity_organizationdetail.*
import kotlinx.android.synthetic.main.toolbar_layout.*
import java.lang.StringBuilder

class OrganizationDetailActivity : AppCompatActivity() {
    var is_delete: Boolean = false
    var is_add_edit: Boolean = false
    private lateinit var viewModel: OrganizationDetailViewModel
    lateinit var orgDetailModel: GetUserCompanyModel.Data
    lateinit var binding: ActivityOrganizationdetailBinding
    var selectedCompanyID: String? = null
    lateinit var companyAddressModel: CompanyAddressModel

    lateinit var loginModel: LoginModel
    lateinit var popupMenu: PopupMenu

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_organizationdetail)

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
                if (this::orgDetailModel.isInitialized) {
                    when (item.itemId) {

                        R.id.actionInactiveItemData -> {

                        }

                        R.id.actionDeleteItemData -> {
                            ensureDeleteDialog(orgDetailModel.company_name.toString())
                        }
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
            if (selectedCompanyID?.isNotEmpty()!!) {
                getUserCompanyDetailsAPI(
                    loginModel.data?.bearer_access_token,
                    /*loginModel.data?.company_info?.id*/selectedCompanyID
                )
            }
        }

        if (!ConnectivityStateHolder.isConnected) {
            // Network is not available
            CommonUtils.showDialog(this, getString(R.string.please_check_internet_msg))

        }
    }

    val clickableReadMore = object : ClickableSpan() {
        override fun onClick(widget: View) {

        }

        override fun updateDrawState(ds: TextPaint) {
            super.updateDrawState(ds)
            ds.color = ContextCompat.getColor(this@OrganizationDetailActivity, R.color.red)
            ds.typeface = ResourcesCompat.getFont(
                this@OrganizationDetailActivity,
                R.font.proxima_nova_bold
            )
            ds.isUnderlineText = false
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
                        if (is_add_edit) {
                            imgRight2.visibility = View.VISIBLE
                            imgEnd.visibility = View.GONE
                            imgRight.visibility = View.VISIBLE
                        } else {
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
    private fun ensureDeleteDialog(companyName: String) {
        val builder = AlertDialog.Builder(this)
        val DeleteClick = { dialog: DialogInterface, which: Int ->
            //branch delete api call

                deleteCompany(loginModel?.data?.bearer_access_token, selectedCompanyID)
        }
        val dialogdismiss = { dialog: DialogInterface, which: Int ->
            dialog.dismiss()
        }
        with(builder)
        {
            setTitle(context.getString(R.string.companyDelDialogTitle))
            var firstPart: String = context.getString(R.string.companyDelDialogMsg)
            var spart: String = context.getString(R.string.companyDelDialogMsgPart)
            var sspart: String = context.getString(R.string.companyDelDialogMsgPart1)
            var secondPart: String = companyName + " ?\n\n"
            var thirdPart: String = context.getString(R.string.companyDelDialogMessage)

            val myCustomizedString = SpannableStringBuilder()
                .append(firstPart)
                .bold { append(spart) }
                .append(sspart)
                .bold {
                    append(secondPart)
                        .color(resources.getColor(R.color.red), { bold { append(thirdPart) } })

                }
            val font = ResourcesCompat.getFont(
                this@OrganizationDetailActivity,
                R.font.proxima_nova_regular
            )


            //var finalString: String = firstPart + secondPart + thirdPart

            setMessage(myCustomizedString)
            setPositiveButton(context.getString(R.string.branchDialogCancel), dialogdismiss)
            setNeutralButton(
                context.getString(R.string.companyDialogDelete),
                DialogInterface.OnClickListener(function = DeleteClick)
            )
            show()
        }
    }

    private fun deleteCompany(token: String?, companyID: String?) {
        if (NetworkUtils.isConnected()) {
            if (isValidClickPressed()) {

                viewModel.deleteCompany(token, companyID).observe(this, Observer {
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

    fun getUserCompanyDetailsAPI(token: String?, companyID: String?) {

        if (NetworkUtils.isConnected()) {

            viewModel.getUserCompanyDetails(token, companyID)
                .observe(this, Observer {
                    it?.let { resource ->
                        when (resource.status) {
                            Status.SUCCESS -> {
                                if (it.data?.status == true) {

                                    orgDetailModel = it.data.data!!
                                    fillOrgDetailData(orgDetailModel)


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
                OrganizationDetailViewModel::class.java
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

            startActivity(
                Intent(this, EditOrganizationActivity::class.java)
                    .putExtra(Constants.ORGS_DETAIL_KEY, Gson().toJson(orgDetailModel))
            )
        }

        if (intent.extras != null && intent.extras!!.containsKey("company_id")) {
            selectedCompanyID = intent.getStringExtra("company_id")
            popupMenu = PopupMenu(this, imgRight)
            popupMenu.menuInflater.inflate(R.menu.popup_menu_customer_detail, popupMenu.menu)
            //sent statement invisible
            popupMenu.menu.getItem(0).isVisible = false
            popupMenu.menu.getItem(1).isVisible = true

            when (loginModel?.data?.company_info?.id.equals(selectedCompanyID)) {
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


    private fun fillOrgDetailData(orgDetailModel: GetUserCompanyModel.Data) {
        binding.toolbar.tvTitle.text = orgDetailModel.company_name
        tvOrgDetailName.text = orgDetailModel.company_name

        val sb = StringBuilder()
        sb.append(orgDetailModel.contact_person_first_name)
        sb.append(' ')
        sb.append(orgDetailModel.contact_person_last_name)

        tvOrgDetailCeo.text = sb


        var addressStringBuilder: java.lang.StringBuilder = java.lang.StringBuilder()


        addressStringBuilder
            .append(orgDetailModel.reg_address.toString().trim()).append(", ")
            .append(orgDetailModel.area.toString().trim()).append(", ")
            .append(orgDetailModel.landmark.toString().trim()).append(", ")
            .append(orgDetailModel.postal_code.toString().trim()).append(", ")
        tvOrgDetailAddress.text = CommonUtils.removeUnwantedComma(addressStringBuilder.toString())


        tvOrgDetailMobile.text = orgDetailModel.mobile_number
        tvOrgDetailPhone.text = orgDetailModel.alternate_number
        tvOrgDetailEmail.text = orgDetailModel.email
        tvOrgDetailState.text = orgDetailModel.state_name
        tvOrgDetailCountry.text = orgDetailModel.country_name
        tvOrgDetailCity.text = orgDetailModel.city_name
        tvOrgDetailFinYear.text = orgDetailModel.fiscal_year_name
        tvOrgDetailPAN.text = orgDetailModel.pan_number
        tvOrgDetailCIN.text = orgDetailModel.cin_number
        if (orgDetailModel?.gst_register == "1") {
            tvOrgDetailOrgType.text = "GST"
            tvOrgDetailGSTIN.text = orgDetailModel?.gst_tin_number
            tvOrgDetailLeftGSTIN.visibility = View.VISIBLE
            tvOrgDetailGSTIN.visibility = View.VISIBLE
            tvOrgDetailLeftPAN.visibility = View.VISIBLE
            tvOrgDetailPAN.visibility = View.VISIBLE

        } else {
            tvOrgDetailLeftGSTIN.visibility = View.GONE
            tvOrgDetailGSTIN.visibility = View.GONE
            tvOrgDetailLeftPAN.visibility = View.GONE
            tvOrgDetailPAN.visibility = View.GONE
            tvOrgDetailOrgType.text = "Non-GST"
        }
        // saveCompanyAddressModel(orgDetailModel)
    }

}