package helpers;

import java.util.Collections;
import java.util.List;

/**
 * Created by artur on 23/03/16.
 */
public class Statistics {
    private List<Double> data;

    public Statistics(List<Double> data) {
        this.data = data;
    }

    public double getSum() {
        double sum = 0.0;
        for (double d : data) {
            sum += d;
        }
        return sum;
    }

    public double getCount(){
        return data.size();
    }

    public double getMean() {
        double sum = 0.0;
        for (double d : data) {
            sum += d;
        }
        return sum / (double) data.size();
    }

    public double getVariance() {
        double mean = getMean();
        double tmp = 0;
        for (double d : data) {
            tmp += (mean - d) * (mean - d);
        }
        return tmp / (double) data.size();
    }

    public double getMax(){
        double max=Double.MIN_VALUE;
        for (double d : data) {
            if (max<d)
                max=d;
        }
        return max;
    }

    public double getMin(){
        double min=Double.MAX_VALUE;
        for (double d : data) {
            if (min>d)
                min=d;
        }
        return min;
    }

    public double getStdDev() {
        return Math.sqrt(getVariance());
    }

    public double median() {
        Collections.sort(data);

        if (data.size() % 2 == 0) {
            return (data.get((data.size() / 2) - 1) + data.get(data.size() / 2)) / 2.0;
        } else {
            return data.get(data.size() / 2);
        }
    }

}
