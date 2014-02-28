package net.devilishro.minimap;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import android.util.Log;
import android.widget.Toast;

public class LoginServerThread extends Thread {
	private final Socket socket;
	private final InputStream inputStream;
	private final OutputStream outputStream;
	private Minimap map;

	public LoginServerThread(Minimap map, Socket sock) {
		socket = sock;
		this.map = map;
		InputStream tmpIn = null;
		OutputStream tmpOut = null;
		try {
			tmpIn = socket.getInputStream();
			tmpOut = socket.getOutputStream();
		} catch (IOException e) {
			Log.d("SocketChat", e.getLocalizedMessage());
		}

		inputStream = tmpIn;
		outputStream = tmpOut;
	}

	public void run() {
		byte[] buffer = new byte[1024];
		int bytes;

		while (true) {
			try {
				bytes = inputStream.read(buffer);
				if (bytes == -1) {
					inputStream.close();
					outputStream.close();
					Minimap.login_socket = null;
					break;
				}
				Minimap.auth = new String(buffer);
				String s = "";
				for (int i = 0; i < bytes; i++) {
					s += String.format("%02X ", buffer[i]);
				}
				Log.d("tmp", s);
				int b1 = buffer[2] & 0xff; //magic
				int b2 = buffer[3] & 0xff; //magic
				//if (b2 < 0) {
				//	b2 += 256;
				//}
				b1 = b1 << 8;
				Log.d("b1", "" + b1);
				Log.d("b2", "" + b2);
				int status = b1 + b2;
				Log.d("temp", "" + status);
				if (status != 200 && status != 201) {
					map.UIupdate.obtainMessage(2).sendToTarget();
				} else {
					map.startEventActivity();
					cancel();
				}
			} catch (IOException e) {
				Log.d("SocketChat", e.getLocalizedMessage());
				break;
			}
		}
	}

	public void write(byte[] packet_header, byte[] email, byte[] password) {
		try {
			byte[] email_padded = new byte[64];
			byte[] password_padded = new byte[64];
			byte[] packet = new byte[130];

			for (int i = 0; i < 64; i++)
				if (email.length > i)
					email_padded[i] = email[i];
				else
					email_padded[i] = 0;

			for (int i = 0; i < 64; i++)
				if (password.length > i)
					password_padded[i] = password[i];
				else
					password_padded[i] = 0;

			for (int i = 0; i < 2; i++)
				packet[i] = packet_header[i];
			for (int i = 0; i < 64; i++)
				packet[i + 2] = email_padded[i];
			for (int i = 0; i < 64; i++)
				packet[i + 2 + 64] = password_padded[i];

			outputStream.write(packet);
		} catch (IOException e) {
			Log.d("CommsThread.write()", e.getLocalizedMessage());
		}
	}

	public void cancel() {
		try {
			socket.close();
		} catch (IOException e) {
			Log.d("CommsThread.cancel()", e.getLocalizedMessage());
		}
	}
}
