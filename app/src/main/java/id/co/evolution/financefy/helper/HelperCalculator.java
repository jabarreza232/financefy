package id.co.evolution.financefy.helper;

public class HelperCalculator {
    public enum TYPE_CALCULATOR {
        ADDITION, //TODO PENJUMLAHAN
        SUBTRACTION,//TODO PENGURANGAN
        PARCELING,//TODO PEMBAGIAN
        MULTIPLICATION//TODO PERKALIAN
    }


    public static String calculate(long value1, long value2, String typeCalculator) {
        long result = 0;
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
        return String.valueOf(result);
    }

    public static String[] getSymbolCalculate() {

        return new String[]{"+", "-", "x", "/"};
    }

}
