package com.goldbookapp.model

data class AppVersion(val data: Data?, val message: String?, val code: String?, val status: Boolean?,val errormessage: LoginModel.Errormessage?)

data class Data(val app_version: String?)
