package gidraf.tiaplayer.adapters;

import android.content.Context;
import android.graphics.Color;
import android.media.audiofx.Equalizer;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.sdsmdg.harjot.crollerTest.Croller;
import com.sdsmdg.harjot.crollerTest.OnCrollerChangeListener;

import java.util.List;

import gidraf.tiaplayer.R;
import gidraf.tiaplayer.models.EqualizerModal;
import gidraf.tiaplayer.utils.listeners.EqualizerListener;

public class EqualizerAdapter extends RecyclerView.Adapter<EqualizerHolder> {
    Context context;
    List<EqualizerModal> bandLists;
    EqualizerListener listener;

    public EqualizerAdapter(Context context, List<EqualizerModal> numberOfbandwidth, EqualizerListener listener) {
        this.context = context;
        this.bandLists = numberOfbandwidth;
        this.listener =  listener;
    }

    @NonNull
    @Override
    public EqualizerHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View view = inflater.inflate(R.layout.band_holder, viewGroup, false);

        return new EqualizerHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final EqualizerHolder equalizerHolder, int i) {
        final EqualizerModal equalizerModal = bandLists.get(i);
        Croller croller = equalizerHolder.croller;
        croller.setLabel(equalizerModal.getFrequencyheader());
        croller.setBackCircleColor(Color.parseColor("#666463"));
        croller.setMax(equalizerModal.getMax());
        croller.setOnCrollerChangeListener(new OnCrollerChangeListener() {
            @Override
            public void onProgressChanged(Croller croller, int progress) {
                listener.setEqualizerListener(equalizerModal.getBand(), (short) (progress+equalizerModal.getLowerbandlevel()));
            }

            @Override
            public void onStartTrackingTouch(Croller croller) {

            }

            @Override
            public void onStopTrackingTouch(Croller croller) {

            }
        });


    }

    @Override
    public int getItemCount() {
        return bandLists != null ? bandLists.size(): 0;
    }

}

class EqualizerHolder extends RecyclerView.ViewHolder {
    Croller croller;
    public EqualizerHolder(@NonNull View itemView) {
        super(itemView);
        croller = itemView.findViewById(R.id.croller);
    }
}
