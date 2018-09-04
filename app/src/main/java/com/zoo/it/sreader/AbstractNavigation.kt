package com.zoo.it.sreader

import android.content.Intent
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem

abstract class AbstractNavigation:AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_activity_all -> {
                val intent = Intent(this, ActivityAll::class.java)
                startActivity(intent)
            }
            R.id.nav_activity_catalog -> {
                val intent = Intent(this, ActivityCatalog::class.java)
                startActivity(intent)
            }
            R.id.nav_activity_scan -> {
                val intent = Intent(this, ActivityScan::class.java)
                startActivity(intent)
            }
            R.id.nav_activity_settings -> {
                val intent = Intent(this, ActivitySettings::class.java)
                startActivity(intent)
            }
        }

        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        drawer.closeDrawer(GravityCompat.START)
        return true
    }
}