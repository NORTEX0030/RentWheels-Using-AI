package com.example.enhanceddynamicpricing;

public class VehicleType {
    private String name;
    private boolean isSelected;
    private int drawableResId; // New field for drawable resource ID

    public VehicleType(String name, boolean isSelected, int drawableResId) {
        this.name = name;
        this.isSelected = isSelected;
        this.drawableResId = drawableResId;
    }

    public String getName() {
        return name;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public int getDrawableResId() {
        return drawableResId;
    }
}