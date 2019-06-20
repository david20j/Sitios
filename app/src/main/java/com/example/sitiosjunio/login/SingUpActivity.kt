package com.example.sitiosjunio.activities.login

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.example.sitiosjunio.R
import com.example.sitiosjunio.activities.*
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_sign_up.*

class SingUpActivity : AppCompatActivity() {

    private val mAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        // BOTONES
        btnGoLogin.setOnClickListener{
            goToActivity<LoginActivity>{
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
        btnCreateAccount.setOnClickListener{
            val email = etEmailSignUp.text.toString()
            val password = etPasswordSignUp.text.toString()
            val confirmPassword = etConfirmPasswordSignUp.text.toString()
            if(isValidEmail(email) && isValidPassword(password) && isValidConfirmPasword(password, confirmPassword))
                signUpByEmail(email, password)
            else
                toast("Please make sure all the data is correct.")
        }
        // FIN BOTONES

        // EDITTEXT
        etEmailSignUp.validate {
            etEmailSignUp.error = if(isValidEmail(it)) null else "Email is not valid"
        }
        etPasswordSignUp.validate {
            etPasswordSignUp.error = if(isValidPassword(it)) null else "Password should contain 1 lowercase, 1 uppercase, 1 number, 1 special character and 4 characters length at least"
        }
        etConfirmPasswordSignUp.validate {
            etConfirmPasswordSignUp.error = if(isValidConfirmPasword(etPasswordSignUp.text.toString(), it)) null else "Confirm Password do not match with Password"
        }
        //FIN EDITTEXT
    }

    private fun signUpByEmail(email:String, password:String){
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                mAuth.currentUser!!.sendEmailVerification().addOnCompleteListener(this){
                    //Hasta que no se envia el email no seguira con el proceso.
                    toast("An email has been sent to you. Please, confirm before sign in")
                    goToActivity<LoginActivity>{
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                }
            } else {
                // If sign in fails, display a message to the user.
                toast("An unexpected errror occurred, please try again")
            }
        }
    }
}
