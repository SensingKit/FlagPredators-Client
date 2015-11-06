package uk.ac.qmul.flagpredators;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.Vibrator;
import android.widget.Toast;

import org.sensingkit.sensingkitlib.SKException;
import org.sensingkit.sensingkitlib.SKSensorDataListener;
import org.sensingkit.sensingkitlib.SensingKitLib;
import org.sensingkit.sensingkitlib.SensingKitLibInterface;
import org.sensingkit.sensingkitlib.data.SKLocationData;
import org.sensingkit.sensingkitlib.SKSensorType;
import org.sensingkit.sensingkitlib.data.SKSensorData;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

import uk.ac.qmul.flagpredators.modules.BoundingBox;
import uk.ac.qmul.flagpredators.modules.DataManager;
import uk.ac.qmul.flagpredators.modules.Protocol;

/**
 * Created by Ming-Jiun Huang on 15/6/1.
 *
 */

public class SensingService extends Service {
    private final IBinder sensingBinder = new SensingBinder();
    private SKLocationData currentLocationData;
    private ArrayList<SKLocationData> currentLocationArrayList;
    static SensingKitLibInterface mSensingKitLib;
    private ArrayList<SKSensorType> registeredSensors = new ArrayList<SKSensorType>();

    public static BoundingBox redBox = null;
    public static BoundingBox blueBox = null; //TODO B[T]

    private final String SERVER_IP_ADDRESS = "161.23.77.45";
    private final int SERVER_PORT = 13333;
    private static Socket clientSocket = null;
    private static DataInputStream input;
    private static DataOutputStream output;
    private PowerManager.WakeLock wakeLock;
    private Vibrator mVibrator;
    private static boolean GET_FLAG = false;
    private boolean isCommunicating;

    public IBinder onBind(Intent intent) {
        return sensingBinder;
    }

    public class SensingBinder extends Binder{
        public SensingService getService(){
            return SensingService.this;
        }
    }

    public void onCreate(){
        super.onCreate();
        isCommunicating = true;
        mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        PowerManager powerManager = (PowerManager)getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Sensing WakeLock");
        wakeLock.acquire();
        new Thread(new CommunicationThread()).start();
        try {
            mSensingKitLib = SensingKitLib.getSensingKitLib(this); //Get SensingKitLib
            if (!mSensingKitLib.isSensorRegistered(SKSensorType.LOCATION)) { //Check whether the sensor module is registered
                mSensingKitLib.registerSensor(SKSensorType.LOCATION); //Register Sensor Module: LOCATION
                System.out.println("Registered!");
            }
            new Thread(new SensingServerThread()).start();
            currentLocationArrayList = new ArrayList<SKLocationData>();
        } catch (SKException e) {
            e.printStackTrace();
        }
    }

    public class CommunicationThread implements Runnable{

        public CommunicationThread(){
        }
        public void run() {
            try {
                clientSocket = new Socket(SERVER_IP_ADDRESS, SERVER_PORT);
                input = new DataInputStream(clientSocket.getInputStream());
                output = new DataOutputStream(clientSocket.getOutputStream());
                String inputStr = null;
                //DataManager registerData = new DataManager(Protocol.REGISTER_BROADCAST);
                //registerData.putId(MainActivity.PLAYER_ID);
                //registerData.putGameId(MainActivity.GAME_ID);
                //output.writeUTF(registerData.toJson());
                while (isCommunicating){
                //while ((inputStr = input.readUTF()) != null){
                    if(SensingService.this.checkNetwork()){
                        inputStr = input.readUTF();
                        DataManager inputData = new DataManager(inputStr);
                        this.executeProtocol(inputData);
                        System.out.println("loop: " + inputData.getError());
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        public void executeProtocol(DataManager inputData){
            Intent thisIntent = new Intent();
            Bundle thisBundle = new Bundle();
            switch (inputData.getRespondingProtocol()){
                
                case UPDATE_GAME_INFO:
                    System.out.println(inputData.getDistance());
                    thisIntent.setAction("action.update.game.info");
                    thisBundle.putDouble("distance", inputData.getDistance());
                    thisIntent.putExtra("update_game_info", thisBundle);
                    sendBroadcast(thisIntent);
                    break;
                case GET_FLAG:
                    GET_FLAG = true;
                    System.out.println(inputData.getInfo() + inputData.getDistance());
                    //mVibrator.vibrate(AboutActivity.PATTERN_GET_FLAG, -1);
                    thisIntent.setAction("action.get.flag");
                    thisBundle.putString("flag", inputData.getInfo());
                    thisBundle.putDouble("location", inputData.getDistance());
                    thisIntent.putExtra("get_flag", thisBundle);
                    sendBroadcast(thisIntent);
                    break;
                //TODO GET_CAUGHT
                case GET_BASE:
                    GET_FLAG = false;
                    System.out.println(inputData.getInfo() + inputData.getDistance());
                    //mVibrator.vibrate(AboutActivity.PATTERN_BACK_TO_BASE, -1);
                    thisIntent.setAction("action.get.base");
                    thisBundle.putString("base", inputData.getInfo());
                    thisBundle.putInt("holding", inputData.getHoldingFlags());
                    thisBundle.putDouble("location", inputData.getDistance());
                    thisIntent.putExtra("get_base", thisBundle);
                    sendBroadcast(thisIntent);
                    break;
                case OUT_OF_BOUNDS:
                    System.out.println(inputData.getError());
                    mVibrator.vibrate(AboutActivity.PATTERN_OUT_OF_BOUNDARY, -1);
                    break;
                case GAME_OVER:
                    System.out.println(inputData.getInfo() + inputData.isRed());
                    thisIntent.setAction("action.game.over");
                    thisBundle.putString("info", inputData.getInfo());
                    thisBundle.putBoolean("winner", inputData.isRed());
                    thisIntent.putExtra("game_over", thisBundle);
                    sendBroadcast(thisIntent);
                    //mVibrator.vibrate(AboutActivity.PATTERN_GAME_OVER, -1);
                    break;
                case ERROR:
                    break;
            }
        }
    }

//SENSINGKIT------------------------------------------------------------------------------------------------------------------------
//Start Sensing location, Given a thread(SensingServerThread)
    public void startSensing(){
        new Thread(new SensingServerThread()).start();
    }

//Stop Sensing location
    public Location stopSensing() {
        SKLocationData lastData;
        try {
            wakeLock.release();
            if(mSensingKitLib.isSensorSensing(SKSensorType.LOCATION)){
                mSensingKitLib.stopContinuousSensingWithSensor(SKSensorType.LOCATION);
            }
            if(currentLocationArrayList.size() > 0){ //avoid null pointer exception
                lastData = currentLocationArrayList.get(currentLocationArrayList.size() - 1);
                System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$"); //print out the last data
                System.out.println("The last data : " + lastData.getDataInCSV());
                System.out.println("The size of data list : " + currentLocationArrayList.size());
                System.out.println(">>Latitude : " + lastData.getLocation().getLatitude());
                System.out.println(">>Longitude : " + lastData.getLocation().getLongitude());
                return lastData.getLocation();
            }
        } catch (SKException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean hasData(){
        if(currentLocationArrayList.size() > 0){
            return true;
        }
        return false;
    }

    public ArrayList<Location> getLocations(){
        ArrayList<Location> locations = new ArrayList<Location>();
        for (SKLocationData locationData : currentLocationArrayList){
            locations.add(locationData.getLocation());
        }
        return locations;
    }


//Inner Class of SensingService implements Runnable to give a Thread for this sensing service to get the GPS data.
    public class SensingServerThread implements Runnable, SKSensorDataListener{
        public SensingServerThread(){
            super();
            try {
                //Subscribe the sensor data listener for Location module
                mSensingKitLib.subscribeSensorDataListener(SKSensorType.LOCATION, this);
            } catch (SKException e) {
                e.printStackTrace();
            }
        }
        public void run() {
            try {
                if(!mSensingKitLib.isSensorSensing(SKSensorType.LOCATION)){
                    mSensingKitLib.startContinuousSensingWithSensor(SKSensorType.LOCATION); //Start sensing
                }
            } catch (SKException e) {
                e.printStackTrace();
            }
        }

        public void onDataReceived(SKSensorType sensorType, SKSensorData moduleData) {
            currentLocationData = (SKLocationData) moduleData;
            double lat = currentLocationData.getLocation().getLatitude();
            double lng = currentLocationData.getLocation().getLongitude();
            System.out.println("#####SENSING#####" + currentLocationData.getDataInCSV());
            System.out.println(">> Latitude: " + lat);
            System.out.println(">> Longitude: " + lng);
            currentLocationArrayList.add((SKLocationData) moduleData);
            DataManager outputData;
            if(GET_FLAG){
                outputData = new DataManager(Protocol.CHECK_LOCATION_WITH_BASE);
            }else {
                outputData = new DataManager(Protocol.CHECK_LOCATION_WITH_FLAG);
            }
            outputData.putId(MainActivity.PLAYER_ID);
            outputData.putGameId(MainActivity.GAME_ID);
            outputData.putCurrentLocation(lat, lng);
            try {
                if (checkNetwork()){
                    output.writeUTF(outputData.toJson());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            //Sending location data to Activity to show on the UI
            Intent locationIntent = new Intent();
            locationIntent.setAction("action.location.detecting");
            Bundle locationBundle = new Bundle();
            locationBundle.putDouble("lat", currentLocationData.getLocation().getLatitude());
            locationBundle.putDouble("lng",currentLocationData.getLocation().getLongitude());
            locationIntent.putExtra("location", locationBundle);
            sendBroadcast(locationIntent);
            //TODO TESTING BOUNDINGBOX
            setBoundingBox(lat, lng); //TODO B[T]
        }
    }

    //Check whether the network is connected or not
    public boolean checkNetwork(){
        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if(networkInfo != null && networkInfo.isConnected()){
            System.out.println("ONLINE");
            return true;
        }
        Toast.makeText(this, "OFFLINE", Toast.LENGTH_SHORT).show();
        return false;
    }

//SENSINGKIT------------------------------------------------------------------------------------------------------------------------
    //TODO B[T]
    public void setBoundingBox(double lat, double lng){
        if(currentLocationArrayList.size() == 1){
            redBox = new BoundingBox(lat, lng, true, 40); //flag
            blueBox = new BoundingBox(lat,lng, false, 40);
        }
    }
    //TODO B[T]
    public BoundingBox getRedBox(){
            return redBox;
    }
    public BoundingBox getBlueBox(){
        return blueBox;
    }

}
