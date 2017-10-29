package com.gsbina.adaptiveiconsample

import android.content.Context
import android.graphics.Path
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.widget.ImageView


class AdaptiveIconView(
        context: Context,
        attrs: AttributeSet?
) : ImageView(context, attrs) {

    var fg: Drawable? = null
    var bg: Drawable? = null

    override fun setImageDrawable(drawable: Drawable?) {
        super.setImageDrawable(drawable)

        if (drawable is AdaptiveIconDrawable) {
            fg = drawable.foreground
            bg = drawable.background
        }
    }

    fun setForegroundVisibility(visible: Boolean) {
        if (visible) {
            if (fg?.alpha == 0) {
                fg?.alpha = 255
            }
        } else {
            if (fg?.alpha == 255) {
                fg?.alpha = 0
            }
        }
    }

    fun setBackgroundVisibility(visible: Boolean) {
        bg?.alpha = if (visible) 255 else 0
    }

    fun setMask(path: Path?) {
        if (drawable is AdaptiveIconDrawable) {
            val sMaskField = AdaptiveIconDrawable::class.java.getDeclaredField("sMask")
            sMaskField.isAccessible = true
            sMaskField.set(null, path)
        }
    }
}