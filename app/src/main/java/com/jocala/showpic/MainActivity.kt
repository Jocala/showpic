package com.jocala.showpic

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.AbsListView
import android.widget.BaseAdapter
import android.widget.GridView
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File

class MainActivity : AppCompatActivity() {

    private companion object {
        private const val PERMISSION_REQUEST = 100
        private const val IMAGE_DIR = "/sdcard/Pictures"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        checkPermissionAndLoad()
    }

    private fun checkPermissionAndLoad() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            == PackageManager.PERMISSION_GRANTED
        ) {
            loadImages()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                PERMISSION_REQUEST
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadImages()
            } else {
                Toast.makeText(this, "Storage permission denied", Toast.LENGTH_SHORT).show()
                finishAffinity()
            }
        }
    }

    private fun loadImages() {
        val dir = File(IMAGE_DIR)
        if (!dir.exists() || !dir.isDirectory) {
            Toast.makeText(this, "Directory not found", Toast.LENGTH_SHORT).show()
            finishAffinity()
            return
        }

        val files = dir.listFiles { f ->
            f.isFile && (f.name.endsWith(".jpg", true)
                || f.name.endsWith(".jpeg", true)
                || f.name.endsWith(".png", true))
        }?.sortedBy { it.name } ?: emptyList()

        if (files.isEmpty()) {
            Toast.makeText(this, "No images found", Toast.LENGTH_SHORT).show()
            finishAffinity()
            return
        }

        val grid = findViewById<GridView>(R.id.grid)
        grid.adapter = ImageAdapter(files)
        grid.setOnItemClickListener { _, _, position, _ ->
            val intent = Intent(this, ImageViewerActivity::class.java)
            intent.putExtra("image_path", files[position].absolutePath)
            startActivity(intent)
        }
    }

    private class ImageAdapter(private val files: List<File>) : BaseAdapter() {
        override fun getCount() = files.size
        override fun getItem(position: Int) = files[position]
        override fun getItemId(position: Int) = position.toLong()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val iv = (convertView as? ImageView) ?: ImageView(parent.context).apply {
                scaleType = ImageView.ScaleType.CENTER_CROP
                layoutParams = AbsListView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    240
                )
            }
            val opts = BitmapFactory.Options().apply { inSampleSize = 8 }
            val bm = BitmapFactory.decodeFile(files[position].absolutePath, opts)
            iv.setImageBitmap(bm)
            return iv
        }
    }
}
