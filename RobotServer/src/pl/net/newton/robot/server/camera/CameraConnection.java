package pl.net.newton.robot.server.camera;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import android.util.Log;

public class CameraConnection implements Runnable {
	private static final int CALLBACK_PORT = 9990;
	private static final int MESSAGE_LEN = 16000;
	
	private PhotoProvider photoProvider;
	private AtomicBoolean stopNow;
	private InetAddress address;
	private DatagramSocket clientSocket;
	
	public CameraConnection(PhotoProvider photoProvider, InetAddress address) throws SocketException {
		this.photoProvider = photoProvider;
		this.stopNow = new AtomicBoolean();
		this.address = address;
		this.clientSocket = new DatagramSocket();
	}

	public void start() {
		stopNow.set(false);
		Executors.newSingleThreadExecutor().submit(this);
	}
	
	public void stop() {
		stopNow.set(true);
	}

	@Override
	public void run() {
		while(!stopNow.get()) {
			byte[] photo = photoProvider.getPhoto();
			try {
				sendPhoto(photo);
				Thread.sleep(100);
			} catch(Exception e) {
				Log.e("Robot", "Can't send photo", e);
				stopNow.set(true);
			}
		}
	}
	
	private void sendPhoto(byte[] photo) throws IOException {
		if(photo != null) {
			//Log.d("Robot", "sending photo (size=" + photo.length + ")");
		
			for(int i=0;i<photo.length;i += MESSAGE_LEN) {
				int size = MESSAGE_LEN;
				if(photo.length < (i + size)) {
					size = photo.length - i;
				}
				DatagramPacket packet = new DatagramPacket(photo, i, size, address, CALLBACK_PORT);
				clientSocket.send(packet);
			}
			//DatagramPacket packet = new DatagramPacket(new byte[1], 1, address, CALLBACK_PORT);
			//clientSocket.send(packet);
			//Log.d("Robot", "photo send");
		}
	}

}
