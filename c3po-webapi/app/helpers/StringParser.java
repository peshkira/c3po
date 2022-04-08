package helpers;

import play.Logger;

/**
 * Created by artur on 10/04/16.
 */
public class StringParser {
    public static String getDistributionType(String input) {
        String result = null;
        if (input.contains(" \\|")) {
            String[] strings = input.split(" \\|");
            if (strings[1].contains("fixed"))
                return "fixed";
            return strings[1];
        }
        return result;
    }

    public static String getDistributionTypeWidth(String input) {
        String result = null;
        if (input.contains(" \\|")) {
            String[] strings = input.split(" \\|");
            if (strings[1].contains("fixed")) {
                String width = strings[1].replace("fixed", "");
                return width;
            }
        }
        return result;
    }

    public static Integer[] getDistributionBinRange(String input) {
        Integer[] result = new Integer[2];
        result[0]=null;
        result[1]=null;
        String[] split = input.split(" \\|");
        String[] values = split[0].split(" \\- ");
        try {
            int left = Integer.parseInt(values[0]);
            result[0]=left;
        } catch (Exception e){
            Logger.debug("Error occured during extraction of a range bin value from '" + input + "'");
        }

        try {
            int right = Integer.parseInt(values[1]);
            result[1]=right;
        } catch (Exception e){
            Logger.debug("Error occured during extraction of a range bin value from '" + input + "'");
        }

        return result;
    }


    public static String DistibutionRangeValueToString(String leftValue, String rightValue, String algorithm, String width)
    {
        return leftValue + " - " + rightValue + " |" + algorithm + width;
    }
}
