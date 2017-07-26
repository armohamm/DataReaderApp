package nitdgp.devansh.datareader;

/**
 * Created by devansh on 25/7/17.
 */

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class BroadcastingThread implements Runnable{
    private String BROADCAST_IP;
    private int PORT;
    private DatagramPacket packet;
    private DatagramSocket socket;
    private byte buffer[];
    public Logger logger;

    public BroadcastingThread(String BROADCAST_IP,int PORT){
        this.BROADCAST_IP = BROADCAST_IP;
        this.PORT = PORT;
        logger = new Logger("DataReader","broadcast.txt");
    }

    @Override
    public void run(){
        try{
            socket = new DatagramSocket();
            socket.setBroadcast(true);
            buffer = BROADCAST_IP.getBytes();
            packet = new DatagramPacket(buffer,buffer.length,InetAddress.getByName(BROADCAST_IP),PORT);
            socket.send(packet);
            logger.d("UDP Broadcast Sent At "+System.currentTimeMillis());
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

}