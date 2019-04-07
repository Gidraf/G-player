package gidraf.tiaplayer.adapters;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import gidraf.tiaplayer.R;
import gidraf.tiaplayer.models.Preset;
import gidraf.tiaplayer.utils.listeners.EqualizerListener;
import gidraf.tiaplayer.utils.listeners.PresetChangeListener;

public class PresetAdapter extends RecyclerView.Adapter<PresetAdapter.PresetHolder> {
    List<Preset> presetsLists;
    Context context;
    EqualizerListener listener;
    private int selectedPos = RecyclerView.NO_POSITION;
    Preset previousPreset;
    PresetChangeListener presetChangeListener;


    public PresetAdapter(List<Preset> presetsLists, Context context, EqualizerListener listener, PresetChangeListener  presetChangeListener) {
        this.presetsLists = presetsLists;
        this.context = context;
        this.listener = listener;
        this.presetChangeListener = presetChangeListener;
    }

    @NonNull
    @Override
    public PresetHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View rootView = inflater.inflate(R.layout.preset_holder, viewGroup, false);
        return new PresetHolder(rootView);
    }

    @Override
    public void onBindViewHolder(@NonNull final PresetHolder presetHolder, final int i) {
        final Preset preset = presetsLists.get(presetHolder.getAdapterPosition());
        presetHolder.presetName.setText(preset.getName());
        presetHolder.presetCard.setCardBackgroundColor(Color.parseColor(preset.getColor()));
        presetHolder.presetCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(preset.getColor().equals("#35393e")){
                    presetChangeListener.onResetChangeListner();
                    listener.setEqualizerPresetListenser((short) 0);
                    notifyDataSetChanged();
                    return;
                }
                listener.setEqualizerPresetListenser((short) presetHolder.getAdapterPosition());
                presetChangeListener.onPresetChangeListener(presetHolder.getAdapterPosition());
                notifyDataSetChanged();
            }
        });

    }

    @Override
    public int getItemCount() {
        return presetsLists != null ? presetsLists.size(): 0;
    }

    public class PresetHolder extends RecyclerView.ViewHolder {
        public TextView presetName;
        public CardView presetCard;
        public PresetHolder(@NonNull View itemView) {
            super(itemView);
            presetCard = itemView.findViewById(R.id.preset_card);
            presetName = itemView.findViewById(R.id.preset_name);
        }

    }


}
