package uk.ac.qmul.flagpredators;

import android.os.AsyncTask;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Created by Ming-Jiun Huang on 15/6/11.
 * Contact me at m.huang@hss13.qmul.ac.uk
 */
public class ConnectToServer extends AsyncTask<String,Integer,ArrayList<String>>{
    private ConnectingListener connectingListener;
    private final static String SERVER_IP = "161.23.77.45";
    private final static int SERVER_PORT = 13333;
    private String inputStr;
    private ArrayList<String> respondingJsons;
    private Socket socket = null;
    private DataInputStream input;
    private DataOutputStream output;
    private boolean endOfCommunication;


    public ConnectToServer(ConnectingListener connectingListener){
        this.connectingListener = connectingListener;
        respondingJsons = new ArrayList<String>();
    }

    protected ArrayList<String> doInBackground(String... json) {
        try {
            socket = new Socket(SERVER_IP,SERVER_PORT);
            if(socket.isBound()){
                input = new DataInputStream(socket.getInputStream());
                output = new DataOutputStream(socket.getOutputStream());
                output.writeUTF(json[0]);
                output.flush();//??
                endOfCommunication = false;
                while(!endOfCommunication){
                    inputStr = input.readUTF();
                    System.out.println("~~~~~~~~~" + inputStr);
                    if(inputStr.equals("end")){
                        endOfCommunication = true;
                    }else {
                        respondingJsons.add(inputStr);
                    }
                }
                input.close();
                output.close();
                socket.close(); //TODO DO I HAVE TO CLOSE THE SOCKET? REUSE?
            }
        }catch (IOException e){
            e.printStackTrace();
        }
        return respondingJsons;
    }

    protected void onPostExecute(ArrayList<String> respondingJson){
        super.onPostExecute(respondingJsons);
        connectingListener.onTaskComplete(respondingJsons); //Callback
    }

    protected void onCancelled(){
        super.onCancelled();
        try {
            input.close();
            output.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
