package net.devilishro.minimap;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class Note_acti extends Activity {

	ArrayList<String> listy;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_note_acti);
		listy = new ArrayList<String>();
		setList();
		ListView one = (ListView) this.findViewById(R.id.listView1);
		one.setAdapter(new CoolAdapter(this, listy));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.note_acti, menu);
		return true;
	}

	private void setList() {
		listy.add("Player 1");
		listy.add("Player 2");
		listy.add("Player 2");
		listy.add("Player 4");
		listy.add("Player 5");
	}

	private static class CoolAdapter extends ArrayAdapter<String> {
		Context context;
		ArrayList<String> b;

		public CoolAdapter(Note_acti a, ArrayList liste) {
			super(a, R.id.listView1, liste);
			b = liste;
			context = a;
		}

		@Override
		public View getView(int pos, View convert, ViewGroup parent) {
			String temp;
			if (convert == null)
				convert = View.inflate(context, R.layout.activity_notification,
						null);
			try {
				temp = b.get(pos);
			} catch (Exception e) {
				temp = null;
			}

			if (temp != null)
				((TextView) convert.findViewById(R.id.textView1)).setText(temp);
			return convert;
		}

	}

}