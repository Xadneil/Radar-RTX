package net.devilishro.minimap;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class Replay extends Activity {

	ArrayList<String> listy;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_replay);
		listy = new ArrayList<String>();
		setList();
		ListView one = (ListView) this.findViewById(R.id.team1_list);
		one.setAdapter(new CoolAdapter(this, listy));
	}

	private void setList() {
		listy.add("Match 1");
		listy.add("Match 2");
		listy.add("Match 3");
	}
	
	private static class CoolAdapter extends ArrayAdapter<String> {
		Context context;
		ArrayList<String> b;

		public CoolAdapter(Replay player_state, ArrayList<String> liste) {
			super(player_state, R.id.team1_list, liste);
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
