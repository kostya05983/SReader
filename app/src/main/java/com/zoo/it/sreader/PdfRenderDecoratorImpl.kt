package com.zoo.it.sreader

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor

class PdfRenderDecoratorImpl(descriptor: ParcelFileDescriptor): PdfRenderDecorator {
    private val pdfRenderer = android.graphics.pdf.PdfRenderer(descriptor)

    override fun getPage(width: Int, height: Int,number: Int): Bitmap {
        if(number>pdfRenderer.pageCount)
            TODO("отправляем обратно сообщение о том,что что-то пошло не таки и кидаем тост")

        val page = pdfRenderer.openPage(number)
        val bitmap = Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_4444)
        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

        return bitmap
    }
}