package uk.ac.qmul.flagpredators;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;

import org.sensingkit.sensingkitlib.SKException;
import org.sensingkit.sensingkitlib.SKSensorDataListener;
import org.sensingkit.sensingkitlib.SKSensorType;
import org.sensingkit.sensingkitlib.SensingKitLib;
import org.sensingkit.sensingkitlib.SensingKitLibInterface;
import org.sensingkit.sensingkitlib.data.SKLocationData;
import org.sensingkit.sensingkitlib.data.SKSensorData;


import java.util.ArrayList;

/**
 * Created by Ming-Jiun Huang on 15/7/21.
 * Contact me at m.huang@hss13.qmul.ac.uk
 */

public class LocationService extends Service {
    private final IBinder locationBinder = new LocationBinder();
    private SKLocationData currentLocationData;
    private ArrayList<SKLocationData> currentLocationArrayList;
    static SensingKitLibInterface mSensingKitLib;
    private ArrayList<SKSensorType> registeredSensors = new ArrayList<SKSensorType>();

    private double[] centreLocation = new double[2];//TODO B[T]

    public LocationService() {
    }

    public IBinder onBind(Intent intent) {
        return locationBinder;
    }

    public class LocationBinder extends Binder {
        public LocationService getService(){
            return LocationService.this;
        }
    }

    public void onCreate(){
        super.onCreate();
        try {
            mSensingKitLib = SensingKitLib.getSensingKitLib(this); //Get SensingKitLib
            if (!mSensingKitLib.isSensorRegistered(SKSensorType.LOCATION)) { //Check whether the sensor module is registered
                mSensingKitLib.registerSensor(SKSensorType.LOCATION); //Register Sensor Module: LOCATION
                System.out.println("Registered!");
            }
            currentLocationArrayList = new ArrayList<SKLocationData>();
        } catch (SKException e) {
            e.printStackTrace();
        }
    }

    //Start SensingKit to sense location, given a thread(SensingServerThread)
    public void startSensing(){
        new Thread(new SensingServerThread()).start();

    }

    //Stop Sensing the location and close SensingKit
    public Location stopSensing() {
        SKLocationData lastData = null;
        try {
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
    public class SensingServerThread implements Runnable, SKSensorDataListener {
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
            System.out.println("#####SENSING#####" + currentLocationData.getDataInCSV());
            System.out.println(">> Latitude: " + currentLocationData.getLocation().getLatitude());
            System.out.println(">> Longitude: " + currentLocationData.getLocation().getLongitude());
            currentLocationArrayList.add((SKLocationData) moduleData);
            //Sending location data
            Intent locationIntent = new Intent();
            locationIntent.setAction("action.location.detecting");
            Bundle locationBundle = new Bundle();
            locationBundle.putDouble("lat", currentLocationData.getLocation().getLatitude());
            locationBundle.putDouble("lng",currentLocationData.getLocation().getLongitude());
            locationIntent.putExtra("location", locationBundle);
            sendBroadcast(locationIntent);
        }
    }

    public double[] getCentreLocation(){
        return centreLocation;
    }

    //Return an arraylist that shows which sensor module types are registered.
    //TODO NO NEED?
    public ArrayList<SKSensorType> getRegisteredSensors(){
        try {
            for (SKSensorType t : SKSensorType.values()){
                if(mSensingKitLib.isSensorRegistered(t)){
                    if(!registeredSensors.contains(t)){ //** Is it working?
                        registeredSensors.add(t);
                    }
                }else{
                    if(registeredSensors.contains(t)){
                        registeredSensors.remove(t);
                    }
                }
            }
        } catch (SKException e) {
            e.printStackTrace();
        }
        return registeredSensors;
    }
}
