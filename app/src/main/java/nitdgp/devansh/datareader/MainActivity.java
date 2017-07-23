package nitdgp.devansh.datareader;

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
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;

public class MainActivity extends AppCompatActivity implements SensorEventListener,LocationListener{

    protected SensorManager mSensorManager;
    protected Sensor mAccSensor;
    protected LocationManager mLocationManager;
    protected Location bestKnown;
    protected TextView accelerometerText;
    protected TextView gpsText;
    protected Button gpsTag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if ( ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {
            return;
        }
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,this);
        accelerometerText = (TextView) findViewById(R.id.accText);
        gpsText = (TextView) findViewById(R.id.gpsText);
        gpsTag = (Button) findViewById(R.id.gpsTag);
        Location lastknown = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if(lastknown!=null) {
            String text = "Latitude = " + lastknown.getLatitude() + ",Longitude = " + lastknown.getLongitude();
            gpsText.setText(text);
            bestKnown = lastknown;
        }
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
        if(sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            String acctext = "X = " + event.values[0] + ", Y = " + event.values[1] + ", Z = " + event.values[2];
            accelerometerText.setText(acctext);
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
        mSensorManager.registerListener(this,mAccSensor,SensorManager.SENSOR_DELAY_NORMAL*2,SensorManager.SENSOR_DELAY_NORMAL*2);
        try{
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,this);
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
