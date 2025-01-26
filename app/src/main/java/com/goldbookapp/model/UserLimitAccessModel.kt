data class UserLimitAccessModel(
    val data: Data?,
    val status: Boolean?,
    val message: String?,
    val code: String?
) {
    data class Data(
        val can_add_company: String?,
        val message_company: String?,
        val can_add_branch: String?,
        val message_branch: String?,
        val can_add_sales: String?,
        val message_sales: String?,
        val can_add_purchase: String?,
        val message_purchase: String?,
        val can_add_payment: String?,
        val message_payment: String?,
        val can_add_receipt: String?,
        val message_receipt: String?
    )

}
/*

data class Base(val data: Data?, val code: String?, val message: String?, val status: Boolean?)

data class Data(val can_add_company: Number?, val can_add_branch: Number?, val can_add_sales: Number?, val can_add_purchase: Number?, val can_add_payment: Number?, val can_add_receipt: Number?)*/
