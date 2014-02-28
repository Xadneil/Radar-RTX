package net.devilishro.minimap;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.app.ListActivity;

public class Replay extends ListActivity {
	
	String[] match = {
		"Match 1",
		"Match 2",
		"Match 3",
		"Match 4",
		"Match 5"
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_replay);
		
		setListAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, match));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.replay, menu);
		return true;
	}

}
