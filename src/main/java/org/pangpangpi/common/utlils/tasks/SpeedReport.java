package org.pangpangpi.common.utlils.tasks;

public class SpeedReport {

    private double midQps;
    private double midValue;
    private double midAverage;
    private double midMinValue;
    private double midMaxValue;
    private double midCostPerThread;
    private double qps;
    private double minValue;
    private double maxValue;
    private double average;
    private double totalCostPerThread;

    public double getMidQps() {
        return midQps;
    }

    public void setMidQps(double midQps) {
        this.midQps = midQps;
    }

    public double getMidValue() {
        return midValue;
    }

    public void setMidValue(double midValue) {
        this.midValue = midValue;
    }

    public double getMidAverage() {
        return midAverage;
    }

    public void setMidAverage(double midAverage) {
        this.midAverage = midAverage;
    }

    public double getMidMinValue() {
        return midMinValue;
    }

    public void setMidMinValue(double midMinValue) {
        this.midMinValue = midMinValue;
    }

    public double getMidMaxValue() {
        return midMaxValue;
    }

    public void setMidMaxValue(double midMaxValue) {
        this.midMaxValue = midMaxValue;
    }

    public double getMidCostPerThread() {
        return midCostPerThread;
    }

    public void setMidCostPerThread(double midCostPerThread) {
        this.midCostPerThread = midCostPerThread;
    }

    public double getQps() {
        return qps;
    }

    public void setQps(double qps) {
        this.qps = qps;
    }

    public double getMinValue() {
        return minValue;
    }

    public void setMinValue(double minValue) {
        this.minValue = minValue;
    }

    public double getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(double maxValue) {
        this.maxValue = maxValue;
    }

    public double getAverage() {
        return average;
    }

    public void setAverage(double average) {
        this.average = average;
    }

    public double getTotalCostPerThread() {
        return totalCostPerThread;
    }

    public void setTotalCostPerThread(double totalCostPerThread) {
        this.totalCostPerThread = totalCostPerThread;
    }

    @Override
    public String toString() {
        return "SpeedReport{" +
                "midQps=" + midQps +
                ", midValue=" + midValue +
                ", midAverage=" + midAverage +
                ", midMinValue=" + midMinValue +
                ", midMaxValue=" + midMaxValue +
                ", midCostPerThread=" + midCostPerThread +
                ", qps=" + qps +
                ", minValue=" + minValue +
                ", maxValue=" + maxValue +
                ", average=" + average +
                ", totalCostPerThread=" + totalCostPerThread +
                '}';
    }

}
