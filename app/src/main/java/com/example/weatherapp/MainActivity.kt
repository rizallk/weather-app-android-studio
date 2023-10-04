package com.example.weatherapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.weatherapp.ui.theme.WeatherAppTheme
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.weatherapp.api.WeatherApi
import kotlin.math.round

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WeatherAppTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    MyScreen()
                }
            }
        }
    }
}

data class MainModel(
    var temp: Double,
    var feels_like: Double,
    var temp_min: Double,
    var temp_max: Double,
    var pressure: Int,
    var humidity: Int
)

data class WeatherModel(
    var id: Int,
    var main: String,
    var description: String,
    var icon: String
)

data class MyModel(
    var main: MainModel,
    var weather: List<WeatherModel>
)

@Composable
fun MyScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        AppBar()
        Content()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBar() {
    TopAppBar(
        title = {
            Text(text = "Metric Converter")
        },
        colors = TopAppBarDefaults.smallTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
            actionIconContentColor = MaterialTheme.colorScheme.onSecondary
        ),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Content() {
    val q = remember {
        mutableStateOf(TextFieldValue())
    }

    val main = remember {
        mutableStateOf(MainModel(
            temp = 0.0,
            feels_like = 0.0,
            temp_min = 0.0,
            temp_max = 0.0,
            pressure = 0,
            humidity = 0
        ))
    }

    val weather = remember {
        mutableStateOf(WeatherModel(
            id = 0,
            main = "",
            description = "",
            icon = ""
        ))
    }

    val isLoading = remember {
        mutableStateOf(false)
    }

    val location = remember {
        mutableStateOf("Manado")
    }

    val dataNull = remember {
        mutableStateOf(false)
    }

    val mainData = main.component1()
    val weatherData = weather.component1()


    LaunchedEffect(true) {
        sendRequest(q = "Manado", mainState = main, weatherState = weather, dataNull = dataNull)
    }

    Column(
        modifier = Modifier.padding(all = 16.dp)
    ) {
        Row{
            TextField(
                value = q.value,
                onValueChange = { q.value = it },
                placeholder = {
                    Text(text = "Cari lokasi")
                },
                singleLine = true,
                maxLines = 1,
                modifier = Modifier.heightIn(min = 56.dp)
            )
            Button(
                onClick = {
                    sendRequest(
                        q = q.value.text,
                        mainState = main,
                        weatherState = weather,
                        dataNull = dataNull
                    )
                    main.value.temp = 0.0
                    isLoading.value = true
                    location.value = q.value.text
                },
                shape = RoundedCornerShape(0.dp),
                modifier = Modifier
                    .heightIn(min = 56.dp)
                    .width(300.dp)
            ) {
                Text(text = "Cari", softWrap = false)
            }
        }

        Spacer(modifier = Modifier.height(17.dp))

        val boxSize = with(LocalDensity.current) { 300.dp.toPx() }

        Box (
            modifier = Modifier
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFF06b6d4), Color(0xFF6390f1)),
                        start = Offset(0f, 0f), // top left corner
                        end = Offset(boxSize, boxSize) // bottom right corner
                    )
                )
        ) {
            Card (
                colors = CardDefaults.cardColors(
                    containerColor = Color.Transparent,
                ),
                modifier = Modifier.padding(all = 12.dp)
            ) {
                Column (
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                ) {
                    if (mainData.temp > 0) {
                        isLoading.value = false
                        dataNull.value = false
                    }
                    if (dataNull.value) Text(text = "Lokasi tidak ditemukan.", color = Color.White)
                    if (isLoading.value && !dataNull.value) {
                        Text(text = "Loading...", color = Color.White)
                    } else {
                        if (mainData.temp > 0) {
                            Text(
                                text = location.value.replaceFirstChar { it.uppercase() },
                                fontSize = 26.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Medium,
                            )
                            Row(
                                modifier = Modifier.padding(top = 42.dp, bottom = 38.dp)
                            ) {
                                Text(
                                    text = round(mainData.temp - 273.15).toInt().toString(),
                                    fontSize = 90.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "° C",
                                    color = Color.White,
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(top = 20.dp, start = 3.dp)
                                )
                            }
                            Text(
                                text = "Feels like ${round(mainData.feels_like - 273.15).toInt()}°",
                                fontSize = 18.sp,
                                color = Color.White
                            )
                            Row (
                                modifier = Modifier.padding(top = 15.dp)
                            ) {
                                AsyncImage(
                                    model = "https://openweathermap.org/img/w/${weatherData.icon}.png",
                                    contentDescription = "Weather Icon",
                                    contentScale = ContentScale.FillBounds,
                                    modifier = Modifier
                                        .padding(end = 5.dp, top = 3.dp)
                                        .height(80.dp)
                                        .width(50.dp)
                                )
                                Text(
                                    text = weatherData.description,
                                    fontSize = 18.sp,
                                    color = Color.White
                                )
                            }
                        } else if(!dataNull.value) {
                            Text(text = "Loading...", color = Color.White)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        Text(text = "Aplikasi ini dibuat oleh", modifier = Modifier.padding(bottom = 14.dp))
        Text(text = "Nama : Mohalim Rizal Kadamong")
        Text(text = "NIM : 210211060138")

        Spacer(modifier = Modifier.height(17.dp))
    }
}

fun sendRequest(
    q: String,
    dataNull: MutableState<Boolean>,
    mainState: MutableState<MainModel>,
    weatherState: MutableState<WeatherModel>
) {
    val apiKey = "332051efaa5e49bb9da62feaa09e7007"
    val url = "https://api.openweathermap.org"

    val retrofit = Retrofit.Builder()
        .baseUrl(url)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val api = retrofit.create(WeatherApi::class.java)

    val call: Call<MyModel?>? = api.getData(q, apiKey)

    call!!.enqueue(object: Callback<MyModel?> {
        override fun onResponse(call: Call<MyModel?>, response: Response<MyModel?>) {
            if(response.isSuccessful) {
                Log.d("MainAPI", "success! " + response.body().toString())
                mainState.value = response.body()!!.main
                weatherState.value = response.body()!!.weather[0]
            }
            if(response.body() == null) {
                dataNull.value = true
            }
            Log.d("MainAPI", "response: ${response.body()}")
        }

        override fun onFailure(call: Call<MyModel?>, t: Throwable) {
            Log.e("MainAPI", "Failed mate " + t.message.toString())
        }
    })
}