package com.enjoyingfoss.feeel.view

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.enjoyingfoss.feeel.R
import kotlinx.android.synthetic.main.activity_cover.*


/**
@author Miroslav Mazel
 */

//todo add license info
class CoverActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cover)

        startExerciseButton.setOnClickListener {
            startActivity(Intent(this, WorkoutActivity::class.java))
        }
    }
    //todo consider creating service here, then just passing it onto the activity created
}