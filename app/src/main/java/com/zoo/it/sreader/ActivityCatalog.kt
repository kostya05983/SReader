package com.zoo.it.sreader

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_catalog.*

class ActivityCatalog : AbstractNavigation() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_catalog)
        nav_view_activity_catalog.setNavigationItemSelectedListener(this)
    }
}
