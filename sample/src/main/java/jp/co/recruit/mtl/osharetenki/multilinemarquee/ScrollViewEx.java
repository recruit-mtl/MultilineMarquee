package jp.co.recruit.mtl.osharetenki.multilinemarquee;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ScrollView;

public class ScrollViewEx extends ScrollView {
    public static final String TAG = ScrollViewEx.class.getSimpleName();

    private ScrollViewListener listener;

    public ScrollViewEx(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public interface ScrollViewListener {
        public void onScrollChanged(View view, int l, int t, int oldl, int oldt);
        public void onScrollStopped(View view);
    }

    public void setOnScrollViewListener(ScrollViewListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if (null != listener)
            listener.onScrollChanged(this, l, t, oldl, oldt);
    }

    private int initialPosition;

    public void startStateCheck() {
        initialPosition = getScrollY();
        ScrollViewEx.this.postDelayed(ScrollStateCheck, 100);
    }

    private Runnable ScrollStateCheck = new Runnable() {
        @Override
        public void run() {
            int newPosition = getScrollY();
            if (0 == initialPosition - newPosition) {	//has stopped
                if (null != listener)
                    listener.onScrollStopped(ScrollViewEx.this);
            } else {
                startStateCheck();
            }
        }
    };
}
