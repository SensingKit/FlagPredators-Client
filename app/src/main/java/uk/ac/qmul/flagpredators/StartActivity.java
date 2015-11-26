package uk.ac.qmul.flagpredators;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.IBinder;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by Ming-Jiun Huang on 15/6/18.
 * Contact me at m.huang@hss13.qmul.ac.uk
 */

public class StartActivity extends ActionBarActivity {
    private static final String TAG = "Info";
    private LocationService locationService;
    private Boolean isBound = false; //SensingKit service is not bound
    private Location lastLocation;
    private double currentLat;
    private double currentLng;
    private Button sensingButton;
    private Button createButton;
    private Button joinButton;
    private TextView latitudeTextView;
    private TextView longitudeTextView;
    private ConnectToServer connection;
    StartActivity.LocationReceiver locationReceiver;

    private ServiceConnection serviceConnection  = new ServiceConnection() {
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            LocationService.LocationBinder sBinder = (LocationService.LocationBinder) iBinder;
            locationService = (LocationService) sBinder.getService();
            isBound = true;
            System.out.println("#####Service is connected#####");
            locationService.startSensing();
        }
        public void onServiceDisconnected(ComponentName componentName) {
            isBound = false;
            System.out.println("#####Service is disconnected#####");
        }
    };

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        this.setTitle("SELECT");
        this.getWidget();
    }

    protected void getWidget(){
        sensingButton = (Button) findViewById(R.id.unbindButton);
        createButton = (Button) findViewById(R.id.createAGameButton);
        joinButton = (Button) findViewById(R.id.joinAGameButton);
        latitudeTextView = (TextView)findViewById(R.id.latitudeTextView);
        longitudeTextView = (TextView)findViewById(R.id.longitudeTextView);
    }

    protected void onStart(){
        super.onStart();
        Log.i(TAG, "*****Starting*****");
        this.doBindService();
        locationReceiver = new StartActivity.LocationReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("action.location.detecting");
        this.registerReceiver(locationReceiver, filter);
    }

    public class LocationReceiver extends BroadcastReceiver{
        public void onReceive(Context context, Intent intent) {
            Bundle locationBundle = intent.getBundleExtra("location");
            currentLat = locationBundle.getDouble("lat");
            currentLng = locationBundle.getDouble("lng");
            latitudeTextView.setText(" " + currentLat);
            longitudeTextView.setText(" " + currentLng);
        }
    }
//Bind the SensingService
    private void doBindService(){
        if (!isBound) { //check
            Intent intent = new Intent(StartActivity.this, LocationService.class);
            bindService(intent, serviceConnection, BIND_AUTO_CREATE);
            isBound = true;
            System.out.println("SensingService is bound.");
            //Toast.makeText(this, "SensingService is bound.", Toast.LENGTH_SHORT).show();
        }
    }

//Unbind the SensingService and get the current location
    private void doUnbindService(){
        if(isBound) {
            lastLocation = locationService.stopSensing();
            if(locationService.hasData()) {
                System.out.println("GET CURRENT LOCATION: " + lastLocation.getLatitude() + ", " + lastLocation.getLongitude());
            }
            unbindService(serviceConnection);
            isBound = false;
            System.out.println("SensingService is unbound.");
            //Toast.makeText(this, "SensingService is unbound.", Toast.LENGTH_SHORT).show();
        }
    }


    public void createAGameOnClick (View v){
        if(locationService.hasData()){
            this.doUnbindService(); //Get the last Location
            MainActivity.CURRENT_LOCATION[0] = lastLocation.getLatitude();
            MainActivity.CURRENT_LOCATION[1] = lastLocation.getLongitude();
            Intent goToGameSettings = new Intent(StartActivity.this, GameSettingsActivity.class);
            startActivity(goToGameSettings);
            StartActivity.this.finish();
        }else {
            Toast.makeText(this, "Haven't got your location! Please wait!", Toast.LENGTH_SHORT).show();
            System.out.println("Haven't got your location! Please wait!");
        }
    }

    public void joinAGameOnClick (View v){
        if(locationService.hasData()){
            this.doUnbindService(); //Get the last Location
            MainActivity.CURRENT_LOCATION[0] = lastLocation.getLatitude();
            MainActivity.CURRENT_LOCATION[1] = lastLocation.getLongitude();
            Intent goToGameChoosing = new Intent(StartActivity.this, RoomListActivity.class);
            startActivity(goToGameChoosing);
            StartActivity.this.finish();
        }else {
            Toast.makeText(this, "Haven't got your location! Please wait!", Toast.LENGTH_SHORT).show();
            System.out.println("Haven't got your location! Please wait!");
        }
    }

//Unbind the SensingService or Bind the SensingService.
    public void unbindOnClick(View v){
        if(isBound){
            this.doUnbindService();
            sensingButton.setText("BIND");
        }else{
            this.doBindService();
            sensingButton.setText("UNBIND");
        }
    }

    public void goBackToMainOnClick(View v){
        StartActivity.this.finish();
    }

    protected void onRestart(){
        super.onRestart();
        Log.i(TAG, "*****Restarting*****");
    }
    protected void onResume(){
        super.onResume();
        Log.i(TAG,"*****Resuming*****");
    }
    protected void onPause() {
        super.onPause();
        Log.i(TAG,"*****Pausing*****");
    }

    protected void onStop(){
        super.onStop();
        Log.i(TAG, "*****Stopping*****");
        this.doUnbindService();
        this.unregisterReceiver(locationReceiver);
    }
    protected void onDestroy(){
        super.onDestroy();
        Log.i(TAG, "*****Destroying*****");
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_start, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
