package com.example.sitiosjunio.activities.adapters


import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import com.example.sitiosjunio.R
import com.example.sitiosjunio.activities.inflate
import com.example.sitiosjunio.activities.listener.RecyclerViewListener
import com.example.sitiosjunio.activities.models.Lugar
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_lugares_item.view.*
import java.lang.Exception

class LugaresAdapter(val lugares: List<Lugar>, val listener: RecyclerViewListener)
    : RecyclerView.Adapter<LugaresAdapter.ViewHolder>(){

    override fun getItemCount() = lugares.size

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): LugaresAdapter.ViewHolder = ViewHolder(p0.inflate(R.layout.fragment_lugares_item))

    override fun onBindViewHolder(holder: LugaresAdapter.ViewHolder, position: Int)  = holder.bind(lugares[position], listener)

    class ViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
        fun bind(lugar: Lugar, listener: RecyclerViewListener) = with(itemView){
            tv_titulo.text = lugar.nombre
            tv_ciudad.text = lugar.ciudad
//            when(lugar.distancia){
//                in 0..1000 -> tv_lugar_distancia.text = "${lugar.distancia} m"
//                else -> tv_lugar_distancia.text = "${lugar.distancia/1000} km"
//            }
            //if(lugar.fotoURL != "") {
            try {
                Picasso.get().load(lugar.fotoURL).resize(100, 100)
                    .centerCrop().into(img_foto)
            }catch (e: Exception){
            }
            //}
            //Click Events
            setOnClickListener{listener.onClick(lugar, adapterPosition)}
        }
    }

}