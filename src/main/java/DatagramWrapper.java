import java.io.*;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;

public class DatagramWrapper {
    private Message message;
    private InetSocketAddress sender;
    private InetSocketAddress receiver;

    private ByteArrayOutputStream baos = new ByteArrayOutputStream(2048);
    private ObjectOutputStream oos;

    public DatagramWrapper(Message message, InetSocketAddress sender, InetSocketAddress receiver) throws IOException {
        oos = new ObjectOutputStream(baos);

        this.message = message;
        this.sender = sender;
        this.receiver = receiver;
    }

    public DatagramWrapper(DatagramPacket datagramPacket) throws IOException, ClassNotFoundException {
        oos = new ObjectOutputStream(baos);

        this.sender = new InetSocketAddress(datagramPacket.getAddress(), datagramPacket.getPort());
        this.message = deserializeObject(datagramPacket.getData());
    }

    private synchronized <T> T deserializeObject(byte[] rawData) throws IOException, ClassNotFoundException {
        return (T) new ObjectInputStream(new ByteArrayInputStream(rawData)).readObject();
    }

    public synchronized DatagramPacket convertToDatagramPacket() throws IOException {
        byte[] toSend = serializeObject(message);
        return new DatagramPacket(toSend, toSend.length, receiver.getAddress(), receiver.getPort());
    }

    private synchronized byte[] serializeObject(Object object) throws IOException {
        oos.writeObject(object);
        return baos.toByteArray();
    }

    public Message getMessage() {
        return message;
    }

    public InetSocketAddress getSender(){
        return sender;
    }

    public void setReceiver (InetSocketAddress receiver){
        this.receiver = receiver;
    }

    public InetSocketAddress getReceiver(){
        return receiver;
    }

}
