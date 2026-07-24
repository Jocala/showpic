package com.jocala.showpic

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import java.io.File

class ImageViewerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_viewer)

        val path = intent.getStringExtra("image_path")
        if (path == null || !File(path).exists()) {
            finishAffinity()
            return
        }

        val imageView = findViewById<ImageView>(R.id.image_viewer)

        val display = windowManager.defaultDisplay
        val targetW = if (display.width > display.height) display.width else display.height
        val targetH = if (display.width > display.height) display.height else display.width

        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(path, bounds)

        val sampleSize = (maxOf(bounds.outWidth, bounds.outHeight) / targetW).let { maxOf(it, 1) }

        val opts = BitmapFactory.Options().apply { inSampleSize = sampleSize }
        val bm = BitmapFactory.decodeFile(path, opts)
        imageView.setImageBitmap(bm)

        imageView.setOnClickListener {
            finishAffinity()
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_FULLSCREEN
        }
    }
}
