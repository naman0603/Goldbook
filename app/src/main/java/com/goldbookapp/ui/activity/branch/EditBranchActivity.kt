package com.goldbookapp.ui.activity.branch

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
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
import com.goldbookapp.databinding.EditBranchActivityBinding
import com.goldbookapp.model.BranchAddressModel
import com.goldbookapp.model.BranchDetailModel
import com.goldbookapp.model.LoginModel
import com.goldbookapp.ui.activity.organization.CompanyAddressDetailsActivity
import com.goldbookapp.ui.activity.viewmodel.EditBranchViewModel
import com.goldbookapp.ui.activity.viewmodel.ViewModelFactory
import com.goldbookapp.utils.CommonUtils
import com.goldbookapp.utils.CommonUtils.Companion.clickWithDebounce
import com.goldbookapp.utils.Constants
import com.goldbookapp.utils.PreferenceHelper
import com.goldbookapp.utils.PreferenceHelper.get
import com.goldbookapp.utils.PreferenceHelper.set
import com.goldbookapp.utils.Status
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.Gson
import com.hamzasharuf.networkmonitor.ConnectivityStateHolder
import com.hamzasharuf.networkmonitor.Event
import com.hamzasharuf.networkmonitor.NetworkEvents
import kotlinx.android.synthetic.main.edit_branch_activity.*
import kotlinx.android.synthetic.main.toolbar_layout.*

class EditBranchActivity : AppCompatActivity() {

    private lateinit var viewModel: EditBranchViewModel
    lateinit var binding: EditBranchActivityBinding


    private lateinit var isGSTRegistered: String
    lateinit var prefs: SharedPreferences
    lateinit var branchDetailModel: BranchDetailModel.Data

    lateinit var branchAddressModel: BranchAddressModel

    lateinit var loginModel: LoginModel

    var branchID: String? = ""
    var selectedTermBalanceID: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.edit_branch_activity)

        setupViewModel()
        setupUIandListner()


    }

    private fun setupViewModel() {
        viewModel =
            ViewModelProvider(this, ViewModelFactory(ApiHelper(RetrofitBuilder.apiService))).get(
                EditBranchViewModel::class.java
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
        tvTitle.setText(R.string.edit_branch)

        prefs.edit().remove(Constants.PREF_BRANCH_ADDRESS_KEY).apply()

        imgLeft?.clickWithDebounce {
            onBackPressed()
        }
        btnSaveEdit_Branch?.clickWithDebounce {
            if (performValidation()) {
                if (NetworkUtils.isConnected()) {
                    updateCompanyBranch(
                        loginModel?.data?.bearer_access_token,
                        txtBranchNameEditBranch.text.toString().trim(),
                        branchID,
                        txtBranchCodeEditBranch.text.toString().trim(),
                        branchAddressModel.branch_address.toString().trim(),
                        txtMobileEditBranch.text.toString().trim(),
                        txtAddNumberEditBranch.text.toString().trim(),
                        txtFirstNameEditBranch.text.toString().trim(),
                        txtLastNameEditBranch.text.toString().trim(),
                        txtEmailEditBranch.text.toString().trim(),
                        branchAddressModel.country_id.toString().trim(),
                        branchAddressModel.state_id.toString(),
                        branchAddressModel.city_id.toString(),
                        branchAddressModel.area.toString().trim(),
                        branchAddressModel.landmark.toString().trim(),
                        selectedTermBalanceID,
                        isGSTRegistered,
                        txtGSTINEditBranch.text.toString().trim(),
                        branchAddressModel.pincode
                    )
                }
            }
        }
        cardBranchAddEditBranch?.clickWithDebounce {
            startActivity(
                Intent(
                    this,
                    CompanyAddressDetailsActivity::class.java
                ).putExtra("isFromBranch", true)
            )
        }

        radiogrpEditBranch.setOnCheckedChangeListener(RadioGroup.OnCheckedChangeListener { radioGroup, checkedId ->
            when (checkedId) {
                radiobtnGSTYesEditBranch.id -> {
                    tvGSTINEditBranch.isEnabled = true; isGSTRegistered = "1"
                    tvGSTINEditBranch.visibility = View.VISIBLE
                }
                radiobtnGSTNoEditBranch.id -> {
                    tvGSTINEditBranch.isEnabled = false; isGSTRegistered = "0"
                    tvGSTINEditBranch.visibility = View.GONE
                }
            }
        })



        if (intent.extras != null && intent.extras?.containsKey(Constants.BRANCH_DETAIL_KEY)!!) {
            var branch_str: String? = intent.getStringExtra(Constants.BRANCH_DETAIL_KEY)
            branchDetailModel = Gson().fromJson(
                branch_str,
                BranchDetailModel.Data::class.java
            )


            branchID = branchDetailModel.id.toString()
            selectedTermBalanceID = "1"

            setData(branchDetailModel);
        }


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



        if (prefs.contains(Constants.PREF_BRANCH_ADDRESS_KEY)) {
            branchAddressModel = Gson().fromJson(
                prefs[Constants.PREF_BRANCH_ADDRESS_KEY, ""],
                BranchAddressModel::class.java
            )

            var addressStringBuilder: java.lang.StringBuilder = java.lang.StringBuilder()
            addressStringBuilder
                .append(branchAddressModel.branch_address?.trim()).append(", ")
                .append(branchAddressModel.area?.trim()).append(", ")
                .append(branchAddressModel.landmark?.trim()).append(", ")
                .append(branchAddressModel.country_name?.trim()).append(", ")
                .append(branchAddressModel.state_name?.trim()).append(", ")
                .append(branchAddressModel.city_name?.trim()).append(", ")
                .append(branchAddressModel.pincode?.trim()).append(", ")

            tv_branch_addressEditBranch.text =
                CommonUtils.removeUnwantedComma(addressStringBuilder.toString())
            tvAddAddressEditBranch.visibility = View.GONE
            linear_branch_addressEditBranch.visibility = View.VISIBLE

        }
    }

    private fun handleConnectivityChange() {
        if (ConnectivityStateHolder.isConnected) {
            // Network is available
            CommonUtils.hideInternetDialog()

        }

        if (!ConnectivityStateHolder.isConnected) {
            // Network is not available
            CommonUtils.showDialog(this, getString(R.string.please_check_internet_msg))

        }
    }


    fun setData(branchDetailModel: BranchDetailModel.Data) {
        txtFirstNameEditBranch.setText(branchDetailModel.contact_person_fname)
        txtLastNameEditBranch.setText(branchDetailModel.contact_person_lname)
        txtBranchNameEditBranch.setText(branchDetailModel.branch_name)
        txtBranchCodeEditBranch.setText(branchDetailModel.branch_code)
        txtMobileEditBranch.setText(branchDetailModel.branch_contact_no)
        txtAddNumberEditBranch.setText(branchDetailModel.secondary_contact)
        txtEmailEditBranch.setText(branchDetailModel.branch_email)


        saveAddressmodel(false)

        // UPDATE gst_register in company info in place of "gold_rate?.type" after api changes
        if (loginModel?.data?.company_info?.gst_register?.equals("1", true)!!) {
            // GST Company
            //  checking for default branch or another branch for GST case
            when (loginModel?.data?.company_info?.default_branch_id!!.equals(
                branchDetailModel?.id,
                true
            )) {
                true -> {
                    // default branch
                    radiobtnGSTYesEditBranch.isChecked = true
                    radiobtnGSTYesEditBranch.isEnabled = false
                    radiobtnGSTNoEditBranch.isEnabled = false
                    isGSTRegistered = "1"
                    tvGSTINEditBranch.visibility = View.VISIBLE
                    radiogrpEditBranch.isEnabled = false
                }
                else -> {
                    // another branch
                    if (branchDetailModel.branch_type?.equals("0")!!) {
                        isGSTRegistered = "0"
                        tvGSTINEditBranch.visibility = View.GONE
                        radiobtnGSTYesEditBranch.isEnabled = false
                        radiobtnGSTNoEditBranch.isEnabled = false
                        radiogrpEditBranch.isEnabled = false

                        radiobtnGSTNoEditBranch.isChecked = true
                        radiobtnGSTYesEditBranch.isChecked = false

                    } else {
                        radiobtnGSTYesEditBranch.isEnabled = true
                        radiobtnGSTNoEditBranch.isEnabled = true
                        radiogrpEditBranch.isEnabled = true
                        radiobtnGSTYesEditBranch.isChecked = true
                        radiobtnGSTNoEditBranch.isChecked = false

                        isGSTRegistered = "1"
                        tvGSTINEditBranch.visibility = View.VISIBLE

                    }

                }
            }

        } else {
            // Non GST Company
            isGSTRegistered = "0"
            radiobtnGSTNoEditBranch.isChecked = true
            radiogrpEditBranch.isEnabled = false
            radiobtnGSTYesEditBranch.isEnabled = false
            radiobtnGSTNoEditBranch.isEnabled = false
        }

        txtGSTINEditBranch.setText(branchDetailModel.gst_tin_number)

    }

    fun saveAddressmodel(removePref: Boolean) {
        val childBranchModel = BranchAddressModel(
            branchDetailModel.branch_address?.trim(),
            branchDetailModel.area?.trim(),
            branchDetailModel.landmark?.trim(),
            branchDetailModel.country_id?.trim(),
            branchDetailModel.country_name?.trim(),
            branchDetailModel.state_id?.trim(),
            branchDetailModel.state_name?.trim(),
            branchDetailModel.city_id?.trim(),
            branchDetailModel.city_name?.trim(),
            branchDetailModel.pincode?.trim()
        )

        prefs = PreferenceHelper.defaultPrefs(this)
        if (removePref) prefs.edit().remove(Constants.PREF_BRANCH_ADDRESS_KEY).apply()
        else
            prefs[Constants.PREF_BRANCH_ADDRESS_KEY] = Gson().toJson(childBranchModel) //setter
    }




    fun openNameTitlePopup(view: View?) {
        val popupMenu: PopupMenu = PopupMenu(this, view)
        popupMenu.menu.add("Mr")
        popupMenu.menu.add("Mrs")
        popupMenu.menu.add("Ms")

        popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
            (view as TextInputEditText).setText(item.title)
            true
        })

        popupMenu.show()
    }

    fun performValidation(): Boolean {
        if (txtFirstNameEditBranch.text.toString().isBlank()) {
            CommonUtils.showDialog(this, getString(R.string.enter_first_name_msg))
            txtFirstNameEditBranch.requestFocus()
            return false
        } else if (txtBranchNameEditBranch.text.toString().isBlank()) {
            CommonUtils.showDialog(this, getString(R.string.enter_branch_name_msg))
            txtBranchNameEditBranch.requestFocus()
            return false
        } else if (txtBranchCodeEditBranch.text.toString().isBlank()) {
            CommonUtils.showDialog(this, getString(R.string.enter_branch_code_msg))
            txtBranchCodeEditBranch.requestFocus()
            return false
        } else if (txtMobileEditBranch.text.toString().isBlank()) {
            CommonUtils.showDialog(this, getString(R.string.enter_mobile_no_msg))
            txtMobileEditBranch.requestFocus()
            return false
        } else if (txtMobileEditBranch.text?.length!! < 10) {
            CommonUtils.showDialog(this, getString(R.string.enter_valid_mobileno_msg))
            txtMobileEditBranch.requestFocus()
            return false
        } else if (!txtEmailEditBranch.text.toString().isBlank() && !CommonUtils.isValidEmail(
                txtEmailEditBranch.text.toString()
            )
        ) {
            CommonUtils.showDialog(this, getString(R.string.email_validation_msg))
            txtEmailEditBranch.requestFocus()
            return false
        } else if (!prefs.contains(Constants.PREF_BRANCH_ADDRESS_KEY)) {
            CommonUtils.showDialog(this, getString(R.string.enter_address_details_msg))
            return false
        } else if (radiogrpEditBranch.getCheckedRadioButtonId() == -1) { // no radio buttons are checked
            Toast.makeText(
                this,
                getString(R.string.select_radio_gstbranch_yes_no_msg),
                Toast.LENGTH_LONG
            ).show()
            return false
        } else if (radiobtnGSTYesEditBranch.isChecked && txtGSTINEditBranch.text.toString()
                .isBlank()
        ) {
            tvGSTINEditBranch.error = getString(R.string.enter_gstin_msg)/*"Please Enter GSTIN"*/
            txtGSTINEditBranch.requestFocus()
            return false
        } else if (radiobtnGSTYesEditBranch.isChecked && txtGSTINEditBranch.text?.length!! < 15) {
            tvGSTINEditBranch.error =
                getString(R.string.enter_valid_gstin_msg)/*"Please enter valid GSTIN"*/
            txtGSTINEditBranch.requestFocus()
            return false
        }

        branchAddressModel = Gson().fromJson(
            prefs[Constants.PREF_BRANCH_ADDRESS_KEY, ""],
            BranchAddressModel::class.java
        )

        return true
    }

    fun updateCompanyBranch(
        token: String?,
        branch_name: String?,
        branch_id: String?,
        branch_code: String?,
        branch_address: String?,
        branch_contact_no: String?,
        secondary_contact: String?,
        contact_person_fname: String?,
        contact_person_lname: String?,
        branch_email: String?,
        business_location: String?,
        state_id: String?,
        city_id: String?,
        area: String?,
        landmark: String?,
        term_balance: String?,
        gst_register: String?,
        gst_tin_number: String?, pincode: String?
    ) {

        viewModel.updateCompanyBranch(
            token, branch_name,
            branch_id,
            branch_code,
            branch_address,
            branch_contact_no,
            secondary_contact,
            contact_person_fname,
            contact_person_lname,
            branch_email,
            business_location,
            state_id,
            city_id,
            area,
            landmark,
            term_balance,
            gst_register,
            gst_tin_number,
            pincode
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