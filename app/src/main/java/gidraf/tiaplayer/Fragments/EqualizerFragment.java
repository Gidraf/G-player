package gidraf.tiaplayer.Fragments;


import android.media.audiofx.Equalizer;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import gidraf.tiaplayer.R;
import gidraf.tiaplayer.models.database.HistoryModel;
import gidraf.tiaplayer.services.MusicService;
import gidraf.tiaplayer.utils.ShowControlListener;

/**
 * A simple {@link Fragment} subclass.
 */
public class EqualizerFragment extends Fragment{
    View rootView;
    LinearLayout linearLayout;
    Equalizer equalizer;
    int sessionId = 0;

    public EqualizerFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
       if (getArguments()!=null) {
           sessionId = getArguments().getInt("session");
       }

       equalizer = new Equalizer(0,sessionId);
       setupVisualizerFX();
        rootView = inflater.inflate(R.layout.fragment_equalizer, container, false);
        linearLayout = rootView.findViewById(R.id.equalizer_layout);

        return  rootView;
    }

    private void setupVisualizerFX() {
        TextView textView  = new TextView(getContext());
        textView.setText("G Equalizer");
        textView.setTextSize(30);
        linearLayout.addView(textView);
    }

}
