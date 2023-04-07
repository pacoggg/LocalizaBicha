package com.example.localizabicha
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback

import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import android.widget.Button
import android.widget.TextView
import com.google.android.gms.maps.model.Marker
//import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject

@Suppress("NAME_SHADOWING")
class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var mMarker: Marker // Variable para guardar la referencia del Marker
    private var lat: Double = 0.0
    private var lng: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Obtener el SupportMapFragment y notificar cuando el mapa esté listo para ser utilizado.
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        // Obtener los TextView para la temperatura y la velocidad
        val textViewTemp = findViewById<TextView>(R.id.textViewTemp)
        val textViewVel = findViewById<TextView>(R.id.textViewVel)

        // Llamada a ThingSpeak API para obtener los datos del vehículo más recientes
        val url = "https://api.thingspeak.com/channels/2090424/feeds.json?api_key=GIO7NQE7IRAEC42W&results=1"
        val queue = Volley.newRequestQueue(this)

        val jsonObjectRequest = JsonObjectRequest(Request.Method.GET, url, null,
            Response.Listener { response ->
                // Parsear el JSON y obtener las latitud y longitud del vehículo más recientes
                val feedArray = response.getJSONArray("feeds")
                val feedObj = feedArray.getJSONObject(0)
                lat = feedObj.getString("field1").toDouble()
                lng = feedObj.getString("field2").toDouble()
                // Obtener la temperatura y la velocidad del JSON
                val temperatura = feedObj.getDouble("field3")
                val velocidad = feedObj.getDouble("field5")
                // Establecer la temperatura y la velocidad en los TextView
                textViewTemp.text = "$temperatura ºC"
                textViewVel.text = "$velocidad Km/h"
                // Actualizar la posición del vehículo en el mapa
                val latLng = LatLng(lat, lng)
                mMap.clear()
                mMarker = mMap.addMarker(MarkerOptions().position(latLng))!!
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f))
            },
            Response.ErrorListener { error ->
                Toast.makeText(this, "Error al obtener los datos del vehículo: ${error.message}", Toast.LENGTH_LONG).show()
            }
        )
        val btnUpdate = findViewById<Button>(R.id.btnUpdate)
        btnUpdate.setOnClickListener {
            // Eliminar el Marker anterior si existe
            if (::mMarker.isInitialized) {
                mMarker.remove()
            }
            // Obtener la última posición del vehículo desde ThingSpeak y actualizar la posición en el mapa
            val url = "https://api.thingspeak.com/channels/2090424/feeds.json?api_key=GIO7NQE7IRAEC42W&results=1"
            val requestQueue = Volley.newRequestQueue(this)
            val jsonObjectRequest = JsonObjectRequest(
                Request.Method.GET, url, null,
                { response ->
                    // Parsear el JSON y obtener las latitud y longitud del vehículo más recientes
                    val feedArray = response.getJSONArray("feeds")
                    val feedObj = feedArray.getJSONObject(0)
                    lat = feedObj.getString("field1").toDouble()
                    lng = feedObj.getString("field2").toDouble()// Obtener la temperatura y la velocidad del JSON
                    val temperatura = feedObj.getDouble("field3")
                    val velocidad = feedObj.getDouble("field5")
                    // Establecer la temperatura y la velocidad en los TextView
                    textViewTemp.text = "$temperatura ºC"
                    textViewVel.text = "$velocidad Km/h"
                    actualizarPosicionEnMapa(lat, lng)
                },
                { error ->
                    Toast.makeText(this, "Error al obtener la posición: $error", Toast.LENGTH_SHORT).show()
                }
            )
            requestQueue.add(jsonObjectRequest)
        }

        // Agregar la solicitud a la cola de solicitudes del Volley
        queue.add(jsonObjectRequest)
    }
    private fun actualizarPosicionEnMapa(latitud: Double, longitud: Double) {
        val posicion = LatLng(latitud, longitud)
        val markerOptions = MarkerOptions().position(posicion).title("Posición actual")
        mMarker = mMap.addMarker(markerOptions)!!
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(posicion, 15f))
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
    }
}
