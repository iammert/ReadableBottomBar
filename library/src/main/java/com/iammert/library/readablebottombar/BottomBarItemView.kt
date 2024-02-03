package com.iammert.library.readablebottombar

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.FrameLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import com.iammert.library.readablebottombar.ReadableBottomBar.Companion.ANIMATION_DURATION
import com.iammert.library.readablebottombar.ReadableBottomBar.ItemType


class BottomBarItemView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : FrameLayout(context, attrs, defStyleAttr) {

    private val layoutView = LayoutInflater.from(context).inflate(R.layout.layout_bottombar_item, this, true)
    private val textView = layoutView.findViewById<AppCompatTextView>(R.id.textView)
    private val imageView = layoutView.findViewById<AppCompatImageView>(R.id.imageView)

    private lateinit var animatedView: View

    var animationDuration: Long = ANIMATION_DURATION

    fun setText(text: String) {
        textView.text = text
        // The icon may be the only visible content, so describe the whole item.
        contentDescription = text
    }

    fun setIconDrawable(drawable: Drawable) {
        imageView.setImageDrawable(drawable)
    }

    fun setItemType(itemType: ItemType) {
        animatedView = when (itemType) {
            ItemType.Text -> textView
            ItemType.Icon -> imageView
        }
        animatedView.visibility = View.INVISIBLE
        animatedView.bringToFront()
    }

    fun setTabColor(tabColor: Int) {
        textView.setBackgroundColor(tabColor)
        imageView.setBackgroundColor(tabColor)
    }

    fun setTextSize(textSizePx: Float) {
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSizePx)
    }

    fun setTextColor(textColor: Int) {
        textView.setTextColor(textColor)
    }


    fun setIconColor(iconColor: Int) {
        imageView.setColorFilter(iconColor)
    }

    /**
     * Slides the active content in. Without [animate], or before the view has
     * been measured, the content is shown immediately.
     */
    fun select(animate: Boolean = true) {
        isSelected = true
        val wasHidden = animatedView.visibility != View.VISIBLE
        animatedView.animate().cancel()
        animatedView.visibility = View.VISIBLE
        if (!animate || height == 0) {
            animatedView.translationY = 0f
            return
        }
        if (wasHidden) {
            animatedView.translationY = height.toFloat()
        }
        animatedView.animate()
                .translationY(0f)
                .setDuration(animationDuration)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .start()
    }

    fun deselect(animate: Boolean = true) {
        isSelected = false
        animatedView.animate().cancel()
        if (!animate || height == 0) {
            animatedView.visibility = View.INVISIBLE
            animatedView.translationY = height.toFloat()
            return
        }
        animatedView.animate()
                .translationY(height.toFloat())
                .setDuration(animationDuration)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .withEndAction { animatedView.visibility = View.INVISIBLE }
                .start()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (animatedView.visibility != View.VISIBLE) {
            animatedView.translationY = h.toFloat()
        }
    }

}
