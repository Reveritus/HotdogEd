package com.pushkin.hotdoged.export;

public class HotdogedException extends Exception {
    private static final long serialVersionUID = -5965413386819901643L;

    public HotdogedException() {
    }

    public HotdogedException(String detailMessage) {
        super(detailMessage);
    }

    public HotdogedException(Throwable throwable) {
        super(throwable);
    }

    public HotdogedException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }
}
