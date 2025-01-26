package com.goldbookapp.ui.activity.settings

import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.blankj.utilcode.util.NetworkUtils
import com.goldbookapp.R
import com.goldbookapp.api.ApiHelper
import com.goldbookapp.api.RetrofitBuilder
import com.goldbookapp.databinding.ActivityAboutusBinding
import com.goldbookapp.model.AboutUsModel
import com.goldbookapp.ui.activity.viewmodel.AboutUsViewModel
import com.goldbookapp.ui.activity.viewmodel.ViewModelFactory
import com.goldbookapp.utils.CommonUtils
import com.goldbookapp.utils.CommonUtils.Companion.clickWithDebounce
import com.goldbookapp.utils.Status
import com.hamzasharuf.networkmonitor.ConnectivityStateHolder
import com.hamzasharuf.networkmonitor.Event
import com.hamzasharuf.networkmonitor.NetworkEvents
import kotlinx.android.synthetic.main.toolbar_layout.*
import kotlinx.android.synthetic.main.toolbar_layout.view.*

class AboutUsActivity : AppCompatActivity() {
    private lateinit var viewModel: AboutUsViewModel
    lateinit var binding: ActivityAboutusBinding
    var currentAppVersion: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_aboutus)

        imgLeft.setImageResource(R.drawable.ic_back)

        tvTitle.setText(getString(R.string.about))


        imgLeft.clickWithDebounce {
            onBackPressed()
        }
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
    }

    private fun handleConnectivityChange() {
        if (ConnectivityStateHolder.isConnected) {
            // Network is available
            CommonUtils.hideInternetDialog()
            getAboutUsDataFromApi()
        }

        if (!ConnectivityStateHolder.isConnected) {
            // Network is not available
            CommonUtils.showDialog(this, getString(R.string.please_check_internet_msg))

        }
    }

    private fun setupUIandListner() {
        val packageManager: PackageManager = this.packageManager
        var packageInfo: PackageInfo? = null
        try {
            packageInfo = packageManager.getPackageInfo(packageName, 0)
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        currentAppVersion = packageInfo!!.versionName.toString()
        //dynamic data set


    }

    private fun getAboutUsDataFromApi() {
        if (NetworkUtils.isConnected()) {

            viewModel.aboutUs().observe(this, Observer {
                it?.let { resource ->
                    when (resource.status) {
                        Status.SUCCESS -> {
                            if (it.data?.status == true) {
                                setaboutUsData(it.data.data)

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

    private fun setaboutUsData(data: AboutUsModel.Data?) {
        binding.llRootAboutus.visibility = View.VISIBLE
        binding.root.tvTitle.setText(data!!.title)
        binding.aboutusTvAppVersion.setText("Version " + currentAppVersion)
        val desc: List<String> = data.description!!.split("|")
        binding.aboutusDesc1.setText(desc.get(0))
        binding.aboutusDesc2.setText(desc.get(1))
        // about_tv_website.setText(data.extra_description!!.web)
        binding.aboutTvEmail.setText(data.extra_description!!.email)
        binding.aboutTvPhone.setText(data.extra_description.phone)

        binding.aboutLlWebsite.clickWithDebounce {
            startActivity(
                Intent(this@AboutUsActivity, PrivacyPolicyActivity::class.java).putExtra(
                    "weburi",
                    data.extra_description.web
                ).putExtra(
                    "title", getString(
                        R.string.golbook
                    )
                )
            )
        }
        binding.aboutLlEmail.clickWithDebounce {
            composeEmail(arrayOf(data.extra_description.email), getAppName())
        }
        binding.aboutTvPhone.clickWithDebounce {
            dialContactPhone(data.extra_description.phone!!)
        }

    }

    //dial phone no
    private fun dialContactPhone(phoneNumber: String) {
        startActivity(
            Intent(
                Intent.ACTION_DIAL,
                Uri.fromParts("tel", phoneNumber, null)
            )
        )
    }

    // to get app name
    fun Context.getAppName(): String = applicationInfo.loadLabel(packageManager).toString()

    // to compose email
    fun composeEmail(
        addresses: Array<String?>?,
        subject: String?
    ) {
        val intent = Intent(Intent.ACTION_SENDTO)
        intent.data = Uri.parse("mailto:") // only email apps should handle this
        intent.putExtra(Intent.EXTRA_EMAIL, addresses)
        intent.putExtra(Intent.EXTRA_SUBJECT, subject)
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        }
    }

    private fun setupViewModel() {
        viewModel =
            ViewModelProvider(this, ViewModelFactory(ApiHelper(RetrofitBuilder.apiService))).get(
                AboutUsViewModel::class.java
            )

        binding.setLifecycleOwner(this)
    }
}