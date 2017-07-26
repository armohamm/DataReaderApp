package nitdgp.devansh.datareader;

/**
 * Created by devansh on 25/7/17.
 */

import android.support.v7.app.AppCompatActivity;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class ListenerThread extends AppCompatActivity implements Runnable{
    private DatagramPacket packet;
    private DatagramSocket socket;
    private byte buffer[];
    private boolean isRunning;
    public Logger logger;

    public ListenerThread(){
        isRunning = false;
        logger = new Logger("DataReader","receiver.txt");
    }

    @Override
    public void run(){
        try{
            socket = new DatagramSocket(8080, InetAddress.getByName("192.168.43.255"));
            socket.setBroadcast(true);
            isRunning = true;
            buffer = new byte[15000];
            packet = new DatagramPacket(buffer, buffer.length);
            while(isRunning) {
                socket.receive(packet);
                //String sender = new String(packet.getData()).trim();
                logger.d("UDP Broadcast Received at " + System.currentTimeMillis());
            }
        }
        catch (Exception e){
            e.printStackTrace();
            isRunning = false;
        }
    }

    public void kill(){
        isRunning = false;
    }
}