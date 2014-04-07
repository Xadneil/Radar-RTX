package net.devilishro.minimap.local;

import java.util.ArrayList;
import java.util.List;

import net.devilishro.minimap.AppState;
import android.content.ContentValues;

import com.google.android.gms.maps.model.LatLng;

public class DatabaseHandler {
	
	private static List<LatLng> pos = new ArrayList<LatLng>();
	
	public static void add_point(LatLng point, int p_number){
		AppState.add_db(point, p_number, 0);
	}
	
	public static void send_db(){
		AppState.add_db(new LatLng(0,0), -3, 1); //to set the place holder between a interval of points
	}
	
	public static ArrayList<ContentValues> recv_db(int last_saved){
		return AppState.recv_points(last_saved);
	}
	

}
