package nitdgp.devansh.datareader;

/**
 * Created by devansh on 25/7/17.
 */

import android.Manifest;;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;

public class MainActivity extends AppCompatActivity implements SensorEventListener,LocationListener{

    protected SensorManager mSensorManager;
    protected Sensor mAccSensor;
    protected LocationManager mLocationManager;
    protected Location bestKnown;
    protected TextView gpsText;
    protected Button gpsTag;
    protected ProgressBar progressBarX;
    protected ProgressBar progressBarY;
    protected ProgressBar progressBarZ;
    private final int thresholdAccY = 50;
    public ListenerThread listenerThread;
    public BroadcastingThread broadcastingThread;
    public Thread broadcastThread;
    public Logger logger;
    public Logger loggerBroadcast;
    public Logger loggerReceive;
    public final long TIME_INTERVAL = 1000;
    public long LAST_BROADCAST;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[] {
                        Manifest.permission.INTERNET,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.CAMERA,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                },
                100);
        try {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,0,0,this);
        }
        catch (SecurityException e){
            e.printStackTrace();
        }
        gpsText = (TextView) findViewById(R.id.gpsText);
        gpsTag = (Button) findViewById(R.id.gpsTag);
        progressBarX = (ProgressBar) findViewById(R.id.progressBarX);
        progressBarX.setMax(100);
        progressBarY = (ProgressBar) findViewById(R.id.progressBarY);
        progressBarY.setMax(100);
        progressBarZ = (ProgressBar) findViewById(R.id.progressBarZ);
        progressBarZ.setMax(100);
        Location lastKnown=null;
        try {
            lastKnown = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }
        catch (SecurityException e){
            e.printStackTrace();
        }

        if(lastKnown!=null) {
            String text = "Latitude = " + lastKnown.getLatitude() + ",Longitude = " + lastKnown.getLongitude();
            gpsText.setText(text);
            bestKnown = lastKnown;
        }
        LAST_BROADCAST = System.currentTimeMillis();
        logger = new Logger("DataReader");
        loggerBroadcast = new Logger("DataReader","broadcast.txt");
        loggerReceive = new Logger("DataReader","receiver.txt");
        listenerThread = new ListenerThread(loggerReceive,getApplicationContext());
        listenerThread.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void onRequestPermissionsResult(int requestcode,String[] perms,int[] grantResults){
        if(requestcode==100){
            if(grantResults.length>0){
                for(int i=0;i<grantResults.length;i++){
                    if(grantResults[i]!=PackageManager.PERMISSION_GRANTED){
                        Toast.makeText(this,"Permission Denied",Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
            }
        }
    }

    public void startAcc(View v){
        mSensorManager.registerListener(this,mAccSensor,SensorManager.SENSOR_DELAY_NORMAL*20,SensorManager.SENSOR_DELAY_NORMAL*20);
        logger.revive();
    }

    public void stopAcc(View v){
        mSensorManager.unregisterListener(this,mAccSensor);
        progressBarX.setProgress(0);
        progressBarY.setProgress(0);
        progressBarZ.setProgress(0);
        logger.close();
    }

    public void startCamera(View v){
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        String folder_path = Environment.getExternalStorageDirectory() + "/DataReader";
        File folder  = new File(folder_path);
        if(!folder.exists()){
            File newDirectory = new File(folder_path);
            newDirectory.mkdir();
        }
        if(bestKnown==null){
            Toast.makeText(this,"Wait for GPS",Toast.LENGTH_SHORT).show();
            return;
        }
        String name = bestKnown.getLatitude()+","+bestKnown.getLongitude()+".png";
        File filename = new File(folder,name);
        Uri imgUri = Uri.fromFile(filename);
        try{
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,imgUri);
            startActivityForResult(cameraIntent,1);
        }
        catch (SecurityException e){
            e.printStackTrace();
        }
    }

    protected void onActivityResult(int requestcode,int resultcode,Intent intent){
        if(requestcode==1 && resultcode==RESULT_OK){
            Toast.makeText(this,"Image successfully captured",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event){
        Sensor sensor = event.sensor;
        long timestamp  = System.currentTimeMillis();
        if(sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            progressBarX.setProgress((int)(event.values[0]*10));
            progressBarY.setProgress((int)(event.values[1]*10));
            progressBarZ.setProgress((int)(event.values[2]*10));
            if(thresholdAccY<=event.values[1]*10) {
                if((System.currentTimeMillis() - LAST_BROADCAST)>TIME_INTERVAL) {
                    broadcastingThread = new BroadcastingThread(loggerBroadcast,"192.168.43.255",8080);
                    broadcastingThread.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,bestKnown);
                    LAST_BROADCAST = System.currentTimeMillis();
                }
                String logwrite = timestamp + " : X = " + event.values[0] + ", Y = " + event.values[1] + ", Z = " + event.values[2];
                logger.d(logwrite);
            }
        }
    }

    @Override
    public void onLocationChanged(Location location){
        String text = "Latitude = "+location.getLatitude() + ",Longitude = "+location.getLongitude();
        gpsText.setText(text);
        bestKnown = location;
    }

    @Override
    protected void onPause(){
        super.onPause();
        mSensorManager.unregisterListener(this,mAccSensor);
        try {
            mLocationManager.removeUpdates(this);
        }
        catch (SecurityException e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        progressBarX.setProgress(0);
        progressBarY.setProgress(0);
        progressBarZ.setProgress(0);
        try{
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,this);
            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,0,0,this);
        }
        catch (SecurityException e){
            e.printStackTrace();
        }
    }

    @Override
    public  void onAccuracyChanged(Sensor arg0, int arg1){

    }

    @Override
    public void onProviderEnabled(String s){

    }

    @Override
    public void onProviderDisabled(String s){
    }

    @Override
    public void onStatusChanged(String s,int i, Bundle bundle){

    }

}
