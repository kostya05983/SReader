package com.zoo.it.sreader

import android.graphics.Bitmap

interface PdfRenderDecorator {

    fun getPage(width: Int, height: Int,number: Int):Bitmap
}