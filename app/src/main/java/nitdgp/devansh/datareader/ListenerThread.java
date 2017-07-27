package nitdgp.devansh.datareader;

/**
 * Created by devansh on 25/7/17.
 */

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class ListenerThread extends AsyncTask<Void,String,String>{
    private DatagramPacket packet;
    private DatagramSocket socket;
    private byte buffer[];
    private boolean isRunning;
    public Logger logger;
    public Context context;

    public ListenerThread(Logger logger,Context context){
        isRunning = false;
        this.logger = logger;
        this.context = context;
    }

    @Override
    protected String doInBackground(Void... params){
        try{
            socket = new DatagramSocket(8080, InetAddress.getByName("192.168.43.255"));
            socket.setBroadcast(true);
            isRunning = true;
            buffer = new byte[15000];
            packet = new DatagramPacket(buffer, buffer.length);
            while(isRunning) {
                socket.receive(packet);
                String receiver = new String(packet.getData()).trim();
                String arr[] = receiver.split("-");
                String senderLocation = arr[0];
                String eventType = arr[1];
                publishProgress(senderLocation,eventType);
                Thread.sleep(4000);
                logger.d("UDP Broadcast Received at " + System.currentTimeMillis());
            }
        }
        catch (Exception e){
            e.printStackTrace();
            isRunning = false;
        }
        return "EXIT";
    }

    @Override
    protected void onProgressUpdate(String...params){
        Toast.makeText(context,params[1]+" at "+params[0]+" !",Toast.LENGTH_LONG).show();
    }
}