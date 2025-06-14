package com.pasundane_budget.di

import okhttp3.Interceptor
import okhttp3.Response

class AcceptHeaderInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val newRequest = chain.request().newBuilder()
            .addHeader("Accept", "application/json")
            .build()
        return chain.proceed(newRequest)
    }
}
