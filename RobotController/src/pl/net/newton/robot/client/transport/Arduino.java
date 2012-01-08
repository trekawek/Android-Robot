package pl.net.newton.robot.client.transport;

public class Arduino {
	private ArduinoSerial serialPort;
	
	public Arduino(ArduinoSerial serialPort) {
		this.serialPort = serialPort;
	}
	
	public void setDigital(int pin, boolean value) throws Exception {
		byte[] bytes = String.format("D%02d%d", pin, value ? 1 : 0).getBytes();
		serialPort.write(bytes);
	}
	
	public void setAnalog(int pin, int value) throws Exception {
		byte[] bytes = String.format("A%02d%03d", pin, value).getBytes();
		serialPort.write(bytes);
	}
	
	public void flush() throws Exception {
		serialPort.flush();
	}

	public void close() throws Exception {
		serialPort.close();
	}
}
