package com.goldbookapp.ui.activity

import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager.widget.ViewPager
import com.blankj.utilcode.util.NetworkUtils
import com.goldbookapp.MyApplication
import com.goldbookapp.R
import com.goldbookapp.api.ApiHelper
import com.goldbookapp.api.RetrofitBuilder
import com.goldbookapp.databinding.OnboardingActivityBinding
import com.goldbookapp.inapplock.managers.AppLock
import com.goldbookapp.inapplock.managers.LockManager
import com.goldbookapp.model.LoginModel
import com.goldbookapp.ui.activity.settings.AppLockActivityNew
import com.goldbookapp.ui.activity.auth.LoginActivity
import com.goldbookapp.ui.MainActivity
import com.goldbookapp.ui.activity.auth.SignupActivity
import com.goldbookapp.ui.activity.viewmodel.ViewModelFactory
import com.goldbookapp.ui.activity.viewmodel.WebLinksViewModel
import com.goldbookapp.ui.adapter.ViewPagerAdapter
import com.goldbookapp.utils.*
import com.goldbookapp.utils.CommonUtils.Companion.clickWithDebounce
import com.goldbookapp.utils.PreferenceHelper.get
import com.goldbookapp.utils.PreferenceHelper.set
import com.google.gson.Gson
import com.hamzasharuf.networkmonitor.ConnectivityStateHolder
import com.hamzasharuf.networkmonitor.Event
import com.hamzasharuf.networkmonitor.NetworkEvents
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator
import kotlinx.android.synthetic.main.onboarding_activity.*


class OnboardingActivity  : AppCompatActivity() {
    var isApiCallCompleted : Boolean = false
    lateinit var binding: OnboardingActivityBinding
    lateinit var itemDetailImagesUrls: ArrayList<String>
    private lateinit var viewModel: WebLinksViewModel
    private var shouldUpdateApp: Boolean = false
    lateinit var prefs: SharedPreferences
    lateinit var loginModel: LoginModel

    protected var mType = AppLock.UNLOCK_PIN
    private val REQUEST_CODE_ENABLE = 11

    lateinit var filter: IntentFilter
    private lateinit var lockManager: LockManager<AppLockActivityNew>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.onboarding_activity)
        //binding.loginModel = LoginModel()
        prefs = PreferenceHelper.defaultPrefs(this)
        setupViewModel()
        rlOnboardMain.visibility = View.GONE
        if(NetworkUtils.isConnected()){
            CommonUtils.showProgress(this@OnboardingActivity)
        }else{
            CommonUtils.showDialog(this@OnboardingActivity, getString(R.string.please_check_internet_msg))
        }
    }

    private fun setupUIandListner() {
        binding.btnSignin.clickWithDebounce {
            if (NetworkUtils.isConnected()) {
                startActivity(Intent(this, LoginActivity::class.java))
            }
        }

        binding.btnSignup.clickWithDebounce {
            if (NetworkUtils.isConnected()) {
                startActivity(
                    Intent(this, SignupActivity::class.java).putExtra(
                        "isfromOnBoard",
                        true
                    )
                )

            }

        }


        val dotsIndicator = findViewById<DotsIndicator>(R.id.dots_indicator)
        val viewPager = findViewById<ViewPager>(R.id.view_pager)
        itemDetailImagesUrls = arrayListOf()


        itemDetailImagesUrls.add(Uri.parse("file:///android_asset/dashboard_ss.png").toString())
        itemDetailImagesUrls.add(Uri.parse("file:///android_asset/invoice_ss.png").toString())
        itemDetailImagesUrls.add(Uri.parse("file:///android_asset/side_menu_ss.png").toString())
        itemDetailImagesUrls.add(Uri.parse("file:///android_asset/ratecut_ss.png").toString())

        val adapter = ViewPagerAdapter(itemDetailImagesUrls)
        viewPager.adapter = adapter
        dotsIndicator.setViewPager(viewPager)

        tvDescriptionTitle.setText(R.string.onboarding_title1)
        tvDescriptionBrief.setText(R.string.onboarding_desc1)

        viewPager?.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {

            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {

            }

            override fun onPageSelected(position: Int) {
                when (position) {
                    0 -> {
                        tvDescriptionTitle.setText(R.string.onboarding_title1)
                        tvDescriptionBrief.setText(R.string.onboarding_desc1)
                    }
                    1 -> {
                        tvDescriptionTitle.setText(R.string.onboarding_title2)
                        tvDescriptionBrief.setText(R.string.onboarding_desc2)
                    }
                    2 -> {
                        tvDescriptionTitle.setText(R.string.onboarding_title3)
                        tvDescriptionBrief.setText(R.string.onboarding_desc3)
                    }
                    3 -> {
                        tvDescriptionTitle.setText(R.string.onboarding_title4)
                        tvDescriptionBrief.setText(R.string.onboarding_desc4)
                    }
                }
            }

        })
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
            getWebLinksFromApi()
            prefs = PreferenceHelper.defaultPrefs(this)


            if (prefs.contains(Constants.PREF_LOGIN_DETAIL_KEY)) {
                loginModel = Gson().fromJson(
                    prefs[Constants.PREF_LOGIN_DETAIL_KEY, ""],
                    LoginModel::class.java
                ) //getter
                if(loginModel.data!!.company_info!!.id.equals(Constants.DemoCompanyId)) {
                    lockManager =  LockManager.getInstance() as LockManager<AppLockActivityNew>

                    try {

                        lockManager.disableAppLock()
                        (MyApplication.appContext).unregisterReceiver(ScreenOnOffReceiver.getInstance(MyApplication.appContext))


                    } catch (e: Exception) {
                        // Log.e("dash screen on/off", " unregister error "+e.toString())
                    }

                    startActivity(Intent(this, MainActivity::class.java).putExtra(
                        Constants.isFromDemoCompany,
                        true
                    ))
                }
                else{
                    val intent = Intent(this@OnboardingActivity, AppLockActivityNew::class.java)
                    if (prefs.contains(Constants.PASSWORD_PREFERENCE_KEY)) {
                        intent.putExtra(AppLock.EXTRA_TYPE, AppLock.UNLOCK_PIN)


                        filter = IntentFilter(Intent.ACTION_SCREEN_ON)
                        filter.addAction(Intent.ACTION_SCREEN_OFF)

                        val lockManager: LockManager<AppLockActivityNew> =
                            LockManager.getInstance() as LockManager<AppLockActivityNew>
                        lockManager.enableAppLock(MyApplication.appContext, AppLockActivityNew::class.java)
                        lockManager.getAppLock().logoId = R.mipmap.ic_launcher
                        AppLockActivityNew.isRunning = false

                        (MyApplication.appContext).registerReceiver(ScreenOnOffReceiver.getInstance(MyApplication.appContext),filter)

                        startActivityForResult(intent, REQUEST_CODE_ENABLE)
                    }
                    else{
                        startActivity(Intent(this, MainActivity::class.java))
                        CommonUtils.hideProgress()
                        finish()
                    }

                }
            }
            else{
                setupUIandListner()
                CommonUtils.hideProgress()
                rlOnboardMain.visibility = View.VISIBLE
            }

        }

        if (!ConnectivityStateHolder.isConnected) {
            // Network is not available
            CommonUtils.showDialog(this@OnboardingActivity, getString(R.string.please_check_internet_msg))

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_CODE_ENABLE -> {
                when(mType){
                    AppLock.UNLOCK_PIN ->{
                        startActivity(Intent(this, MainActivity::class.java))
                        CommonUtils.hideProgress()
                        finish()
                    }

                }

            }

        }
    }



    override fun onPause() {
        super.onPause()

    }

    override fun onDestroy() {
        super.onDestroy()
        CommonUtils.hideProgress()
    }

    private fun getWebLinksFromApi() {
        if(NetworkUtils.isConnected()) {
            CommonUtils.hideInternetDialog()
            viewModel.webLinks().observe(this, Observer {
                it?.let { resource ->
                    when (resource.status) {
                        Status.SUCCESS -> {
                            if (it.data?.status == true) {
                                /* Log.v("..wb..", "..Success...")*/
                                /* val  prefs = PreferenceHelper.defaultPrefs(this)*/
                                prefs[Constants.WebLinks] = Gson().toJson(it.data.data) //setter


                            } else {
                                /*Toast.makeText(
                                    this,
                                    it.data?.errormessage?.message,
                                    Toast.LENGTH_LONG
                                )
                                    .show()*/
                            }
                            //CommonUtils.hideProgress()

                        }
                        Status.ERROR -> {
                            //CommonUtils.hideProgress()
                            /*Toast.makeText(this, it.data?.errormessage?.message, Toast.LENGTH_LONG)
                                .show()*/
                        }
                        Status.LOADING -> {
                            //CommonUtils.showProgress(this)
                        }
                    }
                }
            })
        }else{
            /*  CommonUtils.showDialog(this, getString(R.string.please_check_internet_msg))*/
        }
    }

    private fun setupViewModel() {
        viewModel =
            ViewModelProvider(this, ViewModelFactory(ApiHelper(RetrofitBuilder.apiService))).get(
                WebLinksViewModel::class.java
            )
        binding.setLifecycleOwner(this)
    }
}