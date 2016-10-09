package com.sonicworkflow.serveraddlas;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.sonicworkflow.serveraddlas.custom_adapters.CustomShowListAdapter;
import com.sonicworkflow.serveraddlas.custom_adapters.ShowJsonParse;
import com.sonicworkflow.serveraddlas.custom_adapters.ShowListModel;
import com.sonicworkflow.serveraddlas.network_stuff.AppConfig;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Created by umarbradshaw on 12/4/15.
 */
public class ShowsSetup extends ActionBarActivity {

    private static final String TAG = ShowsSetup.class.getSimpleName();
    Context myContext;

    CustomShowListAdapter showListAdapter;
    ListView listView;
    List<ShowListModel> showListModels;
    Button footerAdd;
    Button footerDelete;

    private ProgressDialog pDialog;
    private Dialog customDialog;

    private String show_name;
    private String show_time;
    private String show_id;
    private String contributor_name = "conNameer";
    private final String device= "Android";
    private String region;

    //random show_id generator
    Random rnd;
    private final String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    //dialog variables
    EditText showNameEditText;
    EditText showTimeEditText;

    //wakelock stuff
    PowerManager powerManager;
    PowerManager.WakeLock wakeLock;

    //location stuff
    String locationProvider;
    Location lastKnownLocation;
    LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shows_list);

        myContext = this;

        rnd = new Random();

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationProvider = LocationManager.NETWORK_PROVIDER;


        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        listView = (ListView) findViewById(R.id.show_list);
        LayoutInflater inflateFooter = getLayoutInflater();
        ViewGroup footer = (ViewGroup) inflateFooter.inflate(R.layout.show_list_footer, listView, false);
        listView.addFooterView(footer);

        footerAdd = (Button) footer.findViewById(R.id.footer_add_show);
        footerDelete = (Button) footer.findViewById(R.id.footer_delete_show);
        getAllTheTributeShows();

        footerAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                customAddDialog();
            }
        });
        footerDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String show_id = showListAdapter.getShow_id();
                if(show_id != null){
                    deleteShow(show_id);
                }else {
                    Toast.makeText(myContext,
                            "Check the show you wish to delete", Toast.LENGTH_LONG).show();
                }
            }
        });

        //set wakelock to prevent app from going to sleep
        powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK,
                "MyWakelockTag");
        wakeLock.acquire();
    }

    public void customAddDialog(){
        customDialog = new Dialog(myContext);
        customDialog.setContentView(R.layout.add_show_dialog);
        customDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        showNameEditText = (EditText) customDialog.findViewById(R.id.show_name_entry);
        showTimeEditText = (EditText) customDialog.findViewById(R.id.show_time_entry);

        Button addShowEntryButton = (Button) customDialog.findViewById(R.id.add_show_entry_button);
        Button cancelShowEntryButton = (Button) customDialog.findViewById(R.id.cancel_show_entry_button);

        lastKnownLocation = locationManager.getLastKnownLocation(locationProvider);

        customDialog.show();

         cancelShowEntryButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 customDialog.dismiss();
                 customDialog.closeOptionsMenu();
             }
         });

        addShowEntryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pDialog.setMessage("Adding Show ...");
                showDialog();

                StringRequest strReq = new StringRequest(Request.Method.POST,
                        AppConfig.SHOW_URL, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "Add Show Response: " + response.toString());
                        hideDialog();
                        try {
                            JSONObject jObj = new JSONObject(response);
                            boolean error = jObj.getBoolean("error");
                            if (!error) {
                                // User successfully stored in MySQL
                                // Now store the user in sqlite
                              /*  String uid = jObj.getString("uid");

                                JSONObject user = jObj.getJSONObject("user");
                                String name = user.getString("name");
                                String email = user.getString("email");
                                String created_at = user
                                        .getString("created_at");*/


                                //dismiss dialog
                                customDialog.dismiss();

                                getAllTheTributeShows();

                            } else {

                                // Error occurred in registration. Get the error
                                // message
                                String errorMsg = jObj.getString("error_msg");
                                Toast.makeText(getApplicationContext(),
                                        errorMsg, Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Add Show Error: " + error.getMessage());
                        Toast.makeText(getApplicationContext(),
                                error.getMessage(), Toast.LENGTH_LONG).show();
                        hideDialog();
                    }
                }) {

                    @Override
                    protected Map<String, String> getParams() {

                        //temporary contributor name

                        // Posting params to register url
                        Map<String, String> params = new HashMap<String, String>();
                        params.put("tag", "store_show");
                        params.put("show_name", showNameEditText.getText().toString());
                        params.put("show_time", showTimeEditText.getText().toString());
                        params.put("show_id", randomString(26));
                        params.put("contributor_name", contributor_name);
                        params.put("device", device);
                        params.put("region", String.valueOf(lastKnownLocation));

                        return params;
                    }

                };

                // Adding request to request queue
                Volley.newRequestQueue(myContext).add(strReq);

            }

        });
    }

    public void getAllTheTributeShows(){
        //show progress dialog while getting values
        pDialog.setMessage("Getting Shows...");
        showDialog();

        //make string request to volley
        StringRequest jsonObjectRequest = new StringRequest(Request.Method.POST,
                AppConfig.SHOW_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i(TAG, response.toString());
                hideDialog();

                if (response.length() > 0) {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                            JSONArray results = jsonObject.getJSONArray("results"); //results is the array that holds the current shows data
                            showListModels = new ArrayList<>();
                            for (int i = 0; i < results.length(); i++) {

                                ShowJsonParse singleShowObject = new ShowJsonParse((JSONObject) results.get(i));

                                showListModels.add(new ShowListModel(singleShowObject.getShowName(), singleShowObject.getShowTime()
                                        , contributor_name, singleShowObject.getShowID()));
                            }
                            //show list adapter
                            showListAdapter = new CustomShowListAdapter(getApplicationContext(), R.layout.item_list_item, showListModels);

                            //set the adapter
                            listView.setAdapter(showListAdapter);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(ShowsSetup.this, "No shows right now!", Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                hideDialog();
            }
        }){
            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<String, String>();
                params.put("tag", "get_show_info_by_contributor_name");
                params.put("contributor_name", contributor_name);

                return params;
            }

        };

        // Adding request to request queue
        Volley.newRequestQueue(myContext).add(jsonObjectRequest);

    }

    public void deleteShow(final String show_id){
        pDialog.setMessage("Adding Show ...");
        showDialog();

        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.SHOW_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Add Show Response: " + response.toString());
                hideDialog();
                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    if (!error) {

                        getAllTheTributeShows();

                    } else {

                        // Error occurred in registration. Get the error
                        // message
                        String errorMsg = jObj.getString("error_msg");
                        Toast.makeText(getApplicationContext(),
                                errorMsg, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Add Show Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {

                // Posting params to register url
                Map<String, String> params = new HashMap<String, String>();
                params.put("tag", "delete_show");
                params.put("show_id", show_id);
                return params;
            }

        };

        // Adding request to request queue
        Volley.newRequestQueue(myContext).add(strReq);
    }

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }

    //random string method for show_id
    private String randomString( int len ){
        StringBuilder sb = new StringBuilder( len );
        for( int i = 0; i < len; i++ )
            sb.append( AB.charAt( rnd.nextInt(AB.length()) ) );
        return sb.toString();
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
            customAddDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(wakeLock.isHeld()){
            wakeLock.release();
        }

        if(showListAdapter.socketIsConnected()){
            showListAdapter.disConnectSocket();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        //wakeLock.release();
        //socket.disconnect();
        if(wakeLock.isHeld()){
            wakeLock.release();
        }

        if(showListAdapter.socketIsConnected()){
            showListAdapter.disConnectSocket();
        }

    }

    @Override
    protected void onRestart() {
        super.onRestart();

        if(!wakeLock.isHeld()) {
            wakeLock.acquire();
        }
        if(!showListAdapter.socketIsConnected()) {
            showListAdapter.connectMySocket();
        }

    }

}
