package com.thomaslorincz.chorddiagram.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import com.thomaslorincz.chorddiagram.R;
import com.thomaslorincz.chorddiagram.activities.MainActivity;

/**
 * Created by Thomas on 27/08/2017.
 */

public class SettingsFragment extends Fragment {
    DisplayFragment mDisplay;

    EditText itemName;
    TextView redValue;
    SeekBar redSeekBar;
    TextView greenValue;
    SeekBar greenSeekBar;
    TextView blueValue;
    SeekBar blueSeekBar;
    Button addItem;
    Button deleteItem;
    Button clearItemText;

    EditText linkItem1Name;
    EditText linkItem2Name;
    Button addLink;
    Button deleteLink;
    Button clearLinkTexts;

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

        mDisplay = (DisplayFragment) getActivity().getFragmentManager().findFragmentByTag("android:switcher:" + R.id.pager + ":"+ 0);

        itemName = v.findViewById(R.id.item_edittext);
        redValue = v.findViewById(R.id.red_value);
        redSeekBar = v.findViewById(R.id.red_seekbar);
        greenValue = v.findViewById(R.id.green_value);
        greenSeekBar = v.findViewById(R.id.green_seekbar);
        blueValue = v.findViewById(R.id.blue_value);
        blueSeekBar = v.findViewById(R.id.blue_seekbar);
        addItem = v.findViewById(R.id.add_item);
        deleteItem = v.findViewById(R.id.delete_item);
        clearItemText = v.findViewById(R.id.clear_item_text);

        linkItem1Name = v.findViewById(R.id.item1_edittext);
        linkItem2Name = v.findViewById(R.id.item2_edittext);
        addLink = v.findViewById(R.id.add_link);
        deleteLink = v.findViewById(R.id.delete_link);
        clearLinkTexts = v.findViewById(R.id.clear_link_texts);

        redValue.setText("0");
        greenValue.setText("0");
        blueValue.setText("0");

        redSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                redValue.setText(String.valueOf(i));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        greenSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                greenValue.setText(String.valueOf(i));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        blueSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                blueValue.setText(String.valueOf(i));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        addItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int color = Color.rgb(
                        Integer.parseInt(redValue.getText().toString()),
                        Integer.parseInt(greenValue.getText().toString()),
                        Integer.parseInt(blueValue.getText().toString()));
                mDisplay.chordDiagram.addItem(itemName.getText().toString(), color);
            }
        });

        deleteItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDisplay.chordDiagram.deleteItem(itemName.getText().toString());
            }
        });

        clearItemText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                itemName.setText("");
            }
        });

        addLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDisplay.chordDiagram.addLink(
                        linkItem1Name.getText().toString(),
                        linkItem2Name.getText().toString());
            }
        });

        deleteLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDisplay.chordDiagram.deleteLink(
                        linkItem1Name.getText().toString(),
                        linkItem2Name.getText().toString());
            }
        });

        clearLinkTexts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                linkItem1Name.setText("");
                linkItem2Name.setText("");
            }
        });

        return v;
    }
}
