# MultiLineMarquee
MultilineMarquee provides an easy way to implement marquee for Android.  
 - automatically start marquee if this view is on screen.
 - display text of multi lines if view's height is enough.
![sample_anim](https://github.com/recruit-mtl/MultilineMarquee/blob/master/scroll_sample_anim.gif)

# Usage
layout.xml
```
 <jp.co.recruit.mtl.osharetenki.multilinemarqueelibrary.MultiLineMarqueeTextView
     android:id="@+id/marquee"
     android:layout_width="match_parent"
     android:layout_height="@dimen/marquee_area_two_lines"
     android:text="@string/marquee_message" />
```
Activity
```java
MultiLineMarqueeTextView marqueeText = (MultiLineMarqueeTextView)findViewById(R.id.marquee);
marqueeText.autoMarquee();
```

# Download
clone or copy [this file](https://github.com/recruit-mtl/MultilineMarquee/blob/master/library/src/main/java/jp/co/recruit/mtl/osharetenki/multilinemarqueelibrary/MultiLineMarqueeTextView.java)

# License
MultilineMarquee is available under the MIT license. See the LICENSE file for more info.

