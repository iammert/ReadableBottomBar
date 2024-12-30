# Changelog

## 0.4.0

- **Breaking:** `BottomBarItemView`, `ConfigurationXmlParser`, `BottomBarItem` and
  `BottomBarItemConfig` are now internal.
- Added `setOnItemReselectListener`.
- Listeners are `fun interface`s and accept Kotlin lambdas.
- Added `setTabs(@XmlRes)` to replace the tabs at runtime.
- Added `rbb_animationDuration`, `rbb_textAppearance` and `rbb_indicatorCornerRadius`.
- Tabs show a ripple when pressed.
- Tabs report their title, position and selected state to accessibility services.
- Fixed the indicator position in RTL layouts.
- Fixed an empty strip at the end of the bar when its width was not divisible by the tab count.
- A bar without `rbb_tabs` inflates empty instead of crashing.
- An out of range `rbb_initialIndex` fails with a descriptive error.
- Built with Kotlin 2.0 and compiled against API 35.

## 0.3.0

- Migrated to AndroidX. `minSdk` is now 21.
- Fixed `selectItem(itemCount)` crashing with `IndexOutOfBoundsException`.
- Fixed duplicate item views being added whenever the bar was resized.
- Fixed `rbb_textSize` being scaled twice.
- Fixed a crash when an item was selected before being measured.
- `selectItem` calls made before the first layout are no longer ignored.
- The selected index is saved and restored across configuration changes.
- Added `selectedIndex`.
- Tab `text` accepts literal strings.
- Invalid tab XML now fails with a descriptive `IllegalArgumentException`.
- The default indicator height is now 4dp.
