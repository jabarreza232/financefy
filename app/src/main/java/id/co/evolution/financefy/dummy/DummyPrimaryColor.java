package id.co.evolution.financefy.dummy;

import java.util.ArrayList;
import java.util.List;

import id.co.evolution.financefy.R;
import id.co.evolution.financefy.model.ModelPrimaryColor;

public class DummyPrimaryColor {

    public enum PRIMARY_COLOR{
        Purple,
        Orange,
        Green,
        Brown,
        Red
    }


    public static List<ModelPrimaryColor> getDataPrimaryColor() {
        List<ModelPrimaryColor> data = new ArrayList<>();
        data.add(new ModelPrimaryColor("Purple", R.color.colorPrimary,R.color.colorPrimaryDark));
        data.add(new ModelPrimaryColor("Orange",R.color.colorPrimaryOrange,R.color.colorPrimaryDarkOrange));
        data.add(new ModelPrimaryColor("Green",R.color.colorPrimaryGreen,R.color.colorPrimaryDarkGreen));
        data.add(new ModelPrimaryColor("Brown",R.color.colorPrimaryBrown,R.color.colorPrimaryDarkBrown));
        data.add(new ModelPrimaryColor("Red",R.color.colorPrimaryRed,R.color.colorPrimaryDarkRed));
        return data;
    }
}
