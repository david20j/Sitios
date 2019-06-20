package com.example.sitiosjunio.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.support.annotation.RequiresApi
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.example.mylibrarytoolbar.ToolbarActivity
import com.example.sitiosjunio.R
import com.example.sitiosjunio.activities.models.Lugar
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.OnSuccessListener

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_add_site.*
import kotlinx.android.synthetic.main.dialog_camera_galery.view.*
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.lang.Exception
import java.util.*

class AddSiteActivity : ToolbarActivity() {

    //UBICACION
    private val permisoFineLocation = android.Manifest.permission.ACCESS_FINE_LOCATION
    private val permisoCoarseLocation = android.Manifest.permission.ACCESS_COARSE_LOCATION
    private val CODIGO_SOLICITUD_LOCALIZACION = 100

    private var fusedLocationClient: FusedLocationProviderClient? = null
    private var locationRequest: LocationRequest? = null

    //CONSTANTES PARA PERMISOS
    private val CODIGO_SOLICITUD_CAMARA = 200
    private val CODIGO_SOLICITUD_IMAGEN = 300
    private val CODIGO_SOLICITUD_GALERIA = 400

    //FIREBASE
    private val store: FirebaseFirestore = FirebaseFirestore.getInstance()
    private lateinit var lugaresDBRef: CollectionReference
    private val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private lateinit var currentUser: FirebaseUser
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
    private lateinit var storageRef: StorageReference
    //
    private var coordenadas: LatLng? = null

    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_site)

        fusedLocationClient = FusedLocationProviderClient(this)
        inicializarLocationRequest()
        toolbarToLoad(toolbarViewAdd as Toolbar)

        setUpLugaresDB()
        setUpCurrentUser()

        img_foto_add.setOnClickListener{
            val mDialogView = this.layoutInflater.inflate(R.layout.dialog_camera_galery, null)
            val mBuilder = AlertDialog.Builder(this).setView(mDialogView).setTitle("Seleccion fuente")
            val mAlertDialog = mBuilder.show()
            mDialogView.btn_dialog_camera.setOnClickListener{
                getPictureFromCameraAskingPermision()
                mAlertDialog.dismiss()
            }
            mDialogView.btn_dialog_gallery.setOnClickListener{
                getPictureFromGallery()
                mAlertDialog.dismiss()
            }
        }

    }

    @SuppressLint("RestrictedApi")
    private fun inicializarLocationRequest(){
        locationRequest = LocationRequest()
        locationRequest?.interval = 10000
        locationRequest?.fastestInterval = 5000
        locationRequest?.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    override fun onStart() {
        super.onStart()
        if(validarPermisosUbicacion())
            obtenerUbicacion()
        else
            pedirPermisos()
    }

//    override fun onPause() {
//        super.onPause()
//        detenerActualizacionDeUbicacion()
//    }

//    private fun detenerActualizacionDeUbicacion() {
//        if(fusedLocationClient != null)
//            fusedLocationClient?.removeLocationUpdates(callback)
//    }

    private fun pedirPermisos() {
        val deboProveerContexto = ActivityCompat.shouldShowRequestPermissionRationale(this, permisoFineLocation)

        if(deboProveerContexto)
            solicitudPermiso()
        else
            solicitudPermiso()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun solicitudPermiso() {
        requestPermissions(arrayOf(permisoFineLocation, permisoCoarseLocation), CODIGO_SOLICITUD_LOCALIZACION)
    }

    @SuppressLint("MissingPermission")
    private fun obtenerUbicacion() {
//        callback = object: LocationCallback(){
//            override fun onLocationResult(locationResult: LocationResult?) {
//                super.onLocationResult(locationResult)
//
//                for(ubicacion in locationResult?.locations!!){
//                    coordenadas = LatLng(ubicacion.latitude, ubicacion.longitude)
//                    et_ciudad_add.setText(getCity(coordenadas!!))
//                    toast("" + ubicacion.longitude + " " + ubicacion.latitude)
//                }
//            }
//        }
//        fusedLocationClient?.requestLocationUpdates(locationRequest, callback, null)

//        fusedLocationClient?.lastLocation?.addOnSuccessListener(this, object: OnSuccessListener<Location> {
//            override fun onSuccess(location: Location?) {
//                if(location != null)
//                    toast(location.latitude.toString()+ " "+ location.longitude.toString())
//            }
//
//        })
        val callback = object: LocationCallback(){
            override fun onLocationResult(locationResult: LocationResult?) {
                super.onLocationResult(locationResult)

                for(location in locationResult?.locations!!) {
                    et_ciudad_add.setText(getCity(LatLng(location.latitude, location.longitude)))
                    coordenadas = LatLng(location.latitude, location.longitude)
                }
            }
        }

        fusedLocationClient?.requestLocationUpdates(locationRequest, callback, null)
    }

    private fun validarPermisosUbicacion(): Boolean {
        val hayUbicacionPrecisa = ActivityCompat.checkSelfPermission(this, permisoFineLocation) == PackageManager.PERMISSION_GRANTED
        val hayUbicacionOrdinaria = ActivityCompat.checkSelfPermission(this, permisoCoarseLocation) == PackageManager.PERMISSION_GRANTED

        return hayUbicacionPrecisa && hayUbicacionOrdinaria
    }

    //----------RECOGER IMAGEN----------
    private fun getPictureFromCameraAskingPermision(){
        //Añadir permiso al manifest
        //Comprobar el permiso de la camara
        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            // Si no ha sido aceptado previament(Para versiones desde la 6.0 API 23 en adelante)
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.CAMERA), CODIGO_SOLICITUD_CAMARA)
        }else{
            //Si ha sido aceptado previamente(Para versiones inferiores a 6.0)
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(intent, CODIGO_SOLICITUD_IMAGEN)
        }
    }

    private fun getPictureFromGallery(){
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent, CODIGO_SOLICITUD_GALERIA)
    }
    //----------FIN RECOGER IMAGEN----------

    private fun setUpLugaresDB() {
        lugaresDBRef = store.collection("lugares")
        storageRef = storage.getReference("lugares")
    }

    private fun setUpCurrentUser() {
        currentUser = mAuth.currentUser!!
        et_autor_add.setText(currentUser.email)
    }
    //----------RESULTADOS DE ACCIONES----------
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            CODIGO_SOLICITUD_IMAGEN -> {
                if (resultCode == Activity.RESULT_OK) {
                    //Obtenemos los extras del intent recibido
                    val extras = data!!.extras
                    //Formamos el bitmap a partir de los extras
                    val imageBitmap = extras.get("data") as Bitmap
                    //Cargamos la foto como bitmap en el ImageView
                    img_foto_add.setImageBitmap(imageBitmap)
                } else {
                    //La foto no ha sido tomada
                }
            }
            CODIGO_SOLICITUD_GALERIA -> {
                if (resultCode == Activity.RESULT_OK) {
                    //Obtenemos los extras del intent recibido
                    val uri = data!!.data
                    //Cargamos la foto como bitmap en el ImageView
                    Picasso.get().load(uri).into(img_foto_add)
                } else {
                    //No hemos seleccionado foto
                    Log.d("MYLOG", "FALLO AL SUBIR FOTO")
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when(requestCode){
            CODIGO_SOLICITUD_CAMARA ->{
                if(grantResults.size > 0 && grantResults[grantResults.size -1] == PackageManager.PERMISSION_GRANTED){
                    val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    startActivityForResult(intent, CODIGO_SOLICITUD_IMAGEN)
                }else{
                    toast("You can't take a picture if you don't allow it")
                }
            }
            CODIGO_SOLICITUD_LOCALIZACION->{
                if(grantResults.size > 0 && grantResults[grantResults.size -1] == PackageManager.PERMISSION_GRANTED)
                    obtenerUbicacion()
                else
                  toast("No diste permiso para acceder a ubicacion")
            }
        }
    }
    //----------FIN RESULTADOS DE ACCIONES----------
    //----------MENU----------
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_add, menu)
        menu?.getItem(0)?.isEnabled = true
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId){
            R.id.menu_guardar -> {
                val nombreLugar = et_nombre_add.text.toString()
                if(nombreLugar.isNotEmpty() && img_foto_add.drawable != null){
                    uploadImageToFirebaseStorage()
                }
                else
                    toast("You must type a name to save the site")
            }
        }
        return super.onOptionsItemSelected(item)
    }
    //----------FIN MENU---------

    //----------GUARDAR LUGAR---------
    private fun  uploadImageToFirebaseStorage() {
        try {
            img_foto_add.isDrawingCacheEnabled = true
            img_foto_add.buildDrawingCache()
            val bitmap = (img_foto_add.drawable as BitmapDrawable).bitmap
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val data = baos.toByteArray()
            val filename = UUID.randomUUID().toString()
            val refe = storage.getReference("/images/$filename")
            var uploadTask = refe.putBytes(data)

            uploadTask.addOnSuccessListener {
                refe.downloadUrl.addOnSuccessListener {
                    saveSiteToFirebaseDatabase(it.toString())
                }
            }
        }catch (e: Exception){
            toast("You must select an image to save the site")
            finish()
            startActivity(intent)
        }
    }

    private fun saveSiteToFirebaseDatabase(foto: String){
        val site = Lugar( "", et_autor_add.text.toString(), et_nombre_add.text.toString(), et_ciudad_add.text.toString(),
            et_descripcion.text.toString(), coordenadas!!.latitude, coordenadas!!.longitude, foto, Date())
        lugaresDBRef.add(site).addOnSuccessListener {
            toast("Site added")
            goToActivity<MainEmptyActivity> {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
        }
    }
    //----------FIN GUARDAR LUGAR---------


    private fun getCity(myCoordenates: LatLng):String {
        val geoCoder = Geocoder(this, Locale.getDefault())
        try {
            val addresses = geoCoder.getFromLocation(myCoordenates.latitude, myCoordenates.longitude, 1)
            return addresses.get(0).locality
        } catch (e: IOException) { }
        return ""
    }
}
