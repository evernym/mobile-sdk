# Android

## Integration

- Add below line to top level `build.gradle` file's `repositories` section

  ```gradle
  maven { url 'https://mobile-sdk.jumio.com' }
  ```

- Apply plugin

  ```gradle
  `apply plugin: 'kotlin-parcelize'`
  ```

- If you are using Jetifier, then due to a bug in the Jetifier, the Bouncycastle library needs to be added to the Jetifier ignore list in the `gradle.properties`

  ```gradle
  android.jetifier.blacklist=bcprov-jdk15on
  ```

- Add the SDK in the dependencies. Add below lines in your app's `build.gradle` file

  <details>
    <summary>Add code inside build.gradle</summary>

    ```gradle
    implementation 'com.mastercard.dis.mids:base:2.1.0@aar'
    implementation 'com.mastercard.dis.mids:verification:2.1.0@aar'
    implementation "com.jumio.android:core:3.9.1@aar"
    implementation "com.jumio.android:nv:3.9.1@aar"
    implementation "com.jumio.android:nv-mrz:3.9.1@aar"
    implementation "com.jumio.android:nv-ocr:3.9.1@aar"
    implementation "com.jumio.android:nv-nfc:3.9.1@aar"
    implementation "com.jumio.android:nv-barcode:3.9.1@aar"
    implementation "com.jumio.android:iproov:3.9.1@aar"

    implementation ("com.iproov.sdk:iproov:6.3.1"){
      exclude group: 'org.json', module:'json'
    }

    implementation ("io.socket:socket.io-client:0.9.0") {
            exclude group: 'org.json', module: 'json'
        }

    implementation "org.jmrtd:jmrtd:0.7.24"
    implementation "org.bouncycastle:bcprov-jdk15on:1.67"

    implementation "org.jetbrains.kotlinx:kotlinx-serialization-core:1.0.0"
    implementation "org.jetbrains.kotlinx:kotlinx-serialization-json:1.0.0"

    implementation "androidx.appcompat:appcompat:1.2.0"
    implementation "androidx.cardview:cardview:1.0.0"
    implementation "androidx.room:room-runtime:2.2.6"
    implementation "androidx.constraintlayout:constraintlayout:2.0.4"
    implementation "androidx.core:core-ktx:1.3.2"

    implementation "com.google.android.material:material:1.2.1"
    ```

  </details>

## Get SDK Token

We need to send an API call to get SDK from Evernym backend issuer service.

  <details>
    <summary>See code</summary>

```java
    // using OkHttp library
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    OkHttpClient client = new OkHttpClient();
    String json = "{}";
    RequestBody body = RequestBody.create(JSON, json);
    String demoVerityFlowBaseUrl =
    'https://simple-verifier-backend.pps.evernym.com/Prod/issuer-service/get-mc-sdk-token'
    String prodVerityFlowBaseUrl =
    'https://simple-verifier-backend.evernym.com/Prod/issuer-service/get-mc-sdk-token'
    
    // We are using demo url for our development purpose
    Request request = new Request.Builder()
      .url(demoVerityFlowBaseUrl)
      .addHeader("Authorization", "Token token=<add-device-safetynet-attestation-here>")
      .addHeader("domainDID", "Put your domainDID that has been approved by Evernym")
      .post(body)
      .build();
    try (Response response = client.newCall(request).execute()) {
        return response.body().string();
    }
```

```javascript
    // We get JSON response in above call
    // the format is as below
    {
      result: '{"sdkToken":"<sometokenvalue>","apiDataCenter":"SG"}'
    }
```

  </details>

### Handle errors while getting sdk token

In case API call fails, we get 500 as status code and below in response body

```javascript
  {
    body: '{"errorMessage":"{\"error\":\"{\"data\":\"{\"Errors\":[]}\"}\",\"message\":\"error messages\"}"}'
  }
```

## Init SDK with token

Once we have the token from above API call. We can now init the SDK on app side.

  <details>
    <summary>See code</summary>

```java

  import com.mastercard.dis.mids.base.verification.MIDSVerificationBaseManager;
  import com.mastercard.dis.mids.base.verification.data.enumeration.MIDSDocumentType;
  import com.mastercard.dis.mids.base.verification.data.enumeration.MIDSDocumentVariant;
  import com.mastercard.dis.mids.base.verification.data.enumeration.MIDSScanSide;
  import com.mastercard.dis.mids.base.verification.data.model.MIDSVerificationResponse;
  import com.mastercard.dis.mids.base.verification.data.presenter.MIDSVerificationScanPresenter;
  import com.mastercard.dis.mids.base.verification.enrollment.MIDSEnrollmentManager;
  import com.mastercard.dis.mids.base.verification.data.listener.IMidsVerificationListener;
  import com.mastercard.dis.mids.base.verification.data.model.MIDSVerificationError;
  import com.mastercard.dis.mids.base.verification.data.model.MIDSCountry;
  import com.mastercard.dis.mids.base.verification.data.enumeration.MIDSDataCenter;
  import com.mastercard.dis.mids.base.verification.views.MIDSVerificationScanView;
  import com.mastercard.dis.mids.base.verification.views.MIDSVerificationConfirmationView;
  import com.mastercard.dis.mids.base.verification.data.listener.IMidsVerificationScanListener;

  private MIDSEnrollmentManager sdkManager = null;
  private ArrayList<MIDSScanSide> scanSidesDV = new ArrayList<MIDSScanSide>();
  private MIDSCountry selectedCountry = null;
  private MIDSVerificationConfirmationView midsVerificationConfirmationView = null;
  private MIDSVerificationScanView midsVerificationScanView = null;
  private IMidsVerificationScanListener scanListener = null;
  private MIDSVerificationScanPresenter presenter = null;
  private int sideIndex = 0;

  private MIDSEnrollmentManager getEnrollmentManagerInstance() {
    if (sdkManager == null) {
      System.out.println("getEnrollmentManagerInstance");
      // check listener class details below in success or fail section
      sdkManager = new MIDSEnrollmentManager(new EnrollmentSDKListener());
    }
    return sdkManager;
  }

  private MIDSDataCenter getDataCenter(String dataCenter) {
    for (MIDSDataCenter data : MIDSDataCenter.values()) {
      if (data.name().equals(dataCenter)) {
        return data;
      }
    }
    return MIDSDataCenter.SG;
  }

  private void requestPermissionsForSDK(Activity currentActivity, Context currentContext) {
    MIDSVerificationBaseManager.requestSDKPermissions(currentActivity, PERMISSIONS.ENROLLMENT_PERMISSION);
    MIDSVerificationBaseManager.requestSDKPermissions(currentActivity, PERMISSIONS.ENROLLMENT_SCAN_PERMISSION);
    MIDSVerificationBaseManager.requestSDKPermissions(currentActivity, PERMISSIONS.AUTHENTICATION_PERMISSION);
    while (!MIDSVerificationBaseManager.hasAllRequiredPermissions(currentContext)) {
      try {
        Thread.sleep(5000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }

  private void resetSdk() {
    sdkManager = null;
    scanSidesDV = new ArrayList<MIDSScanSide>();
    selectedCountry = null;
    scanListener = null;
    presenter = null;
    sideIndex = 0;
  }

  // this is the method that inits the SDK
  public void initMIDSSDK(String token, String withDataCenter) {
    // just to show on how to request permission using current activity
    // You can use your own code for requesting permission
    Activity currentActivity = reactContext.getCurrentActivity();
    Context currentContext = reactContext.getApplicationContext();
    
    // data center is passed as a string from API call. We need to convert it to MIDSDataCenter type
    MIDSDataCenter dataCanter = getDataCenter(withDataCenter);

    // get all required permissions
    requestPermissionsForSDK(currentActivity, currentContext);

    // We need to create a singleton and then call initializeSDK for that singleton
    getEnrollmentManagerInstance().initializeSDK(currentActivity, token, dataCanter);
  }

```

  </details>

## Handle success or error of SDK init

Once we have called the SDK method. The success or error are sent to listeners attached on the class. These listeners are need to be created by us. We can add below listeners to above class.

```java

  private class EnrollmentSDKListener implements IMidsVerificationListener {

    @Override
    public void onError(@NotNull MIDSVerificationError error) {
      System.out.println("EnrollmentSDKListener - method: onError - error: " + error.getMessage().toString()
          + MIDSVerificationError.SDK_USER_CANCELLED);
    }

    @Override
    public void onInitializedSuccessfully() {
      System.out.println("EnrollmentSDKListener - method: onInitializedSuccessfully");
    }
  }

```

## Get countries list

Once we know that initialization was success. We can get the supported countries list to show to the user.

```java
  public void getCountryList(Callback resolve, Callback reject) {
    try {
      List<MIDSCountry> countryList = getEnrollmentManagerInstance().getCountryList().getResponse();
      JSONObject countries = new JSONObject();
      if (countryList != null) {
        for (MIDSCountry country : countryList) {
          String countryName = country.getName();
          String countryCode = country.getIsoCode();
          countries.put(countryName, countryCode);
        }
      }
      resolve.invoke(countries.toString());
    } catch (JSONException ignored) {
      reject.invoke();
    }
  }
```

## Get Document as per selected country

Once we show to the user the selected country. And user selects a country. We need to get a list of available documents to scan for this particular country.

```java
  public void getDocumentTypes(String countryCode, Callback resolve, Callback reject) {
    try {
      MIDSCountry code = new MIDSCountry(countryCode);
      this.selectedCountry = code;
      MIDSVerificationResponse<List<MIDSDocumentType>> documentTypeResponse = getEnrollmentManagerInstance()
          .getDocumentTypes(code);
      List<MIDSDocumentType> documentType = documentTypeResponse.getResponse();
      resolve.invoke(documentType.toString());
    } catch (Exception ignored) {
      reject.invoke();
    }
  }
```

### Handle empty document list per selected country

There can be a case where there is no supported document for that country to scan. Please show appropriate error to the user.

## Start scan process

Now when user selects the document that user wants to scan. We can now start scanning process. To start scanning process we need to do the following.

- We need to add another method inside `EnrollmentSDKListener` class.
  
  ```java
    private class EnrollmentSDKListener implements IMidsVerificationListener {

      @Override
      public void onSDKConfigured(@NotNull List<? extends MIDSScanSide> scanSides) {
        scanSidesDV.clear();
        scanSidesDV.addAll(scanSides);

        System.out.println("EnrollmentSDKListener - method: onSDKConfigured - scan sides: " + scanSidesDV);

        // implementation detail of this method is given in section for ### Handle and configure scan screen
        scanning();
      }
    }

  ```

- Call below method to start scan

  ```java
    // documentType is the value of the document that user selected as a String. Below is the implementation for getting Type of that selected document
    MIDSDocumentType type = getMIDSDocumentTypeFromString(documentType);
    System.out.println("start MIDS SDK Scan - type: " + type + " selected country: " + selectedCountry);
    getEnrollmentManagerInstance().startScan(selectedCountry, type, MIDSDocumentVariant.PLASTIC);


    ...

    private MIDSDocumentType getMIDSDocumentTypeFromString(String documentType) {
      if (documentType.equals("Passport")) {
        return MIDSDocumentType.PASSPORT;
      } else if (documentType.equals("Driver's license")) {
        return MIDSDocumentType.DRIVING_LICENSE;
      } else if (documentType.equals("Identity card")) {
        return MIDSDocumentType.IDENTITY_CARD;
      } else if (documentType.equals("Visa")) {
        return MIDSDocumentType.VISA;
      }
      return MIDSDocumentType.PASSPORT;
    }

  ```

### Configure SDK for different events on scan screen

  <details>
    <summary>See code</summary>

```java
    private class ScanListener implements IMidsVerificationScanListener {

      @Override
      public void onCameraAvailable() {
        System.out.println("ScanListener - method: onCameraAvailable ");
        presenter.resume();
      }

      @Override
      public void onProcessStarted() {
        System.out.println("ScanListener - method: onProcessStarted ");
      }

      @Override
      public void onDocumentCaptured() {
        System.out.println("ScanListener - method: onDocumentCaptured ");
      }

      @Override
      public void onError(MIDSVerificationError error) {
        if (error == MIDSVerificationError.PRESENTER_ERROR_SHOW_BLUR_HINT) {
          System.out.println("ScanListener - method: onError - error: " + error.getMessage());
        } else {
          System.out.println("ScanListener - method: onError - error: " + error.getMessage());
          presenter.destroy();
        }
      }

      @Override
      public void onPreparingScan() {
        System.out.println("ScanListener - method: onPreparingScan ");
      }

      @Override
      public void onProcessCancelled(MIDSVerificationError error) {
        System.out.println("ScanListener - method: onProcessCancelled - error: " + error.getMessage().toString());

        if (error == MIDSVerificationError.PRESENTER_ERROR_GENERIC_ERROR) {
          showModal();
        }
      }

      @Override
      public void onProcessFinished(MIDSScanSide scanSide, boolean allPartsScanned) {
        System.out.println("ScanListener - method: onProcessFinished - scan side: " + scanSide
            + " - is all parts scanned: " + allPartsScanned);
        presenter.destroy();
        presenter = null;

        if (allPartsScanned) {
          // you can raise events or tell your code that scanning is done from SDK side
          getEnrollmentManagerInstance().endScan();
        }
      }
    }
```

  </details>

### Handle and configure scan screen

We need to control and dictate on how the scan screen needs to behave. Below is the detail for handling scan events and scan screen.

  <details>
    <summary>See code</summary>

```java
  private void scanning() {
    MIDSScanSide currentScanSide = scanSidesDV.get(sideIndex);
    System.out.println("ScanListener currentScanSide" + currentScanSide);

    if (currentScanSide == MIDSScanSide.FACE) {
      System.out.println("ScanListener scanningFaceSide");

      Activity currentActivity = reactContext.getCurrentActivity();
      if (currentActivity != null) {
        LayoutInflater inflater = currentActivity.getLayoutInflater();
        ViewGroup viewGroup = (ViewGroup) ((ViewGroup) currentActivity.findViewById(android.R.id.content))
          .getRootView();
        View view = inflater.inflate(R.layout.fragment_scan, viewGroup, true);

        this.midsVerificationScanView = (MIDSVerificationScanView) view.findViewById(R.id.sv_scan);
        this.midsVerificationConfirmationView = (MIDSVerificationConfirmationView) view.findViewById(R.id.cv_scan);

        this.scanListener = new ScanListener();
        this.midsVerificationScanView.setMode(MIDSVerificationScanView.MODE_FACE);

        MIDSVerificationResponse<MIDSVerificationScanPresenter> presenterResponse = getEnrollmentManagerInstance()
            .getPresenter(MIDSScanSide.FACE, this.midsVerificationScanView, this.midsVerificationConfirmationView,
                this.scanListener);

        MIDSVerificationError error = presenterResponse.getError();
        if (error != null) {
          System.out.println("MIDSVerificationError" + error.getMessage().toString());
        }

        presenter = presenterResponse.response;

        if (presenter != null) {
          System.out.println("VerificationScanPresenter - startScan" + presenter.getHelpText());
          presenter.startScan();
        } else {
          System.out.println("Scan error");
        }
      } else {
        System.out.println("Inflate scan fragment error");
      }
      return;
    } else if (currentScanSide == MIDSScanSide.FRONT || currentScanSide == MIDSScanSide.BACK) {
      Activity activity = getCurrentActivity();
      FragmentTransaction ft = activity.getFragmentManager().beginTransaction();
      Fragment prev = activity.getFragmentManager().findFragmentByTag(SCAN_DIALOG);
      if (prev != null) {
        ft.remove(prev);
      }
      ft.addToBackStack(null);

      MIDSScanFragment newFragment = MIDSScanFragment.newInstance();
      newFragment.setMIDSEnrollmentManager(getEnrollmentManagerInstance());
      newFragment.setMIDSScanSide(currentScanSide);

      newFragment.show(ft, SCAN_DIALOG);
      activity.getFragmentManager().executePendingTransactions();
      newFragment.setDismissListener(new DismissListener() {
        @Override
        public void onDismiss(boolean isDestroy) {
          if (isDestroy) {
            getEnrollmentManagerInstance().endScan();
            getEnrollmentManagerInstance().terminateSDK();
            reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit("DESTROY",
                Arguments.createMap());
          } else if (sideIndex <= scanSidesDV.size() - 1) {
            sideIndex = sideIndex + 1;
            System.out.println("ScanListener onDismiss" + sideIndex + scanSidesDV.get(sideIndex));
            scanning();
          }
        }
      });
    }
  }
```

  </details>

Implementation for MIDSScanFragment is as below

  <details>
    <summary>See code</summary>

```java
  package com.evernym.sdk.reactnative.mids;

  import android.app.AlertDialog;
  import android.app.Dialog;
  import android.app.DialogFragment;
  import android.content.DialogInterface;
  import android.content.res.ColorStateList;
  import android.graphics.Color;
  import android.os.Bundle;
  import android.view.LayoutInflater;
  import android.view.View;
  import android.view.ViewGroup;
  import android.widget.Button;
  import android.widget.TextView;

  import androidx.annotation.NonNull;
  import androidx.annotation.Nullable;

  import com.mastercard.dis.mids.base.verification.data.enumeration.MIDSScanSide;
  import com.mastercard.dis.mids.base.verification.data.listener.IMidsVerificationScanListener;
  import com.mastercard.dis.mids.base.verification.data.model.MIDSVerificationError;
  import com.mastercard.dis.mids.base.verification.data.model.MIDSVerificationResponse;
  import com.mastercard.dis.mids.base.verification.data.presenter.MIDSVerificationScanPresenter;
  import com.mastercard.dis.mids.base.verification.enrollment.MIDSEnrollmentManager;
  import com.mastercard.dis.mids.base.verification.views.MIDSVerificationConfirmationView;
  import com.mastercard.dis.mids.base.verification.views.MIDSVerificationScanView;

  import com.evernym.sdk.reactnative.R;

  import static com.facebook.react.bridge.UiThreadUtil.runOnUiThread;

  public class MIDSScanFragment extends DialogFragment {
      private MIDSEnrollmentManager midsEnrollmentManager;
      private MIDSScanSide midsScanSide;
      private MIDSVerificationConfirmationView midsVerificationConfirmationView = null;
      private MIDSVerificationScanView midsVerificationScanView = null;
      private IMidsVerificationScanListener scanListener = null;
      private MIDSVerificationScanPresenter presenter = null;

      private Button continueButton;
      private Button retryButton;
      private Button closeButton;
      private TextView title;
      private TextView subtitle;

      private DismissListener listener = null;
      private boolean isDestroy = false;

      public static MIDSScanFragment newInstance() {
          return new MIDSScanFragment();
      }

      public void setDismissListener(DismissListener listener) {
          this.listener = listener;
      }

      @Override
      public void onDismiss(DialogInterface dialog) {
          super.onDismiss(dialog);
          if (listener != null) {
              listener.onDismiss(isDestroy);
          }
      }

      @Override
      public void onStart()
      {
          super.onStart();
          Dialog dialog = getDialog();
          if (dialog != null)
          {
              int width = ViewGroup.LayoutParams.MATCH_PARENT;
              int height = ViewGroup.LayoutParams.MATCH_PARENT;
              dialog.getWindow().setLayout(width, height);
          }
      }

      @Nullable
      @Override
      public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                                        @Nullable Bundle savedInstanceState) {
          View view = inflater.inflate(R.layout.fragment_scan, container, false);
          midsVerificationScanView = (MIDSVerificationScanView) view.findViewById(R.id.sv_scan);
          midsVerificationConfirmationView = (MIDSVerificationConfirmationView) view.findViewById(R.id.cv_scan);
          continueButton = (Button) view.findViewById(R.id.btn_scan_continue);
          retryButton = (Button) view.findViewById(R.id.btn_scan_retry);
          closeButton = (Button) view.findViewById(R.id.btn_scan_cancel);
          title = (TextView) view.findViewById(R.id.btn_scan_title);
          subtitle = (TextView) view.findViewById(R.id.btn_scan_subtitle);

          title.setTextColor(Color.rgb(165, 165, 165));
          title.setText("Good job");
          subtitle.setTextColor(Color.rgb(119, 119, 119));
          subtitle.setText("If your document has two sides, flip it over and press continue to scan the back. Otherwise just press continue.");

          continueButton.setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View view) {
                  if (presenter != null) {
                      presenter.confirmScan();
                  }
              }
          });
          continueButton.setVisibility(View.INVISIBLE);

          retryButton.setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View view) {
                  midsVerificationConfirmationView.setVisibility(View.INVISIBLE);
                  midsVerificationScanView.setVisibility(View.VISIBLE);
                  closeButton.setBackgroundTintList(ColorStateList.valueOf(Color.rgb(255, 255, 255)));
                  continueButton.setVisibility(View.INVISIBLE);
                  retryButton.setVisibility(View.INVISIBLE);
                  if (presenter != null) {
                      presenter.retryScan();
                  }
              }
          });
          retryButton.setVisibility(View.INVISIBLE);

          closeButton.setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View view) {
                  isDestroy = true;
                  if (presenter != null) {
                      presenter.destroy();
                  }
                  onDestroyView();
              }
          });
          closeButton.setVisibility(View.VISIBLE);
          closeButton.setBackgroundTintList(ColorStateList.valueOf(Color.rgb(255, 255, 255)));

          if (this.midsScanSide == MIDSScanSide.FACE) {
              midsVerificationScanView.setMode(MIDSVerificationScanView.MODE_FACE);
          } else {
              midsVerificationScanView.setMode(MIDSVerificationScanView.MODE_ID);
          }
          scanListener = new ScanListener();

          MIDSVerificationResponse<MIDSVerificationScanPresenter> presenterResponse = midsEnrollmentManager.getPresenter(
              midsScanSide,
              midsVerificationScanView,
              midsVerificationConfirmationView,
              scanListener
          );

          MIDSVerificationError error = presenterResponse.getError();
          if (error != null) {
              System.out.println("MIDSVerificationError" + error.getMessage().toString());
          }

          presenter = presenterResponse.response;
          return view;
      }

      @Override
      public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
          super.onViewCreated(view, savedInstanceState);

          if (presenter != null) {
              midsVerificationScanView.setVisibility(View.VISIBLE);
              presenter.startScan();
          } else {
              System.out.println("Scan error");
          }
      }

      public void setMIDSEnrollmentManager(MIDSEnrollmentManager midsEnrollmentManager) {
          this.midsEnrollmentManager = midsEnrollmentManager;
      }

      public void setMIDSScanSide(MIDSScanSide midsScanSide) {
          this.midsScanSide = midsScanSide;
      }

      private void showModal() {
          runOnUiThread(new Runnable() {
              @Override
              public void run() {
                  new AlertDialog.Builder(getActivity())
                      .setTitle("Сouldn't recognize document. Please try again...")
                      .setPositiveButton("Retry scan", new DialogInterface.OnClickListener() {
                          public void onClick(DialogInterface dialog, int which) {
                              presenter.retryScan();
                          }
                      })
                      .setNegativeButton("Finish scan", new DialogInterface.OnClickListener() {
                          public void onClick(DialogInterface dialog, int which) {
                              isDestroy = true;
                              presenter.destroy();
                              presenter = null;
                              continueButton.setVisibility(View.INVISIBLE);
                              retryButton.setVisibility(View.VISIBLE);
                              onDestroyView();
                          }
                      })
                      .setIcon(android.R.drawable.ic_dialog_alert)
                      .show();
              }
          });
      }

      private class ScanListener implements IMidsVerificationScanListener {

          @Override
          public void onCameraAvailable() {
              System.out.println("ScanListener - method: onCameraAvailable ");
              presenter.resume();
          }

          @Override
          public void onProcessStarted() {
              System.out.println("ScanListener - method: onProcessStarted ");
          }

          @Override
          public void onDocumentCaptured() {
              System.out.println("ScanListener - method: onDocumentCaptured ");
              title.setVisibility(View.VISIBLE);
              subtitle.setVisibility(View.VISIBLE);
              continueButton.setVisibility(View.VISIBLE);
              retryButton.setVisibility(View.VISIBLE);
              midsVerificationConfirmationView.setVisibility(View.VISIBLE);
              midsVerificationScanView.setVisibility(View.INVISIBLE);
              closeButton.setBackgroundTintList(ColorStateList.valueOf(Color.rgb(119, 119, 119)));
          }

          @Override
          public void onError(MIDSVerificationError error) {
              if (error == MIDSVerificationError.PRESENTER_ERROR_SHOW_BLUR_HINT) {
                  System.out.println("ScanListener - method: onError - error: " + error.getMessage());
              } else {
                  System.out.println("ScanListener - method: onError - error: " + error.getMessage());
                  presenter.destroy();
              }
          }

          @Override
          public void onPreparingScan() {
              System.out.println("ScanListener - method: onPreparingScan ");
          }

          @Override
          public void onProcessCancelled(MIDSVerificationError error) {
              System.out.println("ScanListener - method: onProcessCancelled - error: " + error.getMessage().toString());
              if (error == MIDSVerificationError.PRESENTER_ERROR_GENERIC_ERROR) {
                  showModal();
              }
          }

          @Override
          public void onProcessFinished(MIDSScanSide scanSide, boolean allPartsScanned) {
              System.out.println("ScanListener - method: onProcessFinished - scan side: " + scanSide + " - is all parts scanned: " + allPartsScanned);
              presenter.destroy();
              presenter = null;
              continueButton.setVisibility(View.INVISIBLE);
              retryButton.setVisibility(View.VISIBLE);
              onDestroyView();
          }
      }
  }

```

  </details>

## Get workflow ID after successful document scan

Once the process for scan is finished. The document is being processed and SDK returns the workflow ID that can be used to get the credential.

Add below code to `EnrollmentSDKListener`

```java

  private class EnrollmentSDKListener implements IMidsVerificationListener {
    
    ...

    @Override
    public void onVerificationFinished(@NotNull String workflowId) {
      System.out
          .println("EnrollmentSDKListener - method: onVerificationFinished - workflowId: " + workflowId);

      getEnrollmentManagerInstance().endScan();
      getEnrollmentManagerInstance().terminateSDK();
      
      // use this "workflowId" to send to server to get the credential issued

      resetSdk();
    }
  }
```

## Make a connection with server

- First we need to make connection with Evernym server. This process is same as defined in [Connections](./../5.Connections.md) documentation.
- Below is the detail for the API that can be used to download connection invitation from Evernym server without user interaction. This step can be done before starting the SDK or in the background to avoid spending time establishing connection after user is done scanning. If we establish connection in background, then user only to needs to wait while we send workflowID to Evernym server.

```java
  String demoVerityFlowBaseUrl =
  'https://simple-verifier-backend.pps.evernym.com/Prod/issuer-service/create-invitation'
  String prodVerityFlowBaseUrl =
  'https://simple-verifier-backend.evernym.com/Prod/issuer-service/create-invitation'
  
  // Send post request to one of the above URL
  // The headers needs to be the same as we sent in API call to get SDK token, refer above steps

  // Handle invitation and establish connection as described in Connections documentation

```

## Send workflow ID to server to get credential for the scanned document

Once the connection is established, now we can send the workflow ID to server and get the credential issued. Before sending the workflow ID, we need to make sure that we have created a credDefId for the credential that we want to issue to our users. Follow verity guidelines to get domainDID, and creating credential definitions.

Below code assumes that we know credDefIds.

```java

  String demoVerityFlowBaseUrl =
  'https://simple-verifier-backend.pps.evernym.com/Prod/issuer-service/issue-credential'
  String prodVerityFlowBaseUrl =
  'https://simple-verifier-backend.evernym.com/Prod/issuer-service/issue-credential'
  
  // Send post request to one of the above URL
  // The headers needs to be the same as we sent in API call to get SDK token, refer above steps

  // In previous requests, our body was empty JSON "{}". For this API call, we need below JSON as body

  JSON.stringify({
    workflowId,
    connectionDID,
    country,
    // document name string "PASSPORT" | "DRIVING_LICENSE" | "IDENTITY_CARD" | "VISA"
    document,
    credDefId,
    // this is the safetyNet attestation token
    hardwareToken,
    platform: "android",
  })

```

### Handle credential offer message

Once above API call is successful. We can check for credential offer message as described in [Messages](./../4.MessagesFlow.md).
Once we get credential offer message, then we can accept the credential offer and show to user as described in [Credentials](./../6.Credentials.md).
