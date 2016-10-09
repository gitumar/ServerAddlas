package com.sonicworkflow.serveraddlas;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.sonicworkflow.serveraddlas.network_stuff.AppConfig;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;


public class MainActivity extends ActionBarActivity {


    private ListView mDrawerList;
    private DrawerLayout mDrawerLayout;
    private ArrayAdapter<String> mAdapter;
    private ActionBarDrawerToggle mDrawerToggle;
    private String mActivityTitle;

    Context mContext;
    Activity myActivity;

    private static final String TAG = MainActivity.class.getSimpleName();

    //Socket Connection
    private Socket socket;
    {
        try{
            //socket = IO.socket("http://192.168.1.67:3000");
            socket = IO.socket(AppConfig.SOCKET_URL);
        }catch (URISyntaxException e){
            throw new RuntimeException(e);
        }
    }

    ToggleButton toggleButton;
    ToggleButton toggleButton2;
    ToggleButton toggleButton3;
    ToggleButton toggleButton4;
    ToggleButton toggleButton5;
    ToggleButton toggleButton6;
    ToggleButton toggleButton7;
    ToggleButton toggleButton8;
    TextView textNumberOfShows;

    int numberOfshows;
    String[] showValues;

    PowerManager powerManager;
    PowerManager.WakeLock wakeLock;

    String showValuesZero;
    String showValuesOne;
    String showValuesTwo;
    String showValuesThree;
    String showValuesFour;
    String showValuesFive;
    String showValuesSix;
    String showValuesSeven;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDrawerList = (ListView)findViewById(R.id.navList);
        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        mActivityTitle = getTitle().toString();

        mContext= this;

        addDrawerItems();
        setupDrawer();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        //initializeToggleButtons
        initiaizeToggleButtons();
        //this method puts the long click disables
        setLongClicks();

        //set toggle listeners to send message when toggled
        setSelectListeners();

        //put the value of shows being monitored on the screen
        textNumberOfShows = (TextView)findViewById(R.id.numberofshows);
        textNumberOfShows.setText(String.valueOf(MySharedPrefs.readInteger(mContext,MySharedPrefs.NUM_SHOWS,0)));

        //wakelock code to keep app from going to sleep
        //Todo get wakelock to actually WORK!!!
        powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK,
                "MyWakelockTag");
        wakeLock.acquire();

        //try to connect to socket
        socket.connect();

        //Keep screen on
       // getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

    }

    //this method sends the message to the server via a socket
    private void sendMessage(String message){

        JSONObject sendText = new JSONObject();

        try{
            sendText.put("text",message);
            socket.emit("message", sendText);
        }catch(JSONException e){
        }
    }

    private void addDrawerItems() {
        String[] drawerItems = { "Channel Control", "Set Up Shows", "Other"};
        mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, drawerItems);
        mDrawerList.setAdapter(mAdapter);

        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Toast.makeText(StartActivity.this, "Time for an upgrade!", Toast.LENGTH_SHORT).show();

                switch (position) {
                    case 0:
                        //Toast.makeText(StartActivity.this, "Time to get shows!", Toast.LENGTH_SHORT).show();

                        //close drawer
                        mDrawerLayout.closeDrawers();
                        break;
                    case 1:
                        //Toast.makeText(StartActivity.this, "Time to get shows!", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(MainActivity.this, ShowsSetup.class);
                        startActivity(intent);
                        finish();
                        //close drawer
                        //mDrawerLayout.closeDrawers();
                        break;
                    case 2:
                        //Toast.makeText(StartActivity.this, "Time to get shows!", Toast.LENGTH_SHORT).show();

                        //close drawer
                        mDrawerLayout.closeDrawers();
                        break;
                }
            }
        });
    }

    private void setupDrawer() {
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close) {

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getSupportActionBar().setTitle("Navigation!");
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                getSupportActionBar().setTitle(mActivityTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };

        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        // Activate the navigation drawer toggle
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void initiaizeToggleButtons(){
        toggleButton = (ToggleButton) findViewById(R.id.toggleButton);
        toggleButton2 = (ToggleButton) findViewById(R.id.toggleButton2);
        toggleButton3 = (ToggleButton) findViewById(R.id.toggleButton3);
        toggleButton4 = (ToggleButton) findViewById(R.id.toggleButton4);
        toggleButton5 = (ToggleButton) findViewById(R.id.toggleButton5);
        toggleButton6 = (ToggleButton) findViewById(R.id.toggleButton6);
        toggleButton7 = (ToggleButton) findViewById(R.id.toggleButton7);
        toggleButton8 = (ToggleButton) findViewById(R.id.toggleButton8);
    }
    private void setLongClicks(){
        toggleButton8.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                toggleButton8.setEnabled(false);

                //decrement number of shows
                //showValues = new char[numberOfshows--];

                return false;
            }
        });

        toggleButton7.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                toggleButton7.setEnabled(false);
                //decrement number of shows
                //showValues = new char[numberOfshows--];
                return false;
            }
        });
        toggleButton6.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                toggleButton6.setEnabled(false);
                //decrement number of shows
                //showValues = new char[numberOfshows--];
                return false;
            }
        });
        toggleButton5.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                toggleButton5.setEnabled(false);
                //decrement number of shows
                //showValues = new char[numberOfshows--];
                return false;
            }
        });
        toggleButton4.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                toggleButton4.setEnabled(false);
                //decrement number of shows
                //showValues = new char[numberOfshows--];
                return false;
            }
        });
        toggleButton3.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                toggleButton3.setEnabled(false);
                //decrement number of shows
                //showValues = new char[numberOfshows--];
                return false;
            }
        });
        toggleButton2.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                toggleButton2.setEnabled(false);
                //decrement number of shows
                //showValues = new char[numberOfshows--];
                return false;
            }
        });
        toggleButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                toggleButton.setEnabled(false);
                //decrement number of shows
                //showValues = new char[numberOfshows--];
                return false;
            }
        });

    }

    private void setSelectListeners(){

        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                getTogglesAndEmit();
            }
        });
        toggleButton2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                getTogglesAndEmit();
            }
        });
        toggleButton3.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                getTogglesAndEmit();
            }
        });
        toggleButton4.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                getTogglesAndEmit();
            }
        });
        toggleButton5.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                getTogglesAndEmit();
            }
        });
        toggleButton6.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                getTogglesAndEmit();
            }
        });
        toggleButton7.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                getTogglesAndEmit();
            }
        });
        toggleButton8.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                getTogglesAndEmit();
            }
        });
    }

    public void getTogglesAndEmit(){


        numberOfshows = MySharedPrefs.readInteger(mContext,MySharedPrefs.NUM_SHOWS,0);
        //initialize string array with correct number of shows
        showValues = new String[numberOfshows];

        if(toggleButton.isEnabled()){
            if(toggleButton.isChecked()){
                showValues[0]="1";

            }else {
                showValues[0]= "0";
            }
            setShowValuesZero(showValues[0]); //initialize setter
        }
        if(toggleButton2.isEnabled()){
            if(toggleButton2.isChecked()){
                showValues[1]="1";
            }else {
                showValues[1]= "0";
            }
            setShowValuesOne(showValues[1]); //initialize setter
        }
        if(toggleButton3.isEnabled()){
            if(toggleButton3.isChecked()){
                showValues[2]="1";
            }else {
                showValues[2]= "0";
            }
            setShowValuesTwo(showValues[2]); //initialize setter
        }
        if(toggleButton4.isEnabled()){
            if(toggleButton4.isChecked()){
                showValues[3]="1";
            }else {
                showValues[3]= "0";
            }
            setShowValuesThree(showValues[3]); //initialize setter
        }
        if(toggleButton5.isEnabled()){
            if(toggleButton5.isChecked()){
                showValues[4]="1";
            }else {
                showValues[4]= "0";
            }
            setShowValuesFour(showValues[4]); //initialize setter
        }
        if(toggleButton6.isEnabled()){
            if(toggleButton6.isChecked()){
                showValues[5]="1";
            }else {
                showValues[5]= "0";
            }
            setShowValuesFive(showValues[5]); //initialize setter
        }
        if(toggleButton7.isEnabled()){
            if(toggleButton7.isChecked()){
                showValues[6]="1";
            }else {
                showValues[6]= "0";
            }
            setShowValuesSix(showValues[6]); //initialize setter
        }
        if(toggleButton8.isEnabled()){
            if(toggleButton8.isChecked()){
                showValues[7]="1";
            }else {
                showValues[7]= "0";
            }
            setShowValuesSeven(showValues[7]); //initialize setter
        }


        StringBuilder builder = new StringBuilder();
        for(String s : showValues) {
            builder.append(s);
        }
        String message = builder.toString();

        sendMessage(message);

    }


    public String getShowValuesZero() {
        return showValuesZero;
    }

    public void setShowValuesZero(String showValuesZero) {
        this.showValuesZero = showValuesZero;
    }

    public String getShowValuesOne() {
        return showValuesOne;
    }

    public void setShowValuesOne(String showValuesOne) {
        this.showValuesOne = showValuesOne;
    }

    public String getShowValuesTwo() {
        return showValuesTwo;
    }

    public void setShowValuesTwo(String showValuesTwo) {
        this.showValuesTwo = showValuesTwo;
    }

    public String getShowValuesThree() {
        return showValuesThree;
    }

    public void setShowValuesThree(String showValuesThree) {
        this.showValuesThree = showValuesThree;
    }

    public String getShowValuesFour() {
        return showValuesFour;
    }

    public void setShowValuesFour(String showValuesFour) {
        this.showValuesFour = showValuesFour;
    }

    public String getShowValuesFive() {
        return showValuesFive;
    }

    public void setShowValuesFive(String showValuesFive) {
        this.showValuesFive = showValuesFive;
    }

    public String getShowValuesSix() {
        return showValuesSix;
    }

    public void setShowValuesSix(String showValuesSix) {
        this.showValuesSix = showValuesSix;
    }

    public String getShowValuesSeven() {
        return showValuesSeven;
    }

    public void setShowValuesSeven(String showValuesSeven) {
        this.showValuesSeven = showValuesSeven;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //wakeLock.release();
        //the connected button has been pressed
        if(wakeLock.isHeld()){
            wakeLock.release();
        }

        if(socket.connected()){
            socket.disconnect();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        //wakeLock.release();
        //socket.disconnect();
        if (wakeLock.isHeld()) {
            wakeLock.release();
        }

        if (socket.connected()) {
            socket.disconnect();
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if(!wakeLock.isHeld()) {
            wakeLock.acquire();
        }
    }
}
