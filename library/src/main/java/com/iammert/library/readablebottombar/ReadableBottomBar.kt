package com.iammert.library.readablebottombar

import android.content.Context
import android.graphics.Color
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.LinearLayout
import java.lang.IllegalArgumentException

class ReadableBottomBar @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : LinearLayout(context, attrs, defStyleAttr) {

    fun interface ItemSelectListener {
        fun onItemSelected(index: Int)
    }

    /**
     * Notified when the user taps the tab that is already selected, for
     * example to scroll the current screen back to the top.
     */
    fun interface ItemReselectListener {
        fun onItemReselected(index: Int)
    }

    enum class ItemType(val value: Int) {
        Text(0),
        Icon(1);

        companion object {

            fun getType(value: Int) = entries.firstOrNull { it.value == value } ?: Icon

        }
    }

    private val bottomBarItemList: List<BottomBarItem>

    private var tabInitialSelectedIndex = 0
    /**
     * Index of the currently selected tab.
     */
    var selectedIndex = 0
        private set
    private var tabBackgroundColor: Int = Color.WHITE
    private var tabIndicatorColor: Int = Color.BLACK
    private var tabIndicatorHeight: Int = 0

    private var layoutWidth: Float = 0f
    private var layoutHeight: Float = 0f

    private var itemWidth: Float = 0f
    private var itemHeight: Float = 0f

    private var currentSelectedView: BottomBarItemView? = null

    private var indicatorView: View? = null

    private var itemSelectListener: ItemSelectListener? = null

    private var itemReselectListener: ItemReselectListener? = null

    init {
        val typedArray = context.theme.obtainStyledAttributes(attrs, R.styleable.ReadableBottomBar, defStyleAttr, defStyleAttr)

        tabBackgroundColor = typedArray.getColor(R.styleable.ReadableBottomBar_rbb_backgroundColor, Color.WHITE)
        tabIndicatorColor = typedArray.getColor(R.styleable.ReadableBottomBar_rbb_indicatorColor, Color.BLACK)
        val defaultIndicatorHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_INDICATOR_HEIGHT_DP, resources.displayMetrics).toInt()
        tabIndicatorHeight = typedArray.getDimensionPixelSize(R.styleable.ReadableBottomBar_rbb_indicatorHeight, defaultIndicatorHeight)
        tabInitialSelectedIndex = typedArray.getInt(R.styleable.ReadableBottomBar_rbb_initialIndex, 0)

        val defaultTextSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, DEFAULT_TEXT_SIZE_SP, resources.displayMetrics)
        val textSize = typedArray.getDimension(R.styleable.ReadableBottomBar_rbb_textSize, defaultTextSize)
        val textColor = typedArray.getColor(R.styleable.ReadableBottomBar_rbb_textColor, Color.BLACK)
        val iconColor = typedArray.getColor(R.styleable.ReadableBottomBar_rbb_iconColor, Color.BLACK)
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
                    iconColor,
                    config.drawable,
                    activeItemType
            )
        }

        require(tabInitialSelectedIndex in bottomBarItemList.indices) {
            "rbb_initialIndex is $tabInitialSelectedIndex but the bar has ${bottomBarItemList.size} tabs"
        }
        selectedIndex = tabInitialSelectedIndex

        typedArray.recycle()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        layoutWidth = w.toFloat()
        layoutHeight = h.toFloat()

        itemWidth = layoutWidth / bottomBarItemList.size
        itemHeight = layoutHeight

        post {
            removeAllViews()
            drawIndicator()
            drawBottomBarItems()
        }
    }

    override fun onSaveInstanceState(): Parcelable {
        return SavedState(super.onSaveInstanceState()).also { it.selectedIndex = selectedIndex }
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state !is SavedState) {
            super.onRestoreInstanceState(state)
            return
        }
        super.onRestoreInstanceState(state.superState)
        if (state.selectedIndex !in bottomBarItemList.indices) {
            return
        }
        if (currentSelectedView == null) {
            selectedIndex = state.selectedIndex
        } else {
            selectItem(state.selectedIndex)
        }
    }

    fun setOnItemSelectListener(itemSelectListener: ItemSelectListener) {
        this.itemSelectListener = itemSelectListener
    }

    fun setOnItemReselectListener(itemReselectListener: ItemReselectListener) {
        this.itemReselectListener = itemReselectListener
    }

    fun selectItem(index: Int) {
        if (index !in bottomBarItemList.indices) {
            throw IllegalArgumentException("Index should be in range of 0-${bottomBarItemList.lastIndex}")
        }

        if (currentSelectedView == null) {
            // Item views are created after the first layout pass. Remember the
            // selection so they are drawn with the right tab selected.
            if (index != selectedIndex) {
                selectedIndex = index
                itemSelectListener?.onItemSelected(index)
            }
            return
        }

        val item = bottomBarItemList[index]
        for (i in 0 until childCount) {
            if (TAG_CONTAINER == getChildAt(i).tag) {
                val selectedItemView = ((getChildAt(i) as LinearLayout).getChildAt(index) as BottomBarItemView)
                if (selectedItemView != currentSelectedView) {
                    onSelected(item.index, selectedItemView)
                }
            }
        }
    }

    private fun drawBottomBarItems() {
        val itemContainerLayout = LinearLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(layoutWidth.toInt(), layoutHeight.toInt())
            orientation = HORIZONTAL
            tag = TAG_CONTAINER
        }

        bottomBarItemList.forEach { item ->
            val bottomBarItem = BottomBarItemView(context).apply {
                layoutParams = LinearLayout.LayoutParams(itemWidth.toInt(), itemHeight.toInt() - tabIndicatorHeight)

                setText(item.text)
                setItemType(item.type)
                setIconDrawable(item.drawable)
                setIconColor(item.iconColor)
                setTextSize(item.textSize)
                setTextColor(item.textColor)
                setTabColor(tabBackgroundColor)

                setOnClickListener {

                    if (it == currentSelectedView) {
                        itemReselectListener?.onItemReselected(item.index)
                        return@setOnClickListener
                    }

                    onSelected(item.index, this)
                }
            }

            itemContainerLayout.addView(bottomBarItem)

            if (item.index == selectedIndex) {
                currentSelectedView = bottomBarItem
                bottomBarItem.select(animate = false)
            }

        }

        addView(itemContainerLayout)
    }

    private fun drawIndicator() {
        indicatorView = View(context).apply {
            layoutParams = LinearLayout.LayoutParams(itemWidth.toInt(), tabIndicatorHeight)
            translationX = indicatorTranslation(selectedIndex)
            setBackgroundColor(tabIndicatorColor)
        }
        addView(indicatorView)
    }

    private fun animateIndicator(currentItemIndex: Int) {
        // Moving the indicator with translationX avoids a layout pass per frame.
        indicatorView?.animate()
                ?.translationX(indicatorTranslation(currentItemIndex))
                ?.setDuration(ANIMATION_DURATION)
                ?.setInterpolator(AccelerateDecelerateInterpolator())
                ?.start()
    }

    /**
     * The indicator is laid out at the start edge, which is the right edge in
     * RTL layouts where the items are also ordered from right to left.
     */
    private fun indicatorTranslation(index: Int): Float {
        val offset = index * itemWidth
        return if (layoutDirection == View.LAYOUT_DIRECTION_RTL) -offset else offset
    }

    private fun onSelected(index: Int, bottomBarItemView: BottomBarItemView) {
        animateIndicator(index)

        currentSelectedView?.deselect()
        selectedIndex = index
        currentSelectedView = bottomBarItemView
        currentSelectedView?.select()
        itemSelectListener?.onItemSelected(index)
    }

    internal class SavedState : BaseSavedState {

        var selectedIndex = 0

        constructor(superState: Parcelable?) : super(superState)

        private constructor(parcel: Parcel) : super(parcel) {
            selectedIndex = parcel.readInt()
        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeInt(selectedIndex)
        }

        companion object {
            @JvmField
            val CREATOR = object : Parcelable.Creator<SavedState> {
                override fun createFromParcel(parcel: Parcel) = SavedState(parcel)
                override fun newArray(size: Int) = arrayOfNulls<SavedState>(size)
            }
        }
    }

    companion object {

        const val ANIMATION_DURATION = 300L

        private const val DEFAULT_TEXT_SIZE_SP = 15f

        private const val DEFAULT_INDICATOR_HEIGHT_DP = 4f

        const val TAG_CONTAINER = "TAG_CONTAINER"

    }

}