package pl.net.newton.robot.client.transport;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.Executors;

public class PreviewClient implements Runnable {
	private PreviewListener listener;
	private DatagramSocket clientSocket;
	
	private static final int CLIENT_PORT = 9998;
	private static final int SERVER_PORT = 9990;
	private InetAddress IPAddress;
	
	private DatagramSocket socket;
	
	public PreviewClient(PreviewListener listener, String serverIp) throws SocketException, UnknownHostException {
		this.listener = listener;
		this.clientSocket = new DatagramSocket();
		this.IPAddress = InetAddress.getByName(serverIp);
		this.socket = new DatagramSocket(SERVER_PORT);
	}
	
	public void start() throws IOException {
		Executors.newSingleThreadExecutor().submit(this);
		sendMessage("register");
	}
	
	public void stop() throws IOException {
		if(!socket.isClosed()) {
			socket.close();
		}

		sendMessage("unregister");
	}

	@Override
	public void run() {
		byte[] receiveData = new byte[16000];
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
	
	private void sendMessage(String message) throws IOException {
		byte[] bytes = message.getBytes();
		DatagramPacket sendPacket = new DatagramPacket(bytes, bytes.length, IPAddress, CLIENT_PORT);
		clientSocket.send(sendPacket);
	}
	
	private void handlePacket(DatagramPacket receivePacket) {
		byte[] data = receivePacket.getData();
		int length = receivePacket.getLength();
		
		byte[] image = new byte[length];
		
		for(int i=0;i<length;i++) {
			image[i] = data[i];
		}
		listener.gotPreview(image);
	}
}
