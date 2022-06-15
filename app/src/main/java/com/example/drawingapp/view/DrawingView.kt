package com.example.drawingapp.view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import androidx.core.graphics.minus

/**
 * This is where the drawing of our app will be able to take place.
 * In order for us to draw something, the class must be of type "View"
 * We pass in two parameters, Inherit the View class, and we add a primary constructor
 * Context is for the context of what is being added
 * Attributes are the attributes that we need to use.
 * In order to be able to draw, you need to understand colors, paints, etc.
 */

// To draw something, we need to do it on the type "View
class DrawingView(context: Context, attrs: AttributeSet): View(context, attrs){

    //Creates variables that we are going to use for our drawing app
    private var aDrawPath: CustomPath? = null
    private var aCanvasBitmap: Bitmap? = null
    private var aDrawPaint: Paint? = null
    private var aCanvasPaint: Paint? = null
    private var aBrushSize: Float = 0.toFloat()
    private var color = Color.BLACK
    private var canvas: Canvas? = null

    // variable declared for line persistence; lines stay on screen
    private var aPaths = ArrayList<CustomPath>()
    // list of custom paths to undo or store the paths
    private var aUndoPaths = ArrayList<CustomPath>()

    // Initializes the setup as soon as the class is called
    init {
        setupDrawingApp()
    }

    // Method to undo the path
    // removes the path length - 1
    fun onClickUndo() {
        if(aPaths.size > 0) {
            aUndoPaths.add(aPaths.removeAt(aPaths.size - 1))
            invalidate()
        }
    }

    // method to put into our init statement that assigns majority of variables
    private fun setupDrawingApp() {
        aDrawPaint = Paint()
        aDrawPath = CustomPath(color, aBrushSize)
        aDrawPaint!!.color = color
        aDrawPaint!!.style = Paint.Style.STROKE
        aDrawPaint!!.strokeJoin = Paint.Join.ROUND
        aDrawPaint!!.strokeCap = Paint.Cap.ROUND
        aCanvasPaint = Paint(Paint.DITHER_FLAG)
        //aBrushSize = 20.toFloat()
    }

    // onSizeChanged() called in a layout when the sze of the view has changed
    // When size of the screen has changed, we want to create & display the bitmap
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        aCanvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        canvas = Canvas(aCanvasBitmap!!)
    }

    // Allows us to draw something on our canvas
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawBitmap(aCanvasBitmap!!, 0f, 0f, aCanvasPaint)

        // path used for line persistence on the screen
        for(path in aPaths) {
            aDrawPaint!!.strokeWidth = path.brushThickness
            aDrawPaint!!.color = path.color
            canvas.drawPath(path, aDrawPaint!!)

        }

        //  For drawing something in general on screen
        if(!aDrawPath!!.isEmpty) {
            aDrawPaint!!.strokeWidth = aDrawPath!!.brushThickness
            aDrawPaint!!.color = aDrawPath!!.color
            canvas.drawPath(aDrawPath!!, aDrawPaint!!)
        }
    }

    // We want to draw on Touch. event is something that happens
    // per each Action, we need the Draw path to move, and assign color and thickness
    // of the brush size
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val touchX = event?.x
        val touchY = event?.y

        when(event?.action) {
            MotionEvent.ACTION_DOWN -> {
                aDrawPath!!.color = color
                aDrawPath!!.brushThickness = aBrushSize

                aDrawPath!!.reset()
                if(touchX != null) {
                    if(touchY != null) {
                        aDrawPath!!.moveTo(touchX,touchY)
                    }
                }
            } MotionEvent.ACTION_MOVE -> {
                if(touchX != null) {
                    if(touchY != null) {
                        aDrawPath!!.lineTo(touchX,touchY)
                    }
                }
            } MotionEvent.ACTION_UP -> {
                aPaths.add(aDrawPath!!)
                aDrawPath = CustomPath(color, aBrushSize)
            }
            else -> return false
        }
        invalidate()
        return true
        return super.onTouchEvent(event)
    }

    // set the brush size on the screen, public method that we can use in our Main
    // must take into account different screen sizes with metrics and dimensions
    fun setSizeForBrush(newSize: Float) {
        aBrushSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            newSize, resources.displayMetrics)
        aDrawPaint!!.strokeWidth = aBrushSize
    }

    fun setColor(newColor: String) {
        color = Color.parseColor(newColor)
        aDrawPaint!!.color = color
    }

    /**
     * The Path class encapsulates compound geometric paths consisting of straight line segments,
     * quadratic curves, cubic curves.
     * It can be drawn with canvas.draw(path, paint), either filled or stroked based on
     * point style, or it can be used for clipping or drawing text on a path.
     * The internal inner class means this class can only be used in DrawingView class we
     * created.
     */

    internal inner class CustomPath(var color: Int, var brushThickness: Float): Path() {
    }


}