package com.example.pdcast.data.api

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.*
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException




object RssFeedService {

    fun getFeed(xmlFileURL: String, callBack: ApiResponse<String>) {

        val client = OkHttpClient()

        val request = Request.Builder()
            .url(xmlFileURL)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                callBack.onFailure(e)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        callBack.onSuccess(it.string())
                    }
                }
                callBack.onFailure(Throwable("Response not successful"))
            }

        })


    }

    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun getFeedXml(xmlFileURL: String):
            String = suspendCancellableCoroutine {

        val client = OkHttpClient()

        val request = Request.Builder()
            .url(xmlFileURL)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                it.resumeWithException(exception = Throwable(e))
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                     response.body()?.let { body ->  it.resume(body.string())}
                }
            }
        })
    }


}
interface ApiResponse<T> {
    fun onSuccess(data: T)
    fun onFailure(throwable: Throwable)
}






