package jnode.protocol.binkp;

import java.nio.ByteBuffer;
import jnode.protocol.io.Frame;

public class BinkpFrame implements Frame {
    private String arg;
    private BinkpCommand command;
    private byte[] data;
    private ByteBuffer frame;
    private boolean isCommand;

    public BinkpFrame(BinkpCommand command) {
        this(command, null);
    }

    public BinkpFrame(BinkpCommand command, String arg) {
        this.arg = arg;
        this.isCommand = true;
        this.command = command;
        int arglen = arg == null ? 0 : arg.getBytes().length;
        ByteBuffer buf = ByteBuffer.allocate(arglen + 1);
        buf.put((byte) command.getCmd());
        if (arglen > 0) {
            buf.put(arg.getBytes());
        }
        this.data = buf.array();
        init();
    }

    public BinkpFrame(byte[] filedata) {
        this.isCommand = false;
        this.data = filedata;
        init();
    }

    public BinkpCommand getCommand() {
        return this.command;
    }

    public byte[] getData() {
        return this.data;
    }

    public boolean isCommand() {
        return this.isCommand;
    }

    public String getArg() {
        return this.arg;
    }

    private void init() {
        int len;
        if (this.data != null && this.data.length != 0) {
            int len2 = this.data.length;
            if (this.isCommand) {
                len = len2 | 32768;
            } else {
                len = len2 & 32767;
            }
            this.frame = ByteBuffer.allocate(this.data.length + 2);
            this.frame.putShort((short) len);
            this.frame.put(this.data);
        }
    }

    @Override // jnode.protocol.io.Frame
    public byte[] getBytes() {
        return this.frame != null ? this.frame.array() : new byte[0];
    }

    public String toString() {
        return "[ " + (this.isCommand ? this.command.toString() + " " + this.arg : " DATA ") + " ]";
    }
}
