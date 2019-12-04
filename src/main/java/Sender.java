import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.ExecutorService;

public class Sender {
    private List<InetSocketAddress> children;
    private List<DatagramWrapper> unconfirmedMessages;
    private ExecutorService executorService;
    private DatagramSocket socket;
    private String name;
    private InetSocketAddress client;
    private InetSocketAddress parent;

    public Sender(List<InetSocketAddress> children,
                  List<DatagramWrapper> unconfirmedMessages, DatagramSocket socket,
                  String name, ExecutorService executor,
                  InetSocketAddress client){
        this.children = children;
        this.unconfirmedMessages = unconfirmedMessages;
        this.socket = socket;
        this.name = name;
        this.executorService = executor;
        this.client = client;
    }

    public Sender(List<InetSocketAddress> children,
                  List<DatagramWrapper> unconfirmedMessages, DatagramSocket socket,
                  String name, ExecutorService executor,
                  InetSocketAddress client, InetSocketAddress parent){
        this(children, unconfirmedMessages, socket, name, executor, client);
        this.parent = parent;
    }

    public synchronized void sendMessageToEveryone(Message message) {
        try {
            for (InetSocketAddress child : children) {
                DatagramWrapper datagramWrapper = new DatagramWrapper(message, client, child);
                unconfirmedMessages.add(datagramWrapper);
                sendMessage(datagramWrapper);
            }

            if (parent != null) {
                DatagramWrapper datagramWrapper = new DatagramWrapper(message, client, parent);
                unconfirmedMessages.add(datagramWrapper);
                sendMessage(datagramWrapper);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public synchronized void sendMessage(DatagramWrapper datagramWrapper) throws IOException {
        //System.out.println("Sending message: " + datagramWrapper.getMessage().getContent());
        socket.send(datagramWrapper.convertToDatagramPacket());
    }



    public void sendConfirmation(DatagramWrapper datagramWrapper) throws IOException {
        DatagramWrapper wrapper = new DatagramWrapper(new Message(datagramWrapper.getMessage()), client, datagramWrapper.getSender());
        wrapper.getMessage().setType(Message.MessageType.CONFIRMATION);
        sendMessage(wrapper);
    }


    public String getName(){
        return name;
    }

    public InetSocketAddress getClient() {
        return client;
    }
}
