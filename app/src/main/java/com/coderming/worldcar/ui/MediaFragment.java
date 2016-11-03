package com.coderming.worldcar.ui;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.coderming.worldcar.R;

/**
 * A placeholder fragment containing a simple view.
 */
public class MediaFragment extends Fragment {

    public MediaFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_ent, container, false);

        return rootView;
    }
}
