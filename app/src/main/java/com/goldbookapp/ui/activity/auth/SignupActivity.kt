package com.goldbookapp.ui.activity.auth

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.doAfterTextChanged
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.blankj.utilcode.util.NetworkUtils
import com.goldbookapp.R
import com.goldbookapp.api.ApiHelper
import com.goldbookapp.api.RetrofitBuilder
import com.goldbookapp.databinding.SignupActivityBinding
import com.goldbookapp.model.SignupModel
import com.goldbookapp.model.WebLinksModel
import com.goldbookapp.ui.activity.settings.PrivacyPolicyActivity
import com.goldbookapp.ui.QuickSetupActivity
import com.goldbookapp.ui.activity.viewmodel.SignupViewModel
import com.goldbookapp.ui.activity.viewmodel.ViewModelFactory
import com.goldbookapp.utils.CommonUtils
import com.goldbookapp.utils.CommonUtils.Companion.clickWithDebounce
import com.goldbookapp.utils.CommonUtils.Companion.isValidClickPressed
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
import kotlinx.android.synthetic.main.signup_activity.*

class SignupActivity : AppCompatActivity() {
    lateinit var prefs: SharedPreferences
    private lateinit var viewModel: SignupViewModel
    var signupModel = SignupModel()
    lateinit var binding: SignupActivityBinding
    var isfromOnBoard: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.signup_activity)

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

    override fun onDestroy() {
        super.onDestroy()
        CommonUtils.hideProgress()
    }

    private fun setupViewModel() {
        viewModel =
            ViewModelProvider(this, ViewModelFactory(ApiHelper(RetrofitBuilder.apiService))).get(
                SignupViewModel::class.java
            )
        binding.signupViewModel = viewModel
    }

    private fun setupUIandListner() {
        prefs = PreferenceHelper.defaultPrefs(this)
        prefs = PreferenceHelper.defaultPrefs(this)
        val weblinks = Gson().fromJson(
            prefs[Constants.WebLinks, ""],
            WebLinksModel.Data::class.java
        ) //getter
        Constants.apicallcount = 0
        var firstPart: String = "I agree to the ";
        var secondPart: String = "Terms of Service ";
        var thirdPart: String = "and ";
        var forthPart: String = "Privacy Policy";
        var finalString: String = firstPart + secondPart + thirdPart + forthPart;
        var spannableStringBuilder: SpannableStringBuilder = SpannableStringBuilder(finalString)

        val clickableTermsSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                if (isValidClickPressed()) {

                    if (!weblinks.terms.isNullOrBlank())
                        startActivity(
                            Intent(
                                this@SignupActivity,
                                PrivacyPolicyActivity::class.java
                            ).putExtra("weburi", weblinks.terms).putExtra(
                                "title", getString(
                                    R.string.terms_of_service
                                )
                            )
                        )
                }
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.color = ContextCompat.getColor(this@SignupActivity, R.color.colorPrimary)
                ds.typeface = ResourcesCompat.getFont(this@SignupActivity, R.font.proxima_nova_bold)
                ds.isUnderlineText = false
            }

        }

        val clickablePolicySpan = object : ClickableSpan() {
            override fun onClick(widget: View) {

                if (isValidClickPressed()) {

                    if (!weblinks.privacy.isNullOrBlank())
                        startActivity(
                            Intent(
                                this@SignupActivity,
                                PrivacyPolicyActivity::class.java
                            ).putExtra("weburi", weblinks.privacy).putExtra(
                                "title", getString(
                                    R.string.privacy_policy
                                )
                            )
                        )
                }
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.color = ContextCompat.getColor(this@SignupActivity, R.color.colorPrimary)
                ds.typeface = ResourcesCompat.getFont(this@SignupActivity, R.font.proxima_nova_bold)
                ds.isUnderlineText = false
            }

        }
        spannableStringBuilder.setSpan(
            clickableTermsSpan,
            15,
            32,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannableStringBuilder.setSpan(
            clickablePolicySpan,
            36,
            50,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        binding.tvPolicy.setText(spannableStringBuilder)
        binding.tvPolicy.setMovementMethod(LinkMovementMethod.getInstance())
        if (intent.extras != null && intent.extras!!.containsKey("isfromOnBoard")) {
            isfromOnBoard = (intent.getBooleanExtra("isfromOnBoard", false))
        }
        binding.tvSignIn.clickWithDebounce {

            when (isfromOnBoard) {
                true -> startActivity(Intent(this, LoginActivity::class.java))
                false -> onBackPressed()
            }

        }
        binding.passWordInfo.visibility = View.VISIBLE
        binding.passWordInfo?.text = getString(R.string.password_string)

        binding.txtNameSignup.doAfterTextChanged { txtNameSignupInputLayout.error = null }
        binding.txtEmailSignup.doAfterTextChanged { txtEmailSignupInputLayout.error = null }
        binding.txtMobileSignup.doAfterTextChanged { txtMobileSignupInputLayout.error = null }
        binding.txtPasswordSignup.doAfterTextChanged {
            binding.txtPasswordSignupInputLayout.error = null
            binding.passWordInfo.visibility = View.VISIBLE
        }
        binding.txtConfirmPasswordSignup.doAfterTextChanged {
            binding.txtConfirmPasswordSignupInputLayout.error = null
        }

    }

    fun signupClicked(view: View) {
        if (NetworkUtils.isConnected()) {
            if (performValidation()) {

                userSignUP()

            }
        } else {
            CommonUtils.showDialog(this, getString(R.string.please_check_internet_msg))
        }
    }

    fun userSignUP() {
        if (NetworkUtils.isConnected()) {

            viewModel.userSignUP(
                binding.txtNameSignup.text.toString().trim(),
                binding.txtPasswordSignup.text.toString().trim(),
                binding.txtEmailSignup.text.toString().trim(),
                binding.txtMobileSignup.text.toString().trim(),
                false

            ).observe(this, Observer {
                it?.let { resource ->
                    when (resource.status) {
                        Status.SUCCESS -> {

                            if (it.data?.status == true) {
                                signupModel = it.data
                                val prefs = defaultPrefs(this)
                                prefs[Constants.PREF_COMPANY_REGISTER_KEY] =
                                    Gson().toJson(signupModel) //setter

                                startActivity(
                                    Intent(
                                        this@SignupActivity,
                                        QuickSetupActivity::class.java
                                    )
                                )
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

    fun performValidation(): Boolean {
        passWord_Info.visibility = View.GONE
        if (binding.txtNameSignup.text.toString().isBlank()) {
            binding.txtNameSignupInputLayout?.error = getString(R.string.signup_full_name)
            return false
        } else if (binding.txtEmailSignup.text.toString().isBlank() || !CommonUtils.isValidEmail(
                binding.txtEmailSignup.text.toString()
            )
        ) {
            binding.txtEmailSignupInputLayout?.error = getString(R.string.email_validation_msg)
            return false
        } else if (binding.txtMobileSignup.text.toString().isBlank()) {
            binding.txtMobileSignupInputLayout?.error = getString(R.string.enter_mobile_no_msg)
            return false
        } else if (binding.txtMobileSignup.text.toString().length < 10) {
            binding.txtMobileSignupInputLayout?.error = getString(R.string.mobile_digit_count)
            return false
        } else if (binding.txtPasswordSignup.text.toString().isBlank()) {
            binding.txtPasswordSignupInputLayout?.error = getString(R.string.signup_pwd)
            return false
        } else if (!CommonUtils.isValidPassword(binding.txtPasswordSignup.text.toString())) {
            binding.passWordInfo.visibility = View.GONE
            binding.txtPasswordSignupInputLayout?.error = getString(R.string.password_condition_text_msg)
            return false
        } else if (binding.txtConfirmPasswordSignup.text.toString().isBlank()) {
            binding.txtConfirmPasswordSignupInputLayout?.error = getString(R.string.enter_confirm_pwd_msg)
            return false
        } else if (binding.txtPasswordSignup.text.toString() != binding.txtConfirmPasswordSignup.text.toString()) {
            binding.txtConfirmPasswordSignupInputLayout?.error =
                getString(R.string.pwd_confirmpwd_doesnt_match_msg)
            return false
        } else if (!binding.checkAgreePolicy.isChecked) {
            Toast.makeText(this, getString(R.string.agree_terms_priivacy), Toast.LENGTH_LONG).show()
            return false
        }

        return true
    }


}