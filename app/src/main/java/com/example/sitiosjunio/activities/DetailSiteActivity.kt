package com.example.sitiosjunio.activities

import android.content.Intent
import android.graphics.Bitmap
import android.location.Geocoder
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.support.v7.app.AlertDialog
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.example.mylibrarytoolbar.ToolbarActivity
import com.example.sitiosjunio.R
import com.example.sitiosjunio.Utils.ViewImageExtended
import com.example.sitiosjunio.activities.models.Lugar
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_add_site.*
import kotlinx.android.synthetic.main.activity_detail_site.*
import kotlinx.android.synthetic.main.dialog_custom_layout.view.*
import kotlinx.android.synthetic.main.fragment_lugares_item.view.*
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class DetailSiteActivity : ToolbarActivity() {

    lateinit var  lugar: Lugar

    //FIREBASE
    private val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val currentUser = mAuth.currentUser
    private val store: FirebaseFirestore = FirebaseFirestore.getInstance()
    private lateinit var lugaresDBRef: CollectionReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_site)

        toolbarToLoad(toolbarViewDetail as Toolbar)

        val value: Bundle ?= intent.extras
        lugar =  value?.get("lugar") as Lugar

        if(lugar.authorId == currentUser?.email) {
            et_nombre_details.isEnabled = true
            et_descripcion_details.isEnabled = true
        }
        et_nombre_details.setText(lugar.nombre)
        et_ciudad_details.setText(lugar.ciudad)
        et_descripcion_details.setText(lugar.descripcion)
        tv_autor_details.setText(lugar.authorId)
        tv_fecha_details.setText(SimpleDateFormat("dd-MM-yyyy").format(lugar.fecha))
        tv_direccion_details.setText(getAdrress(LatLng(lugar.latitud, lugar.longitud)))
        if(lugar.fotoURL.contains("https://"))
            Picasso.get().load(lugar.fotoURL).into(img_foto_detail)

        setUpLugaresDB()

        //AMPLIAMOS LA IMAGEN AL TOCAR SOBRE ELLA
        img_foto_detail.setOnClickListener{
            if(lugar.fotoURL.contains("https://")) {
                var mBuilder = AlertDialog.Builder(this, android.R.style.Theme_Translucent_NoTitleBar)
                val mView = LayoutInflater.from(this).inflate(R.layout.dialog_custom_layout, null)
                val photoView = mView.imageView
                Picasso.get().load(lugar.fotoURL).into(photoView)
                mBuilder.setView(mView)
                val mDialog = mBuilder.create()
                mDialog.show()
            }
        }
    }

    private fun setUpLugaresDB() {
        lugaresDBRef = store.collection("lugares")
    }

    //----------MENU----------
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_details, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId){
            R.id.menu_editar -> {
                if(tv_autor_details.text.toString() == currentUser?.email.toString()) {
                    //item.isEnabled = true
                    update(lugar.id)
                }
                else
                    toast("Can`t be updated")
            }
            R.id.menu_borrar -> {
                if(tv_autor_details.text.toString() == currentUser?.email.toString()) {
                    delete(lugar.id)
                }
                else
                    toast("Can`t be deleted")
            }
            R.id.menu_goto->{
                val intent = Intent(this, MapsActivity::class.java)
                intent.putExtra("lugar", lugar)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }
    //----------FIN MENU---------


    private fun getAdrress(myCoordenates: LatLng):String {
        val geoCoder = Geocoder(this, Locale.getDefault())
        try {
            val addresses = geoCoder.getFromLocation(myCoordenates.latitude, myCoordenates.longitude, 1)
            return addresses.get(0).getAddressLine(0)
        } catch (e: IOException) {
        }

        return ""
    }

    private fun update(id: String){
        val site = Lugar(id, tv_autor_details.text.toString(), et_nombre_details.text.toString(),
            et_ciudad_details.text.toString(), et_descripcion_details.text.toString(), lugar.latitud, lugar.longitud, lugar.fotoURL, Date())
        lugaresDBRef.document(id).set(site).addOnSuccessListener {
            toast("Site has been updated")
            goToActivity<MainActivity>{
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
        }
    }
    private fun delete(id: String){
        lugaresDBRef.document(id).delete().addOnSuccessListener {
            toast("Site has been deleted")
            goToActivity<MainActivity>{
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
        }
    }
}
