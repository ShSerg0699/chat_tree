import java.io.IOException;
import java.net.InetSocketAddress;

public class ArgResolver {
    private final static int NODE_NAME_INDEX = 0;
    private final static int PORT_INDEX = 1;
    private final static int LOSS_PERCENTAGE_INDEX = 2;
    private final static int PARENT_ADDRESS_INDEX = 3;
    private final static int PARENT_PORT_INDEX = 4;
    private final static int MIN_ARGS_COUNT = 3;
    private final static int MAX_ARGS_COUNT = 5;

    public static ClientContext resolve (String args[]) throws IOException {
        if (args.length != MIN_ARGS_COUNT && args.length != MAX_ARGS_COUNT){
            throw new IOException("Wrong arguments count\nUsage: treeChat <node_name> <port> <loss_percentage> [<neighbour_address> <neighbour_port>]");
        }
        ClientContext clientContext = new ClientContext();

        try{
            clientContext.setNodeName(args[NODE_NAME_INDEX]);
            clientContext.setPort(Integer.parseInt(args[PORT_INDEX]));
            clientContext.setLossPercentage(Integer.parseInt(args[LOSS_PERCENTAGE_INDEX]));

            if(args.length == MAX_ARGS_COUNT){
                clientContext.setParent(new InetSocketAddress(args[PARENT_ADDRESS_INDEX], Integer.parseInt(args[PARENT_PORT_INDEX])));
            }
        } catch (Exception e){
            throw new IOException("Wrong arguments " + e.getLocalizedMessage());
        }

        return clientContext;
    }
}
