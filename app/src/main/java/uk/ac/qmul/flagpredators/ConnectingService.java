package uk.ac.qmul.flagpredators;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

import uk.ac.qmul.flagpredators.modules.DataManager;
import uk.ac.qmul.flagpredators.modules.Protocol;

/**
 * Created by Ming-Jiun Huang on 15/7/9.
 * Contact me at m.huang@hss13.qmul.ac.uk
 */

public class ConnectingService extends Service {
    private final IBinder connectingBinder = new ConnectingBinder();
    private final String SERVER_IP_ADDRESS = "161.23.77.45";
    private final int SERVER_PORT = 13333;
    private Socket socket = null;
    private DataInputStream input;
    private DataOutputStream output;
    private boolean endOfCommunication;
    private boolean isConnected;
    private ConnectingThread connectingThread;

    public class ConnectingBinder extends Binder{
        public ConnectingService getService(){
            return ConnectingService.this;
        }
    }

    public IBinder onBind(Intent intent) {
        return connectingBinder;
    }

    public void onCreate(){
        super.onCreate();
        isConnected = true;
        endOfCommunication = false;
        try {
            socket = new Socket(SERVER_IP_ADDRESS, SERVER_PORT);
            input = new DataInputStream(socket.getInputStream());
            output = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setListener(String playerId, Handler handler, ConnectingListener connectingListener){
        connectingThread = new ConnectingThread(playerId, handler, connectingListener);
        new Thread(connectingThread).start();
    }

    public void disconnect(){
        isConnected = false;
    }

    public class ConnectingThread implements Runnable{
        private ConnectingListener connectingListener;
        private String playerId;
        private Handler handler;
        private String inputStr;
        private ArrayList<String> respondingJsons;
        private boolean isRegistered;
        public ConnectingThread(String playerId, Handler handler, ConnectingListener connectingListener){
            this.connectingListener = connectingListener;
            this.playerId = playerId;
            this.handler = handler;
            respondingJsons = new ArrayList<String>();
            isRegistered = false;
        }

        public void run() {
            try {

                if (!isRegistered) {
                    DataManager outputData = new DataManager(Protocol.REGISTER_BROADCAST);
                    outputData.putId(playerId);
                    output.writeUTF(outputData.toJson());
                    isRegistered = true;    //TODO Need to check?
                }
                while (isConnected){
                    respondingJsons.clear();
                    while (!endOfCommunication){
                        System.out.println("[][][][][][][][]");
                        inputStr = input.readUTF();
                        System.out.println(">>" + inputStr);
                        Message msg = new Message();
                        msg.what = 1;
                        msg.obj = inputStr;
                        handler.sendMessage(msg);

                        /*
                        if(inputStr.equals("end")){
                            endOfCommunication = true;
                        }else{
                            respondingJsons.add(inputStr);
                        }
                        */
                    }
                    connectingListener.onTaskComplete(respondingJsons);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void onDestroy(){
        super.onDestroy();
        try {
            input.close();
            output.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
