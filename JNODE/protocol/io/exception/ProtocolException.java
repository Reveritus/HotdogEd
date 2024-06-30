package jnode.protocol.io.exception;

public class ProtocolException extends Exception {
    private static final long serialVersionUID = 1;

    public ProtocolException() {
    }

    public ProtocolException(String message) {
        super(message);
    }

    public ProtocolException(Throwable cause) {
        super(cause);
    }

    public ProtocolException(String message, Throwable cause) {
        super(message, cause);
    }
}
