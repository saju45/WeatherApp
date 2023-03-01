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
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.rlMainLayout.visibility = View.GONE

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        getCurrentLocation()

        binding.etCityName.setOnEditorActionListener ({ v, actionId, event ->

            if (actionId==EditorInfo.IME_ACTION_SEARCH){

                getCityWeather(binding.etCityName.text.toString(),)
                val view=this.currentFocus
                if (view!=null){
                    val imm:InputMethodManager=getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(view.windowToken,0)
                    binding.etCityName.clearFocus()
                }
                true
            } else false

        })

    }

    private fun getCityWeather(cityName: String){

        binding.progressBar.visibility=View.VISIBLE

        ApiUtilites.getApiInterface()!!.getCityWeatherData(cityName, API_KEY).enqueue(object : Callback<WeatherModel?> {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(call: Call<WeatherModel?>, response: Response<WeatherModel?>) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    setDataOnViews(response.body())
                }
            }

            override fun onFailure(call: Call<WeatherModel?>, t: Throwable) {

                Toast.makeText(this@MainActivity, "Error", Toast.LENGTH_SHORT).show()
            }
        })

    }

    fun getCurrentLocation() {

        if (checkPermission()) {

            if (isLocationEnabled()) {

                //final latitude and longitude here

                fusedLocationProviderClient.lastLocation.addOnCompleteListener(this) { task ->

                    val location: Location? = task.result

                    if (location == null) {

                        Toast.makeText(this, "Null Recieved ", Toast.LENGTH_SHORT).show()

                    } else {

                        //fetch weather here
                        fetchCurrentLocationWeather(
                            location.latitude.toString(),
                            location.longitude.toString()
                        )
                    }

                }
            } else {

                Toast.makeText(this, "Turn on location", Toast.LENGTH_SHORT).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }


        } else {

            requestPermissions()

        }
    }

    private fun fetchCurrentLocationWeather(latitude: String, longitude: String) {

        binding.progressBar.visibility = View.VISIBLE

        ApiUtilites.getApiInterface()?.getCurrentWeatherData(latitude, longitude, API_KEY)?.enqueue(
            object : Callback<WeatherModel?> {
                @RequiresApi(Build.VERSION_CODES.O)
                override fun onResponse(
                    call: Call<WeatherModel?>,
                    response: Response<WeatherModel?>
                ) {

                    if (response.isSuccessful) {

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

        val sdf = SimpleDateFormat("dd/MM/yyyy hh:mm")
        val currentData = sdf.format(Date())
        binding.dateAndTime.text = currentData

        binding.tvMaxTemp.text = "Day " + kelvinToCelsius(response!!.main.temp_max) + "°"
        binding.tvMinTemp.text = "Night " + kelvinToCelsius(response!!.main.temp_min) + "°"
        binding.tvTemp.text = "" + kelvinToCelsius(response!!.main.temp) + "°"
        binding.tvFeelLike.text = "" + kelvinToCelsius(response!!.main.feels_like) + "°"
        binding.tvWeatherType.text = response.weather[0].main
        binding.tvSunrise.text = timeStampToLocalDate(response.sys.sunrise.toLong())
        binding.tvSunset.text = timeStampToLocalDate(response.sys.sunset.toLong())
        binding.tvPressure.text = response.main.pressure.toString() + ""
        binding.tvHumidity.text = response.main.humidity.toString() + "%"
        binding.tvWindSpeed.text = response.wind.speed.toString() + "m/s"

        binding.tvTempFarenhite.text =
            "" + ((kelvinToCelsius(response.main.temp)).times(1.8).plus(32).roundToInt())
        binding.etCityName.setText(response.name)

        updateUI(response.weather[0].id)


    }

    private fun updateUI(id: Int) {

        if (id in 200..232) {

            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor = resources.getColor(R.color.thunderstorm)
            binding.rlToolbar.setBackgroundColor(resources.getColor(R.color.thunderstorm))

            binding.rlSubLayout.background = ContextCompat.getDrawable(
                this@MainActivity,
                R.drawable.thunderstoms_bg
            )

            binding.llMainBgBlow.background = ContextCompat.getDrawable(
                this@MainActivity,
                R.drawable.thunderstoms_bg
            )

            binding.llMainBgAvobe.background = ContextCompat.getDrawable(
                this@MainActivity,
                R.drawable.thunderstoms_bg
            )

            binding.weatherBg.setImageResource(R.drawable.thunderstoms_bg)
            binding.weatherIcon.setImageResource(R.drawable.thunderstorm2)


        } else if (id in 300..321) {

            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor = resources.getColor(R.color.drizzle)
            binding.rlToolbar.setBackgroundColor(resources.getColor(R.color.drizzle))

            binding.rlSubLayout.background = ContextCompat.getDrawable(
                this@MainActivity,
                R.drawable.drizzle_bg
            )

            binding.llMainBgBlow.background = ContextCompat.getDrawable(
                this@MainActivity,
                R.drawable.drizzle_bg
            )

            binding.llMainBgAvobe.background = ContextCompat.getDrawable(
                this@MainActivity,
                R.drawable.drizzle_bg
            )

            binding.weatherBg.setImageResource(R.drawable.drizzle_bg)
            binding.weatherIcon.setImageResource(R.drawable.drizzle)
        } else if (id in 500..531) {

            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor = resources.getColor(R.color.rain)
            binding.rlToolbar.setBackgroundColor(resources.getColor(R.color.rain))

            binding.rlSubLayout.background = ContextCompat.getDrawable(
                this@MainActivity,
                R.drawable.rain_bg
            )

            binding.llMainBgBlow.background = ContextCompat.getDrawable(
                this@MainActivity,
                R.drawable.rain_bg
            )

            binding.llMainBgAvobe.background = ContextCompat.getDrawable(
                this@MainActivity,
                R.drawable.rain_bg
            )

            binding.weatherBg.setImageResource(R.drawable.rain_bg)
            binding.weatherIcon.setImageResource(R.drawable.rain)
        } else if (id in 600..622) {

            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor = resources.getColor(R.color.snow)
            binding.rlToolbar.setBackgroundColor(resources.getColor(R.color.snow))

            binding.rlSubLayout.background = ContextCompat.getDrawable(
                this@MainActivity,
                R.drawable.snow_bg
            )

            binding.llMainBgBlow.background = ContextCompat.getDrawable(
                this@MainActivity,
                R.drawable.snow_bg
            )

            binding.llMainBgAvobe.background = ContextCompat.getDrawable(
                this@MainActivity,
                R.drawable.snow_bg
            )

            binding.weatherBg.setImageResource(R.drawable.snow_bg)
            binding.weatherIcon.setImageResource(R.drawable.snow2)
        } else if (id in 700..781) {

            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor = resources.getColor(R.color.atmosphere)
            binding.rlToolbar.setBackgroundColor(resources.getColor(R.color.atmosphere))

            binding.rlSubLayout.background = ContextCompat.getDrawable(
                this@MainActivity,
                R.drawable.misty_bg
            )

            binding.llMainBgBlow.background = ContextCompat.getDrawable(
                this@MainActivity,
                R.drawable.misty_bg
            )

            binding.llMainBgAvobe.background = ContextCompat.getDrawable(
                this@MainActivity,
                R.drawable.misty_bg
            )

            binding.weatherBg.setImageResource(R.drawable.misty_bg)
            binding.weatherIcon.setImageResource(R.drawable.mist)
        } else if (id == 800) {

            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor = resources.getColor(R.color.clear)
            binding.rlToolbar.setBackgroundColor(resources.getColor(R.color.clear))

            binding.rlSubLayout.background = ContextCompat.getDrawable(
                this@MainActivity,
                R.drawable.clear_bg
            )

            binding.llMainBgBlow.background = ContextCompat.getDrawable(
                this@MainActivity,
                R.drawable.clear_bg
            )

            binding.llMainBgAvobe.background = ContextCompat.getDrawable(
                this@MainActivity,
                R.drawable.clear_bg
            )

            binding.weatherBg.setImageResource(R.drawable.clear_bg)
            binding.weatherIcon.setImageResource(R.drawable.clear_sky)
        }

        binding.progressBar.visibility = View.GONE
        binding.rlMainLayout.visibility = View.VISIBLE


    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun timeStampToLocalDate(timeStamp: Long): String {

        val localTime = timeStamp.let {
            Instant.ofEpochSecond(it)
                .atZone(ZoneId.systemDefault())
                .toLocalTime()
        }

        return localTime.toString()
    }

    private fun kelvinToCelsius(tempMax: Double): Double {

        var intTem = tempMax
        intTem = intTem.minus(273)

        return intTem.toBigDecimal().setScale(1, RoundingMode.UP).toDouble()


    }

    private fun requestPermissions() {

        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ),
            PERMISSION_REQUEST_ACCESS_LOCATION
        )
    }


    private fun checkPermission(): Boolean {


        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            )
            == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            )
            == PackageManager.PERMISSION_GRANTED
        ) {

            return true
        }

        return false
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )

    }

    companion object {
        private const val PERMISSION_REQUEST_ACCESS_LOCATION = 101
        private val API_KEY = "4065fdd9885209fe08e8bd924bba35f7"

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSION_REQUEST_ACCESS_LOCATION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                Toast.makeText(this, "Granted", Toast.LENGTH_SHORT).show()
                getCurrentLocation()
            } else {

                Toast.makeText(this, "Denied", Toast.LENGTH_SHORT).show()
            }
        }

    }
}