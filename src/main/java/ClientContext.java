
import java.net.InetSocketAddress;

public class ClientContext {
    private String nodeName;
    private int port;
    private int lossPercentage;
    private InetSocketAddress parent;

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String name) {
        this.nodeName = name;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public InetSocketAddress getParent() {
        return parent;
    }

    public void setParent(InetSocketAddress parent) {
        this.parent = parent;
    }

    public int getLossPercentage() {
        return lossPercentage;
    }

    public void setLossPercentage(int lossPercentage) {
        this.lossPercentage = lossPercentage;
    }
}
