package id.co.evolution.financefy.model;

public class ModelNotification {
    public String title;
    public  String description;
    public int requestCode;
    public ModelNotification(String title, String description) {
        this.title = title;
        this.description = description;
    }
}
