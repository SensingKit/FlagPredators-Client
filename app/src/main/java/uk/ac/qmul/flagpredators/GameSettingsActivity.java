package uk.ac.qmul.flagpredators;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import uk.ac.qmul.flagpredators.modules.DataManager;
import uk.ac.qmul.flagpredators.modules.Protocol;

/**
 * Created by Ming-Jiun Huang on 15/7/11.
 * Contact me at m.huang@hss13.qmul.ac.uk
 */

public class GameSettingsActivity extends ActionBarActivity {
    private TextView statusTextView;
    private TextView locationTextView;
    private RadioGroup playerGroup;
    private RadioGroup flagGroup;
    private RadioGroup boundaryGroup;
    private ConnectToServer connection;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_settings);
        this.getWidget();
        locationTextView.setText("<Latitude>" + MainActivity.CURRENT_LOCATION[0] +
                "\n<Longitude>" + MainActivity.CURRENT_LOCATION[1]);
    }

    protected void getWidget(){
        statusTextView = (TextView)findViewById(R.id.statusTextView);
        locationTextView = (TextView)findViewById(R.id.locationTextView);
        playerGroup = (RadioGroup)findViewById(R.id.playerGroup);
        flagGroup = (RadioGroup)findViewById(R.id.flagGroup);
        boundaryGroup = (RadioGroup)findViewById(R.id.boundaryGroup);
        //The radio buttons are checked as default in radio groups
        playerGroup.check(R.id.twoPlayersButton);
        flagGroup.check(R.id.oneFlagButton);
        boundaryGroup.check(R.id.fortyMButton);
    }

    public void createOnClick(View view){
        final DataManager outputData = new DataManager(Protocol.CREATE_GAME);
        outputData.putGameName(MainActivity.USERNAME);
        outputData.putId(MainActivity.PLAYER_ID);
        outputData.putCurrentLocation(MainActivity.CURRENT_LOCATION[0], MainActivity.CURRENT_LOCATION[1]);
        outputData.putJail(false);  //**The jail is not implemented yet, so it is false for now
        switch (playerGroup.getCheckedRadioButtonId()){
            case R.id.twoPlayersButton:
                outputData.putNoOfPlayers(2);
                break;
            case R.id.fourPlayersButton:
                outputData.putNoOfPlayers(4);
                break;
            case R.id.sixPlayersButton:
                outputData.putNoOfPlayers(6);
                break;
            case R.id.eightPlayersButton:
                outputData.putNoOfPlayers(8);
                break;
            case R.id.tenPlayersButton:
                outputData.putNoOfPlayers(10);
                break;
        }
        switch (flagGroup.getCheckedRadioButtonId()){
            case R.id.oneFlagButton:
                outputData.putNoOfFlags(1);
                break;
            case R.id.twoFlagsButton:
                outputData.putNoOfFlags(2);
                break;
            case R.id.threeFlagsButton:
                outputData.putNoOfFlags(3);
                break;
        }
        switch (boundaryGroup.getCheckedRadioButtonId()){
            case R.id.fortyMButton:
                outputData.putGameBoundry(15);
                break;
            case R.id.fiftyMButton:
                outputData.putGameBoundry(20);
                break;
            case R.id.sixtyMButton:
                outputData.putGameBoundry(25);
                break;
        }
        connection = new ConnectToServer(new ConnectingListener() {
            public void onTaskComplete(ArrayList<String> respondingJsons) {
                for (String json : respondingJsons) {
                    DataManager inputData = new DataManager(json);
                    MainActivity.releaseGameInfo(); //Release previous data before creating a new game
                    MainActivity.GAME_ID = inputData.getGameId();
                    MainActivity.GAME_NAME = inputData.getGameName();
                    MainActivity.NO_OF_PLAYERS = inputData.getNoOfPlayers();
                    MainActivity.IS_INITIATOR = true;
                    MainActivity.NO_OF_PLAYERS_IN_RED = 0;
                    MainActivity.NO_OF_PLAYERS_IN_BLUE = 0;
                    Toast.makeText(GameSettingsActivity.this, "LOADING", Toast.LENGTH_SHORT).show();
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        public void run() {
                            Intent goToGameRoom = new Intent(GameSettingsActivity.this, GameRoomActivity.class);
                            startActivity(goToGameRoom);
                            finish();
                        }       //TODO STILL has bugs when clicking too fast >> the buttons will be all disable
                    }, 1000);    //Make the switch between two activities delay hals second in order to make sure onPostExecute() finished.
                }
                System.out.println(MainActivity.GAME_ID + MainActivity.IS_INITIATOR); //TODO [Testing]
            }
        });
        if(this.checkNetwork()){
            connection.execute(outputData.toJson());
        }

    }

    public void backToMainOnClick(View view){
        GameSettingsActivity.this.finish();
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

    protected void onStart(){
        super.onStart();
        System.out.println("*****Starting*****");
    }
    protected void onResume(){
        super.onResume();
        System.out.println("*****Resuming*****");
    }
    protected void onPause(){
        super.onPause();
        System.out.println("*****Pausing*****");
    }
    protected void onStop(){
        super.onStop();
        System.out.println("*****Stopping*****");
    }
    protected void onDestroy(){
        super.onDestroy();
        System.out.println("*****Destroying*****");
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_game_settings, menu);
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
