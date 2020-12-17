package march.dao;

import org.apache.tomcat.jni.Local;

import java.time.Instant;
import java.time.LocalDate;

public class TSDataPoint implements Comparable<TSDataPoint> {
    private LocalDate time;
    private double value;

    public LocalDate getTime() {
        return time;
    }

    public void setTime(LocalDate time) {
        this.time = time;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public TSDataPoint(LocalDate time, double value) {
        this.time = time;
        this.value = value;
    }

    @Override
    public int compareTo(TSDataPoint o) {
        return time.compareTo(o.time);
    }

    public void sumValue(double amount) {
        value+=amount;
    }

    public void swapIfGreater(double amount) {
        if(amount>value) {
            value = amount;
        }
    }

    public TSDataPoint invert() {
        return new TSDataPoint(getTime(), -1 * getValue());
    }
}
