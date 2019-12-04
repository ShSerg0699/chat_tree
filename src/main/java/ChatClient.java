import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatClient {
    private List<InetSocketAddress> children = new Vector<>();
    private ExecutorService executorService = Executors.newCachedThreadPool();
    private InputHandler inputHandler;
    private Listener listener;
    private Sender sender;
    private DatagramSocket socket;
    private List<DatagramWrapper> unconfirmedMessages = new Vector<>();
    private InetSocketAddress client;
    private InetSocketAddress parent;


    public ChatClient (ClientContext clientContext) throws IOException {
        this.client = new InetSocketAddress("localhost", clientContext.getPort());
        this.socket = new DatagramSocket(clientContext.getPort());
        this.parent = clientContext.getParent();
        if(parent == null) {
            this.sender = new Sender(children, unconfirmedMessages, socket, clientContext.getNodeName(), executorService, client);
            this.listener = new Listener(children, unconfirmedMessages, socket, sender, clientContext.getLossPercentage());
        } else{
            this.sender = new Sender(children, unconfirmedMessages, socket, clientContext.getNodeName(), executorService, client, parent);
            this.listener = new Listener(children, unconfirmedMessages, socket, sender, clientContext.getLossPercentage(), parent);

        }

        this.inputHandler = new InputHandler(sender);
    }

    public void start() throws IOException{
        if (parent != null) {
            Message helloMessage = new Message(sender.getName(),"", Message.MessageType.NEW_CHILD);
            DatagramWrapper datagramWrapper = new DatagramWrapper(helloMessage, client, parent);
            unconfirmedMessages.add(datagramWrapper);
            sender.sendMessage(datagramWrapper);
        }
        System.out.println("You joined to chat");
        System.out.println(client.getAddress().getHostAddress());
        executorService.submit(inputHandler);
        listener.listen();
    }

    public void stop(){
        listener.interrupt();
        inputHandler.interrupt();
        executorService.shutdownNow();
    }
}
