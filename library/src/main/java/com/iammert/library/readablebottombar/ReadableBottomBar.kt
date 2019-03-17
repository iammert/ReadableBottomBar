package com.iammert.library.readablebottombar

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import android.view.ViewTreeObserver
import android.widget.LinearLayout
import java.util.ArrayList

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

    private val bottomBarItemList: List<BottomBarItem>
    private val bottomBarViewItemList  =  ArrayList<BottomBarItemView>()

    private var tabInitialSelectedIndex = 0
    private var tabBackgroundColor: Int = Color.WHITE
    private var tabIndicatorColor: Int = Color.BLACK
    private var tabIndicatorHeight: Int = 10

    private var layoutWidth: Float = 0f
    private var layoutHeight: Float = 0f

    private var itemWidth: Float = 0f
    private var itemHeight: Float = 0f

    private var currentSelectedView: BottomBarItemView? = null

    private var indicatorView: View? = null

    private var itemSelectListener: ItemSelectListener? = null

    private var indicatorAnimator: ValueAnimator? = ValueAnimator.ofFloat(0f, 0f).apply {
        duration = ANIMATION_DURATION
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

        val textSize = typedArray.getDimension(R.styleable.ReadableBottomBar_rbb_textSize, 15f)
        val textColor = typedArray.getColor(R.styleable.ReadableBottomBar_rbb_textColor, Color.BLACK)
        val activeItemType = ItemType.getType(typedArray.getInt(R.styleable.ReadableBottomBar_rbb_activeItemType, ItemType.Icon.value))

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

    fun setSelectedItem(position : Int){
        getChildViewAt(position)?.callOnClick()
    }

    fun getChildViewAt(position : Int) = bottomBarViewItemList.getOrNull(position)

    private fun drawBottomBarItems() {
        val itemContainerLayout = LinearLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(layoutWidth.toInt(), layoutHeight.toInt())
            orientation = HORIZONTAL
        }

        bottomBarItemList.forEach { item ->
            val bottomBarItem = BottomBarItemView(context).apply {
                layoutParams = LinearLayout.LayoutParams(itemWidth.toInt(), itemHeight.toInt() - tabIndicatorHeight)

                setText(item.text)
                setItemType(item.type)
                setIconDrawable(item.drawable)

                setTextSize(item.textSize)
                setTextColor(item.textColor)
                setTabColor(tabBackgroundColor)

                setOnClickListener {

                    if (it == currentSelectedView) {
                        return@setOnClickListener
                    }

                    animateIndicator(item.index)

                    currentSelectedView?.deselect()
                    currentSelectedView = this
                    currentSelectedView?.select()
                    itemSelectListener?.onItemSelected(item.index)

                }
            }
            bottomBarViewItemList.add(bottomBarItem)
            itemContainerLayout.addView(bottomBarItem)

            if (item.index == tabInitialSelectedIndex) {
                currentSelectedView = bottomBarItem

                val listener = object : ViewTreeObserver.OnGlobalLayoutListener {

                    override fun onGlobalLayout() {
                        bottomBarItem.select()
                        bottomBarItem.viewTreeObserver.removeGlobalOnLayoutListener(this)
                    }

                }

                bottomBarItem.viewTreeObserver.addOnGlobalLayoutListener(listener)
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

    private fun animateIndicator(currentItemIndex: Int) {
        val previousMargin: Float = (indicatorView?.layoutParams as? LinearLayout.LayoutParams)?.leftMargin?.toFloat() ?: 0F
        val currentMargin: Float = currentItemIndex * itemWidth
        indicatorAnimator?.setFloatValues(previousMargin, currentMargin)
        indicatorAnimator?.start()
    }

    companion object {

        const val ANIMATION_DURATION = 300L

    }

}