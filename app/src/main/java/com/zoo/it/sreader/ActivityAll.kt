package com.zoo.it.sreader

import android.os.Bundle

class ActivityAll : AbstractGoogleCloud() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        signIn()
        existFolder()
        setContentView(R.layout.activity_all)
    }


}
