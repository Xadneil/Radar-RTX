package net.devilishro.minimap;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class Replay extends Activity {

	ArrayList<String> listy;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_replay);
		listy = new ArrayList<String>();
		setList();
		ListView one = (ListView) this.findViewById(R.id.player_list);
		one.setAdapter(new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, listy));
	}

	private void setList() {
		listy.add("Match 1");
		listy.add("Match 2");
		listy.add("Match 3");
	}

}
