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

            if((email.isNotEmpty() && passwd.isNotEmpty()) && (passwd == passConfirm) && binding.privacyCheckBox.isChecked){
                val bundle = Bundle()
                bundle.putString("email", email)
                bundle.putString("passwd", passwd)
                val intent = Intent(this, Sign_Up_Personalization::class.java)
                intent.putExtras(bundle)

                startActivity(intent)
                finish()
            }else {
                Toast.makeText(this, "Gagal Membuat Akun.", Toast.LENGTH_LONG).show()
            }
        }

        binding.loginTextView.setOnClickListener(){
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }
}