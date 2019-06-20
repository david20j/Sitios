package com.example.sitiosjunio.activities.login

import android.app.Dialog
import android.content.Intent
import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.example.sitiosjunio.R
import com.example.sitiosjunio.activities.*
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity(), GoogleApiClient.OnConnectionFailedListener{

    private val mAuth: FirebaseAuth by lazy{ FirebaseAuth.getInstance()}
    private val mGoogleApiClient: GoogleApiClient by lazy {getGoogleApiClient() }
    private val RC_GOOGLE_SIGN_IN = 99

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        //BOTONES DEL LOGIN
        btnLogIn.setOnClickListener{
            val email = etEmail.text.toString()
            val password = etPassword.text.toString()
            if(isValidEmail(email) && isValidPassword(password))
                logInByEmail(email, password)
            else
                toast("Please make sure all the data is correct.")
        }

        tvForgotPassword.setOnClickListener{
            goToActivity<ForgotPasswordActivity>()
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
        }
        btnCreateAccount.setOnClickListener{
            goToActivity<SingUpActivity>()
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
        }

        btnLogInGoogle.setOnClickListener{
            val signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient)
            startActivityForResult(signInIntent, RC_GOOGLE_SIGN_IN)
        }
        //FIN DE BOTONES

        //VALIDACION EN TIEMPO REAL DE LOS EDIT TEXT
        etEmail.validate {
            etEmail.error = if(isValidEmail(it)) null else "Email is not valid"
        }
        etPassword.validate {
            etPassword.error = if(isValidPassword(it)) null else "Password should contain 1 lowercase, 1 uppercase, 1 number, 1 special character and 4 characters length at least"
        }
        //FIN VALIDACION
    }

    private fun logInByEmail(email: String, password: String){
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                val currentUser = mAuth.currentUser!!
                if(currentUser.isEmailVerified) {
                    toast("Hello, ${currentUser.email.toString()}")
                    goToActivity<MainEmptyActivity>{
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                }
                else
                    toast("${currentUser.email.toString()} must confirm email first")
            }
            else
                toast("An unexpected error occurred, please try again.")
        }
    }

    private fun getGoogleApiClient():GoogleApiClient{
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        return GoogleApiClient.Builder(this)
            .enableAutoManage(this, this)
            .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
            .build()
    }

    private fun loginByGoogleAccountIntoFirebase(googleAccount: GoogleSignInAccount) {
        val dialog = Dialog(this, android.R.style.Theme_Translucent_NoTitleBar)
        val view = this.layoutInflater.inflate(R.layout.full_screen_progress_bar, null)
        dialog.setContentView(view)
        dialog.setCancelable(false)
        dialog.show()
        val creadential = GoogleAuthProvider.getCredential(googleAccount.idToken, null)
        mAuth.signInWithCredential(creadential).addOnCompleteListener{
            if(mGoogleApiClient.isConnected){
                Auth.GoogleSignInApi.signOut(mGoogleApiClient)
            }
            dialog.dismiss()
            goToActivity<MainActivity>{
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            toast("Hello, ${mAuth.currentUser?.displayName.toString()}")
        }
    }

    override fun onConnectionFailed(p0: ConnectionResult) {

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == RC_GOOGLE_SIGN_IN){
            val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
            if(result.isSuccess) {
                val account = result.signInAccount
                loginByGoogleAccountIntoFirebase(account!!)
            }
        }
    }
}
