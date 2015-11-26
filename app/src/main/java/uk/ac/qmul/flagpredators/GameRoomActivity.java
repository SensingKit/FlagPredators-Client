package uk.ac.qmul.flagpredators;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.GradientDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import uk.ac.qmul.flagpredators.modules.DataManager;
import uk.ac.qmul.flagpredators.modules.Protocol;

/**
 * Created by Ming-Jiun Huang on 15/7/13.
 * Contact me at m.huang@hss13.qmul.ac.uk
 */

public class GameRoomActivity extends ActionBarActivity {
    private TextView gameNameTextView;
    private Button startButton, cancelButton;
    private Button addRedButton, addBlueButton;
    private Button redPlayerButton1, redPlayerButton2, redPlayerButton3, redPlayerButton4, redPlayerButton5;
    private Button bluePlayerButton1, bluePlayerButton2, bluePlayerButton3, bluePlayerButton4, bluePlayerButton5;
    private ArrayList<Button> buttons;
    private ConnectToServer updateConnection;
    private ConnectToServer addTeamConnection;
    private ConnectToServer readyConnection;
    private ConnectToServer startConnecton;
    private ConnectToServer leaveConnection;
    private ConnectToServer cancelConnection;
    private int redButtonPointer = 0;
    private int blueButtonPointer = 1;
    private boolean hasAdded;
    private boolean hasStarted;

    private ConnectingService connectingService;
    private boolean isBound = false;
    private Handler handler;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, IBinder iBinder) {
            ConnectingService.ConnectingBinder cBinder = (ConnectingService.ConnectingBinder) iBinder;
            connectingService = cBinder.getService();
            isBound = true;
            connectingService.setListener(MainActivity.PLAYER_ID, handler, new ConnectingListener() {
                public void onTaskComplete(ArrayList<String> respondingJsons) {
                    resetPointer();
                    System.out.println("I GOT HERE");
                    //updateReadyStatus();
                }
            });
            System.out.println("#####ConnectingService is connected#####");
        }
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
            System.out.println("#####ConnectingService is disconnected#####");
        }
    };

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_room);
        buttons = new ArrayList<Button>();
        this.getWidget();
        System.out.println("<><><><><><><><><><><><><><><><>Is Initiator >>> " + MainActivity.IS_INITIATOR); //TODO [Testing] Initiator needs to handle IS_READY?
        startButton.setEnabled(false);  //Need to add a team to enable the startButton
        if(!MainActivity.IS_INITIATOR){
            cancelButton.setText("LEAVE");
            startButton.setText("READY");
            MainActivity.IS_READY = false; //Initiated the ready status for joined players
            this.setTitle(MainActivity.GAME_NAME + "'s Room");
            if(this.checkNetwork()){
                this.updateGameInfo();
            }
        }else {
            this.setTitle(MainActivity.GAME_NAME + "'s Room");
            gameNameTextView.setText(MainActivity.GAME_NAME + "'s Room");
            this.refreshWidget();
        }
        hasAdded = false;
        hasStarted = false;
    }

    protected void onStart(){
        super.onStart();
        System.out.println("GameRoom*****Starting*****");
        //this.doBindService();
    }

    //Reset the pointers of buttons
    void resetPointer(){
        redButtonPointer = 0;
        blueButtonPointer = 1;
    }

    protected void getWidget(){
        gameNameTextView = (TextView)findViewById(R.id.gameNameTextView);
        startButton = (Button)findViewById(R.id.startButton);
        cancelButton = (Button)findViewById(R.id.cancelButton);
        addRedButton = (Button)findViewById(R.id.addRedButton);
        addBlueButton = (Button)findViewById(R.id.addBlueButton);
        redPlayerButton1 = (Button)findViewById(R.id.redPlayerButton1);
        redPlayerButton2 = (Button)findViewById(R.id.redPlayerButton2);
        redPlayerButton3 = (Button)findViewById(R.id.redPlayerButton3);
        redPlayerButton4 = (Button)findViewById(R.id.redPlayerButton4);
        redPlayerButton5 = (Button)findViewById(R.id.redPlayerButton5);
        bluePlayerButton1 = (Button)findViewById(R.id.bluePlayerButton1);
        bluePlayerButton2 = (Button)findViewById(R.id.bluePlayerButton2);
        bluePlayerButton3 = (Button)findViewById(R.id.bluePlayerButton3);
        bluePlayerButton4 = (Button)findViewById(R.id.bluePlayerButton4);
        bluePlayerButton5 = (Button)findViewById(R.id.bluePlayerButton5);
        this.setWidget(redPlayerButton1);
        this.setWidget(bluePlayerButton1);
        this.setWidget(redPlayerButton2);
        this.setWidget(bluePlayerButton2);
        this.setWidget(redPlayerButton3);
        this.setWidget(bluePlayerButton3);
        this.setWidget(redPlayerButton4);
        this.setWidget(bluePlayerButton4);
        this.setWidget(redPlayerButton5);
        this.setWidget(bluePlayerButton5);
    }

    protected void setButton(Button btn, int color){
        GradientDrawable drawable = new GradientDrawable();
        drawable.setStroke(3, color);
        btn.setBackground(drawable);
    }
    protected void setWidget(Button btn){
        //btn.setBackground(getResources().getDrawable(R.drawable.transparent));
        buttons.add(btn);
    }

    //Refresh the texts of the buttons that show the players in each team
    protected void refreshWidget() {
        this.resetPointer();
        for (int i = 0; i < buttons.size(); i++) {
            if ((i + 1) > MainActivity.NO_OF_PLAYERS) {
                buttons.get(i).setEnabled(false);
                buttons.get(i).setText("---------");
            } else {
                if ((i % 2) == 0) {
                    if (i < (MainActivity.NO_OF_PLAYERS_IN_RED * 2)) {
                        buttons.get(i).setText("UNAVAILABLE");
                        buttons.get(i).setBackground(getResources().getDrawable(R.drawable.team_red_light));
                    } else {
                        buttons.get(i).setText("Red Player " + ((i / 2) + 1));
                        buttons.get(i).setBackground(getResources().getDrawable(R.drawable.transparent));
                    }
                } else if ((i % 2) == 1) {
                    if (i < (MainActivity.NO_OF_PLAYERS_IN_BLUE * 2)) {
                        buttons.get(i).setText("UNAVAILABLE");
                        buttons.get(i).setBackground(getResources().getDrawable(R.drawable.team_blue_light));
                    } else {
                        buttons.get(i).setText("Blue Player " + (((i - 1) / 2) + 1));
                        buttons.get(i).setBackground(getResources().getDrawable(R.drawable.transparent));
                    }
                }
            }
        }
    }

    //Send the JOIN_TEAM Protocol to server for adding this player into a team
    //TODO Right now just don't allow to change to another team after added one team
    public void addTeam(final boolean isRed){
        this.resetPointer();
        addTeamConnection = new ConnectToServer(new ConnectingListener() {
            public void onTaskComplete(ArrayList<String> respondingJsons) {
                for(String json : respondingJsons){
                    DataManager inputData = new DataManager(json);
                    switch (inputData.getRespondingProtocol()){
                        case SHOW_GAMEROOM_INFO:
                            MainActivity.NO_OF_PLAYERS_IN_RED = inputData.getNoOfPlayersInRed();
                            MainActivity.NO_OF_PLAYERS_IN_BLUE = inputData.getNoOfPlayersInBlue();
                            GameRoomActivity.this.refreshWidget();
                            break;
                        case UPDATE_GAME_ROOM:
                            GameRoomActivity.this.updateReadyStatus(inputData);
                            if(inputData.getId().equals(MainActivity.PLAYER_ID)){
                                if(inputData.isRed()){
                                    MainActivity.IS_RED = true;
                                }else if(inputData.isRed() == false){
                                    MainActivity.IS_RED = false;
                                }
                                startButton.setEnabled(true);
                                addRedButton.setEnabled(false);
                                addBlueButton.setEnabled(false);
                                hasAdded = true;
                            }
                            break;
                        case ERROR:
                            if(inputData.getError().equals("already_in")){
                                Intent goBackToList = new Intent(GameRoomActivity.this, RoomListActivity.class);
                                startActivity(goBackToList);
                                GameRoomActivity.this.finish();
                            }else if(inputData.getError().equals("team_full")){
                                System.out.println("The team you choose is full");
                                if(isRed){
                                    Toast.makeText(GameRoomActivity.this, "Red team is full", Toast.LENGTH_SHORT).show();
                                    addRedButton.setEnabled(false);
                                }else if(!isRed){
                                    Toast.makeText(GameRoomActivity.this, "Blue team is full", Toast.LENGTH_SHORT).show();
                                    addBlueButton.setEnabled(false);
                                }
                            }
                        default:
                    }
                }
            }
        });
        final DataManager outputData = new DataManager(Protocol.JOIN_TEAM);
        outputData.putGameId(MainActivity.GAME_ID);
        outputData.putId(MainActivity.PLAYER_ID);
        outputData.putTeam(isRed);
        addTeamConnection.execute(outputData.toJson());
    }

    public void addRedOnClick(View view){
        if(this.checkNetwork()){
            this.addTeam(true);
        }
    }

    public void addBlueOnClick(View view){
        if(this.checkNetwork()) {
            this.addTeam(false);
        }
    }

    public void startGame(){
        DataManager outputData = new DataManager(Protocol.START_GAME);
        outputData.putGameId(MainActivity.GAME_ID);
        outputData.putId(MainActivity.PLAYER_ID);
        startConnecton = new ConnectToServer(new ConnectingListener() {
            public void onTaskComplete(ArrayList<String> respondingJsons) {
                for(String json : respondingJsons){
                    DataManager inputData = new DataManager(json);
                    switch(inputData.getRespondingProtocol()){
                        case SHOW_GAMEROOM_INFO:
                            MainActivity.NO_OF_PLAYERS_IN_RED = inputData.getNoOfPlayersInRed();
                            MainActivity.NO_OF_PLAYERS_IN_BLUE = inputData.getNoOfPlayersInBlue();
                            GameRoomActivity.this.refreshWidget();
                            break;
                        case UPDATE_GAME_INFO:
                            hasStarted = true;
                            Intent startGame = new Intent(GameRoomActivity.this,InGameActivity.class);
                            startActivity(startGame);
                            GameRoomActivity.this.doUnbindService();
                            GameRoomActivity.this.finish();
                            break;
                        case UPDATE_GAME_ROOM:
                            GameRoomActivity.this.updateReadyStatus(inputData);
                            Toast.makeText(GameRoomActivity.this, "Someone is not ready", Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
            }
        });
        startConnecton.execute(outputData.toJson());
    }

    public void startTheGameOnClick(View view){
        this.resetPointer();
        if(MainActivity.IS_INITIATOR && this.checkNetwork()){
            //TODO check if all players are ready>>start button becomes working
            this.startGame();
        }else if(MainActivity.IS_INITIATOR == false && this.checkNetwork()){
            if(MainActivity.IS_READY == false) {
                DataManager outputData = new DataManager(Protocol.READY_TO_GO);
                outputData.putId(MainActivity.PLAYER_ID);
                outputData.putGameId(MainActivity.GAME_ID);
                outputData.putReady(true);
                readyConnection = new ConnectToServer(new ConnectingListener() {
                    public void onTaskComplete(ArrayList<String> respondingJsons) {
                        for (String json : respondingJsons) {
                            DataManager inputData = new DataManager(json);
                            switch (inputData.getRespondingProtocol()){
                                case UPDATE_GAME_ROOM:
                                    GameRoomActivity.this.updateReadyStatus(inputData);
                                    if(inputData.getId().equals(MainActivity.PLAYER_ID) && inputData.isReady()){
                                        MainActivity.IS_READY = true;
                                        startButton.setText("Start");
                                    }
                                    break;
                                case SHOW_GAMEROOM_INFO:
                                    MainActivity.NO_OF_PLAYERS_IN_RED = inputData.getNoOfPlayersInRed();
                                    MainActivity.NO_OF_PLAYERS_IN_BLUE = inputData.getNoOfPlayersInBlue();
                                    GameRoomActivity.this.refreshWidget();
                                    break;
                            }
                        }
                    }
                });
                readyConnection.execute(outputData.toJson());
            }else if(MainActivity.IS_READY == true){
                this.startGame();
            }
        }
    }

    public void cancelTheGameOnClick(View view){
        //TODO Should call finish() in onDestory()?
        if(this.checkNetwork()){
            Intent backToStart = new Intent(GameRoomActivity.this, StartActivity.class);
            startActivity(backToStart);
            this.finish();
        }
    }

    //Update the game information for UI
    void updateGameInfo(){
        DataManager outputData = new DataManager(Protocol.JOIN_GAME);
        outputData.putGameId(MainActivity.GAME_ID); //TODO Need PLAYER_ID?
        updateConnection = new ConnectToServer(new ConnectingListener() {
            public void onTaskComplete(ArrayList<String> respondingJsons) {
                for(String json : respondingJsons){
                    DataManager inputData = new DataManager(json);
                    gameNameTextView.setText(inputData.getGameName() + "'s Room");
                    MainActivity.NO_OF_PLAYERS = inputData.getNoOfPlayers();
                    MainActivity.NO_OF_PLAYERS_IN_RED = inputData.getNoOfPlayersInRed();
                    MainActivity.NO_OF_PLAYERS_IN_BLUE = inputData.getNoOfPlayersInBlue();
                    GameRoomActivity.this.refreshWidget();
                }
            }
        });
        updateConnection.execute(outputData.toJson());
    }

    //Update the ready status of all players in this game
    void updateReadyStatus(DataManager inputData){
        if(inputData.isRed()){
            buttons.get(redButtonPointer).setText(inputData.getName());
            if(inputData.isReady()){
                buttons.get(redButtonPointer).setBackgroundColor(0xffff0088);
            }else {
                buttons.get(redButtonPointer).setBackgroundColor(0x59ff0088);
            }
            redButtonPointer += 2;
        }else if(!inputData.isRed()){
            buttons.get(blueButtonPointer).setText(inputData.getName());
            if(inputData.isReady()){
                buttons.get(blueButtonPointer).setBackgroundColor(0xff0095ff);
            }else {
                buttons.get(blueButtonPointer).setBackgroundColor(0x590095ff);
            }
            blueButtonPointer += 2;
        }
    }

    //Leave the game room and notify the server to remove this player from the game
    void leaveRoom(){
        this.doUnbindService();
        //TODO Leave the game, remove this player from the game. If this player is an initiator delete the game.
        if(hasAdded && !hasStarted){    //Make sure leave room only once
            if(MainActivity.IS_INITIATOR){
                //TODO FOR TESTING conveniently >> DISABLE the cancelGame()
                //this.cancelGame();
                System.out.println("#####~~~~~~~~~~~~~~~~~~HHHHHHHHHHHH");
            }else {
                DataManager outputData = new DataManager(Protocol.LEAVE_ROOM);
                outputData.putId(MainActivity.PLAYER_ID);
                outputData.putGameId(MainActivity.GAME_ID);
                leaveConnection = new ConnectToServer(new ConnectingListener() {
                    public void onTaskComplete(ArrayList<String> respondingJsons) {

                    }
                });
                leaveConnection.execute(outputData.toJson());
            }
            MainActivity.releaseGameInfo(); //Release Game Info stored in this device
        }
    }

    //Cancel the game room by the game initiator and notify the server to remove this game from the list of live games
    void cancelGame(){
        DataManager outputData = new DataManager(Protocol.CANCEL_GAME);
        outputData.putId(MainActivity.PLAYER_ID);
        outputData.putGameId(MainActivity.GAME_ID);
        cancelConnection = new ConnectToServer(new ConnectingListener() {
            public void onTaskComplete(ArrayList<String> respondingJsons) {

            }
        });
        leaveConnection.execute(outputData.toJson());
    }

    //Bind the SensingService
    private void doBindService(){
        if (!isBound) { //check
            Intent intent = new Intent(GameRoomActivity.this, ConnectingService.class);
            bindService(intent, serviceConnection, BIND_AUTO_CREATE);
            isBound = true;
            System.out.println("ConnectingService is bound.");
            Toast.makeText(this, "ConnectingService is bound.", Toast.LENGTH_SHORT).show();
        }
    }

    //Unbind the SensingService and get the current location
    private void doUnbindService(){
        if(isBound) {
            connectingService.disconnect();
            unbindService(serviceConnection);
            isBound = false;
            System.out.println("ConnectingService is unbound.");
            Toast.makeText(this, "ConnectingService is unbound.", Toast.LENGTH_SHORT).show();
        }
    }

    protected void onStop(){
        super.onStop();
        System.out.println("GameRoom*****Stopping*****");

    }

    protected void onDestroy(){
        super.onDestroy();
        this.leaveRoom();
        //TODO check if the onDestory() will be called when calling finish()
        System.out.println("GameRoom*****Destroying*****" + MainActivity.IS_INITIATOR);
    }

    //Check whether the network is connected or not
    protected boolean checkNetwork(){
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
        getMenuInflater().inflate(R.menu.menu_game_room, menu);
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
