package com.goldbookapp.api

import ErrorInterceptor
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


object RetrofitBuilder {

    // private const val BASE_URL = "https://goldbook.in/api/"
       private const val BASE_URL = "https://sandbox.goldbook.in/api/"


    private const val REQUEST_TIMEOUT = 60

    private fun getRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(getHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build() //Doesn't require the adapter
    }

    fun getHttpClient() : OkHttpClient{
        val client = OkHttpClient().newBuilder()
            .connectTimeout(REQUEST_TIMEOUT.toLong(), TimeUnit.SECONDS)
            .readTimeout(REQUEST_TIMEOUT.toLong(), TimeUnit.SECONDS)
            .writeTimeout(REQUEST_TIMEOUT.toLong(), TimeUnit.SECONDS)


        //if(BuildConfig.DEBUG){
        // error interceptor
        val errorInterceptor = ErrorInterceptor()
        // errorInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
        client.addInterceptor(errorInterceptor)


        // http interceptor
        val httpinterceptor = HttpLoggingInterceptor()
        httpinterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
        client.addInterceptor(httpinterceptor)
        //  }

        return client.build();
    }

    fun <S> createService(serviceClass: Class<S>): S {
        return getRetrofit().create(serviceClass)
    }
    val apiService: ApiService = getRetrofit().create(ApiService::class.java)


}