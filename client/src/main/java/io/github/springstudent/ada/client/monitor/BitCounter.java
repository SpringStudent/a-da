package io.github.springstudent.ada.client.monitor;


import io.github.springstudent.ada.client.utils.UnitUtilities;

public class BitCounter extends RateCounter {
    public BitCounter(String uid, String shortDescription) {
        super(uid, shortDescription);
    }

    @Override
    public String formatRate(Double rate) {
        if (rate == null || Double.isNaN(rate)) {
            return "- bit/s";
        }
        return String.format("%s/s", UnitUtilities.toBitSize(rate));
    }

    @Override
    public int getWidth() {
        return 100;
    }
}
