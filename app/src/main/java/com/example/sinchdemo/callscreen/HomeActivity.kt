package com.example.sinchdemo.callscreen

import android.content.Intent
import android.os.Bundle
import android.provider.CallLog
import android.text.Html
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.example.sinchdemo.BaseActivity
import com.example.sinchdemo.R
import com.example.sinchdemo.callscreen.adapter.CallLogsRecyclerAdapter
import com.example.sinchdemo.callscreen.adapter.UsersRecyclerAdapter
import com.example.sinchdemo.login.MainActivity
import com.example.sinchdemo.model.User
import com.example.sinchdemo.model.VidoeChatDetails
import com.example.sinchdemo.service.SinchService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.sinch.android.rtc.calling.Call
import kotlinx.android.synthetic.main.activity_home.*
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


class HomeActivity : UsersRecyclerAdapter.Interaction,
    BaseActivity() {
    lateinit var databaseReference: DatabaseReference
    lateinit var usersArrayList: MutableList<User>
    lateinit var userRecyclerAdapter: UsersRecyclerAdapter
    lateinit var callLogRecycler: CallLogsRecyclerAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        usersArrayList = mutableListOf()
        supportActionBar?.title = Html.fromHtml("<font color='#ffffff'>Choose user to initiate a call </font>");
        userRecyclerAdapter = UsersRecyclerAdapter(this@HomeActivity)
        callLogRecycler = CallLogsRecyclerAdapter()

        databaseReference = FirebaseDatabase.getInstance().reference
        featchAllUsers()

        fetchCallLogs()


    }

    private fun fetchCallLogs() {
        Log.i(javaClass.simpleName, getFirebaseUser()?.uid.toString())

        getFirebaseUser()?.uid?.let {
            databaseReference.child("users").child(it).child("chat Details")
                .addValueEventListener(object : ValueEventListener {
                    override fun onCancelled(error: DatabaseError) {

                    }

                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val chatDetails = mutableListOf<VidoeChatDetails>()
                        dataSnapshot.children.forEach {
                            val videoChatDetails = it.getValue(VidoeChatDetails::class.java)
                            if (videoChatDetails != null) chatDetails.add(videoChatDetails)
                        }
                        callLogRecycler.submitList(chatDetails)
                        Log.i("call", chatDetails.toString())
                        Log.i("call", chatDetails.size.toString())

//                        call_logs_recyclr.apply {
//                            adapter = callLogRecycler
//                        }
                    }

                })
        }

    }

    private fun featchAllUsers() {
        databaseReference.child("users").addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                Log.i(javaClass.simpleName, error.message.toString())
                Log.i(javaClass.simpleName, error.details)

            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                usersArrayList.clear()
                dataSnapshot.children.forEach {
                    val User = it.getValue(User::class.java)
                    if (User != null && User.id != getFirebaseUser()?.uid) {
                        usersArrayList.add(User)
                    }

                }
                userRecyclerAdapter.submitList(usersArrayList)
                /**
                 * adapter to submit list to it ..
                 */
                recycler.apply {
                    adapter = userRecyclerAdapter
                }
            }

        })
    }


    override fun getLAyout() = R.layout.activity_home

    override fun onServiceConnected() {
        getSinchServiceInterfaceNew()?.userName?.let { Log.i(javaClass.simpleName, it) }

    }

    override fun onDestroy() {
        if (getSinchServiceInterfaceNew() != null) {
            getSinchServiceInterfaceNew()?.stopClient()
        }
        super.onDestroy()
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.signOut) {
            firebaseAuth.signOut()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return true
        }

        return super.onOptionsItemSelected(item)

    }


    override fun onItemSelected(position: Int, item: User) {
        val call: Call? = getSinchServiceInterfaceNew()?.callUserVideo(item.name)
        val callId = call?.callId
        val callScreen = Intent(this, CallActivity::class.java)
        callScreen.putExtra(SinchService.CALL_ID, callId)
        startActivity(callScreen)


    }


}





