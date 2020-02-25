package me.connect.sdk.java;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends BaseActivity {

    public static final String TAG = "MainActivity";

    private boolean sdkInited = false;
    private ConnectMeVcx sdkApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Init the sdkApi
        sdkApi = new ConnectMeVcx(this);
        sdkApi.init();
    }

    public void addConnectionOnClick(View v) {
        EditText editText   = (EditText)findViewById(R.id.editText2);
        String invitationDetails = editText.getText().toString();
        Log.d(TAG, "connection invitation is set to: " + invitationDetails);

        try {
            JSONObject json = new JSONObject(invitationDetails);
            sdkApi.createConnectionWithInvite(json.getString("id"), invitationDetails, new CompletableFuturePromise<>(connectionHandle -> {
                Log.e(TAG, "createConnectionWithInvite return code is: " + connectionHandle);
                if(connectionHandle != -1) {
                    sdkApi.vcxAcceptInvitation(connectionHandle, "{\"connection_type\":\"QR\",\"phone\":\"\"}", new CompletableFuturePromise<>(inviteDetails -> {
                        Log.e(TAG, "vcxAcceptInvitation return code is: " + inviteDetails);
                        if(invitationDetails != null) {
                            sdkApi.getSerializedConnection(connectionHandle, new CompletableFuturePromise<>(state -> {
                                Log.e(TAG, "getSerializedConnection returned state is: " + state);
                            }, (t) -> {
                                Log.e(TAG, "getSerializedConnection error is: ", t);
                                return null;
                            }));
                        }
                    }, (t) -> {
                        Log.e(TAG, "vcxAcceptInvitation error is: ", t);
                        return null;
                    }));
                }
            }, (t) -> {
                Log.e(TAG, "createConnectionWithInvite error is: ", t);
                return -1;
            }));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case VcxStaticData.REQUEST_WRITE_EXTERNAL_STORAGE: {
                if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    VcxStaticData.initLoggerFile(this);
                    Toast.makeText(this,
                            "File system access allowed",
                            Toast.LENGTH_SHORT).show();

                    // resolve the promise for file system access
                    VcxStaticData.resolveLoggerPromise(VcxStaticData.LOG_FILE_PATH);
                } else {
                    Toast.makeText(this,
                            "File system access NOT allowed",
                            Toast.LENGTH_SHORT).show();

                    // reject the promise for file system access
                    VcxStaticData.rejectLoggerPromise("ERR-104", "File system access NOT allowed");
                }
                return;
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

}
