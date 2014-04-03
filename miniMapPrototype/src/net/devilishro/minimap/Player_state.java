package net.devilishro.minimap;

import net.devilishro.minimap.network.Network;
import net.devilishro.minimap.network.PacketCreator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class Player_state extends Activity {

	@SuppressLint("HandlerLeak")
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == 0) {
				String players[] = (String[]) msg.obj;
				ListAdapter adapter = new ArrayAdapter<String>(
						Player_state.this, R.id.player_list, players);
				list.setAdapter(adapter);
			} else if (msg.what == 1) {
				PlayerInfo info = (PlayerInfo) msg.obj;

				((TextView) Player_state.this.findViewById(R.id.player_login))
						.setText(info.login);

				((TextView) Player_state.this.findViewById(R.id.player_event))
						.setText(info.event);

				((TextView) Player_state.this.findViewById(R.id.player_team))
						.setText(info.team);
			}
		}
	};

	private OnItemClickListener listener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			String name = (String) list.getAdapter().getItem(position);
			AppState.getEventServer().send(PacketCreator.playerInfo(name));
			((TextView) Player_state.this.findViewById(R.id.player_name))
					.setText(name);
		}
	};

	private ListView list;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_player_state);
		list = (ListView) findViewById(R.id.player_list);
		list.setOnItemClickListener(listener);
	}

	@Override
	public void onResume() {
		super.onResume();
		AppState.getEventServer().registerContext(this,
				Network.Activities.PLAYER_LIST);
		AppState.getEventServer().send(PacketCreator.playerList());
	}

	@Override
	public void onPause() {
		super.onPause();
		AppState.getEventServer().unregisterContext(
				Network.Activities.PLAYER_LIST);
	}

	public void refresh(String[] names) {
		handler.obtainMessage(0, names).sendToTarget();
	}

	public void info(PlayerInfo info) {
		handler.obtainMessage(1, info).sendToTarget();
	}

	public static class PlayerInfo {
		public String name, login, event, team;
	}
}
