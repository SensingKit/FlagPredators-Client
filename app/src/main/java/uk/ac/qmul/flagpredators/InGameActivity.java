package uk.ac.qmul.flagpredators;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.IBinder;
import android.os.Vibrator;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import uk.ac.qmul.flagpredators.modules.BoundingBox;

/**
 * Created by Ming-Jiun Huang on 15/7/31.
 * Contact me at m.huang@hss13.qmul.ac.uk
 */

public class InGameActivity extends ActionBarActivity {
    private SensingService sensingService;
    private Vibrator mVibrator;
    private boolean isBound;
    private Location currentLocation;
    private TextView latValueText;
    private TextView lngValueText;
    private TextView centreLatVText;
    private TextView centreLngVText;
    private TextView distanceTextView;
    private TextView distanceValueTextView;
    private TextView inBounds;
    private TextView statusTextView;
    private TextView statusValueView;

    private double currentLat;
    private double currentLng;
    private InGameActivity.LocationReceiver locationReceiver;
    IntentFilter locationFilter;
    //TODO Testing inbounds
    private int countInBounds;
    private int countNotInBounds;
    private int count;
    private BoundingBox gameArea;//TODO B[T]
    private ArrayList<Location> locations;//TODO B[T]
    private BoundingBox redBox;//TODO B[T]
    private BoundingBox blueBox;//TODO B[T]

    private ServiceConnection serviceConnection  = new ServiceConnection() {
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            SensingService.SensingBinder sBinder = (SensingService.SensingBinder) iBinder;
            sensingService = (SensingService) sBinder.getService();
            isBound = true;
            System.out.println("#####Service is connected#####");
            //TODO test
            //sensingService.startSensing();
        }
        public void onServiceDisconnected(ComponentName componentName) {
            isBound = false;
            System.out.println("#####Service is disconnected#####");
        }
    };

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_in_game);
        if(MainActivity.IS_RED){
            this.setTitle("TEAM RED");
        }else if(MainActivity.IS_RED == false){
            this.setTitle("TEAM BLUE");
        }
        this.getWidget();
        mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        locationReceiver = new InGameActivity.LocationReceiver();
        locationFilter = new IntentFilter();
        locationFilter.addAction("action.location.detecting");
        locationFilter.addAction("action.get.flag");
        locationFilter.addAction("action.update.game.info");
        locationFilter.addAction("action.get.base");
        locationFilter.addAction("action.game.over");
        this.registerReceiver(locationReceiver, locationFilter);
        this.doBindService();
        gameArea = new BoundingBox(MainActivity.CURRENT_LOCATION[0],MainActivity.CURRENT_LOCATION[1], 20);//TODO B[T]
    }

    protected void getWidget(){
        latValueText = (TextView)findViewById(R.id.latValueText);
        lngValueText = (TextView)findViewById(R.id.lngValueText);
        centreLatVText = (TextView)findViewById(R.id.centreLatVtextView);
        centreLngVText = (TextView)findViewById(R.id.centreLngVtextView);
        inBounds = (TextView)findViewById(R.id.resultText);
        distanceTextView = (TextView)findViewById(R.id.distanceTextView);
        distanceValueTextView = (TextView)findViewById(R.id.distanceValueTextView);
        statusTextView = (TextView)findViewById(R.id.statusTextView);
        statusValueView = (TextView)findViewById(R.id.statusValueView);
    }

    public class LocationReceiver extends BroadcastReceiver{
        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra("location")) {
                Bundle locationBundle = intent.getBundleExtra("location");
                currentLat = (double) locationBundle.get("lat");
                currentLng = (double) locationBundle.get("lng");
                latValueText.setText("" + currentLat);
                lngValueText.setText("" + currentLng);
            }else if (intent.hasExtra("get_flag")) {
                Bundle getFlagBundle = intent.getBundleExtra("get_flag");
                statusValueView.setText(getFlagBundle.getString("flag"));
                distanceValueTextView.setText("" + getFlagBundle.getDouble("location"));
                mVibrator.vibrate(AboutActivity.PATTERN_GET_FLAG, -1);
            }else if(intent.hasExtra("get_base")){
                Bundle getBaseBundle = intent.getBundleExtra("get_base");
                String msg = getBaseBundle.getString("base") + "\nYour team holds " + getBaseBundle.getInt("holding");
                if(getBaseBundle.getInt("holding") > 1){
                     msg += " flags";
                }else {
                    msg += " flag";
                }
                statusValueView.setText(msg);
                distanceValueTextView.setText("" + getBaseBundle.getDouble("location"));
                mVibrator.vibrate(AboutActivity.PATTERN_BACK_TO_BASE, -1);
            }else if(intent.hasExtra("update_game_info")){
                Bundle updateBundle = intent.getBundleExtra("update_game_info");
                distanceValueTextView.setText("" + updateBundle.getDouble("distance"));
            }else if(intent.hasExtra("game_over")){
                Bundle gameOverBundle = intent.getBundleExtra("game_over");
                distanceValueTextView.setText(gameOverBundle.getString("info"));
                if(gameOverBundle.getBoolean("winner") && MainActivity.IS_RED){
                    statusValueView.setText("WIN");
                }else if((gameOverBundle.getBoolean("winner") == false) && (MainActivity.IS_RED == false)){
                    statusValueView.setText("WIN");
                }else {
                    statusValueView.setText("LOSE");
                }
                InGameActivity.this.doUnbindService();
            }
            /*
            double ad= SensingService.boundingBox.getAccurateDistance(currentLat,currentLng);
            if ( ad <= 1.0){
                inBounds.setText("In Bounds: " + ad);
                mVibrator.vibrate(1000);
                count++;
                countInBounds++;
            }else {
                inBounds.setText("Not: " + ad);
                mVibrator.vibrate(300);
                count++;
                countNotInBounds++;
            }*/
        }
    }

    //Bind the SensingService
    private void doBindService(){
        if (!isBound) { //check
            Intent intent = new Intent(InGameActivity.this, SensingService.class);
            bindService(intent, serviceConnection, BIND_AUTO_CREATE);
            isBound = true;
            System.out.println("Service is bound.");
            Toast.makeText(this, "START.", Toast.LENGTH_SHORT).show();
        }
    }

    //Unbind the SensingService and get the current location
    private void doUnbindService(){
        if(isBound && sensingService.hasData()) {
            currentLocation = sensingService.stopSensing();
            //Toast.makeText(this, "Get Current Location", Toast.LENGTH_SHORT).show();
            System.out.println("GET CURRENT LOCATION: " + currentLocation.getLatitude() + ", " + currentLocation.getLongitude());
            unbindService(serviceConnection);
            isBound = false;
            System.out.println("Service is unbound.");
            Toast.makeText(this, "END", Toast.LENGTH_SHORT).show();
        } else if(isBound && !sensingService.hasData()){
            sensingService.stopSensing();
            System.out.println("Didn't get any location data!");
        }
        //TODO If there is no signal, but still need to be closed.
    }

    //TODO B[T]
    public void refreshOnClick(View view){
        if(sensingService.hasData()){
            locations = sensingService.getLocations();
            System.out.println("<><><><><><><><><><><><><><><><><><><><><><><><><><><><>");
            double[] lastLocation = {locations.get(locations.size() - 1).getLatitude(),
                    locations.get(locations.size() - 1).getLongitude()};
            latValueText.setText("" + lastLocation[0]);
            lngValueText.setText("" + lastLocation[1]);
            this.checkBoundary(lastLocation);
        }else {
            Toast.makeText(this, "NO DATA", Toast.LENGTH_SHORT).show();
            System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~NO DATA~~~~~~~~~~~~~~~~~~~~~~~~~");
        }

    }
    //Check if the given location is in bounds.
    private void checkBoundary(double[] location){
        if(gameArea.checkInBoundsByCoordinate(location[0], location[1])){
            inBounds.setText("In Bounds!");
            mVibrator.vibrate(500); //Vibrate half second
        }else {
            inBounds.setText("Out of Bounds");
            mVibrator.vibrate(AboutActivity.PATTERN_OUT_OF_BOUNDARY, -1);
        }
    }
    //TODO B[T]
    public void testADOnClick(View view){
        if(sensingService.hasData()){
            locations = sensingService.getLocations();
            centreLatVText.setText("" + MainActivity.CURRENT_LOCATION[0]);
            centreLngVText.setText("" + MainActivity.CURRENT_LOCATION[1]);
            System.out.println("<><><><><><><><><><><><><><><><><><><><><><><><><><><><>");
            double[] lastLocation = {locations.get(locations.size() - 1).getLatitude(),
                    locations.get(locations.size() - 1).getLongitude()};
            latValueText.setText("" + lastLocation[0]);
            lngValueText.setText("" + lastLocation[1]);

            if(MainActivity.IS_RED){
                blueBox = sensingService.getBlueBox();
                double ad = blueBox.getAccurateDistance(lastLocation[0], lastLocation[1]);
                inBounds.setText("AD: " + ad);
                System.out.println("AD: " + ad);
                if( ad <= 1.0){
                    mVibrator.vibrate(1500);
                }
            }else if(MainActivity.IS_RED == false){
                redBox = sensingService.getRedBox();
                double ad = redBox.getAccurateDistance(lastLocation[0], lastLocation[1]);
                inBounds.setText("AD: " + ad);
                System.out.println("AD: " + ad);
                if( ad <= 1.0){
                    mVibrator.vibrate(1500);
                }
            }
        }else {
            Toast.makeText(this, "NO DATA", Toast.LENGTH_SHORT).show();
            System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~NO DATA~~~~~~~~~~~~~~~~~~~~~~~~~");
        }
    }

    public void leaveTheGameOnClick(View view){
        this.doUnbindService();
        System.out.println("Count: " + count +
                "\nInBounds: " + countInBounds +
                "\nNot: " + countNotInBounds);
        inBounds.setText("Times" + count);
        centreLatVText.setText("In: " + countInBounds);
        centreLngVText.setText("Not: " + countNotInBounds);
        //MainActivity.releaseGameInfo();
        //Intent goToMain = new Intent(InGameActivity.this, MainActivity.class);
        //startActivity(goToMain);
        finish();
    }

    public void leaveGame(){
        this.doUnbindService();
        MainActivity.releaseGameInfo();
    }

    public void onRestart(){
        super.onRestart();
        System.out.println("*****Retarting*****");
        this.registerReceiver(locationReceiver, locationFilter);
    }
    public void onStart(){
        super.onStart();
        System.out.println("*****Starting*****");
    }
    public void onResume(){
        super.onResume();
        System.out.println("*****Resuming*****");
    }
    public void onPause(){
        super.onPause();
        System.out.println("*****Pausing*****");
    }
    public void onStop(){
        super.onStop();
        System.out.println("*****Stopping*****");
        this.unregisterReceiver(locationReceiver);

    }
    public void onDestroy(){
        super.onDestroy();
        System.out.println("*****Destroying*****");
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_in_game, menu);
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
