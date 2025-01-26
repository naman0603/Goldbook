package com.goldbookapp.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.blankj.utilcode.util.NetworkUtils
import com.goldbookapp.R
import com.goldbookapp.api.ApiHelper
import com.goldbookapp.api.RetrofitBuilder
import com.goldbookapp.databinding.QuickSetupActivityBinding
import com.goldbookapp.model.SignupModel
import com.goldbookapp.ui.activity.auth.LoginActivity
import com.goldbookapp.ui.activity.user.VerifyPhoneActivity
import com.goldbookapp.ui.activity.viewmodel.QuickSetupSuggestionViewModel
import com.goldbookapp.ui.activity.viewmodel.ViewModelFactory
import com.goldbookapp.ui.adapter.QuickSetupSuggestionAdapter
import com.goldbookapp.utils.CommonUtils
import com.goldbookapp.utils.CommonUtils.Companion.clickWithDebounce
import com.goldbookapp.utils.Constants
import com.goldbookapp.utils.PreferenceHelper
import com.goldbookapp.utils.PreferenceHelper.defaultPrefs
import com.goldbookapp.utils.PreferenceHelper.get
import com.goldbookapp.utils.PreferenceHelper.set
import com.goldbookapp.utils.Status
import com.google.gson.Gson
import com.hamzasharuf.networkmonitor.ConnectivityStateHolder
import com.hamzasharuf.networkmonitor.Event
import com.hamzasharuf.networkmonitor.NetworkEvents
import kotlinx.android.synthetic.main.quick_setup_activity.*

class QuickSetupActivity : AppCompatActivity(){

    private lateinit var viewModel: QuickSetupSuggestionViewModel

    lateinit var binding: QuickSetupActivityBinding

    private lateinit var adapter: QuickSetupSuggestionAdapter
    private var isfromSuggestionClick:Boolean = false
    private var clickedNameLength = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.quick_setup_activity)


        setupViewModel()

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
            setupUIandListner()
        }

        if (!ConnectivityStateHolder.isConnected) {
            // Network is not available
            CommonUtils.showDialog(this, getString(R.string.please_check_internet_msg))

        }
    }

    private fun setupViewModel() {
        viewModel =
            ViewModelProvider(this, ViewModelFactory(ApiHelper(RetrofitBuilder.apiService))).get(
                QuickSetupSuggestionViewModel::class.java
            )

    }

    private fun setupUIandListner(){

        val prefs = PreferenceHelper.defaultPrefs(this)
        val signupModel: SignupModel? = Gson().fromJson(
            prefs[Constants.PREF_COMPANY_REGISTER_KEY, ""],
            SignupModel::class.java
        ) //getter

        binding.recyclerViewSuggestion.layoutManager = LinearLayoutManager(this)
        adapter = QuickSetupSuggestionAdapter(arrayListOf())
        binding.recyclerViewSuggestion.adapter = adapter

        binding.imgNext.clickWithDebounce {

            if(binding.txtUsername.text.toString().length > 0) {
                updateUsername(binding.txtUsername.text.toString(), signupModel)
            }else{
                binding.txtUsernameInputLayout?.error = "Please Enter Username"
            }
        }


        binding.txtUsername.doOnTextChanged { text, start, before, count ->
            txtUsernameInputLayout?.error = null
            if (text?.length!! > 2) {
                when (clickedNameLength.equals(text?.length) && binding.txtUsername.text?.equals(text)!!) {
                    true -> {
                        isfromSuggestionClick = true
                        //getSuggestion(binding.txtUsername.text.toString(), signupModel)
                        binding.txtUsername.setSelection(text.length)
                    }
                    false -> {
                        isfromSuggestionClick = false
                        clickedNameLength = 0
                       // getSuggestion(binding.txtUsername.text.toString(), signupModel)
                        binding.txtUsername.setSelection(text.length)
                    }
                }

            } else {
                binding.recyclerViewSuggestion.visibility = View.GONE
            }
        }

    }

    private fun retrieveList(names: List<String>?) {
        adapter.apply {
            addSuggesstion(names)
            notifyDataSetChanged()
        }
    }

    fun getSuggestion(username: String?, signupModel: SignupModel?) {
        if(NetworkUtils.isConnected()){
        viewModel.getSuggestion(
            username,
            signupModel?.data?.user?.userInfo?.company_id
        ).observe(this, Observer {
            it?.let { resource ->
                when (resource.status) {
                    Status.SUCCESS -> {

                        if (it.data?.status == true) {
                            //CommonUtils.hideProgress()

                            if(binding.txtUsername.text.toString().length > 2 && isfromSuggestionClick!=true) {
                                binding.recyclerViewSuggestion.visibility = View.VISIBLE
                            }
                            retrieveList(it.data?.data?.company?.suggestion)

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
    fun setSuggestedUsername(name: String) {
        binding.txtUsername.setText(name)
        clickedNameLength = name.length
        isfromSuggestionClick = true
        binding.recyclerViewSuggestion.visibility = View.GONE
    }

    fun updateUsername(username: String?, signupModel: SignupModel?) {
        if(NetworkUtils.isConnected()){

            viewModel.userRegister(
                signupModel?.data?.user?.userInfo?.name,
                signupModel?.data?.user?.userInfo?.password,
                signupModel?.data?.user?.userInfo?.email,
                signupModel?.data?.user?.userInfo?.mobile_no,
                username,
                false
            ).observe(this, Observer {
                it?.let { resource ->
                    when (resource.status) {
                        Status.SUCCESS -> {

                            if (it.data?.status == true) {

                                val prefs = defaultPrefs(this)
                                prefs[Constants.PREF_COMPANY_REGISTER_KEY] = Gson().toJson(it.data) //setter


                                startActivity(Intent(this, VerifyPhoneActivity::class.java))
                                this.finish()
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
    override fun onDestroy() {
        super.onDestroy()
        CommonUtils.hideProgress()
    }

    override fun onResume() {
        super.onResume()

        NetworkEvents.observe(this, Observer {
            if (it is Event.ConnectivityEvent) {
                if (ConnectivityStateHolder.isConnected) {
                    // Network is available
                    CommonUtils.hideInternetDialog()

                }

                if (!ConnectivityStateHolder.isConnected) {
                    // Network is not available
                    CommonUtils.showDialog(this, getString(R.string.please_check_internet_msg))

                }
            }
        })


    }
    override fun onBackPressed() {
        super.onBackPressed()

    }

    fun showWarningDialog(context: Context, message: String) {
        MaterialDialog(context).show {
            title(R.string.app_name)
            message(text = message)
            cancelable(false)
            positiveButton(R.string.yes){
                startActivity(Intent(context, LoginActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_NEW_TASK))
            }
            negativeButton(R.string.no){
                dismiss()
            }
        }
    }
}