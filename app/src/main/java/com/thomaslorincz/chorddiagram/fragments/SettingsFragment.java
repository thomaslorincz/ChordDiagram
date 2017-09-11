package com.thomaslorincz.chorddiagram.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.thomaslorincz.chorddiagram.R;

/**
 * Created by Thomas on 27/08/2017.
 */

public class SettingsFragment extends Fragment {
    TextView itemName;
    ImageButton addItem;
    ImageButton removeItem;

    TextView itemName1;
    TextView itemName2;
    ImageButton addLink;
    ImageButton removeLink;

    public SettingsFragment() {
        // Required empty public constructor.
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_settings, container, false);
        return v;
    }
}
