package com.example.sitiosjunio.activities.models

import java.io.Serializable
import java.util.*

data class Lugar(var id: String = "",
                 val authorId: String ="",
                 val nombre: String="",
                 val ciudad: String="",
                 val descripcion: String="",
                 val latitud: Double=0.0,
                 val longitud: Double=0.0,
                 val fotoURL: String="",
                 val fecha: Date=Date()):Serializable {
}