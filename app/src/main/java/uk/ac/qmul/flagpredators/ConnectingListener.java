package uk.ac.qmul.flagpredators;

import java.util.ArrayList;

/**
 * Created by Ming-Jiun Huang on 15/7/19.
 * Contact me at m.huang@hss13.qmul.ac.uk
 * Call back for ConnectToServer AsyncTask
 */
public interface ConnectingListener {
    void onTaskComplete(ArrayList<String> respondingJsons);
}
