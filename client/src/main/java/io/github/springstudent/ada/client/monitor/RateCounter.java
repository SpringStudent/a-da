package io.github.springstudent.ada.client.monitor;

public abstract class RateCounter extends Counter<Double> {

	private double instantValue = 0;

	RateCounter(String uid, String shortDescription) {
		super(uid, shortDescription);
	}

	@Override
    public String formatInstantValue(Double value) {
		return formatRate(value);
	}

	protected abstract String formatRate(Double rate);

	public void add(double value) {
		synchronized (this) {
			instantValue += value;
		}
	}

	@Override
    public void computeAndResetInstantValue() {
		double rate = Double.NaN;

		synchronized (this) {
			if (instantStart.get() != -1) {
				final long elapsed = System.currentTimeMillis() - instantStart.get();
				rate = 1000.0 * instantValue / elapsed;
				instantStart.set(System.currentTimeMillis());
				instantValue = 0;
			}
		}

		fireOnInstantValueUpdated(rate);
	}

}