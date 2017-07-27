package nitdgp.devansh.datareader;

/**
 * Created by devansh on 25/7/17.
 */

import android.location.Location;
import android.os.AsyncTask;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class BroadcastingThread extends AsyncTask<Location,Void,String>{
    private String BROADCAST_IP;
    private int PORT;
    private DatagramPacket packet;
    private DatagramSocket socket;
    private byte buffer[];
    public Logger logger;

    public BroadcastingThread(Logger logger,String BROADCAST_IP,int PORT){
        this.BROADCAST_IP = BROADCAST_IP;
        this.PORT = PORT;
        this.logger = logger;
    }

    @Override
    protected String doInBackground(Location... params){
        try{
            socket = new DatagramSocket();
            socket.setBroadcast(true);
            buffer = (params[0].getLatitude()+","+params[0].getLongitude()).getBytes();
            packet = new DatagramPacket(buffer,buffer.length,InetAddress.getByName(BROADCAST_IP),PORT);
            socket.send(packet);
            logger.d("UDP Broadcast Sent At "+System.currentTimeMillis());
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return "EXIT";
    }

}