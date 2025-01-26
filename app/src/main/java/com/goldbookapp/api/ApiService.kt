package com.goldbookapp.api

import OpeningStockDetailModel
import PaymentDetailModel
import PurchaseDetailModel
import ReceiptDetailModel
import UserLimitAccessModel
import com.goldbookapp.model.*
import com.goldbookapp.ui.adapter.SupplierDetailModel
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface ApiService {

    @FormUrlEncoded
    @POST("login")
    suspend fun loginAPI(
        @Field("name") name: String?,
        @Field("password") password: String?
    ): LoginModel


    @POST("logout")
    suspend fun logoutApi(
        @Header("Authorization") token: String?
    ): LogoutModel

    @FormUrlEncoded
    @POST("username/register")
    suspend fun userRegister(
        @Field("name") name: String?,
        @Field("password") password: String?,
        @Field("email") email: String?,
        @Field("mobile_no") mobile_no: String?,
        @Field("username") username: String?,
        @Field("resend") resend: Boolean?
    ): SignupModel

    @FormUrlEncoded
    @POST("user/register")
    suspend fun userSignUP(
        @Field("name") name: String?,
        @Field("password") password: String?,
        @Field("email") email: String?,
        @Field("mobile_no") mobile_no: String?,
        @Field("resend") resend: Boolean?
    ): SignupModel


    @GET("get/country")
    suspend fun getCountry(): CountryModel

    @GET("about")
    suspend fun aboutUs(): AboutUsModel


    @POST("userPlan")
    suspend fun activeplan(
        @Header("Authorization") token: String?
    ): ActivePlanModel

    @GET("webLinks")
    suspend fun webLinks(): WebLinksModel

    @FormUrlEncoded
    @POST("get/state")
    suspend fun getState(@Field("country_id") country_id: String?): StateModel

    @FormUrlEncoded
    @POST("get/city")
    suspend fun getCity(@Field("state_id") state_id: String?): CityModel

    @FormUrlEncoded
    @POST("otp/verify")
    suspend fun verifyOTP(
        @Field("otp") otp: String?,
        @Field("mobile_no") mobile_no: String?,
        @Field("name") name: String?,
        @Field("email") email: String?,
        @Field("password") password: String?,
        @Field("username") username: String?,
        @Field("otp_email") otp_email: String?
    ): VerifyPhoneOTPModel

    @FormUrlEncoded
    @POST("username/suggestion")
    suspend fun getSuggestion(
        @Field("username") username: String?,
        @Field("company_id") company_id: String?
    ): QuickSetupSuggestionModel

    @FormUrlEncoded
    @POST("update/username")
    suspend fun updateUsername(
        @Field("username") username: String?
    ): UpdateUsernameModel


    @FormUrlEncoded
    @POST("company/setup")
    suspend fun companySetup(
        @Header("Authorization") token: String?,
        @Field("company_name") company_name: String?,
        @Field("business_location") business_location: String?,
        @Field("state_id") state_id: String?,
        @Field("city_id") city_id: String?,
        @Field("term_balance") term_balance: String?,
        @Field("default_term") default_term: String?,
        @Field("gst_register") gst_register: String?,
        @Field("gst_tin_number") gst_tin_number: String?,
        @Field("company_id") company_id: String?
    ): CompanySetupModel

    @FormUrlEncoded
    @POST("auth/forgot-password")
    suspend fun forgotPassword(@Field("email") email_mobile: String?): RecoverAccountModel

    @FormUrlEncoded
    @POST("password/verify")
    suspend fun verifyPassword(
        @Header("Authorization") token: String?,
        @Field("password") password: String?
    ): VerifyPasswordModel

    @FormUrlEncoded
    @POST("update/contact")
    suspend fun updateContact(
        @Header("Authorization") token: String?,
        @Field("mobile_no") mobile_no: String?,
        @Field("email") email: String?,
        @Field("otp") otp: String?
    ): ProfileDetailModel

    @FormUrlEncoded
    @POST("add/category")
    suspend fun addItemCategory(
        @Header("Authorization") token: String?,
        @Field("category_name") category_name: String?,
        @Field("category_code") category_code: String?,
        @Field("status") status: String?
    ): NewItemCatModel

    @FormUrlEncoded
    @POST("update/category")
    suspend fun updateItemCategory(
        @Header("Authorization") token: String?,
        @Field("category_name") category_name: String?,
        @Field("category_id") category_id: String?,
        @Field("category_code") category_code: String?,
        @Field("status") status: Number?
    ): EditItemCatModel

    @POST("profile")
    suspend fun profileDetail(@Header("Authorization") token: String?): ProfileDetailModel

    @POST("get/user_company")
    suspend fun userCompanyList(@Header("Authorization") token: String?): UserCompanyListModel


    @FormUrlEncoded
    @POST("company/switch")
    suspend fun companySwitch(
        @Header("Authorization") token: String?,
        @Field("company_id") company_id: String?
    ): LoginModel


    @POST("user/getUpdateSessionDetails")
    suspend fun getUpdateSessionDetails(
        @Header("Authorization") token: String?
    ): LoginModel


    @FormUrlEncoded
    @POST("branch/switch")
    suspend fun branchSwitch(
        @Header("Authorization") token: String?,
        @Field("branch_id") branch_id: String?
    ): LoginModel


    @FormUrlEncoded
    @POST("edit/profile")
    suspend fun editProfile(
        @Header("Authorization") token: String?,
        @Field("name") name: String?,
        @Field("birthdate") birthdate: String?,
        @Field("gender") gender: String?
    ): ProfileDetailModel

    @Multipart
    @POST("update/profile/image")
    suspend fun updateProfileImage(
        @Header("Authorization") token: String?,
        @Part profile_image: MultipartBody.Part?
    ): ProfileDetailModel

    @FormUrlEncoded
    @POST("add/customer")
    suspend fun addCustomer(
        @Header("Authorization") token: String?,
        @Field("company_id") company_id: String?,
        @Field("customer_type") customer_type: String?,
        @Field("title") title: String?,
        @Field("first_name") first_name: String?,
        @Field("last_name") last_name: String?,
        @Field("company_name") company_name: String?,
        @Field("customer_code") customer_code: String?,
        @Field("display_name") display_name: String?,
        @Field("mobile_number") mobile_number: String?,
        @Field("secondary_contact") secondary_contact: String?,
        @Field("email") email: String?,
        @Field("opening_fine_balance") opening_fine_balance: String?,
        @Field("opening_fine_default_term") opening_fine_default_term: String?,
        @Field("opening_silver_fine_balance") opening_silver_fine_balance: String?,
        @Field("opening_silver_fine_default_term") opening_silver_fine_default_term: String?,
        @Field("opening_cash_balance") opening_cash_balance: String?,
        @Field("opening_cash_default_term") opening_cash_default_term: String?,
        @Field("fine_limit") fine_limit: String?,
        @Field("cash_limit") cash_limit: String?,
        @Field("is_tcs_applicable") is_tcs_applicable: String?,
        @Field("gst_register") gst_register: String?,
        @Field("gst_treatment") gst_treatment: String?,
        @Field("gst_tin_number") gst_tin_number: String?,
        @Field("pan_number") pan_number: String?,
        @Field("courier") courier: String?,
        @Field("notes") notes: String?,
        @Field("is_shipping") is_shipping: String?,
        @Field("billing_address") billing_address: String?,
        @Field("shipping_address") shipping_address: String?,
        @Field("is_tds_applicable") is_tds_applicable: String?,
        @Field("tds_deductor_type") tds_deductor_type: String?,
        @Field("tcs_collector_type") tcs_collector_type: String?,
        @Field("nature_of_good_name") nature_of_good_name: String?,
        @Field("nature_of_payment_name") nature_of_payment_name: String?,
        @Field("nature_of_payment") nature_of_payment: String?,
        @Field("nature_of_goods") nature_of_good_id: String?
    ): NewCustomerModel

    @FormUrlEncoded
    @POST("add/user_company")
    suspend fun addUserCompany(
        @Header("Authorization") token: String?,
        @Field("company_name") company_name: String?,
        @Field("reg_address") reg_address: String?,
        @Field("area") area: String?,
        @Field("landmark") landmark: String?,
        @Field("business_location") country_id: String?,
        @Field("state_id") state_id: String?,
        @Field("city_id") city_id: String?,
        @Field("postal_code") postal_code: String?,
        @Field("contact_person_first_name") contact_person_first_name: String?,
        @Field("contact_person_last_name") contact_person_last_name: String?,
        @Field("mobile_number") mobile_number: String?,
        @Field("alternate_number") alternate_number: String?,
        @Field("email") email: String?,
        @Field("fiscal_year_id") fiscal_year_id: String?,
        @Field("pan_number") pan_number: String?,
        @Field("cin_number") cin_number: String?,
        @Field("term_balance") term_balance: String?,
        @Field("default_term") default_term: String?,
        @Field("gst_register") gst_register: String?,
        @Field("gst_tin_number") gst_tin_number: String?
    ): NewOrganizationModel

    @FormUrlEncoded
    @POST("add/company_branch")
    suspend fun addCompanyBranch(
        @Header("Authorization") token: String?,
        @Field("branch_name") branch_name: String?,
        @Field("branch_code") branch_code: String?,
        @Field("branch_address") branch_address: String?,
        @Field("branch_contact_no") branch_contact_no: String?,
        @Field("secondary_contact") secondary_contact: String?,
        @Field("contact_person_fname") contact_person_fname: String?,
        @Field("contact_person_lname") contact_person_lname: String?,
        @Field("branch_email") branch_email: String?,
        @Field("business_location") business_location: String?,
        @Field("state_id") state_id: String?,
        @Field("city_id") city_id: String?,
        @Field("area") area: String?,
        @Field("landmark") landmark: String?,
        @Field("gst_register") gst_register: String?,
        @Field("gst_tin_number") gst_tin_number: String?,
        @Field("pincode") pincode: String?
    ): NewCompanyBranchModel

    @FormUrlEncoded
    @POST("update/company_branch")
    suspend fun updateCompanyBranch(
        @Header("Authorization") token: String?,
        @Field("branch_name") branch_name: String?,
        @Field("branch_id") branch_id: String?,
        @Field("branch_code") branch_code: String?,
        @Field("branch_address") branch_address: String?,
        @Field("branch_contact_no") branch_contact_no: String?,
        @Field("secondary_contact") secondary_contact: String?,
        @Field("contact_person_fname") contact_person_fname: String?,
        @Field("contact_person_lname") contact_person_lname: String?,
        @Field("branch_email") branch_email: String?,
        @Field("business_location") business_location: String?,
        @Field("state_id") state_id: String?,
        @Field("city_id") city_id: String?,
        @Field("area") area: String?,
        @Field("landmark") landmark: String?,
        @Field("term_balance") term_balance: String?,
        @Field("gst_register") gst_register: String?,
        @Field("gst_tin_number") gst_tin_number: String?,
        @Field("pincode") pincode: String?
    ): UpdateBranchModel


    @FormUrlEncoded
    @POST("contact/transaction/details")
    suspend fun transactionHistory(
        @Header("Authorization") token: String?,
        @Field("contact_id") contact_id: String?,
        @Field("page") current_page: Int?,
        @Field("date_range_from") date_range_from: String?,
        @Field("date_range_to") date_range_to: String?

    ): TransactionHistoryModel


    @FormUrlEncoded
    @POST("update/user_company")
    suspend fun updateUserCompany(
        @Header("Authorization") token: String?,
        @Field("company_id") company_id: Number?,
        @Field("company_name") company_name: String?,
        @Field("reg_address") reg_address: String?,
        @Field("area") area: String?,
        @Field("landmark") landmark: String?,
        @Field("business_location") country_id: String?,
        @Field("state_id") state_id: String?,
        @Field("city_id") city_id: String?,
        @Field("postal_code") postal_code: String?,
        @Field("contact_person_first_name") contact_person_first_name: String?,
        @Field("contact_person_last_name") contact_person_last_name: String?,
        @Field("mobile_number") mobile_number: String?,
        @Field("alternate_number") alternate_number: String?,
        @Field("email") email: String?,
        @Field("fiscal_year_id") fiscal_year_id: String?,
        @Field("pan_number") pan_number: String?,
        @Field("cin_number") cin_number: String?,
        @Field("term_balance") term_balance: String?,
        @Field("default_term") default_term: String?,
        @Field("gst_register") gst_register: String?,
        @Field("gst_tin_number") gst_tin_number: String?
    ): UpdateOrganizationModel

    @FormUrlEncoded
    @POST("update/customer")
    suspend fun updateCustomer(
        @Header("Authorization") token: String?,
        @Field("customer_id") customer_id: String?,
        @Field("company_id") company_id: String?,
        @Field("customer_type") customer_type: String?,
        @Field("title") title: String?,
        @Field("first_name") first_name: String?,
        @Field("last_name") last_name: String?,
        @Field("company_name") company_name: String?,
        @Field("customer_code") customer_code: String?,
        @Field("display_name") display_name: String?,
        @Field("mobile_number") mobile_number: String?,
        @Field("secondary_contact") secondary_contact: String?,
        @Field("email") email: String?,
        @Field("opening_fine_balance") opening_fine_balance: String?,
        @Field("opening_fine_default_term") opening_fine_default_term: String?,
        @Field("opening_silver_fine_balance") opening_silver_fine_balance: String?,
        @Field("opening_silver_fine_default_term") opening_silver_fine_default_term: String?,
        @Field("opening_cash_balance") opening_cash_balance: String?,
        @Field("opening_cash_default_term") opening_cash_default_term: String?,
        @Field("fine_limit") fine_limit: String?,
        @Field("cash_limit") cash_limit: String?,
        @Field("is_tcs_applicable") is_tcs_applicable: String?,
        @Field("gst_register") gst_register: String?,
        @Field("gst_treatment") gst_treatment: String?,
        @Field("gst_tin_number") gst_tin_number: String?,
        @Field("pan_number") pan_number: String?,
        @Field("courier") courier: String?,
        @Field("notes") notes: String?,
        @Field("is_shipping") is_shipping: String?,
        @Field("billing_address") billing_address: String?,
        @Field("shipping_address") shipping_address: String?,
        @Field("is_tds_applicable") is_tds_applicable: String?,
        @Field("tds_deductor_type") tds_deductor_type: String?,
        @Field("tcs_collector_type") tcs_collector_type: String?,
        @Field("nature_of_good_name") nature_of_good_name: String?,
        @Field("nature_of_payment_name") nature_of_payment_name: String?,
        @Field("nature_of_payment") nature_of_payment: String?,
        @Field("nature_of_goods") nature_of_good_id: String?
    ): NewCustomerModel


    @FormUrlEncoded
    @POST("get/customers")
    suspend fun searchListCustomer(
        @Header("Authorization") token: String?,
        @Field("status") status: String?,
        @Field("page") offset: Int?,
        @Field("search") name: String?,
        @Field("sort_by_column") sort_by_column: String?,
        @Field("sort_type") sort_type: String?
    ): SearchListCustomerModel

    /*@FormUrlEncoded
    @POST("search/customers")
    suspend fun searchListCustomer(@Header("Authorization") token: String?,
                                   @Field("company_id") company_id: String?): SearchListCustomerModel*/


    @FormUrlEncoded
    @POST("customers/details")
    suspend fun customerDetail(
        @Header("Authorization") token: String?,
        @Field("company_id") company_id: String?,
        @Field("customer_id") customer_id: String?
    ): CustomerDetailModel

    @FormUrlEncoded
    @POST("change_status/customers")
    suspend fun changeStatusCustomers(
        @Header("Authorization") token: String?,
        @Field("customer_id") customer_id: String?,
        @Field("status") status: String?
    ): ChangeStatusCustomerModel

    @FormUrlEncoded
    @POST("change_status/vendors")
    suspend fun changeStatusSupplier(
        @Header("Authorization") token: String?,
        @Field("vendor_id") vendor_id: String?,
        @Field("status") status: String?
    ): ChangeStatusSupplierModel

    @FormUrlEncoded
    @POST("item/changeitemstatus")
    suspend fun changeStatusItem(
        @Header("Authorization") token: String?,
        @Field("item_id") item_id: String?,
        @Field("status") status: String?
    ): ChangeStatusItemModel

    @FormUrlEncoded
    @POST("change_status/company_branch")
    suspend fun changeStatusBranch(
        @Header("Authorization") token: String?,
        @Field("branch_id") branch_id: String?,
        @Field("status") status: String?
    ): ChangeStatusBranchModel

    @FormUrlEncoded
    @POST("change_status/category")
    suspend fun changeStatusItemCategory(
        @Header("Authorization") token: String?,
        @Field("item_category_id") item_category_id: String?,
        @Field("status") status: String?
    ): ChangeStatusItemCategoryModel

    @FormUrlEncoded
    @POST("add/vendor")
    suspend fun addSupplier(
        @Header("Authorization") token: String?,
        @Field("company_id") company_id: String?,
        @Field("customer_type") customer_type: String?,
        @Field("title") title: String?,
        @Field("first_name") first_name: String?,
        @Field("last_name") last_name: String?,
        @Field("company_name") company_name: String?,
        @Field("supplier_code") customer_code: String?,
        @Field("mobile_number") mobile_number: String?,
        @Field("secondary_contact") secondary_contact: String?,
        @Field("email") email: String?,
        @Field("opening_fine_balance") opening_fine_balance: String?,
        @Field("opening_fine_default_term") opening_fine_default_term: String?,
        @Field("opening_silver_fine_balance") opening_silver_fine_balance: String?,
        @Field("opening_silver_fine_default_term") opening_silver_fine_default_term: String?,
        @Field("opening_cash_balance") opening_cash_balance: String?,
        @Field("opening_cash_default_term") opening_cash_default_term: String?,
        @Field("fine_limit") fine_limit: String?,
        @Field("cash_limit") cash_limit: String?,
        @Field("is_tcs_applicable") is_tcs_applicable: String?,
        @Field("gst_register") gst_register: String?,
        @Field("gst_treatment") gst_treatment: String?,
        @Field("gst_tin_number") gst_tin_number: String?,
        @Field("pan_number") pan_number: String?,
        @Field("courier") courier: String?,
        @Field("notes") notes: String?,
        @Field("is_shipping") is_shipping: String?,
        @Field("billing_address") billing_address: String?,
        @Field("shipping_address") shipping_address: String?,
        @Field("display_name") display_name: String?,
        @Field("is_tds_applicable") is_tds_applicable: String?,
        @Field("tds_deductor_type") tds_deductor_type: String?,
        @Field("tcs_collector_type") tcs_collector_type: String?,
        @Field("nature_of_good_name") nature_of_good_name: String?,
        @Field("nature_of_payment_name") nature_of_payment_name: String?,
        @Field("nature_of_payment") nature_of_payment: String?,
        @Field("nature_of_goods") nature_of_good_id: String?
    ): NewSupplierModel


    @FormUrlEncoded
    @POST("update/vendor")
    suspend fun updateSupplier(
        @Header("Authorization") token: String?,
        @Field("vendor_id") vendor_id: String?,
        @Field("company_id") company_id: String?,
        @Field("customer_type") customer_type: String?,
        @Field("title") title: String?,
        @Field("first_name") first_name: String?,
        @Field("last_name") last_name: String?,
        @Field("company_name") company_name: String?,
        @Field("supplier_code") supplier_code: String?,
        @Field("display_name") display_name: String?,
        @Field("mobile_number") mobile_number: String?,
        @Field("secondary_contact") secondary_contact: String?,
        @Field("email") email: String?,
        @Field("opening_fine_balance") opening_fine_balance: String?,
        @Field("opening_fine_default_term") opening_fine_default_term: String?,
        @Field("opening_silver_fine_balance") opening_silver_fine_balance: String?,
        @Field("opening_silver_fine_default_term") opening_silver_fine_default_term: String?,
        @Field("opening_cash_balance") opening_cash_balance: String?,
        @Field("opening_cash_default_term") opening_cash_default_term: String?,
        @Field("fine_limit") fine_limit: String?,
        @Field("cash_limit") cash_limit: String?,
        @Field("is_tcs_applicable") is_tcs_applicable: String?,
        @Field("gst_register") gst_register: String?,
        @Field("gst_treatment") gst_treatment: String?,
        @Field("gst_tin_number") gst_tin_number: String?,
        @Field("pan_number") pan_number: String?,
        @Field("courier") courier: String?,
        @Field("notes") notes: String?,
        @Field("is_shipping") is_shipping: String?,
        @Field("billing_address") billing_address: String?,
        @Field("shipping_address") shipping_address: String?,
        @Field("is_tds_applicable") is_tds_applicable: String?,
        @Field("tds_deductor_type") tds_deductor_type: String?,
        @Field("tcs_collector_type") tcs_collector_type: String?,
        @Field("nature_of_good_name") nature_of_good_name: String?,
        @Field("nature_of_payment_name") nature_of_payment_name: String?,
        @Field("nature_of_payment") nature_of_payment: String?,
        @Field("nature_of_goods") nature_of_good_id: String?
    ): NewSupplierModel

    @FormUrlEncoded
    @POST("get/vendor")
    suspend fun getListSupplier(
        @Header("Authorization") token: String?,
        @Field("status") status: String?,
        @Field("page") current_page: Int?,
        @Field("search") name: String?,
        @Field("sort_by_column") sort_by_column: String?,
        @Field("sort_type") sort_type: String?
    ): GetListSupplierModel

    @FormUrlEncoded
    @POST("vendor/details")
    suspend fun supplierDetail(
        @Header("Authorization") token: String?,
        @Field("company_id") company_id: String?,
        @Field("vendor_id") supplier_id: String?
    ): SupplierDetailModel

    @FormUrlEncoded
    @POST("get/category")
    suspend fun getItemCategory(
        @Header("Authorization") token: String?,
        @Field("offset") offset: String?
    ): ItemCategoryModel

    @FormUrlEncoded
    @POST("report_support/itemCategories")
    suspend fun getReportsItemCategories(
        @Header("Authorization") token: String?,
        @Field("search") search: String?
    ): ReportsItemCategoryModel

    @FormUrlEncoded
    @POST("report_support/items")
    suspend fun getReportsItems(
        @Header("Authorization") token: String?,
        @Field("search") search: String?,
        @Field("item_category_id") item_category_id: String?
    ): ReportsItemModel


    @FormUrlEncoded
    @POST("search/category")
    suspend fun searchItemCategory(
        @Header("Authorization") token: String?,
        @Field("offset") offset: String?
    ): ActiveCategoriesModel


    @FormUrlEncoded
    @POST("get/unit")
    suspend fun getItemUnit(
        @Header("Authorization") token: String?,
        @Field("company_id") company_id: String?
    ): ItemUnitModel

    @GET("dropdown/gst")
    suspend fun getItemGSTMenu(
        @Header("Authorization") token: String?
    ): ItemGSTMenuModel


    @GET("dropdown/unit")
    suspend fun getItemUnitMenu(
        @Header("Authorization") token: String?
    ): ItemUnitMenuModel

    @GET("dropdown/maintainstockin")
    suspend fun getMaintainStock(
        @Header("Authorization") token: String?
    ): MaintainStockModel


    @GET("dropdown/metaltype")
    suspend fun getMetalType(
        @Header("Authorization") token: String?
    ): MetalTypeModel


    @GET("dropdown/stamp")
    suspend fun getItemStamp(
        @Header("Authorization") token: String?,
        @Query("item_id") item_id: String?
    ): ItemStampModel

    @FormUrlEncoded
    @POST("sales/getTagDetails")
    suspend fun getTagDetails(
        @Header("Authorization") token: String?,
        @Field("tag_number") tag_number: String?
    ): TagDetailItemModel

    @FormUrlEncoded
    @POST("get/vendors")
    suspend fun getItemVendors(
        @Header("Authorization") token: String?,
        @Field("company_id") company_id: String?
    ): ItemVendorModel


    @Multipart
    @POST("item/add")
    suspend fun addNewItem(
        @Header("Authorization") token: String?,
        @Part("item_type") item_type: RequestBody?,
        @Part("item_name") item_name: RequestBody?,
        @Part("item_code") item_code: RequestBody?,
        @Part("category_id") category_id: RequestBody?,
        @Part("notes") notes: RequestBody?,
        @Part("metal_type_id") metal_type_id: RequestBody?,
        @Part("maintain_stock_in_id") maintain_stock_in_id: RequestBody?,
        @Part("unit_id") unit_id: RequestBody?,
        @Part("is_studded") is_studded: RequestBody?,
        @Part("stock_method") stock_method: RequestBody?,
        @Part("tax_preference") tax_preference: RequestBody?,
        @Part("sales_wastage") sales_wastage: RequestBody?,
        @Part("sales_making_charges") sales_making_charges: RequestBody?,
        @Part("purchase_wastage") purchase_wastage: RequestBody?,
        @Part("purchase_making_charges") purchase_making_charges: RequestBody?,
        @Part("jobwork_rate") jobwork_rate: RequestBody?,
        @Part("labourwork_rate") labourwork_rate: RequestBody?,
        @Part("sales_purchase_gst_rate_id") sales_purchase_gst_rate_id: RequestBody?,
        @Part("sales_purchase_hsn") sales_purchase_hsn: RequestBody?,
        @Part("jobwork_labourwork_gst_rate_id") jobwork_labourwork_gst_rate_id: RequestBody?,
        @Part("jobwork_labourwork_sac") jobwork_labourwork_sac: RequestBody?,
        @Part("sales_rate") sales_rate: RequestBody?,
        @Part("purchase_rate") purchase_rate: RequestBody?,
        @Part("sales_ledger_id") sales_ledger_id: RequestBody?,
        @Part("purchase_ledger_id") purchase_ledger_id: RequestBody?,
        @Part("jobwork_ledger_id") jobwork_ledger_id: RequestBody?,
        @Part("labourwork_ledger_id") labourwork_ledger_id: RequestBody?,
        @Part("discount_ledger_id") discount_ledger_id: RequestBody?,
        @Part("tag_prefix") tag_prefix: RequestBody?,
        @Part("use_stamp") use_stamp: RequestBody?,
        @Part("use_gold_color") use_gold_color: RequestBody?,
        @Part("min_stock_level_gm") min_stock_level_gm: RequestBody?,
        @Part("min_stock_level_pcs") min_stock_level_pcs: RequestBody?,
        @Part("max_stock_level_gm") max_stock_level_gm: RequestBody?,
        @Part("max_stock_level_pcs") max_stock_level_pcs: RequestBody?,
        @Part("product_wt") product_wt: RequestBody?,
        @Part("item_rate") item_rate: RequestBody?,
        @Part("vendor_id") vendor_id: RequestBody?,
        @Part("gold_colour") gold_colour: RequestBody?,
        @Part item_image: MultipartBody.Part?
    ): NewItemModel

    @Multipart
    @POST("item/edit")
    suspend fun editItem(
        @Header("Authorization") token: String?,
        @Part("item_id") item_id: RequestBody?,
        @Part("item_type") item_type: RequestBody?,
        @Part("item_name") item_name: RequestBody?,
        @Part("item_code") item_code: RequestBody?,
        @Part("category_id") category_id: RequestBody?,
        @Part("notes") notes: RequestBody?,
        @Part("metal_type_id") metal_type_id: RequestBody?,
        @Part("maintain_stock_in_id") maintain_stock_in_id: RequestBody?,
        @Part("unit_id") unit_id: RequestBody?,
        @Part("is_studded") is_studded: RequestBody?,
        @Part("stock_method") stock_method: RequestBody?,
        @Part("tax_preference") tax_preference: RequestBody?,
        @Part("sales_wastage") sales_wastage: RequestBody?,
        @Part("sales_making_charges") sales_making_charges: RequestBody?,
        @Part("purchase_wastage") purchase_wastage: RequestBody?,
        @Part("purchase_making_charges") purchase_making_charges: RequestBody?,
        @Part("jobwork_rate") jobwork_rate: RequestBody?,
        @Part("labourwork_rate") labourwork_rate: RequestBody?,
        @Part("sales_purchase_gst_rate_id") sales_purchase_gst_rate_id: RequestBody?,
        @Part("sales_purchase_hsn") sales_purchase_hsn: RequestBody?,
        @Part("jobwork_labourwork_gst_rate_id") jobwork_labourwork_gst_rate_id: RequestBody?,
        @Part("jobwork_labourwork_sac") jobwork_labourwork_sac: RequestBody?,
        @Part("sales_rate") sales_rate: RequestBody?,
        @Part("purchase_rate") purchase_rate: RequestBody?,
        @Part("sales_ledger_id") sales_ledger_id: RequestBody?,
        @Part("purchase_ledger_id") purchase_ledger_id: RequestBody?,
        @Part("jobwork_ledger_id") jobwork_ledger_id: RequestBody?,
        @Part("labourwork_ledger_id") labourwork_ledger_id: RequestBody?,
        @Part("discount_ledger_id") discount_ledger_id: RequestBody?,
        @Part("tag_prefix") tag_prefix: RequestBody?,
        @Part("use_stamp") use_stamp: RequestBody?,
        @Part("use_gold_color") use_gold_color: RequestBody?,
        @Part("min_stock_level_gm") min_stock_level_gm: RequestBody?,
        @Part("min_stock_level_pcs") min_stock_level_pcs: RequestBody?,
        @Part("max_stock_level_gm") max_stock_level_gm: RequestBody?,
        @Part("max_stock_level_pcs") max_stock_level_pcs: RequestBody?,
        @Part("product_wt") product_wt: RequestBody?,
        @Part("item_rate") item_rate: RequestBody?,
        @Part("vendor_id") vendor_id: RequestBody?,
        @Part("gold_colour") gold_colour: RequestBody?,
        @Part item_image: MultipartBody.Part?
    ): NewItemModel


    @FormUrlEncoded
    @POST("item/get")
    suspend fun getItemList(
        @Header("Authorization") token: String?,
        @Field("status") status: String?,
        @Field("page") current_page: Int?,
        @Field("search") search: String?,
        @Field("sort_by_column") sort_by_column: String?,
        @Field("sort_type") sort_type: String?
    ): GetItemListModel

    @FormUrlEncoded
    @POST("item/detail")
    suspend fun getItemDetails(
        @Header("Authorization") token: String?,
        @Field("item_id") item_id: String?
    ): ItemDetailModel

    @Multipart
    @POST("item/update")
    suspend fun updateItem(
        @Header("Authorization") token: String?,
        @Part("company_id") company_id: RequestBody?,
        @Part("item_id") item_id: RequestBody?,
        @Part("item_name") item_name: RequestBody?,
        @Part("item_code") item_code: RequestBody?,
        @Part("item_stock_type") item_stock_type: RequestBody?,
        @Part("category_id") category_id: RequestBody?,
        @Part("unit") unit: RequestBody?,
        @Part("vendor_id") vendor_id: RequestBody?,
        @Part("sales_wastage") sales_wastage: RequestBody?,
        @Part("sales_making_charge") sales_making_charge: RequestBody?,
        @Part("purchase_wastage") purchase_wastage: RequestBody?,
        @Part("purchase_making_charge") purchase_making_charge: RequestBody?,
        @Part("opening_stocks") opening_stocks: RequestBody?,
//        @Part("quantity") quantity: RequestBody?,
//        @Part("gross_wt") gross_wt: RequestBody?,
//        @Part("less_wt") less_wt: RequestBody?,
//        @Part("touch") touch: RequestBody?,
//        @Part("making_charge") making_charge: RequestBody?,
        @Part("notes") notes: RequestBody?,
        @Part("default_image_index") default_image_index: RequestBody?,
        @Part("show_in_sales") show_in_sales: RequestBody?,
        @Part("show_in_purchase") show_in_purchase: RequestBody?,
        @Part("is_raw_material") is_raw_material: RequestBody?,
        @Part("colour_id") colour_id: RequestBody?,
        @Part("hsn_code") hsn_code: RequestBody?,
        @Part("minimum_stock_level") minimum_stock_level: RequestBody?,
        @Part("maximum_stock_level") maximum_stock_level: RequestBody?,
        @Part item_image: MultipartBody.Part?
    ): NewItemModel

    @FormUrlEncoded
    @POST("search/items")
    suspend fun getSearchItem(
        @Header("Authorization") token: String?,
        @Field("search") search: String?,
        @Field("offset") offset: String?,
        @Field("transaction_type") transaction_type: String?,
        @Field("transaction_id") transaction_id: String?
    ): SearchItemModule


    @FormUrlEncoded
    @POST("item/search")
    suspend fun getItemSearch(
        @Header("Authorization") token: String?,
        @Field("item_name") item_name: String?,
        @Field("module") module: String?,
        @Field("item_type") item_type: String?
    ): ItemSearchModel


    @POST("getDefaultTerm")
    suspend fun getDefaultTerm(
        @Header("Authorization") token: String?
    ): ItemDefaultTermModel


    @FormUrlEncoded
    @POST("search/ledger")
    suspend fun getSearchLedger(
        @Header("Authorization") token: String?,
        @Field("type") type: String?
    ): SearchLedgerModel

    @FormUrlEncoded
    @POST("sales/calculate")
    suspend fun getCalculateItem(
        @Header("Authorization") token: String?,
        @Field("calculate") calculate: String?,
        @Field("contact_id") contact_id: String?,
        @Field("transaction_id") transaction_id: String?,
        @Field("item_json") item: String?,
        @Field("issue_receive_transaction") issue_receive_transaction: String?,
        @Field("is_gst_applicable") is_gst_applicable: String?,
        @Field("tds_percentage") tds_percentage: String?,
        @Field("tcs_percentage") tcs_percentage: String?,
        @Field("place_of_supply") place_of_supply: String?,
        @Field("tds_tcs_enable") tds_tcs_enable: String?,
        @Field("sgst_ledger_id") sgst_ledger_id: String?,
        @Field("cgst_ledger_id") cgst_ledger_id: String?,
        @Field("igst_ledger_id") igst_ledger_id: String?,
        @Field("tcs_ledger_id") tcs_ledger_id: String?,
        @Field("round_off_ledger_id") round_off_ledger_id: String?,
        @Field("round_off_total") round_off_total: String?,
        @Field("transaction_date") transaction_date: String?
    ): CalculateSalesModel

    @FormUrlEncoded
    @POST("receipt/calculate")
    suspend fun getCalculateItemReceipt(
        @Header("Authorization") token: String?,
        @Field("item_json") item: String?,
        @Field("contact_id") contact_id: String?,
        @Field("ledger_contact_type") ledger_contact_type: String?,
        @Field("transaction_id") transaction_id: String?,
        @Field("transaction_date") transaction_date: String?,
        @Field("issue_receive_transaction") issue_receive_transaction: String?
    ): CalculationReceiptModel




    @FormUrlEncoded
    @POST("payment/calculate")
    suspend fun getCalculateItemPayment(
        @Header("Authorization") token: String?,
        @Field("item_json") item: String?,
        @Field("contact_id") contact_id: String?,
        @Field("ledger_contact_type") ledger_contact_type: String?,
        @Field("transaction_id") transaction_id: String?,
        @Field("transaction_date") transaction_date: String?,
        @Field("issue_receive_transaction") issue_receive_transaction: String?
    ): CalculationReceiptModel

    /* @FormUrlEncoded
     @POST("payment/calculateReceiptTotal")
     suspend fun getCalculateItemReceipt(@Header("Authorization") token: String?,
                                         @Field("contact_id") vendor_id: String?,
                                         @Field("item") item: String?): CalculationReceiptModel*/


    @FormUrlEncoded
    @POST("purchase/calculate")
    suspend fun getCalculateItemPurchase(
        @Header("Authorization") token: String?,
        @Field("calculate") calculate: String?,
        @Field("contact_id") contact_id: String?,
        @Field("transaction_id") transaction_id: String?,
        @Field("item_json") item: String?,
        @Field("issue_receive_transaction") issue_receive_transaction: String?,
        @Field("is_gst_applicable") is_gst_applicable: String?,
        @Field("tds_percentage") tds_percentage: String?,
        @Field("tcs_percentage") tcs_percentage: String?,
        @Field("place_of_supply") place_of_supply: String?,
        @Field("tds_tcs_enable") tds_tcs_enable: String?,
        @Field("sgst_ledger_id") sgst_ledger_id: String?,
        @Field("cgst_ledger_id") cgst_ledger_id: String?,
        @Field("igst_ledger_id") igst_ledger_id: String?,
        @Field("tcs_ledger_id") tcs_ledger_id: String?,
        @Field("round_off_ledger_id") round_off_ledger_id: String?,
        @Field("round_off_total") round_off_total: String?,
        @Field("transaction_date") transaction_date: String?
    ): CalculateSalesModel

    @Multipart
    @POST("sales/add")
    suspend fun addNewInvoice(
        @Header("Authorization") token: String?,
        @Part("transaction_type_id") transaction_type_id: RequestBody?,
        @Part("transaction_type_name") transaction_type_name: RequestBody?,
        @Part("transaction_date") transaction_date: RequestBody?,
        @Part("customer_code") customer_code: RequestBody?,
        @Part("display_name") display_name: RequestBody?,
        @Part("contact_id") contact_id: RequestBody?,
        @Part("party_po_no") party_po_no: RequestBody?,
        @Part("reference") reference: RequestBody?,
        @Part("remarks") remarks: RequestBody?,
        @Part("invoice_number") invoice_number: RequestBody?,
        @Part("item_json") item_json: RequestBody?,
        @Part("issue_receive_transaction") issue_receive_transaction: RequestBody?,
        @Part("place_of_supply") place_of_supply: RequestBody?,
        @Part("sgst_ledger_id") sgst_ledger_id: RequestBody?,
        @Part("cgst_ledger_id") cgst_ledger_id: RequestBody?,
        @Part("igst_ledger_id") igst_ledger_id: RequestBody?,
        @Part("tds_ledger_id") tds_ledger_id: RequestBody?,
        @Part("tds_percentage") tds_percentage: RequestBody?,
        @Part("tcs_ledger_id") tcs_ledger_id: RequestBody?,
        @Part("tcs_percentage") tcs_percentage: RequestBody?,
        @Part("tds_tcs_enable") tds_tcs_enable: RequestBody?,
        @Part("round_off_ledger_id") round_off_ledger_id: RequestBody?,
        @Part("round_off_total") round_off_total: RequestBody?,
        @Part("branch_type") branch_type: RequestBody?,
        @Part("ledger_id") ledger_id: RequestBody?,
        @Part image: MultipartBody.Part?,
        @Part("transaction_type") transaction_type: RequestBody?
    ): SearchListSalesModel

    @Multipart
    @POST("sales/edit")
    suspend fun editInvoive(
        @Header("Authorization") token: String?,
        @Part("transaction_id") transaction_id: RequestBody?,
        @Part("transaction_type_id") transaction_type_id: RequestBody?,
        @Part("transaction_type_name") transaction_type_name: RequestBody?,
        @Part("transaction_date") transaction_date: RequestBody?,
        @Part("customer_code") customer_code: RequestBody?,
        @Part("display_name") display_name: RequestBody?,
        @Part("contact_id") contact_id: RequestBody?,
        @Part("party_po_no") party_po_no: RequestBody?,
        @Part("reference") reference: RequestBody?,
        @Part("remarks") remarks: RequestBody?,
        @Part("invoice_number") invoice_number: RequestBody?,
        @Part("item_json") item_json: RequestBody?,
        @Part("issue_receive_transaction") issue_receive_transaction: RequestBody?,
        @Part("place_of_supply") place_of_supply: RequestBody?,
        @Part("sgst_ledger_id") sgst_ledger_id: RequestBody?,
        @Part("cgst_ledger_id") cgst_ledger_id: RequestBody?,
        @Part("igst_ledger_id") igst_ledger_id: RequestBody?,
        @Part("tds_ledger_id") tds_ledger_id: RequestBody?,
        @Part("tds_percentage") tds_percentage: RequestBody?,
        @Part("tcs_ledger_id") tcs_ledger_id: RequestBody?,
        @Part("tcs_percentage") tcs_percentage: RequestBody?,
        @Part("tds_tcs_enable") tds_tcs_enable: RequestBody?,
        @Part("round_off_ledger_id") round_off_ledger_id: RequestBody?,
        @Part("round_off_total") round_off_total: RequestBody?,
        @Part("branch_type") branch_type: RequestBody?,
        @Part("ledger_id") ledger_id: RequestBody?,
        @Part image: MultipartBody.Part?,
        @Part("transaction_type") transaction_type: RequestBody?
    ): SearchListSalesModel




    @Multipart
    @POST("payment/add")
    suspend fun addNewPayment(
        @Header("Authorization") token: String?,
        @Part("transaction_type_id") transaction_type_id: RequestBody?,
        @Part("transaction_type_name") transaction_type_name: RequestBody?,
        @Part("transaction_date") transaction_date: RequestBody?,
        @Part("contact_id") customer_id: RequestBody?,
        @Part("ledger_contact_type") ledger_contact_type: RequestBody?,
        @Part("invoice_number") invoice_number: RequestBody?,
        @Part("item_json") item: RequestBody?,
        @Part("issue_receive_transaction") issue_receive_transaction: RequestBody?,
        @Part("is_gst_applicable") is_gst_applicable: RequestBody?,
        @Part("party_po_no") party_po_no: RequestBody?,
        @Part("reference") reference: RequestBody?,
        @Part("remarks") remarks: RequestBody?,
        @Part image: MultipartBody.Part?
    ): SearchListPayment


    @Multipart
    @POST("receipt/add")
    suspend fun addNewReceipt(
        @Header("Authorization") token: String?,
        @Part("transaction_type_id") transaction_type_id: RequestBody?,
        @Part("transaction_type_name") transaction_type_name: RequestBody?,
        @Part("transaction_date") transaction_date: RequestBody?,
        @Part("contact_id") customer_id: RequestBody?,
        @Part("ledger_contact_type") ledger_contact_type: RequestBody?,
        @Part("invoice_number") invoice_number: RequestBody?,
        @Part("item_json") item_json: RequestBody?,
        @Part("issue_receive_transaction") issue_receive_transaction: RequestBody?,
        @Part("is_gst_applicable") is_gst_applicable: RequestBody?,
        @Part("party_po_no") party_po_no: RequestBody?,
        @Part("reference") reference: RequestBody?,
        @Part("remarks") remarks: RequestBody?,
        @Part image: MultipartBody.Part?
    ): SearchListReceipt


    @Multipart
    @POST("payment/edit")
    suspend fun editPayment(
        @Header("Authorization") token: String?,
        @Part("transaction_type_id") transaction_type_id: RequestBody?,
        @Part("transaction_type_name") transaction_type_name: RequestBody?,
        @Part("transaction_id") transaction_id: RequestBody?,
        @Part("transaction_date") transaction_date: RequestBody?,
        @Part("contact_id") customer_id: RequestBody?,
        @Part("ledger_contact_type") ledger_contact_type: RequestBody?,
        @Part("invoice_number") invoice_number: RequestBody?,
        @Part("item_json") item: RequestBody?,
        @Part("issue_receive_transaction") issue_receive_transaction: RequestBody?,
        @Part("is_gst_applicable") is_gst_applicable: RequestBody?,
        @Part("party_po_no") party_po_no: RequestBody?,
        @Part("reference") reference: RequestBody?,
        @Part("remarks") remarks: RequestBody?,
        @Part image: MultipartBody.Part?
    ): SearchListPayment

    @Multipart
    @POST("receipt/edit")
    suspend fun editReceipt(
        @Header("Authorization") token: String?,
        @Part("transaction_id") transaction_id: RequestBody?,
        @Part("transaction_type_id") transaction_type_id: RequestBody?,
        @Part("transaction_type_name") transaction_type_name: RequestBody?,
        @Part("transaction_date") transaction_date: RequestBody?,
        @Part("contact_id") customer_id: RequestBody?,
        @Part("ledger_contact_type") ledger_contact_type: RequestBody?,
        @Part("invoice_number") invoice_number: RequestBody?,
        @Part("item_json") item: RequestBody?,
        @Part("issue_receive_transaction") issue_receive_transaction: RequestBody?,
        @Part("is_gst_applicable") is_gst_applicable: RequestBody?,
        @Part("party_po_no") party_po_no: RequestBody?,
        @Part("reference") reference: RequestBody?,
        @Part("remarks") remarks: RequestBody?,
        @Part image: MultipartBody.Part?
    ): SearchListReceipt


    @Multipart
    @POST("purchase/add")
    suspend fun addPBM(
        @Header("Authorization") token: String?,
        @Part("transaction_type_id") transaction_type_id: RequestBody?,
        @Part("transaction_type_name") transaction_type_name: RequestBody?,
        @Part("transaction_date") transaction_date: RequestBody?,
        @Part("customer_code") customer_code: RequestBody?,
        @Part("display_name") display_name: RequestBody?,
        @Part("contact_id") contact_id: RequestBody?,
        @Part("party_po_no") party_po_no: RequestBody?,
        @Part("reference") reference: RequestBody?,
        @Part("remarks") remarks: RequestBody?,
        @Part("invoice_number") invoice_number: RequestBody?,
        @Part("item_json") item_json: RequestBody?,
        @Part("issue_receive_transaction") issue_receive_transaction: RequestBody?,
        @Part("place_of_supply") place_of_supply: RequestBody?,
        @Part("sgst_ledger_id") sgst_ledger_id: RequestBody?,
        @Part("cgst_ledger_id") cgst_ledger_id: RequestBody?,
        @Part("igst_ledger_id") igst_ledger_id: RequestBody?,
        @Part("tds_ledger_id") tds_ledger_id: RequestBody?,
        @Part("tds_percentage") tds_percentage: RequestBody?,
        @Part("tcs_ledger_id") tcs_ledger_id: RequestBody?,
        @Part("tcs_percentage") tcs_percentage: RequestBody?,
        @Part("tds_tcs_enable") tds_tcs_enable: RequestBody?,
        @Part("round_off_ledger_id") round_off_ledger_id: RequestBody?,
        @Part("round_off_total") round_off_total: RequestBody?,
        @Part("branch_type") branch_type: RequestBody?,
        @Part("ledger_id") ledger_id: RequestBody?,
        @Part image: MultipartBody.Part?,
        @Part("transaction_type") transaction_type: RequestBody?
    ): SearchListPurchaseModel

    @FormUrlEncoded
    @POST("search/customers")
    suspend fun getSearchCustomer(
        @Header("Authorization") token: String?,
        @Field("company_id") company_id: String,
        @Field("search") search: String?,
        @Field("offset") offset: String?
    ): SearchCustomerModel

    @FormUrlEncoded
    @POST("search/vendors")
    suspend fun getSearchVendor(
        @Header("Authorization") token: String?,
        @Field("company_id") company_id: String,
        @Field("search") search: String?,
        @Field("offset") offset: String?,
        @Field("transaction_type") transaction_type: String?
    ): SearchVendorModel

    @FormUrlEncoded
    @POST("report_support/contacts")
    suspend fun reportSupportContacts(
        @Header("Authorization") token: String?,
        @Field("company_id") company_id: String,
        @Field("search") search: String?,
        @Field("offset") offset: String?
    ): ReportSupportContactsModel

    @FormUrlEncoded
    @POST("search/contacts")
    suspend fun getSearchContacts(
        @Header("Authorization") token: String?,
        @Field("company_id") company_id: String,
        @Field("search") search: String?,
        @Field("offset") offset: String?,
        @Field(" transaction_id") transaction_id: String?
    ): SearchContactModel


    @POST("ledgerContactDD")
    suspend fun getSearchContactsLedger(
        @Header("Authorization") token: String?
    ): SearchContactLedgerModel

    @FormUrlEncoded
    @POST("sales/get")
    suspend fun searchListSales(
        @Header("Authorization") token: String?,
        /*@Field("company_id") company_id: String?,*/
        @Field("page") current_page: Int?,
        @Field("search") name: String?,
        @Field("sort_by_column") sort_by_column: String?,
        @Field("sort_type") sort_type: String?,
        @Field("date_range_from") date_range_from: String?,
        @Field("date_range_to") date_range_to: String?
    ): SearchListSalesModel

    @FormUrlEncoded
    @POST("get/user_company")
    suspend fun getUserCompanies(
        @Header("Authorization") token: String?,
        @Field("company_id") company_id: String?
    )
            : GetUserCompaniesModel

//    @POST("get/category")
//    suspend fun getItemCategories(@Header("Authorization") token: String?)
//            : GetItemCategoriesModel

    @FormUrlEncoded
    @POST("transaction/reference")
    suspend fun transactionReference(
        @Header("Authorization") token: String?,
        @Field("contact_id") contact_id: String?,
        @Field("transaction_id") transaction_id: String?
    ): TrasactionReferenceModel

    @FormUrlEncoded
    @POST("get/company_branches")
    suspend fun getCompanyBranches(
        @Header("Authorization") token: String?,
        @Field("company_id") company_id: String?
    )
            : BranchListModel

    @FormUrlEncoded
    @POST("get/company_details")
    suspend fun getUserCompanyDetails(
        @Header("Authorization") token: String?,
        @Field("company_id") company_id: String?
    )
            : GetUserCompanyModel

    @FormUrlEncoded
    @POST("get/branch_details")
    suspend fun getBranchDetails(
        @Header("Authorization") token: String?,
        @Field("branch_id") branch_id: String?
    )
            : BranchDetailModel


    @FormUrlEncoded
    @POST("sales/detail")
    suspend fun saleDetail(
        @Header("Authorization") token: String?,
        @Field("transaction_id") transaction_id: String?
    ): SaleDetailModel


    @FormUrlEncoded
    @POST("{transaction_type}/text")
    /* @POST("voucherText")*/
    suspend fun voucherText(
        @Header("Authorization") token: String?,
        @Path("transaction_type") transaction_type: String?,
        @Field("transaction_id") transaction_id: String?
    ): VoucherTextModel

    @FormUrlEncoded
    @POST("payment/detail")
    suspend fun paymentDetail(
        @Header("Authorization") token: String?,
        @Field("transaction_id") transaction_id: String?
    ): ReceiptDetailModel

    @FormUrlEncoded
    @POST("receipt/detail")
    suspend fun receiptDetail(
        @Header("Authorization") token: String?,
        @Field("transaction_id") transaction_id: String?
    ): ReceiptDetailModel

    @GET("sale/invoiceNumber")
    suspend fun getInvoiceNumber(
        @Header("Authorization") token: String?,
        @Query("invoice_date") invoice_date: String?,
        @Query("transaction_id") transaction_id: String?
    ): InvoiceNumberModel

    @GET("payment/invoiceNumber")
    suspend fun getPaymentInvoiceNumber(
        @Header("Authorization") token: String?,
        @Query("invoice_date") invoice_date: String?,
        @Query("transaction_id") transaction_id: String?
    ): InvoiceNumberModel

    @GET("receipt/invoiceNumber")
    suspend fun getReceiptInvoiceNumber(
        @Header("Authorization") token: String?,
        @Query("invoice_date") invoice_date: String?,
        @Query("transaction_id") transaction_id: String?
    ): InvoiceNumberModel

    @GET("purchase/invoiceNumber")
    suspend fun getPurchaseInvoiceNoFromApi(
        @Header("Authorization") token: String?,
        @Query("invoice_date") invoice_date: String?,
        @Query("transaction_id") transaction_id: String?
    ): InvoiceNumberModel



    @Multipart
    @POST("purchase/edit")
    suspend fun editPurchase(
        @Header("Authorization") token: String?,
        @Part("transaction_id") transaction_id: RequestBody?,
        @Part("transaction_type_id") transaction_type_id: RequestBody?,
        @Part("transaction_type_name") transaction_type_name: RequestBody?,
        @Part("transaction_date") transaction_date: RequestBody?,
        @Part("customer_code") customer_code: RequestBody?,
        @Part("display_name") display_name: RequestBody?,
        @Part("contact_id") contact_id: RequestBody?,
        @Part("party_po_no") party_po_no: RequestBody?,
        @Part("reference") reference: RequestBody?,
        @Part("remarks") remarks: RequestBody?,
        @Part("invoice_number") invoice_number: RequestBody?,
        @Part("item_json") item_json: RequestBody?,
        @Part("issue_receive_transaction") issue_receive_transaction: RequestBody?,
        @Part("place_of_supply") place_of_supply: RequestBody?,
        @Part("sgst_ledger_id") sgst_ledger_id: RequestBody?,
        @Part("cgst_ledger_id") cgst_ledger_id: RequestBody?,
        @Part("igst_ledger_id") igst_ledger_id: RequestBody?,
        @Part("tds_ledger_id") tds_ledger_id: RequestBody?,
        @Part("tds_percentage") tds_percentage: RequestBody?,
        @Part("tcs_ledger_id") tcs_ledger_id: RequestBody?,
        @Part("tcs_percentage") tcs_percentage: RequestBody?,
        @Part("tds_tcs_enable") tds_tcs_enable: RequestBody?,
        @Part("round_off_ledger_id") round_off_ledger_id: RequestBody?,
        @Part("round_off_total") round_off_total: RequestBody?,
        @Part("branch_type") branch_type: RequestBody?,
        @Part("ledger_id") ledger_id: RequestBody?,
        @Part image: MultipartBody.Part?,
        @Part("transaction_type") transaction_type: RequestBody?
    ): SearchListPurchaseModel

    @FormUrlEncoded
    @POST("purchase/get")
    suspend fun searchListPurchase(
        @Header("Authorization") token: String?,
        @Field("page") current_page: Int?,
        @Field("search") name: String?,
        @Field("sort_by_column") sort_by_column: String?,
        @Field("sort_type") sort_type: String?,
        @Field("date_range_from") date_range_from: String?,
        @Field("date_range_to") date_range_to: String?
    ): SearchListPurchaseModel


    @FormUrlEncoded
    @POST("openingstocks/get")
    suspend fun searchListOpeningStocks(
        @Header("Authorization") token: String?,
        @Field("company_id") company_id: String?,
        @Field("page") current_page: Int?,
        @Field("search") name: String?,
        @Field("sort_by_column") sort_by_column: String?,
        @Field("sort_type") sort_type: String?,
        @Field("date_range_from") date_range_from: String?,
        @Field("date_range_to") date_range_to: String?
    ): SearchListOpeningStockModel

    @FormUrlEncoded
    @POST("payment/list")
    suspend fun searchListPayment(
        @Header("Authorization") token: String?,
        @Field("page") current_page: Int?,
        @Field("search") name: String?,
        @Field("sort_by_column") sort_by_column: String?,
        @Field("sort_type") sort_type: String?,
        @Field("date_range_from") date_range_from: String?,
        @Field("date_range_to") date_range_to: String?
    ): SearchListPayment

    @FormUrlEncoded
    @POST("receipt/list")
    suspend fun searchListReceipt(
        @Header("Authorization") token: String?,
        @Field("page") current_page: Int?,
        @Field("search") name: String?,
        @Field("sort_by_column") sort_by_column: String?,
        @Field("sort_type") sort_type: String?,
        @Field("date_range_from") date_range_from: String?,
        @Field("date_range_to") date_range_to: String?
    ): SearchListReceipt


    @GET("openingstocks/invoiceNumber")
    suspend fun getOpeningStockVoucherNoFromApi(
        @Header("Authorization") token: String?,
        @Query("invoice_date") invoice_date: String?,
        @Query("transaction_id") transaction_id: String?
    ): InvoiceNumberModel

    @Multipart
    @POST("openingstocks/add")
    suspend fun addOpeningStock(
        @Header("Authorization") token: String?,
        @Part("transaction_date") transaction_date: RequestBody?,
        @Part("invoice_number") invoice_number: RequestBody?,
        @Part("item_json") item_json: RequestBody?,
        @Part("remarks") remarks: RequestBody?,
        @Part("reference") reference: RequestBody?

    ): NewLedgerModel

    @Multipart
    @POST("openingstocks/edit")
    suspend fun editOpeningStock(
        @Header("Authorization") token: String?,
        @Part("transaction_id") transaction_id: RequestBody?,
        @Part("transaction_date") transaction_date: RequestBody?,
        @Part("invoice_number") invoice_number: RequestBody?,
        @Part("item_json") item_json: RequestBody?,
        @Part("remarks") remarks: RequestBody?,
        @Part("reference") reference: RequestBody?

    ): NewLedgerModel

    @FormUrlEncoded
    @POST("openingstocks/delete")
    suspend fun deleteOpeningStock(
        @Header("Authorization") token: String?,
        @Field("transaction_id") transaction_id: String?
    ): DeleteItemCategoryModel




    @Multipart
    @POST("calculate/itemscalculate")
    suspend fun getOpeningStockCalculate(
        @Header("Authorization") token: String?,
        @Part("item_json") item_json: RequestBody?
    ): OpeningStockCalcModel


    @FormUrlEncoded
    @POST("openingstocks/detail")
    suspend fun openinStockDetail(
        @Header("Authorization") token: String?,
        @Field("transaction_id") transaction_id: String?
    ): OpeningStockDetailModel


    @FormUrlEncoded
    @POST("purchase/detail")
    suspend fun purchaseDetail(
        @Header("Authorization") token: String?,
        @Field("transaction_id") transaction_id: String?
    ): SaleDetailModel

    @FormUrlEncoded
    @POST("update/goldrate")
    suspend fun updateGoldrate(
        @Header("Authorization") token: String?,
        @Field("company_id") company_id: String?,
        @Field("bill_rate") bill_rate: String?,
        @Field("cash_rate") cash_rate: String?,
        @Field("type") type: String?
    ): UpdateGoldrateModel

    @FormUrlEncoded
    @POST("company/{cms}")
    suspend fun termsOfService(/*@Header("Authorization") token: String?,*/
        @Path("cms") cms: String?,
        @Field("page_id") page_id: Int?
    ): TermsOfServiceModel

    @FormUrlEncoded
    @POST("update/password")
    suspend fun updatePassword(
        @Header("Authorization") token: String?,
        @Field("current_password") current_password: String?,
        @Field("password") password: String?,
        @Field("password_confirmation") password_confirmation: String?
    ): UpdatePasswordModel

    @FormUrlEncoded
    @POST("get/company_branches")
    suspend fun getSelectedCompanyBranches(
        @Header("Authorization") token: String?,
        @Field("company_id") company_id: String?
    ): SelectedCompanyBranchesModel


    @FormUrlEncoded
    @POST("delete/contact")
    suspend fun deleteContact(
        @Header("Authorization") token: String?,
        @Field("contact_id") contact_id: String?
    ): DeleteItemCategoryModel

    @FormUrlEncoded
    @POST("delete/item")
    suspend fun deleteItem(
        @Header("Authorization") token: String?,
        @Field("item_id") item_id: String?
    ): DeleteItemCategoryModel

    @FormUrlEncoded
    @POST("delete/company_branch")
    suspend fun deleteBranch(
        @Header("Authorization") token: String?,
        @Field("branch_id") branch_id: String?
    ): DeleteItemCategoryModel

    @FormUrlEncoded
    @POST("delete/user_company")
    suspend fun deleteCompany(
        @Header("Authorization") token: String?,
        @Field("company_id") branch_id: String?
    ): DeleteItemCategoryModel


    @FormUrlEncoded
    @POST("delete/category")
    suspend fun deleteItemCatrgory(
        @Header("Authorization") token: String?,
        @Field("item_category_id") category_id: String?
    ): DeleteItemCategoryModel

    @FormUrlEncoded
    @POST("sales/delete")
    suspend fun deleteSale(
        @Header("Authorization") token: String?,
        @Field("transaction_id") transaction_id: String?
    ): DeleteItemCategoryModel

    @FormUrlEncoded
    @POST("purchase/delete")
    suspend fun deletePurchase(
        @Header("Authorization") token: String?,
        @Field("transaction_id") transaction_id: String?
    ): DeleteItemCategoryModel

    @FormUrlEncoded
    @POST("payment/delete")
    suspend fun deletePayment(
        @Header("Authorization") token: String?,
        @Field("transaction_id") transaction_id: String?
    ): DeleteItemCategoryModel

    @FormUrlEncoded
    @POST("receipt/delete")
    suspend fun deleteReceipt(
        @Header("Authorization") token: String?,
        @Field("transaction_id") transaction_id: String?
    ): DeleteItemCategoryModel

    @FormUrlEncoded
    @Streaming
    @POST("report/download")
    fun downloadReport(
        @Header("Authorization") token: String?,
        @Field("from_date") from_date: String?,
        @Field("to_date") to_date: String?,
        @Field("report_type") report_type: String?,
        @Field("all_contacts") all_contacts: String?,
        @Field("type_of_contact") type_of_contact: String?,
        @Field("contact_id") contact_id: String?,
        @Field("all_cash_ledgers") all_cash_ledgers: String?,
        @Field("cash_ledger_id") cash_ledger_id: String?,
        @Field("all_bank_ledgers") all_bank_ledgers: String?,
        @Field("bank_ledger_id") bank_ledger_id: String?,
        @Field("period") period: String?,
        @Field("all_item_categories") all_item_categories: String?,
        @Field("item_category_id") item_category_id: String?,
        @Field("all_items") all_items: String?,
        @Field("item_id") item_id: String?,
        @Field("all_ledgers") all_ledgers: String?,
        @Field("ledger_id") ledger_id: String?

    ): Call<ResponseBody>


    @FormUrlEncoded
    @Streaming
    @POST("{transaction_type}/print")
    fun voucherPrint(
        @Header("Authorization") token: String?,
        @Path("transaction_type") transaction_type: String?,
        @Field("transaction_id") transaction_id: String?,
        @Field("report_type") report_type: String?
    ): Call<ResponseBody>

    @FormUrlEncoded
    @POST("report/print")
    suspend fun contactReportPrint(
        @Header("Authorization") token: String?,
        @Field("from_date") from_date: String?,
        @Field("to_date") to_date: String?,
        @Field("report_type") report_type: String?,
        @Field("all_contacts") all_contacts: String?,
        @Field("type_of_contact") type_of_contact: String?,
        @Field("contact_id") contact_id: String?,
        @Field("all_cash_ledgers") all_cash_ledgers: String?,
        @Field("cash_ledger_id") cash_ledger_id: String?,
        @Field("all_bank_ledgers") all_bank_ledgers: String?,
        @Field("bank_ledger_id") bank_ledger_id: String?,
        @Field("period") period: String?,
        @Field("all_item_categories") all_item_categories: String?,
        @Field("item_category_id") item_category_id: String?,
        @Field("all_items") all_items: String?,
        @Field("item_id") item_id: String?
    ): ContactPrintModel

    @FormUrlEncoded
    @POST("report/print")
    suspend fun dayReportPrint(
        @Header("Authorization") token: String?,
        @Field("from_date") from_date: String?,
        @Field("to_date") to_date: String?,
        @Field("report_type") report_type: String?,
        @Field("all_contacts") all_contacts: String?,
        @Field("type_of_contact") type_of_contact: String?,
        @Field("contact_id") contact_id: String?,
        @Field("all_cash_ledgers") all_cash_ledgers: String?,
        @Field("cash_ledger_id") cash_ledger_id: String?,
        @Field("all_bank_ledgers") all_bank_ledgers: String?,
        @Field("bank_ledger_id") bank_ledger_id: String?,
        @Field("period") period: String?,
        @Field("all_item_categories") all_item_categories: String?,
        @Field("item_category_id") item_category_id: String?,
        @Field("all_items") all_items: String?,
        @Field("item_id") item_id: String?
    ): DayPrintModel

    @FormUrlEncoded
    @POST("report/print")
    suspend fun cashbankReportPrint(
        @Header("Authorization") token: String?,
        @Field("from_date") from_date: String?,
        @Field("to_date") to_date: String?,
        @Field("report_type") report_type: String?,
        @Field("all_contacts") all_contacts: String?,
        @Field("type_of_contact") type_of_contact: String?,
        @Field("contact_id") contact_id: String?,
        @Field("all_cash_ledgers") all_cash_ledgers: String?,
        @Field("cash_ledger_id") cash_ledger_id: String?,
        @Field("all_bank_ledgers") all_bank_ledgers: String?,
        @Field("bank_ledger_id") bank_ledger_id: String?,
        @Field("period") period: String?,
        @Field("all_item_categories") all_item_categories: String?,
        @Field("item_category_id") item_category_id: String?,
        @Field("all_items") all_items: String?,
        @Field("item_id") item_id: String?
    ): CashBankPrintModel

    @FormUrlEncoded
    @POST("report/print")
    suspend fun stockReportPrint(
        @Header("Authorization") token: String?,
        @Field("from_date") from_date: String?,
        @Field("to_date") to_date: String?,
        @Field("report_type") report_type: String?,
        @Field("all_contacts") all_contacts: String?,
        @Field("type_of_contact") type_of_contact: String?,
        @Field("contact_id") contact_id: String?,
        @Field("all_cash_ledgers") all_cash_ledgers: String?,
        @Field("cash_ledger_id") cash_ledger_id: String?,
        @Field("all_bank_ledgers") all_bank_ledgers: String?,
        @Field("bank_ledger_id") bank_ledger_id: String?,
        @Field("period") period: String?,
        @Field("all_item_categories") all_item_categories: String?,
        @Field("item_category_id") item_category_id: String?,
        @Field("all_items") all_items: String?,
        @Field("item_id") item_id: String?
    ): StockPrintModel


    @FormUrlEncoded
    @POST("report/print")
    suspend fun salePurchaseReportPrint(
        @Header("Authorization") token: String?,
        @Field("from_date") from_date: String?,
        @Field("to_date") to_date: String?,
        @Field("report_type") report_type: String?,
        @Field("all_contacts") all_contacts: String?,
        @Field("type_of_contact") type_of_contact: String?,
        @Field("contact_id") contact_id: String?,
        @Field("all_cash_ledgers") all_cash_ledgers: String?,
        @Field("cash_ledger_id") cash_ledger_id: String?,
        @Field("all_bank_ledgers") all_bank_ledgers: String?,
        @Field("bank_ledger_id") bank_ledger_id: String?,
        @Field("period") period: String?,
        @Field("all_item_categories") all_item_categories: String?,
        @Field("item_category_id") item_category_id: String?,
        @Field("all_items") all_items: String?,
        @Field("item_id") item_id: String?
    ): SalesPurchasePrintModel


    @GET("app_version")
    suspend fun getAppVersion(): AppVersion

    @POST("user/limitAccess")
    suspend fun userLimitAccess(
        @Header("Authorization") token: String?
    ): UserLimitAccessModel


    @GET("get/usersPermission")
    suspend fun userWiseRestriction(
        @Header("Authorization") token: String?
    ): UserWiseRestrictionModel

    @POST("dashboard")
    suspend fun dashboardDetails(
        @Header("Authorization") token: String?
    ): DashboardDetailsModel

    @FormUrlEncoded
    @POST("user/setPin")
    suspend fun setAppLockPin(
        @Header("Authorization") token: String?,
        @Field("pin") pin: String?
    ): SetPinModel

    @FormUrlEncoded
    @POST("user/checkPin")
    suspend fun checkPin(
        @Header("Authorization") token: String?,
        @Field("pin") pin: String?
    ): CustomPinModel


    @POST("user/forgetPin")
    suspend fun forgetPin(
        @Header("Authorization") token: String?
    ): CustomPinModel

    // setting
    @POST("detail/preference")
    suspend fun getdetailPreferenceApi(
        @Header("Authorization") token: String?
    ): PreferenceDetailModel

    @FormUrlEncoded
    @POST("save/preference")
    suspend fun savePreferenceApi(
        @Header("Authorization") token: String?,
        @Field("enable_cheque_reg_for_bank_acc") enable_cheque_reg_for_bank_acc: Int?,
        @Field("round_off_for_sales") round_off_for_sales: Int?,
        @Field("default_term") default_term: String?,
        @Field("print_copies") print_copies: String?
    ): ErrorModel

    // setting contact
    @POST("detail/contact")
    suspend fun getdetailContactApi(
        @Header("Authorization") token: String?
    ): SettinContactDetailModel

    // setting contact
    @POST("detail/gst")
    suspend fun getdetailGstApi(
        @Header("Authorization") token: String?
    ): SettingsGstDetailModel

    @FormUrlEncoded
    @POST("save/contact")
    suspend fun saveContactApi(
        @Header("Authorization") token: String?,
        @Field("disable_credit_limit") disable_credit_limit: Int?,
        @Field("stop_transaction_if_limit_over") stop_transaction_if_limit_over: Int?
    ): ErrorModel


    // setting taxes (tcs)
    @GET("get/tcsCollectorType")
    suspend fun tcsCollectorType(
        @Header("Authorization") token: String?
    ): GetTcsCollectorTypeModel

    // setting taxes (tcs detail)
    @POST("detail/tcs")
    suspend fun taxTcsDetail(
        @Header("Authorization") token: String?
    ): TaxDetailTcsModel

    // setting taxes (tds)
    @GET("get/tdsDeductorType")
    suspend fun tdsDeductorType(
        @Header("Authorization") token: String?
    ): GetTcsCollectorTypeModel

    // setting taxes (tds detail)
    @POST("detail/tds")
    suspend fun taxTdsDetail(
        @Header("Authorization") token: String?
    ): TaxDetailTdsModel


    // setting taxes (gst/tcs/tds) common api for all three type taxes
    @FormUrlEncoded
    @POST("save/tax")
    suspend fun saveTaxApi(
        @Header("Authorization") token: String?,
        @Field("type") type: String?,
        @Field("enable_gst") enable_gst: Int?,
        @Field("gst_state_id") gst_state_id: String?,
        @Field("gstin") gstin: String?,
        @Field("registration_date") registration_date: String?,
        @Field("periodicity_of_gst1") periodicity_of_gst1: String?,
        @Field("enable_tcs") enable_tcs: Int?,
        @Field("enable_tds") enable_tds: Int?,
        @Field("tan_number") tan_number: String?,
        @Field("tds_circle") tds_circle: String?,
        @Field("tcs_collector_type") tcs_collector_type: String?,
        @Field("tcs_person_responsible") tcs_person_responsible: String?,
        @Field("tcs_designation") tcs_designation: String?,
        @Field("tcs_contact_number") tcs_contact_number: String?,
        @Field("nature_of_goods") nature_of_goods: String?,
        @Field("tds_deductor_type") tds_deductor_type: String?,
        @Field("tds_person_responsible") tds_person_responsible: String?,
        @Field("tds_designation") tds_designation: String?,
        @Field("tds_contact_number") tds_contact_number: String?,
        @Field("nature_of_payment") nature_of_payment: String?
    ): ErrorModel

    @FormUrlEncoded
    @POST("delete/natureOfGoods")
    suspend fun deleteNatureOfGoods(
        @Header("Authorization") token: String?,
        @Field("nature_of_goods_id") nature_of_goods_id: String?
    ): ErrorModel

    @FormUrlEncoded
    @POST("delete/natureOfPayment")
    suspend fun deleteNatureOfPayment(
        @Header("Authorization") token: String?,
        @Field("nature_of_payment_id") nature_of_payment_id: String?
    ): ErrorModel

    @FormUrlEncoded
    @POST("search/ledger")
    suspend fun searchLedger(
        @Header("Authorization") token: String?,
        @Field("type") type: String?
    ): SearchLedgerModel


    @FormUrlEncoded
    @POST("get/ledgerMaster")
    suspend fun searchListLedger(
        @Header("Authorization") token: String?,
        @Field("company_id") company_id: String?,
        @Field("page") current_page: Int?,
        @Field("search") name: String?,
        @Field("sort_by_column") sort_by_column: String?,
        @Field("sort_type") sort_type: String?
    ): SearchListLedgerModel

    @FormUrlEncoded
    @POST("add/ledgerMaster")
    suspend fun addLedgerDetails(
        @Header("Authorization") token: String?,
        @Field("name") name: String?,
        @Field("code") code: String?,
        @Field("is_sub_account") is_sub_account: String?,
        @Field("group_id") group_id: String?,
        @Field("sub_group_id") sub_group_id: String?,
        @Field("is_bank_account") is_bank_account: String?,
        @Field("bank_name") bank_name: String?,
        @Field("account_number") account_number: String?,
        @Field("ifsc_code") ifsc_code: String?,
        @Field("branch_name") branch_name: String?,
        @Field("is_duties_and_taxes") is_duties_and_taxes: String?,
        @Field("type_of_duty") type_of_duty: String?,
        @Field("is_tcs_applicable") is_tcs_applicable: String?,
        @Field("is_tds_applicable") is_tds_applicable: String?,
        @Field("nature_of_goods") nature_of_goods: String?,
        @Field("nature_of_payment") nature_of_payment: String?,
        @Field("type_of_gst") type_of_gst: String?,
        @Field("percentage_of_duty") percentage_of_duty: String?,
        @Field("bill_by_bill_reference") bill_by_bill_reference: String?,
        @Field("pan_card") pan_card: String?,
        @Field("gst_treatment") gst_treatment: String?,
        @Field("gstin") gstin: String?,
        @Field("notes") notes: String?,
        @Field("opening_balance_type") opening_balance_type: String?,
        @Field("opening_balance") opening_balance: String?,
        @Field("cheque_register_array") cheque_register_array: String?
    ): NewLedgerModel

    @FormUrlEncoded
    @POST("edit/ledgerMaster")
    suspend fun editLedgerDetails(
        @Header("Authorization") token: String?,
        @Field("ledger_id") ledger_id: String?,
        @Field("name") name: String?,
        @Field("code") code: String?,
        @Field("is_sub_account") is_sub_account: String?,
        @Field("group_id") group_id: String?,
        @Field("sub_group_id") sub_group_id: String?,
        @Field("is_bank_account") is_bank_account: String?,
        @Field("bank_name") bank_name: String?,
        @Field("account_number") account_number: String?,
        @Field("ifsc_code") ifsc_code: String?,
        @Field("branch_name") branch_name: String?,
        @Field("is_duties_and_taxes") is_duties_and_taxes: String?,
        @Field("type_of_duty") type_of_duty: String?,
        @Field("is_tcs_applicable") is_tcs_applicable: String?,
        @Field("is_tds_applicable") is_tds_applicable: String?,
        @Field("nature_of_goods") nature_of_goods: String?,
        @Field("nature_of_payment") nature_of_payment: String?,
        @Field("type_of_gst") type_of_gst: String?,
        @Field("percentage_of_duty") percentage_of_duty: String?,
        @Field("bill_by_bill_reference") bill_by_bill_reference: String?,
        @Field("pan_card") pan_card: String?,
        @Field("gst_treatment") gst_treatment: String?,
        @Field("gstin") gstin: String?,
        @Field("notes") notes: String?,
        @Field("opening_balance_type") opening_balance_type: String?,
        @Field("opening_balance") opening_balance: String?,
        @Field("cheque_register_array") cheque_register_array: String?
    ): NewLedgerModel


    @FormUrlEncoded
    @POST("detail/ledgerMaster")
    suspend fun ledgerDetail(
        @Header("Authorization") token: String?,
        @Field("ledger_id") ledger_id: String?
    ): LedgerDetailsModel

    @FormUrlEncoded
    @POST("delete/ledgerMaster")
    suspend fun deleteLedger(
        @Header("Authorization") token: String?,
        @Field("ledger_id") ledger_id: String?
    ): ErrorModel

    @FormUrlEncoded
    @POST("get/ledgerSubGroup")
    suspend fun getLedgerGroupSubGroup(
        @Header("Authorization") token: String?,
        @Field("group_id") group_id: String?
    ): LedgerGroupSubGroupModel


    @GET("get/natureOfGoods")
    suspend fun getNatureofGoods(
        @Header("Authorization") token: String?
    ): NatureOfGoodsModel

    @GET("get/natureOfPayment")
    suspend fun getNatureOfPayment(
        @Header("Authorization") token: String?
    ): NatureOfPaymentModel


    @FormUrlEncoded
    @POST("get/group")
    suspend fun searchListGroup(
        @Header("Authorization") token: String?,
        @Field("company_id") company_id: String?,
        @Field("page") current_page: Int?,
        @Field("search") name: String?,
        @Field("sort_by_column") sort_by_column: String?,
        @Field("sort_type") sort_type: String?
    ): SearchListGroupModel


    @FormUrlEncoded
    @POST("add/group")
    suspend fun addGroup(
        @Header("Authorization") token: String?,
        @Field("group_name") group_name: String?,
        @Field("ledger_group_id") ledger_group_id: String?,
        @Field("nature_group_id") nature_group_id: String?,
        @Field("affect_gross_profit") affect_gross_profit: String?,
        @Field("is_bank_account") is_bank_account: String?,
        @Field("description") description: String?,
        @Field("make_this_sub_group") make_this_sub_group: String?
    ): NewGroupModel

    @FormUrlEncoded
    @POST("detail/group")
    suspend fun groupDetail(
        @Header("Authorization") token: String?,
        @Field("group_id") group_id: String?
    ): LedgerGroupDetailModel


    @FormUrlEncoded
    @POST("delete/group")
    suspend fun deleteGroup(
        @Header("Authorization") token: String?,
        @Field("group_id") group_id: String?
    ): ErrorModel

    @FormUrlEncoded
    @POST("detail/subGroup")
    suspend fun subGroupDetail(
        @Header("Authorization") token: String?,
        @Field("sub_group_id") sub_group_id: String?
    ): LedgerSubGroupDetailModel

    @FormUrlEncoded
    @POST("delete/subGroup")
    suspend fun deleteSubGroup(
        @Header("Authorization") token: String?,
        @Field("sub_group_id") sub_group_id: String?
    ): ErrorModel

    @FormUrlEncoded
    @POST("edit/group")
    suspend fun editGroup(
        @Header("Authorization") token: String?,
        @Field("group_name") group_name: String?,
        @Field("nature_group_id") nature_group_id: String?,
        @Field("affect_gross_profit") affect_gross_profit: String?,
        @Field("is_bank_account") is_bank_account: String?,
        @Field("description") description: String?,
        @Field("group_id") group_id: String?
    ): NewGroupModel

    @FormUrlEncoded
    @POST("edit/subGroup")
    suspend fun editSubGroup(
        @Header("Authorization") token: String?,
        @Field("group_name") group_name: String?,
        @Field("ledger_group_id") ledger_group_id: String?,
        @Field("is_bank_account") is_bank_account: String?,
        @Field("description") description: String?,
        @Field("sub_group_id") sub_group_id: String?
    ): NewGroupModel


    @GET("get/nature")
    suspend fun getNatureGroup(
        @Header("Authorization") token: String?
    ): NatureGroupModel

    @GET("get/ledgerGroup")
    suspend fun getParentGroup(
        @Header("Authorization") token: String?
    ): ParentGroupModel


    @FormUrlEncoded
    @POST("add/metalColor")
    suspend fun addMetalColour(
        @Header("Authorization") token: String?,
        @Field("colour_name") colour_name: String?,
        @Field("colour_code") colour_code: String?,
        @Field("status") status: String?
    ): ErrorModel

    @FormUrlEncoded
    @POST("get/metalColor")
    suspend fun getMetalColours(
        @Header("Authorization") token: String?,
        @Field("status") status: String?
    ): MetalColourModel


    @FormUrlEncoded
    @POST("delete/metalColor")
    suspend fun deleteMetalColour(
        @Header("Authorization") token: String?,
        @Field("metal_colour_id") metal_colour_id: String?
    ): ErrorModel

    @FormUrlEncoded
    @POST("edit/metalColor")
    suspend fun editMetalColour(
        @Header("Authorization") token: String?,
        @Field("metal_colour_id") metal_colour_id: String?,
        @Field("colour_name") colour_name: String?,
        @Field("colour_code") colour_code: String?,
        @Field("status") status: Number?
    ): ErrorModel

    @FormUrlEncoded
    @POST("report/print")
    suspend fun ledgerReportPrint(
        @Header("Authorization") token: String?,
        @Field("from_date") from_date: String?,
        @Field("to_date") to_date: String?,
        @Field("report_type") report_type: String?,
        @Field("all_contacts") all_contacts: String?,
        @Field("type_of_contact") type_of_contact: String?,
        @Field("contact_id") contact_id: String?,
        @Field("all_cash_ledgers") all_cash_ledgers: String?,
        @Field("cash_ledger_id") cash_ledger_id: String?,
        @Field("all_bank_ledgers") all_bank_ledgers: String?,
        @Field("bank_ledger_id") bank_ledger_id: String?,
        @Field("period") period: String?,
        @Field("all_item_categories") all_item_categories: String?,
        @Field("item_category_id") item_category_id: String?,
        @Field("all_items") all_items: String?,
        @Field("item_id") item_id: String?,
        @Field("all_ledgers") all_ledgers: String?,
        @Field("ledger_id") ledger_id: String?
    ): CashBankPrintModel

    @FormUrlEncoded
    @POST("delete/contactaddressinfo")
    suspend fun deletecontactaddressinfo(
        @Header("Authorization") token: String?,
        @Field("type") type: String?,
        @Field("edit_id") edit_id: String?
    ): ErrorModel


}