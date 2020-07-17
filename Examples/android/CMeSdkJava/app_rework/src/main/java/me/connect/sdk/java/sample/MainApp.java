package me.connect.sdk.java.sample;

import android.app.Application;

import me.connect.sdk.java.AgencyConfig;
import me.connect.sdk.java.ConnectMeVcx;

public class MainApp extends Application {
    private ConnectMeVcx sdk;
    @Override
    public void onCreate() {
        super.onCreate();
        sdk = ConnectMeVcx.builder()
                .withAgency(AgencyConfig.DEFAULT)
                .withGenesisPool(Constants.POOL_TXN_GENESIS)
                .withContext(this)
                .build();
        sdk.init();
    }
}
