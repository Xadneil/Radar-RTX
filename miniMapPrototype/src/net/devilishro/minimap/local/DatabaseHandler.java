package net.devilishro.minimap.local;

import java.util.ArrayList;
import java.util.List;

import net.devilishro.minimap.AppState;

import com.google.android.gms.maps.model.LatLng;

public class DatabaseHandler {
	
	private static List<LatLng> pos = new ArrayList<LatLng>();
	
	public static void add_point(LatLng point){
		pos.add(point);
	}
	
	public static void send_db(int field_number){
		String temp = null;
		LatLng temp_two = null;
		
		for(int i = 0; i < pos.size(); i++)
		{
			temp_two = pos.get(i);
			temp += temp_two.latitude + ":" + temp_two.longitude + ";";
		}
		
		AppState.add_db(field_number, temp);
	}

}
