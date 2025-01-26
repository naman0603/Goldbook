package com.goldbookapp.model

data class ActivePlanModel(
    val data: Data?,
    val code: String?,
    val message: String?,
    val status: Boolean?,
    val errormessage: LoginModel.Errormessage?
) {
    data class Data(val plan_name: String?

    )
}
