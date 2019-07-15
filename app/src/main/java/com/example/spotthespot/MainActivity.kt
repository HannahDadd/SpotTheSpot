package com.example.spotthespot

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity;
import android.view.Menu
import android.view.MenuItem

import kotlinx.android.synthetic.main.activity_main.*
import android.R

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        // When you click the button it takes a picture
        fab.setOnClickListener { view ->
            dispatchTakePictureIntent()
        }
    }

    val REQUEST_IMAGE_CAPTURE = 1

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(packageManager)?.also {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            imageView.setImageBitmap(imageBitmap)
            thresholding(imageBitmap)
        }
    }

    // Use thresholding to remove lighter skin from darker skin (spot)
    private fun thresholding(image: Bitmap) {
        var isCancer = true
        val threshold = 0.4
        val factor = 255f;
        val newImage = Array(image.width) { IntArray(image.height) }
        val ranges = HashMap<Int, Int>()
        for(i in 4..10) {
            ranges.put(i, 0)
        }
        var pixelsInSpot = 0
        for(y in 0..image.height-1) {
            for(x in 0..image.width-1) {
                val pixel = image.getPixel(x, y)
                val R = pixel shr 16 and 0xFF
                val G = pixel shr 8 and 0xFF
                val B = pixel and 0xFF
                val lum = 0.2126f * R / factor + 0.2126f * G / factor + 0.0722f * B / factor

                // If it is a spot pixel plot it's value on the graph
                if (lum > 0.4) {
                    ranges.put((lum*10).toInt(), ranges.getValue((lum*10).toInt()) + 1)
                    pixelsInSpot = pixelsInSpot + 1
                    newImage[x][y] = pixel
                }
                else newImage[x][y] = pixel
            }
        }
        // Check that 2/3 of the pixels in the image are in the same range
        for(i in 70..120 step 10) {
            if(ranges.getValue(i) > pixelsInSpot*0.6) isCancer = false
        }
        newImage.size
        //imageView.setImageBitmap(newImage as Bitmap)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
}
