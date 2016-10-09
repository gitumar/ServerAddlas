package com.sonicworkflow.serveraddlas.custom_adapters;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.sonicworkflow.serveraddlas.R;
import com.sonicworkflow.serveraddlas.ShowsSetup;
import com.sonicworkflow.serveraddlas.network_stuff.AppConfig;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by umarbradshaw on 12/5/15.
 */
public class CustomShowListAdapter extends ArrayAdapter<ShowListModel> {

    private static final String TAG = ShowsSetup.class.getSimpleName();

    private final List<ShowListModel> showList;
    private final Context context;
    private String show_id;


    private Socket socket;
    {
        try{
            //socket = IO.socket("http://192.168.1.67:3000");
            socket = IO.socket(AppConfig.SOCKET_URL);
        }catch(URISyntaxException e){
            throw new RuntimeException(e);
        }
    }

    public CustomShowListAdapter(Context context, int resource, List<ShowListModel> objects) {
        super(context, resource, objects);

        showList = objects;
        this.context = context;

        //try to connect to socket
        socket.connect();
    }

    static class ViewHolder {
        TextView showName;
        TextView showTime;
        TextView tributeName;
        CheckBox checkBoxToDelete;
        Switch commercialsSwitch;
        Button killMonitoring;
        Button startMonitoring;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        final ViewHolder holder;

        if(convertView==null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            convertView = inflater.inflate(R.layout.item_list_item, null);
            holder = new ViewHolder();

            holder.showName = (TextView) convertView.findViewById(R.id.list_item_show_name);
            holder.showTime = (TextView) convertView.findViewById(R.id.list_item_show_time);
            holder.tributeName = (TextView) convertView.findViewById(R.id.tribute_name);
            holder.checkBoxToDelete = (CheckBox) convertView.findViewById(R.id.checkBoxToDelete);
            holder.commercialsSwitch = (Switch)convertView.findViewById(R.id.commercial_switch);
            holder.killMonitoring = (Button)convertView.findViewById(R.id.kill_monitor);
            holder.startMonitoring = (Button)convertView.findViewById(R.id.start_monitor);


            convertView.setTag(holder);
        }else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.showName.setText(showList.get(position).getShowName());
        holder.showTime.setText(showList.get(position).getShowTime());
        holder.tributeName.setText(showList.get(position).getContributor_name());

        holder.checkBoxToDelete.setFocusable(false);
        holder.checkBoxToDelete.setTag(position);

        holder.checkBoxToDelete.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    show_id = showList.get(position).getShowID();
                    setShow_id(show_id);
                    /*Toast.makeText(context,
                            "You selected "+ show_id, Toast.LENGTH_LONG).show();*/
                }
            }
        });

        holder.commercialsSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    show_id = showList.get(position).getShowID();
                    sendMessage(show_id, "1");
                }else {
                    show_id = showList.get(position).getShowID();
                    sendMessage(show_id, "0");
                }
            }
        });


        holder.killMonitoring.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                show_id = showList.get(position).getShowID();
                sendMessage(show_id, "6");

                holder.killMonitoring.setBackgroundColor(Color.BLUE);
            }
        });

        holder.startMonitoring.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                show_id = showList.get(position).getShowID();
                sendMessage(show_id,"0");

                holder.startMonitoring.setBackgroundColor(Color.GREEN);
            }
        });

        return convertView;
    }

    //this method sends the message to the server via a socket
    private void sendMessage(String message, String status){

        JSONObject sendText = new JSONObject();
        try{
            sendText.put("show_id",message);
            sendText.put("comm_status",status);
            socket.emit("message", sendText);
        }catch(JSONException e){
        }

        updateTheDb(message,status);
    }

    public String getShow_id() {
        return show_id;
    }

    public void setShow_id(String show_id) {
        this.show_id = show_id;
    }

    public void disConnectSocket(){
        if(socket.connected()){
            socket.disconnect();
        }
    }


    public boolean socketIsConnected(){
        return socket.connected();
    }

    public void connectMySocket(){
        socket.connect();
    }


    public void updateTheDb(final String show_id,final String commercial_status){
        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.SHOW_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Change Status Response: " + response.toString());

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    if (!error) {

                    } else {
                        // Error occurred in registration. Get the error
                        // message
                        String errorMsg = jObj.getString("error_msg");
                        Toast.makeText(context,
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
                Toast.makeText(context,
                        error.getMessage(), Toast.LENGTH_LONG).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {

                // Posting params to update show url
                Map<String, String> params = new HashMap<>();
                params.put("tag", "update_show_status");
                params.put("show_id", show_id);
                params.put("show_status", commercial_status);
                return params;
            }
        };

        // Adding request to request queue
        Volley.newRequestQueue(context).add(strReq);
    }



}
