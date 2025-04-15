package com.example.sellersection

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.sellersection.databinding.ActivityEnterOtpactivityBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.auth

class EnterOTPActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEnterOtpactivityBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityEnterOtpactivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = Firebase.auth

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        verifyCode()

    }

    private fun verifyCode() {
        val storedVerificationId = intent.getStringExtra("verification_id")
        binding.OTPButton.setOnClickListener{
            //binding.verifyingProgressBar.visibility = View.VISIBLE
            val code0 = binding.OTPcode0.text.toString()
            val code1 = binding.OTPcode1.text.toString()
            val code2 = binding.OTPcode2.text.toString()
            val code3 = binding.OTPcode3.text.toString()
            val code4 = binding.OTPcode4.text.toString()
            val code5 = binding.OTPcode5.text.toString()
            val code = code0 + code1 + code2 + code3 + code4 + code5
            Toast.makeText(this, code, Toast.LENGTH_SHORT).show()
            //val credential = PhoneAuthProvider.getCredential(storedVerificationId.toString(), code)
            val credential = PhoneAuthProvider.getCredential(storedVerificationId!!, code)
            signInWithPhoneAuthCredential(credential)
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "signInWithCredential:success", Toast.LENGTH_SHORT).show()
                    val user = task.result?.user
                    replaceActivity(this, OpeningActivity::class.java)
                } else {
                    Toast.makeText(this, "signInWithCredential:failure", Toast.LENGTH_SHORT).show()
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        Toast.makeText(this, "The verification code is invalid", Toast.LENGTH_SHORT).show()
                    }
                }
            }
    }

}