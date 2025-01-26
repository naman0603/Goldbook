package com.goldbookapp.ui.activity.settings

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.blankj.utilcode.util.NetworkUtils
import com.goldbookapp.R
import com.goldbookapp.api.ApiHelper
import com.goldbookapp.api.RetrofitBuilder
import com.goldbookapp.databinding.TermsOfServiceActivityBinding
import com.goldbookapp.model.TermsOfServiceModel
import com.goldbookapp.ui.activity.viewmodel.TermsOfServiceViewModel
import com.goldbookapp.ui.activity.viewmodel.ViewModelFactory
import com.goldbookapp.utils.CommonUtils
import com.goldbookapp.utils.CommonUtils.Companion.clickWithDebounce
import com.goldbookapp.utils.PreferenceHelper
import com.goldbookapp.utils.Status
import com.hamzasharuf.networkmonitor.ConnectivityStateHolder
import com.hamzasharuf.networkmonitor.Event
import com.hamzasharuf.networkmonitor.NetworkEvents
import kotlinx.android.synthetic.main.toolbar_layout.view.*


class TermsOfServiceActivity : AppCompatActivity(){

    lateinit var binding: TermsOfServiceActivityBinding
     var page_id: Int=1
    lateinit var cms : String
  //  lateinit var loginModel: LoginModel

    lateinit var termsOfServiceModel: TermsOfServiceModel
    private lateinit var viewModel: TermsOfServiceViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.terms_of_service_activity)

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
            setupUIandListener()
        }

        if (!ConnectivityStateHolder.isConnected) {
            // Network is not available
            CommonUtils.showDialog(this, getString(R.string.please_check_internet_msg))

        }
    }
    override fun onDestroy() {
        super.onDestroy()
        CommonUtils.hideProgress()
    }
    private fun setupViewModel() {
        viewModel =
            ViewModelProvider(this, ViewModelFactory(ApiHelper(RetrofitBuilder.apiService))).get(
                TermsOfServiceViewModel::class.java
            )
        binding.setLifecycleOwner(this)
        //binding.editProfileViewModel = viewModel
    }
    private fun setupUIandListener() {
        val prefs = PreferenceHelper.defaultPrefs(this)
//        loginModel = Gson().fromJson(
//            prefs[Constants.PREF_LOGIN_DETAIL_KEY, ""],
//            LoginModel::class.java
//        ) //getter

        binding.root.imgLeft.setImageResource(R.drawable.ic_back)

        binding.root.imgLeft?.clickWithDebounce{
            onBackPressed()
        }
        if(intent.extras?.containsKey("page_id")!!){
            page_id = intent.getIntExtra("page_id",1)
            cms = intent.getStringExtra("cms")!!
        }
        termsOfServiceAPI(/*loginModel?.data?.bearer_access_token,*/ cms, page_id)
    }
    fun termsOfServiceAPI(/*token: String?,*/
                          cms : String?,
                          page_id: Int?){

        if(NetworkUtils.isConnected()) {

            viewModel.termsOfService(/*token,*/ cms, page_id).observe(this, Observer {
                it?.let { resource ->
                    when (resource.status) {
                        Status.SUCCESS -> {
                            if (it.data?.status == true) {

                                termsOfServiceModel = it.data
                                setTosHeadernDescription(termsOfServiceModel)

                            } else {
                                Toast.makeText(
                                    this,
                                    it.data?.errormessage?.message,
                                    Toast.LENGTH_LONG
                                )
                                    .show()
                            }
                            CommonUtils.hideProgress()

                        }
                        Status.ERROR -> {
                            CommonUtils.hideProgress()
                            Toast.makeText(this, it.data?.errormessage?.message, Toast.LENGTH_LONG)
                                .show()
                        }
                        Status.LOADING -> {
                            CommonUtils.showProgress(this)
                        }
                    }
                }
            })
        }
    }

    private fun setTosHeadernDescription(termsOfServiceModel: TermsOfServiceModel) {
        if(termsOfServiceModel.data?.services!=null){
            for (service in termsOfServiceModel.data?.services){
                when(service.id.toString().trim()){
                    "1" -> {
                        binding.tvTosHeadingOne.text = service.title.toString()
                        binding.tvTosDescriptionOne.text = service.description.toString()
                        binding.root.tvTitle.setText(service.title.toString())
                    }
                    "2" -> {
                        binding.tvTosHeadingOne.text = service.title.toString()
                        binding.tvTosDescriptionOne.text = service.description.toString()
                        binding.root.tvTitle.setText(service.title.toString())
                    }

                }
            }

        }


    }
}