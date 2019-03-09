package com.iammert.library.readablebottombar

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout

class ReadableBottomBar @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : LinearLayout(context, attrs, defStyleAttr) {

    interface ItemSelectListener {
        fun onItemSelected(index: Int)
    }

    private val bottomBarItemConfigList: ArrayList<BottomBarItemConfig>

    private var tabInitialSelectedIndex = 0
    private var tabBackgroundColor: Int = Color.WHITE
    private var tabIndicatorColor: Int = Color.BLACK
    private var tabIndicatorHeight: Int = 10
    private var tabItemTextColor: Int = Color.BLACK
    private var tabItemTextSize: Float = 15f

    private var layoutWidth: Float = 0f
    private var layoutHeight: Float = 0f

    private var itemWidth: Float = 0f
    private var itemHeight: Float = 0f

    private var currentSelectedView: BottomBarItemView? = null

    private var indicatorView: View? = null

    private var itemSelectListener: ItemSelectListener? = null

    private var indicatorAnimator: ValueAnimator? = ValueAnimator.ofFloat(0f, 0f).apply {
        duration = 300L
        addUpdateListener { animation ->
            val marginLeft = animation.animatedValue as Float
            val marginParam: LinearLayout.LayoutParams = indicatorView?.layoutParams as LayoutParams
            marginParam.setMargins(marginLeft.toInt(), marginParam.topMargin, marginParam.rightMargin, marginParam.bottomMargin)
            indicatorView?.layoutParams = marginParam
        }
    }

    init {
        val typedArray = context.theme.obtainStyledAttributes(attrs, R.styleable.ReadableBottomBar, defStyleAttr, defStyleAttr)
        tabBackgroundColor = typedArray.getColor(R.styleable.ReadableBottomBar_rbb_backgroundColor, Color.WHITE)
        tabIndicatorColor = typedArray.getColor(R.styleable.ReadableBottomBar_rbb_indicatorColor, Color.BLACK)
        tabIndicatorHeight = typedArray.getDimensionPixelSize(R.styleable.ReadableBottomBar_rbb_indicatorHeight, 10)
        tabInitialSelectedIndex = typedArray.getInt(R.styleable.ReadableBottomBar_rbb_initialIndex, 0)
        tabItemTextColor = typedArray.getColor(R.styleable.ReadableBottomBar_rbb_textColor, Color.BLACK)
        tabItemTextSize = typedArray.getDimension(R.styleable.ReadableBottomBar_rbb_textSize, 15f)
        val tabXmlResource = typedArray?.getResourceId(R.styleable.ReadableBottomBar_rbb_tabs, 0)

        setBackgroundColor(tabBackgroundColor)
        orientation = VERTICAL
        bottomBarItemConfigList = ConfigurationXmlParser(context = context, xmlRes = tabXmlResource!!).parse()
        bottomBarItemConfigList[tabInitialSelectedIndex].selected = true

        typedArray.recycle()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        layoutWidth = w.toFloat()
        layoutHeight = h.toFloat()

        itemWidth = layoutWidth / bottomBarItemConfigList.size
        itemHeight = layoutHeight

        post {
            drawIndicator()
            drawBottomBarItems()
        }
    }

    fun setOnItemSelectListener(itemSelectListener: ItemSelectListener) {
        this.itemSelectListener = itemSelectListener
    }

    private fun drawBottomBarItems() {
        val itemContainerLayout = LinearLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(layoutWidth.toInt(), layoutHeight.toInt())
            orientation = HORIZONTAL
        }

        bottomBarItemConfigList.forEach { itemConfig ->
            val bottomBarItem = BottomBarItemView(context).apply {
                layoutParams = LinearLayout.LayoutParams(itemWidth.toInt(), itemHeight.toInt() - tabIndicatorHeight)
                setItemConfig(itemConfig)
                setTabColor(tabBackgroundColor)
                setTextSize(tabItemTextSize)
                setTextColor(tabItemTextColor)
                setOnClickListener {
                    if (it == currentSelectedView) {
                        return@setOnClickListener
                    }
                    animateIndicator(
                            previousItemIndex = currentSelectedView?.getItemIndex() ?: 0,
                            currentItemIndex = (it as BottomBarItemView).getItemIndex())
                    currentSelectedView?.deselect()
                    currentSelectedView = it
                    currentSelectedView?.select()
                    itemSelectListener?.onItemSelected(it.getItemIndex())
                }
            }

            itemContainerLayout.addView(bottomBarItem)

            if (itemConfig.selected) {
                currentSelectedView = bottomBarItem
            }
        }

        addView(itemContainerLayout)
    }

    private fun drawIndicator() {
        indicatorView = View(context).apply {
            val indicatorLayoutParams = LinearLayout.LayoutParams(itemWidth.toInt(), tabIndicatorHeight)
            indicatorLayoutParams.setMargins((tabInitialSelectedIndex * itemWidth).toInt(), 0, 0, 0)
            layoutParams = indicatorLayoutParams
            setBackgroundColor(tabIndicatorColor)
        }
        addView(indicatorView)
    }

    private fun animateIndicator(previousItemIndex: Int, currentItemIndex: Int) {
        val previousMargin: Float = previousItemIndex * itemWidth
        val currentMargin: Float = currentItemIndex * itemWidth
        indicatorAnimator?.setFloatValues(previousMargin, currentMargin)
        indicatorAnimator?.start()
    }
}