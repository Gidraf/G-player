package gidraf.tiaplayer.models;

public class EqualizerModal {
    short band, lowerbandlevel;
    String frequencyheader, upperEqualizer, lowerEqualizer;
    int max;

    public short getSeek_id() {
        return seek_id;
    }

    public void setSeek_id(short seek_id) {
        this.seek_id = seek_id;
    }

    public int getMax() {

        return max;
    }

    public void setMax(int max) {
        this.max = max;
    }

    public EqualizerModal(short band, String frequencyheader, String upperEqualizer, String lowerEqualizer, short seek_id, short max, short lowerbandlevel) {
        this.band = band;
        this.frequencyheader = frequencyheader;
        this.upperEqualizer = upperEqualizer;
        this.lowerEqualizer = lowerEqualizer;
        this.seek_id = seek_id;
        this.max = max;
        this.lowerbandlevel = lowerbandlevel;
    }

    short seek_id;

    public EqualizerModal() {
    }

    public short getLowerbandlevel() {
        return lowerbandlevel;
    }

    public void setLowerbandlevel(short lowerbandlevel) {
        this.lowerbandlevel = lowerbandlevel;
    }

    public String getFrequencyheader() {
        return frequencyheader;
    }

    public void setFrequencyheader(String frequencyheader) {
        this.frequencyheader = frequencyheader;
    }

    public String getUpperEqualizer() {
        return upperEqualizer;
    }

    public void setUpperEqualizer(String upperEqualizer) {
        this.upperEqualizer = upperEqualizer;
    }

    public String getLowerEqualizer() {
        return lowerEqualizer;
    }

    public void setLowerEqualizer(String lowerEqualizer) {
        this.lowerEqualizer = lowerEqualizer;
    }

    public EqualizerModal(short band) {
        this.band = band;
    }

    public short getBand() {
        return band;
    }

    public void setBand(short band) {
        this.band = band;
    }
}
