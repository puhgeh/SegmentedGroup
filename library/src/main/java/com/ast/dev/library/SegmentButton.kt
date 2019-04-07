package com.ast.dev.library

import android.content.Context
import android.util.AttributeSet
import android.widget.RadioButton
import android.widget.RadioGroup

/**
 * Custom view element for the {@link SegmentedGroup}
 * Created by Axel Trajano on 07/04/2019.
 */
class SegmentButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = R.style.SegmentButton
) : RadioButton(context, attrs, defStyleAttr, defStyleRes) {

    constructor(context: Context,
                title: String,
                weight: Float = 1f,
                attrs: AttributeSet? = null,
                defStyleAttr: Int = 0,
                defStyleRes: Int = R.style.SegmentButton
                ) : this(context, attrs, defStyleAttr, defStyleRes) {

        val params = RadioGroup.LayoutParams(0, RadioGroup.LayoutParams.WRAP_CONTENT, weight)
        layoutParams = params
        text = title
    }
}