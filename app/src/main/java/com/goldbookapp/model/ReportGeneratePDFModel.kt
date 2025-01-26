package com.goldbookapp.model

import okhttp3.ResponseBody
import retrofit2.Call

data class ReportGeneratePDFModel(
    val data: Call<ResponseBody>,
    val file_name: String?,
    val message: String?,
    val code: String?,
    val status: Boolean?,
    val errormessage: LoginModel.Errormessage?
)
