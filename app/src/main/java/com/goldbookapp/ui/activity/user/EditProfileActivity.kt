package com.goldbookapp.ui.activity.user

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.blankj.utilcode.util.NetworkUtils
import com.bumptech.glide.Glide
import com.github.dhaval2404.imagepicker.ImagePicker
import com.goldbookapp.R
import com.goldbookapp.api.ApiHelper
import com.goldbookapp.api.RetrofitBuilder
import com.goldbookapp.databinding.EditProfileActivityBinding
import com.goldbookapp.model.LoginModel
import com.goldbookapp.model.ProfileDetailModel
import com.goldbookapp.ui.activity.viewmodel.EditProfileViewModel
import com.goldbookapp.ui.activity.viewmodel.ViewModelFactory
import com.goldbookapp.utils.CommonUtils
import com.goldbookapp.utils.CommonUtils.Companion.clickWithDebounce
import com.goldbookapp.utils.Constants
import com.goldbookapp.utils.PreferenceHelper
import com.goldbookapp.utils.PreferenceHelper.get
import com.goldbookapp.utils.Status
import com.google.gson.Gson
import com.hamzasharuf.networkmonitor.ConnectivityStateHolder
import com.hamzasharuf.networkmonitor.Event
import com.hamzasharuf.networkmonitor.NetworkEvents
import kotlinx.android.synthetic.main.edit_profile_activity.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.util.*

class EditProfileActivity : AppCompatActivity(){

    private lateinit var viewModel: EditProfileViewModel

    lateinit var binding: EditProfileActivityBinding

    var c = Calendar.getInstance()
    private var changeContact: Boolean = false
    lateinit var loginModel: LoginModel
    private var isImageChanged:Boolean = false
    lateinit var fileBody: RequestBody
    lateinit var multipartBody: MultipartBody.Part
    lateinit var profileDetailModel: ProfileDetailModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.edit_profile_activity)

        setupViewModel()
        setupUIandListner()


    }

    private fun setupViewModel() {
        viewModel =
            ViewModelProvider(this, ViewModelFactory(ApiHelper(RetrofitBuilder.apiService))).get(
                EditProfileViewModel::class.java
            )
        binding.setLifecycleOwner(this)
        binding.editProfileViewModel = viewModel
    }

    private fun setupUIandListner(){

        val prefs = PreferenceHelper.defaultPrefs(this)
        loginModel = Gson().fromJson(
            prefs[Constants.PREF_LOGIN_DETAIL_KEY, ""],
            LoginModel::class.java
        ) //getter

        profileDetailModel = Gson().fromJson(
            prefs[Constants.PREF_PROFILE_DETAIL_KEY, ""],
            ProfileDetailModel::class.java
        ) //getter

        viewModel.profileDetail.postValue(profileDetailModel)

        viewModel.profileDetail.observe(this, Observer { profileModel ->

            Glide.with(this).load(profileModel.data?.user?.imageurl).circleCrop().into(imgProfileEdit)

        })


        txtNameEdit.doAfterTextChanged { tvNameEditInputLayout.error = null }

        binding.toolbar.imgLeft.setImageResource(R.drawable.ic_back)
        binding.toolbar.imgLeft.setColorFilter(Color.BLACK)
        //binding.toolbar.tvRight.setText(getString(R.string.save))
        //binding.toolbar.tvRight.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary))
        binding.toolbar.tvTitle.setText("Edit Profile")


        binding.toolbar.imgLeft.clickWithDebounce {
            onBackPressed()
        }

        binding.btnSaveEditProfile.clickWithDebounce {

            txtNameEdit.setFocusableInTouchMode(false)
            txtNameEdit.clearFocus()
            txtNameEdit.setFocusableInTouchMode(true)
            if(performValidation()){
                if(NetworkUtils.isConnected()){
                    when(isImageChanged){
                       true -> updateProfileImageAPI(loginModel?.data?.bearer_access_token, multipartBody)
                        false ->  {
                            if(NetworkUtils.isConnected()){
                                editProfileAPI(loginModel?.data?.bearer_access_token, txtNameEdit.text.toString(), txtDOBEdit.text.toString(), txtGenderEdit.text.toString())
                            }

                        }
                    }

                }
            }
        }

        binding.imgEditContact.clickWithDebounce {
            startActivity(Intent(this, VerifyPasswordActivity::class.java))
        }

        binding.txtGenderEdit.clickWithDebounce {
            openGenderPopup(binding.txtGenderEdit)
        }

        binding.txtDOBEdit.clickWithDebounce {

            openDatePicker()
        }

        binding.imgProfileEdit.clickWithDebounce {

            ImagePicker.with(this)
                .cropSquare()	    			//Crop image(Optional), Check Customization for more option
                .compress(1024)			//Final image size will be less than 1 MB(Optional)
                .maxResultSize(1080, 1080)	//Final image resolution will be less than 1080 x 1080(Optional)
                .start()
        }

    }
    override fun onDestroy() {
        super.onDestroy()
        CommonUtils.hideProgress()
    }
    override fun onResume() {
        super.onResume()
               val prefs = PreferenceHelper.defaultPrefs(this)
               profileDetailModel = Gson().fromJson(
                   prefs[Constants.PREF_PROFILE_DETAIL_KEY, ""],
                   ProfileDetailModel::class.java
               ) //getter
               txtPhoneEditProfile.setText(profileDetailModel.data?.user?.mobile_no)
               txtEmailEditProfile.setText(profileDetailModel.data?.user?.email)


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
                    defaultEnableAllButtonnUI()
                    /*defaultDisableAllButtonnUI()
                    if (intent.extras != null && intent.extras!!.containsKey(Constants.Change_Status)) {
                        changeContact = intent.getBooleanExtra(Constants.Change_Status,false)
                    }

                    when(changeContact){
                        true -> {
                            binding.imgEditContact.visibility = View.VISIBLE
                        }
                        false->{
                            binding.imgEditContact.visibility = View.GONE
                        }
                    }*/
                }
                // user_type -> admin or super_admin or any other
                false -> {
                    defaultEnableAllButtonnUI()
                }
            }






        }

        if (!ConnectivityStateHolder.isConnected) {
            // Network is not available
            CommonUtils.showDialog(this, getString(R.string.please_check_internet_msg))

        }
    }

    private fun defaultEnableAllButtonnUI() {
        binding.imgEditContact.visibility = View.VISIBLE
    }

    private fun defaultDisableAllButtonnUI() {
        binding.imgEditContact.visibility = View.GONE
    }

    fun openGenderPopup(view: View){
        val popupMenu: PopupMenu = PopupMenu(this, view)
        popupMenu.menu.add("Male")
        popupMenu.menu.add("Female")

        popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
            binding.txtGenderEdit.setText(item.title)
            true
        })

        popupMenu.show()
    }

    fun openDatePicker(){

        //val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        val dpd = DatePickerDialog(this, DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->

            c.set(Calendar.YEAR, year)
            c.set(Calendar.MONTH, monthOfYear)
            c.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            // Display Selected date in textbox
            txtDOBEdit.setText("" + String.format("%02d", dayOfMonth)   + "-" + String.format("%02d", (monthOfYear + 1)) + "-" + year)

        }, year, month, day)

        dpd.datePicker.maxDate = Date().time
        dpd.show()
    }

    fun editProfileAPI(token: String?,
                       name: String?,
                       birthdate: String?,
                       gender: String?){

        viewModel.editProfile(token, name, birthdate, gender).observe(this, Observer {
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

    fun updateProfileImageAPI(token: String?,
                              profile_image: MultipartBody.Part?){
        if(NetworkUtils.isConnected()){
            viewModel.updateProfileImage(token, profile_image).observe(this, Observer {
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

    fun performValidation(): Boolean {

        if(txtNameEdit.text.toString().isBlank()){
            tvNameEditInputLayout?.error = getString(R.string.name_validation_msg)
            return false
        }else if(txtDOBEdit.text.toString().isBlank()){
            tvDOBEditInputLayout?.error = getString(R.string.dob_validation_msg)
            return false
        }else if(txtGenderEdit.text.toString().isBlank()){
            tvGenderEditInputLayout?.error = getString(R.string.gender_validation_msg)
            return false
        }

        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            //Image Uri will not be null for RESULT_OK
            val fileUri = data?.data
            //imgProfile.setImageURI(fileUri)
            Glide.with(this).load(fileUri).circleCrop().into(imgProfileEdit)

            //You can get File object from intent
            val imageFile: File = ImagePicker.getFile(data)!!

            fileBody = RequestBody.create(MediaType.parse("multipart/form-data"), imageFile)
            multipartBody= MultipartBody.Part.createFormData("profile_image", imageFile.name, fileBody)

            isImageChanged = true


            //You can also get File Path from intent
            val filePath:String = ImagePicker.getFilePath(data)!!


        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
        } else {

        }
    }
}