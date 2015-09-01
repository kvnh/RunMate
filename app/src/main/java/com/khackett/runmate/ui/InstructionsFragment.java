package com.khackett.runmate.ui;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.khackett.runmate.R;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link //InstructionsFragment.//OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link InstructionsFragment#//newInstance} factory method to
 * create an instance of this fragment.
 */
public class InstructionsFragment extends Fragment {

    /**
     * Default constructor
     */
    public InstructionsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_instructions, container, false);
        return rootView;
    }

}
