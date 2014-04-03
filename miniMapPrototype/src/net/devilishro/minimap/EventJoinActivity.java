package net.devilishro.minimap;

import net.devilishro.minimap.network.Network;
import net.devilishro.minimap.network.PacketCreator;
import net.devilishro.minimap.network.PacketHandlers;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class EventJoinActivity extends Activity {

	OnClickListener listener = new OnClickListener() {
		@Override
		public void onClick(View view) {
			String team = view.getId() == R.id.group_1_button ? AppState
					.getCurrentEvent().team1 : AppState.getCurrentEvent().team1;
			AppState.getEventServer().send(PacketCreator.joinTeam(team));
			chosenTeam = team;
			if (AppState.networkBypass) {
				PacketHandlers.teamJoin.handlePacket(null, AppState
						.getEventServer(), AppState.getEventServer()
						.getContext());
			}
		}
	};

	private String chosenTeam;

	@SuppressLint("HandlerLeak")
	public Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == 0) {
				finish();
			} else if (msg.what == 1) {
				int team = msg.arg1;
				ListView view = team == 0 ? team1List : team2List;
				view.setAdapter(new ArrayAdapter<String>(
						EventJoinActivity.this,
						android.R.layout.simple_list_item_1, AppState
								.getTeamNames(team).toArray(new String[0])));
			} else if (msg.what == 2) {
				AppState.getFieldServer().registerContext(
						EventJoinActivity.this, Network.Activities.TEAM_JOIN);
				if (!AppState.getFieldServer().isRunning()
						&& !AppState.getFieldServer().isError()) {
					AppState.getFieldServer().start();
				}
				new Thread() {
					@Override
					public void run() {
						while (!AppState.getFieldServer().isRunning()
								&& !AppState.getFieldServer().isError()) {
							try {
								Thread.sleep(200);
							} catch (InterruptedException e) {
							}
						}
						if (!AppState.getFieldServer().isError()) {
							AppState.getFieldServer().send(
									PacketCreator.fieldConnect(chosenTeam));
							if (AppState.networkBypass) {
								PacketHandlers.fieldConnect.handlePacket(null,
										AppState.getFieldServer(), AppState
												.getFieldServer().getContext());
							}
						}
					}
				}.start();
			} else if (msg.what == 3) {
				String error = "";
				int status = msg.arg1;
				if ((status & 0x0002) != 0) {
					// team full
					error += "Event is full. ";
				}
				if ((status & 0x0004) != 0) {
					// team doesn't exist
					error += "Team doesn't exist. ";
				}
				if ((status & 0x0008) != 0) {
					// event doesn't exist
					error += "Event doesn't exist.";
				}
				if ((status & 0x0010) != 0 || (status & 0x0020) != 0) {
					// already in event, team; shouldn't happen
					throw new RuntimeException("Already in event or team!");
				}
				if (!"".equals(error)) {
					Toast.makeText(EventJoinActivity.this, error,
							Toast.LENGTH_LONG).show();
					finish();
				}
			} else if (msg.what == 4) {
				Intent i = new Intent(EventJoinActivity.this, MapActivity.class);
				startActivity(i);
			}
		}
	};

	private ListView team1List, team2List;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_event_join);
		((TextView) this.findViewById(R.id.event_join_name)).setText(AppState
				.getCurrentEvent().title);
		((Button) this.findViewById(R.id.group_1_button))
				.setOnClickListener(listener);
		((Button) this.findViewById(R.id.group_2_button))
				.setOnClickListener(listener);
		team1List = (ListView) findViewById(R.id.player_list);
		team2List = (ListView) findViewById(R.id.team2_list);

		if (AppState.getCurrentEvent().team1 != null
				&& AppState.getCurrentEvent().team2 != null) {
			((Button) findViewById(R.id.group_1_button)).setText("Join "
					+ AppState.getCurrentEvent().team1);
			((Button) findViewById(R.id.group_2_button)).setText("Join "
					+ AppState.getCurrentEvent().team2);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		AppState.getEventServer().registerContext(this,
				Network.Activities.TEAM_JOIN);
	}

	@Override
	public void onPause() {
		AppState.getEventServer().unregisterContext(
				Network.Activities.TEAM_JOIN);
		super.onPause();
	}

	@Override
	public void onDestroy() {
		// in case finish() is called
		AppState.getEventServer().unregisterContext(
				Network.Activities.TEAM_JOIN);
		super.onDestroy();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// If back button pushed, send leave event packet, let handler change
		// activity
		if ((keyCode == KeyEvent.KEYCODE_BACK)) {
			AppState.getEventServer().send(
					PacketCreator.leaveEvent(AppState.getCurrentEvent().id));
			if (AppState.networkBypass) {
				PacketHandlers.eventLeave.handlePacket(null, AppState
						.getEventServer(), AppState.getEventServer()
						.getContext());
			}
			return true;
		} else {
			return super.onKeyDown(keyCode, event);
		}
	}

	public ListView getTeam1() {
		return team1List;
	}

	public ListView getTeam2() {
		return team2List;
	}

	public void refresh(int team) {
		Message m = handler.obtainMessage(1);
		m.arg1 = team;
		m.sendToTarget();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.event_join, menu);
		return true;
	}
}
