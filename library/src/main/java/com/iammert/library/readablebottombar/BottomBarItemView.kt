package com.iammert.library.readablebottombar

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.TypedValue
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.FrameLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.TooltipCompat
import androidx.annotation.ColorInt
import androidx.annotation.StyleRes
import androidx.core.content.ContextCompat
import androidx.core.widget.TextViewCompat
import com.iammert.library.readablebottombar.ReadableBottomBar.Companion.ANIMATION_DURATION
import com.iammert.library.readablebottombar.ReadableBottomBar.ItemType
import kotlin.math.abs


internal class BottomBarItemView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : FrameLayout(context, attrs, defStyleAttr) {

    private val layoutView = LayoutInflater.from(context).inflate(R.layout.layout_bottombar_item, this, true)
    private val textView = layoutView.findViewById<AppCompatTextView>(R.id.textView)
    private val imageView = layoutView.findViewById<AppCompatImageView>(R.id.imageView)

    private lateinit var animatedView: View

    private var title: String = ""

    private val badgeView = AppCompatTextView(context).apply {
        gravity = Gravity.CENTER
        includeFontPadding = false
        setTextSize(TypedValue.COMPLEX_UNIT_SP, BADGE_TEXT_SIZE_SP)
        importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_NO
        visibility = View.GONE
    }

    var animationDuration: Long = ANIMATION_DURATION

    // FrameLayout has supported a foreground drawable since API 1, lint only
    // knows about the View.setForeground overload added in API 23.
    @SuppressLint("NewApi")
    private fun applyTouchFeedback() {
        val outValue = TypedValue()
        if (context.theme.resolveAttribute(android.R.attr.selectableItemBackground, outValue, true)) {
            // Drawn as foreground so the opaque item backgrounds do not hide it.
            foreground = ContextCompat.getDrawable(context, outValue.resourceId)
        }
    }

    init {
        applyTouchFeedback()
        addView(badgeView, LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, Gravity.TOP or Gravity.CENTER_HORIZONTAL))
    }

    fun setText(text: String) {
        title = text
        textView.text = text
        // The icon may be the only visible content, so describe the whole item
        // and show the title on long press.
        contentDescription = text
        TooltipCompat.setTooltipText(this, text)
    }

    /**
     * Shows a badge with [count], a dot for [BADGE_DOT], or hides it for 0.
     */
    fun setBadge(count: Int, @ColorInt badgeColor: Int, @ColorInt badgeTextColor: Int) {
        if (count == 0) {
            badgeView.visibility = View.GONE
            contentDescription = title
            return
        }
        val density = resources.displayMetrics.density
        val isDot = count == BADGE_DOT
        badgeView.text = when {
            isDot -> ""
            count > MAX_BADGE_COUNT -> resources.getString(R.string.rbb_badge_overflow)
            else -> count.toString()
        }
        badgeView.setTextColor(badgeTextColor)
        badgeView.background = GradientDrawable().apply {
            setColor(badgeColor)
            cornerRadius = BADGE_SIZE_DP * density
        }
        val size = ((if (isDot) BADGE_DOT_SIZE_DP else BADGE_SIZE_DP) * density).toInt()
        badgeView.minWidth = size
        badgeView.minHeight = size
        val horizontalPadding = if (isDot) 0 else (BADGE_PADDING_DP * density).toInt()
        badgeView.setPadding(horizontalPadding, 0, horizontalPadding, 0)
        (badgeView.layoutParams as LayoutParams).topMargin = (BADGE_MARGIN_DP * density).toInt()
        // Sit next to the top end corner of a centered 24dp icon.
        val offset = BADGE_OFFSET_DP * density
        badgeView.translationX = if (layoutDirection == View.LAYOUT_DIRECTION_RTL) -offset else offset
        badgeView.visibility = View.VISIBLE
        badgeView.bringToFront()

        contentDescription = if (isDot) {
            resources.getString(R.string.rbb_badge_new, title)
        } else {
            resources.getQuantityString(R.plurals.rbb_badge_count, count, title, count)
        }
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

    fun setTextAppearance(@StyleRes textAppearance: Int) {
        if (textAppearance != 0) {
            TextViewCompat.setTextAppearance(textView, textAppearance)
        }
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

    override fun onRtlPropertiesChanged(layoutDirection: Int) {
        super.onRtlPropertiesChanged(layoutDirection)
        val offset = abs(badgeView.translationX)
        badgeView.translationX = if (layoutDirection == View.LAYOUT_DIRECTION_RTL) -offset else offset
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (animatedView.visibility != View.VISIBLE) {
            animatedView.translationY = h.toFloat()
        }
    }

    companion object {
        const val BADGE_DOT = -1

        private const val MAX_BADGE_COUNT = 99
        private const val BADGE_TEXT_SIZE_SP = 10f
        private const val BADGE_SIZE_DP = 16f
        private const val BADGE_DOT_SIZE_DP = 8f
        private const val BADGE_PADDING_DP = 4f
        private const val BADGE_MARGIN_DP = 6f
        private const val BADGE_OFFSET_DP = 14f
    }
}
