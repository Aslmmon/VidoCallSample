package com.example.sinchdemo.IncomingCall

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.example.sinchdemo.AudioPlayer
import com.example.sinchdemo.BaseActivity
import com.example.sinchdemo.R
import com.example.sinchdemo.callscreen.CallActivity
import com.example.sinchdemo.service.SinchService
import com.sinch.android.rtc.PushPair
import com.sinch.android.rtc.calling.Call
import com.sinch.android.rtc.video.VideoCallListener

class IncomingCallActivity : BaseActivity() {
    val TAG: String = IncomingCallActivity::class.java.simpleName
    private var mCallId: String? = null
    private var mAudioPlayer: AudioPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val answer = findViewById<Button>(R.id.answerButton)
        answer.setOnClickListener(mClickListener)
        val decline = findViewById<Button>(R.id.declineButton)
        decline.setOnClickListener(mClickListener)

        mAudioPlayer = AudioPlayer(this)
        mAudioPlayer?.playRingtone()
        mCallId = intent.getStringExtra(SinchService.CALL_ID)
    }

    override fun onServiceConnected() {
        val call: Call? = getSinchServiceInterfaceNew()?.getCall(mCallId)
        if (call != null) {
            call.addCallListener(SinchCallListener())
            val remoteUser = findViewById<TextView>(R.id.remoteUser)
            remoteUser.text = call.remoteUserId
        } else {
            Log.e(TAG, "Started with invalid callId, aborting")
            finish()
        }
    }

    private fun answerClicked() {
        mAudioPlayer?.stopRingtone()
        val call: Call? = getSinchServiceInterfaceNew()?.getCall(mCallId)
        if (call != null) {
            call.answer()
            val intent = Intent(this, CallActivity::class.java)
            intent.putExtra(SinchService.CALL_ID, mCallId)
            startActivity(intent)
        } else {
            finish()
        }
    }

    private fun declineClicked() {
        mAudioPlayer!!.stopRingtone()
        val call: Call? = getSinchServiceInterfaceNew()?.getCall(mCallId)
        call?.hangup()
        finish()
    }

    inner class SinchCallListener : VideoCallListener {
        override fun onCallEnded(call: Call) {
            val cause = call.details.endCause
            Log.d(TAG, "Call ended, cause: $cause")
            mAudioPlayer?.stopRingtone()
            finish()
        }

        override fun onCallEstablished(call: Call) {
            Log.d(TAG, "Call established")
        }

        override fun onVideoTrackResumed(p0: Call?) {
        }

        override fun onCallProgressing(call: Call) {
            Log.d(TAG, "Call progressing")
        }

        override fun onShouldSendPushNotification(
            call: Call,
            pushPairs: List<PushPair>
        ) {
            // Send a push through your push provider here, e.g. GCM
        }

        override fun onVideoTrackAdded(call: Call) {
            // Display some kind of icon showing it's a video call
        }

        override fun onVideoTrackPaused(p0: Call?) {
        }
    }

    private val mClickListener =
        View.OnClickListener { v ->
            when (v.id) {
                R.id.answerButton -> answerClicked()
                R.id.declineButton -> declineClicked()
            }
        }
    override fun getLAyout(): Int = R.layout.activity_incoming_call
}