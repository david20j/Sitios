package com.example.sitiosjunio.activities.fragments

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.example.sitiosjunio.R
import com.example.sitiosjunio.Utils.RxBus
import com.example.sitiosjunio.activities.DetailSiteActivity
import com.example.sitiosjunio.activities.adapters.LugaresAdapter
import com.example.sitiosjunio.activities.listener.RecyclerViewListener
import com.example.sitiosjunio.activities.models.Lugar
import com.example.sitiosjunio.activities.toast
import com.example.sitiosjunio.adapters.PropioAdapter
import com.example.sitiosjunio.models.TotalLugaresEvent
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.*
import kotlinx.android.synthetic.main.fragment_lugares.view.*
import kotlinx.android.synthetic.main.fragment_propio.view.*
import java.util.ArrayList


class FavoritosFragment : Fragment() {

    private lateinit var _view: View
    private lateinit var adapter: PropioAdapter
    private val lugaresList: ArrayList<Lugar> = ArrayList()

    private val store: FirebaseFirestore = FirebaseFirestore.getInstance()
    private lateinit var lugaresDBRef: CollectionReference

    private val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private lateinit var currentUser: FirebaseUser

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        _view = inflater.inflate(R.layout.fragment_propio, container, false)

        setUpLugaresDB()
        setUpCurrentUser()
        setUpRecylerView()
        subscribeToSites()

        return _view
    }


    private fun setUpRecylerView() {
        val layoutManager = LinearLayoutManager(context)
        adapter = PropioAdapter(lugaresList, object: RecyclerViewListener {
            override fun onClick(vista: Lugar, position: Int) {
                val intent = Intent(activity!!, DetailSiteActivity::class.java)
                intent.putExtra("lugar", vista)
                startActivity(intent)
            }
        } )
        _view.recyclerViewPropio.setHasFixedSize(true)
        _view.recyclerViewPropio.layoutManager = layoutManager
        _view.recyclerViewPropio.itemAnimator = DefaultItemAnimator()
        _view.recyclerViewPropio.adapter = adapter
    }

    private fun setUpCurrentUser() {
        currentUser = mAuth.currentUser!!
    }

    private fun setUpLugaresDB() {
        lugaresDBRef = store.collection("lugares")
    }

    private fun subscribeToSites(){
        lugaresDBRef
            .orderBy("fecha", Query.Direction.DESCENDING)
            .addSnapshotListener(object: java.util.EventListener, EventListener<QuerySnapshot> {
                override fun onEvent(snapshot: QuerySnapshot?, exception: FirebaseFirestoreException?) {
                    exception?.let{
                        activity!!.toast("Exception!")
                        return
                    }
                    snapshot?.let {
                        lugaresList.clear()
                        val lugares = it.toObjects(Lugar::class.java)
                        for(lugar in lugares){
                            if(lugar.authorId == currentUser.email)
                                lugaresList.add(lugar)
                        }
                        adapter.notifyDataSetChanged()
                        RxBus.publish(TotalLugaresEvent(lugaresList.size))
                    }
                }

            })
    }

}
