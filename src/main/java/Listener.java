import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.*;

public class Listener {
    private List<InetSocketAddress> children;
    private List<DatagramWrapper> unconfirmedMessages;
    private List<DatagramWrapper> receiveMessages = new Vector<>();
    private DatagramSocket socket;
    private Sender sender;
    private InetSocketAddress parent;
    private InetSocketAddress alternate;
    private int lossPercentage;
    private boolean isInterrupted = false;
    private Random random = new Random();
    private static final int CONFIRMATION_PERIOD = 1000;
    private static final int ALTERNATE_SEND_PERIOD = 3000;

    public Listener(List<InetSocketAddress> children,
                    List<DatagramWrapper> unconfirmedMessages,
                    DatagramSocket socket, Sender sender, int lossPercentage) {
        this.children = children;
        this.unconfirmedMessages = unconfirmedMessages;
        this.socket = socket;
        this.sender = sender;
        this.lossPercentage = lossPercentage;
    }

    public Listener(List<InetSocketAddress> children,
                    List<DatagramWrapper> unconfirmedMessages,
                    DatagramSocket socket, Sender sender, int lossPercentage, InetSocketAddress parent){
        this(children, unconfirmedMessages, socket,sender, lossPercentage);
        this.parent = parent;
    }

    public void listen() {
        try {
            Timer confirmationTimer = new Timer();
            confirmationTimer.schedule(new ResendUnconfirmedMessageTimerTask(), 0, CONFIRMATION_PERIOD);

            Timer alternateSendTimer = new Timer();
            alternateSendTimer.schedule(new AlternateSendTimerTask(), 0, ALTERNATE_SEND_PERIOD);

            while (!isInterrupted) {
                DatagramPacket receivedPacket = new DatagramPacket(new byte[2048], 0, 2048);
                socket.receive(receivedPacket);

                if (random.nextInt(100) < lossPercentage) {
                    System.out.println("Some packet was lost");
                    continue;
                }

                DatagramWrapper datagramWrapper = new DatagramWrapper(receivedPacket);

                switch (datagramWrapper.getMessage().getType()) {
                    case NEW_CHILD:
                        InetSocketAddress node = new InetSocketAddress(receivedPacket.getAddress(), receivedPacket.getPort());

                        if (children.indexOf(node) == -1) {
                            children.add(node);
                            System.out.println("New child: " + datagramWrapper.getMessage().getSenderName());
                        }
                        receiveMessages.add(datagramWrapper);
                        sender.sendConfirmation(datagramWrapper);
                        break;
                    case MESSAGE:
                        if (hasSuchGuid(datagramWrapper.getMessage().getGuid()) == -1) {
                            System.out.println("<" + datagramWrapper.getMessage().getSenderName() + ">: " + datagramWrapper.getMessage().getContent());
                            if ((parent != null) && !(parent.getAddress().equals(datagramWrapper.getSender().getAddress())
                                    && (parent.getPort() == datagramWrapper.getSender().getPort()))) {
                                DatagramWrapper forwardDatagramWrapper = new DatagramWrapper(datagramWrapper.getMessage(), sender.getClient(), parent);
                                unconfirmedMessages.add(forwardDatagramWrapper);
                                sender.sendMessage(forwardDatagramWrapper);
                            }

                            for(InetSocketAddress child : children){
                                if (!(child.getAddress().equals(datagramWrapper.getSender().getAddress()) &&
                                        (child.getPort() == datagramWrapper.getSender().getPort()))) {
                                    DatagramWrapper forwardDatagramWrapper = new DatagramWrapper(datagramWrapper.getMessage(), sender.getClient(), child);
                                    unconfirmedMessages.add(forwardDatagramWrapper);
                                    sender.sendMessage(forwardDatagramWrapper);
                                }
                            }
                        }
                        receiveMessages.add(datagramWrapper);
                        sender.sendConfirmation(datagramWrapper);

                        break;
                    case CONFIRMATION:
                        confirmMessageWithGuid(datagramWrapper.getMessage().getGuid());
                        break;
                    case ALTERNATE:
                        alternate = datagramWrapper.getMessage().getAlternate();
                        break;
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void interrupt() {
        isInterrupted = true;
    }

    private synchronized int hasSuchGuid(UUID uuid) {
        for (int i = 0; i < receiveMessages.size(); i++) {
            if (receiveMessages.get(i).getMessage().getGuid().equals(uuid)) {
                return i;
            }
        }
        return -1;
    }

    private synchronized void confirmMessageWithGuid(UUID guid) {
        for (int i = 0; i < unconfirmedMessages.size(); i++) {
            if (unconfirmedMessages.get(i).getMessage().getGuid().equals(guid)) {
                unconfirmedMessages.remove(i);
                return;
            }
        }
    }

    private class ResendUnconfirmedMessageTimerTask extends TimerTask {
        private int MAX_TIME_SENT = 3;

        @Override
        public void run() {
            for (int i = 0; i < unconfirmedMessages.size(); ++i) {
                DatagramWrapper datagramWrapper = unconfirmedMessages.get(i);
                if (datagramWrapper.getMessage().getSentCount() >= MAX_TIME_SENT) {
                    try {
                        for (int j = 0; j < children.size(); ++j) {
                            if (children.get(j).equals(datagramWrapper.getReceiver())) {
                                System.out.println("Child " + children.get(j).getAddress() + " deleted");
                                children.remove(j);
                                return;
                            }
                        }
                        if (parent.equals(datagramWrapper.getReceiver())) {
                            System.out.println("Parent " + parent.getAddress() +  " deleted");
                            parent = alternate;
                            alternate = null;

                            if (parent != null) {
                                DatagramWrapper helloYouAreMyFather = new DatagramWrapper(new Message(sender.getName(), "", Message.MessageType.NEW_CHILD), sender.getClient(), parent);
                                sender.sendMessage(helloYouAreMyFather);
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    unconfirmedMessages.remove(datagramWrapper);
                    break;
                }
                datagramWrapper.getMessage().increaseSentCount();
                try {
                    sender.sendMessage(datagramWrapper);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    private class AlternateSendTimerTask extends TimerTask {
        @Override
        public void run() {
            for (InetSocketAddress child : children) {
                try {
                    DatagramWrapper datagramWrapper;
                    if (parent != null) {
                        datagramWrapper = new DatagramWrapper(new Message( sender.getName(), "", Message.MessageType.ALTERNATE, parent), sender.getClient(), child);
                    } else {
                        if (child.equals(children.get(0))) {
                            datagramWrapper = new DatagramWrapper(new Message(sender.getName(), "", Message.MessageType.ALTERNATE, null), sender.getClient(), child);
                        } else {
                            datagramWrapper = new DatagramWrapper(new Message(sender.getName(), "", Message.MessageType.ALTERNATE, children.get(0)), sender.getClient(), child);
                        }
                    }
                    sender.sendMessage(datagramWrapper);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
