package com.example.sinchdemo

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.sinchdemo.service.SinchService
import com.example.sinchdemo.service.SinchService.SinchServiceInterface
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser


abstract class BaseActivity : AppCompatActivity(), ServiceConnection {

    private var mSinchServiceInterface: SinchServiceInterface? = null
    lateinit var firebaseAuth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firebaseAuth = FirebaseAuth.getInstance()
        applicationContext.bindService(Intent(this, SinchService::class.java), this, Context.BIND_AUTO_CREATE)
        setContentView(getLAyout())
    }


    override fun onServiceConnected(p0: ComponentName?, iBinder: IBinder?) {
      //  if (SinchService::class.java.name == componentName.className) {
            mSinchServiceInterface = iBinder as SinchServiceInterface
            onServiceConnected()
      //  }

    }

    override fun onServiceDisconnected(p0: ComponentName?) {
       // if (SinchService::class.java.name == componentName.className) {
            mSinchServiceInterface = null
            onServiceDisconnected()
        //}

    }


    protected open fun onServiceConnected() {
        Log.i("service", "connected")
    }

    protected fun onServiceDisconnected() {
        Log.i("service", "disconnected")
    }

    protected fun getSinchServiceInterfaceNew(): SinchService.SinchServiceInterface? {
        return mSinchServiceInterface
    }

    protected fun getFirebaseUser(): FirebaseUser? {
        val firebaseUser = firebaseAuth.currentUser
        return firebaseUser

    }
    abstract fun getLAyout(): Int

}