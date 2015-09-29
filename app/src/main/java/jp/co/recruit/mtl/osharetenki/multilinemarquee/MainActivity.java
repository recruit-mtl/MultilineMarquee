package jp.co.recruit.mtl.osharetenki.multilinemarquee;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import jp.co.recruit.mtl.osharetenki.multilinemarqueelibrary.MultiLineMarqueeTextView;

public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final MultiLineMarqueeTextView marqueeText = (MultiLineMarqueeTextView)findViewById(R.id.marquee);
        findViewById(R.id.button_stop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                marqueeText.stopMarquee();
            }
        });
        findViewById(R.id.button_start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                marqueeText.startMarquee();
            }
        });
        findViewById(R.id.button_four).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                marqueeText.getLayoutParams().height = getResources().getDimensionPixelSize(R.dimen.marquee_area_one_line) * 4;
                marqueeText.requestLayout();
            }
        });
        findViewById(R.id.button_three).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                marqueeText.getLayoutParams().height = getResources().getDimensionPixelSize(R.dimen.marquee_area_one_line) * 3;
                marqueeText.requestLayout();
            }
        });
        findViewById(R.id.button_two).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                marqueeText.getLayoutParams().height = getResources().getDimensionPixelSize(R.dimen.marquee_area_one_line) * 2;
                marqueeText.requestLayout();
            }
        });
        findViewById(R.id.button_one).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                marqueeText.getLayoutParams().height = getResources().getDimensionPixelSize(R.dimen.marquee_area_one_line);
                marqueeText.requestLayout();
            }
        });
        ScrollViewEx scroll = (ScrollViewEx) findViewById(R.id.scroll);
        scroll.setOnScrollViewListener(new ScrollViewEx.ScrollViewListener() {
            @Override
            public void onScrollChanged(View view, int l, int t, int oldl, int oldt) {
                marqueeText.autoMarquee();
            }

            @Override
            public void onScrollStopped(View view) {
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
