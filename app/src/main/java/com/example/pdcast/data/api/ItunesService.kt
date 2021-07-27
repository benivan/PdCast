package com.example.pdcast.data.api

import com.example.pdcast.data.response.PodcastResponse
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface ItunesService {
    @GET("/search?entity=podcast")
    suspend fun searchPodcastByTerms(
        @Query("term") term:String
    ):PodcastResponse


    companion object{

        private const val baseUrl ="https://itunes.apple.com"

        private  val client = OkHttpClient.Builder()
            .build()

        fun createItunesService():ItunesService{
            return Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build()
                .create(ItunesService::class.java)
        }
    }
}