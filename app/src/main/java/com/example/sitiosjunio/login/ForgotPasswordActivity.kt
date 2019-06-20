package com.example.sitiosjunio.activities.login

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.example.sitiosjunio.R
import com.example.sitiosjunio.activities.goToActivity
import com.example.sitiosjunio.activities.isValidEmail
import com.example.sitiosjunio.activities.toast
import com.example.sitiosjunio.activities.validate
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_forgot_password.*

class ForgotPasswordActivity : AppCompatActivity() {

    private val mAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance()}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        //BOTONES FORGOT PASSWORD
        btnGoLogin.setOnClickListener{
            goToActivity<LoginActivity>{
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        btnForgot.setOnClickListener{
            val email = etEmailForgot.text.toString()
            if(isValidEmail(email)){
                mAuth.sendPasswordResetEmail(email).addOnCompleteListener(this){
                    toast("Email has been sent to reset your password.")
                }
                goToActivity<LoginActivity>{
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            }else{
                toast("Please make sure the email address is correct.")
            }
        }
        //FIN BOTONES

        etEmailForgot.validate {
            etEmailForgot.error = if(isValidEmail(it))null else "Email is not valid."
        }
    }
}
