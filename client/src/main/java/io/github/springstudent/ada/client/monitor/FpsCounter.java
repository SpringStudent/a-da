package io.github.springstudent.ada.client.monitor;

public class FpsCounter extends RateCounter{

    public FpsCounter(String uid, String shortDescription) {
        super(uid, shortDescription);
    }

    @Override
    public String formatRate(Double rate) {
        if (rate == null || Double.isNaN(rate)) {
            return "- FPS";
        }
        return String.format("%.0f FPS", rate);
    }

    @Override
    public int getWidth() {
        return 80;
    }
}
