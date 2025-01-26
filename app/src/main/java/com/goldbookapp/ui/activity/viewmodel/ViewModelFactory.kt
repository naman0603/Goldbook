package com.goldbookapp.ui.activity.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.goldbookapp.api.ApiHelper
import com.goldbookapp.ui.activity.*
import com.goldbookapp.ui.fragment.viewmodel.*
import com.goldbookapp.ui.ui.gallery.CustomersViewModel
import com.goldbookapp.ui.ui.gallery.ReportsViewModel
import com.example.goldbookapp.ui.activity.viewmodel.SettingsViewModel
import com.goldbookapp.ui.ui.home.DashboardViewModel
import com.goldbookapp.ui.ui.send.*
import com.goldbookapp.ui.ui.share.OpeningStockFragViewModel
import com.goldbookapp.ui.ui.share.PurchaseViewModel

class ViewModelFactory(private val apiHelper: ApiHelper) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            return LoginViewModel(apiHelper) as T
        } else if (modelClass.isAssignableFrom(SignupViewModel::class.java)) {
            return SignupViewModel(apiHelper) as T
        } else if (modelClass.isAssignableFrom(AlmostThereViewModel::class.java)) {
            return AlmostThereViewModel(apiHelper) as T
        } else if (modelClass.isAssignableFrom(VerifyPhoneOTPViewModel::class.java)) {
            return VerifyPhoneOTPViewModel(apiHelper) as T
        } else if (modelClass.isAssignableFrom(QuickSetupSuggestionViewModel::class.java)) {
            return QuickSetupSuggestionViewModel(apiHelper) as T
        } else if (modelClass.isAssignableFrom(RecoverAccountViewModel::class.java)) {
            return RecoverAccountViewModel(apiHelper) as T
        } else if (modelClass.isAssignableFrom(VerifyPasswordViewModel::class.java)) {
            return VerifyPasswordViewModel(apiHelper) as T
        } else if (modelClass.isAssignableFrom(UpdateContactViewModel::class.java)) {
            return UpdateContactViewModel(apiHelper) as T
        } else if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            return ProfileViewModel(apiHelper) as T
        } else if (modelClass.isAssignableFrom(EditProfileViewModel::class.java)) {
            return EditProfileViewModel(apiHelper) as T
        } else if (modelClass.isAssignableFrom(AddressDetailsViewModel::class.java)) {
            return AddressDetailsViewModel(apiHelper) as T
        } else if (modelClass.isAssignableFrom(CompanyAddressDetailsViewModel::class.java)) {
            return CompanyAddressDetailsViewModel(apiHelper) as T
        } else if (modelClass.isAssignableFrom(OrganizationDetailViewModel::class.java)) {
            return OrganizationDetailViewModel(apiHelper) as T
        } else if (modelClass.isAssignableFrom(NewItemCatViewModel::class.java)) {
            return NewItemCatViewModel(apiHelper) as T
        } else if (modelClass.isAssignableFrom(EditItemCatViewModel::class.java)) {
            return EditItemCatViewModel(apiHelper) as T
        } else if (modelClass.isAssignableFrom(NewCustomerViewModel::class.java)) {
            return NewCustomerViewModel(apiHelper) as T
        } else if (modelClass.isAssignableFrom(NewOrgnizationViewModel::class.java)) {
            return NewOrgnizationViewModel(apiHelper) as T
        } else if (modelClass.isAssignableFrom(EditBranchViewModel::class.java)) {
            return EditBranchViewModel(apiHelper) as T
        } else if (modelClass.isAssignableFrom(BranchDetailViewModel::class.java)) {
            return BranchDetailViewModel(apiHelper) as T
        } else if (modelClass.isAssignableFrom(NewCompanyBranchViewModel::class.java)) {
            return NewCompanyBranchViewModel(apiHelper) as T
        } else if (modelClass.isAssignableFrom(CustomersViewModel::class.java)) {
            return CustomersViewModel(apiHelper) as T
        }else if (modelClass.isAssignableFrom(ReportsViewModel::class.java)) {
            return ReportsViewModel(apiHelper) as T
        }else if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            return SettingsViewModel(apiHelper) as T
        }else if (modelClass.isAssignableFrom(CustomerDetailsViewModel::class.java)) {
            return CustomerDetailsViewModel(apiHelper) as T
        } else if (modelClass.isAssignableFrom(ContactInfoViewModel::class.java)) {
            return ContactInfoViewModel(apiHelper) as T
        } else if (modelClass.isAssignableFrom(TransactionViewModel::class.java)) {
            return TransactionViewModel(apiHelper) as T
        } else if (modelClass.isAssignableFrom(EditCustomerViewModel::class.java)) {
            return EditCustomerViewModel(apiHelper) as T
        } else if (modelClass.isAssignableFrom(EditOrganizationViewModel::class.java)) {
            return EditOrganizationViewModel(apiHelper) as T
        } else if (modelClass.isAssignableFrom(ItemCategoriesListViewModel::class.java)) {
            return ItemCategoriesListViewModel(apiHelper) as T
        } else if (modelClass.isAssignableFrom(NewSupplierViewModel::class.java)) {
            return NewSupplierViewModel(apiHelper) as T
        } else if (modelClass.isAssignableFrom(SupplierViewModel::class.java)) {
            return SupplierViewModel(apiHelper) as T
        } else if (modelClass.isAssignableFrom(SupplierDetailsViewModal::class.java)) {
            return SupplierDetailsViewModal(apiHelper) as T
        } else if (modelClass.isAssignableFrom(SupplierContactInfoViewModel::class.java)) {
            return SupplierContactInfoViewModel(apiHelper) as T
        } else if (modelClass.isAssignableFrom(EditSuplierViewModel::class.java)) {
            return EditSuplierViewModel(apiHelper) as T
        } else if (modelClass.isAssignableFrom(MainActivityViewModel::class.java)) {
            return MainActivityViewModel(apiHelper) as T
        } else if (modelClass.isAssignableFrom(ItemsViewModel::class.java)) {
            return ItemsViewModel(apiHelper) as T
        } else if (modelClass.isAssignableFrom(NewItemViewModel::class.java)) {
            return NewItemViewModel(apiHelper) as T
        } else if (modelClass.isAssignableFrom(OpeningStockFragViewModel::class.java)) {
            return OpeningStockFragViewModel(apiHelper) as T
        } else if (modelClass.isAssignableFrom(ItemCategoryDetailViewModel::class.java)) {
            return ItemCategoryDetailViewModel(apiHelper) as T
        } else if (modelClass.isAssignableFrom(ItemDetailViewModel::class.java)) {
            return ItemDetailViewModel(apiHelper) as T
        } else if (modelClass.isAssignableFrom(EditItemViewModel::class.java)) {
            return EditItemViewModel(apiHelper) as T
        } else if (modelClass.isAssignableFrom(SaleViewModel::class.java)) {
            return SaleViewModel(apiHelper) as T
        } else if (modelClass.isAssignableFrom(OrganizationViewModel::class.java)) {
            return OrganizationViewModel(apiHelper) as T
        } else if (modelClass.isAssignableFrom(BranchViewModel::class.java)) {
            return BranchViewModel(apiHelper) as T
        } else if (modelClass.isAssignableFrom(NewInvoiceViewModel::class.java)) {
            return NewInvoiceViewModel(apiHelper) as T
        } else if (modelClass.isAssignableFrom(NewPurchaseViewModel::class.java)) {
            return NewPurchaseViewModel(apiHelper) as T
        } else if (modelClass.isAssignableFrom(AddItemViewModel::class.java)) {
            return AddItemViewModel(apiHelper) as T
        } else if (modelClass.isAssignableFrom(AddItemPaymentViewModel::class.java)) {
            return AddItemPaymentViewModel(apiHelper) as T
        } else if (modelClass.isAssignableFrom(SaleDetailsViewModel::class.java)) {
            return SaleDetailsViewModel(apiHelper) as T
        } else if (modelClass.isAssignableFrom(PaymentDetailViewModel::class.java)) {
            return PaymentDetailViewModel(apiHelper) as T
        } else if (modelClass.isAssignableFrom(ReceiptDetailViewModel::class.java)) {
            return ReceiptDetailViewModel(apiHelper) as T
        } else if (modelClass.isAssignableFrom(PurchaseViewModel::class.java)) {
            return PurchaseViewModel(apiHelper) as T
        } else if (modelClass.isAssignableFrom(PurchaseDetailsViewModel::class.java)) {
            return PurchaseDetailsViewModel(apiHelper) as T
        } else if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
            return DashboardViewModel(apiHelper) as T
        } else if (modelClass.isAssignableFrom(UpdatePasswordViewModel::class.java)) {
            return UpdatePasswordViewModel(apiHelper) as T
        } else if (modelClass.isAssignableFrom(TermsOfServiceViewModel::class.java)) {
            return TermsOfServiceViewModel(apiHelper) as T
        } else if (modelClass.isAssignableFrom(PaymentViewModel::class.java)) {
            return PaymentViewModel(apiHelper) as T
        } else if (modelClass.isAssignableFrom(ReceiptViewModel::class.java)) {
            return ReceiptViewModel(apiHelper) as T
        } else if (modelClass.isAssignableFrom(NewPaymentViewModel::class.java)) {
            return NewPaymentViewModel(apiHelper) as T
        } else if (modelClass.isAssignableFrom(NewReceiptViewModel::class.java)) {
            return NewReceiptViewModel(apiHelper) as T
        }  else if (modelClass.isAssignableFrom(ReportTypesCommonViewModel::class.java)) {
            return ReportTypesCommonViewModel(apiHelper) as T
        }  else if (modelClass.isAssignableFrom(UpdateContactVerifyOTPViewModel::class.java)) {
            return UpdateContactVerifyOTPViewModel(apiHelper) as T
        } else if (modelClass.isAssignableFrom(AboutUsViewModel::class.java)) {
            return AboutUsViewModel(apiHelper) as T
        } else if (modelClass.isAssignableFrom(WebLinksViewModel::class.java)) {
            return WebLinksViewModel(apiHelper) as T
        } else if (modelClass.isAssignableFrom(SubscriptionViewModel::class.java)) {
            return SubscriptionViewModel(apiHelper) as T
        } else if (modelClass.isAssignableFrom(CustomPinViewModel::class.java)) {
            return CustomPinViewModel(apiHelper) as T
        } else if (modelClass.isAssignableFrom(PreferencesActivityViewModel::class.java)) {
            return PreferencesActivityViewModel(apiHelper) as T
        } else if (modelClass.isAssignableFrom(SettingsContactViewModel::class.java)) {
            return SettingsContactViewModel(apiHelper) as T
        } else if (modelClass.isAssignableFrom(SaveTaxesViewModel::class.java)) {
            return SaveTaxesViewModel(apiHelper) as T
        } else if (modelClass.isAssignableFrom(TaxTcsDetailViewModel::class.java)) {
            return TaxTcsDetailViewModel(apiHelper) as T
        }
        else if (modelClass.isAssignableFrom(LedgerViewModel::class.java)) {
            return LedgerViewModel(apiHelper) as T
        } else if (modelClass.isAssignableFrom(NewLedgerViewModel::class.java)) {
            return NewLedgerViewModel(apiHelper) as T
        } else if (modelClass.isAssignableFrom(LedgerDetailsViewModal::class.java)) {
            return LedgerDetailsViewModal(apiHelper) as T
        } else if (modelClass.isAssignableFrom(GroupViewModel::class.java)) {
            return GroupViewModel(apiHelper) as T
        } else if (modelClass.isAssignableFrom(GroupDetailsViewModal::class.java)) {
            return GroupDetailsViewModal(apiHelper) as T
        } else if (modelClass.isAssignableFrom(SubGroupDetailsViewModal::class.java)) {
            return SubGroupDetailsViewModal(apiHelper) as T
        } else if (modelClass.isAssignableFrom(NewGroupViewModel::class.java)) {
            return NewGroupViewModel(apiHelper) as T
        } else if (modelClass.isAssignableFrom(EditLedgerViewModel::class.java)) {
            return EditLedgerViewModel(apiHelper) as T
        } else if (modelClass.isAssignableFrom(EditGroupViewModel::class.java)) {
            return EditGroupViewModel(apiHelper) as T
        } else if (modelClass.isAssignableFrom(EditSubGroupViewModel::class.java)) {
            return EditSubGroupViewModel(apiHelper) as T
        } else if (modelClass.isAssignableFrom(EditMetalColourViewModel::class.java)) {
            return EditMetalColourViewModel(apiHelper) as T
        } else if (modelClass.isAssignableFrom(AddMetalColourViewModel::class.java)) {
            return AddMetalColourViewModel(apiHelper) as T
        } else if (modelClass.isAssignableFrom(MetalColourListViewModel::class.java)) {
            return MetalColourListViewModel(apiHelper) as T
        } else if (modelClass.isAssignableFrom(MetalColourDetailViewModel::class.java)) {
            return MetalColourDetailViewModel(apiHelper) as T
        } else if (modelClass.isAssignableFrom(BillingAddressDetailViewModel::class.java)) {
            return BillingAddressDetailViewModel(apiHelper) as T
        } else if (modelClass.isAssignableFrom(CustSuppTcsTdsViewModel::class.java)) {
            return CustSuppTcsTdsViewModel(apiHelper) as T
        }else if (modelClass.isAssignableFrom(CashPayRecViewModel::class.java)) {
            return CashPayRecViewModel(apiHelper) as T
        }else if (modelClass.isAssignableFrom(OpeningStockDetailViewModel::class.java)) {
            return OpeningStockDetailViewModel(apiHelper) as T
        }else if (modelClass.isAssignableFrom(NewOpeningStockViewModel::class.java)) {
            return NewOpeningStockViewModel(apiHelper) as T
        }else if (modelClass.isAssignableFrom(TaxAnalysisViewModel::class.java)) {
            return TaxAnalysisViewModel(apiHelper) as T
        }
        throw IllegalArgumentException("Unknown class name")
    }

}