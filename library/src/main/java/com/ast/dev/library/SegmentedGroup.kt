package com.ast.dev.library

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.StateListDrawable
import android.graphics.drawable.TransitionDrawable
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.util.StateSet
import android.util.TypedValue
import android.view.View
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup

/**
 * Custom view for generating similar look as a UISegmentedControl in iOS
 * Created by Axel Trajano on 07/04/2019.
 */
class SegmentedGroup(context: Context, attrs: AttributeSet) : RadioGroup(context, attrs) {

    companion object {
        private const val TINT = Color.WHITE
        private const val SELECTED_TINT = Color.BLUE
        private const val CORNER_RADIUS = 10f
        private const val BORDER_WIDTH = 3
        private const val SELECTED_TEXT_COLOR = Color.WHITE
    }

    // public properties
    var cornerRadius = CORNER_RADIUS
        set(value) {
            field = value
            updateBackground()
        }
    var borderWidth = BORDER_WIDTH
        set(value) {
            field = value
            updateBackground()
        }
    var borderTint = SELECTED_TINT
        set(value) {
            field = value
            updateBackground()
        }
    var tint = TINT
        set(value) {
            field = value
            updateBackground()
        }
    var selectedTint = SELECTED_TINT
        set(value) {
            field = value
            updateBackground()
        }
    var textTint = SELECTED_TINT
        set(value) {
            field = value
            updateBackground()
        }
    var selectedTextTint = SELECTED_TEXT_COLOR
        set(value) {
            field = value
            updateBackground()
        }
    var onSegmentSelected: OnSegmentSelected? = null
    var selectedSegmentIndex = 0

    // private properties
    private var lastCheckedId = 0
    private var currentChildCount = -1
    private var currentChildIndex = -1
    private var currentRadii = floatArrayOf()
    private var drawableMap = HashMap<Int, TransitionDrawable>()
    private var flat = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0.1f, resources.displayMetrics)
    private var leftRadii = floatArrayOf(cornerRadius, cornerRadius, flat, flat, flat, flat, cornerRadius, cornerRadius)
    private var rightRadii = floatArrayOf(flat, flat, cornerRadius, cornerRadius, cornerRadius, cornerRadius, flat, flat)
    private var middleRadii = floatArrayOf(flat, flat, flat, flat, flat, flat, flat, flat)
    private var topRadii = floatArrayOf(cornerRadius, cornerRadius, cornerRadius, cornerRadius, flat, flat, flat, flat)
    private var bottomRadii = floatArrayOf(flat, flat, flat, flat, cornerRadius, cornerRadius, cornerRadius, cornerRadius)
    private var defaultRadii = floatArrayOf(cornerRadius, cornerRadius, cornerRadius, cornerRadius,
                                            cornerRadius, cornerRadius, cornerRadius, cornerRadius)


    init {
        setupAttributes(attrs)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        updateBackground()
    }

    private fun setupAttributes(attrs: AttributeSet) {
        // Obtain a typed array of attributes
        val typedArray = context.theme.obtainStyledAttributes(attrs, R.styleable.SegmentedGroup, 0, 0)

        try {
            // Extract custom attributes into member variables
            tint = typedArray.getColor(R.styleable.SegmentedGroup_tint, TINT)
            selectedTint = typedArray.getColor(R.styleable.SegmentedGroup_selectedTint, SELECTED_TINT)
            textTint = typedArray.getColor(R.styleable.SegmentedGroup_textColor, selectedTint)
            selectedTextTint = typedArray.getColor(R.styleable.SegmentedGroup_selectedTextColor, SELECTED_TEXT_COLOR)
            cornerRadius = typedArray.getDimension(R.styleable.SegmentedGroup_cornerRadius, CORNER_RADIUS)
            borderWidth = typedArray.getDimension(R.styleable.SegmentedGroup_borderWidth, BORDER_WIDTH.toFloat()).toInt()
            borderTint = typedArray.getColor(R.styleable.SegmentedGroup_borderTint, selectedTint)
        } finally {
            // Typed array objects are shared and must be recycled
            typedArray.recycle()
        }
    }

    private fun updateBackground() {
        drawableMap.clear()
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            updateBackground(child)
            val initParams = child.layoutParams as LayoutParams
            val params = LayoutParams(initParams.width, initParams.height, initParams.weight)
            child.layoutParams = params
        }
    }

    private fun updateBackground(child: View) {
        val colorStateList = ColorStateList(
            arrayOf(intArrayOf(-android.R.attr.state_checked), intArrayOf(android.R.attr.state_checked)),
            intArrayOf(textTint, selectedTextTint))

        if (child is RadioButton) {
            child.setTextColor(colorStateList)
        }

        val selectedDrawable = ContextCompat.getDrawable(context, R.drawable.segment_button_selected)?.mutate() as GradientDrawable
        val unselectedDrawable = ContextCompat.getDrawable(context, R.drawable.segment_button_unselected)?.mutate() as GradientDrawable

        val cornerRadii = getChildRadii(child)

        // apply tints
        selectedDrawable.setColor(selectedTint)
        selectedDrawable.setStroke(borderWidth, borderTint)
        unselectedDrawable.setColor(tint)
        unselectedDrawable.setStroke(borderWidth, borderTint)

        // apply corner radius
        selectedDrawable.cornerRadii = cornerRadii
        unselectedDrawable.cornerRadii = cornerRadii

        // setup pressed drawable
        val maskDrawable = ContextCompat.getDrawable(context, R.drawable.segment_button_unselected)?.mutate() as GradientDrawable
        maskDrawable.setColor(tint)
        maskDrawable.setStroke(borderWidth, borderTint)
        maskDrawable.cornerRadii = cornerRadii
        maskDrawable.setColor(Color.argb(50, Color.red(tint), Color.green(tint), Color.blue(tint)))
        val pressedDrawable = LayerDrawable(arrayOf(unselectedDrawable, maskDrawable))

        val drawables = arrayOf(unselectedDrawable, selectedDrawable)
        val transitionDrawable = TransitionDrawable(drawables)
        if (child is RadioButton && child.isChecked) {
            transitionDrawable.reverseTransition(0)
            lastCheckedId = child.id
            selectedSegmentIndex = indexOfChild(child)
        }

        // apply states
        val stateListDrawable = StateListDrawable()
        stateListDrawable.addState(intArrayOf(-android.R.attr.state_checked, android.R.attr.state_pressed), pressedDrawable)
        stateListDrawable.addState(StateSet.WILD_CARD, transitionDrawable)
        child.background = stateListDrawable

        // add to drawable map
        drawableMap[child.id] = transitionDrawable

        // handle selection changes
        super.setOnCheckedChangeListener { group, checkId ->
            val current = drawableMap[checkId]
            current?.reverseTransition(200)
            if (lastCheckedId != 0) {
                drawableMap[lastCheckedId]?.reverseTransition(200)
                val lastChecked = findViewById<RadioButton>(lastCheckedId)
                lastChecked.isChecked = false
            }
            lastCheckedId = checkId

            // callback
            val checked = findViewById<RadioButton>(checkId)
            selectedSegmentIndex = group.indexOfChild(checked)
            onSegmentSelected?.invoke(selectedSegmentIndex)
        }
    }

    private fun getChildRadii(child: View) : FloatArray {
        val index = indexOfChild(child)
        if (currentChildCount == childCount && currentChildIndex == index) return currentRadii

        currentChildCount = childCount
        currentChildIndex = index

        currentRadii = if (childCount == 1) defaultRadii
        else if (index == 0) if (orientation == LinearLayout.HORIZONTAL) leftRadii else topRadii
        else if (index == childCount - 1) if (orientation == LinearLayout.HORIZONTAL) rightRadii else bottomRadii
        else middleRadii

        return currentRadii
    }

    override fun onViewRemoved(child: View?) {
        super.onViewRemoved(child)
        drawableMap.remove(child?.id)
    }

    override fun addView(child: View?) {
        super.addView(child)
        updateBackground()
    }
}

typealias OnSegmentSelected = (index: Int) -> Unit