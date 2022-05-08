package id.co.evolution.financefy.helper;

import android.widget.Toast;

public class HelperCalculator {
    public enum TYPE_CALCULATOR {
        ADDITION, //TODO PENJUMLAHAN
        SUBTRACTION,//TODO PENGURANGAN
        PARCELING,//TODO PEMBAGIAN
        MULTIPLICATION//TODO PERKALIAN
    }


    public static String calculate(long value1, long value2, String typeCalculator) {
        long result = 0;
        try {
            switch (typeCalculator) {
                case "+":
                    result = value1 + value2;
                    break;
                case "-":
                    result = value1 - value2;
                    break;
                case "x":
                    result = value1 * value2;
                    break;
                case "/":
                    result = value1 / value2;
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return String.valueOf(result);
    }

    public static String[] getSymbolCalculate() {

        return new String[]{"+", "-", "x", "/"};
    }

}
