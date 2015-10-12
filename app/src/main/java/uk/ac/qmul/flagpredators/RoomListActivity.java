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
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import uk.ac.qmul.flagpredators.modules.DataManager;
import uk.ac.qmul.flagpredators.modules.Protocol;

/**
 * Created by Ming-Jiun Huang on 15/7/05.
 * Contact me at m.huang@hss13.qmul.ac.uk
 */

public class RoomListActivity extends ActionBarActivity {
    private LinearLayout listLayout;
    private ConnectToServer connection;
    private ConnectToServer joinConnection;
    private LinearLayout gameLayout;
    private Button reconnectButton;
    private boolean isCreated;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_list);
        this.setTitle("AVAILABLE ROOMS");
        isCreated = false;
        this.getWidget();
        reconnectButton.setEnabled(false);
    }

    public void onStart(){
        super.onStart();
        System.out.println("*****Starting*****");
        if(this.checkNetwork()){
            this.updateList();
        }
    }
    public void getWidget(){
        listLayout = (LinearLayout)findViewById(R.id.listLayout);
        reconnectButton = (Button)findViewById(R.id.reconnectButton);
    }

    public void updateList(){
        if(isCreated){
            listLayout.removeAllViews();
        }
        DataManager outputData = new DataManager(Protocol.REQUEST_GAMES);
        outputData.putId(MainActivity.PLAYER_ID);
        connection = new ConnectToServer(new ConnectingListener() {
            public void onTaskComplete(ArrayList<String> respondingJsons) {
                for(String json : respondingJsons){
                    final DataManager inputData = new DataManager(json);
                    switch (inputData.getRespondingProtocol()){
                        case STILL_IN_GAME:
                            Toast.makeText(RoomListActivity.this, "You are still in one live game", Toast.LENGTH_SHORT).show();
                            reconnectButton.setEnabled(true);
                            MainActivity.GAME_ID = inputData.getGameId();
                            break;
                        case SHOW_GAMES:
                            gameLayout = new LinearLayout(RoomListActivity.this);
                            gameLayout.setOrientation(LinearLayout.HORIZONTAL);
                            Button btn = new Button(RoomListActivity.this);
                            btn.setText(inputData.getGameName() + "'s Room");
                            btn.setBackground(getResources().getDrawable(R.drawable.button_green_stroke_selector));
                            btn.setTextColor(0xffaccb12);
                            btn.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                            final TextView capacityText = new TextView(RoomListActivity.this);
                            capacityText.setText(inputData.getJoinedPlayers() + "/" + inputData.getNoOfPlayers());
                            capacityText.setTextColor(0xffaccb12);
                            gameLayout.addView(btn);
                            gameLayout.addView(capacityText);
                            listLayout.addView(gameLayout);

                            btn.setOnClickListener(new View.OnClickListener() {
                                public void onClick(View view) {
                                    DataManager outputData = new DataManager(Protocol.JOIN_GAME);
                                    outputData.putId(MainActivity.PLAYER_ID);
                                    outputData.putGameId(inputData.getGameId());
                                    joinConnection = new ConnectToServer(new ConnectingListener() {
                                        public void onTaskComplete(ArrayList<String> respondingJsons) {
                                            for(String json : respondingJsons){
                                                DataManager joinedGameData = new DataManager(json);
                                                switch(joinedGameData.getRespondingProtocol()){
                                                    case SHOW_GAMEROOM_INFO:
                                                        MainActivity.releaseGameInfo(); //Release previous data before joining a new game
                                                        MainActivity.GAME_ID = joinedGameData.getGameId();
                                                        MainActivity.GAME_NAME = joinedGameData.getGameName();
                                                        MainActivity.NO_OF_PLAYERS = inputData.getNoOfPlayers();
                                                        MainActivity.NO_OF_PLAYERS_IN_RED = inputData.getNoOfPlayersInRed();
                                                        MainActivity.NO_OF_PLAYERS_IN_BLUE = inputData.getNoOfPlayersInBlue();
                                                        Toast.makeText(RoomListActivity.this, "LOADING", Toast.LENGTH_SHORT).show();
                                                        Handler handler = new Handler();
                                                        handler.postDelayed(new Runnable() {
                                                            public void run() {
                                                                Intent goToRoom = new Intent(RoomListActivity.this, GameRoomActivity.class);
                                                                startActivity(goToRoom);
                                                                RoomListActivity.this.finish();
                                                            }
                                                        }, 1000);
                                                        break;
                                                    case JOINING_DENIED:
                                                        Toast.makeText(RoomListActivity.this, "This game is full", Toast.LENGTH_SHORT).show();
                                                        updateList();   //TODO Need to be checked
                                                        break;
                                                    case ERROR:
                                                        Toast.makeText(RoomListActivity.this, "This game doesn't exist", Toast.LENGTH_SHORT).show();
                                                        updateList();   //TODO Need to be checked
                                                        break;
                                                }
                                            }
                                        }
                                    });
                                    joinConnection.execute(outputData.toJson());
                                }
                            });
                            break;
                    }
                }
            }
        });
        connection.execute(outputData.toJson());
        isCreated = true;
    }

    public void updateListOnClick(View view){
        if(this.checkNetwork()){
            this.updateList();
        }
    }

    public void reconnectOnClick(View view){
        if (this.checkNetwork()){
            Intent restartIntent = new Intent(RoomListActivity.this, InGameActivity.class);
            startActivity(restartIntent);
            this.finish();
        }
    }

    public void backToStartOnClick(View view){
        this.finish();
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

    public void onRestart(){
        super.onRestart();
        System.out.println("*****Restarting*****");
        this.updateList();
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
    }
    public void onDestroy(){
        super.onDestroy();
        System.out.println("*****Destroying*****");
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_room_list, menu);
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
