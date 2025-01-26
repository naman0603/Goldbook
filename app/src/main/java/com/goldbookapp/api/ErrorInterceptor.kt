// for any errors that should be handled before being handed off to RxJava. 
// In other words global error logic. 
// An example might be 401 when not logging in

import android.content.Intent
import com.goldbookapp.MyApplication
import com.goldbookapp.ui.AccessDeniedActivity
import com.goldbookapp.ui.activity.auth.LoginActivity
import com.goldbookapp.utils.Constants
import okhttp3.Interceptor
import okhttp3.Response

class ErrorInterceptor: Interceptor {

    /*private var level = HttpLoggingInterceptor.Level.NONE*/
    override fun intercept(chain: Interceptor.Chain?): Response {

        val originalResponse = chain!!.proceed(chain.request())

        if (shouldLogout(originalResponse)) {
            // your logout logic here

            try {
                if (originalResponse.code() == 401) {
                    if(Constants.apicallcount == 1) {

                    }
                    else{
                        MyApplication.appContext.startActivity(
                            Intent(
                                MyApplication.appContext,
                                LoginActivity::class.java
                            ).putExtra(Constants.Error, Constants.ErrorCode)
                                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_NEW_TASK)
                        )
                        Constants.apicallcount = 1

                    }

                } else if (originalResponse.code() == 403) {
                    if(Constants.apicallcount == 1) {

                    }
                    else{
                        MyApplication.appContext.startActivity(
                            Intent(
                                MyApplication.appContext,
                                AccessDeniedActivity::class.java
                            ).putExtra(Constants.AccessDeniedCode, Constants.AccessDeniedCode)
                                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_NEW_TASK)
                        )
                        Constants.apicallcount = 1
                    }

                }

                // send empty response down the chain
                if(Constants.apicallcount == 0){
                    Constants.apicallcount = 1
                    /*Log.v("apicallcount", Constants.apicallcount.toString())*/
                  //  Toast.makeText(MyApplication.appContext,Constants.apicallcount,Toast.LENGTH_SHORT).show()
                    return Response.Builder().build()
                }

            } catch (e: Exception) {
            }

        }

        //Constants.apicallcount=0
        //Log.v("apicallcount", Constants.apicallcount.toString())
      //  Toast.makeText(MyApplication.appContext,Constants.apicallcount,Toast.LENGTH_SHORT).show()
        return originalResponse
    }
    /*fun setLevel(level: HttpLoggingInterceptor.Level): ErrorInterceptor {
        if (level == null) throw NullPointerException("level == null. Use Level.NONE instead.")
        this.level = level
        return this
    }*/

    private fun shouldLogout(response: Response) : Boolean {
        if (response.code() == 401 || response.code() == 403) {
            return true
        }
        else{
            return false
        }

    }
}