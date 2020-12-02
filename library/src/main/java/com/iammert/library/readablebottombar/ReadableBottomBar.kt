package com.iammert.library.readablebottombar

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.support.annotation.XmlRes
import android.util.AttributeSet
import android.view.View
import android.view.ViewTreeObserver
import android.widget.LinearLayout

class ReadableBottomBar @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : LinearLayout(context, attrs, defStyleAttr) {

    interface ItemSelectListener {
        fun onItemSelected(index: Int)
    }

    enum class ItemType(val value: Int) {
        Text(0),
        Icon(1);

        companion object {

            fun getType(value: Int) = values().firstOrNull { it.value == value } ?: Icon

        }
    }

    private var bottomBarItemList: List<BottomBarItem>

    private var tabInitialSelectedIndex = 0
    private var tabBackgroundColor: Int = Color.WHITE
    private var tabIndicatorColor: Int = Color.BLACK
    private var tabIndicatorHeight: Int = 10

    private var textSize: Float = 15f
    private var textColor: Int = Color.BLACK
    private var iconColor: Int = Color.BLACK
    private var activeItemType: ItemType = ItemType.Icon

    private var layoutWidth: Float = 0f
    private var layoutHeight: Float = 0f

    private var itemWidth: Float = 0f
    private var itemHeight: Float = 0f

    private var currentSelectedView: BottomBarItemView? = null

    private var indicatorView: View? = null

    private var itemSelectListener: ItemSelectListener? = null

    private var pendingSelectable: Int? = null

    private var indicatorAnimator: ValueAnimator? = ValueAnimator.ofFloat(0f, 0f).apply {
        duration = ANIMATION_DURATION
        addUpdateListener { animation ->
            val marginLeft = animation.animatedValue as Float
            val marginParam: LayoutParams = indicatorView?.layoutParams as LayoutParams
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

        textSize = typedArray.getDimension(R.styleable.ReadableBottomBar_rbb_textSize, 15f)
        textColor = typedArray.getColor(R.styleable.ReadableBottomBar_rbb_textColor, Color.BLACK)
        iconColor = typedArray.getColor(R.styleable.ReadableBottomBar_rbb_iconColor, Color.BLACK)
        activeItemType = ItemType.getType(typedArray.getInt(R.styleable.ReadableBottomBar_rbb_activeItemType, ItemType.Icon.value))

        val tabXmlResource = typedArray?.getResourceId(R.styleable.ReadableBottomBar_rbb_tabs, 0)
        val bottomBarItemConfigList = ConfigurationXmlParser(context = context, xmlRes = tabXmlResource!!).parse()

        setBackgroundColor(tabBackgroundColor)
        orientation = VERTICAL

        bottomBarItemList = bottomBarItemConfigList.map { config ->
            BottomBarItem(
                    config.index,
                    config.text,
                    textSize,
                    textColor,
                    iconColor,
                    config.drawable,
                    activeItemType
            )
        }

        typedArray.recycle()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        layoutWidth = w.toFloat()
        layoutHeight = h.toFloat()

        itemWidth = layoutWidth / bottomBarItemList.size
        itemHeight = layoutHeight

        post {
            drawIndicator()
            drawBottomBarItems()
        }
    }

    fun setOnItemSelectListener(itemSelectListener: ItemSelectListener) {
        this.itemSelectListener = itemSelectListener
    }

    fun selectItem(index: Int) {
        if (index < 0 || index > bottomBarItemList.size) {
            throw IllegalArgumentException("Index should be in range of 0-${bottomBarItemList.size}")
        }

        val item = bottomBarItemList[index]
        if (childCount > 0) {
            for (i in 0 until childCount) {
                if (TAG_CONTAINER == getChildAt(i).tag) {
                    val selectedItemView = ((getChildAt(i) as LinearLayout).getChildAt(index) as BottomBarItemView)
                    if (selectedItemView != currentSelectedView) {
                        onSelected(item.index, selectedItemView)
                    }
                }
            }
        } else {
            pendingSelectable = index
        }
    }


    private fun drawBottomBarItems() {
        val itemContainerLayout = LinearLayout(context).apply {
            layoutParams = LayoutParams(layoutWidth.toInt(), layoutHeight.toInt())
            orientation = HORIZONTAL
            tag = TAG_CONTAINER
        }

        bottomBarItemList.forEach { item ->
            val bottomBarItem = BottomBarItemView(context).apply {
                layoutParams = LayoutParams(itemWidth.toInt(), itemHeight.toInt() - tabIndicatorHeight)

                setText(item.text)
                setItemType(item.type)
                setIconDrawable(item.drawable)
                setIconColor(item.iconColor)
                setTextSize(item.textSize)
                setTextColor(item.textColor)
                setTabColor(tabBackgroundColor)

                setOnClickListener {

                    if (it == currentSelectedView) {
                        return@setOnClickListener
                    }

                    onSelected(item.index, this)
                }
            }

            itemContainerLayout.addView(bottomBarItem)

            if (item.index == tabInitialSelectedIndex) {
                currentSelectedView = bottomBarItem

                val listener = object : ViewTreeObserver.OnGlobalLayoutListener {

                    override fun onGlobalLayout() {
                        bottomBarItem.select()
                        bottomBarItem.viewTreeObserver.removeGlobalOnLayoutListener(this)

                        pendingSelectable?.let {
                            selectItem(it)
                            pendingSelectable = null
                        }
                    }
                }

                bottomBarItem.viewTreeObserver.addOnGlobalLayoutListener(listener)
            }

        }

        addView(itemContainerLayout)
    }

    private fun drawIndicator() {
        indicatorView = View(context).apply {
            val indicatorLayoutParams = LayoutParams(itemWidth.toInt(), tabIndicatorHeight)
            indicatorLayoutParams.setMargins((tabInitialSelectedIndex * itemWidth).toInt(), 0, 0, 0)
            layoutParams = indicatorLayoutParams
            setBackgroundColor(tabIndicatorColor)
        }
        addView(indicatorView)
    }

    private fun animateIndicator(currentItemIndex: Int) {
        val previousMargin: Float = (indicatorView?.layoutParams as? LayoutParams)?.leftMargin?.toFloat()
                ?: 0F
        val currentMargin: Float = currentItemIndex * itemWidth
        indicatorAnimator?.setFloatValues(previousMargin, currentMargin)
        indicatorAnimator?.start()
    }

    private fun onSelected(index: Int, bottomBarItemView: BottomBarItemView) {
        animateIndicator(index)

        currentSelectedView?.deselect()
        currentSelectedView = bottomBarItemView
        currentSelectedView?.select()
        itemSelectListener?.onItemSelected(index)
    }


    public fun setTabItems(items : List<BottomBarItem>) {
        bottomBarItemList = items
        post {
            drawIndicator()
            drawBottomBarItems()
        }
    }

    /*public fun setTabItems(@XmlRes tabResource: Int) {
        val bottomBarItemConfigList = ConfigurationXmlParser(context = context,
                xmlRes = tabResource).parse()

        bottomBarItemList = bottomBarItemConfigList.map { config ->
            BottomBarItem(
                    config.index,
                    config.text,
                    textSize,
                    textColor,
                    iconColor,
                    config.drawable,
                    activeItemType
            )
        }
        post {
            drawIndicator()
            drawBottomBarItems()
        }
    }*/


    companion object {

        const val ANIMATION_DURATION = 300L

        const val TAG_CONTAINER = "TAG_CONTAINER"

    }

}