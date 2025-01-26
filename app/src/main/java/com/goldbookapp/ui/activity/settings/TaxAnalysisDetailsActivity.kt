package com.goldbookapp.ui.activity.settings

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.goldbookapp.R
import com.goldbookapp.databinding.ActivityTaxAnalysisDetailsBinding
import com.goldbookapp.model.LoginModel
import com.goldbookapp.model.TaxAnalysisListModel
import com.goldbookapp.ui.adapter.TaxAnalysisAdapter
import com.goldbookapp.utils.CommonUtils
import com.goldbookapp.utils.CommonUtils.Companion.clickWithDebounce
import com.goldbookapp.utils.Constants
import com.goldbookapp.utils.PreferenceHelper
import com.goldbookapp.utils.PreferenceHelper.get
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hamzasharuf.networkmonitor.ConnectivityStateHolder
import com.hamzasharuf.networkmonitor.Event
import com.hamzasharuf.networkmonitor.NetworkEvents
import kotlinx.android.synthetic.main.toolbar_layout.*

class TaxAnalysisDetailsActivity : AppCompatActivity() {

    lateinit var binding: ActivityTaxAnalysisDetailsBinding
    lateinit var prefs: SharedPreferences
    lateinit var loginModel: LoginModel
    lateinit var taxAnalysisList: ArrayList<TaxAnalysisListModel.TaxAnalysisList>
    private lateinit var adapter: TaxAnalysisAdapter
    var transaction_type: String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_tax_analysis_details)
        setupUIandListner()
    }

    private fun setupUIandListner() {

        prefs = PreferenceHelper.defaultPrefs(this)
        loginModel = Gson().fromJson(
            prefs[Constants.PREF_LOGIN_DETAIL_KEY, ""],
            LoginModel::class.java
        )

        imgLeft.setImageResource(R.drawable.ic_back)
        tvTitle.setText("Tax Analysis Details")

        getDataFromIntent()
        getDataFromPref()

        imgLeft?.clickWithDebounce {
            onBackPressed()
        }
    }

    private fun getDataFromIntent() {
        if (intent.extras != null && intent.extras!!.containsKey(Constants.TRANSACTION_TYPE)) {
            transaction_type = intent.getStringExtra(Constants.TRANSACTION_TYPE)
        }
    }

    private fun getDataFromPref() {
        if (prefs.contains(Constants.PREF_SALES_TAX_ANALYSIS_LIST_KEY)) {

            Log.v("datafound", "")
            val lessWeightBreakup =
                object :
                    TypeToken<ArrayList<TaxAnalysisListModel.TaxAnalysisList>>() {}.type
            taxAnalysisList = Gson().fromJson(
                prefs[Constants.PREF_SALES_TAX_ANALYSIS_LIST_KEY, ""],
                lessWeightBreakup
            )
            binding.lyLessWeightDetails.visibility = View.VISIBLE
            binding.recyclerViewTaxAnalysis.layoutManager = LinearLayoutManager(this)
            adapter = TaxAnalysisAdapter(taxAnalysisList, false,transaction_type!!)
            binding.recyclerViewTaxAnalysis.adapter = adapter
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
            getDataFromPref()
        }

        if (!ConnectivityStateHolder.isConnected) {
            // Network is not available
            CommonUtils.showDialog(this, getString(R.string.please_check_internet_msg))
        }
    }
}