package io.github.springstudent.ada.transport.file.bean;

/**
 * @author ZhouNing
 * @date 2024/12/31 10:32
 **/
public class FileException extends RuntimeException{
    public FileException() {
        super();
    }

    public FileException(String message) {
        super(message);
    }

    public FileException(String message, Throwable cause) {
        super(message, cause);
    }

    public FileException(Throwable cause) {
        super(cause);
    }

    protected FileException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
