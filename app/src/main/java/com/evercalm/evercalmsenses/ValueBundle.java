package com.evercalm.evercalmsenses;

/**
 * Created by mattias on 2016-01-12.
 */
public class ValueBundle {
    double value;
    double timestamp;

    ValueBundle(float value, double timestamp) {
        this((double)value, timestamp);
    }

    ValueBundle(double value, double timestamp) {
        this.value = value;
        this.timestamp = timestamp;
    }

    @Override
    public ValueBundle clone() {
        return new ValueBundle(value, timestamp);
    }


}
