package jnode.ftn.types;

public class Ftn2D {
    private int net;
    private int node;

    public int getNet() {
        return this.net;
    }

    public void setNet(int net) {
        this.net = net;
    }

    public int getNode() {
        return this.node;
    }

    public void setNode(int node) {
        this.node = node;
    }

    public Ftn2D() {
    }

    public Ftn2D(int net, int node) {
        this.net = net;
        this.node = node;
    }

    public int hashCode() {
        int result = this.net + 31;
        return (result * 31) + this.node;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj != null && getClass() == obj.getClass()) {
            Ftn2D other = (Ftn2D) obj;
            return this.net == other.net && this.node == other.node;
        }
        return false;
    }

    public String toString() {
        return String.format("%d/%d", Integer.valueOf(this.net), Integer.valueOf(this.node));
    }
}
