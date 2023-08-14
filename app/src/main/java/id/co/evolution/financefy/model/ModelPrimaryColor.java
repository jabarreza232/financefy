package id.co.evolution.financefy.model;

public class ModelPrimaryColor {
    String name;
    int colorPrimary;
    int colorPrimaryDark;

    public ModelPrimaryColor(String name, int colorPrimary, int colorPrimaryDark) {
        this.name = name;
        this.colorPrimary = colorPrimary;
        this.colorPrimaryDark = colorPrimaryDark;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getColorPrimary() {
        return colorPrimary;
    }

    public void setColorPrimary(int colorPrimary) {
        this.colorPrimary = colorPrimary;
    }

    public int getColorPrimaryDark() {
        return colorPrimaryDark;
    }

    public void setColorPrimaryDark(int colorPrimaryDark) {
        this.colorPrimaryDark = colorPrimaryDark;
    }
}
