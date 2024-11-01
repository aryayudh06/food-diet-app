package com.pam.gemastik_app.ui.login

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.pam.gemastik_app.BuildConfig
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.pam.gemastik_app.R
import com.pam.gemastik_app.databinding.ActivityLoginBinding
import com.pam.gemastik_app.ui.HomeActivity

class LoginActivity : AppCompatActivity() {
    private lateinit var googleSignInClient: GoogleSignInClient

    private val REQ_ONE_TAP = 2  // Can be any integer unique to the Activity
    private val RC_SIGN_IN = 1
    private var showOneTapUI = true
    private var CLIENT_ID = BuildConfig.CLIENT_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        HomeActivity.auth = FirebaseAuth.getInstance()
        supportActionBar?.title = "Login"
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(BuildConfig.CLIENT_ID)
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)


        binding.registerTextView.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
        }

        binding.loginButton.setOnClickListener(){
            val email = binding.emailEditText.text.toString()
            val passwd = binding.passwordEditText.text.toString()

            if(email.isNotEmpty() && passwd.isNotEmpty())
                HomeActivity.auth.signInWithEmailAndPassword(email, passwd).addOnCompleteListener(){
                    if(it.isSuccessful){
                        startActivity(Intent(this, HomeActivity::class.java))
                        finish()
                    }
                }.addOnFailureListener(){
                    Toast.makeText(this, it.localizedMessage, Toast.LENGTH_LONG).show()
                }
        }

        binding.googleBtn.setOnClickListener(){
            signInWithGoogle()
        }
    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.id)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Log.w(TAG, "Google sign in failed", e)
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        HomeActivity.auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithCredential:success")
                    val user = HomeActivity.auth.currentUser
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                } else {
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    Toast.makeText(this, "Authentication Failed.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    override fun onResume() {
        super.onResume()
    }
}