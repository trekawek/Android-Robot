package pl.net.newton.robot.server;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.concurrent.Executors;

import android.util.Log;

public class SerialServer implements Runnable {
	private static final int PORT = 9999;
	private static final String SERIAL_PATH = "/dev/ttyMSM2";

	private byte[] receiveData = new byte[1024];
	private DatagramSocket socket;
	private OutputStream outputStream;

	public void start() throws Exception {
		SuHelper.chmod(SERIAL_PATH, "666");
		socket = new DatagramSocket(PORT);
		Log.i("Robot", "Listening on port " + PORT);
		outputStream = new FileOutputStream(SERIAL_PATH);
		Log.i("Robot", "and writing to " + SERIAL_PATH);
		
		Executors.newSingleThreadExecutor().submit(this);
	}

	synchronized public void stop() throws IOException {
		socket.close();
		outputStream.close();
	}
	
	synchronized private void handlePacket(DatagramPacket receivePacket) throws IOException {
		Log.d("Robot", "serial: " + new String(receivePacket.getData(), receivePacket.getOffset(), receivePacket.getLength()));
		outputStream.write(receivePacket.getData(), receivePacket.getOffset(), receivePacket.getLength());
		outputStream.flush();
	}

	@Override
	public void run() {
		while (!socket.isClosed()) {
			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
			try {
				socket.receive(receivePacket);
				handlePacket(receivePacket);
			} catch (IOException e) {
				return;
			}
		}		
	}
}
