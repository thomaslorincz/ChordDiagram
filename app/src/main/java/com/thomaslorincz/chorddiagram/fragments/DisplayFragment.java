package com.thomaslorincz.chorddiagram.fragments;

import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.thomaslorincz.chord_diagram.ChordDiagram;
import com.thomaslorincz.chorddiagram.R;

/**
 * Created by Thomas on 27/08/2017.
 */

public class DisplayFragment extends Fragment {
    ChordDiagram chordDiagram;

    public DisplayFragment() {
        // Required empty public constructor.
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_display, container, false);
        chordDiagram = v.findViewById(R.id.chord_diagram);

        chordDiagram.addItem("1", Color.BLUE);
        chordDiagram.addItem("2", Color.RED);
        chordDiagram.addItem("3", Color.GREEN);
        chordDiagram.addItem("4", Color.YELLOW);
        chordDiagram.addItem("5", Color.MAGENTA);
        chordDiagram.addItem("5", Color.MAGENTA);

        chordDiagram.addLink("1", "2");
        chordDiagram.addLink("1", "3");
        chordDiagram.addLink("1", "4");
        chordDiagram.addLink("1", "5");

        chordDiagram.addLink("2", "3");
        chordDiagram.addLink("2", "4");
        chordDiagram.addLink("2", "5");

        chordDiagram.addLink("3", "4");
        chordDiagram.addLink("3", "5");

        chordDiagram.addLink("4", "5");
        chordDiagram.addLink("4", "5");
        chordDiagram.addLink("4", "5");

        return v;
    }
}
