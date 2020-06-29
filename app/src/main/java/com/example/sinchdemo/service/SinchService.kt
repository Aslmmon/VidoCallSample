package com.example.sinchdemo.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import com.example.sinchdemo.IncomingCall.IncomingCallActivity
import com.sinch.android.rtc.*
import com.sinch.android.rtc.calling.Call
import com.sinch.android.rtc.calling.CallClient
import com.sinch.android.rtc.calling.CallClientListener
import com.sinch.android.rtc.video.VideoController


class SinchService : Service() {
    private val APP_KEY = "98db0acf-dc4b-4c76-8096-c01a22c2b70f"
    private val APP_SECRET = "UtN0xRgU20GFLuWO59L/+Q=="
    private val ENVIRONMENT = "clientapi.sinch.com"

    companion object {
        val CALL_ID = "CALL_ID"

    }

    val TAG = SinchService::class.java.simpleName
    private val mSinchServiceInterface  = SinchServiceInterface()
    private var mSinchClient: SinchClient? = null
    private var mUserId: String? = null
    private var mListener: StartFailedListener? = null


    override fun onCreate() {
        super.onCreate()
    }

    override fun onDestroy() {
        if (mSinchClient != null && mSinchClient!!.isStarted) {
            this.mSinchClient?.terminate()
        }
        super.onDestroy()
    }

    private fun start(userName: String) {
        if (mSinchClient == null) {
            mUserId = userName
            mSinchClient =
                Sinch.getSinchClientBuilder().context(applicationContext).userId(userName)
                    .applicationKey(APP_KEY)
                    .applicationSecret(APP_SECRET)
                    .environmentHost(ENVIRONMENT).build()
            this.mSinchClient?.setSupportCalling(true)
            this.mSinchClient?.startListeningOnActiveConnection()
            this.mSinchClient?.addSinchClientListener(MySinchClientListener())
            this.mSinchClient?.callClient?.addCallClientListener(SinchCallClientListener())
            this.mSinchClient?.start()
        }

        else{

            Toast.makeText(this, "Not initialized", Toast.LENGTH_SHORT).show()
        }
        
        
    }


    private fun stop() {
        if (mSinchClient != null) {
            mSinchClient!!.terminate()
            mSinchClient = null
        }
    }

    private fun isStarted(): Boolean {
        return mSinchClient != null && mSinchClient!!.isStarted
    }


    override fun onBind(p0: Intent?): IBinder? {
        return mSinchServiceInterface;
    }


    inner class SinchServiceInterface : Binder() {
        fun callUserVideo(userId: String?): Call {
            return mSinchClient?.callClient?.callUserVideo(userId)!!
        }

        fun callPhone(userId: String?): Call {
            return mSinchClient?.callClient?.callUser(userId)!!
        }


        val userName: String?
            get() = mUserId

        val isStarted: Boolean get() = this@SinchService.isStarted()

        fun startClient(userName: String?) {
            userName?.let { start(it) }
        }

        fun stopClient() {
            stop()
        }

        fun setStartListener(listener: StartFailedListener) {
            mListener = listener
        }

        fun getCall(callId: String?): Call {
            return mSinchClient?.callClient?.getCall(callId)!!
        }

        fun pauseVideo(callId: String?): Unit? {
            return mSinchClient?.callClient?.getCall(callId)?.pauseVideo()
        }


        val videoController: VideoController?
            get() = if (!isStarted) {
                null
            } else mSinchClient?.videoController

        val audioController: AudioController?
            get() {
                return if (!isStarted) {
                    null
                } else mSinchClient?.audioController
            }
    }

    interface StartFailedListener {
        fun onStartFailed(error: SinchError?)
        fun onStarted()
    }


    private inner class MySinchClientListener : SinchClientListener {
        override fun onClientFailed(client: SinchClient, error: SinchError) {
            if (mListener != null) {
                mListener?.onStartFailed(error)
            }
            mSinchClient?.terminate()
            mSinchClient = null
        }

        override fun onClientStarted(client: SinchClient) {
            Log.d(TAG, "SinchClient started")
            if (mListener != null) {
                mListener?.onStarted()
            }
        }

        override fun onClientStopped(client: SinchClient) {
            Log.d(TAG, "SinchClient stopped")
        }

        override fun onLogMessage(
            level: Int,
            area: String,
            message: String
        ) {
            when (level) {
                Log.DEBUG -> Log.d(area, message)
                Log.ERROR -> Log.e(area, message)
                Log.INFO -> Log.i(area, message)
                Log.VERBOSE -> Log.v(area, message)
                Log.WARN -> Log.w(area, message)
            }
        }

        override fun onRegistrationCredentialsRequired(
            client: SinchClient,
            clientRegistration: ClientRegistration
        ) {
        }
    }

    private inner class SinchCallClientListener : CallClientListener {
        override fun onIncomingCall(
            callClient: CallClient?,
            call: Call
        ) {
            val intent = Intent(this@SinchService, IncomingCallActivity::class.java)
            intent.putExtra(CALL_ID, call.callId)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            this@SinchService.startActivity(intent)
        }
    }

}