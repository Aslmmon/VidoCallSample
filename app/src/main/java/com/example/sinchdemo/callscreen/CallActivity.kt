package com.example.sinchdemo.callscreen

import android.media.AudioManager
import android.os.Bundle
import android.util.Log
import android.widget.*
import com.example.sinchdemo.AudioPlayer
import com.example.sinchdemo.BaseActivity
import com.example.sinchdemo.R
import com.example.sinchdemo.model.VidoeChatDetails
import com.example.sinchdemo.service.SinchService
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.sinch.android.rtc.AudioController
import com.sinch.android.rtc.PushPair
import com.sinch.android.rtc.calling.Call
import com.sinch.android.rtc.calling.CallDetails
import com.sinch.android.rtc.calling.CallEndCause
import com.sinch.android.rtc.calling.CallState
import com.sinch.android.rtc.video.VideoCallListener
import com.sinch.android.rtc.video.VideoController
import kotlinx.android.synthetic.main.activity_call.*
import java.util.*
import java.util.concurrent.TimeUnit


class CallActivity : BaseActivity() {
    val CALL_START_TIME = "callStartTime"
    val ADDED_LISTENER = "addedListener"
    private var mAudioPlayer: AudioPlayer? = null
    private var mTimer: Timer? = null
    private var mDurationTask: UpdateCallDurationTask? = null
    lateinit var databaseReference: DatabaseReference
    lateinit var currentTime: Date
    lateinit var endTime: Date


    private var mCallId: String? = null
    private var mCallStart: Long = 0
    private var mAddedListener = false
    private var mVideoViewsAdded = false

    private var mCallDuration: TextView? = null
    private var mCallState: TextView? = null
    private var mCallerName: TextView? = null


    inner class UpdateCallDurationTask : TimerTask() {
        override fun run() {
            this@CallActivity.runOnUiThread(Runnable { updateCallDuration() })
        }
    }

    //method to update live duration of the call
    private fun updateCallDuration() {
        if (mCallStart > 0) {
            mCallDuration?.text = formatTimespan(System.currentTimeMillis() - mCallStart)
        }
    }

    private fun formatTimespan(timespan: Long): String? {
        val totalSeconds = timespan / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format(Locale.US, "%02d:%02d", minutes, seconds)
    }


    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        savedInstanceState.putLong(
            CALL_START_TIME, mCallStart
        )
        savedInstanceState.putBoolean(
            ADDED_LISTENER, mAddedListener
        )
        super.onSaveInstanceState(savedInstanceState)

    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        mCallStart = savedInstanceState.getLong(CALL_START_TIME)
        mAddedListener = savedInstanceState.getBoolean(ADDED_LISTENER)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mAudioPlayer = AudioPlayer(this)
        mCallDuration = findViewById(R.id.callDuration)
        mCallerName = findViewById(R.id.remoteUser)
        mCallState = findViewById(R.id.callState)
        val endCallButton = findViewById<Button>(R.id.hangupButton)
        endCallButton.setOnClickListener { endCall() }
        databaseReference = FirebaseDatabase.getInstance().reference.child("users")


        mCallId = intent.getStringExtra(SinchService.CALL_ID)
        if (savedInstanceState == null) {
            mCallStart = System.currentTimeMillis()
        }
    }

    //method to end the call
    private fun endCall() {
        mAudioPlayer?.stopProgressTone()
        val call: Call? = getSinchServiceInterfaceNew()?.getCall(mCallId)
        call?.hangup()
        finish()
    }

    private fun pauseCall() {
        mAudioPlayer?.stopProgressTone()
        val call: Call? = getSinchServiceInterfaceNew()?.getCall(mCallId)
        call?.pauseVideo()
        //finish()
    }

    private fun resumeCall() {
        mAudioPlayer?.stopProgressTone()
        val call: Call? = getSinchServiceInterfaceNew()?.getCall(mCallId)
        call?.resumeVideo()
        //finish()
    }

    override fun getLAyout() = R.layout.activity_call

    override fun onServiceConnected() {
        val call: Call? = getSinchServiceInterfaceNew()?.getCall(mCallId)
        if (call != null) {
            if (!mAddedListener) {
                call.addCallListener(SinchCallListener())
                mAddedListener = true
            }
        } else {
            Log.e(
                "test", "Started with invalid callId, aborting."
            )
            finish()
        }
        updateUI()
    }

    private fun updateUI() {
        if (getSinchServiceInterfaceNew() == null) {
            return  // early
        }
        val call: Call? = getSinchServiceInterfaceNew()?.getCall(mCallId)
        if (call != null) {
            mCallerName?.text = call.remoteUserId
            if (call.state == CallState.INITIATING) mCallState?.text = "Connecting"
            else mCallState?.text = call.state.toString()
            if (call.state == CallState.ESTABLISHED) {
                //when the call is established, addVideoViews configures the video to  be shown
                addVideoViews()
            }
        }
    }

    //stop the timer when call is ended
    override fun onStop() {
        super.onStop()
        mDurationTask?.cancel()
        mTimer?.cancel()
        removeVideoViews()
    }

    //start the timer for the call duration here
    override fun onStart() {
        super.onStart()
        mTimer = Timer()
        mDurationTask = UpdateCallDurationTask()
        mTimer?.schedule(mDurationTask, 0, 500)
        updateUI()
    }

    override fun onBackPressed() {
        // User should exit activity by ending call, not by going back.
    }

    //method which sets up the video feeds from the server to the UI of the activity
    private fun addVideoViews() {
        if (mVideoViewsAdded || getSinchServiceInterfaceNew() == null) {
            return  //early
        }
        val vc: VideoController? = getSinchServiceInterfaceNew()?.videoController
        if (vc != null) {
            val localView = findViewById<RelativeLayout>(R.id.localVideo)
            localView.addView(vc.localView)
            localView.setOnClickListener { //this toggles the front camera to rear camera and vice versa
                vc.toggleCaptureDevicePosition()
            }
            val view = findViewById<LinearLayout>(R.id.remoteVideo)
            view.addView(vc.remoteView)
            mVideoViewsAdded = true
        }
    }

    //removes video feeds from the app once the call is terminated
    private fun removeVideoViews() {
        if (getSinchServiceInterfaceNew() == null) {
            return  // early
        }
        val vc: VideoController? = getSinchServiceInterfaceNew()?.videoController
        if (vc != null) {
            val view = findViewById<LinearLayout>(R.id.remoteVideo)
            view.removeView(vc.remoteView)
            val localView = findViewById<RelativeLayout>(R.id.localVideo)
            localView.removeView(vc.localView)
            mVideoViewsAdded = false
        }
    }

    inner class SinchCallListener : VideoCallListener {
        override fun onCallEnded(call: Call) {
            val cause = call.details.endCause
            endTime = Calendar.getInstance().time
            Log.i("cause", "Call ended. Reason: $cause")
            mAudioPlayer?.stopProgressTone()
            volumeControlStream = AudioManager.USE_DEFAULT_STREAM_TYPE
            when (cause) {
                CallEndCause.HUNG_UP -> {
                    saveToDatabase(call.details)
                }
                CallEndCause.DENIED -> {
                    Toast.makeText(this@CallActivity, "call Denied", Toast.LENGTH_SHORT).show()

                }
                CallEndCause.CANCELED -> {
                    Toast.makeText(this@CallActivity, "call Cancelled", Toast.LENGTH_SHORT).show()

                }
                CallEndCause.NO_ANSWER -> {
                    Toast.makeText(
                        this@CallActivity,
                        "No Answer after  ${callDuration.text} seconds",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                CallEndCause.TIMEOUT -> {
                    Log.i(javaClass.simpleName, cause.toString())
                    Toast.makeText(
                        this@CallActivity,
                        "call Timeout after  ${callDuration.text} seconds",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                CallEndCause.FAILURE -> {
                    Toast.makeText(this@CallActivity, "call Failed", Toast.LENGTH_SHORT).show()

                }
            }
            endCall()
        }

        override fun onCallEstablished(call: Call) {
            currentTime = Calendar.getInstance().time
            mAudioPlayer?.stopProgressTone()
            mCallState?.text = call.state.toString()
            volumeControlStream = AudioManager.STREAM_VOICE_CALL
            val audioController: AudioController? = getSinchServiceInterfaceNew()?.audioController
            audioController?.enableSpeaker()
            mCallStart = System.currentTimeMillis()
        }

        override fun onVideoTrackResumed(p0: Call?) {

        }

        override fun onCallProgressing(call: Call) {
            Log.d(
                "test",
                "Call progressing"
            )
            mAudioPlayer?.playProgressTone()
        }

        override fun onShouldSendPushNotification(
            call: Call,
            pushPairs: List<PushPair>
        ) {
            // Send a push through your push provider here, e.g. GCM
        }

        override fun onVideoTrackAdded(call: Call) {
            Log.d("test", "Video track added")
            addVideoViews()
        }

        override fun onVideoTrackPaused(p0: Call?) {

        }
    }

    private fun saveToDatabase(details: CallDetails?) {
        val duration = endTime.time - currentTime.time
        val durationSeconds = TimeUnit.MILLISECONDS.toSeconds(duration)
        val durationMinutes = "${TimeUnit.MILLISECONDS.toMinutes(duration)} minutes "
        Log.i("cause", "user " + getFirebaseUser()?.email)
        Log.i("cause", "user reciever $mCallerName")
        val videoChatDetails = VidoeChatDetails(
            callStartTime = currentTime.toString(),
            callEndTime = endTime.toString(),
            userCalling = getFirebaseUser()?.email.toString(),
            userRecieveing = mCallerName?.text.toString(),
            duration = getDuration(durationSeconds, durationMinutes)
        )
        Toast.makeText(this, getDuration(durationSeconds, durationMinutes), Toast.LENGTH_SHORT)
            .show()

        /**
         * user calling , user Recieveing
         */
        getFirebaseUser()?.uid?.let { fuId ->
            databaseReference.child(fuId).child("chat Details").push().setValue(videoChatDetails)
                .addOnCompleteListener {
                    Toast.makeText(this, "Not Saved", Toast.LENGTH_SHORT).show()
                }
        }

    }

    private fun getDuration(durationSeconds: Long, durationMinutes: String) =
        if (durationSeconds > 60) durationMinutes + "minutes" else "$durationSeconds seconds"


}