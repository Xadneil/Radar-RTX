package net.devilishro.minimap;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

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
		one.setAdapter(new CoolAdapter(this, listy));
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
	
	private static class CoolAdapter extends ArrayAdapter<String> {
		Context context;
		ArrayList<String> b;

		public CoolAdapter(Replay player_state, ArrayList<String> liste) {
			super(player_state, R.id.player_list, liste);
			b = liste;
			context = player_state;
		}

		@Override
		public View getView(int pos, View convert, ViewGroup parent) {
			String temp;
			if (convert == null)
				convert = View.inflate(context, R.layout.state,
						null);
			try {
				temp = b.get(pos);
			} catch (Exception e) {
				temp = null;
			}

			if (temp != null)
				((TextView) convert.findViewById(R.id.login_error_view)).setText(temp);
			return convert;
		}

	}
	
}
