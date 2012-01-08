package pl.net.newton.robot.client.transport;

public interface ArduinoSerial {
	void open() throws Exception;
	void write(byte[] c) throws Exception;
	void flush() throws Exception;
	void close() throws Exception;
}
