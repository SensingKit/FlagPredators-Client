package uk.ac.qmul.flagpredators;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import uk.ac.qmul.flagpredators.modules.DataManager;
import uk.ac.qmul.flagpredators.modules.Protocol;

/**
 * Created by Ming-Jiun Huang on 15/4/02.
 * Contact me at m.huang@hss13.qmul.ac.uk
 */

public class MainActivity extends ActionBarActivity {
    public static String PLAYER_ID;
    public static String USERNAME;
    public static double[] CURRENT_LOCATION = new double[2];
    public static String GAME_ID;    //TODO static gameID ??
    public static String GAME_NAME;  //TODO NEED TO ASSIGN it and gameId to be null when leaving the game
    public static boolean IS_INITIATOR = false;
    public static Boolean IS_RED = null;
    public static Boolean IS_READY =null;
    public static int NO_OF_PLAYERS;
    public static int NO_OF_PLAYERS_IN_RED;
    public static int NO_OF_PLAYERS_IN_BLUE;
    public static int GAMES_ON_LIST;


    private TextView resultTextView;
    private TextView registerTextView;
    private EditText usernameEditText;
    private Button startButton;
    private Button settingButton;
    private Button aboutButton;
    private Button registerButton;
    private Button reconnectButton;
    private ConnectToServer connection;
    private ConnectToServer reconnection;
    private boolean isRegistered;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.setTitle("Flag Predators");
        this.getWidget();
        isRegistered = false;
        if(PLAYER_ID == null){   //Check if the device is registered or not
            startButton.setEnabled(false);
        }else {
            usernameEditText.setText(USERNAME);
            registerTextView.setText(registerTextView.getText() + PLAYER_ID);
            registerButton.setEnabled(false);
            usernameEditText.setEnabled(false);
        }
        usernameEditText.setSelectAllOnFocus(true);
        System.out.println("Main*****Creating*****");
        reconnectButton.setEnabled(false);
    }
    protected void getWidget(){
        resultTextView = (TextView)findViewById(R.id.resultTextView);
        registerTextView = (TextView)findViewById(R.id.registerTextView);
        usernameEditText = (EditText)findViewById(R.id.usernameEditText);
        startButton = (Button)findViewById(R.id.startButton);
        //settingButton = (Button)findViewById(R.id.settingsButton);
        reconnectButton = (Button)findViewById(R.id.reconnectButton);
        aboutButton = (Button)findViewById(R.id.aboutButton);
        registerButton = (Button)findViewById(R.id.registerButton);
    }

    public void onRestart(){
        super.onRestart();
        System.out.println("Main*****Restarting*****");
    }
    public void onStart(){
        super.onStart();
        System.out.println("Main*****Starting*****");
        this.checkInGame();
    }
    public void onResume(){
        super.onResume();
        System.out.println("Main*****Resuming*****");
    }
    public void onPause(){
        super.onPause();
        System.out.println("Main*****Pausing*****");
    }
    public void onStop(){
        super.onStop();
        System.out.println("Main*****Stopping*****");
    }
    public void onDestroy(){
        super.onDestroy();
        System.out.println("Main*****Destroying*****");
    }

    public static void releaseGameInfo(){
        MainActivity.GAME_ID = null;
        MainActivity.GAME_NAME = null;
        MainActivity.NO_OF_PLAYERS = 0;
        MainActivity.NO_OF_PLAYERS_IN_RED = 0;
        MainActivity.NO_OF_PLAYERS_IN_BLUE = 0;
        MainActivity.GAMES_ON_LIST = 0;
        MainActivity.IS_INITIATOR = false;
        MainActivity.IS_RED = null;
        MainActivity.IS_READY =null;
        System.out.println("Game info is released.");
    }

    public void checkInGame(){
        if(isRegistered && this.checkNetwork()){
            DataManager outputData = new DataManager(Protocol.REQUEST_GAMES);
            outputData.putId(MainActivity.PLAYER_ID);
            reconnection = new ConnectToServer(new ConnectingListener() {
                public void onTaskComplete(ArrayList<String> respondingJsons) {
                    for(String json : respondingJsons){
                        DataManager inputData = new DataManager(json);
                        switch (inputData.getRespondingProtocol()){
                            case STILL_IN_GAME:
                                Toast.makeText(MainActivity.this, "You are still in one live game", Toast.LENGTH_SHORT).show();
                                reconnectButton.setEnabled(true);
                                MainActivity.GAME_ID = inputData.getGameId();
                                break;
                        }
                    }
                }
            });
            reconnection.execute(outputData.toJson());
        }
    }

    public void reconnectOnClick(View view){
        if (this.checkNetwork()){
            Intent restartIntent = new Intent(MainActivity.this, InGameActivity.class);
            startActivity(restartIntent);
        }
    }

    public void connectOnClick(View view){
        //<register_player>{username=\"Ming\",latitude=\"2.2\",longitude=\"1.1\"}
        //<create_game>{playerid=\"pn00000001\",latitude=\"2.2\",longitude=\"1.1\",players=\"10\",flags=\"3\",boundary=\"50\",jail=\"false\"}
        //<request_games>{playerid=\"pn00000001\"}
        //<join_game>{playerid=\"pn00000001\",gameid=\"gn00000001\",latitude=\"2.2\",longitude=\"1.1\"}
        //<join_team>{playerid="pn00000001",gameid="gn00000001",isred="true"}
        //Intent testINGAME = new Intent(this, InGameActivity.class);
        //startActivity(testINGAME);
    }

    public void startOnClick(View view){
        Intent goToGameStart = new Intent(MainActivity.this,StartActivity.class);
        startActivity(goToGameStart);
    }

    public void settingsOnClick(View view){

    }

    public void aboutOnClick(View view){

        Intent goToAbout = new Intent(MainActivity.this, AboutActivity.class);
        startActivity(goToAbout);
    }
//TODO Future work: Wrtie a file to store username and playerId, so next time app can read it if it exists(check) and get the player information.
    public void registerOnClick(View view){
        if((usernameEditText.getText().length() > 0) && !isRegistered && this.checkNetwork()) {
            //Send the information to register a player for this device.
            DataManager outputData = new DataManager(Protocol.REGISTER_PLAYER);
            outputData.putName(usernameEditText.getText().toString());
            //TODO Using random temporarily. NEED to get Location from SensingService
            outputData.putCurrentLocation(Math.random(), Math.random());
            connection = new ConnectToServer(new ConnectingListener() {
                public void onTaskComplete(ArrayList<String> respondingJsons) {
                    String msg = "";
                    for (String json : respondingJsons) {
                        DataManager inputData = new DataManager(json);
                        msg += json + "\n";
                        switch (inputData.getRespondingProtocol()){
                            case RESPOND_ID:
                                PLAYER_ID = inputData.getId();
                                USERNAME = inputData.getName();
                                registerTextView.setText("Player ID:\n" + PLAYER_ID);
                                usernameEditText.setText(USERNAME);
                                resultTextView.setText("Player Name:\n" + USERNAME);
                                startButton.setEnabled(true);   //TO enable the start button once getting a player id
                                registerButton.setEnabled(false);
                                usernameEditText.setEnabled(false);
                                isRegistered = true;
                                break;
                        }
                    }
                    System.out.println(msg);
                }
            });
            connection.execute(outputData.toJson());    //Send a playerID request
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

    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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