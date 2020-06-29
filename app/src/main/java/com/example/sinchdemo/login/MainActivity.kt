package com.example.sinchdemo.login

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.sinchdemo.BaseActivity
import com.example.sinchdemo.R
import com.example.sinchdemo.callscreen.HomeActivity
import com.example.sinchdemo.service.SinchService
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.sinch.android.rtc.SinchError
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity :  BaseActivity(),
    SinchService.StartFailedListener {
    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mAuth = FirebaseAuth.getInstance()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.CAMERA,
                    Manifest.permission.ACCESS_NETWORK_STATE,
                    Manifest.permission.READ_PHONE_STATE
                ), 100
            )
        }

        btn_signIn.setOnClickListener {
            val name = et_name_ed.text.toString()
            val password = et_password_ed.text.toString()
            if(name.isEmpty() || password.isEmpty()) return@setOnClickListener
            mAuth.signInWithEmailAndPassword(name, password)
                .addOnCompleteListener(this, OnCompleteListener<AuthResult?> { task ->
                    if (task.isSuccessful) {
                        if (!getSinchServiceInterfaceNew()?.isStarted!!) {
                            getSinchServiceInterfaceNew()?.startClient(getFirebaseUser()?.email)
                        }
                        else {
                            goToHomeActivity()
                        }
                    } else {
                        Toast.makeText(
                            this, "Authentication failed.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    task.exception?.message?.let { it1 -> Log.i(javaClass.simpleName, it1) }
                    task.exception?.localizedMessage?.let { it1 ->
                        Log.i(
                            javaClass.simpleName,
                            it1
                        )
                    }

                })

        }
        btn_signUp.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }
    }

    override fun getLAyout(): Int {
        return R.layout.activity_main
    }


    override fun onStart() {
        super.onStart()
//        val currentUser = mAuth.currentUser
//        if (currentUser != null) {
//            goToHomeActivity()
//        }
    }


    override fun onStartFailed(error: SinchError?) {
        Log.i(javaClass.simpleName,error.toString())
    }

    //this method is invoked when the connection is established with the SinchService
    override fun onServiceConnected() {
        Log.i("service", "connected")
        getSinchServiceInterfaceNew()?.setStartListener(this)
    }



    override fun onStarted() {
        goToHomeActivity()
    }

    private fun goToHomeActivity() {
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }
}