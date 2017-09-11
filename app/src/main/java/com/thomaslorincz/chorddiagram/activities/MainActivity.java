package com.thomaslorincz.chorddiagram.activities;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;

import com.thomaslorincz.chorddiagram.R;
import com.thomaslorincz.chorddiagram.adapters.ViewPagerAdapter;
import com.thomaslorincz.chorddiagram.pagers.NonSwipeableViewPager;


/**
 * Created by Thomas on 11/07/2017.
 */

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        NonSwipeableViewPager mViewPager = findViewById(R.id.pager);
        TabLayout mTabLayout = findViewById(R.id.tab_layout);
        mTabLayout.setupWithViewPager(mViewPager);
        mViewPager.setAdapter(new ViewPagerAdapter(getFragmentManager()));
    }
}
