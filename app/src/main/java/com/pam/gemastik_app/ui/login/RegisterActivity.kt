package com.pam.gemastik_app.ui.login

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.pam.gemastik_app.R
import com.pam.gemastik_app.databinding.ActivityRegisterBinding
import com.pam.gemastik_app.ui.MainActivity

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "Register"

        binding.btRegisterAcc.setOnClickListener(){
            val email = binding.etRegisterEmail.text.toString()
            val passwd = binding.etRegisterPass.text.toString()

            if(email.isNotEmpty() && passwd.isNotEmpty())
                MainActivity.auth.createUserWithEmailAndPassword(email, passwd).addOnCompleteListener(){
                    if(it.isSuccessful){
                        startActivity(Intent(this, LoginActivity::class.java))
                        finish()
                    }
                }.addOnFailureListener(){
                    Toast.makeText(this, it.localizedMessage, Toast.LENGTH_LONG).show()
                }
        }
    }
}