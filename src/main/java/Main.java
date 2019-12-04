import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        ChatClient chatClient;
        try {
             chatClient = new ChatClient(ArgResolver.resolve(args));
            chatClient.start();
        }catch (IOException e){
            System.out.println(e.getLocalizedMessage());
        }
    }
}
