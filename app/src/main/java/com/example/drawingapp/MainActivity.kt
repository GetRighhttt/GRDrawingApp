package com.example.drawingapp

import android.app.AlertDialog
import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.get
import com.example.drawingapp.view.DrawingView

class MainActivity : AppCompatActivity() {

    private var drawingView: DrawingView? = null
    private var aImageButtonCurrentPaint: ImageButton? = null

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

        // finding the id of the brush dialog box and setting the onclicklistener for pop up
        val ib_brush_dialog: ImageButton = findViewById(R.id.ib_brush_dialog)
        ib_brush_dialog.setOnClickListener {
            showBrushSizeChooserDialog()
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

}

