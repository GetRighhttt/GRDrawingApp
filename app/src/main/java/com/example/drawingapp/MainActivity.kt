package com.example.drawingapp

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Color.WHITE
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.get
import com.example.drawingapp.view.DrawingView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception

class MainActivity : AppCompatActivity() {

    private var drawingView: DrawingView? = null
    private var aImageButtonCurrentPaint: ImageButton? = null

    // assigning our intent to go to the media of our device
    // want to be able to replace our imageView with an image in the gallery
    val openGalleryLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            result ->
            if(result.resultCode == RESULT_OK && result.data != null) {
                val imageBackground: ImageView = findViewById(R.id.iv_background)
                // gives us the location of the data
                imageBackground.setImageURI(result.data?.data)
            }
        }

    // request permission check
    val requestPermission: ActivityResultLauncher<Array<String>> =
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

//        // method to store Bitmap in file
//        val ibSave: ImageButton = findViewById(R.id.ib_store)
//        ibSave.setOnClickListener {
//            if(isReadStorageAllowed()) {
//                {
//                    val flDrawingView: FrameLayout =
//                        findViewById(R.id.fl_drawing_view_container)
//                    suspend { saveBitmapFile(getBitmapFromView(flDrawingView)) }
//                }
//            }
//        }
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
    // useing Coroutines, and Outputstreams
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
}


    /**
     * *********************************************************************************
     * Below are Methods for Dialog boxes for future reference
     * *********************************************************************************
     */

//    // Method to create an alert Dialog box
//    private fun alertDialogFunction() {
//        // Use the Builder class to assign to a variable for construction
//        val builder = AlertDialog.Builder(this)
//        // sets the title for the Alert Dialog box
//        builder.setTitle("Alert")
//        // sets the message
//        builder.setMessage("This is Alert Dialog.")
//        // sets the icon for the dialog
//        builder.setIcon(android.R.drawable.ic_dialog_alert)
//
//        // performing positive action
//        builder.setPositiveButton("Yes") {
//            dialogInterface, which ->
//            Toast.makeText(applicationContext,
//                "clicked Yes", Toast.LENGTH_LONG).show()
//            dialogInterface.dismiss() // dialog will be dismissed
//        }
//
//        // performing cancel action
//        builder.setNeutralButton("Cancel"){dialogInterface, which ->
//            Toast.makeText(
//                applicationContext,
//                "click cancel\n operation cancel", Toast.LENGTH_LONG).show()
//            dialogInterface.dismiss()
//        }
//
//        // performing negative action
//        builder.setNegativeButton("No") {dialogInterface, which ->
//            Toast.makeText(applicationContext,
//                "clicked No", Toast.LENGTH_LONG).show()
//            dialogInterface.dismiss()
//        }
//
//        // Once we finish setting up dialog, we Create the AlertDialog
//        val alertDialog: AlertDialog = builder.create()
//        // set Other dialog properties
//        alertDialog.setCancelable(false) // won't allow user to cancel
//        alertDialog.show() // show the dialog to the UI
//    }

//    // Custom Progress Bar Method
//    private fun customProgressDialogFunction() {
//        val customProgress = Dialog(this)
//        // Set the screen content from a layout resource
//        // The resource will be inflated from the xml
//        customProgress.setContentView(R.id.custom_progress)
//
//        customProgress.show()
//    }



    /**
     * snackbar creation
     * image_button.setOnClickListener {view ->
     * Snackbar.make(view, "Some Text", Snackbar)}
     */



