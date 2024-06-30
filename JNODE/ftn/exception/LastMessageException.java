package jnode.ftn.exception;

public class LastMessageException extends Exception {
    private static final long serialVersionUID = 1;

    public LastMessageException() {
    }

    public LastMessageException(String message, Throwable cause) {
        super(message, cause);
    }

    public LastMessageException(String message) {
        super(message);
    }

    public LastMessageException(Throwable cause) {
        super(cause);
    }
}
