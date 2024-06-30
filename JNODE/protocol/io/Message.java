package jnode.protocol.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public class Message {
    private File file;
    private InputStream inputStream;
    private long messageLength;
    private String messageName;
    private boolean secure = true;

    public Message(File file) throws Exception {
        this.file = file;
        this.messageName = file.getName();
        this.messageLength = file.length();
        this.inputStream = new FileInputStream(file);
    }

    public Message(String name, long len) {
        this.messageName = name;
        this.messageLength = len;
    }

    public void delete() {
        if (this.file != null) {
            this.file.delete();
        }
    }

    public long getMessageLength() {
        return this.messageLength;
    }

    public void setMessageLength(long arg) {
        this.messageLength = arg;
    }

    public String getMessageName() {
        return this.messageName;
    }

    public void setMessageName(String arg) {
        this.messageName = arg;
    }

    public InputStream getInputStream() {
        return this.inputStream;
    }

    public void setInputStream(InputStream arg) {
        this.inputStream = arg;
    }

    public boolean isSecure() {
        return this.secure;
    }

    public void setSecure(boolean secure) {
        this.secure = secure;
    }
}
