package com.pam.gemastik_app.ui.login

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.pam.gemastik_app.databinding.ActivityRegisterBinding
import com.pam.gemastik_app.ui.MainActivity
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "Register"

        binding.registerButton.setOnClickListener(){
            val uname = binding.nameEditText.text.toString()
            val email = binding.emailEditText.text.toString()
            val passwd = binding.passwordEditText.text.toString()
            val passConfirm = binding.confirmPasswordEditText.text.toString()

            lifecycleScope.launch {
                if (isValidInput(email, passwd, passConfirm, binding)) {
                    val bundle = Bundle().apply {
                        putString("email", email)
                        putString("passwd", passwd)
                        putString("uname", uname)
                    }

                    val intent = Intent(this@RegisterActivity, Sign_Up_Personalization::class.java)
                    intent.putExtras(bundle)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this@RegisterActivity, "Gagal Membuat Akun.", Toast.LENGTH_LONG).show()
                }
            }

            binding.loginTextView.text = "Login"
            binding.loginTextView.setOnClickListener {
                if (binding.loginTextView.text.toString() == "Login") {
                    startActivity(Intent(this, LoginActivity::class.java))
                }
            }
    }
}
    private suspend fun isValidInput(email: String, passwd: String, passConfirm: String, binding: ActivityRegisterBinding): Boolean {
        // Menambahkan sedikit delay untuk mensimulasikan operasi asinkron
        kotlinx.coroutines.delay(500) // Optional, hanya untuk simulasi jika ada operasi async lain
        return email.isNotEmpty() && passwd.isNotEmpty() && passwd == passConfirm && binding.privacyCheckBox.isChecked
    }
}