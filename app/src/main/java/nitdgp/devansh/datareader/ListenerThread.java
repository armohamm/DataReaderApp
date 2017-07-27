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
                String senderLocation = new String(packet.getData()).trim();
                publishProgress(senderLocation);
                Thread.sleep(5000);
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
        Toast.makeText(context,"Pothole at "+params[0]+" !",Toast.LENGTH_LONG).show();
    }
}