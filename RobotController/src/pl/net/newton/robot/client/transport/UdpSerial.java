package pl.net.newton.robot.client.transport;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import android.util.Log;

public class UdpSerial implements ArduinoSerial {

	private static final int PORT = 9999;
	private String serverIp;
	
	private DatagramSocket clientSocket;
	private InetAddress IPAddress;
	
	private byte[] buffer = new byte[1024];
	private int bufferSize = 0;
	
	public UdpSerial(String serverIp) {
		this.serverIp = serverIp;
	}

	@Override
	public void open() throws Exception {
	      clientSocket = new DatagramSocket();
	      IPAddress = InetAddress.getByName(serverIp);		
	}

	@Override
	public void write(byte[] data) throws Exception {
		for(int i=0;i<data.length;i++) {
			buffer[bufferSize + i] = data[i];
		}
		bufferSize += data.length;
	}

	@Override
	public void flush() throws Exception {
		DatagramPacket sendPacket = new DatagramPacket(buffer, bufferSize, IPAddress, PORT);
		clientSocket.send(sendPacket);
		bufferSize = 0;
	}

	@Override
	public void close() throws Exception {
		clientSocket.close();
	}

}
