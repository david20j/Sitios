package com.example.sitiosjunio.adapters

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import com.example.sitiosjunio.R
import com.example.sitiosjunio.activities.adapters.LugaresAdapter
import com.example.sitiosjunio.activities.inflate
import com.example.sitiosjunio.activities.listener.RecyclerViewListener
import com.example.sitiosjunio.activities.models.Lugar
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_lugares_item.view.*
import kotlinx.android.synthetic.main.fragment_propio_item.view.*

class PropioAdapter(val lugares: List<Lugar>, val listener: RecyclerViewListener)
    : RecyclerView.Adapter<PropioAdapter.ViewHolder>(){

    override fun getItemCount() = lugares.size

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): PropioAdapter.ViewHolder = ViewHolder(p0.inflate(R.layout.fragment_propio_item))

    override fun onBindViewHolder(holder: PropioAdapter.ViewHolder, position: Int)  = holder.bind(lugares[position], listener)

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        fun bind(lugar: Lugar, listener: RecyclerViewListener) = with(itemView){
            tv_titulo_propio.text = lugar.nombre
            tv_ciudad_propio.text = lugar.ciudad
            if(lugar.fotoURL != "") {
                Picasso.get().load(lugar.fotoURL).resize(100, 100)
                    .centerCrop().into(img_foto_propio)
            }
            //Click Events
            setOnClickListener{listener.onClick(lugar, adapterPosition)}
        }
    }
}