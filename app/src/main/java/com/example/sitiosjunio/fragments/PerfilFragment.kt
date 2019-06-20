package com.example.sitiosjunio.activities.fragments


import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.example.sitiosjunio.R
import com.example.sitiosjunio.Utils.CircleTransform
import com.example.sitiosjunio.Utils.RxBus
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.fragment_prefil.*
import com.example.sitiosjunio.activities.goToActivity
import com.example.sitiosjunio.activities.login.LoginActivity
import com.example.sitiosjunio.activities.toast
import com.example.sitiosjunio.models.TotalLugaresEvent
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.*
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_add_site.*
import kotlinx.android.synthetic.main.fragment_prefil.view.*


class PerfilFragment : Fragment() {

    private lateinit var _view: View

    private var lugaresSubscription: ListenerRegistration? = null

    //Firebase
    private val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private lateinit var currentUser: FirebaseUser

    private val store: FirebaseFirestore = FirebaseFirestore.getInstance()
    private lateinit var sitiosDBRef: CollectionReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        _view = inflater.inflate(R.layout.fragment_prefil, container, false)

        _view.btnLogOut.setOnClickListener{view ->
            FirebaseAuth.getInstance().signOut()
            activity?.goToActivity<LoginActivity>{
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
        }

        setUpLugaresDB()
        setUpCurrentUser()
        setUpCurrentUserInfoUI()
        //subscribeToTotalSitesFirebase()
        subcribeToTotalSitesEventBusReactiveStyle()

        return _view
    }

    private fun subcribeToTotalSitesEventBusReactiveStyle() {
        RxBus.listen(TotalLugaresEvent::class.java).subscribe({
            _view.tv_perfil_total.text = "${it.total}"
        })
    }

    private fun setUpCurrentUserInfoUI() {
        _view.tv_perfil_email.text = currentUser.email
        if(currentUser?.displayName == "" || currentUser?.displayName == "null")
            _view.tv_perfil_nombre.text = getString(R.string.label_lugar_no_name)
        else
            _view.tv_perfil_nombre.text = currentUser.displayName

        //_view.tv_nombre_perfil.text = currentUser.displayName?.let{ currentUser.displayName } ?: run{ getString(R.string.label_lugar_no_name)}//Si no tiene nombre carga el string marcado.

        currentUser.photoUrl?.let {
            Picasso.get().load(currentUser.photoUrl).resize(300, 300).centerCrop().transform(CircleTransform()).into(_view.iv_foto_perfil) } ?: run{
            Picasso.get().load(R.drawable.ic_person).resize(300, 300).centerCrop().into(_view.iv_foto_perfil) }
    }

    private fun setUpLugaresDB() {
        sitiosDBRef = store.collection("lugares")
    }

    private fun setUpCurrentUser() {
        currentUser = mAuth.currentUser!!
    }

    override fun onDestroyView() {
        lugaresSubscription?.remove()
        super.onDestroyView()
    }
}
