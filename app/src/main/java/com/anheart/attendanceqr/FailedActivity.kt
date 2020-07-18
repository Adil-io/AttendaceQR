package com.anheart.attendanceqr

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_failed.*

class FailedActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_failed)

        btn_scanAgain.setOnClickListener {
            Intent(this,LoginActivity::class.java).also {
                startActivity(it)
                finish()
            }
        }
    }

    override fun onBackPressed() {
//        Intent(this,LoginActivity::class.java).also {
//            startActivity(it)
//            finish()
//        }
        Toast.makeText(this@FailedActivity,"Cant go back at this stage!\n Tap Scan Again",Toast.LENGTH_SHORT).show()
    }
}