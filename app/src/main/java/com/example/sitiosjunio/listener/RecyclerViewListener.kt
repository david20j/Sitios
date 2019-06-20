package com.example.sitiosjunio.activities.listener


import com.example.sitiosjunio.activities.models.Lugar


interface RecyclerViewListener {

    fun onClick(lugar: Lugar, position: Int) {

    }
}