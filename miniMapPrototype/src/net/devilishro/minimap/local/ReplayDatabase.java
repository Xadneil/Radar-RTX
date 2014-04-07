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

// imported from Google

public class ReplayDatabase extends SQLiteOpenHelper {

	private static final int DATABASE_VERSION = 1;
	private static final String DATABASE_NAME = "replay_database";
	private static final String TABLE_ONE = "\"Field One\"";
	public static final String KEY_ID = "id";
	public static final String Column_playerID = "Pos_PID";
	public static final String Column_lat = "Pos_Lat";
	public static final String Column_lng = "Pos_Long";
	public static final String Column_delimiter = "Pos_End";
	private final String TAG = "ReplayDatabase";
	private int key_count = 0;

	public ReplayDatabase(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		String CREATE_TABLE = "CREATE TABLE " + TABLE_ONE + " (" + KEY_ID
				+ " INTEGER PRIMARY KEY, " + Column_playerID
				+ " INTEGER, " + Column_lat + " REAL, " + Column_lng + " REAL, "
				+ Column_delimiter + " INTEGER" + ")";
		db.execSQL(CREATE_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int old, int new_v) {
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_ONE);
		onCreate(db);
	}

	// add a string of all the points the match gives.
	public void addPoints(LatLng points, int player, int end) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues input = new ContentValues();
		long result = -1;
		input.put(KEY_ID, key_count);
		input.put(Column_playerID, player);
		input.put(Column_lat, points.latitude);
		input.put(Column_lng, points.longitude);
		input.put(Column_delimiter, end);

		Log.d(TAG, "Key: " + key_count + " | ID: " + player + " | Lat: "
				+ points.latitude + " | Lng: " + points.longitude);
		
		db.beginTransaction();
		try {
			result = db.insert(TABLE_ONE, null, input);
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
		Log.d(TAG, "Key_Id: " + key_count);
		Log.d(TAG, "Entry to DB Row: " + result);

		key_count++;
		db.close();
	}

	// Returns a string of all the points saved for a certain time
	public ArrayList<ContentValues> readPoints(int count) {
		SQLiteDatabase db = this.getWritableDatabase();
		ArrayList<ContentValues> recv_points = new ArrayList<ContentValues>();
		ContentValues temp;
		Cursor curs = null;
		int count_one = count;

		int id = 0;
		int pid = 0;
		double latitude = 0;
		double longitude = 0;
		int mark = 0;

		while (true) {
			temp = new ContentValues();
			curs = db.query(TABLE_ONE, new String[] { KEY_ID, Column_playerID,
					Column_lat, Column_lng, Column_delimiter }, KEY_ID + "=?",
					new String[] { String.valueOf(count_one) }, null, null,
					null, null);
			if (curs != null)
				curs.moveToFirst();
			else {
				Log.d(TAG, "Failed to retrieve row: " + count_one);
				break;
			}

			id = curs.getInt(0);
			pid = curs.getInt(1);
			latitude = curs.getDouble(2);
			longitude = curs.getDouble(3);
			mark = curs.getInt(4);

			temp.put(KEY_ID, id);
			temp.put(Column_playerID, pid);
			temp.put(Column_lat, latitude);
			temp.put(Column_lng, longitude);
			temp.put(Column_delimiter, mark);

			Log.d(TAG, "Count: " + curs.getCount() + " " + temp.toString());
			recv_points.add(temp);
			if (mark == 1)
				break;
			//else {
				
			//}
			count_one++;
		}
		db.close();
		Log.d(TAG, "Added player id " + recv_points.get(0).getAsInteger(Column_playerID) + " to the list");
		return recv_points;

	}

	// wipes the db for next match
	public void resetDatabase() {
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete(TABLE_ONE, null, null);
		key_count = 0;
		db.close();
	}
};
