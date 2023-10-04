package com.example.weatherapp.api

import com.example.weatherapp.MyModel
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApi {
    @GET("/data/2.5/weather")
    fun getData(
        @Query("q") q: String,
        @Query("appid") appid: String
    ): Call<MyModel?>?
}