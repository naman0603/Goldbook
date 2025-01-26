package com.goldbookapp.ui.activity.settings

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.NetworkUtils
import com.goldbookapp.R
import com.goldbookapp.api.ApiHelper
import com.goldbookapp.api.RetrofitBuilder
import com.goldbookapp.databinding.ActivityTcsTdsBinding
import com.goldbookapp.model.*
import com.goldbookapp.ui.activity.viewmodel.TaxTcsDetailViewModel
import com.goldbookapp.ui.activity.viewmodel.ViewModelFactory
import com.goldbookapp.ui.adapter.TcsNatureOfGoodsAdapter
import com.goldbookapp.utils.*
import com.goldbookapp.utils.CommonUtils.Companion.clickWithDebounce
import com.goldbookapp.utils.CommonUtils.Companion.isValidClickPressed
import com.goldbookapp.utils.PreferenceHelper.get
import com.goldbookapp.utils.PreferenceHelper.set
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hamzasharuf.networkmonitor.ConnectivityStateHolder
import com.hamzasharuf.networkmonitor.Event
import com.hamzasharuf.networkmonitor.NetworkEvents
import kotlinx.android.synthetic.main.activity_tcs_tds.*
import kotlinx.android.synthetic.main.toolbar_layout.*

class TCSActivity : AppCompatActivity() {
    private var saveTaxbtnShow: Boolean = false
    private var nog_nop_type : String? = "nog"
    private var isFromTaxTcs : Boolean = true
    private var isFirstTime : Boolean = false
    var list_Nog: ArrayList<TaxDetailTcsModel.Data.Nature_of_goods>? = null
    var list_Nop: ArrayList<TaxDetailTdsModel.Data.Nature_of_payment>? = null
    lateinit var popupMenu: PopupMenu
    private lateinit var viewModel: TaxTcsDetailViewModel
    lateinit var binding: ActivityTcsTdsBinding
    lateinit var prefs: SharedPreferences
    lateinit var loginModel: LoginModel
    lateinit var selectedCollectorType: String
    var enable_tcs: Int = 0
    private lateinit var tcsNogAdapter: TcsNatureOfGoodsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_tcs_tds)
        setupViewModel()
        setupUIandListner()
    }

    private fun setupUIandListner() {
        imgLeft.setImageResource(R.drawable.ic_back)
        tvTitle.setText(R.string.tcs)

        getLoginModelFromPrefs()
        isFirstTime = true
        if (prefs.contains(Constants.PREF_ADD_NOG_KEY)) {
            prefs.edit().remove(Constants.PREF_ADD_NOG_KEY).apply()
        }
        if (prefs.contains(Constants.PREF_ADD_NOP_KEY)) {
            prefs.edit().remove(Constants.PREF_ADD_NOP_KEY).apply()
        }


        if(intent.extras?.containsKey(Constants.isFromTaxTcs)!!){
            isFromTaxTcs = intent.getBooleanExtra(Constants.isFromTaxTcs,true)
            when(isFromTaxTcs){
                true -> {
                    tvTitle.setText(R.string.tcs)
                    txtDisableTcsTds.setText("Enable TCS?")
                    txtTCSTDSCollectorDeductorDetail.setText("Tax Collector Details")
                    txt_tcs_tds_nog_nop_title.text = "Nature of Goods"
                    nog_nop_type = "nog"
                    // tcs api call
                    getTaxTcsDetailApi(loginModel.data?.bearer_access_token)
                    gettcsCollectorTypeApi(loginModel.data?.bearer_access_token)

                }
                false -> {
                    tvTitle.setText(R.string.tds)
                    txtDisableTcsTds.setText("Enable TDS?")
                    txtTCSTDSCollectorDeductorDetail.setText("Tax Deductor Details")
                    txt_tcs_tds_nog_nop_title.text = "Nature of Payments"
                    nog_nop_type = "nop"

                    // tds api call
                    getTaxTdsDetailApi(loginModel.data?.bearer_access_token)
                    gettdsDeductorTypeApi(loginModel.data?.bearer_access_token)
                }
            }
        }


        imgLeft?.clickWithDebounce {

            onBackPressed()
        }

        tvAddTCSTDSNature?.clickWithDebounce {
            when(isFromTaxTcs){
                true -> {
                    startActivity(
                        Intent(
                            this,
                            TCSNatureActivity::class.java
                        )  .putExtra(Constants.NOG_NOP_TYPE,"nog" ).putExtra(Constants.isFromTcsNogAdd, true)
                    )

                }
                false -> {
                    startActivity(
                        Intent(
                            this,
                            TCSNatureActivity::class.java
                        )  .putExtra(Constants.NOG_NOP_TYPE,"nop").putExtra(Constants.isFromTcsNogAdd, false)
                    )
                }
            }

        }
    }
    private fun getLoginModelFromPrefs() {
        prefs = PreferenceHelper.defaultPrefs(applicationContext!!)
        loginModel = Gson().fromJson(
            prefs[Constants.PREF_LOGIN_DETAIL_KEY, ""],
            LoginModel::class.java
        ) //getter
    }
    private fun setupViewModel() {
        viewModel =
            ViewModelProvider(this, ViewModelFactory(ApiHelper(RetrofitBuilder.apiService))).get(
                TaxTcsDetailViewModel::class.java
            )

        binding.setLifecycleOwner(this)
    }
    fun removeNopNogItem(index: Int, isFromTCSNog: Boolean) {
        if (isValidClickPressed()) {
            when (isFromTCSNog) {
                true -> {
                    if (list_Nog != null && list_Nog!!.size > 0) {
                        if (index >= list_Nog!!.size) {
                            //index not exists
                        } else {
                            // index exists
                            when (list_Nog!!.get(index).nature_of_goods_id.isNullOrBlank()) {
                                true -> {
                                    list_Nog!!.removeAt(index)
                                    tcsNogAdapter.notifyDataSetChanged()

                                    if (list_Nog!!.size > 0) {
                                        prefs[Constants.PREF_ADD_NOG_KEY] = Gson().toJson(list_Nog)

                                    } else {
                                        prefs.edit().remove(Constants.PREF_ADD_NOG_KEY).apply()

                                    }

                                }
                                false -> {
                                    ensureDeleteNogDialog(index)

                                }
                            }
                        }
                    }
                }
                false -> {
                    if (list_Nop != null && list_Nop!!.size > 0) {
                        if (index >= list_Nop!!.size) {
                            //index not exists
                        } else {
                            // index exists
                            when (list_Nop!!.get(index).nature_of_payment_id.isNullOrBlank()) {
                                true -> {
                                    list_Nop!!.removeAt(index)
                                    tcsNogAdapter.notifyDataSetChanged()

                                    if (list_Nop!!.size > 0) {
                                        prefs[Constants.PREF_ADD_NOP_KEY] = Gson().toJson(list_Nop)

                                    } else {
                                        prefs.edit().remove(Constants.PREF_ADD_NOP_KEY).apply()

                                    }

                                }
                                false -> {
                                    ensureDeleteNopDialog(index)
                                }
                            }

                        }
                    }
                }
            }
        }
    }
    private fun ensureDeleteNogDialog(index: Int) {
        val builder = AlertDialog.Builder(this)
        val DeleteClick = { dialog: DialogInterface, which: Int ->
            //itemCategory delete api call
            deleteNog(index,loginModel.data?.bearer_access_token,list_Nog!!.get(index).nature_of_goods_id)
        }
        val dialogdismiss = { dialog: DialogInterface, which: Int ->
            dialog.dismiss()
        }
        with(builder)
        {
            setTitle(context.getString(R.string.delnogDialogTitle))
            setMessage(context.getString(R.string.nogDialogMessage)+" "+list_Nog!!.get(index).name+ " ?")
            setPositiveButton(context.getString(R.string.Cancel),dialogdismiss)
            setNeutralButton(context.getString(R.string.Delete), DialogInterface.OnClickListener(function = DeleteClick))
            show()
        }
    }
    private fun ensureDeleteNopDialog(index: Int) {
        val builder = AlertDialog.Builder(this)
        val DeleteClick = { dialog: DialogInterface, which: Int ->
            //itemCategory delete api call
            deleteNop(index,loginModel.data?.bearer_access_token,list_Nop!!.get(index).nature_of_payment_id)
        }
        val dialogdismiss = { dialog: DialogInterface, which: Int ->
            dialog.dismiss()
        }
        with(builder)
        {
            setTitle(context.getString(R.string.delnopDialogTitle))
            setMessage(context.getString(R.string.nogDialogMessage)+" "+list_Nop!!.get(index).name+ " ?")
            setPositiveButton(context.getString(R.string.Cancel),dialogdismiss)
            setNeutralButton(context.getString(R.string.Delete), DialogInterface.OnClickListener(function = DeleteClick))
            show()
        }
    }
    private fun deleteNog(index: Int, token: String?, nog_id: String?) {
        if(NetworkUtils.isConnected()) {
            viewModel.deleteNog(token, nog_id).observe(this, Observer {
                it?.let { resource ->
                    when (resource.status) {
                        Status.SUCCESS -> {
                            if (it.data?.status == true) {
                                list_Nog!!.removeAt(index)
                                tcsNogAdapter.notifyDataSetChanged()
                                Toast.makeText(
                                    this,
                                    it.data?.message,
                                    Toast.LENGTH_LONG
                                )
                                    .show()
                                if(list_Nog!!.size > 0){
                                    prefs[Constants.PREF_ADD_NOG_KEY] = Gson().toJson(list_Nog)

                                }else{
                                    prefs.edit().remove(Constants.PREF_ADD_NOG_KEY).apply()

                                }


                            } else {

                                when(it.data!!.code == Constants.ErrorCode){
                                    true-> {
                                        Toast.makeText(
                                            this,
                                            it.data.errormessage?.message,
                                            Toast.LENGTH_LONG
                                        )
                                            .show()
                                    }
                                    false->{
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
    private fun deleteNop(index: Int, token: String?, nop_id: String?) {
        if(NetworkUtils.isConnected()) {
            viewModel.deleteNop(token, nop_id).observe(this, Observer {
                it?.let { resource ->
                    when (resource.status) {
                        Status.SUCCESS -> {
                            if (it.data?.status == true) {
                                list_Nop!!.removeAt(index)
                                tcsNogAdapter.notifyDataSetChanged()
                                Toast.makeText(
                                    this,
                                    it.data?.message,
                                    Toast.LENGTH_LONG
                                )
                                    .show()
                                if(list_Nop!!.size > 0){
                                    prefs[Constants.PREF_ADD_NOP_KEY] = Gson().toJson(list_Nop)

                                }else{
                                    prefs.edit().remove(Constants.PREF_ADD_NOP_KEY).apply()

                                }


                            } else {

                                when(it.data!!.code == Constants.ErrorCode){
                                    true-> {
                                        Toast.makeText(
                                            this,
                                            it.data.errormessage?.message,
                                            Toast.LENGTH_LONG
                                        )
                                            .show()
                                    }
                                    false->{
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
            if (intent.extras != null && intent.extras!!.containsKey(Constants.Change_Status)) {
                saveTaxbtnShow = intent.getBooleanExtra(Constants.Change_Status, false)
            }
            when(loginModel.data!!.user_info!!.user_type.equals("user",true)){
                // user type user
                true -> {
                    // apply restriciton
                    defaultDisableAllButtonnUI()
                        when(saveTaxbtnShow){
                            true -> {
                                binding.btnSaveSettingTcsTds.visibility = View.VISIBLE
                                binding.tvAddTCSTDSNature.visibility = View.VISIBLE
                            }
                            false -> {
                                binding.btnSaveSettingTcsTds.visibility = View.GONE
                                binding.tvAddTCSTDSNature.visibility = View.GONE
                            }

                    }
                }
                // user_type -> admin or super_admin or any other
                false -> {
                    saveTaxbtnShow= true
                    defaultEnableAllButtonnUI()
                }
            }


            when(nog_nop_type){
                "nog" -> {
                    //tcs
                    if(!isFirstTime){
                        getTcsNogsFromPref()
                      //  Log.v("NogsFromPref","true")
                    }
                    isFirstTime = false

                }
                "nop" -> {
                    //tds
                    if(!isFirstTime){
                        getTdsNopsFromPref()
                     //   Log.v("NopsFromPref","true")
                    }
                    isFirstTime = false

                }
            }
        }

        if (!ConnectivityStateHolder.isConnected) {
            // Network is not available
            CommonUtils.showDialog(this, getString(R.string.please_check_internet_msg))

        }
    }
    private fun defaultEnableAllButtonnUI() {
        binding.btnSaveSettingTcsTds.visibility = View.VISIBLE
        binding.tvAddTCSTDSNature.visibility = View.VISIBLE
    }

    private fun defaultDisableAllButtonnUI() {
        binding.btnSaveSettingTcsTds.visibility = View.GONE
        binding.tvAddTCSTDSNature.visibility = View.GONE
    }
    private fun getTcsNogsFromPref() {
        if (prefs.contains(Constants.PREF_ADD_NOG_KEY)) {

            val multipleNogType = object : TypeToken<ArrayList<TaxDetailTcsModel.Data.Nature_of_goods>>() {}.type
            list_Nog = Gson().fromJson(prefs[Constants.PREF_ADD_NOG_KEY, ""], multipleNogType)

            if(enable_tcs == 1){
                setupNatureofGoods(list_Nog,null)
            }

        }
    }
    private fun getTdsNopsFromPref() {
        if (prefs.contains(Constants.PREF_ADD_NOP_KEY)) {

            val multipleNopType = object : TypeToken<ArrayList<TaxDetailTdsModel.Data.Nature_of_payment>>() {}.type
            list_Nop = Gson().fromJson(prefs[Constants.PREF_ADD_NOP_KEY, ""], multipleNopType)

            if(enable_tcs == 1){
                setupNatureofGoods(null,list_Nop)
            }
        }
    }

    private fun gettcsCollectorTypeApi(token: String?) {
        if(NetworkUtils.isConnected()) {

            viewModel.gettcsCollectorTypeApi(token).observe(this, Observer {
                it?.let { resource ->
                    when (resource.status) {
                        Status.SUCCESS -> {
                            if (it.data?.status == true) {
                                setCollectorDropDownData(it.data.data)

                            } else {

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
    private fun gettdsDeductorTypeApi(token: String?) {
        if(NetworkUtils.isConnected()) {

            viewModel.gettdsDeductorTypeApi(token).observe(this, Observer {
                it?.let { resource ->
                    when (resource.status) {
                        Status.SUCCESS -> {
                            if (it.data?.status == true) {
                                setCollectorDropDownData(it.data.data)

                            } else {

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

    private fun setCollectorDropDownData(data: List<GetTcsCollectorTypeModel.Data>?) {
        fillDropDownCollectorType(data)

    }

    private fun fillDropDownCollectorType(data: List<GetTcsCollectorTypeModel.Data>?) {
        popupMenu = PopupMenu(this, binding.txtDuductorTypeTCSTDS)

        popupMenu.menu.add(Menu.NONE, 1, 1, data!!.get(0).artificial_juridical_person)
        popupMenu.menu.add(Menu.NONE, 2, 2, data.get(1).association_of_persons)
        popupMenu.menu.add(Menu.NONE, 3, 3, data.get(2).body_of_individuals)
        popupMenu.menu.add(Menu.NONE, 4, 4, data.get(3).company_non_resident)
        popupMenu.menu.add(Menu.NONE, 5, 5, data.get(4).company_resident)
        popupMenu.menu.add(Menu.NONE, 6, 6, data.get(5).cooperative_society)
        popupMenu.menu.add(Menu.NONE, 7, 7, data.get(6).government)
        popupMenu.menu.add(Menu.NONE, 8, 8, data.get(7).individual_huf_non_resident)
        popupMenu.menu.add(Menu.NONE, 9, 9, data.get(8).individual_huf_resident)
        popupMenu.menu.add(Menu.NONE, 10, 10, data.get(9).local_authority)
        popupMenu.menu.add(Menu.NONE, 11, 11, data.get(10).partnership_firm)

        binding.txtDuductorTypeTCSTDS.clickWithDebounce {

            openCollectorType(binding.txtDuductorTypeTCSTDS)
        }

    }

    private fun openCollectorType(view: View) {
        popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
            when(item.itemId.toString()){
                "1" -> {
                    selectedCollectorType = "artificial_juridical_person"
                }
                "2" -> {
                    selectedCollectorType = "association_of_persons"
                }
                "3" -> {
                    selectedCollectorType = "body_of_individuals"

                }
                "4" -> {
                    selectedCollectorType = "company_non_resident"
                }
                "5" -> {
                    selectedCollectorType = "company_resident"
                }
                "6" -> {
                    selectedCollectorType = "cooperative_society"
                }
                "7" -> {
                    selectedCollectorType = "government"

                }
                "8" -> {
                    selectedCollectorType = "individual_huf_non_resident"
                }
                "9" -> {
                    selectedCollectorType = "individual_huf_resident"
                }
                "10" -> {
                    selectedCollectorType = "local_authority"
                }
                "11" -> {
                    selectedCollectorType = "partnership_firm"

                }

            }

            binding.txtDuductorTypeTCSTDS.setText(item.title)
            true

        })

        popupMenu.show()


    }

    private fun getTaxTcsDetailApi(token: String?) {
        if(NetworkUtils.isConnected()) {

            viewModel.getTaxTcsDetailApi(token).observe(this, Observer {
                it?.let { resource ->
                    when (resource.status) {
                        Status.SUCCESS -> {
                            if (it.data?.status == true) {
                                setdefaultTcsData(it.data.data)

                            } else {

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

    private fun setdefaultTcsData(data: TaxDetailTcsModel.Data) {

        prefs.edit().remove(Constants.PREF_ADD_NOG_KEY).apply()
        // default selection for drop down on basis of api
         selectedCollectorType = data.tcs_collector_type!!
         binding.txtDuductorTypeTCSTDS.setText(data.tcs_collector_type_name)

        binding.switchEnableDisableTCSTDS.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                enable_tcs = 1
                binding.llTcsTdsOrgdetails.visibility = View.VISIBLE
                binding.cardtcstdsTaxCollectorDetails.visibility = View.VISIBLE
                binding.llTcsTdsNog.visibility = View.VISIBLE
                binding.recyclerViewNatureGoods.visibility = View.VISIBLE
                when(data.nature_of_goods!!.get(0).nature_of_goods_id.isNullOrEmpty()){
                    true -> {
                        binding.recyclerViewNatureGoods.visibility = View.GONE
                    }
                    false -> {
                        binding.recyclerViewNatureGoods.visibility = View.VISIBLE
                        binding.llTcsTdsNog.visibility = View.VISIBLE
                        setupNatureofGoods(data.nature_of_goods,null)
                        prefs[Constants.PREF_ADD_NOG_KEY] = Gson().toJson(data.nature_of_goods)
                    }
                }

            } else {
                enable_tcs = 0
                binding.llTcsTdsOrgdetails.visibility = View.GONE
                binding.cardtcstdsTaxCollectorDetails.visibility = View.GONE
                binding.llTcsTdsNog.visibility = View.GONE
                binding.recyclerViewNatureGoods.visibility = View.GONE
            }
        }
        when(data.enable_tcs){
            1 -> {
                binding.switchEnableDisableTCSTDS.isChecked = true
                binding.switchEnableDisableTCSTDS.isEnabled = false
            }
            0 -> {
                binding.switchEnableDisableTCSTDS.isChecked = false
                binding.switchEnableDisableTCSTDS.isEnabled = true
            }
        }

        list_Nog = data.nature_of_goods as ArrayList<TaxDetailTcsModel.Data.Nature_of_goods>?
        // fill organization details
        binding.txtTANTcsTds.setText(data.tan_number)
        binding.txtTDSTCSCircle.setText(data.tds_circle)
        binding.txtTaxPersonTCSTDS.setText(data.tcs_person_responsible)
        binding.txtTaxDesignation.setText(data.tcs_designation)
        binding.txtContactNoTCSTDS.setText(data.tcs_contact_number)

        binding.btnSaveSettingTcsTds?.clickWithDebounce {

            if (performValidation()) {
                if (NetworkUtils.isConnected()) {
// common for tcs/tds/gst
                    saveTaxApi(loginModel.data?.bearer_access_token,
                        "tcs",
                        0,
                        "",
                        "",
                        "",
                        "",
                        enable_tcs,
                        0,
                        binding.txtTANTcsTds.text.toString().trim(),
                        binding.txtTDSTCSCircle.text.toString().trim(),
                        selectedCollectorType,
                        binding.txtTaxPersonTCSTDS.text.toString().trim(),
                        binding.txtTaxDesignation.text.toString().trim(),
                        binding.txtContactNoTCSTDS.text.toString().trim(),
                        Gson().toJson(list_Nog),
                        "",
                        "",
                        "",
                        "",
                        ""
                    )
                }
            }

        }
    }

    fun performValidation(): Boolean {
        when(enable_tcs == 1){
            true -> {
                if(binding.txtTANTcsTds.text.toString().isBlank()){
                    CommonUtils.showDialog(this, "Please Enter TAN Number")
                    binding.txtTANTcsTds.requestFocus()
                    return false
                }else if(!CommonUtils.isValidTANDetail(binding.txtTANTcsTds.text.toString())){
                    binding.tvTANTcsTds.error = getString(R.string.enter_valid_tanno_msg)
                    return false
                }
            }
            false -> {
                return true
            }
        }


        return true
    }

    private fun setupNatureofGoods(listNog: List<TaxDetailTcsModel.Data.Nature_of_goods>?,
                                   listNop: List<TaxDetailTdsModel.Data.Nature_of_payment>?) {
        // recyclerview nature of goods setup
        binding.recyclerViewNatureGoods.visibility = View.VISIBLE
        binding.txtNatureofGoodsNoEntries.visibility = View.GONE
        binding.recyclerViewNatureGoods.layoutManager = LinearLayoutManager(this@TCSActivity)
        when(isFromTaxTcs){
            true -> {
                // tcs nog list
                tcsNogAdapter = TcsNatureOfGoodsAdapter(listNog as ArrayList<TaxDetailTcsModel.Data.Nature_of_goods>?,null,saveTaxbtnShow)
            }
            false -> {
                // tds nop list
                tcsNogAdapter = TcsNatureOfGoodsAdapter(null,
                    listNop as ArrayList<TaxDetailTdsModel.Data.Nature_of_payment>?,saveTaxbtnShow
                )

            }            }
        binding.recyclerViewNatureGoods.adapter = tcsNogAdapter
    }

    private fun getTaxTdsDetailApi(token: String?) {
        if(NetworkUtils.isConnected()) {

            viewModel.getTaxTdsDetailApi(token).observe(this, Observer {
                it?.let { resource ->
                    when (resource.status) {
                        Status.SUCCESS -> {
                            if (it.data?.status == true) {
                                setdefaultTdsData(it.data.data)

                            } else {

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

    private fun setdefaultTdsData(data: TaxDetailTdsModel.Data) {
        prefs.edit().remove(Constants.PREF_ADD_NOP_KEY).apply()
        // default selection for drop down on basis of api
        selectedCollectorType = data.tds_deductor_type!!
        binding.txtDuductorTypeTCSTDS.setText(data.tds_deductor_type_name)
        binding.switchEnableDisableTCSTDS.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                enable_tcs = 1
                binding.llTcsTdsOrgdetails.visibility = View.VISIBLE
                binding.cardtcstdsTaxCollectorDetails.visibility = View.VISIBLE
                binding.llTcsTdsNog.visibility = View.VISIBLE
                binding.recyclerViewNatureGoods.visibility = View.VISIBLE
                when(data.nature_of_payment!!.get(0).nature_of_payment_id.isNullOrEmpty()){
                    true -> {
                        binding.recyclerViewNatureGoods.visibility = View.GONE
                    }
                    false -> {
                        binding.recyclerViewNatureGoods.visibility = View.VISIBLE
                        binding.llTcsTdsNog.visibility = View.VISIBLE
                        setupNatureofGoods(null,data.nature_of_payment)
                        prefs[Constants.PREF_ADD_NOP_KEY] = Gson().toJson(data.nature_of_payment)
                    }
                }

            } else {
                enable_tcs = 0
                binding.llTcsTdsOrgdetails.visibility = View.GONE
                binding.cardtcstdsTaxCollectorDetails.visibility = View.GONE
                binding.llTcsTdsNog.visibility = View.GONE
                binding.recyclerViewNatureGoods.visibility = View.GONE
            }
        }
        when(data.enable_tds){
            1 -> {
                binding.switchEnableDisableTCSTDS.isChecked = true
                binding.switchEnableDisableTCSTDS.isEnabled = false
            }
            0 -> {
                binding.switchEnableDisableTCSTDS.isChecked = false
                binding.switchEnableDisableTCSTDS.isEnabled = true
            }
        }

        list_Nop = data.nature_of_payment as ArrayList<TaxDetailTdsModel.Data.Nature_of_payment>?
        // fill organization details
        binding.txtTANTcsTds.setText(data.tan_number)
        binding.txtTDSTCSCircle.setText(data.tds_circle)
        binding.txtTaxPersonTCSTDS.setText(data.tds_person_responsible)
        binding.txtTaxDesignation.setText(data.tds_designation)
        binding.txtContactNoTCSTDS.setText(data.tds_contact_number)

        binding.btnSaveSettingTcsTds?.clickWithDebounce {

            if(performValidation()){
            // common for tcs/tds/gst
            saveTaxApi(loginModel.data?.bearer_access_token,
                "tds",
                0,
                "",
                "",
                "",
                "",
                0,
                enable_tcs,
                binding.txtTANTcsTds.text.toString().trim(),
                binding.txtTDSTCSCircle.text.toString().trim(),
                "",
                "",
                "",
                "",
                "",
                selectedCollectorType,
                binding.txtTaxPersonTCSTDS.text.toString().trim(),
                binding.txtTaxDesignation.text.toString().trim(),
                binding.txtContactNoTCSTDS.text.toString().trim(),
                Gson().toJson(list_Nop)
            )
            }
        }

    }
    fun saveTaxApi(token: String?,
                   type: String?,
                   enable_gst: Int?,
                   gst_state_id: String?,
                   gstin: String?,
                   registration_date: String?,
                   periodicity_of_gst1: String?,
                   enable_tcs: Int?,
                   enable_tds: Int?,
                   tan_number: String?,
                   tds_circle: String?,
                   tcs_collector_type: String?,
                   tcs_person_responsible: String?,
                   tcs_designation: String?,
                   tcs_contact_number: String?,
                   nature_of_goods: String?,
                   tds_deductor_type: String?,
                   tds_person_responsible: String?,
                   tds_designation: String?,
                   tds_contact_number: String?,
                   nature_of_payment: String?
    ){

        viewModel.saveTcsTdsDetailApi(
            token,
            type,
            enable_gst,
            gst_state_id,
            gstin,
            registration_date,
            periodicity_of_gst1,
            enable_tcs,
            enable_tds,
            tan_number,
            tds_circle,
            tcs_collector_type,
            tcs_person_responsible,
            tcs_designation,
            tcs_contact_number,
            nature_of_goods,
            tds_deductor_type,
            tds_person_responsible,
            tds_designation,
            tds_contact_number,
            nature_of_payment

        ).observe(this, Observer {
            it?.let { resource ->
                when (resource.status) {
                    Status.SUCCESS -> {
                        if (it.data?.status == true) {

                            Toast.makeText(
                                this,
                                it.data.message,
                                Toast.LENGTH_LONG
                            )
                                .show()

                            this.finish()

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