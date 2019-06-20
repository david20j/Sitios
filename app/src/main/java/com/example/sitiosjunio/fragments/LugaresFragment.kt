package com.example.sitiosjunio.activities.fragments


import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.sitiosjunio.R
import com.example.sitiosjunio.activities.DetailSiteActivity
import com.example.sitiosjunio.activities.adapters.LugaresAdapter
import com.example.sitiosjunio.activities.listener.RecyclerViewListener
import com.example.sitiosjunio.activities.models.Lugar
import com.example.sitiosjunio.activities.toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.*
import com.google.firebase.firestore.EventListener
import kotlinx.android.synthetic.main.fragment_lugares.view.*
import java.util.*

class LugaresFragment : Fragment() {

    private lateinit var _view: View
    private lateinit var adapter: LugaresAdapter
    private val lugaresList: ArrayList<Lugar> = ArrayList()

    private val store: FirebaseFirestore = FirebaseFirestore.getInstance()
    private lateinit var lugaresDBRef: CollectionReference
    private val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private lateinit var currentUser: FirebaseUser

    private var lugaresSubscription: ListenerRegistration? = null

    //UBICACION
//    private val permisoFineLocation = android.Manifest.permission.ACCESS_FINE_LOCATION
//    private val permisoCoarseLocation = android.Manifest.permission.ACCESS_COARSE_LOCATION
//    private val CODIGO_SOLICITUD_PERMISO = 100
//    private var distancia: Int = 0
//    private var miPosicion: LatLng? = null
//    var fusedLocationClient: FusedLocationProviderClient? = null
//    var locationRequest: LocationRequest? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        _view = inflater.inflate(R.layout.fragment_lugares, container, false)

        //fusedLocationClient = FusedLocationProviderClient(activity!!)
        //inicializarLocationRequest()

        setUpLugaresDB()
        setUpCurrentUser()
        setUpRecylerView()
        subscribeToSites()

        return _view
    }


    private fun setUpRecylerView() {
        val layoutManager = LinearLayoutManager(context)
        adapter = LugaresAdapter(lugaresList, object: RecyclerViewListener{
            override fun onClick(vista: Lugar, position: Int) {
                val intent = Intent(activity!!, DetailSiteActivity::class.java)
                intent.putExtra("lugar", vista)
                startActivity(intent)
            }
        } )
        _view.recyclerView.setHasFixedSize(true)
        _view.recyclerView.layoutManager = layoutManager
        _view.recyclerView.itemAnimator = DefaultItemAnimator()
        _view.recyclerView.adapter = adapter
    }

    private fun setUpCurrentUser() {
        currentUser = mAuth.currentUser!!
    }

    private fun setUpLugaresDB() {
        lugaresDBRef = store.collection("lugares")
    }

    private fun subscribeToSites(){
        lugaresSubscription = lugaresDBRef
            .orderBy("fecha", Query.Direction.DESCENDING)
            .addSnapshotListener(object: java.util.EventListener, EventListener<QuerySnapshot>{
            override fun onEvent(snapshot: QuerySnapshot?, exception: FirebaseFirestoreException?) {
                exception?.let{
                    activity!!.toast("Exception!")
                    return
                }
                snapshot?.let {
                    lugaresList.clear()
                    val lugares = it.toObjects(Lugar::class.java)
                        var index = 0
                        for (lugar in lugares) {
                            lugar.id = it.documents.get(index).id
                            index++
                        }
                        lugaresList.addAll(lugares)
                        adapter.notifyDataSetChanged()

                }}})
    }

    //UBICACION + DISTANCIA
    //----------UBICACION----------

   /* @SuppressLint("RestrictedApi")
    private fun inicializarLocationRequest(){
        locationRequest = LocationRequest()
        locationRequest?.interval = 10000
        locationRequest?.fastestInterval = 5000
        locationRequest?.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onStart() {
        super.onStart()

        if(validadrPermisosUbicacion())
            obtenerUbicacion()
        else
            pedirPermisos()
    }
    //PERMISOS
    @RequiresApi(Build.VERSION_CODES.M)
    private fun pedirPermisos() {
        val deboProveerContexto = ActivityCompat.shouldShowRequestPermissionRationale(activity!!, permisoFineLocation)

        if(deboProveerContexto)

        else
            solicitudPermiso()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun solicitudPermiso() {
        requestPermissions(arrayOf(permisoFineLocation, permisoCoarseLocation), CODIGO_SOLICITUD_PERMISO)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when(requestCode){
            CODIGO_SOLICITUD_PERMISO ->{
                if(grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    obtenerUbicacion()
                else
                   activity?.toast("No diste permiso para acceder a ubicacion")
            }
        }
    }

    private fun validadrPermisosUbicacion(): Boolean {
        val hayUbicacionPrecisa = ActivityCompat.checkSelfPermission(context!!, permisoFineLocation) == PackageManager.PERMISSION_GRANTED
        val hayUbicacionOrdinaria = ActivityCompat.checkSelfPermission(context!!, permisoCoarseLocation) == PackageManager.PERMISSION_GRANTED

        return hayUbicacionPrecisa && hayUbicacionOrdinaria
    }

    @SuppressLint("MissingPermission")
    private fun obtenerUbicacion() {
        fusedLocationClient?.lastLocation?.addOnSuccessListener(activity!!, object: OnSuccessListener<Location> {
            override fun onSuccess(location: Location?) {
                if(location != null){
                    miPosicion = LatLng(location.latitude, location.longitude)
                    Log.d("MyLog obtenerUbicacion", miPosicion.toString())
                    subscribeToSites()
                }

            }
        })
    }

    private fun obtenerDistancia(lugar: Lugar): Int{
        val queue = Volley.newRequestQueue(context)
        Log.d("MyLog ObtenerDistancia", miPosicion.toString())
        val url = "https://maps.googleapis.com/maps/api/directions/json?origin=${miPosicion?.latitude},${miPosicion?.longitude}&destination=${lugar.latitud},${lugar.longitud}&sensor=false&mode=driving&key=AIzaSyAigf2C8QKrJ13kRfR7IbHohUHkwqQ6WhE"
        var dist = 0
        val solicitud = StringRequest(Request.Method.GET, url, Response.Listener<String>{
                response ->
            //Log.d("MyLog", response)
            val objeto = Gson().fromJson(response, com.example.sitiosjunio.Response::class.java)
            distancia = objeto.routes?.get(0)!!.legs?.get(0)?.distance?.toValue()!!
            Log.d("MyLog", dist.toString())
        }, Response.ErrorListener {})
        queue.add(solicitud)
        return dist
    }*/

    override fun onDestroyView() {
        lugaresSubscription?.remove()
        super.onDestroyView()
    }
}
