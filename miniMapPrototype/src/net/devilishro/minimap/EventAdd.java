package net.devilishro.minimap;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

public class EventAdd extends Activity {
	String[] eventMatchTypeList;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.eventadd);
		
		// setup spinner
		eventMatchTypeList = getResources().getStringArray(R.array.eventmatchtypelst);
		Spinner eventMatchTypeSpinner = (Spinner) findViewById(R.id.eventmatchtype);
		ArrayAdapter<String> eventMatchTypeAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, eventMatchTypeList);
		eventMatchTypeSpinner.setAdapter(eventMatchTypeAdapter);
		eventMatchTypeSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				int index = arg0.getSelectedItemPosition();
				Toast.makeText(getBaseContext(), eventMatchTypeList[index], Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {}
		});
	}
}
