package io.github.springstudent.ada.client.monitor;

import io.github.springstudent.ada.client.bean.Listener;
public interface CounterListener<T> extends Listener {
	void onInstantValueUpdated(Counter<T> counter, T value);
}
