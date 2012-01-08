package pl.net.newton.robot.server.camera;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.concurrent.Executors;

import android.util.Log;

public class CameraServer implements Runnable {
	private static final int PORT = 9998;

	private byte[] receiveData = new byte[1024];
	private DatagramSocket socket;
	private CameraConnection connection;
	private PhotoProvider photoProvider;

	public CameraServer(PhotoProvider photoProvider) {
		this.photoProvider = photoProvider;
	}

	public void start() throws IOException {
		socket = new DatagramSocket(PORT);
		Log.i("Robot", "Camera server is listening on port " + PORT);
		
		Executors.newSingleThreadExecutor().submit(this);
	}

	synchronized public void stop() throws IOException {
		socket.close();
		if(connection != null) {
			connection.stop();
		}
	}

	synchronized private void handlePacket(DatagramPacket receivePacket) throws IOException {
		String message = new String(receivePacket.getData(), receivePacket.getOffset(), receivePacket.getLength()).trim();
		
		Log.i("Robot", "Got message: " + message);
		if ("register".equals(message)) {
			if(connection != null) {
				connection.stop();
			}
			connection = new CameraConnection(photoProvider, receivePacket.getAddress());
			connection.start();
		} else if ("unregister".equals(message)) {
			connection.stop();
		}
	}

	@Override
	public void run() {
		while (!socket.isClosed()) {
			DatagramPacket receivePacket = new DatagramPacket(receiveData,
					receiveData.length);
			try {
				socket.receive(receivePacket);
				handlePacket(receivePacket);
			} catch (IOException e) {
				return;
			}
		}		
	}
}
