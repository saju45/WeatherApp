package com.example.weatherapp

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import com.example.weatherapp.Pojo.WeatherModel
import com.example.weatherapp.databinding.ActivityMainBinding
import com.example.weatherapp.utilities.ApiInterface
import com.example.weatherapp.utilities.ApiUtilites
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.util.*
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //binding.rlMainLayout.visibility=View.GONE

        fusedLocationProviderClient= LocationServices.getFusedLocationProviderClient(this)

        getCurrentLocation()

    }
    fun getCurrentLocation(){

        if(checkPermission()){

            if(isLocationEnabled()){

                //final latitude and longitude here

                fusedLocationProviderClient.lastLocation.addOnCompleteListener(this) { task->

                    val location: Location?=task.result

                    if(location==null){

                        Toast.makeText(this, "Null Recieved ", Toast.LENGTH_SHORT).show()

                    }else{

                        //fetch weather here
                        fetchCurrentLocationWeather(location.latitude.toString(),location.longitude.toString())
                    }

                }
            }else{

                Toast.makeText(this, "Turn on location", Toast.LENGTH_SHORT).show()
                val intent= Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }


        }else{

            requestPermissions()

        }
    }

    private fun fetchCurrentLocationWeather(latitude: String, longitude: String) {

        binding.progressBar.visibility=View.VISIBLE

        ApiUtilites.getApiInterface()?.getCurrentWeatherData(latitude,longitude,API_KEY)?.enqueue(
            object : Callback<WeatherModel?> {
                @RequiresApi(Build.VERSION_CODES.O)
                override fun onResponse(
                    call: Call<WeatherModel?>,
                    response: Response<WeatherModel?>
                ) {

                 if (response.isSuccessful){

                     setDataOnViews(response.body())
                 }

                }

                override fun onFailure(call: Call<WeatherModel?>, t: Throwable) {

                    Toast.makeText(this@MainActivity, "error", Toast.LENGTH_SHORT).show()
                }
            })



    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setDataOnViews(response: WeatherModel?) {

        val sdf=SimpleDateFormat("dd/MM/yyyy hh:mm")
        val currentData=sdf.format(Date())
        binding.dateAndTime.text=currentData

        binding.tvMaxTemp.text="Day "+kelvinToCelsius(response!!.main.temp_max)+"°"
        binding.tvMinTemp.text="Night "+kelvinToCelsius(response!!.main.temp_min)+"°"
        binding.tvTemp.text=""+kelvinToCelsius(response!!.main.temp)+"°"
        binding.tvFeelLike.text=""+kelvinToCelsius(response!!.main.feels_like)+"°"
        binding.tvWeatherType.text=response.weather[0].main
        binding.tvSunrise.text=timeStampToLocalDate(response.sys.sunrise.toLong())
        binding.tvSunset.text=timeStampToLocalDate(response.sys.sunset.toLong())
        binding.tvPressure.text=response.main.pressure.toString()+""
        binding.tvHumidity.text=response.main.humidity.toString()+"%"
        binding.tvWindSpeed.text=response.wind.speed.toString()+"m/s"

        binding.tvTempFarenhite.text=""+((kelvinToCelsius(response.main.temp)).times(1.8).plus(32).roundToInt())
        binding.etCityName.setText(response.name)

        updateUI(response.weather[0].id)







    }

    private fun updateUI(id: Int) {

        if (id in 200..232){


        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun timeStampToLocalDate(timeStamp:Long):String{

        val localTime=timeStamp.let {
            Instant.ofEpochSecond(it)
                .atZone(ZoneId.systemDefault())
                .toLocalTime()
        }

        return localTime.toString()
    }

    private fun kelvinToCelsius(tempMax: Double): Double {

        var intTem=tempMax
        intTem=intTem.minus(273)

        return intTem.toBigDecimal().setScale(1,RoundingMode.UP).toDouble()


    }

    private fun requestPermissions(){

        ActivityCompat.requestPermissions(this,
            arrayOf( android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_FINE_LOCATION),
            PERMISSION_REQUEST_ACCESS_LOCATION
        )
    }


    private fun checkPermission():Boolean{


        if(ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION)
            == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED){

            return true
        }

        return false
    }

    private fun isLocationEnabled():Boolean{
        val locationManager: LocationManager =getSystemService(Context.LOCATION_SERVICE) as LocationManager

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)|| locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )

    }

    companion object{
        private const val PERMISSION_REQUEST_ACCESS_LOCATION=101
        private val API_KEY="4065fdd9885209fe08e8bd924bba35f7"

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if(requestCode== PERMISSION_REQUEST_ACCESS_LOCATION){
            if (grantResults.isNotEmpty() && grantResults[0]== PackageManager.PERMISSION_GRANTED){

                Toast.makeText(this, "Granted", Toast.LENGTH_SHORT).show()
                getCurrentLocation()
            }else{

                Toast.makeText(this, "Denied", Toast.LENGTH_SHORT).show()
            }
        }

    }
}