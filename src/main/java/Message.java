import java.io.Serializable;
import java.net.InetSocketAddress;
import java.util.UUID;

public class Message implements Serializable {
    private UUID guid;
    private String senderName;
    private String content;
    private MessageType type;
    private int sentCount = 0;
    private InetSocketAddress alternate;

    public enum MessageType {
        NEW_CHILD,
        MESSAGE,
        CONFIRMATION,
        ALTERNATE
    }

    public Message(Message message) {
        this.guid = message.guid;
        this.content = message.content;
        this.senderName = message.senderName;
        this.type = message.type;
    }

    public Message(String senderName, String content, MessageType type) {
        this.guid = UUID.randomUUID();
        this.senderName = senderName;
        this.content = content;
        this.type = type;
    }

    public Message(String senderName, String content, MessageType type, InetSocketAddress alternate) {
        this(senderName, content, type);
        this.alternate = alternate;
    }

    public UUID getGuid() {
        return guid;
    }

    public void setGuid(UUID guid) {
        this.guid = guid;
    }

    public String getSenderName() {
        return senderName;
    }

    public String getContent() {
        return content;
    }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public int getSentCount() {
        return sentCount;
    }

    public void increaseSentCount() {
        sentCount++;
    }

    public InetSocketAddress getAlternate() {
        return alternate;
    }
}
