package nitdgp.devansh.datareader;

/**
 * Created by devansh on 25/7/17.
 */

import android.os.Environment;
import android.os.SystemClock;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class Logger {
    private File logFile;
    private File folder;
    private FileOutputStream fileOutputStream;

    public Logger(String folder,String filename){
        try {
            this.folder = new File(Environment.getExternalStorageDirectory()+"/"+folder);
            if(!this.folder.exists()){
                this.folder.mkdir();
            }
            logFile = new File(Environment.getExternalStorageDirectory() + "/"+folder, filename);
            if(!logFile.exists()){
                logFile.createNewFile();
            }
            fileOutputStream = new FileOutputStream(logFile);
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    public Logger(String folder){
        try {
            this.folder = new File(Environment.getExternalStorageDirectory()+"/"+folder);
            if(!this.folder.exists()){
                this.folder.mkdir();
            }
            logFile = new File(Environment.getExternalStorageDirectory() + "/"+folder, "logFile.txt");
            if(!logFile.exists()){
                logFile.createNewFile();
            }
            fileOutputStream = new FileOutputStream(logFile);
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    public void d(String text){
        try{
            fileOutputStream.write(text.getBytes());
            fileOutputStream.write(System.getProperty("line.separator").getBytes());
            fileOutputStream.flush();
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    public void close(){
        try{
            fileOutputStream.close();
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }
}