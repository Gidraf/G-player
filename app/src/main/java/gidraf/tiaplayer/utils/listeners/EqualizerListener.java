package gidraf.tiaplayer.utils.listeners;

public interface EqualizerListener {
    void onActivateEqualizer(boolean isActivated);

    void setEqualizerListener(short equalizerBandwidth, short lowerEqualizerBandwidth);

    void setEqualizerPresetListenser(short presetName);
}
