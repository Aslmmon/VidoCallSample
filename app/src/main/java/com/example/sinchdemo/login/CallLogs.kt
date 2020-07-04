package com.example.sinchdemo.login

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import com.example.sinchdemo.R
import com.example.sinchdemo.callscreen.adapter.CallLogsRecyclerAdapter
import com.example.sinchdemo.model.VidoeChatDetails
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.call_logs.*

class CallLogs : BottomSheetDialogFragment() {
    lateinit var databaseReference: DatabaseReference
    lateinit var callLogRecycler: CallLogsRecyclerAdapter
    lateinit var chatDetails : MutableList<VidoeChatDetails>





    @Nullable
    override fun onCreateView(
        @NonNull inflater: LayoutInflater,
        @Nullable container: ViewGroup?,
        @Nullable savedInstanceState: Bundle?
    ): View {

        return inflater.inflate(R.layout.call_logs, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        databaseReference = FirebaseDatabase.getInstance().reference
        callLogRecycler = CallLogsRecyclerAdapter()
        call_logs_recyclr.apply {
            adapter = callLogRecycler
        }
        fetchCallLogs()



    }

    private fun fetchCallLogs() {

        FirebaseAuth.getInstance().currentUser?.uid?.let {
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
                    }
                })
//            if(chatDetails.isEmpty()) tv_empty_call_logs.visibility = View.VISIBLE
//            else tv_empty_call_logs.visibility = View.GONE
        }


    }

}