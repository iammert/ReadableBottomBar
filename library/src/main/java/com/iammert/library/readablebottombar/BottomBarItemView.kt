package com.iammert.library.readablebottombar

import android.content.Context
import android.support.v7.widget.AppCompatImageView
import android.support.v7.widget.AppCompatTextView
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Animation
import android.view.animation.TranslateAnimation


class BottomBarItemView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : FrameLayout(context, attrs, defStyleAttr) {

    private val layoutView = LayoutInflater.from(context).inflate(R.layout.layout_bottombar_item, this, true)
    private val textView = layoutView.findViewById<AppCompatTextView>(R.id.textView)
    private val imageView = layoutView.findViewById<AppCompatImageView>(R.id.imageView)
    private val layoutImageView = layoutView.findViewById<FrameLayout>(R.id.layoutImageView)

    private var translateUpAnimation: TranslateAnimation? = null
    private var translateDownAnimation: TranslateAnimation? = null

    private lateinit var bottomBarItemConfig: BottomBarItemConfig

    init {
        setOnClickListener {
            if (bottomBarItemConfig.selected) {
                return@setOnClickListener
            }
            select()
            bottomBarItemConfig.selected = true
        }
    }

    fun setItemConfig(bottomBarItemConfig: BottomBarItemConfig) {
        this.bottomBarItemConfig = bottomBarItemConfig
        textView.text = bottomBarItemConfig.text
        imageView.setImageDrawable(bottomBarItemConfig.drawable)
        layoutImageView.visibility = if (bottomBarItemConfig.selected) View.VISIBLE else View.INVISIBLE
    }

    fun setTabColor(tabColor: Int) {
        layoutImageView.setBackgroundColor(tabColor)
    }

    fun setTextSize(textSize: Float) {
        textView.textSize = textSize
    }

    fun setTextColor(textColor: Int) {
        textView.setTextColor(textColor)
    }

    fun getItemIndex(): Int = bottomBarItemConfig.index

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        initializeAnimations()
    }

    fun select() {
        layoutImageView.startAnimation(translateUpAnimation)
    }

    fun deselect() {
        layoutImageView.startAnimation(translateDownAnimation)
    }

    private fun initializeAnimations() {
        translateUpAnimation = TranslateAnimation(0f, 0f, height.toFloat(), 0f).apply {
            duration = ANIMATION_DURATION
            interpolator = AccelerateDecelerateInterpolator()
            setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationRepeat(animation: Animation?) {
                }

                override fun onAnimationEnd(animation: Animation?) {
                }

                override fun onAnimationStart(animation: Animation?) {
                    layoutImageView.visibility = View.VISIBLE
                }

            })
        }

        translateDownAnimation = TranslateAnimation(0f, 0f, 0f, height.toFloat()).apply {
            duration = ANIMATION_DURATION
            interpolator = AccelerateDecelerateInterpolator()
            setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationRepeat(animation: Animation?) {
                }

                override fun onAnimationEnd(animation: Animation?) {
                    layoutImageView.visibility = View.INVISIBLE
                }

                override fun onAnimationStart(animation: Animation?) {
                }
            })
        }
    }

    companion object {

        private const val ANIMATION_DURATION = 300L
    }
}