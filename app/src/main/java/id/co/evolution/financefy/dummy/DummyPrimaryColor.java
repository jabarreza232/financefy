package id.co.evolution.financefy.dummy;

import java.util.ArrayList;
import java.util.List;

import id.co.evolution.financefy.R;
import id.co.evolution.financefy.model.ModelPrimaryColor;

public class DummyPrimaryColor {

    public enum PRIMARY_COLOR {
        Purple,
        Orange,
        Green,
        Brown,
        Red,
        Pink,    // Warna baru
        Indigo,  // Warna baru
        Slate    // Warna baru
    }

    public static List<ModelPrimaryColor> getDataPrimaryColor() {
        List<ModelPrimaryColor> data = new ArrayList<>();

        // Praktik terbaik: Gunakan .name() dari enum alih-alih mengetik String manual ("Purple")
        // untuk mencegah typo dan memastikan pencocokan yang sempurna di fungsi Helper tema.
        data.add(new ModelPrimaryColor(PRIMARY_COLOR.Purple.name(), R.color.colorPrimary, R.color.colorPrimaryDark));
        data.add(new ModelPrimaryColor(PRIMARY_COLOR.Orange.name(), R.color.colorPrimaryOrange, R.color.colorPrimaryDarkOrange));
        data.add(new ModelPrimaryColor(PRIMARY_COLOR.Green.name(), R.color.colorPrimaryGreen, R.color.colorPrimaryDarkGreen));
        data.add(new ModelPrimaryColor(PRIMARY_COLOR.Brown.name(), R.color.colorPrimaryBrown, R.color.colorPrimaryDarkBrown));
        data.add(new ModelPrimaryColor(PRIMARY_COLOR.Red.name(), R.color.colorPrimaryRed, R.color.colorPrimaryDarkRed));

        // Penambahan Palet Warna Baru
        data.add(new ModelPrimaryColor(PRIMARY_COLOR.Pink.name(), R.color.colorPrimaryPink, R.color.colorPrimaryDarkPink));
        data.add(new ModelPrimaryColor(PRIMARY_COLOR.Indigo.name(), R.color.colorPrimaryIndigo, R.color.colorPrimaryDarkIndigo));
        data.add(new ModelPrimaryColor(PRIMARY_COLOR.Slate.name(), R.color.colorPrimarySlate, R.color.colorPrimaryDarkSlate));

        return data;
    }
}
