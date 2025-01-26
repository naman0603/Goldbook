package com.goldbookapp.ui.activity.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.goldbookapp.api.ApiHelper
import com.goldbookapp.api.Resource
import com.goldbookapp.model.LoginModel
import kotlinx.coroutines.Dispatchers

class LoginViewModel (private val apiHelper: ApiHelper) : ViewModel() {

    //var email = MutableLiveData<String>()
    //var password = MutableLiveData<String>()
    //var progressDialog: SingleLiveEvent<Boolean>? = null
    var userLogin: MutableLiveData<LoginModel>? = null
    val loginValidation: MutableLiveData<String>? = null

   /* // This observer will invoke onEmailChanged() when the user updates the email
    private val emailObserver = Observer<String> { onEmailChanged(it) }*/

   /* init {
        //progressDialog = SingleLiveEvent<Boolean>()
        //email = ObservableField("")
        //password = ObservableField("")
        userLogin = MutableLiveData<LoginModel>()
    }
*/
    fun getLoginData(email: String, pass: String) = liveData(Dispatchers.IO){
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = apiHelper.getLoginData(email, pass)))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }


    fun logout(
        token: String?
    ) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = apiHelper.logout(token)))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
           /* Log.v("..errormsg..", exception.message)*/
        }
    }
    /*fun performValidation() {

        if (email.toString().isBlank() || !CommonUtils.isValidEmail(email.toString())) {
            loginValidation?.value = "Invalid username"
            return
        }

        if (password.toString().isBlank()) {
            loginValidation?.value = "Invalid password"
            return
        }

        loginValidation?.value = "Valid credentials :)"
    }

    fun onEmailChanged(s: String) {
        this.email.value = s
    }

    fun onPasswordChanged(s: CharSequence) {
        this.password.value = s.toString()
    }*/
}