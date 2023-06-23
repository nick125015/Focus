package com.example.focus

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.example.focus.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseUser
import com.facebook.*
import com.facebook.login.LoginResult
import com.facebook.login.widget.LoginButton
import com.facebook.appevents.AppEventsLogger
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.ktx.initialize

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityMainBinding
    private lateinit var callbackManager: CallbackManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FacebookSdk.sdkInitialize(applicationContext)
        AppEventsLogger.activateApp(application)
        val rootLayout = findViewById<View>(android.R.id.content)
        rootLayout.setBackgroundResource(R.drawable.activityground)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Firebase.initialize(this)
        auth = FirebaseAuth.getInstance()
        callbackManager = CallbackManager.Factory.create()


        binding.signUp.setOnClickListener {

            var intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        val loginButton = findViewById<LoginButton>(R.id.fbSignIn)
        loginButton.setPermissions("email_address", "public_profile")
        loginButton.registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {
                handleFacebookAccessToken(loginResult.accessToken)
            }

            override fun onCancel() {}
            override fun onError(error: FacebookException) {}
        })


        binding.signIn.setOnClickListener {
            if (binding.email.text.toString().isEmpty()) {
                showMessage("請輸入帳號")
            } else if (binding.password.text.toString().isEmpty()) {
                showMessage("請輸入密碼")
            } else {
                signIn()
            }
        }

    }

    private fun signIn() {
        val email = binding.email.text.toString()
        val password = binding.password.text.toString()

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    println("---------signInWithEmail:success-----------")
                    val user = auth.currentUser
                    updateUI(user)
                } else {
                    it.exception?.message?.let { }
                    println("---------error---------------")
                    showMessage("登入失敗，帳號或密碼錯誤")
                    updateUI(null)
                }
            }
    }

    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            binding.email.visibility = View.GONE
            binding.password.visibility = View.GONE
            binding.signIn.visibility = View.GONE
            binding.signUp.visibility = View.GONE

            val intent = Intent(this, MyMenu::class.java)
            intent.putExtra("userId", user.uid)
            intent.putExtra("initialFragment", "weather") // 添加此行以指定初始的WeatherFragment
            startActivity(intent)
            finish()
        } else {
            binding.email.visibility = View.VISIBLE
            binding.password.visibility = View.VISIBLE
            binding.signIn.visibility = View.VISIBLE
            binding.signUp.visibility = View.VISIBLE
        }
    }

    private fun showMessage(message: String) {
        val alertDialog: AlertDialog.Builder = AlertDialog.Builder(this@MainActivity)
        alertDialog.setMessage(message)
        alertDialog.setPositiveButton("確定") { dialog, which -> }
        alertDialog.show()
    }

    private fun handleFacebookAccessToken(token: AccessToken) {
        val credential = FacebookAuthProvider.getCredential(token.token)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                } else {
                    Toast.makeText(this@MainActivity, "登入失敗", Toast.LENGTH_SHORT).show()
                }
            }

    }
}