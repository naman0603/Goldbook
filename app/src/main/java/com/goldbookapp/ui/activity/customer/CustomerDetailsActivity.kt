package com.goldbookapp.ui.activity.customer

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.blankj.utilcode.util.NetworkUtils
import com.goldbookapp.R
import com.goldbookapp.api.ApiHelper
import com.goldbookapp.api.RetrofitBuilder
import com.goldbookapp.databinding.CustomerDetailActivityBinding
import com.goldbookapp.model.*
import com.goldbookapp.ui.activity.viewmodel.CustomerDetailsViewModel
import com.goldbookapp.ui.activity.viewmodel.ViewModelFactory
import com.goldbookapp.ui.adapter.CustomerDetailAdapter
import com.goldbookapp.utils.CommonUtils
import com.goldbookapp.utils.CommonUtils.Companion.clickWithDebounce
import com.goldbookapp.utils.CommonUtils.Companion.isValidClickPressed
import com.goldbookapp.utils.Constants
import com.goldbookapp.utils.PreferenceHelper
import com.goldbookapp.utils.PreferenceHelper.get
import com.goldbookapp.utils.Status
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.android.material.tabs.TabLayout.TabLayoutOnPageChangeListener
import com.google.gson.Gson
import com.hamzasharuf.networkmonitor.ConnectivityStateHolder
import com.hamzasharuf.networkmonitor.Event
import com.hamzasharuf.networkmonitor.NetworkEvents
import kotlinx.android.synthetic.main.customer_detail_activity.*
import kotlinx.android.synthetic.main.toolbar_layout.*


class CustomerDetailsActivity : AppCompatActivity() {

    lateinit var binding: CustomerDetailActivityBinding

    private lateinit var viewModel: CustomerDetailsViewModel
    lateinit var loginModel: LoginModel
    lateinit var popupMenu: PopupMenu
    lateinit var cutomerDetailModel: CustomerDetailModel
    lateinit var changeStatusCustomerModel: ChangeStatusCustomerModel

    var customer_id: String? = ""
    private var status: String = "2"

    var tabposition: Int = 0
    internal lateinit var adapter: CustomerDetailAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.customer_detail_activity)

        setupViewModel()
        setupUIandListner()

    }

    private fun setupViewModel() {
        viewModel =
            ViewModelProvider(this, ViewModelFactory(ApiHelper(RetrofitBuilder.apiService))).get(
                CustomerDetailsViewModel::class.java
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
        imgRight.setImageResource(R.drawable.ic_more)

        imgLeft?.clickWithDebounce {
            onBackPressed()
        }

        //setup tablayout
        binding.tabLayout.removeAllTabs()
        binding.tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.contact_info)))
        binding.tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.transactions)))
        binding.tabLayout.tabGravity = TabLayout.GRAVITY_FILL

        imgRight2?.clickWithDebounce {
            startActivity(
                Intent(this, EditCustomerActivity::class.java)
                    .putExtra(Constants.CUSTOMER_DETAIL_KEY, Gson().toJson(cutomerDetailModel))
            )
        }

        if (intent.extras?.containsKey(Constants.CUSTOMER_DETAIL_KEY)!!) {
            var customer_str: String? = intent.getStringExtra(Constants.CUSTOMER_DETAIL_KEY)
            var customerDetailModel: SearchListCustomerModel.Data1037062284 = Gson().fromJson(
                customer_str,
                SearchListCustomerModel.Data1037062284::class.java
            )

            customer_id = customerDetailModel.customer_id
            popupMenu = PopupMenu(this, imgRight)
            popupMenu.menuInflater.inflate(R.menu.popup_menu_customer_detail, popupMenu.menu)

        }

    }

    override fun onDestroy() {
        super.onDestroy()
        CommonUtils.hideProgress()
    }

    override fun onResume() {
        super.onResume()
        if (viewPager != null) {
            if (viewPager.currentItem == 0) {

            } else {

            }
        }

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
                    userWiseRestriction(loginModel.data?.bearer_access_token)
                }
                false->{
                    popupMenu.menu.getItem(1).setVisible(true)
                }
            }
            if (customer_id != null && !customer_id!!.isBlank()) {
                //if (!this::adapter.isInitialized){

                customerDetailAPI(
                    loginModel?.data?.bearer_access_token,
                    loginModel?.data?.company_info?.id,
                    customer_id
                )
                // }

                imgRight.clickWithDebounce {

                    popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
                        if (this::cutomerDetailModel.isInitialized) {
                            when (item.itemId) {

                                R.id.actionInactiveItemData ->
                                    when (status) {
                                        "1" -> { // active to inactive case
                                            // 2 inactive

                                            status = "2"
                                            popupMenu.menu.getItem(0).title =
                                                getString(R.string.mark_as_active)
                                            changeStatusCustomersAPI(
                                                loginModel?.data?.bearer_access_token,
                                                customer_id,
                                                status
                                            )


                                        }
                                        "2" -> {//inactive to active case
                                            //1 active

                                            status = "1"
                                            popupMenu.menu.getItem(0).title =
                                                getString(R.string.mark_as_inactive)
                                            changeStatusCustomersAPI(
                                                loginModel?.data?.bearer_access_token,
                                                customer_id,
                                                status
                                            )


                                        }
                                    }

                                R.id.actionDeleteItemData ->
                                    ensureDeleteDialog(cutomerDetailModel.customers?.display_name.toString())

                            }
                        }
                        true
                    })
                    popupMenu.show()
                }
            }
        }

        if (!ConnectivityStateHolder.isConnected) {
            // Network is not available
            CommonUtils.showDialog(this, getString(R.string.please_check_internet_msg))
        }
    }

    // is editable 0 case (transaction are there for the customer)
    private fun deleteCustDialog(customer: String) {
        val builder = AlertDialog.Builder(this)
        val InActiveClick = { dialog: DialogInterface, which: Int ->

            changeStatusCustomersAPI(loginModel?.data?.bearer_access_token, customer_id, "2")

        }
        val dialogdismiss = { dialog: DialogInterface, which: Int ->
            dialog.dismiss()
        }
        with(builder)
        {
            setTitle(context.getString(R.string.delCustDialog1Title))
            setMessage(context.getString(R.string.custDialog1Message))
            setPositiveButton(context.getString(R.string.Cancel), dialogdismiss)
            setNeutralButton(
                context.getString(R.string.mark_as_inactive),
                DialogInterface.OnClickListener(function = InActiveClick)
            )
            show()
        }
    }

    // is editable 1 case (no transaction are there for the customer)
    private fun ensureDeleteDialog(customer: String) {
        val builder = AlertDialog.Builder(this)
        val DeleteClick = { dialog: DialogInterface, which: Int ->
            //customer delete api call

            deleteContact(loginModel?.data?.bearer_access_token, customer_id)

        }
        val dialogdismiss = { dialog: DialogInterface, which: Int ->
            dialog.dismiss()
        }
        with(builder)
        {
            setTitle(context.getString(R.string.delCustDialog2Title))
            setMessage(context.getString(R.string.custDialog2Message))
            setPositiveButton(context.getString(R.string.Cancel), dialogdismiss)
            setNeutralButton(
                context.getString(R.string.Delete),
                DialogInterface.OnClickListener(function = DeleteClick)
            )
            show()
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
            if (data.permission!!.get(i).startsWith(getString(R.string.customers))) {
                // Restriction check for Customer
                when (data.permission!!.get(i).equals(getString(R.string.cust_add_edit), true)) {
                    true -> {
                        imgRight2.visibility = View.VISIBLE
                    }
                    else->{

                    }
                }
            }
            if (data.permission!!.get(i).startsWith(getString(R.string.customers))) {
                // Restriction check for Customer
                when (data.permission!!.get(i).equals(getString(R.string.cust_delete), true)) {
                    true -> {
                        popupMenu.menu.getItem(1).setVisible(true)
                    }
                    else->{

                    }
                }
            }

        }
    }



    fun customerDetailAPI(
        token: String?,
        company_id: String?,
        customer_id: String?
    ) {

        if (NetworkUtils.isConnected()) {

            viewModel.customerDetail(token, company_id, customer_id).observe(this, Observer {
                it?.let { resource ->
                    when (resource.status) {
                        Status.SUCCESS -> {
                            if (it.data?.status == true) {
                                llCustDetail_Root.visibility = View.VISIBLE
                                cutomerDetailModel = it.data
                                tvTitle.text = cutomerDetailModel.customers?.display_name
                                fill_fine_cash_card_details(cutomerDetailModel)

                                when (cutomerDetailModel.customers?.status?.equals("1", true)) {
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
                                    else->{

                                    }
                                }
                                setupTabViewPager(cutomerDetailModel)

                                tvDebitUserDeta.isSelected = true
                                tvCreditUserDeta.isSelected = true
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
                            if (!this::adapter.isInitialized)
                                CommonUtils.showProgress(this)
                        }
                    }
                }
            })
        } else {

        }
    }

    private fun fill_fine_cash_card_details(cutomerDetailModel: CustomerDetailModel) {
        when (cutomerDetailModel.customers!!.display_fine_balance) {
            "0.000" -> {
                binding.tvDebitUserDeta.text = cutomerDetailModel.customers!!.display_fine_balance
                binding.tvDebitUserDeta.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.header_black_text
                    )
                )
            }

            else -> {
                binding.tvDebitUserDeta.text =
                    cutomerDetailModel.customers!!.display_fine_balance + " " + cutomerDetailModel.customers!!.display_fine_default_term

                if (cutomerDetailModel.customers!!.display_fine_default_term.equals("Dr", ignoreCase = true)) {
                    binding.tvDebitUserDeta.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.debit_color
                        )
                    )
                } else
                    binding.tvDebitUserDeta.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.credit_color
                        )
                    )
            }
        }
        when (cutomerDetailModel.customers!!.cash_balance) {
            "0.00" -> {
                binding.tvCreditUserDeta.text = cutomerDetailModel.customers!!.cash_balance
                binding.tvCreditUserDeta.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.header_black_text
                    )
                )
            }
            else -> {
                binding.tvCreditUserDeta.text =
                    cutomerDetailModel.customers!!.cash_balance + " " + cutomerDetailModel.customers!!.cash_balance_type

                if (cutomerDetailModel.customers!!.cash_balance_type.equals("Dr", ignoreCase = true))
                    binding.tvCreditUserDeta.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.debit_color
                        )
                    )
                else
                    binding.tvCreditUserDeta.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.credit_color
                        )
                    )
            }
        }

        when (cutomerDetailModel.customers!!.display_silver_fine_balance) {
            "0.00" -> {
                binding.tvSilverUserDeta.text = cutomerDetailModel.customers!!.display_silver_fine_balance
                binding.tvSilverUserDeta.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.header_black_text
                    )
                )
            }
            else -> {
                binding.tvSilverUserDeta.text =
                    cutomerDetailModel.customers!!.display_silver_fine_balance + " " + cutomerDetailModel.customers!!.display_silver_fine_default_term

                if (cutomerDetailModel.customers!!.display_silver_fine_default_term.equals("Dr", ignoreCase = true))
                    binding.tvSilverUserDeta.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.debit_color
                        )
                    )
                else
                    binding.tvSilverUserDeta.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.credit_color
                        )
                    )
            }
        }

    }

    private fun deleteContact(token: String?, contact_id: String?) {
        if (NetworkUtils.isConnected()) {
            if (isValidClickPressed()) {
                viewModel.deleteContact(token, contact_id).observe(this, Observer {
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
                                        deleteCustDialog(cutomerDetailModel.customers?.display_name.toString())
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

    fun changeStatusCustomersAPI(
        token: String?,
        customer_id: String?,
        status: String?
    ) {

        if (NetworkUtils.isConnected()) {
            if (isValidClickPressed()) {
                viewModel.changeStatusCustomers(token, customer_id, status).observe(this, Observer {
                    it?.let { resource ->
                        when (resource.status) {
                            Status.SUCCESS -> {
                                if (it.data?.status == true) {

                                    changeStatusCustomerModel = it.data
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

    private fun setupTabViewPager(customerDetailModel: CustomerDetailModel) {

        if (!this::adapter.isInitialized) {
            adapter = CustomerDetailAdapter(
                this, supportFragmentManager,
                tabLayout.tabCount,
                customerDetailModel
            )
            binding.viewPager.removeAllViews()
            binding.viewPager.adapter = adapter
            binding.viewPager.addOnPageChangeListener(TabLayoutOnPageChangeListener(tabLayout))
            binding.tabLayout.addOnTabSelectedListener(object : OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab) {
                    binding.viewPager.currentItem = tab.position
                    tabposition = tab.position
                }

                override fun onTabUnselected(tab: TabLayout.Tab) {}
                override fun onTabReselected(tab: TabLayout.Tab) {}
            })
            binding.viewPager.currentItem = tabposition
        }
    }
}