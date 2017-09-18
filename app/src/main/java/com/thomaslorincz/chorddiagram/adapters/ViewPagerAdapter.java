package com.thomaslorincz.chorddiagram.adapters;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentPagerAdapter;

import com.thomaslorincz.chorddiagram.fragments.DisplayFragment;
import com.thomaslorincz.chorddiagram.fragments.SettingsFragment;

/**
 * Created by Thomas on 17/09/2017.
 */

public class ViewPagerAdapter extends FragmentPagerAdapter {

    private static final int PAGE_COUNT = 2;
    private final String tabTitles[] = new String[] {"DISPLAY", "SETTINGS"};

    public ViewPagerAdapter(FragmentManager mFragmentManager) {
        super(mFragmentManager);
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new DisplayFragment();
            case 1:
                return new SettingsFragment();
        }
        return null;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        // Generate title based on item position
        return tabTitles[position];
    }
}
