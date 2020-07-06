package me.connect.sdk.java;

import android.os.FileObserver;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class LogFileObserver extends FileObserver {
    public String absolutePath;
    public int MAX_ALLOWED_FILE_BYTES;
    public Process process;

    public LogFileObserver(String path, int MAX_ALLOWED_FILE_BYTES) {
        super(path, FileObserver.ALL_EVENTS);
        this.absolutePath = path;
        this.MAX_ALLOWED_FILE_BYTES = MAX_ALLOWED_FILE_BYTES;
        //this.startLogcat(this.absolutePath);
    }

    @Override
    public void onEvent(int event, String path) {

        //data was written to a file
        if ((FileObserver.MODIFY & event) != 0) {
            File logFile = new File(this.absolutePath);
            if(logFile.length() > MAX_ALLOWED_FILE_BYTES) {
                try {
                    new FileWriter(this.absolutePath).close();
                } catch(IOException ex) {
                    ex.printStackTrace();
                }
            }
        } else if((FileObserver.DELETE_SELF & event) != 0) {
            this.stopWatching();
            try {
                new FileWriter(this.absolutePath).close();
            } catch(IOException ex) {
                ex.printStackTrace();
            }
            this.startWatching();
        }
    }

    // public void stopLogcat() {
    //     this.process.destroy();
    // }

    // public void startLogcat(String filename) {
    //     try {
    //         String cmd = "logcat -f "+filename;
    //         this.process = Runtime.getRuntime().exec(cmd);
    //     } catch (IOException e) {
    //         e.printStackTrace();
    //     }
    // }
}