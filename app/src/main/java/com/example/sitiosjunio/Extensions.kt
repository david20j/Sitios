package com.example.sitiosjunio.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.support.v4.app.Fragment
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import java.util.regex.Pattern

fun Activity.toast(message: CharSequence, duration: Int = Toast.LENGTH_SHORT) = Toast.makeText(this, message, duration).show()

fun Activity.toast(resourceId: Int, duration: Int = Toast.LENGTH_SHORT) = Toast.makeText(this, resourceId, duration).show()

fun ViewGroup.inflate(layoutId: Int) = LayoutInflater.from(context).inflate(layoutId, this, false)!!

inline fun <reified T : Activity> Activity.goToActivity(noinline init: Intent.()-> Unit = {}){
    val intent = Intent(this, T::class.java)
    intent.init()
    startActivity(intent)
}

fun EditText.validate(validation: (String)-> Unit){
    this.addTextChangedListener(object: TextWatcher{
        override fun afterTextChanged(editable: Editable?) {
            validation(editable.toString())
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    })
}

fun Activity.isValidEmail(email: String):Boolean{
    val pattern = Patterns.EMAIL_ADDRESS
    return pattern.matcher(email).matches()
}

fun Activity.isValidPassword(password: String):Boolean{
    //Necesita --> 1 Numero / 1 Minuscula / 1 Especial / Min 4 Caracteres
    val passwordPattern = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{4,}$"
    val pattern = Pattern.compile(passwordPattern)
    return pattern.matcher(password).matches()
}

fun Activity.isValidConfirmPasword(password: String, confirmPassword: String):Boolean{
    return password == confirmPassword
}


