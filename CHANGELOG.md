# Changelog

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
