package gidraf.tiaplayer.Fragments;

public class PresetModel {
    String color;
    String name;

    public PresetModel(String color, String name) {
        this.color = color;
        this.name = name;
    }

    public PresetModel() {
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

