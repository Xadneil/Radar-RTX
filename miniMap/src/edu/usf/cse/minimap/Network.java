package edu.usf.cse.minimap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.maps.model.LatLng;

import edu.usf.cse.minimap.EventActivity.Event;
import edu.usf.cse.minimap.GroupActivity.Group;

public class Network {
    private static Network instance = null;
    private HttpClient client = new DefaultHttpClient();
    private String host = "50.62.212.171";

    public static Network getInstance() {
        if (instance == null) {
            instance = new Network();
        }
        return instance;
    }

    public boolean login(final String email, final String passHash) {
        if (State.networkDebug) {
            return true;
        }
        JSONObject o = new JSONObject();
        try {
            o.put("email", email);
            o.put("password", passHash);
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
        String s = sendJSONAndReceive(o, "");
        JSONObject ret;
        boolean success;
        try {
            ret = new JSONObject(s);
            success = ret.getBoolean("success");
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
        return success;
    }

    /**
     * Requests positions, ping, and drawing from network
     * 
     * @return If anything on the map needs to be updated
     */
    public boolean requestMapState() {
        if (State.networkDebug) {
            return true;
        }
        JSONObject obj = new JSONObject();
        try {
            // TODO figure out session v. individual ID
            // obj.put("email", email);
            obj.put("request_state", null);
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
        String s = sendJSONAndReceive(obj, "");
        JSONObject ret;
        try {
            ret = new JSONObject(s);
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
        boolean update = false;
        boolean hasPositions, hasPing, hasDraw;
        try {
            hasPositions = ret.getBoolean("has_position");
            hasPing = ret.getBoolean("has_ping");
            hasDraw = ret.getBoolean("has_draw");
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
        if (hasPositions) {
            JSONArray positions;
            try {
                positions = ret.getJSONArray("positions");
            } catch (JSONException e) {
                e.printStackTrace();
                return false;
            }
            synchronized (State.getPositionsLock()) {
                State.setHasNewPositions(true);
                for (int i = 0; i < positions.length(); i++) {
                    try {
                        JSONObject o = positions.getJSONObject(i);
                        int number = o.getInt("number");
                        double latitude = o.getDouble("latitude");
                        double longitude = o.getDouble("longitude");
                        LatLng l = new LatLng(latitude, longitude);
                        State.getPositions()[number] = l;
                        update = true;
                    } catch (JSONException e) {
                        e.printStackTrace();
                        continue;
                    }
                }
            }
        }
        if (hasPing) {
            update = true;
            synchronized (State.getPingLock()) {
                try {
                    double latitude = ret.getDouble("ping_latitude");
                    double longitude = ret.getDouble("ping_longitude");
                    State.setPing(new LatLng(latitude, longitude));
                    State.setHasNewPing(true);
                } catch (JSONException e) {
                    e.printStackTrace();
                    return false;
                }
            }
        }
        if (hasDraw) {

        }
        return update;
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

    private boolean sendJSON(JSONObject o, String location) {
        try {
            HttpPost postReq = new HttpPost(host + "/" + location);
            StringEntity se = new StringEntity(o.toString());
            se.setContentType("application/json;charset=UTF-8");
            se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE,
                    "application/json;charset=UTF-8"));
            postReq.setEntity(se);
            HttpResponse response = client.execute(postReq);
            return 200 == response.getStatusLine().getStatusCode();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Code source: Osama Shabrez
     * 
     * @param o
     * @param location
     * @return
     */
    private String sendJSONAndReceive(JSONObject o, String location) {
        try {
            HttpPost postReq = new HttpPost(host + "/" + location);
            StringEntity se = new StringEntity(o.toString());
            se.setContentType("application/json;charset=UTF-8");
            se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE,
                    "application/json;charset=UTF-8"));
            postReq.setEntity(se);
            HttpResponse response = client.execute(postReq);

            HttpEntity resultEntity = response.getEntity();
            InputStream inputStream = resultEntity.getContent();
            // gzip stuff
            // Header contentEncoding =
            // httpresponse.getFirstHeader("Content-Encoding");
            // if(contentEncoding != null &&
            // contentencoding.getValue().equalsIgnoreCase("gzip")) {
            // inputStream = new GZIPInputStream(inputStream);
            // }
            String ret = convertStreamToString(inputStream);
            inputStream.close();
            return ret;
        } catch (UnsupportedEncodingException ex) {

        } catch (ClientProtocolException ex) {

        } catch (IOException ex) {

        }
        return null;
    }

    /**
     * Code source: Osama Shabrez
     * 
     * @param is
     * @return
     */
    private String convertStreamToString(InputStream is) {
        String line = "";
        StringBuilder total = new StringBuilder();
        BufferedReader rd = new BufferedReader(new InputStreamReader(is));
        try {
            while ((line = rd.readLine()) != null) {
                total.append(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return total.toString();
    }
}
