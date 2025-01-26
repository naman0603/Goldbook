package com.goldbookapp.api

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Field
import retrofit2.http.Part

class ApiHelper(private val apiService: ApiService) {

    suspend fun getLoginData(name: String?, pass: String?) = apiService.loginAPI(name, pass)
    suspend fun logout(access_token: String?) = apiService.logoutApi(access_token)

    suspend fun userRegister(
        name: String?,
        password: String?,
        email: String?,
        mobile_no: String?,
        username: String?,
        resend: Boolean?
    ) = apiService.userRegister(name, password, email, mobile_no, username, resend)

    suspend fun userSignUP(
        name: String?,
        password: String?,
        email: String?,
        mobile_no: String?,
        resend: Boolean?
    ) = apiService.userSignUP(name, password, email, mobile_no, resend)

    suspend fun getCountry() = apiService.getCountry()

    suspend fun aboutUs() = apiService.aboutUs()
    suspend fun activeplan(access_token: String?) = apiService.activeplan(access_token)
    suspend fun webLinks() = apiService.webLinks()

    suspend fun getState(country: String?) = apiService.getState(country)

    suspend fun getCity(state: String?) = apiService.getCity(state)

    suspend fun verifyOTP(
        otp: String?,
        mobile_no: String?,
        name: String?,
        email: String?,
        password: String?,
        username: String?,
        otp_email: String?
    ) = apiService.verifyOTP(otp, mobile_no, name, email, password, username, otp_email)

    suspend fun getSuggestion(
        username: String?,
        company_id: String?
    ) = apiService.getSuggestion(username, company_id)

    suspend fun transactionHistory(
        access_token: String?,
        contact_id: String?,
        current_page: Int?,
        date_range_from: String?,
        date_range_to: String?,
    ) = apiService.transactionHistory(access_token, contact_id,current_page,date_range_from,date_range_to)


    suspend fun updateUsername(
        username: String?
    ) = apiService.updateUsername(username)

    suspend fun companySetup(
        access_token: String?,
        company_name: String?,
        business_location: String?,
        state_id: String?,
        city_id: String?,
        term_balance: String?,
        default_term: String?,
        gst_register: String?,
        gst_tin_number: String?,
        company_id: String?
    ) = apiService.companySetup(
        access_token,
        company_name,
        business_location,
        state_id,
        city_id,
        term_balance,
        default_term,
        gst_register,
        gst_tin_number,
        company_id
    )

    suspend fun forgotPassword(
        email_mobile: String?
    ) = apiService.forgotPassword(email_mobile)

    suspend fun verifyPassword(
        token: String?,
        password: String?
    ) = apiService.verifyPassword(token, password)

    suspend fun updateContact(
        token: String?,
        mobile_no: String?,
        email: String?,
        otp: String?
    ) = apiService.updateContact(token, mobile_no, email, otp)

    suspend fun profileDetail(
        token: String?
    ) = apiService.profileDetail(token)

    suspend fun userCompanyList(
        token: String?
    ) = apiService.userCompanyList(token)

    suspend fun editProfile(
        token: String?,
        name: String?,
        birthdate: String?,
        gender: String?
    ) = apiService.editProfile(token, name, birthdate, gender)

    suspend fun updateProfileImage(
        token: String?,
        profile_image: MultipartBody.Part?
    ) = apiService.updateProfileImage(token, profile_image)

    suspend fun addCustomer(
        token: String?,
        company_id: String?,
        customer_type: String?,
        title: String?,
        first_name: String?,
        last_name: String?,
        company_name: String?,
        customer_code: String?,
        display_name: String?,
        mobile_number: String?,
        secondary_contact: String?,
        email: String?,
        opening_fine_balance: String?,
        opening_fine_default_term: String?,
        opening_silver_fine_balance: String?,
        opening_silver_fine_default_term: String?,
        opening_cash_balance: String?,
        opening_cash_default_term: String?,
        fine_limit: String?,
        cash_limit: String?,
        is_tcs_applicable: String?,
        gst_register: String?,
        gst_treatment: String?,
        gst_tin_number: String?,
        pan_number: String?,
        courier: String?,
        notes: String?,
        is_shipping: String?,
        billing_address: String?,
        shipping_address: String?,
        is_tds_applicable: String?,
        tax_deductor_type: String?,
        tax_collector_type: String?,
        selectedNogType: String?,
        selectedNopType: String?,
        selectedNatureofPaymentID: String?,
        selectedNatureofGoodsID: String?
    ) = apiService.addCustomer(
        token, company_id,
        customer_type,
        title,
        first_name,
        last_name,
        company_name,
        customer_code,
        display_name,
        mobile_number,
        secondary_contact,
        email,
        opening_fine_balance,
        opening_fine_default_term,
        opening_silver_fine_balance,
        opening_silver_fine_default_term,
        opening_cash_balance,
        opening_cash_default_term,
        fine_limit,
        cash_limit,
        is_tcs_applicable,
        gst_register,
        gst_treatment,
        gst_tin_number,
        pan_number,
        courier,
        notes,
        is_shipping,
        billing_address,
        shipping_address,
        is_tds_applicable,
        tax_deductor_type,
        tax_collector_type,
        selectedNogType,
        selectedNopType,
        selectedNatureofPaymentID,
        selectedNatureofGoodsID
    )

    suspend fun addCompanyBranch(
        token: String?,
        branch_name: String?,
        branch_code: String?,
        branch_address: String?,
        branch_contact_no: String?,
        secondary_contact: String?,
        contact_person_fname: String?,
        contact_person_lname: String?,
        branch_email: String?,
        business_location: String?,
        state_id: String?,
        city_id: String?,
        area: String?,
        landmark: String?,
        gst_register: String?,
        gst_tin_number: String?,
        pincode: String?
    ) = apiService.addCompanyBranch(
        token, branch_name,
        branch_code,
        branch_address,
        branch_contact_no,
        secondary_contact,
        contact_person_fname,
        contact_person_lname,
        branch_email,
        business_location,
        state_id,
        city_id,
        area,
        landmark,
        gst_register,
        gst_tin_number,
        pincode

    )

    suspend fun updateCompanyBranch(
        token: String?,
        branch_name: String?,
        branch_id: String?,
        branch_code: String?,
        branch_address: String?,
        branch_contact_no: String?,
        secondary_contact: String?,
        contact_person_fname: String?,
        contact_person_lname: String?,
        branch_email: String?,
        business_location: String?,
        state_id: String?,
        city_id: String?,
        area: String?,
        landmark: String?,
        term_balance: String?,
        gst_register: String?,
        gst_tin_number: String?,
        pincode: String?
    ) = apiService.updateCompanyBranch(
        token, branch_name, branch_id,
        branch_code,
        branch_address,
        branch_contact_no,
        secondary_contact,
        contact_person_fname,
        contact_person_lname,
        branch_email,
        business_location,
        state_id,
        city_id,
        area,
        landmark,
        term_balance,
        gst_register,
        gst_tin_number,
        pincode

    )

    suspend fun addUserCompany(
        token: String?,
        company_name: String?,
        reg_address: String?,
        area: String?,
        landmark: String?,
        country_id: String?,
        state_id: String?,
        city_id: String?,
        postal_code: String?,
        contact_person_first_name: String?,
        contact_person_last_name: String?,
        mobile_number: String?,
        alternate_number: String?,
        email: String?,
        fiscal_year_id: String?,
        pan_number: String?,
        cin_number: String?,
        term_balance: String?,
        default_term: String?,
        gst_register: String?,
        gst_tin_number: String?

    ) = apiService.addUserCompany(
        token, company_name,
        reg_address,
        area,
        landmark,
        country_id,
        state_id,
        city_id,
        postal_code,
        contact_person_first_name,
        contact_person_last_name,
        mobile_number,
        alternate_number,
        email,
        fiscal_year_id,
        pan_number,
        cin_number,
        term_balance,
        default_term,
        gst_register,
        gst_tin_number


    )

    suspend fun updateUserCompany(
        token: String?,
        company_id: Number?,
        company_name: String?,
        reg_address: String?,
        area: String?,
        landmark: String?,
        country_id: String?,
        state_id: String?,
        city_id: String?,
        postal_code: String?,
        contact_person_first_name: String?,
        contact_person_last_name: String?,
        mobile_number: String?,
        alternate_number: String?,
        email: String?,
        fiscal_year_id: String?,
        pan_number: String?,
        cin_number: String?,
        term_balance: String?,
        default_term: String?,
        gst_register: String?,
        gst_tin_number: String?

    ) = apiService.updateUserCompany(
        token, company_id, company_name,
        reg_address,
        area,
        landmark,
        country_id,
        state_id,
        city_id,
        postal_code,
        contact_person_first_name,
        contact_person_last_name,
        mobile_number,
        alternate_number,
        email,
        fiscal_year_id,
        pan_number,
        cin_number,
        term_balance,
        default_term,
        gst_register,
        gst_tin_number
    )

    suspend fun updateCustomer(
        token: String?,
        customer_id: String?,
        company_id: String?,
        customer_type: String?,
        title: String?,
        first_name: String?,
        last_name: String?,
        company_name: String?,
        customer_code: String?,
        display_name: String?,
        mobile_number: String?,
        secondary_contact: String?,
        email: String?,
        opening_fine_balance: String?,
        opening_fine_default_term: String?,
        opening_silver_fine_balance: String?,
        opening_silver_fine_default_term: String?,
        opening_cash_balance: String?,
        opening_cash_default_term: String?,
        fine_limit: String?,
        cash_limit: String?,
        is_tcs_applicable: String?,
        gst_register: String?,
        gst_treatment: String?,
        gst_tin_number: String?,
        pan_number: String?,
        courier: String?,
        notes: String?,
        is_shipping: String?,
        billing_address: String?,
        shipping_address: String?,
        is_tds_applicable: String?,
        tax_deductor_type: String?,
        tax_collector_type: String?,
        selectedNogType: String?,
        selectedNopType: String?,
        selectedNatureofPaymentID: String?,
        selectedNatureofGoodsID: String?
    ) = apiService.updateCustomer(
        token,
        customer_id,
        company_id,
        customer_type,
        title,
        first_name,
        last_name,
        company_name,
        customer_code,
        display_name,
        mobile_number,
        secondary_contact,
        email,
        opening_fine_balance,
        opening_fine_default_term,
        opening_silver_fine_balance,
        opening_silver_fine_default_term,
        opening_cash_balance,
        opening_cash_default_term,
        fine_limit,
        cash_limit,
        is_tcs_applicable,
        gst_register,
        gst_treatment,
        gst_tin_number,
        pan_number,
        courier,
        notes,
        is_shipping,
        billing_address,
        shipping_address,
        is_tds_applicable,
        tax_deductor_type,
        tax_collector_type,
        selectedNogType,
        selectedNopType,
        selectedNatureofPaymentID,
        selectedNatureofGoodsID

    )

    suspend fun addItemCategory(
        token: String?,
        category_name: String?,
        category_code: String?,
        status: String?
    ) = apiService.addItemCategory(
        token, category_name,
        category_code,
        status
    )

    suspend fun updateItemCategory(
        token: String?,
        category_name: String?,
        category_id: String?,
        category_code: String?,
        status: Number?
    ) = apiService.updateItemCategory(
        token, category_name, category_id,
        category_code,
        status
    )
    /*suspend fun searchListCustomer(
        token: String?,
        company_id: String?
    ) = apiService.searchListCustomer(token, company_id)*/

    suspend fun searchListCustomer(
        token: String?,
        status: String?,
        offset: Int?,
        name: String?,
        sort_by_column: String?,
        sort_type: String?
    ) = apiService.searchListCustomer(token, status, offset, name, sort_by_column, sort_type)

    suspend fun customerDetail(
        token: String?,
        company_id: String?,
        customer_id: String?
    ) = apiService.customerDetail(token, company_id, customer_id)

    suspend fun changeStatusCustomers(
        token: String?,
        customer_id: String?,
        status: String?
    ) = apiService.changeStatusCustomers(token, customer_id, status)

    suspend fun changeStatusSupplier(
        token: String?,
        vendor_id: String?,
        status: String?
    ) = apiService.changeStatusSupplier(token, vendor_id, status)

    suspend fun changeStatusItem(
        token: String?,
        item_id: String?,
        status: String?
    ) = apiService.changeStatusItem(token, item_id, status)

    suspend fun changeStatusBranch(
        token: String?,
        branch_id: String?,
        status: String?
    ) = apiService.changeStatusBranch(token, branch_id, status)

    suspend fun changeStatusItemCategory(
        token: String?,
        id: String?,
        status: String?
    ) = apiService.changeStatusItemCategory(token, id, status)

    suspend fun deleteContact(
        token: String?,
        contact_id: String?
    ) = apiService.deleteContact(token, contact_id)

    suspend fun deleteItem(
        token: String?,
        item_id: String?
    ) = apiService.deleteItem(token, item_id)

    suspend fun deleteBranch(
        token: String?,
        branch_id: String?
    ) = apiService.deleteBranch(token, branch_id)

    suspend fun deleteCompany(
        token: String?,
        company_id: String?
    ) = apiService.deleteCompany(token, company_id)


    suspend fun deleteItemCategory(
        token: String?,
        id: String?
    ) = apiService.deleteItemCatrgory(token, id)

    suspend fun deleteSale(
        token: String?,
        id: String?
    ) = apiService.deleteSale(token, id)


    suspend fun deletePurchase(
        token: String?,
        id: String?
    ) = apiService.deletePurchase(token, id)

    suspend fun deletePayment(
        token: String?,
        id: String?
    ) = apiService.deletePayment(token, id)

    suspend fun deleteReceipt(
        token: String?,
        id: String?
    ) = apiService.deleteReceipt(token, id)

    suspend fun addSupplier(
        token: String?,
        company_id: String?,
        customer_type: String?,
        title: String?,
        first_name: String?,
        last_name: String?,
        company_name: String?,
        customer_code: String?,
        mobile_number: String?,
        secondary_contact: String?,
        email: String?,
        opening_fine_balance: String?,
        opening_fine_default_term: String?,
        opening_silver_fine_balance: String?,
        opening_silver_fine_default_term: String?,
        opening_cash_balance: String?,
        opening_cash_default_term: String?,
         fine_limit: String?,
         cash_limit: String?,
        is_tcs_applicable: String?,
        gst_register: String?,
        gst_treatment: String?,
        gst_tin_number: String?,
        pan_number: String?,
        courier: String?,
        notes: String?,
        is_shipping: String?,
        billing_address: String?,
        shipping_address: String?,
        display_name: String?,
        is_tds_applicable: String?,
        selectedDeductorType: String?,
        selectedCollectorType: String?,
        selectedNogType: String?,
        selectedNopType: String?,
        selectedNatureofPaymentID: String?,
        selectedNatureofGoodsID: String?
    ) = apiService.addSupplier(
        token,
        company_id,
        customer_type,
        title,
        first_name,
        last_name,
        company_name,
        customer_code,
        mobile_number,
        secondary_contact,
        email,
        opening_fine_balance,
        opening_fine_default_term,
        opening_silver_fine_balance,
        opening_silver_fine_default_term,
        opening_cash_balance,
        opening_cash_default_term,
        fine_limit,
        cash_limit,
        is_tcs_applicable,
        gst_register,
        gst_treatment,
        gst_tin_number,
        pan_number,
        courier,
        notes,
        is_shipping,
        billing_address,
        shipping_address,
        display_name,
        is_tds_applicable,
        selectedDeductorType,
        selectedCollectorType,
        selectedNogType,
        selectedNopType,
        selectedNatureofPaymentID,
        selectedNatureofGoodsID
    )

    suspend fun updateSupplier(
        token: String?,
        vendor_id: String?,
        company_id: String?,
        customer_type: String?,
        title: String?,
        first_name: String?,
        last_name: String?,
        company_name: String?,
        customer_code: String?,
        display_name: String?,
        mobile_number: String?,
        secondary_contact: String?,
        email: String?,
        opening_fine_balance: String?,
        opening_fine_default_term: String?,
        opening_silver_fine_balance: String?,
        opening_silver_fine_default_term: String?,
        opening_cash_balance: String?,
        opening_cash_default_term: String?,
         fine_limit: String?,
         cash_limit: String?,
        is_tcs_applicable: String?,
        gst_register: String?,
        gst_treatment: String?,
        gst_tin_number: String?,
        pan_number: String?,
        courier: String?,
        notes: String?,
        is_shipping: String?,
        billing_address: String?,
        shipping_address: String?,
        is_tds_applicable: String?,
        tax_deductor_type: String?,
        tax_collector_type: String?,
        selectedNogType: String?,
        selectedNopType: String?,
        selectedNatureofPaymentID: String?,
        selectedNatureofGoodsID: String?
    ) = apiService.updateSupplier(
        token,
        vendor_id,
        company_id,
        customer_type,
        title,
        first_name,
        last_name,
        company_name,
        customer_code,
        display_name,
        mobile_number,
        secondary_contact,
        email,
        opening_fine_balance,
        opening_fine_default_term,
        opening_silver_fine_balance,
        opening_silver_fine_default_term,
        opening_cash_balance,
        opening_cash_default_term,
        fine_limit,
        cash_limit,
        is_tcs_applicable,
        gst_register,
        gst_treatment,
        gst_tin_number,
        pan_number,
        courier,
        notes,
        is_shipping,
        billing_address,
        shipping_address,
        is_tds_applicable,
        tax_deductor_type,
        tax_collector_type,
        selectedNogType,
        selectedNopType,
        selectedNatureofPaymentID,
        selectedNatureofGoodsID
    )

    suspend fun getListSupplier(
        token: String?,
        status: String?,
        current_page: Int?,
        name: String?,
        sort_by_column: String?,
        sort_type: String?
    ) = apiService.getListSupplier(token, status, current_page, name, sort_by_column, sort_type)

    suspend fun supplierDetail(
        token: String?,
        company_id: String?,
        supplier_id: String?
    ) = apiService.supplierDetail(token, company_id, supplier_id)

    suspend fun getItemCategory(
        token: String?,
        offset: String?
    ) = apiService.getItemCategory(token, offset)

    suspend fun searchItemCategory(
        token: String?,
        offset: String?
    ) = apiService.searchItemCategory(token, offset)

    suspend fun getItemUnit(
        token: String?,
        company_id: String?
    ) = apiService.getItemUnit(token, company_id)

    suspend fun getTagDetails(
        token: String?,
        tag_number: String?
    ) = apiService.getTagDetails(token, tag_number)

    suspend fun getItemStamp(
        token: String?,
        item_id: String?
    ) = apiService.getItemStamp(token, item_id)

    suspend fun getDefaultTerm(
        token: String?
    ) = apiService.getDefaultTerm(token)

    suspend fun getItemUnitMenu(
        token: String?
    ) = apiService.getItemUnitMenu(token)

    suspend fun getItemGSTMenu(
        token: String?
    ) = apiService.getItemGSTMenu(token)


    suspend fun getMaintainStock(
        token: String?
    ) = apiService.getMaintainStock(token)

    suspend fun getMetalType(
        token: String?
    ) = apiService.getMetalType(token)

    suspend fun getItemVendors(
        token: String?,
        company_id: String?
    ) = apiService.getItemVendors(token, company_id)

    suspend fun addNewItem(
        token: String?,
        item_type: RequestBody?,
        item_name: RequestBody?,
        item_code: RequestBody?,
        category_id: RequestBody?,
        notes: RequestBody?,
        metal_type_id: RequestBody?,
        maintain_stock_in_id: RequestBody?,
        unit_id: RequestBody?,
        is_studded: RequestBody?,
        stock_method: RequestBody?,
        tax_preference: RequestBody?,
        sales_wastage: RequestBody?,
        sales_making_charges: RequestBody?,
        purchase_wastage: RequestBody?,
        purchase_making_charges: RequestBody?,
        jobwork_rate: RequestBody?,
        labourwork_rate: RequestBody?,
        sales_purchase_gst_rate_id: RequestBody?,
        sales_purchase_hsn: RequestBody?,
        jobwork_labourwork_gst_rate_id: RequestBody?,
        jobwork_labourwork_sac: RequestBody?,
        sales_rate: RequestBody?,
        purchase_rate: RequestBody?,
        sales_ledger_id: RequestBody?,
        purchase_ledger_id: RequestBody?,
        jobwork_ledger_id: RequestBody?,
        labourwork_ledger_id: RequestBody?,
        discount_ledger_id: RequestBody?,
        tag_prefix: RequestBody?,
        use_stamp: RequestBody?,
        use_gold_color: RequestBody?,
        min_stock_level_gm: RequestBody?,
        min_stock_level_pcs: RequestBody?,
        max_stock_level_gm: RequestBody?,
        max_stock_level_pcs: RequestBody?,
        product_wt: RequestBody?,
        item_rate: RequestBody?,
        vendor_id: RequestBody?,
        gold_colour: RequestBody?,
        item_image: MultipartBody.Part?
    ) = apiService.addNewItem(
        token, item_type, item_name, item_code, category_id,
        notes, metal_type_id, maintain_stock_in_id, unit_id,
        is_studded, stock_method,tax_preference,
        sales_wastage,
        sales_making_charges,
        purchase_wastage,
        purchase_making_charges,
        jobwork_rate,
        labourwork_rate,
        sales_purchase_gst_rate_id,
        sales_purchase_hsn,
        jobwork_labourwork_gst_rate_id,
        jobwork_labourwork_sac,
        sales_rate,
        purchase_rate,
        sales_ledger_id,
        purchase_ledger_id,
        jobwork_ledger_id,
        labourwork_ledger_id,
        discount_ledger_id,
        tag_prefix,
        use_stamp,
        use_gold_color,
        min_stock_level_gm,
        min_stock_level_pcs,
        max_stock_level_gm,
        max_stock_level_pcs,
        product_wt,
        item_rate,
        vendor_id,
        gold_colour,
        item_image
    )

    suspend fun editItem(
        token: String?,
        item_id : RequestBody?,
        item_type: RequestBody?,
        item_name: RequestBody?,
        item_code: RequestBody?,
        category_id: RequestBody?,
        notes: RequestBody?,
        metal_type_id: RequestBody?,
        maintain_stock_in_id: RequestBody?,
        unit_id: RequestBody?,
        is_studded: RequestBody?,
        stock_method: RequestBody?,
        tax_preference: RequestBody?,
        sales_wastage: RequestBody?,
        sales_making_charges: RequestBody?,
        purchase_wastage: RequestBody?,
        purchase_making_charges: RequestBody?,
        jobwork_rate: RequestBody?,
        labourwork_rate: RequestBody?,
        sales_purchase_gst_rate_id: RequestBody?,
        sales_purchase_hsn: RequestBody?,
        jobwork_labourwork_gst_rate_id: RequestBody?,
        jobwork_labourwork_sac: RequestBody?,
        sales_rate: RequestBody?,
        purchase_rate: RequestBody?,
        sales_ledger_id: RequestBody?,
        purchase_ledger_id: RequestBody?,
        jobwork_ledger_id: RequestBody?,
        labourwork_ledger_id: RequestBody?,
        discount_ledger_id: RequestBody?,
        tag_prefix: RequestBody?,
        use_stamp: RequestBody?,
        use_gold_color: RequestBody?,
        min_stock_level_gm: RequestBody?,
        min_stock_level_pcs: RequestBody?,
        max_stock_level_gm: RequestBody?,
        max_stock_level_pcs: RequestBody?,
        product_wt: RequestBody?,
        item_rate: RequestBody?,
        vendor_id: RequestBody?,
        gold_colour: RequestBody?,
        item_image: MultipartBody.Part?
    ) = apiService.editItem(
        token, item_id, item_type,item_name, item_code, category_id,
        notes, metal_type_id, maintain_stock_in_id, unit_id,
        is_studded, stock_method,tax_preference,
        sales_wastage,
        sales_making_charges,
        purchase_wastage,
        purchase_making_charges,
        jobwork_rate,
        labourwork_rate,
        sales_purchase_gst_rate_id,
        sales_purchase_hsn,
        jobwork_labourwork_gst_rate_id,
        jobwork_labourwork_sac,
        sales_rate,
        purchase_rate,
        sales_ledger_id,
        purchase_ledger_id,
        jobwork_ledger_id,
        labourwork_ledger_id,
        discount_ledger_id,
        tag_prefix,
        use_stamp,
        use_gold_color,
        min_stock_level_gm,
        min_stock_level_pcs,
        max_stock_level_gm,
        max_stock_level_pcs,
        product_wt,
        item_rate,
        vendor_id,
        gold_colour,
        item_image
    )


    suspend fun getItemList(
        token: String?,
        status: String?,
        current_page: Int?,
        search: String?,
        sort_by_column: String?,
        sort_type: String?
    ) = apiService.getItemList(token,status, current_page, search, sort_by_column, sort_type)

    suspend fun getItemDetails(
        token: String?,
        item_id: String?
    ) = apiService.getItemDetails(token, item_id)


    suspend fun updateItem(
        token: String?,
        company_id: RequestBody?,
        item_id: RequestBody?,
        item_name: RequestBody?,
        item_code: RequestBody?,
        item_stock_type: RequestBody?,
        category_id: RequestBody?,
        unit: RequestBody?,
        vendor_id: RequestBody?,
        sales_wastage: RequestBody?,
        sales_making_charge: RequestBody?,
        purchase_wastage: RequestBody?,
        purchase_making_charge: RequestBody?,
        opening_stocks: RequestBody?,
//        quantity: RequestBody?,
//        gross_wt: RequestBody?,
//        less_wt: RequestBody?,
//        touch: RequestBody?,
//        making_charge: RequestBody?,
        notes: RequestBody?,
        default_image_index: RequestBody?,
        show_in_sales: RequestBody?,
        show_in_purchase: RequestBody?,
        is_raw_material: RequestBody?,
        colour_id: RequestBody?,
        hsn_code: RequestBody?,
        minimum_stock_level: RequestBody?,
        maximum_stock_level: RequestBody?,
        item_image: MultipartBody.Part?
    ) = apiService.updateItem(
        token, company_id, item_id, item_name, item_code, item_stock_type, category_id, unit,
        vendor_id,
        sales_wastage,
        sales_making_charge,
        purchase_wastage,
        purchase_making_charge,
        opening_stocks,
//        quantity,
//        gross_wt,
//        less_wt,
//        touch,
//        making_charge,
        notes,
        default_image_index,
        show_in_sales,
        show_in_purchase,
        is_raw_material,
        colour_id,
        hsn_code,
        minimum_stock_level,
        maximum_stock_level,
        item_image
    )

    suspend fun getSearchCustomer(
        token: String?,
        company_id: String?,
        search: String?,
        offset: String?
    ) = company_id?.let { apiService.getSearchCustomer(token, it, search, offset) }

    suspend fun getSearchVendor(
        token: String?,
        company_id: String?,
        search: String?,
        offset: String?,
        transaction_type: String?
    ) = company_id?.let { apiService.getSearchVendor(token, it, search, offset, transaction_type) }

    suspend fun getSearchContacts(
        token: String?,
        company_id: String?,
        search: String?,
        offset: String?,
        transaction_id: String?
    ) = company_id?.let { apiService.getSearchContacts(token, it, search, offset, transaction_id) }

    suspend fun getSearchContactLedger(
        token: String?

    ) =  apiService.getSearchContactsLedger(token)


    suspend fun reportSupportContacts(
        token: String?,
        company_id: String?,
        search: String?,
        offset: String?
    ) = company_id?.let { apiService.reportSupportContacts(token, it, search, offset) }


    suspend fun saleDetail(
        token: String?,
        transaction_id: String?
    ) = apiService.saleDetail(token, transaction_id)

    suspend fun voucherText(
        token: String?,
        transaction_type: String?,
        transaction_id: String?
    ) = apiService.voucherText(token, transaction_type, transaction_id)

    suspend fun paymentDetail(
        token: String?,
        transaction_id: String?
    ) = apiService.paymentDetail(token, transaction_id)

    suspend fun receiptDetail(
        token: String?,
        transaction_id: String?
    ) = apiService.receiptDetail(token, transaction_id)

    suspend fun searchListSales(
        token: String?,
        current_page: Int?,
        name: String?,
        sort_by_column: String?,
        sort_type: String?,
        date_range_from: String?,
        date_range_to: String?
    ) = apiService.searchListSales(token,  current_page, name, sort_by_column,
        sort_type,date_range_from,date_range_to)

    suspend fun getUserCompanies(
        token: String?,
        company_id: String?

    ) = apiService.getUserCompanies(token, company_id)

    suspend fun getItemCategories(
        token: String?,
        offset: String?
    ) = apiService.getItemCategory(token, offset)

    suspend fun getReportsItemCategories(
        token: String?,
        search: String?
    ) = apiService.getReportsItemCategories(token, search)

    suspend fun getReportsItems(
        token: String?,
        search: String?,
        item_category_id: String?
    ) = apiService.getReportsItems(token, search, item_category_id)

    suspend fun getCompanyBranches(
        token: String?,
        company_id: String?

    ) = apiService.getCompanyBranches(token, company_id)

    suspend fun getUserCompanyDetails(
        token: String?,
        company_id: String?

    ) = apiService.getUserCompanyDetails(token, company_id)

    suspend fun getBranchDetails(
        token: String?,
        branch_id: String?

    ) = apiService.getBranchDetails(token, branch_id)

    suspend fun getSearchItem(
        token: String?,
        search: String?,
        offset: String?,
        transaction_type: String?,
        transaction_id: String?
    ) = apiService.getSearchItem(token, search, offset, transaction_type, transaction_id)



    suspend fun getItemSearch(
        token: String?,
        item_name: String?,
        module: String?,
        item_type: String?
    ) = apiService.getItemSearch(token, item_name, module,item_type)



    suspend fun getSearchLedger(
        token: String?,
        type: String?
    ) = apiService.getSearchLedger(token, type)

    suspend fun getCalculateItem(
        token: String?,
        calculate: String?,
        contact_id: String?,
        transaction_id: String?,
        item: String?,
        issue_receive_transaction: String?,
        is_gst_applicable: String?,
        tds_percentage: String?,
        tcs_percentage: String?,
        place_of_supply: String?,
        tds_tcs_enable: String?,
        sgst_ledger_id: String?,
        cgst_ledger_id: String?,
        igst_ledger_id: String?,
        tcs_ledger_id: String?,
        round_off_ledger_id: String?,
        round_off_total: String?,
        invoice_date: String?
    ) = apiService.getCalculateItem(
        token,
        calculate,
        contact_id,
        transaction_id,
        item,
        issue_receive_transaction,
        is_gst_applicable,
        tds_percentage,
        tcs_percentage,
        place_of_supply,
        tds_tcs_enable,
        sgst_ledger_id,
        cgst_ledger_id,
        igst_ledger_id,
        tcs_ledger_id,
        round_off_ledger_id,
        round_off_total,
        invoice_date
    )

    suspend fun getCalculateItemPayment(
        token: String?,
        item: String?,
        contact_id: String?,
        ledger_contact_type: String?,
        transaction_id: String,
        transaction_date: String?,
        issue_receive_transaction: String?
    ) = apiService.getCalculateItemPayment(
        token,
        item,
        contact_id,
        ledger_contact_type,
        transaction_id,
        transaction_date,
        issue_receive_transaction
    )

    suspend fun getCalculateItemReceipt(
        token: String?,
        item: String?,
        contact_id: String?,
        ledger_contact_type: String?,
        transaction_id: String,
        transaction_date: String?,
        issue_receive_transaction: String?
    ) = apiService.getCalculateItemReceipt(
        token,
        item,
        contact_id,
        ledger_contact_type,
        transaction_id,
        transaction_date,
        issue_receive_transaction

    )

    suspend fun getCalculateItemPurchase(
        token: String?,
        calculate: String?,
        contact_id: String?,
        transaction_id: String?,
        item: String?,
        issue_receive_transaction: String?,
        is_gst_applicable: String?,
        tds_percentage: String?,
        tcs_percentage: String?,
        place_of_supply: String?,
        tds_tcs_enable: String?,
        sgst_ledger_id: String?,
        cgst_ledger_id: String?,
        igst_ledger_id: String?,
        tcs_ledger_id: String?,
        round_off_ledger_id: String?,
        round_off_total: String?,
        invoice_date: String?
    ) = apiService.getCalculateItemPurchase(
        token,
        calculate,
        contact_id,
        transaction_id,
        item,
        issue_receive_transaction,
        is_gst_applicable,
        tds_percentage,
        tcs_percentage,
        place_of_supply,
        tds_tcs_enable,
        sgst_ledger_id,
        cgst_ledger_id,
        igst_ledger_id,
        tcs_ledger_id,
        round_off_ledger_id,
        round_off_total,
        invoice_date
    )


    suspend fun addnewinvoice(
        token: String?,
        transaction_type_id: RequestBody?,
        transaction_type_name: RequestBody?,
        transaction_date: RequestBody?,
        customer_code: RequestBody?,
        display_name: RequestBody?,
        contact_id: RequestBody?,
        party_po_no: RequestBody?,
        reference: RequestBody?,
        renarks: RequestBody?,
        invoice_number: RequestBody?,
        item_json: RequestBody?,
        issue_receive_transaction: RequestBody?,
        place_of_supply: RequestBody?,
        sgst_ledger_id: RequestBody?,
        cgst_ledger_id: RequestBody?,
        igst_ledger_id: RequestBody?,
        tds_ledger_id: RequestBody?,
        tds_percentage: RequestBody?,
        tcs_ledger_id: RequestBody?,
        tcs_percentage: RequestBody?,
        tds_tcs_enable: RequestBody?,
        round_off_ledger_id: RequestBody?,
        round_off_total: RequestBody?,
        branch_type: RequestBody?,
        ledger_id: RequestBody?,
        image: MultipartBody.Part?,
        transaction_type: RequestBody?
    ) = apiService.addNewInvoice(
        token,
        transaction_type_id,
        transaction_type_name,
        transaction_date,
        customer_code,
        display_name,
        contact_id,
        party_po_no,
        reference,
        renarks,
        invoice_number,
        item_json,
        issue_receive_transaction,
        place_of_supply,
        sgst_ledger_id,
        cgst_ledger_id,
        igst_ledger_id,
        tds_ledger_id,
        tds_percentage,
        tcs_ledger_id,
        tcs_percentage,
        tds_tcs_enable,
        round_off_ledger_id,
        round_off_total,
        branch_type,
        ledger_id,
        image,
        transaction_type
    )


    suspend fun editInvoice(
        token: String?,
        transaction_id: RequestBody?,
        transaction_type_id: RequestBody?,
        transaction_type_name: RequestBody?,
        transaction_date: RequestBody?,
        customer_code: RequestBody?,
        display_name: RequestBody?,
        contact_id: RequestBody?,
        party_po_no: RequestBody?,
        reference: RequestBody?,
        renarks: RequestBody?,
        invoice_number: RequestBody?,
        item_json: RequestBody?,
        issue_receive_transaction: RequestBody?,
        place_of_supply: RequestBody?,
        sgst_ledger_id: RequestBody?,
        cgst_ledger_id: RequestBody?,
        igst_ledger_id: RequestBody?,
        tds_ledger_id: RequestBody?,
        tds_percentage: RequestBody?,
        tcs_ledger_id: RequestBody?,
        tcs_percentage: RequestBody?,
        tds_tcs_enable: RequestBody?,
        round_off_ledger_id: RequestBody?,
        round_off_total: RequestBody?,
        branch_type: RequestBody?,
        ledger_id: RequestBody?,
        image: MultipartBody.Part?,
        transaction_type: RequestBody?
    ) = apiService.editInvoive(
        token,
        transaction_id,
        transaction_type_id,
        transaction_type_name,
        transaction_date,
        customer_code,
        display_name,
        contact_id,
        party_po_no,
        reference,
        renarks,
        invoice_number,
        item_json,
        issue_receive_transaction,
        place_of_supply,
        sgst_ledger_id,
        cgst_ledger_id,
        igst_ledger_id,
        tds_ledger_id,
        tds_percentage,
        tcs_ledger_id,
        tcs_percentage,
        tds_tcs_enable,
        round_off_ledger_id,
        round_off_total,
        branch_type,
        ledger_id,
        image,
        transaction_type
    )



    suspend fun addnewpayment(
        token: String?,
        transaction_type_id: RequestBody?,
        transaction_type_name: RequestBody?,
        transaction_date: RequestBody?,
        contactId: RequestBody?,
        ledger_contact_type: RequestBody?,
        invoice_number: RequestBody?,
        item_json: RequestBody?,
        issue_receive_transaction: RequestBody?,
        is_gst_applicable: RequestBody?,
        party_po_no: RequestBody?,
        reference: RequestBody?,
        remarks: RequestBody?,
        image: MultipartBody.Part?
    ) = apiService.addNewPayment(
        token,
        transaction_type_id,
        transaction_type_name,
        transaction_date,
        contactId,
        ledger_contact_type,
        invoice_number,
        item_json,
        issue_receive_transaction,
        is_gst_applicable,
        party_po_no,
        reference,
        remarks,
        image
    )

    suspend fun addnewReceipt(
        token: String?,
        transaction_type_id: RequestBody?,
        transaction_type_name: RequestBody?,
        transaction_date: RequestBody?,
        contactId: RequestBody?,
        ledger_contact_type: RequestBody?,
        invoice_number: RequestBody?,
        item_json: RequestBody?,
        issue_receive_transaction: RequestBody?,
        is_gst_applicable: RequestBody?,
        party_po_no: RequestBody?,
        reference: RequestBody?,
        remarks: RequestBody?,
        image: MultipartBody.Part?
    ) = apiService.addNewReceipt(
        token,
        transaction_type_id,
        transaction_type_name,
        transaction_date,
        contactId,
        ledger_contact_type,
        invoice_number,
        item_json,
        issue_receive_transaction,
        is_gst_applicable,
        party_po_no,
        reference,
        remarks,
        image
    )

    suspend fun editPayment(
        token: String?,
        transaction_type_id: RequestBody?,
        transaction_type_name: RequestBody?,
        transaction_id: RequestBody?,
        transaction_date: RequestBody?,
        contactId: RequestBody?,
        ledger_contact_type: RequestBody?,
        invoice_number: RequestBody?,
        item_json: RequestBody?,
        issue_receive_transaction: RequestBody?,
        is_gst_applicable: RequestBody?,
        party_po_no: RequestBody?,
        reference: RequestBody?,
        remarks: RequestBody?,
        image: MultipartBody.Part?
    ) = apiService.editPayment(
        token,
        transaction_type_id,
        transaction_type_name,
        transaction_id,
        transaction_date,
        contactId,
        ledger_contact_type,
        invoice_number,
        item_json,
        issue_receive_transaction,
        is_gst_applicable,
        party_po_no,
        reference,
        remarks,
        image
    )

    suspend fun editReceipt(
        token: String?,
        transaction_id: RequestBody?,
        transaction_type_id: RequestBody?,
        transaction_type_name: RequestBody?,
        transaction_date: RequestBody?,
        contact_id: RequestBody?,
        ledger_contact_type: RequestBody?,
        invoice_number: RequestBody?,
        item_json: RequestBody?,
        issue_receive_transaction: RequestBody?,
        is_gst_applicable: RequestBody?,
        party_po_no: RequestBody?,
        reference: RequestBody?,
        remarks: RequestBody?,
        image: MultipartBody.Part?
    ) = apiService.editReceipt(
        token,
        transaction_id,
        transaction_type_id,
        transaction_type_name,
        transaction_date,
        contact_id,
        ledger_contact_type,
        invoice_number,
        item_json,
        issue_receive_transaction,
        is_gst_applicable,
        party_po_no,
        reference,
        remarks,
        image
    )

    suspend fun addPBM(
        token: String?,
        transaction_type_id: RequestBody?,
        transaction_type_name: RequestBody?,
        transaction_date: RequestBody?,
        customer_code: RequestBody?,
        display_name: RequestBody?,
        contact_id: RequestBody?,
        party_po_no: RequestBody?,
        reference: RequestBody?,
        remarks: RequestBody?,
        invoice_number: RequestBody?,
        item_json: RequestBody?,
        issue_receive_transaction: RequestBody?,
        place_of_supply: RequestBody?,
        sgst_ledger_id: RequestBody?,
        cgst_ledger_id: RequestBody?,
        igst_ledger_id: RequestBody?,
        tds_ledger_id: RequestBody?,
        tds_percentage: RequestBody?,
        tcs_ledger_id: RequestBody?,
        tcs_percentage: RequestBody?,
        tds_tcs_enable: RequestBody?,
        round_off_ledger_id: RequestBody?,
        round_off_total: RequestBody?,
        branch_type: RequestBody?,
        ledger_id: RequestBody?,
        image: MultipartBody.Part?,
        transaction_type: RequestBody?
    ) = apiService.addPBM(
        token,
        transaction_type_id,
        transaction_type_name,
        transaction_date,
        customer_code,
        display_name,
        contact_id,
        party_po_no,
        reference,
        remarks,
        invoice_number,
        item_json,
        issue_receive_transaction,
        place_of_supply,
        sgst_ledger_id,
        cgst_ledger_id,
        igst_ledger_id,
        tds_ledger_id,
        tds_percentage,
        tcs_ledger_id,
        tcs_percentage,
        tds_tcs_enable,
        round_off_ledger_id,
        round_off_total,
        branch_type,
        ledger_id,
        image,
        transaction_type
    )


    suspend fun getInvoiceNumber(
        token: String?,
        invoice_date: String?,
        transaction_id: String?
    ) = apiService.getInvoiceNumber(token, invoice_date, transaction_id)

    suspend fun getPaymentInvoiceNumber(
        token: String?,
        invoice_date: String?,
        transaction_id: String?
    ) = apiService.getPaymentInvoiceNumber(token, invoice_date, transaction_id)

    suspend fun getReceiptInvoiceNumber(
        token: String?,
        invoice_date: String?,
        transaction_id: String?
    ) = apiService.getReceiptInvoiceNumber(token, invoice_date, transaction_id)

    suspend fun getPurchaseInvoiceNoFromApi(
        token: String?,
        invoice_date: String?,
        transaction_id: String?
    ) = apiService.getPurchaseInvoiceNoFromApi(token, invoice_date, transaction_id)



    suspend fun editPurchase(
        token: String?,
        transaction_id: RequestBody?,
        transaction_type_id: RequestBody?,
        transaction_type_name: RequestBody?,
        transaction_date: RequestBody?,
        customer_code: RequestBody?,
        display_name: RequestBody?,
        contact_id: RequestBody?,
        party_po_no: RequestBody?,
        reference: RequestBody?,
        remarks: RequestBody?,
        invoice_number: RequestBody?,
        item_json: RequestBody?,
        issue_receive_transaction: RequestBody?,
        place_of_supply: RequestBody?,
        sgst_ledger_id: RequestBody?,
        cgst_ledger_id: RequestBody?,
        igst_ledger_id: RequestBody?,
        tds_ledger_id: RequestBody?,
        tds_percentage: RequestBody?,
        tcs_ledger_id: RequestBody?,
        tcs_percentage: RequestBody?,
        tds_tcs_enable: RequestBody?,
        round_off_ledger_id: RequestBody?,
        round_off_total: RequestBody?,
        branch_type: RequestBody?,
        ledger_id: RequestBody?,
        image: MultipartBody.Part?,
        transaction_type: RequestBody?
    ) = apiService.editPurchase(
        token,transaction_id,
        transaction_type_id,
        transaction_type_name,
        transaction_date,
        customer_code,
        display_name,
        contact_id,
        party_po_no,
        reference,
        remarks,
        invoice_number,
        item_json,
        issue_receive_transaction,
        place_of_supply,
        sgst_ledger_id,
        cgst_ledger_id,
        igst_ledger_id,
        tds_ledger_id,
        tds_percentage,
        tcs_ledger_id,
        tcs_percentage,
        tds_tcs_enable,
        round_off_ledger_id,
        round_off_total,
        branch_type,
        ledger_id,
        image,
        transaction_type
    )


    suspend fun searchListPurchase(
        token: String?,
        current_page: Int?,
        name: String?,
        sort_by_column: String?,
        sort_type: String?,
        date_range_from: String?,
        date_range_to: String?
    ) = apiService.searchListPurchase(
        token,
        current_page,
        name,
        sort_by_column,
        sort_type,
        date_range_from,
        date_range_to
    )

    suspend fun transactionReference(
        token: String?,
        contact_id: String?,
        transaction_id: String?
    ) = apiService.transactionReference(token, contact_id, transaction_id)

    suspend fun searchListPayment(
        token: String?,
        current_page: Int?,
        name: String?,
        sort_by_column: String?,
        sort_type: String?,
        date_range_from: String?,
        date_range_to: String?
    ) = apiService.searchListPayment(token, current_page, name, sort_by_column,
        sort_type,date_range_from,date_range_to)

    suspend fun searchListReceipt(
        token: String?,
        current_page: Int?,
        name: String?,
        sort_by_column: String?,
        sort_type: String?,
        date_range_from: String?,
        date_range_to: String?
    ) = apiService.searchListReceipt(token, current_page, name, sort_by_column,
        sort_type,date_range_from,date_range_to)


    suspend fun getOpeningStockVoucherNoFromApi(
        token: String?,
        invoice_date: String?,
        transaction_id: String?
    ) = apiService.getOpeningStockVoucherNoFromApi(token, invoice_date, transaction_id)


    suspend fun addOpeningStock(
        token: String?,
        transaction_date: RequestBody?,
        invoice_number: RequestBody?,
        item_json: RequestBody?,
        remarks: RequestBody?,
        reference: RequestBody?
    ) = apiService.addOpeningStock(
        token, transaction_date, invoice_number, item_json, remarks,
        reference
    )


    suspend fun editOpeningStock(
        token: String?,
        transaction_id: RequestBody?,
        transaction_date: RequestBody?,
        invoice_number: RequestBody?,
        item_json: RequestBody?,
        remarks: RequestBody?,
        reference: RequestBody?
    ) = apiService.editOpeningStock(
        token, transaction_id,transaction_date, invoice_number, item_json, remarks,
        reference
    )

    suspend fun deleteOpeningStock(
        token: String?,
        transaction_id: String?
    ) = apiService.deleteOpeningStock(token, transaction_id)

    suspend fun getCalculateOpeningStock(
        token: String?,
        item_json: RequestBody?
    ) = apiService.getOpeningStockCalculate(token,item_json)



    suspend fun openingStockDetail(
        token: String?,
        transaction_id: String?
    ) = apiService.openinStockDetail(token,  transaction_id)


    suspend fun purchaseDetail(
        token: String?,
        transaction_id: String?
    ) = apiService.purchaseDetail(token, transaction_id)

    suspend fun updateGoldrate(
        token: String?,
        company_id: String?,
        bill_rate: String?,
        cash_rate: String?,
        type: String?
    ) = apiService.updateGoldrate(token, company_id, bill_rate, cash_rate, type)

    suspend fun termsOfService(
        /* token: String?,*/
        cms: String?,

        page_id: Int?
    ) = apiService.termsOfService(/*token,*/cms, page_id)

    suspend fun updatePassword(
        token: String?,
        current_password: String?,
        password: String?,
        password_confirmation: String?
    ) = apiService.updatePassword(token, current_password, password, password_confirmation)

    suspend fun companySwitch(
        token: String?,
        company_id: String?
    ) = apiService.companySwitch(token, company_id)

    suspend fun getUpdateSessionDetails(
        token: String?

    ) = apiService.getUpdateSessionDetails(token)

    suspend fun branchSwitch(
        token: String?,
        branch_id: String?
    ) = apiService.branchSwitch(token, branch_id)

    suspend fun getSelectedCompanyBranches(
        token: String?, company_id: String?
    ) = apiService.getSelectedCompanyBranches(token, company_id)

    suspend fun contactReportPrint(
        token: String?,
        from_date: String?,
        to_date: String?,
        report_type: String?,
        all_contacts: String?,
        type_of_contact: String?,
        contact_id: String?,
        all_cash_ledgers: String?,
        cash_ledger_id: String?,
        all_bank_ledgers: String?,
        bank_ledger_id: String?,
        period: String?,
        all_item_categories: String?,
        item_category_id: String?,
        all_items: String?,
        item_id: String?
    ) = apiService.contactReportPrint(
        token,
        from_date,
        to_date,
        report_type,
        all_contacts,
        type_of_contact,
        contact_id,
        all_cash_ledgers,
        cash_ledger_id,
        all_bank_ledgers,
        bank_ledger_id,
        period,
        all_item_categories,
        item_category_id,
        all_items,
        item_id
    )

    suspend fun dayReportPrint(
        token: String?,
        from_date: String?,
        to_date: String?,
        report_type: String?,
        all_contacts: String?,
        type_of_contact: String?,
        contact_id: String?,
        all_cash_ledgers: String?,
        cash_ledger_id: String?,
        all_bank_ledgers: String?,
        bank_ledger_id: String?,
        period: String?,
        all_item_categories: String?,
        item_category_id: String?,
        all_items: String?,
        item_id: String?
    ) = apiService.dayReportPrint(
        token,
        from_date,
        to_date,
        report_type,
        all_contacts,
        type_of_contact,
        contact_id,
        all_cash_ledgers,
        cash_ledger_id,
        all_bank_ledgers,
        bank_ledger_id,
        period,
        all_item_categories,
        item_category_id,
        all_items,
        item_id
    )

    suspend fun cashbankReportPrint(
        token: String?,
        from_date: String?,
        to_date: String?,
        report_type: String?,
        all_contacts: String?,
        type_of_contact: String?,
        contact_id: String?,
        all_cash_ledgers: String?,
        cash_ledger_id: String?,
        all_bank_ledgers: String?,
        bank_ledger_id: String?,
        period: String?,
        all_item_categories: String?,
        item_category_id: String?,
        all_items: String?,
        item_id: String?
    ) = apiService.cashbankReportPrint(
        token,
        from_date,
        to_date,
        report_type,
        all_contacts,
        type_of_contact,
        contact_id,
        all_cash_ledgers,
        cash_ledger_id,
        all_bank_ledgers,
        bank_ledger_id,
        period,
        all_item_categories,
        item_category_id,
        all_items,
        item_id
    )

    suspend fun stockReportPrint(
        token: String?,
        from_date: String?,
        to_date: String?,
        report_type: String?,
        all_contacts: String?,
        type_of_contact: String?,
        contact_id: String?,
        all_cash_ledgers: String?,
        cash_ledger_id: String?,
        all_bank_ledgers: String?,
        bank_ledger_id: String?,
        period: String?,
        all_item_categories: String?,
        item_category_id: String?,
        all_items: String?,
        item_id: String?
    ) = apiService.stockReportPrint(
        token,
        from_date,
        to_date,
        report_type,
        all_contacts,
        type_of_contact,
        contact_id,
        all_cash_ledgers,
        cash_ledger_id,
        all_bank_ledgers,
        bank_ledger_id,
        period,
        all_item_categories,
        item_category_id,
        all_items,
        item_id
    )


    suspend fun salesPurchaseReportPrint(
        token: String?,
        from_date: String?,
        to_date: String?,
        report_type: String?,
        all_contacts: String?,
        type_of_contact: String?,
        contact_id: String?,
        all_cash_ledgers: String?,
        cash_ledger_id: String?,
        all_bank_ledgers: String?,
        bank_ledger_id: String?,
        period: String?,
        all_item_categories: String?,
        item_category_id: String?,
        all_items: String?,
        item_id: String?
    ) = apiService.salePurchaseReportPrint(
        token,
        from_date,
        to_date,
        report_type,
        all_contacts,
        type_of_contact,
        contact_id,
        all_cash_ledgers,
        cash_ledger_id,
        all_bank_ledgers,
        bank_ledger_id,
        period,
        all_item_categories,
        item_category_id,
        all_items,
        item_id
    )


    suspend fun getAppVersion() = apiService.getAppVersion()
    suspend fun userLimitAccess(token: String?) = apiService.userLimitAccess(token)
    suspend fun userWiseRestriction(token: String?) = apiService.userWiseRestriction(token)
    suspend fun dashboardDetails(token: String?) = apiService.dashboardDetails(token)
    suspend fun setAppLockPin(
        token: String?,
        pin: String?
    ) = apiService.setAppLockPin(token, pin)

    suspend fun checkPin(
        token: String?,
        pin: String?
    ) = apiService.checkPin(token, pin)

    suspend fun forgetPin(
        token: String?
    ) = apiService.forgetPin(token)

    suspend fun getdetailPreferenceApi(
        token: String?
    ) = apiService.getdetailPreferenceApi(token)

    suspend fun getTaxTcsDetailApi(
        token: String?
    ) = apiService.taxTcsDetail(token)

    suspend fun getTaxTdsDetailApi(
        token: String?
    ) = apiService.taxTdsDetail(token)

    suspend fun savePreferenceApi(
        token: String?,
        enable_cheque_reg_for_bank_acc: Int?,
        round_off_for_sales: Int?,
        default_term: String?,
        print_copies: String?
    ) = apiService.savePreferenceApi(
        token,
        enable_cheque_reg_for_bank_acc,
        round_off_for_sales,
        default_term,
        print_copies
    )

    //settings contact
    suspend fun getdetailContactApi(
        token: String?
    ) = apiService.getdetailContactApi(token)

    suspend fun gettdsDeductorTypeApi(
        token: String?
    ) = apiService.tdsDeductorType(token)

    suspend fun gettcsCollectorTypeApi(
        token: String?
    ) = apiService.tcsCollectorType(token)

    suspend fun getdetailGstApi(
        token: String?
    ) = apiService.getdetailGstApi(token)

    //settings contact
    suspend fun saveContactApi(
        token: String?,
        disable_credit_limit: Int?,
        stop_transaction_if_limit_over: Int?
    ) = apiService.saveContactApi(
        token,
        disable_credit_limit,
        stop_transaction_if_limit_over
    )

    suspend fun saveTcsDetailApi(
        token: String?,
        type: String?,
        enable_gst: Int?,
        gst_state_id: String?,
        gstin: String?,
        registration_date: String?,
        periodicity_of_gst1: String?,
        enable_tcs: Int?,
        enable_tds: Int?,
        tan_number: String?,
        tds_circle: String?,
        tcs_collector_type: String?,
        tcs_person_responsible: String?,
        tcs_designation: String?,
        tcs_contact_number: String?,
        nature_of_goods: String?,
        tds_deductor_type: String?,
        tds_person_responsible: String?,
        tds_designation: String?,
        tds_contact_number: String?,
        nature_of_payment: String?
    ) = apiService.saveTaxApi(
        token,
        type,
        enable_gst,
        gst_state_id,
        gstin,
        registration_date,
        periodicity_of_gst1,
        enable_tcs,
        enable_tds,
        tan_number,
        tds_circle,
        tcs_collector_type,
        tcs_person_responsible,
        tcs_designation,
        tcs_contact_number,
        nature_of_goods,
        tds_deductor_type,
        tds_person_responsible,
        tds_designation,
        tds_contact_number,
        nature_of_payment
    )

    suspend fun deleteNatureOfGoods(
        token: String?,
        nature_of_good_id: String?
    ) = apiService.deleteNatureOfGoods(token, nature_of_good_id)

    suspend fun deleteNatureOfPayment(
        token: String?,
        nature_of_payment_id: String?
    ) = apiService.deleteNatureOfPayment(token, nature_of_payment_id)

    suspend fun searchLedger(
        token: String?,
        type: String?
    ) = apiService.searchLedger(token, type)



    suspend fun searchListLedger(
        token: String?,
        company_id: String?,
        current_page: Int?,
        name: String?,
        sort_by_column: String?,
        sort_type: String?
    ) = apiService.searchListLedger(
        token,
        company_id,
        current_page,
        name,
        sort_by_column,
        sort_type
    )

    suspend fun addLedgerDetails(
        token: String?,
        name: String?,
        code: String?,
        is_sub_account: String?,
        group_id: String?,
        sub_group_id: String?,
        is_bank_account: String?,
        bank_name: String?,
        account_number: String?,
        ifsc_code: String?,
        branch_name: String?,
        is_duties_and_taxes: String?,
        type_of_duty: String?,
        is_tcs_applicable: String?,
        is_tds_applicable: String?,
        nature_of_goods: String?,
        nature_of_payment: String?,
        type_of_gst: String?,
        percentage_of_duty: String?,
        bill_by_bill_reference: String?,
        pan_card: String?,
        gst_treatment: String?,
        gstin: String?,
        notes: String?,
        opening_balance_type: String?,
        opening_balance: String?,
        cheque_register_array: String?
    ) = apiService.addLedgerDetails(
        token,
        name,
        code,
        is_sub_account,
        group_id,
        sub_group_id,
        is_bank_account,
        bank_name,
        account_number,
        ifsc_code,
        branch_name,
        is_duties_and_taxes,
        type_of_duty,
        is_tcs_applicable,
        is_tds_applicable,
        nature_of_goods,
        nature_of_payment,
        type_of_gst,
        percentage_of_duty,
        bill_by_bill_reference,
        pan_card,
        gst_treatment,
        gstin,
        notes,
        opening_balance_type,
        opening_balance,
        cheque_register_array
    )


    suspend fun editLedgerDetails(
        token: String?,
        ledger_id: String?,
        name: String?,
        code: String?,
        is_sub_account: String?,
        group_id: String?,
        sub_group_id: String?,
        is_bank_account: String?,
        bank_name: String?,
        account_number: String?,
        ifsc_code: String?,
        branch_name: String?,
        is_duties_and_taxes: String?,
        type_of_duty: String?,
        is_tcs_applicable: String?,
        is_tds_applicable: String?,
        nature_of_goods: String?,
        nature_of_payment: String?,
        type_of_gst: String?,
        percentage_of_duty: String?,
        bill_by_bill_reference: String?,
        pan_card: String?,
        gst_treatment: String?,
        gstin: String?,
        notes: String?,
        opening_balance_type: String?,
        opening_balance: String?,
        cheque_register_array: String?
    ) = apiService.editLedgerDetails(
        token,
        ledger_id,
        name,
        code,
        is_sub_account,
        group_id,
        sub_group_id,
        is_bank_account,
        bank_name,
        account_number,
        ifsc_code,
        branch_name,
        is_duties_and_taxes,
        type_of_duty,
        is_tcs_applicable,
        is_tds_applicable,
        nature_of_goods,
        nature_of_payment,
        type_of_gst,
        percentage_of_duty,
        bill_by_bill_reference,
        pan_card,
        gst_treatment,
        gstin,
        notes,
        opening_balance_type,
        opening_balance,
        cheque_register_array
    )


    suspend fun getLedgerGroupSubGroup(
        token: String?,
        group_id: String?
    ) = apiService.getLedgerGroupSubGroup(token, group_id)

    suspend fun ledgerDetail(
        token: String?,
        ledger_id: String?
    ) = apiService.ledgerDetail(token, ledger_id)

    suspend fun deleteLedger(
        token: String?,
        ledger_id: String?
    ) = apiService.deleteLedger(token, ledger_id)

    suspend fun getNatureofGoods(
        token: String?
    ) = apiService.getNatureofGoods(token)

    suspend fun getNatureOfPayment(
        token: String?
    ) = apiService.getNatureOfPayment(token)


    suspend fun searchListGroup(
        token: String?,
        company_id: String?,
        current_page: Int?,
        name: String?,
        sort_by_column: String?,
        sort_type: String?
    ) = apiService.searchListGroup(token, company_id, current_page, name, sort_by_column, sort_type)


    suspend fun addGroup(
        token: String?,
        group_name: String?,
        ledger_group_id: String?,
        nature_group_id: String?,
        affect_gross_profit: String?,
        is_bank_account: String?,
        description: String?,
        make_this_sub_group: String?

    ) = apiService.addGroup(
        token,
        group_name,
        ledger_group_id,
        nature_group_id,
        affect_gross_profit,
        is_bank_account,
        description,
        make_this_sub_group
    )

    suspend fun groupDetail(
        token: String?,
        group_id: String?
    ) = apiService.groupDetail(token, group_id)

    suspend fun deleteGroup(
        token: String?,
        group_id: String?
    ) = apiService.deleteGroup(token, group_id)

    suspend fun subGroupDetail(
        token: String?,
        sub_group_id: String?
    ) = apiService.subGroupDetail(token, sub_group_id)

    suspend fun deleteSubGroup(
        token: String?,
        sub_group_id: String?
    ) = apiService.deleteSubGroup(token, sub_group_id)

    suspend fun editGroup(
        token: String?,
        group_name: String?,
        nature_group_id: String?,
        affect_gross_profit: String?,
        is_bank_account: String?,
        description: String?,
        group_id: String?

    ) = apiService.editGroup(
        token,
        group_name,
        nature_group_id,
        affect_gross_profit,
        is_bank_account,
        description,
        group_id
    )

    suspend fun editSubGroup(
        token: String?,
        group_name: String?,
        ledger_group_id: String?,
        is_bank_account: String?,
        description: String?,
        sub_group_id: String?

    ) = apiService.editSubGroup(
        token,
        group_name,
        ledger_group_id,
        is_bank_account,
        description,
        sub_group_id
    )

    suspend fun getNatureGroup(
        token: String?
    ) = apiService.getNatureGroup(token)

    suspend fun getParentGroup(
        token: String?
    ) = apiService.getParentGroup(token)

    suspend fun addMetalColour(
        token: String?,
        colour_name: String?,
        colour_code: String?,
        status: String?
    ) = apiService.addMetalColour(
        token, colour_name,
        colour_code,
        status
    )

    suspend fun getMetalColour(
        token: String?,
        status: String?
    ) = apiService.getMetalColours(token, status)

    suspend fun deleteMetalColour(
        token: String?,
        metal_colour_id: String?
    ) = apiService.deleteMetalColour(token, metal_colour_id)

    suspend fun editMetalColour(
        token: String?,
        metal_colour_id: String?,
        colour_name: String?,
        colour_code: String?,
        status: Number?
    ) = apiService.editMetalColour(
        token, metal_colour_id, colour_name,
        colour_code,
        status
    )

    suspend fun ledgerReportPrint(
        token: String?,
        from_date: String?,
        to_date: String?,
        report_type: String?,
        all_contacts: String?,
        type_of_contact: String?,
        contact_id: String?,
        all_cash_ledgers: String?,
        cash_ledger_id: String?,
        all_bank_ledgers: String?,
        bank_ledger_id: String?,
        period: String?,
        all_item_categories: String?,
        item_category_id: String?,
        all_items: String?,
        item_id: String?,
        all_ledgers: String?,
        ledger_id: String?
    ) = apiService.ledgerReportPrint(
        token,
        from_date,
        to_date,
        report_type,
        all_contacts,
        type_of_contact,
        contact_id,
        all_cash_ledgers,
        cash_ledger_id,
        all_bank_ledgers,
        bank_ledger_id,
        period,
        all_item_categories,
        item_category_id,
        all_items,
        item_id,
        all_ledgers,
        ledger_id

    )

    suspend fun deletecontactaddressinfo(
        token: String?,
        type: String?,
        edit_id: String?
    ) = apiService.deletecontactaddressinfo(token, type, edit_id)

    suspend fun searchListOpeningStock(
        token: String?,
        company_id: String?,
        current_page: Int?,
        name: String?,
        sort_by_column: String?,
        sort_type: String?,
        date_range_from: String?,
        date_range_to: String?
    ) = apiService.searchListOpeningStocks(
        token,
        company_id,
        current_page,
        name,
        sort_by_column,
        sort_type,
        date_range_from,
        date_range_to
    )

}