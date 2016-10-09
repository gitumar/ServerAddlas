package com.sonicworkflow.serveraddlas;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;


public class ChannelSetup extends ActionBarActivity {

    EditText numberOfShows;
    Button acceptChannels;
    int intNumberOfShows;

    Context myContext;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_channel_setup);

        myContext = this;

        numberOfShows = (EditText) findViewById(R.id.numberofshows);
        acceptChannels = (Button) findViewById(R.id.acceptchannelbutton);


        acceptChannels.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //store number of channels in shared prefs

                intNumberOfShows=Integer.parseInt(numberOfShows.getText().toString());

                MySharedPrefs.writeInteger(myContext, MySharedPrefs.NUM_SHOWS, intNumberOfShows);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_channel_setup, menu);
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

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {

        Intent intent = new Intent(ChannelSetup.this, MainActivity.class);
        startActivity(intent);
        finish();
        super.onBackPressed();
    }
}
