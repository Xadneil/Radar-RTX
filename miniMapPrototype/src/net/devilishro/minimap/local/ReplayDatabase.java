package net.devilishro.minimap.local;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

/*********************************************************
 * 
 * @author David
 *
 ********************************************************/

//imported from Google

public class ReplayDatabase extends SQLiteOpenHelper{

	private static final int DATABASE_VERSION = 1;
	private static final String DATABASE_NAME = "replay_database";
	private static final String TABLE_ONE = "\"Field One\"";
	public static final String KEY_ID = "id";
	public static final String KEY_NAME1 = "Pos_PID";
	public static final String KEY_NAME2 = "Pos_Lat";
	public static final String KEY_NAME3 = "Pos_Long";
	public static final String KEY_NAME4 = "Pos_End";
	private final String TAG = "ReplayDatabase";
	private int key_count = 0;
	
	
	
	public ReplayDatabase(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void onCreate(SQLiteDatabase db){
		String CREATE_TABLE = "CREATE TABLE " + TABLE_ONE + " (" 
				+ KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + KEY_NAME1 + " INTEGER, " + KEY_NAME2 + " REAL, " + KEY_NAME3
				+ " REAL, " + KEY_NAME4 + " INTEGER" + ")";
		db.execSQL(CREATE_TABLE);
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int old, int new_v){
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_ONE);
		onCreate(db);
	}
	
	//add a string of all the points the match gives.
	public void addPoints(LatLng points, int player, int end){
		SQLiteDatabase db = this.getWritableDatabase();
		
		ContentValues input = new ContentValues();
		//input.put(KEY_ID, key_count);
		input.put(KEY_NAME1, player);
		input.put(KEY_NAME2, points.latitude);
		input.put(KEY_NAME3, points.longitude);
		if(end == 1)
			input.put(KEY_NAME4, 1);
		else
			input.put(KEY_NAME4, 0);
		Log.d(TAG, "About to add points to db");
		long result = db.insert(TABLE_ONE, null, input);
		
			Log.d(TAG, "Inserted " + result);
		key_count++;
		db.close();
	}
	
	
	//Returns a string of all the points saved for a certain time
	public ArrayList<ContentValues> readPoints(int count) {
		SQLiteDatabase db = this.getWritableDatabase();
		ArrayList<ContentValues> recv_points = new ArrayList<ContentValues>();
		ContentValues temp = new ContentValues();
		boolean run = true;
		String select = null;
		Cursor curs = null;
		int check = 0;
		int count_one = count;
		while(run)
		{
			curs = db.query(TABLE_ONE, new String[] { KEY_ID, KEY_NAME1, KEY_NAME2, KEY_NAME3, KEY_NAME4}, KEY_ID + "=?", new String[] { String.valueOf(count_one) }, null, null, null, null);
			if(curs != null)
				curs.moveToFirst();
			
			Log.d(TAG, "Cursor size: " + curs.getCount());
			
			temp.put(KEY_ID, curs.getInt(0));
			temp.put(KEY_NAME1, curs.getInt(1));
			temp.put(KEY_NAME2, curs.getDouble(2));
			temp.put(KEY_NAME3, curs.getDouble(3));
			temp.put(KEY_NAME4, curs.getInt(4));
			
			check = (Integer) temp.get(KEY_NAME4);
			if (check == 1)
			{
				run = false;
				break; //i know it does the same as the prev instruction
			} 
			else
			{
				recv_points.add(temp);
			}
			count_one++;
		}
		db.close();
		return recv_points;
		
	}
	//wipes the db for next match
	public void resetDatabase(int field_number){
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete(TABLE_ONE, null, null);	
	}
};
