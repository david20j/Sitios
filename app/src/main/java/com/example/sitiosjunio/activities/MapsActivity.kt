package com.example.sitiosjunio.activities

import android.annotation.SuppressLint
import android.app.Dialog
import android.app.DownloadManager
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.AsyncTask
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.v4.app.ActivityCompat
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.example.mylibrarytoolbar.ToolbarActivity
import com.example.sitiosjunio.GoogleMapDTO
import com.example.sitiosjunio.R
import com.example.sitiosjunio.activities.models.Lugar
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.gms.tasks.OnSuccessListener
import com.squareup.okhttp.OkHttpClient
import kotlinx.android.synthetic.main.activity_add_site.*
import kotlinx.android.synthetic.main.activity_detail_site.*
import kotlinx.android.synthetic.main.activity_maps.*
import java.lang.Exception

class MapsActivity : ToolbarActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var lugar: Lugar

    //----------VARIABLES UBICACION----------
    private val permisoFineLocation = android.Manifest.permission.ACCESS_FINE_LOCATION
    private val permisoCoarseLocation = android.Manifest.permission.ACCESS_COARSE_LOCATION
    private val CODIGO_SOLICITUD_PERMISO = 100

    private var fusedLocationClient: FusedLocationProviderClient? = null
    private var locationRequest: LocationRequest? = null
    private var callback: LocationCallback? = null

    private var miPosicion: LatLng? = null
    //----------FIN VARIABLES UBICACION----------

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        toolbarToLoad(toolbarViewMap as Toolbar)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val value: Bundle ?= intent.extras
        lugar =  value?.get("lugar") as Lugar

        fusedLocationClient = FusedLocationProviderClient(this)
        inicializarLocationRequest()

        callback = object:LocationCallback(){
            override fun onLocationResult(locations: LocationResult?) {
                super.onLocationResult(locations)
                for(location in locations?.locations!!){
                    miPosicion = LatLng(location.latitude, location.longitude)
                }
            }
        }

    }

    //MENU
   /* override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_map, menu)
        menu?.getItem(0)?.isEnabled = true
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId){
            R.id.menu_route -> {
                    //val URL = getDirectionURL(miPosicion!!, LatLng(lugar.latitud, lugar.longitud))
                    val URL = getDirectionURL(LatLng(42.305539, -1.968979), LatLng(42.303049, -1.967100))
                    Log.d("MyLog", miPosicion.toString()+ "  "+lugar.latitud +","+lugar.longitud)
                    GetDirection(URL, this).execute()
                }

        }
        return super.onOptionsItemSelected(item)
    }*/
    //FIN MENU

    //----------METODOS UBICACION----------
    private fun inicializarLocationRequest(){
        locationRequest = LocationRequest()
        locationRequest?.interval = 10000
        locationRequest?.fastestInterval = 5000
        locationRequest?.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    override fun onStart() {
        super.onStart()

        if(validadrPermisosUbicacion())
            obtenerUbicacion()
        else
            pedirPermisos()
    }

    override fun onPause() {
        super.onPause()
        detenerActualizacion()
    }

    private fun obtenerUbicacion() {
        fusedLocationClient?.requestLocationUpdates(locationRequest, callback, null)
    }

    private fun detenerActualizacion(){
        fusedLocationClient?.removeLocationUpdates(callback)
    }
    //permisos
    private fun pedirPermisos() {
        val deboProveerContexto = ActivityCompat.shouldShowRequestPermissionRationale(this, permisoFineLocation)

        if(deboProveerContexto)

        else
            solicitudPermiso()
    }

    private fun solicitudPermiso() {
        requestPermissions(arrayOf(permisoFineLocation, permisoCoarseLocation), CODIGO_SOLICITUD_PERMISO)
    }

    private fun validadrPermisosUbicacion(): Boolean {
        val hayUbicacionPrecisa = ActivityCompat.checkSelfPermission(this, permisoFineLocation) == PackageManager.PERMISSION_GRANTED
        val hayUbicacionOrdinaria = ActivityCompat.checkSelfPermission(this, permisoCoarseLocation) == PackageManager.PERMISSION_GRANTED

        return hayUbicacionPrecisa && hayUbicacionOrdinaria
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when(requestCode){
            CODIGO_SOLICITUD_PERMISO ->{
                if(grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    obtenerUbicacion()
                else
                    Toast.makeText(this, "No diste permiso para acceder a ubicacion", Toast.LENGTH_LONG).show()
            }
        }
    }

    //----------FIN METODOS UBICACION----------
    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.isMyLocationEnabled = true
        mMap.uiSettings.isMyLocationButtonEnabled = true
        mMap.uiSettings.isZoomControlsEnabled = true
        val ubicacionLugar = LatLng(lugar.latitud, lugar.longitud)


        val markerOptions = MarkerOptions()
            .position(ubicacionLugar)
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)) // Color del marcador
            .alpha(0.7f)                                                                   // Transparencia del marcador
            .title(lugar.nombre)
        mMap.addMarker(markerOptions)
            .isDraggable = true
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ubicacionLugar, 16.0f))
        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.estilo_map)) //Cambia estilo de mapa por uno personalizado.
        //line = mMap.addPolyline(PolylineOptions().add(LatLng(lugar.latitud, lugar.longitud)).width(5f).color(Color.RED))



    }
    /*//Ruta
    fun getDirectionURL(origin:LatLng, dest:LatLng) = "https://maps.googleapis.com/maps/api/directions/json?origin=${origin.latitude},${origin.longitude}&destination=${dest.latitude},${dest.longitude}&mode=driving&key=AIzaSyAigf2C8QKrJ13kRfR7IbHohUHkwqQ6WhE"

    inner class GetDirection(val url: String, var activity: MapsActivity): AsyncTask<Void, Void, List<List<LatLng>>>() {
        val dialog = Dialog(applicationContext, android.R.style.Theme_Translucent_NoTitleBar)

        override fun doInBackground(vararg params: Void?): List<List<LatLng>> {
            val client = OkHttpClient()
            val request = com.squareup.okhttp.Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            val data = response.body()!!.string()
            val result = ArrayList<List<LatLng>>()
            try {
                Log.d("MyLog", data)
                val respObj = Gson().fromJson(data, GoogleMapDTO::class.java)

                val path = ArrayList<LatLng>()
                for(i in 0..(respObj.routes[0].legs[0].steps.size-1)){
//                    val startLatLng = LatLng(respObj.routes[0].legs[0].steps[i].start_location.lat.toDouble(),
//                        respObj.routes[0].legs[0].steps[i].start_location.lng.toDouble())
//                    path.add(startLatLng)
//                    val endLatLng = LatLng(respObj.routes[0].legs[0].steps[i].end_location.lat.toDouble(),
//                        respObj.routes[0].legs[0].steps[i].end_location.lng.toDouble())
                    path.addAll(decodePolyline(respObj.routes[0].legs[0].steps[i].polyline.points))
                }
                result.add(path)
            }catch (e: Exception){
                e.printStackTrace()
            }
            return result
        }

        override fun onPostExecute(result: List<List<LatLng>>) {
            val lineoption = PolylineOptions()
            for(i in result.indices){
                lineoption.addAll(result[i])
                lineoption.width(10f)
                lineoption.color(Color.BLUE)
                lineoption.geodesic(true)
            }
            mMap.addPolyline(lineoption)
        }
    }

    public fun decodePolyline(encoded: String): List<LatLng> {

        val poly = ArrayList<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0

        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].toInt() - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat

            shift = 0
            result = 0
            do {
                b = encoded[index++].toInt() - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng

            val latLng = LatLng((lat.toDouble() / 1E5),(lng.toDouble() / 1E5))
            poly.add(latLng)
        }

        return poly
    }
*/
    /* private fun rutaEntreDosPuntos(url:String){
         val queue = Volley.newRequestQueue(this)
         val solicitud = StringRequest(Request.Method.GET, url, Response.Listener<String>{
             response ->
             Log.d("MyLog", response)
             val coordenadas = obtenerCoordenadas(response)
             mMap.addPolyline(coordenadas)
         }, Response.ErrorListener { })
         queue.add(solicitud)
         Log.d("MyLog", "EJECUTADO RUTA")
     }

     private fun obtenerCoordenadas(json: String): PolylineOptions{
         val gson = Gson()
         val objeto = gson.fromJson(json, com.example.sitiosjunio.Response::class.java)
         val puntos = objeto.routes?.get(0)!!.legs?.get(0)!!.steps!!
         val coordenadas = PolylineOptions()
         val mostrar = objeto.routes?.get(0)!!.legs?.get(0)!!
         if(mostrar?.distance!!.toValue() >= 1000.0){
             toast(mostrar?.distance?.toValueTextHigh()!!)
         }else
             toast(mostrar?.distance?.toValueText()!!)
         for(punto in puntos) {
             coordenadas.add(punto.start_location?.toLanLng())
             coordenadas.add(punto.end_location?.toLanLng())
         }
         coordenadas.color(Color.CYAN).width(15f)
             return coordenadas
     }*/
}
