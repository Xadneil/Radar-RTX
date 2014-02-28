package net.devilishro.minimap;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class Minimap extends Activity {
	EditText login_email;		// login email
	EditText login_pass;		// login password
	InetAddress serverAddress;	// login server address
	static Socket login_socket;		// login server connection socket
	LoginServerThread login_thread;	// login server socket handler
	static String auth;
	
	/**
	 * Send packet 0xa1; authentication
	 * @author trickyloki3
	 */
	public void onClickRegisterButton(View view) {
		sendToServer(0xa3,login_email.getText().toString(), login_pass.getText().toString());
	}
	
	/**
	 * Send packet 0xa3; registration
	 * @author trickyloki3
	 */
	public void onClickConnectButton(View view) {
		sendToServer(0xa1,login_email.getText().toString(), login_pass.getText().toString());
	}
	
	/**
	 * Packet sender functions
	 * @author trickyloki3
	 */
	private class WriteToServerTask extends AsyncTask <byte[], Void, Void> {
		protected Void doInBackground(byte[]...data) {
			login_thread.write(data[0], data[1], data[2]);
			return null;
		}
	}
	
	private void sendToServer(int packet_header, String packet_email, String packet_password) {
		byte[] header = new byte[3];
		header[1] = (byte) (packet_header >> 0);
		header[0] = (byte) (packet_header >> 8);
		byte[] email_encode = packet_email.getBytes();
		byte[] pass_encode = packet_password.getBytes();
		if(login_socket != null) {
			new WriteToServerTask().execute(header,email_encode,pass_encode);
		} else {
			Toast.makeText(this, "Attempting to connect to login server.", Toast.LENGTH_LONG).show();
			new CreateCommThreadTask().execute();
		}
	}
	

	/**
	 * Generate the XML layout and initialize the email, 
	 * password, and authentication view.
	 * @author trickyloki3
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_minimap);
		
		login_email = (EditText) findViewById(R.id.useremail);
		login_pass = (EditText) findViewById(R.id.userpass);
	}

	public void startEventActivity() {
		State.setEventNumber(1);
		State.getEvents()[0] = new EventActivity.Event("Temp Event", "Best Paintball");
		State.setAdmin(true);
		Intent i = new Intent(this, EventActivity.class);
		startActivity(i);
	}
	
	/**
	 * CreateCommThreadTask adapted from Beginning Android 4
	 * Creates a connection with login server
	 * @author trickyloki3
	 */
	private class CreateCommThreadTask extends AsyncTask<Void, Integer, Socket> {
		@Override
		protected Socket doInBackground(Void... params) {
			try {
				// connect to the login server: 50.62.212.171:3360
				serverAddress = InetAddress.getByName("50.62.212.171");
				login_socket = new Socket(serverAddress, 33600);
				
				// create client thread to handle server IO
				if(login_socket != null)
				{
					login_thread = new LoginServerThread(Minimap.this, login_socket);
					login_thread.start();
				}
			} catch (UnknownHostException e) {
				// Failed to get host name
				Log.d("sockets",e.getLocalizedMessage());
			} catch (IOException e) {
				// Failed to connect to login server 
				Log.d("Sockets",e.getLocalizedMessage());
			}
			return login_socket;
		}
		
		protected void onPostExecute(Socket login_thread) {
			if(login_thread == null)
				Toast.makeText(getBaseContext(), "Unable to connect to login server, please try again later.", Toast.LENGTH_LONG).show();
			else
				Toast.makeText(getBaseContext(), "Connected to login server, please enter your email and password.", Toast.LENGTH_LONG).show();
		}
	}
	
	/**
	 * Connect to login server.
	 * @author trickyloki3
	 */
	@Override
	public void onResume() {
		super.onResume();
		new CreateCommThreadTask().execute();
	}
	
	/**
	 * Close the login server connection socket.
	 * @author trickyloki3
	 */
	private class CloseSocketTask extends AsyncTask <Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... params) {
			try {
				login_socket.close();
			} catch (IOException e) {
				Log.d("Sockets", e.getLocalizedMessage());
			}
			return null;
		}
	}
	
	/**
	 * Close the login server connection.
	 * @author trickyloki3
	 */
	@Override
	public void onPause() {
		super.onPause();
		if(login_socket != null)
			new CloseSocketTask().execute();
	}
	
	/**
	 * UIupdate adapted from Beginning Android 4
	 * Update txtMessage's EditText view
	 * @author trickyloki3
	 */
	public Handler UIupdate = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			int numOfBytesReceived = msg.arg1;
			byte[] buffer = (byte[]) msg.obj;
			
			String strReceived = new String(buffer);
			strReceived = strReceived.substring(0, numOfBytesReceived);
			startEventActivity();
		}
	};

	public void showToast() {
		Toast.makeText(this, "Incorrect Login", Toast.LENGTH_LONG).show();
	}
}
