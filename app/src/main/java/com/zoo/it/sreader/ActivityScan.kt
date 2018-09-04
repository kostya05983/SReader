package com.zoo.it.sreader

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_scan.*

class ActivityScan : AbstractGoogleCloud() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan)
        nav_view_activity_scan.setNavigationItemSelectedListener(this)
    }
}
