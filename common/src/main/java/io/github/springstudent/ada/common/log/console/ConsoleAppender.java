package io.github.springstudent.ada.common.log.console;


import io.github.springstudent.ada.common.log.LogAppender;
import io.github.springstudent.ada.common.log.LogLevel;

public class ConsoleAppender extends LogAppender {

	@Override
	@SuppressWarnings("squid:S106")
    public synchronized void append(LogLevel level, String message, Throwable error) {
		System.out.println(format(level, message));

		if (error != null) {
			error.printStackTrace(System.out);
		}
	}
}
