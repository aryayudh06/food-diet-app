package com.pam.gemastik_app.ui.login

import android.annotation.SuppressLint
import com.pam.gemastik_app.ui.healthconnect.HealthConnectTest
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.pam.gemastik_app.BuildConfig
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.pam.gemastik_app.databinding.ActivityLoginBinding
import com.pam.gemastik_app.ui.HomeActivity
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class LoginActivity : AppCompatActivity() {
    private lateinit var googleSignInClient: GoogleSignInClient

    private val REQ_ONE_TAP = 2  // Can be any integer unique to the Activity
    private val RC_SIGN_IN = 1
    private var showOneTapUI = true
    private var CLIENT_ID = BuildConfig.CLIENT_ID

    @SuppressLint("SetTextI18n")
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


        binding.registerTextView.text = Html.fromHtml("Don't have an account? <u>Register</u>")
        binding.registerTextView.setOnClickListener {
            if (binding.registerTextView.text.toString() == "Register") {
                startActivity(Intent(this, RegisterActivity::class.java))
            }
        }

        binding.loginButton.setOnClickListener {
            val email = binding.emailEditText.text.toString()
            val passwd = binding.passwordEditText.text.toString()

            if (email.isNotEmpty() && passwd.isNotEmpty()) {
                lifecycleScope.launch {
                    try {
                        HomeActivity.auth.signInWithEmailAndPassword(email, passwd).await()
                        startActivity(Intent(this@LoginActivity, HomeActivity::class.java))
                        finish()
                    } catch (e: Exception) {
                        Toast.makeText(this@LoginActivity, e.localizedMessage, Toast.LENGTH_LONG).show()
                    }
                }
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
                lifecycleScope.launch {
                    firebaseAuthWithGoogle(account.idToken!!)
                }
            } catch (e: ApiException) {
                Log.w(TAG, "Google sign in failed", e)
            }
        }
    }

    private suspend fun firebaseAuthWithGoogle(idToken: String) {
        try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            HomeActivity.auth.signInWithCredential(credential).await()
            Log.d(TAG, "signInWithCredential:success")
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        } catch (e: Exception) {
            Log.w(TAG, "signInWithCredential:failure", e)
            Toast.makeText(this, "Authentication Failed.", Toast.LENGTH_SHORT).show()
        }
    }


    override fun onResume() {
        super.onResume()
    }
}