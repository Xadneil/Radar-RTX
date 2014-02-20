package edu.usf.cse.minimap;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.maps.model.LatLng;

import edu.usf.cse.minimap.EventActivity.Event;
import edu.usf.cse.minimap.GroupActivity.Group;

public class Network {
    private static Network instance = null;
    private SocketReader client = null;
    private String host = "50.62.212.171";

    public static Network getInstance() {
        if (instance == null) {
            instance = new Network();
        }
        return instance;
    }

    public void send(Packet p) {
        try {
            client.getSocket().getOutputStream().write(p.toByteArray());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void connect(short port) {
        Socket s;
        try {
            s = new Socket(host, port);
            s.setTcpNoDelay(false);
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return;
        }

        client = new SocketReader(s);
        client.start();
    }

    public void disconnect() {
        client.close();
    }

    public void fetchEvents() {
        if (State.networkDebug) {
            State.setEventNumber(1);
            State.getEvents()[0] = new Event("Test", "Provider", new LatLng(
                    28.059891, -82.416183), 17.0f);
            return;
        }
        JSONObject obj = new JSONObject();
        try {
            obj.put("request_events", null);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }
        String s = sendJSONAndReceive(obj, "");
        JSONArray ret;
        try {
            ret = new JSONArray(s);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }
        State.setEventNumber(ret.length());
        for (int i = 0; i < ret.length(); i++) {
            try {
                JSONObject o = ret.getJSONObject(i);
                String title = o.getString("title");
                String provider = o.getString("provider");
                double latitude = o.getDouble("latitude");
                double longitude = o.getDouble("longitude");
                LatLng position = new LatLng(latitude, longitude);
                float zoom = (float) o.getDouble("zoom");
                Event e = new Event(title, provider, position, zoom);
                State.getEvents()[i] = e;
            } catch (JSONException e) {
                e.printStackTrace();
                continue;
            }
        }
    }

    public void fetchGroups() {
        if (State.networkDebug) {
            State.setGroupNumber(1);
            State.getGroups()[0] = new Group("Test Group", 0);
            return;
        }
        JSONObject obj = new JSONObject();
        try {
            obj.put("request_groups", null);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }
        String s = sendJSONAndReceive(obj, "");
        JSONArray ret;
        try {
            ret = new JSONArray(s);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }
        State.setGroupNumber(ret.length());
        for (int i = 0; i < ret.length(); i++) {
            try {
                JSONObject o = ret.getJSONObject(i);
                String name = o.getString("name");
                int capacity = o.getInt("capacity");
                Group group = new Group(name, capacity);
                State.getGroups()[i] = group;
            } catch (JSONException e) {
                e.printStackTrace();
                continue;
            }
        }
    }

    public void reportLocation(double latitude, double longitude) {
        if (State.networkDebug) {

            return;
        }
        JSONObject obj = new JSONObject();
        try {
            obj.put("latitude", latitude);
            obj.put("longitude", longitude);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }
        sendJSON(obj, "");
    }

    public void sendPing(double latitude, double longitude) {
        if (State.networkDebug) {

            return;
        }
        JSONObject obj = new JSONObject();
        try {
            obj.put("latitude", latitude);
            obj.put("longitude", longitude);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }
        sendJSON(obj, "");
    }
}
