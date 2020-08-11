package me.connect.sdk.java.sample;

import android.app.Activity;
import android.app.Application;

import com.getkeepsafe.relinker.ReLinker;

public class MainApplication extends Application {

    private Activity mCurrentActivity = null;

    public Activity getCurrentActivity(){
        return mCurrentActivity;
    }

    public void setCurrentActivity(Activity mCurrentActivity){
        this.mCurrentActivity = mCurrentActivity;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        ReLinker.loadLibrary(this, "vcx");
    }
}
