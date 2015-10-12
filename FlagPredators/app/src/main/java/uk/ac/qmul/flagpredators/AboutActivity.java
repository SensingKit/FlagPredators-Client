package uk.ac.qmul.flagpredators;

import android.content.Context;
import android.os.Vibrator;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

/**
 * Created by Ming-Jiun Huang on 15/7/02.
 * Contact me at m.huang@hss13.qmul.ac.uk
 */

public class AboutActivity extends ActionBarActivity {
    private Vibrator mVibrator;
    public static long[] PATTERN_GET_FLAG = {0, 250, 50, 250, 50, 250, 50, 800, 100,
                                                250, 50, 250, 50, 250, 50, 800,100};
    public static long[] PATTERN_BACK_TO_BASE = {0, 600, 50, 125, 50, 125, 50, 125, 50 ,125, 100,
                                        600, 50, 125, 50, 125, 50, 125, 50 ,125, 100};
    public static long[] PATTERN_OUT_OF_BOUNDARY = {0, 150, 50, 300, 50, 150, 50, 300, 50, 150, 50, 300, 100,
                                                        150, 50, 300, 50, 150, 50, 300, 50, 150, 50, 300};
    public static long[] PATTERN_GET_CAUGHT = {0, 400, 50, 125, 50, 125, 50,
                                                    400, 50, 125, 50, 125, 50,
                                                    400, 50, 125, 50, 125};
    public static long[] PATTERN_GAME_OVER = {0, 100, 50, 2500};

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        this.setTitle("ABOUT");
        mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
    }

    public void getFlagPatternOnClick(View view){
        mVibrator.vibrate(PATTERN_GET_FLAG, -1);
    }

    public void getBasePatternOnClick(View view){
        long[] pattern = {0,125, 250, 125, 125, 125, 250, 125, 375, 125, 250, 125, 125, 125, 125, 125, 125, 125, 250, 125};
        long[] pattern1 = {0,250, 500, 250, 250, 250, 500, 250, 750, 250, 500, 250, 250, 250, 250, 250, 250, 250, 500, 250};
        mVibrator.vibrate(PATTERN_BACK_TO_BASE, -1);
    }

    public void outPatternOnClick(View view){
        mVibrator.vibrate(PATTERN_OUT_OF_BOUNDARY, -1);
    }

    public void getCaughtPatternOnClick(View view){
        mVibrator.vibrate(PATTERN_GET_CAUGHT, -1);
    }

    public void goBackToMainOnClick(View view){
        finish();
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
        finish();
    }
    public void onDestroy(){
        super.onDestroy();
        System.out.println("*****Destroying*****");
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_about, menu);
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
