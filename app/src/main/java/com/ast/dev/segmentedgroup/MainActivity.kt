package com.ast.dev.segmentedgroup

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.ast.dev.library.SegmentButton
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val button1 = SegmentButton(this, "Bird")
        val button2 = SegmentButton(this, "Fish")
        button1.isChecked = true
        segmented_group.addView(button1)
        segmented_group.addView(button2)
    }
}
