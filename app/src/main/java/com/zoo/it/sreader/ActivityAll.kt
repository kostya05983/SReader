package com.zoo.it.sreader

import android.os.Bundle
import kotlinx.android.synthetic.main.activity_all.*

class ActivityAll : AbstractGoogleCloud() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all)
        nav_view_activity_all.setNavigationItemSelectedListener(this)
    }

}
