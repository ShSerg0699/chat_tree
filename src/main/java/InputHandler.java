import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class InputHandler implements Runnable {
    private boolean isInterrupted = false;
    private Sender sender;

    public InputHandler(Sender sender){
        this.sender = sender;
    }

    public void interrupt(){
        isInterrupted = true;
    }

    @Override
    public void run() {
        try{
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            String message;
            while (!isInterrupted){
                message = reader.readLine();
                System.out.println("<" + sender.getName() + ">: " + message);
                sender.sendMessageToEveryone(new Message(sender.getName(),message, Message.MessageType.MESSAGE));
            }
        } catch (IOException e){
            e.printStackTrace();
        }

    }
}
