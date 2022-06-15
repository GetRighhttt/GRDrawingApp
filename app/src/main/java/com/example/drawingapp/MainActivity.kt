package com.example.drawingapp

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
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
}