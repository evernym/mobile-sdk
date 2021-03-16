package test.java.utility;


import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;

class DeviceInfo {

  private static DeviceInfo instance;

  private String UDID;
  private String name;

  private DeviceInfo() {
    readDeviceInfo();
  }

  public static DeviceInfo getInstance() {
    if (instance == null) {
      instance = new DeviceInfo();
    }
    return instance;
  }

  public String getUDID() {
    return UDID;
  }
  public String getName() {
    return name;
  }

  private void readDeviceInfo() {
    try {
      Process process = Runtime.getRuntime().exec("xcrun simctl list devices available -j");
      process.waitFor();

      StringBuilder output = new StringBuilder();

      BufferedReader reader = new BufferedReader(
        new InputStreamReader(process.getInputStream()));

      String line;
      while ((line = reader.readLine()) != null) {
        output.append(line);
      }

      JSONObject info = new JSONObject(output.toString()).getJSONObject("devices");
      for (String keyStr : info.keySet()) {
        JSONArray devices = info.getJSONArray(keyStr);
        for (Object object: devices){
          JSONObject device = (JSONObject) object;
          String name = device.getString("name");
          String udid = device.getString("udid");
          if(name.equals(test.java.utility.Config.Device_Name)){
            System.out.println("Device Name: " + name);
            System.out.println("Device udid: " + udid);
            this.UDID = udid;
            this.name = name;
          }
        }
      }
    } catch (Exception e){}
  }
}
