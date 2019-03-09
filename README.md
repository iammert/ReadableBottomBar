# ReadableBottomBar
Yet another material bottom bar library for Android

<img src="https://raw.githubusercontent.com/iammert/ReadableBottomBar/master/art/readablebottombar.png"/>

## GIF
<img src="https://raw.githubusercontent.com/iammert/ReadableBottomBar/master/art/preview.gif"/>

## Design Credits
All design and inspiration credits goes to [Ranjit](https://www.uplabs.com/posts/readable-tab-bar-concept).

## Usage

* Create your tabs.xml under your res/xml/ folder
```xml
<?xml version="1.0" encoding="utf-8"?>
<tabs>
    <tab
        drawable="@drawable/ic_home_black_24dp"
        text="@string/home" />

    <tab
        drawable="@drawable/ic_search_black_24dp"
        text="@string/search" />

    <tab
        drawable="@drawable/ic_shopping_basket_black_24dp"
        text="@string/bag" />

    <tab
        drawable="@drawable/ic_favorite_black_24dp"
        text="@string/favorite" />

    <tab
        drawable="@drawable/ic_account_circle_black_24dp"
        text="@string/profile" />
</tabs>
```
* Add bottom bar to your layout
```xml
<com.iammert.library.readablebottombar.ReadableBottomBar
    android:layout_width="match_parent"
    android:layout_height="56dp"
    app:rbb_tabs="@xml/tabs"/>
```
* Customize if you need
```xml
<com.iammert.library.readablebottombar.ReadableBottomBar
    android:layout_width="match_parent"
    android:layout_height="56dp"
    app:rbb_tabs="@xml/tabs"
    app:rbb_textColor=""
    app:rbb_indicatorHeight=""
    app:rbb_indicatorColor=""
    app:rbb_initialIndex=""
    app:rbb_backgroundColor=""
    app:rbb_textSize=""/>
```


License
--------


    Copyright 2019 Mert Şimşek

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.



