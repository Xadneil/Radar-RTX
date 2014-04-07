package net.devilishro.minimap;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class Replay extends Activity {

	ArrayList<String> listy;

	private OnItemClickListener clickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View v, int position,
			long id) 
		{
			start_replay();
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_replay);
		listy = new ArrayList<String>();
		setList();
		ListView one = (ListView) this.findViewById(R.id.player_list);

		one.setAdapter(new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, listy));
		one.setOnItemClickListener(clickListener);
	}

	private void setList() {
		listy.add("Match 1");
		listy.add("Match 2");
		listy.add("Match 3");
	}

	private void start_replay()
	{
		Intent i = new Intent(this, ReplayerActivity.class);
		startActivity(i);
	}
}
