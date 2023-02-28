package com.example.weatherapp.utilities

import com.example.weatherapp.Pojo.WeatherModel
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiInterface {


    @GET("weather")
    fun getCurrentWeatherData(
        @Query("lat") latitude:String,
        @Query("lon") longitude:String,
        @Query("appid") apiKey:String
    ):Call<WeatherModel>


    @GET("weather")
    fun getCityWeatherData(
        @Query("q") city_name:String,
        @Query("appid") apiKey:String

    ):Call<WeatherModel>
}