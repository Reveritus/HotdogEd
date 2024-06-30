package com.pushkin.mime;

public class MIMEException extends Exception {
    private static final long serialVersionUID = -4666533792583873742L;

    public MIMEException() {
    }

    public MIMEException(String message) {
        super(message);
    }

    public MIMEException(Throwable cause) {
        super(cause);
    }

    public MIMEException(String message, Throwable cause) {
        super(message, cause);
    }
}
