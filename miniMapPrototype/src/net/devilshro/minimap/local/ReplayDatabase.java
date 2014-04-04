package net.devilshro.minimap.local;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/*********************************************************
 * 
 * @author David
 *
 ********************************************************/

//imported from Google

public class ReplayDatabase extends SQLiteOpenHelper{

	private static final int DATABASE_VERSION = 1;
	private static final String DATABASE_NAME = "replay_database";
	private static final String TABLE_ONE = "Field One";
	private static final String TABLE_TWO = "Field Two";
	private static final String TABLE_THREE = "Field Three";
	private static final String KEY_ID = "id";
	private static final String KEY_NAME = "Pos_Data";
	
	public ReplayDatabase(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void onCreate(SQLiteDatabase db){
		String CREATE_TABLE = "CREATE TABLE " + TABLE_ONE + "(" 
				+ KEY_ID + "INTEGER PRIMARY KEY," + KEY_NAME + "TEXT" + ")";
		db.execSQL(CREATE_TABLE);
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int old, int new_v){
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_ONE);
		onCreate(db);
	}
	
	//add a string of all the points the match gives.
	public void addPoints(String points, int field_number){
		SQLiteDatabase db = this.getWritableDatabase();
		
		ContentValues input = new ContentValues();
		input.put(KEY_NAME, points);
			
		switch(field_number){
		case 1:
			db.insert(TABLE_ONE, null, input);
			break;
		case 2:
			db.insert(TABLE_TWO, null, input);
			break;
		case 3:
			db.insert(TABLE_THREE, null, input);
			break;
		}
		
		db.close();
	}
	
	//Returns a string of all the points saved
	public List<String> readPoints(int field_number) {
		List<String> all_points = new ArrayList<String>();
		SQLiteDatabase db = this.getWritableDatabase();
		String select = null;
		Cursor curs = null;
		switch(field_number){
			case 1:
				select = "SELECT * FROM " + TABLE_ONE;
				break;
			case 2:
				select = "SELECT * FROM " + TABLE_TWO;
				break;
			case 3:
				select = "SELECT * FROM " + TABLE_THREE;
				break;
		}
		
		if(select != null){
			curs = db.rawQuery(select, null);
			
			if(curs.moveToFirst())
			{
				do
				{
					all_points.add(curs.getString(1));
				}while(curs.moveToLast());
			}
		}
		return all_points;	
	}
	//wipes the db for next match
	public void resetDatabase(int field_number){
		SQLiteDatabase db = this.getWritableDatabase();
		
		switch(field_number){
		case 1:
			db.delete(TABLE_ONE, null, null);
			break;
		case 2:
			db.delete(TABLE_TWO, null, null);
			break;
		case 3:
			db.delete(TABLE_THREE, null, null);
			break;
		}
	}
};
