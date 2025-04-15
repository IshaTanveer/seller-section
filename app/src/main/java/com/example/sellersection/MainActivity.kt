package com.example.sellersection

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.sellersection.databinding.ActivityMainBinding
import com.google.firebase.Firebase
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthMissingActivityForRecaptchaException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.auth
import java.util.concurrent.TimeUnit

fun replaceActivity(context: Context, target: Class<out Activity>) {
    val intent = Intent(context, target)
    context.startActivity(intent)
    if (context is Activity) {
        context.finish()
    }
}
fun startNewActivity(context: Context, target: Class<out Activity>){
    val intent = Intent(context, target)
    context.startActivity(intent)
}

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = Firebase.auth

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        getOTP()

    }

    private var callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        @SuppressLint("ShowToast")
        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            // This callback will be invoked in two situations:
            // 1 - Instant verification. In some cases the phone number can be instantly
            //     verified without needing to send or enter a verification code.
            // 2 - Auto-retrieval. On some devices Google Play services can automatically
            //     detect the incoming verification SMS and perform verification without
            //     user action.
            binding.OTPProgressBar.visibility = View.GONE
            Toast.makeText(this@MainActivity, "signin with phone auth credential", Toast.LENGTH_SHORT).show()
            //signInWithPhoneAuthCredential(credential)
        }

        override fun onVerificationFailed(e: FirebaseException) {
            binding.OTPProgressBar.visibility = View.GONE
            e.printStackTrace()
            //val TAG = "OTPVerification"

            //Log.e(TAG, "Verification failed: ${e.message}", e)
            Toast.makeText(this@MainActivity, "Verification failed: ${e.message}", Toast.LENGTH_LONG).show()

            if (e is FirebaseAuthInvalidCredentialsException) {
                Toast.makeText(this@MainActivity, "invalid request", Toast.LENGTH_SHORT).show()
            } else if (e is FirebaseTooManyRequestsException) {
                Toast.makeText(this@MainActivity, "The SMS quota for the project has been exceeded", Toast.LENGTH_SHORT).show()
            } else if (e is FirebaseAuthMissingActivityForRecaptchaException) {
                Toast.makeText(this@MainActivity, "reCAPTCHA verification attempted with null Activity", Toast.LENGTH_SHORT).show()
            }

            Toast.makeText(this@MainActivity, "show a msg and update the UI", Toast.LENGTH_SHORT).show()
            // Show a message and update the UI
        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken,
        ) {
            // The SMS verification code has been sent to the provided phone number, we
            // now need to ask the user to enter the code and then construct a credential
            // by combining the code with a verification ID.
            Toast.makeText(this@MainActivity, "OTP code sent successfully!", Toast.LENGTH_SHORT).show()
            //Log.d(TAG, "onCodeSent:$verificationId")

            // Save verification ID and resending token so we can use them later
            val storedVerificationId = verificationId
            var resendToken = token
            binding.OTPProgressBar.visibility = View.GONE
            val intent = Intent(this@MainActivity, EnterOTPActivity::class.java)
            intent.putExtra("verification_id", storedVerificationId)
            startActivity(intent)
            finish()
        }
    }

    private fun getOTP(){
        binding.OTPButton.setOnClickListener{
            binding.OTPProgressBar.visibility = View.VISIBLE
            val phoneNumber = binding.numberTextEdit.text.toString()
            if (phoneNumber.isEmpty())
                Toast.makeText(this, "Please enter your phone number.", Toast.LENGTH_SHORT).show()
            else{
                val options = PhoneAuthOptions.newBuilder(auth)
                    .setPhoneNumber(phoneNumber) // Phone number to verify
                    .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                    .setActivity(this) // Activity (for callback binding)
                    .setCallbacks(callbacks) // OnVerificationStateChangedCallbacks
                    .build()
                PhoneAuthProvider.verifyPhoneNumber(options)
            }
        }

    }

    public override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            replaceActivity(this, OpeningActivity::class.java)
        }
    }

}