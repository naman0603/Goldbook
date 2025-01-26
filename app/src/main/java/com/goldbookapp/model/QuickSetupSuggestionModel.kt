package com.goldbookapp.model

data class QuickSetupSuggestionModel(
    val data: Data?,
    val code: String?,
    val message: String?,
    val status: Boolean?,
    val errormessage: LoginModel.Errormessage?
) {

    data class Data(val company: Company?) {
        data class Company(val company_id: String?, val suggestion: List<String>?)
    }
}
//data class QuickSetupSuggestionModel(
//    val data: Data?,
//    val code: Number?,
//    val message: String?,
//    val status: Boolean?,
//    val errormessage: LoginModel.Errormessage?
//) {
//    data class Data(val company: Company?){
//        data class Company(val suggestion: List<String>?)
//    }
//}




