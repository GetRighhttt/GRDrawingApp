package com.example.drawingapp

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.media.MediaScannerConnection
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.lifecycle.lifecycleScope
import com.example.drawingapp.view.DrawingView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {

    private var drawingView: DrawingView? = null
    private var aImageButtonCurrentPaint: ImageButton? = null

    // assigning our intent to go to the media of our device
    // want to be able to replace our imageView with an image in the gallery
    private val openGalleryLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            result ->
            if(result.resultCode == RESULT_OK && result.data != null) {
                val imageBackground: ImageView = findViewById(R.id.iv_background)
                // gives us the location of the data
                imageBackground.setImageURI(result.data?.data)
            }
        }

    // request permission check
    private val requestPermission: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            permissions ->
            permissions.entries.forEach{
                val permissionName = it.key // type string
                val isGranted = it.value // type Boolean

                if(isGranted) { // if permission is granted
                   Toast.makeText(this@MainActivity,
                   "Permission granted now you can read the storage files.",
                   Toast.LENGTH_LONG).show()

                    // An intent to go to the External Storage of our Device
                    val pickIntent = Intent(Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    openGalleryLauncher.launch(pickIntent)
                } else {
                    if(permissionName == Manifest.permission.READ_EXTERNAL_STORAGE) {
                        Toast.makeText(this@MainActivity,
                        "Permission Denied.",
                        Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // find the id of Drawing view
        drawingView = findViewById(R.id.drawing_view)
        drawingView?.setSizeForBrush(20.toFloat()) // set size of brush

        // reference to linear layout
        val linearLayoutPaintColors = findViewById<LinearLayout>(R.id.ll_paint_colors)
        // want to use item at index 1 and treated like a button
        aImageButtonCurrentPaint = linearLayoutPaintColors[1] as ImageButton
        // Set the onclick to use the palletpressed drawable
        // When pallet is pressed, it turns grey
        aImageButtonCurrentPaint!!.setImageDrawable(
            ContextCompat.getDrawable(this, R.drawable.pallet_pressed))

        // Undo button functionality
        val ibUndo: ImageButton = findViewById(R.id.ib_undo)
        ibUndo.setOnClickListener {
            drawingView?.onClickUndo()
        }

        // finding the id of the brush dialog box and setting the onclicklistener for pop up
        val ib_brush_dialog: ImageButton = findViewById(R.id.ib_brush_dialog)
        ib_brush_dialog.setOnClickListener {
            showBrushSizeChooserDialog()
        }

        // Request to store button in gallery
        val ibGallery: ImageButton = findViewById(R.id.ib_gallery)
        ibGallery.setOnClickListener {
            requestStoragePermission()
        }

        // method to store Bitmap in file
        val ibSave: ImageButton = findViewById(R.id.ib_store)
        //set onclick listener
        ibSave.setOnClickListener{
            //check if permission is allowed
            if (isReadStorageAllowed()){
                //launch a coroutine block
                lifecycleScope.launch {
                    //reference the frame layout
                    val flDrawingView:FrameLayout = findViewById(R.id.fl_drawing_view_container)
                    //Save the image to the device
                    saveBitmapFile(getBitmapFromView(flDrawingView))
                }
            }
        }
    }

    // Method to show the size of the brush in a dialog box that pops up on the screen
    // This is a custom dialog example as well. Be sure to create the XML before
    private fun showBrushSizeChooserDialog() {
        val brushDialog = Dialog(this)
        brushDialog.setContentView(R.layout.dialog_brush_size)
        brushDialog.setTitle("Brush Size")
        val xSmallBtn: ImageButton = brushDialog.findViewById(R.id.ib_xsmall_brush)
        xSmallBtn.setOnClickListener {
            drawingView?.setSizeForBrush(5.toFloat())
            brushDialog.dismiss()
        }
        val smallBtn: ImageButton = brushDialog.findViewById(R.id.ib_small_brush)
        smallBtn.setOnClickListener {
            drawingView?.setSizeForBrush(10.toFloat())
            brushDialog.dismiss()
        }
        val mediumBtn: ImageButton = brushDialog.findViewById(R.id.ib_medium_brush)
        mediumBtn.setOnClickListener {
            drawingView?.setSizeForBrush(20.toFloat())
            brushDialog.dismiss()
        }
        val largeBtn: ImageButton = brushDialog.findViewById(R.id.ib_large_brush)
        largeBtn.setOnClickListener {
            drawingView?.setSizeForBrush(30.toFloat())
            brushDialog.dismiss()
        }
        val xLargeBtn: ImageButton = brushDialog.findViewById(R.id.ib_xlarge_brush)
        xLargeBtn.setOnClickListener {
            drawingView?.setSizeForBrush(40.toFloat())
            brushDialog.dismiss()
        }
        brushDialog.show()
    }

    // method to click paint colors when they are clicked
    fun paintClicked(view: View) {
        if(view != aImageButtonCurrentPaint) {
            val imageButton = view as ImageButton
            val colorTag = imageButton.tag.toString()
            drawingView?.setColor(colorTag)

            // set current button to be pressed
            imageButton.setImageDrawable(
                ContextCompat.getDrawable(this, R.drawable.pallet_pressed)
            )
            // making the old button unpressed and setting it to normal
            aImageButtonCurrentPaint?.setImageDrawable(
                ContextCompat.getDrawable(this, R.drawable.pallet_normal)
            )
            // making sure the current button is eqal to the view
            aImageButtonCurrentPaint = view
        }
    }

    // show the alert dialog box with a title and a message
    private fun showRationaleDialog(
        title: String,
        message: String
    ) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle(title)
            .setMessage(message)
            .setPositiveButton("Cancel") {dialog, _->
                dialog.dismiss()
            }
        builder.create().show()
    }

    private fun isReadStorageAllowed(): Boolean {
        val result = ContextCompat.checkSelfPermission(this,
        Manifest.permission.READ_EXTERNAL_STORAGE
        )
        return result == PackageManager.PERMISSION_GRANTED
    }


    // Method to request external storage
    private fun requestStoragePermission() {
        if(ActivityCompat.shouldShowRequestPermissionRationale(this,
            Manifest.permission.READ_EXTERNAL_STORAGE
            ))
        {
            showRationaleDialog("Kids Creation App", "Kids Creation App" +
            " needs to Access Your External Storage, otherwise" +
            " cannot access background image...")
        } else {
            requestPermission.launch(arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ))
        }
    }

    // want to be able to combine the colors, image, and layers into a bitmap..
    // creating a sandwich
    private fun getBitmapFromView(view: View) : Bitmap {
        val returnedBitmap = Bitmap.createBitmap(
            view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(returnedBitmap)
        val bgDrawable = view.background
        if(bgDrawable != null) {
            bgDrawable.draw(canvas)
        } else {
            canvas.drawColor(Color.WHITE)
        }
        view.draw(canvas)
        return returnedBitmap
    }

    // save the bitmap file if everything goes right
    // using Coroutines, and Output streams
    private suspend fun saveBitmapFile(aBitmap: Bitmap?): String {
        var result = ""
        withContext(Dispatchers.IO) {
            if(aBitmap != null) {
                try{
                    val bytes = ByteArrayOutputStream()
                    aBitmap.compress(Bitmap.CompressFormat.PNG, 90, bytes)

                    val f = File(externalCacheDir?.absoluteFile.toString() +
                    File.separator + "KidsCreation" +
                            System.currentTimeMillis() / 1000 + ".png"
                    )

                    val fo = FileOutputStream(f)
                    fo.write(bytes.toByteArray())
                    fo.close()

                    result = f.absolutePath

                    runOnUiThread {
                        if(result.isNotEmpty()) {
                            Toast.makeText(this@MainActivity,
                                "File saved successfully",
                            Toast.LENGTH_SHORT).show()
                            shareImage(result)
                        } else {
                            Toast.makeText(this@MainActivity,
                                "File not saved successfully",
                                Toast.LENGTH_SHORT).show()
                        }
                    }

                } catch (e: Exception) {
                    result = ""
                    e.printStackTrace()
                }
            }
        }
        return result
    }

    // method to share the image with a media service(email, social media, etc.)
    private fun shareImage(result: String) {

        MediaScannerConnection.scanFile(this, arrayOf(result), null) {
            path, uri ->
            val shareIntent = Intent()
            shareIntent.action = Intent.ACTION_SEND
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
            shareIntent.type = "image/png"
            startActivity(Intent.createChooser(shareIntent, "Share"))
        }
    }

}



