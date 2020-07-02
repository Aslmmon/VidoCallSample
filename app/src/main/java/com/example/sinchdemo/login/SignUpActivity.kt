package com.example.sinchdemo.login

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.sinchdemo.BaseActivity
import com.example.sinchdemo.R
import com.example.sinchdemo.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_sign_up.*

class SignUpActivity : BaseActivity() {
    lateinit var databaseReference: DatabaseReference


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        databaseReference = FirebaseDatabase.getInstance().reference.child("users")

        btn_signUp_register.setOnClickListener {
            if(et_name_ed_register.text?.isEmpty()!! || et_password_ed_register.text?.isEmpty()!!){
                Toast.makeText(this, "fill Empty Fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            showProgress()
            firebaseAuth.createUserWithEmailAndPassword(
                et_name_ed_register.text.toString(),
                et_password_ed_register.text.toString()
            ).addOnCompleteListener {
                    if (it.isSuccessful) {
                        val firebaseUser = firebaseAuth.currentUser
                        val user = firebaseUser?.uid?.let { it1 -> User(name = et_name_ed_register.text.toString(), id = it1) }
                        firebaseUser?.uid?.let { fuId ->
                            databaseReference.child(fuId).setValue(user).addOnCompleteListener {
                                    if (it.isSuccessful) {
                                        Toast.makeText(
                                            this,
                                            "Created Successfully",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        dismisProgress()
                                        finish()
                                    } else {
                                        dismisProgress()
                                        Toast.makeText(this, "Not Created", Toast.LENGTH_SHORT).show() }
                                }
                        }
                    }
                }
                .addOnFailureListener { it->
                    dismisProgress()
                    Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
                    Log.i(javaClass.simpleName,it.toString())
                }
        }
    }

    override fun getLAyout(): Int {
        return R.layout.activity_sign_up
    }
}