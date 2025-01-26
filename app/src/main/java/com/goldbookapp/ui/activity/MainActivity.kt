package com.goldbookapp.ui

import DrawerCompanyListAdapter
import android.app.Dialog
import android.content.*
import android.content.IntentSender.SendIntentException
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.Window
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.FragmentNavigator
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.aurelhubert.ahbottomnavigation.AHBottomNavigation
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem
import com.blankj.utilcode.util.NetworkUtils
import com.bumptech.glide.Glide
import com.goldbookapp.BuildConfig
import com.goldbookapp.MyApplication
import com.goldbookapp.R
import com.goldbookapp.api.ApiHelper
import com.goldbookapp.api.RetrofitBuilder
import com.goldbookapp.databinding.ActivityMainBinding
import com.goldbookapp.inapplock.managers.LockManager
import com.goldbookapp.model.LoginModel
import com.goldbookapp.model.ReviewAppModel
import com.goldbookapp.model.UserCompanyListModel
import com.goldbookapp.model.UserWiseRestrictionModel
import com.goldbookapp.ui.activity.auth.LoginActivity
import com.goldbookapp.ui.activity.branch.BranchDetailActivity
import com.goldbookapp.ui.activity.organization.OrganizationDetailActivity
import com.goldbookapp.ui.activity.settings.AppLockActivityNew
import com.goldbookapp.ui.activity.settings.SettingsActivity
import com.goldbookapp.ui.activity.user.ProfileActivity
import com.goldbookapp.ui.activity.viewmodel.ViewModelFactory
import com.goldbookapp.ui.ui.send.MainActivityViewModel
import com.goldbookapp.utils.*
import com.goldbookapp.utils.CommonUtils.Companion.clickWithDebounce
import com.goldbookapp.utils.CommonUtils.Companion.isValidClickPressed
import com.goldbookapp.utils.Constants.Companion.PASSWORD_PREFERENCE_KEY
import com.goldbookapp.utils.Constants.Companion.WebLinks
import com.goldbookapp.utils.PreferenceHelper.get
import com.goldbookapp.utils.PreferenceHelper.set
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.AppUpdateType.IMMEDIATE
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManager
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.gson.Gson
import com.hamzasharuf.networkmonitor.ConnectivityStateHolder
import com.hamzasharuf.networkmonitor.Event
import com.hamzasharuf.networkmonitor.NetworkEvents
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.view.*
import kotlinx.android.synthetic.main.appupdatedialog.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.nav_header_main.*
import kotlinx.android.synthetic.main.nav_header_main.view.*
import kotlinx.android.synthetic.main.review_dialog.*
import kotlinx.android.synthetic.main.toolbar_layout.*
import org.jsoup.Jsoup
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity(), View.OnClickListener {


    lateinit var selectedSubData: String
    var enableSettingFromDrawer: Boolean = false
    val ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE = 5469
    lateinit var prefs: SharedPreferences
    lateinit var onoffReceiver: BroadcastReceiver
    lateinit var receiverManager: ReceiverManager
    lateinit var filter: IntentFilter

    private lateinit var adapter: DrawerCompanyListAdapter
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var dashboardGoldrate: String
    lateinit var binding: ActivityMainBinding

    lateinit var viewModel: MainActivityViewModel

    lateinit var navController: NavController

    lateinit var loginModel: LoginModel
    lateinit var reviewAppModel: ReviewAppModel

    var isClicked: Boolean = false

    var currentAppVersion: String = ""
    var appVersioninApi: String = ""

    lateinit var dialog: Dialog
    lateinit var dialog1: Dialog

    var companyList: List<UserCompanyListModel.Company968753762>? = null

    var companyNameList: List<String>? = null

    var selectedCompanyID: String? = null
    var selectedBranchID: String? = null
    var isFromDemoCompany: Boolean = false;
    var selectedBranchName: String? = null
    var selectedCompanyName: String? = null

    lateinit var companyNameAdapter: ArrayAdapter<String>

    var doubleBackToExitPressedOnce: Boolean = false
    var MY_REQUEST_CODE = 12

    var reviewInfo: ReviewInfo? = null
    var reviewManager: ReviewManager? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // below line of code to disable screen shot
        //window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        setupViewModel()
        //viewModel = ViewModelProvider(this).get(MainActivityViewModel::class.java)
        //val toolbar: Toolbar = findViewById(R.id.toolbar)
        //setSupportActionBar(toolbar)


        imgLeft?.setImageResource(R.drawable.ic_menu_black_24dp)
        // tvTitle?.setText("ABC Jewellers")
        val fab: FloatingActionButton = findViewById(R.id.fab)
        fab.setOnClickListener { view ->
            if (isValidClickPressed()) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
            }
        }
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        /*appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow,
                R.id.nav_tools, R.id.nav_share, R.id.nav_send
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)*/
        navView.setupWithNavController(navController)

        /*linearDrawerDashboard?.setOnClickListener { view -> navController.navigate(R.id.nav_gallery) }*/

        observeViewModel()
        initListeners()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == MY_REQUEST_CODE) {
            if (resultCode != RESULT_OK) {
                // Log.e("MY_APP", "Update flow failed! Result code: $resultCode")
                // If the update is cancelled or fails,
                // you can request to start the update again.
            } else {

            }
        }

    }

    /* override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
         super.onActivityResult(requestCode, resultCode, data)
         if (requestCode == ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE) {
             if (if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                     !Settings.canDrawOverlays(this)
                 } else {
                     TODO("VERSION.SDK_INT < M")
                 }
             ) {
                 // You don't have permission
                 checkPermission()
             } else {
                 // Do as per your logic
                     onBackPressed()
                 Toast.makeText(this@MainActivity,"System overlay permission granted",Toast.LENGTH_SHORT).show()
             }
         }
     }*/


    private fun setupViewModel() {
        viewModel =
            ViewModelProvider(this, ViewModelFactory(ApiHelper(RetrofitBuilder.apiService))).get(
                MainActivityViewModel::class.java
            )
        binding.setLifecycleOwner(this)
        //binding.pr = viewModel

        //viewModel.profileDetail.observe(this, Observer {  })
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
            //InAppReview()
            when (getCurrentFragment()) {
                resources.getString(R.string.reportfragfullname) -> {
                    CommonUtils.hideInternetDialog()
                    // navController.navigate(R.id.nav_reports)
                }
                resources.getString(R.string.customerfragfullname) -> {
                    CommonUtils.hideInternetDialog()
                    // loadSelectedFragment(1)

                }
                resources.getString(R.string.openingStockfragfullname) -> {
                    CommonUtils.hideInternetDialog()
                    //  navController.navigate(R.id.nav_openingstock)

                }

                resources.getString(R.string.supplierfragfullname) -> {
                    CommonUtils.hideInternetDialog()
                    //  loadSelectedFragment(2)

                }
                resources.getString(R.string.itemfragfullname) -> {
                    CommonUtils.hideInternetDialog()
                    // loadSelectedFragment(3)

                }
                resources.getString(R.string.salesfragfullname) -> {
                    CommonUtils.hideInternetDialog()
                    // loadSelectedFragment(4)

                }
                resources.getString(R.string.purchasefragfullname) -> {
                    CommonUtils.hideInternetDialog()
                    //   navController?.navigate(R.id.nav_purchase)


                }
                resources.getString(R.string.paymentfragfullname) -> {
                    CommonUtils.hideInternetDialog()
                    //  navController?.navigate(R.id.nav_payment)

                }
                resources.getString(R.string.receiptfragfullname) -> {
                    CommonUtils.hideInternetDialog()
                    //  bottom_navigation.setCurrentItem(3)

                }
                resources.getString(R.string.dashboardfragfullname) -> {
                    //Toast.makeText(this,getCurrentFragment(),Toast.LENGTH_SHORT).show()
                    CommonUtils.hideInternetDialog()
                    if (loginModel.data!!.company_info!!.id.equals(Constants.DemoCompanyId)) {
                        linearDrawerSettings.visibility = View.GONE
                    } else {
                        linearDrawerSettings.visibility = View.VISIBLE
                    }
                    // userwise restric api call (for applying user restriction)
                    /* when(loginModel.data!!.user_info!!.user_type.equals("user",true)){
                         // user type user
                         true -> {
                             // apply restriciton
                             defaultDisableAllButtonnUI()
                             userWiseRestriction(loginModel.data?.bearer_access_token)
                         }
                         // user_type -> admin or super_admin or any other
                         false -> {
                             defaultEnableAllButtonnUI()
                             when(enableSettingFromDrawer){
                                 true -> {
                                     linearDrawerSettings.visibility = View.VISIBLE
                                 }
                                 false -> {
                                     linearDrawerSettings.visibility = View.GONE
                                 }
                             }
                         }
                     }
 */

                    profileDetailAPI(loginModel?.data?.bearer_access_token)
                    if (intent.extras == null) {
                        getCompanyList("2")
                        getUpdateSessionDetails(loginModel?.data?.bearer_access_token)
                        //getAppVersion()

                        // IsUpdateAvailable()
                    }
                    if (!prefs.contains(WebLinks)) {
                        getWebLinksFromApi()
                    }

                }
                resources.getString(R.string.ledgerfragfullname) -> {
                    getUpdateSessionDetails(loginModel?.data?.bearer_access_token)
                }
            }
            val appUpdateManager = AppUpdateManagerFactory.create(this@MainActivity)
            appUpdateManager
                .appUpdateInfo
                .addOnSuccessListener { appUpdateInfo ->

                    if (appUpdateInfo.updateAvailability()
                        == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS
                    ) {
                        // If an in-app update is already running, resume the update.
                        appUpdateManager.startUpdateFlowForResult(
                            appUpdateInfo,
                            IMMEDIATE,
                            this,
                            12
                        );
                    }
                }
        }

        if (!ConnectivityStateHolder.isConnected) {
            // Network is not available
            CommonUtils.showDialog(this, getString(R.string.please_check_internet_msg))

        }
    }

    private fun InAppReview() {
        reviewManager = ReviewManagerFactory.create(this)
        val request = reviewManager?.requestReviewFlow()
        request?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // We got the ReviewInfo object
                reviewInfo = task.result
                Log.d("review",""+reviewInfo)
            } else {
                // There was some problem, log or handle the error code.
                //@ReviewErrorCode val reviewErrorCode = (task.getException() as TaskException).errorCode
            }
        }
    }


    private fun updateDateForReview() {

        val sdf = SimpleDateFormat("yyyy-MM-dd")
        val date = Date()
        val today_date = sdf.format(date)

        val c = Calendar.getInstance()
        //firstDateOf Month
        c.set(Calendar.DAY_OF_MONTH, 1)
        //c.set(Calendar.DAY_OF_MONTH, 27)
        val firstDateOfMonth = sdf.format(c.time)
        Log.v(
            "first_date", "" +
                    firstDateOfMonth
        )

        c.set(Calendar.DAY_OF_MONTH, 15)
        val fifteenDateofMonth = sdf.format(c.time)
        Log.v("fifteen_date", "" + fifteenDateofMonth)

        if (today_date.equals(firstDateOfMonth)) {
            Log.v("datereview", "true")
            //for the first time show
            if (!prefs.contains(Constants.ReviewApp)) {
                //if review is not given then show dialog
                if (reviewInfo != null) {
                   // openReviewDialog()
                    InAppReview()
                    startReviewFlow()
               }
            }
        } else if (today_date.equals(fifteenDateofMonth)) {
            if (!prefs.contains(Constants.ReviewApp)) {
                if (reviewInfo != null) {
                    //openReviewDialog()
                    InAppReview()
                    startReviewFlow()
                }
            }

        } else {

        }
    }

    private fun openReviewDialog() {

        /* FancyAlertDialog.Builder
             .with(this)
             .setTitle("Enjoying GoldBook?")
             .setBackgroundColor(Color.parseColor("#fdba00")) // for @ColorRes use setBackgroundColorRes(R.color.colorvalue)
             .setMessage("If you like using GoldBook, please take a moment to rate it. Thanks for your support!")
             .setNegativeBtnText("Later")
             .setPositiveBtnBackground(Color.parseColor("#FF4081")) // for @ColorRes use setPositiveBtnBackgroundRes(R.color.colorvalue)
             .setPositiveBtnText("Rate")
             .setNegativeBtnBackground(Color.parseColor("#FFA9A7A8")) // for @ColorRes use setNegativeBtnBackgroundRes(R.color.colorvalue)
             .setAnimation(Animation.POP)
             .isCancellable(true)
             .setIcon(R.mipmap.ic_launcher, VISIBLE)
             .onPositiveClicked { dialog: Dialog? ->

             }
             .onNegativeClicked { dialog: Dialog? ->

             }
             .build()
             .show()
        */

        dialog1 = Dialog(this, R.style.Full_Dialog)
        dialog1.requestWindowFeature(Window.FEATURE_NO_TITLE)
        this.window.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        );

        dialog1.setCancelable(false)
        dialog1.setCanceledOnTouchOutside(false)
        dialog1.setContentView(R.layout.review_dialog)
        dialog1.tvLater.clickWithDebounce {
            dialog1.dismiss()
        }
        dialog1.tvNothanks.clickWithDebounce {
            dialog1.dismiss()
        }
        dialog1.tvRate.clickWithDebounce {
            InAppReview()
            startReviewFlow()
            dialog1.dismiss()
        }
        dialog1.show()
        val childModel = ReviewAppModel("true")
        prefs[Constants.ReviewApp] = Gson().toJson(childModel)

    }


    private fun startReviewFlow() {
        // if (reviewInfo != null) {
        val flow = reviewManager!!.launchReviewFlow(this, reviewInfo!!)
        flow.addOnCompleteListener {
            /* Toast.makeText(
                 applicationContext,
                 "Rating complete",
                 Toast.LENGTH_LONG
             ).show()*/
        }
        /*  } else {
         Toast.makeText(this, "Rating failed", Toast.LENGTH_LONG).show()
     }*/
    }


    private fun defaultEnableAllButtonnUI() {
        linearDrawerCustomer.visibility = View.VISIBLE
        bottom_navigation.enableItemAtPosition(1)

        linearDrawerSupplier.visibility = View.VISIBLE
        linearDrawerItems.visibility = View.VISIBLE
        linearDrawerSales.visibility = View.VISIBLE
        bottom_navigation.enableItemAtPosition(2)
        linearDrawerPurchase.visibility = View.VISIBLE
        linearDrawerReceipt.visibility = View.VISIBLE
        bottom_navigation.enableItemAtPosition(3)
        linearDrawerPayment.visibility = View.VISIBLE
        linearDrawerLedger.visibility = View.VISIBLE
        linearDrawerGroups.visibility = View.VISIBLE
        linearDrawerReports.visibility = View.VISIBLE
        linearDrawerSettings.visibility = View.VISIBLE
        imgDrawerProfile.isEnabled = true
        enableSettingFromDrawer = true

    }

    private fun defaultDisableAllButtonnUI() {
        linearDrawerCustomer.visibility = View.GONE
        bottom_navigation.disableItemAtPosition(1)

        linearDrawerSupplier.visibility = View.GONE
        linearDrawerItems.visibility = View.GONE
        linearDrawerSales.visibility = View.GONE
        bottom_navigation.disableItemAtPosition(2)
        linearDrawerPurchase.visibility = View.GONE
        linearDrawerReceipt.visibility = View.GONE
        bottom_navigation.disableItemAtPosition(3)
        linearDrawerPayment.visibility = View.GONE
        linearDrawerLedger.visibility = View.GONE
        linearDrawerGroups.visibility = View.GONE
        linearDrawerReports.visibility = View.GONE
        linearDrawerSettings.visibility = View.GONE
        //imgDrawerProfile.isEnabled = false
        enableSettingFromDrawer = false
        // setting report pending
    }


    fun forceUpdate() {
        val packageManager: PackageManager = this.packageManager
        var packageInfo: PackageInfo? = null
        try {
            packageInfo = packageManager.getPackageInfo(packageName, 0)
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }

        currentAppVersion = packageInfo!!.versionName.toString()
        when (appVersioninApi.equals(currentAppVersion, ignoreCase = true)) {
            true -> {
                // enjoy app
            }
            false -> {
                // block app
                prefs = PreferenceHelper.defaultPrefs(this)
                //prefs.edit().remove(Constants.PREF_LOGIN_DETAIL_KEY).apply()
                //prefs.edit().clear().apply()
                CommonUtils.clearAllAppPrefs(prefs)
                //logout()
                openUpdateDialog()
                //startActivity(Intent(this,LoginActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))

            }
        }

    }

    override fun onPause() {
        super.onPause()
        if (this::dialog.isInitialized)
            dialog.dismiss()
        CommonUtils.hideInternetDialog()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (this::dialog.isInitialized)
            dialog.dismiss()
    }

    private fun playstoreVerisonChecker(): String {
        var newVersion: String = ""
        try {
            val document =
                Jsoup.connect("https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID + "&hl=en")
                    .timeout(30000)
                    .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                    .referrer("http://www.google.com")
                    .get()

            if (document != null) {
                val element = document.getElementsContainingOwnText("Current Version")
                for (ele in element) {
                    if (ele.siblingElements() != null) {
                        val sibElemets = ele.siblingElements()
                        for (sibElemet in sibElemets) {
                            newVersion = sibElemet.text()
                        }
                    }
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return newVersion
    }

    private fun openUpdateDialog() {

        dialog = Dialog(this, R.style.Full_Dialog)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        this.window.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        );

        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setContentView(R.layout.appupdatedialog)
        dialog.appupdateCancel.visibility = View.GONE
        dialog.appupdateCancelCloseApp.setText("Update")
        dialog.appupdateCancel.clickWithDebounce {
            dialog.dismiss()
        }
        dialog.appupdateCancelCloseApp.clickWithDebounce {

            /*finishAffinity()*/
            try {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("market://details?id=$packageName")
                    )
                )
            } catch (e: ActivityNotFoundException) {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
                    )
                )
            }
            finish()
        }

        dialog.show()

    }

    private fun getCurrentFragment(): String {
        val currentNavHost = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
        val currentFragmentClassName =
            (navController.currentDestination as FragmentNavigator.Destination).className
        return currentFragmentClassName
//        return currentNavHost?.childFragmentManager?.fragments?.filterNotNull()?.find {
//            it.javaClass.name == currentFragmentClassName
//        }
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    private fun observeViewModel() {

        viewModel.badgeCount.observe(this, Observer {
            showToast(it)
        })

    }

    private fun showToast(value: Int) {
        Toast.makeText(this, value.toString(), Toast.LENGTH_LONG).show()
    }

    fun checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
                startActivityForResult(intent, ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE)
            }
        }
    }


    private fun initListeners() {
        /*val prefs = PreferenceHelper.defaultPrefs(this)
        loginModel = Gson().fromJson(
            prefs[Constants.PREF_LOGIN_DETAIL_KEY, ""],
            LoginModel::class.java
        ) //getter*/

        getloginDetailsFromPref()
        // checkPermission()
        InAppReview()
        removePrefForReview()

        imgDrawerProfile?.clickWithDebounce {

            viewModel.incrementBadgeCount()
        }
        //default company name set
        txtDrawerCompanyName.text = loginModel.data?.company_info?.company_name
        txtDrawerCompanyName.isSelected = true
        //get company list for Logged in User.
        //getCompanyList()

        adapter =
            DrawerCompanyListAdapter(
                arrayListOf(),
                txtDrawerCompanyName.text.toString()
            )
        binding.root.rvCompanyListDrawer.adapter = adapter
        llDrawerCompanyName.clickWithDebounce {

            if (NetworkUtils.isConnected()) {
                if (!isClicked) {
                    if (isValidClickPressed()) {
                        getCompanyList("2")
                        binding.root.rvCompanyListDrawer.visibility = View.VISIBLE
                        binding.root.lldrawerItemsRoot.visibility = View.GONE
                        binding.root.expandBtn.rotation = 180f
                        isClicked = true
                    }
                } else {
                    binding.root.rvCompanyListDrawer.visibility = View.GONE
                    binding.root.lldrawerItemsRoot.visibility = View.VISIBLE
                    binding.root.expandBtn.rotation = 0f
                    isClicked = false
                }
            } else {
                /*CommonUtils.showDialog(this, getString(R.string.please_check_internet_msg))*/
            }


        }

        imgDrawerProfile?.clickWithDebounce {

            if (NetworkUtils.isConnected()) {
                startActivity(Intent(this, ProfileActivity::class.java))
            } else {
                /*CommonUtils.showDialog(this, getString(R.string.please_check_internet_msg))*/
            }
        }

        linearDrawerSettings?.clickWithDebounce {

            if (NetworkUtils.isConnected()) {
                startActivity(Intent(this, SettingsActivity::class.java))
            }
        }

        viewModel.profileDetail.observe(this, Observer { profileModel ->

            Glide.with(this).load(profileModel.data?.user?.imageurl).circleCrop()
                .placeholder(R.drawable.ic_user_placeholder).into(imgDrawerProfile)
            tvDrawerUserName.text = profileModel.data?.user?.name


        })
        if (intent.extras != null && intent.extras!!.containsKey("branch_id")) {
            selectedBranchID = intent.getStringExtra("branch_id")
            selectedBranchName = intent.getStringExtra("branch_name")
            if (isValidClickPressed())
                getCompanyList("3")
        } else if (intent.extras != null && intent.extras!!.containsKey("company_id")) {
            selectedCompanyID = intent.getStringExtra("company_id")
            selectedCompanyName = intent.getStringExtra("company_name")
            if (isValidClickPressed())
                getCompanyList("1")


        }
        if (intent.extras != null && intent.extras!!.containsKey(Constants.isFromDemoCompany)) {
            CommonUtils.hideInternetDialog()
            isFromDemoCompany = intent.extras!!.getBoolean(Constants.isFromDemoCompany)
        }
        filter = IntentFilter(Intent.ACTION_SCREEN_ON)
        filter.addAction(Intent.ACTION_SCREEN_OFF)

        linearDrawerCurrentCompanyDetails?.setOnClickListener(this)
        linearDrawerCurrentBranchDetails?.setOnClickListener(this)
        linearDrawerDashboard?.setOnClickListener(this)
        linearDrawerCustomer?.setOnClickListener(this)
        linearDrawerSupplier?.setOnClickListener(this)
        linearDrawerItems?.setOnClickListener(this)
        linearDrawerSales?.setOnClickListener(this)
        linearDrawerPurchase?.setOnClickListener(this)
        linearDrawerReceipt?.setOnClickListener(this)
        linearDrawerPayment?.setOnClickListener(this)
        linearDrawerReports?.setOnClickListener(this)
        linearDrawerLedger?.setOnClickListener(this)
        linearDrawerGroups?.setOnClickListener(this)
        linearDrawerOpeningStock?.setOnClickListener(this)
        btnBackToLogin?.setOnClickListener(this)

        // Create items
        val item1 = AHBottomNavigationItem(
            R.string.dashboard,
            R.drawable.ic_bn_dashboard,
            android.R.color.black
        )
        val item2 = AHBottomNavigationItem(
            R.string.customers,
            R.drawable.ic_bn_customers,
            R.color.blackcolor
        )
        val item3 = AHBottomNavigationItem(
            R.string.sale,
            R.drawable.ic_bn_sales,
            R.color.blackcolor
        )
        val item4 =
            AHBottomNavigationItem(R.string.receipt, R.drawable.ic_bn_receipt, R.color.blackcolor)

        // Add items
        bottom_navigation.addItem(item1)
        bottom_navigation.addItem(item2)
        bottom_navigation.addItem(item3)
        bottom_navigation.addItem(item4)

        bottom_navigation.setAccentColor(ContextCompat.getColor(this, R.color.colorPrimary));
        bottom_navigation.setInactiveColor(ContextCompat.getColor(this, android.R.color.black));
        bottom_navigation.setTitleState(AHBottomNavigation.TitleState.ALWAYS_SHOW);
        //bottom_navigation.setColored(true);

        var typeFace: Typeface? =
            ResourcesCompat.getFont(this.applicationContext, R.font.proxima_nova_bold)
        bottom_navigation.setTitleTypeface(typeFace)
        bottom_navigation.setTitleTextSize(
            resources.getDimension(R.dimen._10sdp),
            resources.getDimension(R.dimen._10sdp)
        )
        if (isFromDemoCompany) {
            getCompanyList("2")
            getUpdateSessionDetails(loginModel?.data?.bearer_access_token)
            //getAppVersion()
            IsUpdateAvailable()
        } else {
            // bottom_navigation.setCurrentItem(0, true)
        }


        // Set listeners
        bottom_navigation.setOnTabSelectedListener() { position: Int, wasSelected: Boolean ->
            when (position) {
                0 -> navController?.navigate(R.id.nav_dashboard)
                1 -> navController?.navigate(R.id.nav_customers)
                2 -> navController?.navigate(R.id.nav_sale)
                3 -> navController?.navigate(R.id.nav_receipt)
            }
            bottom_navigation.visibility = VISIBLE
            true
        }


    }

    private fun removePrefForReview() {
        val sdf = SimpleDateFormat("yyyy-MM-dd")
        val date = Date()
        val today_date = sdf.format(date)

        val c = Calendar.getInstance()
        //firstDateOf Month
        c.set(Calendar.DAY_OF_MONTH, 2)
        val secondDateOfMonth = sdf.format(c.time)
        Log.v("first_date", "" + secondDateOfMonth)

        c.set(Calendar.DAY_OF_MONTH, 16)
        val sixteenDateofMonth = sdf.format(c.time)
        Log.v("fifteen_date", "" + sixteenDateofMonth)

        if (today_date.equals(secondDateOfMonth)) {
            Log.v("datereview", "true")
            prefs.edit().remove(Constants.ReviewApp).apply()

        } else if (today_date.equals(sixteenDateofMonth)) {
            prefs.edit().remove(Constants.ReviewApp).apply()

        } else {

        }
    }


    private fun getAppVersion() {
        if (NetworkUtils.isConnected()) {
            viewModel.getAppVersion()
                .observe(this, Observer {
                    it?.let { resource ->
                        when (resource.status) {
                            Status.SUCCESS -> {
                                /*Log.v("..setupObservers..", "..Success...")*/
                                if (it.data?.status == true) {
                                    appVersioninApi = it.data.data!!.app_version!!
                                    //Toast.makeText(this,appVersioninApi,Toast.LENGTH_SHORT).show()
                                    forceUpdate()
                                } else {
                                    Toast.makeText(
                                        this,
                                        it.data?.errormessage?.message,
                                        Toast.LENGTH_LONG
                                    )
                                        .show()
                                }


                            }
                            Status.ERROR -> {
                                /* Toast.makeText(
                                    this,
                                    it.data?.errormessage?.message,
                                    Toast.LENGTH_LONG
                                )
                                    .show()*/
                                /*Log.v("..setupObservers..", "..ERROR...")*/
                            }
                            Status.LOADING -> {
                                /* Log.v("..setupObservers..", "..LOADING...")*/
                            }
                        }
                    }
                })
        } else {
            /*CommonUtils.showDialog(this, getString(R.string.please_check_internet_msg))*/
        }
    }


    private fun applyUserWiseRestriction(data: UserWiseRestrictionModel.Data) {
        for (i in 0 until data.permission!!.size) {
            if (data.permission!!.get(i).startsWith(getString(R.string.customers))) {
                // Restriction check for Customerl
                when (data.permission!!.get(i).endsWith(getString(R.string.list), true)) {
                    true -> {
                        linearDrawerCustomer.visibility = View.VISIBLE
                        bottom_navigation.enableItemAtPosition(1)
                        //   Log.v("..customerlistcheck..", "true")
                    }
                    else -> {

                    }
                }
            }
            if (data.permission!!.get(i).startsWith(getString(R.string.supp))) {
                // Restriction check for Supplier
                when (data.permission!!.get(i).endsWith(getString(R.string.list), true)) {
                    true -> {
                        linearDrawerSupplier.visibility = View.VISIBLE
                        //  Log.v("..supplistcheck..", "true")
                    }
                    else -> {

                    }
                }

            }
            if (data.permission!!.get(i).startsWith(getString(R.string.items))) {
                // Restriction check for Supplier
                when (data.permission!!.get(i).endsWith(getString(R.string.list), true)) {
                    true -> {
                        linearDrawerItems.visibility = View.VISIBLE
                        //  Log.v("..itemslistcheck..", "true")
                    }
                    else -> {

                    }
                }

            }
            if (data.permission!!.get(i).startsWith(getString(R.string.sales))) {
                // Restriction check for Supplier
                when (data.permission!!.get(i).endsWith(getString(R.string.list), true)) {
                    true -> {
                        linearDrawerSales.visibility = View.VISIBLE
                        bottom_navigation.enableItemAtPosition(2)
                        //  Log.v("..saleslistcheck..", "true")
                    }
                    else -> {

                    }
                }

            }
            if (data.permission!!.get(i).startsWith(getString(R.string.purchase))) {
                // Restriction check for Purchase
                when (data.permission!!.get(i).endsWith(getString(R.string.list), true)) {
                    true -> {
                        linearDrawerPurchase.visibility = View.VISIBLE
                        //  Log.v("..purchaselistcheck..", "true")

                    }
                    else -> {

                    }
                }

            }
            if (data.permission!!.get(i).startsWith(getString(R.string.receipt))) {
                // Restriction check for Purchase
                when (data.permission!!.get(i).endsWith(getString(R.string.list), true)) {
                    true -> {
                        linearDrawerReceipt.visibility = View.VISIBLE
                        bottom_navigation.enableItemAtPosition(3)
                        // Log.v("..receiptlistcheck..", "true")
                    }
                    else -> {

                    }
                }

            }
            if (data.permission!!.get(i).startsWith(getString(R.string.payment))) {
                // Restriction check for Purchase
                when (data.permission!!.get(i).endsWith(getString(R.string.list), true)) {
                    true -> {
                        linearDrawerPayment.visibility = View.VISIBLE
                        //   Log.v("..paymentlistcheck..", "true")
                    }
                    else -> {

                    }
                }
            }
            if (data.permission!!.get(i).startsWith(getString(R.string.ledgr))) {
                // Restriction check for Purchase
                when (data.permission!!.get(i).endsWith(getString(R.string.list), true)) {
                    true -> {
                        linearDrawerLedger.visibility = View.VISIBLE
                        //  Log.v("..ledgerlistcheck..", "true")
                    }
                    else -> {

                    }
                }
            }
            if (data.permission!!.get(i).startsWith(getString(R.string.ledger_Group))) {
                // Restriction check for Purchase
                when (data.permission!!.get(i).endsWith(getString(R.string.list), true)) {
                    true -> {
                        linearDrawerGroups.visibility = View.VISIBLE
                        // Log.v("..lgrouplistcheck..", "true")
                    }
                    else -> {

                    }
                }

            }
            if (data.permission!!.get(i).startsWith(getString(R.string.setting), true)) {
                // Restriction check for Purchase
                when (data.permission!!.get(i)
                    .startsWith(getString(R.string.setting_preference), true)) {
                    true -> {
                        enableSettingFromDrawer = true
                    }
                    else -> {

                    }
                }
                when (data.permission!!.get(i)
                    .startsWith(getString(R.string.setting_taxes), true)) {
                    true -> {
                        enableSettingFromDrawer = true
                    }
                    else -> {

                    }
                }
                when (data.permission!!.get(i)
                    .startsWith(getString(R.string.setting_contacts), true)) {
                    true -> {
                        enableSettingFromDrawer = true
                    }
                    else -> {

                    }
                }
                when (data.permission!!.get(i)
                    .startsWith(getString(R.string.setting_items), true)) {
                    true -> {
                        enableSettingFromDrawer = true
                    }
                    else -> {

                    }
                }
                when (data.permission!!.get(i).startsWith(getString(R.string.org_list), true)) {
                    true -> {
                        enableSettingFromDrawer = true
                    }
                    else -> {

                    }
                }
                when (data.permission!!.get(i).startsWith(getString(R.string.branch_list), true)) {
                    true -> {
                        enableSettingFromDrawer = true
                    }
                    else -> {

                    }
                }


                when (enableSettingFromDrawer) {
                    true -> {
                        //   Log.v("..Settings..", enableSettingFromDrawer.toString())
                        linearDrawerSettings.visibility = View.VISIBLE
                    }
                    false -> {
                        //   Log.v("..Settings..", enableSettingFromDrawer.toString())
                        linearDrawerSettings.visibility = View.GONE
                    }
                }
                // linearDrawerSettings.visibility = View.VISIBLE


            }

            if (data.permission!!.get(i).contains(getString(R.string.reprt), true)) {
                // Restriction check for Purchase
                linearDrawerReports.visibility = View.VISIBLE
                // Log.v("..report..", "true")

            }


            /* if (data.permission!!.get(i).startsWith(getString(R.string.profile))) {
                 // Restriction check for Purchase
                 when (data.permission!!.get(i).endsWith(getString(R.string.edit_view), true)) {
                     true -> {
                         imgDrawerProfile.isEnabled = true
                         Log.v("..lgrouplistcheck..", "true")
                     }
                 }

             }*/

        }
    }

    private fun IsUpdateAvailable() {
        if (NetworkUtils.isConnected()) {
            /*Thread(Runnable {
                var newversion = "no"
                var newversiondot = "no"
                try {

                    newversiondot = playstoreVerisonChecker()
                    Log.v("newversiondot",newversiondot)
                    if(newversiondot.isNotEmpty()){
                       *//* Jsoup.connect("https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID + "&hl=en")
                            .timeout(30000)
                            .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                            .referrer("http://www.google.com")
                            .get()
                            .select("div.hAyfc:nth-child(4) > span:nth-child(2) > div:nth-child(1) > span:nth-child(1)")
                            .first()
                            .ownText()*//*
                    newversion = newversiondot.replace("[^0-9]".toRegex(), "")
                    Log.v("newversion",newversion)
                    val finalNewversion = newversion
                    val finalNewversiondot = newversiondot

                        runOnUiThread {
                            try {
                                if (finalNewversion.toInt() > applicationContext.packageManager
                                        .getPackageInfo(
                                            BuildConfig.APPLICATION_ID,
                                            0
                                        ).versionName.replace("[^0-9]".toRegex(), "").toInt()
                                ) {
                                    openUpdateDialog()

                                }
                            } catch (e: PackageManager.NameNotFoundException) {
                                e.printStackTrace()
                            }
                        }
                    }
                } catch (e: IOException) {
                    //Log.d("TAG NEW", "run: $e")
                }

            }).start()*/
            val appUpdateManager = AppUpdateManagerFactory.create(this@MainActivity)

// Returns an intent object that you use to check for an update.
            val appUpdateInfoTask = appUpdateManager.appUpdateInfo

// Checks that the platform will allow the specified type of update.
            appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
                if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                    // This example applies an immediate update. To apply a flexible update
                    // instead, pass in AppUpdateType.FLEXIBLE
                    && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
                ) {
                    // Request the update.
                    //  openUpdateDialog()

                    try {
                        appUpdateManager.startUpdateFlowForResult(
                            appUpdateInfo,
                            AppUpdateType.IMMEDIATE /*AppUpdateType.IMMEDIATE*/,
                            this@MainActivity, MY_REQUEST_CODE
                        )
                    } catch (e: SendIntentException) {
                        e.printStackTrace()
                    }
                }
            }


        } else {
            /*CommonUtils.showDialog(this, getString(R.string.please_check_internet_msg))*/
        }

    }

    private fun clearPasscodePrefs(): Boolean {

        if (this::prefs.isInitialized) {
            try {
                prefs.edit().remove(Constants.PASSWORD_PREFERENCE_KEY).apply()
                prefs.edit().remove("ALGORITHM").apply()
                prefs.edit().remove("LOGO_ID_PREFERENCE_KEY").apply()
                prefs.edit().remove("LAST_ACTIVE_MILLIS").apply()
                prefs.edit().remove("PIN_CHALLENGE_CANCELLED_PREFERENCE_KEY").apply()
                return true
            } catch (e: Exception) {
                return false
                // Log.e("passcode pref exception", e.toString())
            }


        }
        return true
    }

    private fun logout() {
        if (NetworkUtils.isConnected()) {
            viewModel.logout(loginModel?.data?.bearer_access_token)
                .observe(this, Observer {
                    it?.let { resource ->
                        when (resource.status) {
                            Status.SUCCESS -> {
                                /* Log.v("..setupObservers..", "..Success...")*/
                                if (it.data?.status == true) {
                                    Toast.makeText(
                                        this,
                                        it.data?.message,
                                        Toast.LENGTH_LONG
                                    )
                                        .show()
                                    //openUpdateDialog()
                                    try {
                                        receiverManager = ReceiverManager.init(this)
                                        onoffReceiver = ScreenOnOffReceiver.getInstance(this)
                                        /* if(receiverManager.isReceiverRegistered(onoffReceiver)){*/
                                        //  Log.v("Screen on/off", "receiver already registered")
                                        //receiverManager.unregisterReceiver(onoffReceiver)
                                        try {
                                            /* if(receiverManager.isReceiverRegistered(onoffReceiver)){*/
                                            //receiverManager.unregisterReceiver(onoffReceiver)
                                            // this.unregisterReceiver(onoffReceiver)
                                            (application as MyApplication).unregisterReceiver(
                                                ScreenOnOffReceiver.getInstance(
                                                    this
                                                )
                                            )

                                            /*}*/
                                        } catch (e: Exception) {
                                            //   Log.e("screen on/off", "unregister error"+e.toString())
                                        }
                                        /*}*/
                                    } catch (e: Exception) {
                                    }
                                    /*try {
                                        unregisterReceiver(ScreenOnOffReceiver())
                                    } catch (e: Exception) {
                                    }*/

                                    prefs = PreferenceHelper.defaultPrefs(this)
                                    CommonUtils.clearAllAppPrefs(prefs)
                                    prefs.edit().remove(Constants.PASSWORD_PREFERENCE_KEY).apply()
                                    if (clearPasscodePrefs())
                                        startActivity(
                                            Intent(this, LoginActivity::class.java).setFlags(
                                                Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_NEW_TASK
                                            )
                                        )
                                } else {
                                    /* Toast.makeText(
                                         this,
                                         it.data?.errormessage?.message,
                                         Toast.LENGTH_LONG
                                     )
                                         .show()*/
                                }
                                CommonUtils.hideProgress()

                            }
                            Status.ERROR -> {
                                /* Toast.makeText(
                                     this,
                                     it.data?.errormessage?.message,
                                     Toast.LENGTH_LONG
                                 )
                                     .show()*/
                                /* Log.v("..logout..", "..ERROR...")*/
                                CommonUtils.hideProgress()
                            }
                            Status.LOADING -> {
                                /* Log.v("..logout..", "..LOADING...")*/
                                CommonUtils.showProgress(this)
                            }
                        }
                    }
                })
        } else {
            /*  CommonUtils.showDialog(this, getString(R.string.please_check_internet_msg))*/
        }
    }

    private fun getCompanyList(isFromSettingOrgList: String) {
        companyList = ArrayList<UserCompanyListModel.Company968753762>()
        companyNameList = ArrayList<String>()
        if (NetworkUtils.isConnected()) {
            CommonUtils.hideInternetDialog()
            viewModel.userCompanyList(loginModel?.data?.bearer_access_token)
                .observe(this, Observer {
                    it?.let { resource ->
                        when (resource.status) {
                            Status.SUCCESS -> {
                                /*Log.v("..setupObservers..", "..Success...")*/
                                if (it.data?.status == true) {

                                    companyList = it.data.data?.company

                                    companyNameList =
                                        companyList?.map { it.company_name.toString() }
                                    //recyclerviewsetup for getting company list
                                    binding.root.rvCompanyListDrawer.layoutManager =
                                        LinearLayoutManager(this)
                                    /*adapter =
                                        DrawerCompanyListAdapter(
                                            companyList as ArrayList<UserCompanyListModel.Company968753762>,
                                            txtDrawerCompanyName.text.toString()
                                        )*/
                                    adapter.apply {
                                        addCompanies(
                                            companyList as ArrayList<UserCompanyListModel.Company968753762>?,
                                            isFromDemoCompany
                                        )
                                        notifyDataSetChanged()
                                    }


                                    when (isFromSettingOrgList) {
                                        "1" -> {
                                            for (companyName in this!!.companyNameList!!) {
                                                if (selectedCompanyName.equals(
                                                        companyName,
                                                        ignoreCase = true
                                                    )
                                                ) {
                                                    val pos = companyNameList?.indexOf(companyName)
                                                    if (pos != null) {
                                                        setSelectedCompanyName(pos)

                                                    }
                                                }
                                            }
                                        }
                                        "3" -> {
                                            setSelectedBranchName()
                                        }
                                        else -> {
                                        }
                                    }


//                            companyNameAdapter = ArrayAdapter<String>(
//                                this,
//                                android.R.layout.simple_dropdown_item_1line,
//                                companyNameList!!
//                            )
//                            txtDrawerCompanyName.setAdapter(companyNameAdapter)
//                            txtDrawerCompanyName.threshold = 1
//
//                            txtDrawerCompanyName.setOnItemClickListener {
//                                    adapterView, view, position, l
//                                -> val selected: String = adapterView.getItemAtPosition(position).toString()
//                                val pos: Int? = companyNameList?.indexOf(selected)
//
//                                selectedCompanyID = pos?.let { it1 -> companyList?.get(it1)?.company_id }
//
//                            }

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


                            }
                            Status.ERROR -> {
                                /* Toast.makeText(
                                    this,
                                    it.data?.errormessage?.message,
                                    Toast.LENGTH_LONG
                                )
                                    .show()*/
                                /* Log.v("..setupObservers..", "..ERROR...")*/
                            }
                            Status.LOADING -> {
                                /*Log.v("..setupObservers..", "..LOADING...")*/
                            }
                        }
                    }
                })
        } else {
            /*CommonUtils.showDialog(this, getString(R.string.please_check_internet_msg))*/
        }
    }

    override fun onClick(view: View?) {
//        bottom_navigation.visibility = GONE
        // to get updated data from login model after switching branch or company
        getloginDetailsFromPref()
        when (view) {

            linearDrawerCurrentBranchDetails -> {
                if (isValidClickPressed()) {
                    if (NetworkUtils.isConnected()) startActivity(
                        Intent(
                            this,
                            BranchDetailActivity::class.java
                        ).putExtra("branch_id", loginModel.data?.branch_info?.id)
                    )
                }
            }
            linearDrawerCurrentCompanyDetails -> {
                if (isValidClickPressed()) {
                    if (NetworkUtils.isConnected()) startActivity(
                        Intent(
                            this,
                            OrganizationDetailActivity::class.java
                        ).putExtra("company_id", loginModel.data?.company_info?.id)
                    )

                }
            }
            linearDrawerDashboard -> {
                if (isValidClickPressed()) {
                    delayLoad(bottom_navigation, 0, navController, -1)
                }

            }
            linearDrawerCustomer -> {
                if (isValidClickPressed()) {
                    delayLoad(bottom_navigation, 1, navController, -1)
                    //bottom_navigation.setCurrentItem(1)
                }
            }
            linearDrawerSupplier -> {
                if (isValidClickPressed())
                    delayLoad(bottom_navigation, -1, navController, R.id.nav_supplier)
                // navController?.navigate(R.id.nav_supplier)
                bottom_navigation.visibility = GONE
            }
            linearDrawerItems -> {
                if (isValidClickPressed()) {
                    delayLoad(bottom_navigation, -1, navController, R.id.nav_items)
                    // navController?.navigate(R.id.nav_items)
                    bottom_navigation.visibility = GONE
                }
            }
            linearDrawerSales -> {
                if (isValidClickPressed()) {
                    delayLoad(bottom_navigation, 2, navController, -1)
                    //bottom_navigation.setCurrentItem(2)
                }
            }
            linearDrawerPurchase -> {
                if (isValidClickPressed()) {
                    delayLoad(bottom_navigation, -1, navController, R.id.nav_purchase)
                    // navController?.navigate(R.id.nav_purchase)
                    bottom_navigation.visibility = GONE
                }
            }
            linearDrawerReceipt -> {
                if (isValidClickPressed()) {
                    delayLoad(bottom_navigation, 3, navController, -1)
                    //bottom_navigation.setCurrentItem(3)
                }
            }
            linearDrawerPayment -> {
                if (isValidClickPressed()) {
                    delayLoad(bottom_navigation, -1, navController, R.id.nav_payment)
                    // navController?.navigate(R.id.nav_payment)
                    bottom_navigation.visibility = GONE
                }
            }
            linearDrawerReports -> {
                if (isValidClickPressed()) {
                    delayLoad(bottom_navigation, -1, navController, R.id.nav_reports)
                    // navController?.navigate(R.id.nav_reports)
                    bottom_navigation.visibility = GONE
                }
            }
            linearDrawerLedger -> {
                if (isValidClickPressed()) {
                    delayLoad(bottom_navigation, -1, navController, R.id.nav_ledger)
                    // navController?.navigate(R.id.nav_purchase)
                    bottom_navigation.visibility = GONE
                }
            }

            linearDrawerGroups -> {
                if (isValidClickPressed()) {
                    delayLoad(bottom_navigation, -1, navController, R.id.nav_groups)
                    // navController?.navigate(R.id.nav_purchase)
                    bottom_navigation.visibility = GONE
                }
            }
            linearDrawerOpeningStock -> {
                if (isValidClickPressed()) {
                    delayLoad(bottom_navigation, -1, navController, R.id.nav_openingstock)
                    // navController?.navigate(R.id.nav_purchase)
                    bottom_navigation.visibility = GONE
                }
            }

            btnBackToLogin -> {
                if (isValidClickPressed()) {

                    delayLoad(bottom_navigation, -2, navController, -2)

                    bottom_navigation.visibility = GONE

                }
            }
        }

        closeDrawer()
    }

    private fun delayLoad(
        bottom_navigation: AHBottomNavigation,
        fragno: Int,
        navController: NavController,
        navcontrollerViewToLoadId: Int
    ) {

        Handler(Looper.getMainLooper()).postDelayed({
            //Do something after 260ms
            if (fragno >= 0) {
                bottom_navigation.setCurrentItem(fragno)
            } else if (fragno == -1) {
                navController.navigate(navcontrollerViewToLoadId)
            } else {
                logout()
            }

        }, 260)
    }

    private fun getloginDetailsFromPref() {
        prefs = PreferenceHelper.defaultPrefs(this)
        loginModel = Gson().fromJson(
            prefs[Constants.PREF_LOGIN_DETAIL_KEY, ""],
            LoginModel::class.java
        ) //getter

    }

    fun openCloseDrawer() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            drawer_layout.openDrawer(GravityCompat.START)
            handleDrawerInternalVisibility()
            CommonUtils.hideKeyboardnew(this)

        }
    }

    private fun handleDrawerInternalVisibility() {
        binding.root.rvCompanyListDrawer.visibility = View.GONE
        binding.root.lldrawerItemsRoot.visibility = View.VISIBLE
        binding.root.expandBtn.rotation = 0f
        isClicked = false
    }

    fun closeDrawer() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            // Hide bottom navigation
            drawer_layout.closeDrawer(GravityCompat.START)
        }
    }

    fun showBottomSheet() {
        bottom_navigation.visibility = VISIBLE
    }

    fun hideBottomSheet() {
        bottom_navigation.visibility = GONE
    }

    fun defaultdashboardselected() {
        if (!isFromDemoCompany)
            bottom_navigation.setCurrentItem(0, false)

    }

    fun loadSelectedFragment(position: Int) {
        when (position) {
            1 -> bottom_navigation.setCurrentItem(1)
            2 -> {
                navController.navigate(R.id.nav_supplier)
                hideBottomSheet()
            }
            3 -> {
                navController.navigate(R.id.nav_items)
                hideBottomSheet()
            }
            4 -> bottom_navigation.setCurrentItem(2)
        }
    }

    fun clearBackstack() {
        nav_host_fragment?.childFragmentManager?.popBackStack(
            null,
            FragmentManager.POP_BACK_STACK_INCLUSIVE
        );
    }

    override fun onBackPressed() {


        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {

            if (nav_host_fragment?.childFragmentManager?.backStackEntryCount == 0) {
                // First fragment is open, backstack is empty
                doubleBackToExitPressedOnce()
            } else {
                // there are fragments in backstack
                bottom_navigation.setCurrentItem(0)

                nav_host_fragment?.childFragmentManager?.popBackStack(
                    null,
                    FragmentManager.POP_BACK_STACK_INCLUSIVE
                );
            }
        }

    }

    fun profileDetailAPI(token: String?) {
        if (NetworkUtils.isConnected()) {
            CommonUtils.hideInternetDialog()
            viewModel.profileDetail(token).observe(this, Observer {
                it?.let { resource ->
                    when (resource.status) {
                        Status.SUCCESS -> {
                            if (it.data?.status == true) {

                                viewModel.profileDetail.postValue(it.data)
                                prefs[Constants.PREF_PROFILE_DETAIL_KEY] =
                                    Gson().toJson(it.data)

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


                        }
                        Status.ERROR -> {

                        }
                        Status.LOADING -> {

                        }
                        else -> {

                        }
                    }
                }
            })
        }
    }

    fun doubleBackToExitPressedOnce() {
        if (doubleBackToExitPressedOnce) {
            //super.onBackPressed();
            this.finish()
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, getString(R.string.click_back_again_to_exit), Toast.LENGTH_SHORT)
            .show();

        Handler().postDelayed(Runnable { doubleBackToExitPressedOnce = false }, 2000)
    }

    fun setSelectedCompanyName(position: Int) {
        if (companyNameList?.size!! > 0) {
            if (txtDrawerCompanyName.text.equals(companyNameList?.get(position))) {
                Toast.makeText(
                    this,
                    companyNameList?.get(position) + getString(R.string.already_selected),
                    Toast.LENGTH_SHORT
                ).show()
                //Toast.makeText(this,"Select Another Company to Switch",Toast.LENGTH_SHORT).show()
            } else {

                txtDrawerCompanyName.setText(companyNameList?.get(position))
                txtDrawerCompanyName.isSelected = true
                selectedCompanyID = companyList?.get(position)?.company_id
                binding.root.expandBtn.rotation = 0f
                isClicked = false
                closeDrawer()
                handleDrawerInternalVisibility()
                //switch company api call
                if (NetworkUtils.isConnected()) {
                    CommonUtils.hideInternetDialog()
                    switchCompanyApi(loginModel?.data?.bearer_access_token, selectedCompanyID)
                }


            }
        }


    }

    fun setSelectedBranchName() {
        //switch branch api call
        switchBranchAPI(loginModel?.data?.bearer_access_token, selectedBranchID)


    }

    private fun switchBranchAPI(token: String?, selectedBranchID: String?) {
        if (NetworkUtils.isConnected()) {
            viewModel.branchSwitch(token, selectedBranchID)
                .observe(this, Observer {
                    it?.let { resource ->
                        when (resource.status) {
                            Status.SUCCESS -> {
                                if (it.data?.status == true) {

                                    //updating loginmodel data
                                    loginModel.data?.branch_info = it.data?.data?.branch_info
                                    loginModel.data?.user_info = it.data?.data?.user_info
                                    loginModel.data?.company_info = it.data?.data?.company_info

                                    //updating login_detail pref
                                    val prefs = PreferenceHelper.defaultPrefs(this)
                                    prefs.edit().remove(Constants.PREF_LOGIN_DETAIL_KEY).apply()
                                    prefs[Constants.PREF_LOGIN_DETAIL_KEY] =
                                        Gson().toJson(loginModel) //setter

                                    if (!isFromDemoCompany && !loginModel.data!!.user_info!!.pin.isNullOrBlank()) {
                                        prefs[PASSWORD_PREFERENCE_KEY] =
                                            loginModel.data!!.user_info!!.pin
                                    }
                                    Toast.makeText(this, it.data.message, Toast.LENGTH_SHORT).show()

                                    //backstack cleared and refreshing dashboard fragment

                                    nav_host_fragment?.childFragmentManager?.popBackStack(
                                        null,
                                        FragmentManager.POP_BACK_STACK_INCLUSIVE
                                    );
                                    // bottom_navigation.setCurrentItem(0)
                                    navController?.navigate(R.id.nav_dashboard)


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
        } else {
            /*CommonUtils.showDialog(this, getString(R.string.please_check_internet_msg))*/
        }
    }

    private fun switchCompanyApi(bearerAccessToken: String?, companyId: String?) {
        if (NetworkUtils.isConnected()) {
            viewModel.companySwitch(bearerAccessToken, companyId).observe(this, Observer {
                it?.let { resource ->
                    when (resource.status) {
                        Status.SUCCESS -> {
                            if (it.data?.status == true) {
                                //updating loginmodel data
                                loginModel.data?.branch_info = it.data?.data?.branch_info
                                loginModel.data?.user_info = it.data?.data?.user_info
                                loginModel.data?.company_info = it.data?.data?.company_info

                                adapter.currentCompName =
                                    loginModel.data!!.company_info!!.company_name.toString()
                                adapter.notifyDataSetChanged()
                                //updating login_detail pref
                                val prefs = PreferenceHelper.defaultPrefs(this)
                                prefs.edit().remove(Constants.PREF_LOGIN_DETAIL_KEY).apply()
                                prefs[Constants.PREF_LOGIN_DETAIL_KEY] =
                                    Gson().toJson(loginModel) //setter

                                if (!isFromDemoCompany && !loginModel.data!!.user_info!!.pin.isNullOrBlank()) {
                                    prefs[PASSWORD_PREFERENCE_KEY] =
                                        loginModel.data!!.user_info!!.pin
                                }

                                if (loginModel.data?.company_info?.gold_rate?.type.equals(
                                        "1",
                                        true
                                    )
                                ) {
                                    dashboardGoldrate =
                                        loginModel.data?.company_info?.gold_rate?.bill_rate_amount.toString()
                                } else
                                    dashboardGoldrate =
                                        loginModel.data?.company_info?.gold_rate?.cash_rate_amount.toString()
                                //if (dashboardGoldrate.equals("0.00"))
                                //CommonUtils.goldRateDialogShow = 0
                                //backstack cleared and refreshing dashboard fragment
                                nav_host_fragment?.childFragmentManager?.popBackStack(
                                    null,
                                    FragmentManager.POP_BACK_STACK_INCLUSIVE
                                );
                                // bottom_navigation.setCurrentItem(0)
                                navController?.navigate(R.id.nav_dashboard)
                                if (loginModel.data!!.company_info!!.id.equals(Constants.DemoCompanyId)) {
                                    linearDrawerSettings.visibility = View.GONE
                                } else {
                                    linearDrawerSettings.visibility = View.VISIBLE
                                }
                                Toast.makeText(this, it.data.message, Toast.LENGTH_SHORT).show()

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
                            /*Toast.makeText(this, it.data?.errormessage?.message, Toast.LENGTH_LONG)
                                .show()*/
                        }
                        Status.LOADING -> {
                            CommonUtils.showProgress(this)
                        }
                    }
                }
            })
        }

    }

    private fun getUpdateSessionDetails(token: String?) {
        if (NetworkUtils.isConnected()) {
            CommonUtils.hideInternetDialog()
            viewModel.getUpdateSessionDetails(token)
                .observe(this, Observer {
                    it?.let { resource ->
                        when (resource.status) {
                            Status.SUCCESS -> {
                                if (it.data?.status == true) {
                                    IsUpdateAvailable()
                                    updateDateForReview()
                                    //updating loginmodel data
                                    loginModel.data?.branch_info = it.data?.data?.branch_info
                                    loginModel.data?.user_info = it.data?.data?.user_info
                                    loginModel.data?.company_info = it.data?.data?.company_info

                                    //updating login_detail pref
                                    val prefs = PreferenceHelper.defaultPrefs(this)
                                    prefs.edit().remove(Constants.PREF_LOGIN_DETAIL_KEY).apply()
                                    prefs[Constants.PREF_LOGIN_DETAIL_KEY] =
                                        Gson().toJson(loginModel) //setter


                                    if (isFromDemoCompany) {
                                        val lockManager: LockManager<AppLockActivityNew> =
                                            LockManager.getInstance() as LockManager<AppLockActivityNew>

                                        txtDrawerCompanyName.text =
                                            loginModel.data?.company_info?.company_name
                                        txtDrawerCompanyName.isSelected = true

                                        if (loginModel.data!!.company_info!!.id.equals(Constants.DemoCompanyId)) {
                                            linearDrawerSettings.visibility = View.GONE
                                        } else {
                                            linearDrawerSettings.visibility = View.VISIBLE
                                        }
                                        bottom_navigation.setCurrentItem(0)
                                    } else {
                                        if (!loginModel.data!!.user_info!!.pin.isNullOrBlank()) {
                                            prefs[PASSWORD_PREFERENCE_KEY] =
                                                loginModel.data!!.user_info!!.pin
                                        }
                                        /*when(getCurrentFragment().equals((resources.getString(R.string.dashboardfragfullname)),true)){
                                            true->{
                                                nav_host_fragment?.childFragmentManager?.popBackStack(
                                                    null,
                                                    FragmentManager.POP_BACK_STACK_INCLUSIVE
                                                );
                                                navController?.navigate(R.id.nav_dashboard)
                                            }
                                        }*/

                                    }
                                    //   Toast.makeText(this,it.data.message,Toast.LENGTH_SHORT).show()

                                    //backstack cleared and refreshing dashboard fragment

                                    /*when (getCurrentFragment().equals(
                                        (resources.getString(R.string.dashboardfragfullname)),
                                        true
                                    )) {
                                        true -> {
                                            if (bottom_navigation.currentItem == 0) {
                                                nav_host_fragment?.childFragmentManager?.popBackStack(
                                                    null,
                                                    FragmentManager.POP_BACK_STACK_INCLUSIVE
                                                );
                                                navController?.navigate(R.id.nav_dashboard)
                                            }
                                        }
                                    }*/
                                    /*if(nav_host_fragment?.childFragmentManager?.findFragmentById(R.id.nav_dashboard)?.isVisible!!){
                                        nav_host_fragment?.childFragmentManager?.popBackStack(
                                            null,
                                            FragmentManager.POP_BACK_STACK_INCLUSIVE
                                        );
                                        navController?.navigate(R.id.nav_dashboard)
                                    }*/


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
                                /*Toast.makeText(
                                    this,
                                    it.data?.errormessage?.message,
                                    Toast.LENGTH_LONG
                                )
                                    .show()*/
                            }
                            Status.LOADING -> {
                                CommonUtils.showProgress(this)
                            }
                        }
                    }
                })
        } else {
            /*CommonUtils.showDialog(this, getString(R.string.please_check_internet_msg))*/
        }
    }


    private fun getWebLinksFromApi() {
        if (NetworkUtils.isConnected()) {
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
        } else {
            /*  CommonUtils.showDialog(this, getString(R.string.please_check_internet_msg))*/
        }
    }

    fun updateDrawerList(data: UserWiseRestrictionModel.Data) {

        when (loginModel.data!!.user_info!!.user_type.equals("user", true)) {
            // user type user
            true -> {
                // apply restriciton
                defaultDisableAllButtonnUI()
                // userWiseRestriction(loginModel.data?.bearer_access_token)
                applyUserWiseRestriction(data)
            }
            // user_type -> admin or super_admin or any other
            false -> {
                defaultEnableAllButtonnUI()
                when (enableSettingFromDrawer) {
                    true -> {
                        linearDrawerSettings.visibility = View.VISIBLE
                    }
                    false -> {
                        linearDrawerSettings.visibility = View.GONE
                    }
                }
            }
        }

    }
}
