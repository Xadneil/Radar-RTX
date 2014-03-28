package net.devilishro.minimap;

import net.devilishro.minimap.network.Network;
import net.devilishro.minimap.network.PacketCreator;
import net.devilishro.minimap.network.PacketHandlers;
import net.devilishro.minimap.network.PacketHandlers.Type;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

//TODO check internet connectivity
/**
 * Main app activity. Contains login logic.
 * 
 * @author trickyloki3
 * @author Daniel
 */
public class Minimap extends Activity {
	@SuppressWarnings("unused")
	private final String TAG = "Minimap";
	private EditText login_username; // login email
	private EditText login_pass; // login password
	private EditText login_pass_confirm; // login password confirm
	private TextView register_error;
	private boolean isRegister = false;
	private Network loginServer;

	/**
	 * Send registration packet
	 */
	public void onClickRegisterButton(View view) {
		if (!isRegister) {
			isRegister = true;
			findViewById(R.id.password_confirm_text)
					.setVisibility(View.VISIBLE);
			login_pass_confirm.setVisibility(View.VISIBLE);
			login_pass_confirm.requestFocus();
		} else {
			if (login_pass.getText().toString()
					.equals(login_pass_confirm.getText().toString())) {

				isRegister = false;
				findViewById(R.id.password_confirm_text).setVisibility(
						View.INVISIBLE);
				login_pass_confirm.setVisibility(View.INVISIBLE);

				if (AppState.networkBypass) {
					PacketHandlers.register.handlePacket(null, loginServer, loginServer.getContext());
				} else {
					loginServer.send(PacketCreator.register(login_username
							.getText().toString(), login_pass.getText()
							.toString()));
				}
			} else {
				Toast.makeText(this, "The passwords must be the same!",
						Toast.LENGTH_LONG).show();
			}
		}
	}

	/**
	 * Send login packet
	 */
	public void onClickConnectButton(View view) {
		if (AppState.networkBypass) {
			PacketHandlers.login.handlePacket(null, loginServer, loginServer.getContext());
		} else {
			if (loginServer == null) {
				Toast.makeText(this, "Please wait until the network is ready.",
						Toast.LENGTH_LONG).show();
			} else {
				loginServer.send(PacketCreator.login(login_username.getText()
						.toString(), login_pass.getText().toString()));
			}
		}
	}

	/**
	 * Generate the XML layout and initialize the email, password, and error
	 * view.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_minimap);

		login_username = (EditText) findViewById(R.id.useremail);
		login_pass = (EditText) findViewById(R.id.userpass);
		login_pass_confirm = (EditText) findViewById(R.id.password_confirm);
		register_error = (TextView) findViewById(R.id.login_error_view);
	}

	/**
	 * Connect to login server.
	 */
	@Override
	public void onResume() {
		super.onResume();
		if (loginServer == null || loginServer.isError()) {
			loginServer = new Network(Type.LOGIN, AppState.getServerAddress(),
					33620);
		}
		loginServer.registerContext(this, Network.Activities.LOGIN);
		if (!loginServer.isRunning()) {
			loginServer.start();
		}
	}

	/**
	 * Close the login server connection.
	 * 
	 * @author trickyloki3
	 */
	@Override
	public void onPause() {
		super.onPause();
		if (loginServer != null) {
			loginServer.unregisterContext(Network.Activities.LOGIN);
			loginServer.close();
			loginServer = null;
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	public void startEventActivity() {
		Intent i = new Intent(this, EventActivity.class);
		startActivity(i);
	}

	/**
	 * UIupdate adapted from Beginning Android 4 Update txtMessage's EditText
	 * view
	 * 
	 * @author trickyloki3
	 */
	@SuppressLint("HandlerLeak")
	public Handler UIupdate = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			String error = "";
			if (msg.what == 0) {
				Toast.makeText(Minimap.this, "Registration Successful",
						Toast.LENGTH_LONG).show();
			} else if (msg.what == 1) {
				short status = (short) msg.arg1;
				if ((status & 0x0002) != 0) {
					// username length
					error += "Username must be between 5-25 characters long.\n";
				}
				if ((status & 0x0004) != 0) {
					// username invalid character
					error += "Username must be composed of only letters and numbers.\n";
				}
				if ((status & 0x0008) != 0) {
					// password length
					error += "Password must be between 5-25 characters long.\n";
				}
				if ((status & 0x0010) != 0) {
					// password digit
					error += "Password must contain a digit.\n";
				}
				if ((status & 0x0020) != 0) {
					// password capital
					error += "Password must contain a capital letter.\n";
				}
				if ((status & 0x0040) != 0) {
					// password special character
					error += "Password must contain a special character (#$%&'()*+,`-.).\n";
				}
				if ((status & 0x0080) != 0) {
					// username exists
					error += "Username " + login_username.getText().toString()
							+ " already exists.\n";
				}
				register_error.setText(error);
			} else if (msg.what == 2) {
				Toast.makeText(Minimap.this, "Invalid username or password.",
						Toast.LENGTH_LONG).show();
			} else if (msg.what == 3) {
				Toast.makeText(
						Minimap.this,
						Minimap.this.login_username.getText().toString()
								+ "Is Already Logged In", Toast.LENGTH_LONG)
						.show();
			}
		}
	};
}
