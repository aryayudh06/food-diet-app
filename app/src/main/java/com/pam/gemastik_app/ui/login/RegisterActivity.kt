package com.pam.gemastik_app.ui.login

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.pam.gemastik_app.databinding.ActivityRegisterBinding
import com.pam.gemastik_app.ui.MainActivity

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "Register"

        binding.registerButton.setOnClickListener(){
            val name = binding.nameEditText.text.toString()
            val email = binding.emailEditText.text.toString()
            val passwd = binding.passwordEditText.text.toString()
            val passConfirm = binding.confirmPasswordEditText.text.toString()

            if((email.isNotEmpty() && passwd.isNotEmpty()) && (passwd == passConfirm) && binding.privacyCheckBox.isChecked)
                MainActivity.auth.createUserWithEmailAndPassword(email, passwd).addOnCompleteListener(){
                    if(it.isSuccessful){
                        startActivity(Intent(this, Sign_Up_Personalization::class.java))
                        finish()
                    }
                }.addOnFailureListener(){
                    Toast.makeText(this, it.localizedMessage, Toast.LENGTH_LONG).show()
                }
        }

        binding.loginTextView.setOnClickListener(){
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }
}