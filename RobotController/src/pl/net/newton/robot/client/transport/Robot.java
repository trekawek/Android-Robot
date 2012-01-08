package pl.net.newton.robot.client.transport;

import static java.lang.Math.PI;
import static java.lang.Math.abs;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;
import static java.lang.Math.atan2;

public class Robot {
	private Arduino arduino;
	private static final int[] MOTOR_PINS = new int[] { 7, 4 };
	private static final int[] PWM_PINS = new int[] { 6, 5 };
	private static final int[] RGB = new int[] { 9, 10, 11};

	private static final double PI_4 = PI / 4;

	public Robot(Arduino arduino) {
		this.arduino = arduino;
	}

	public void led(char r, char g, char b) throws Exception {
		arduino.setAnalog(RGB[0], r);
		arduino.setAnalog(RGB[1], g);
		arduino.setAnalog(RGB[2], b);
		arduino.flush();
	}
	
	public void move(double x, double y) throws Exception {
		Pair p = rotate(new Pair(x, y));
		p = diskToSquare(p);
		int valX = (int) (p.x * 255);
		int valY = (int) (p.y * 255);

		//System.out.println("x = " + valX);
		//System.out.println("x = " + valY);
		
		setMotor(0, valX < 0 ? 0 : 1, abs(valX));
		setMotor(1, valY < 0 ? 0 : 1, abs(valY));
		arduino.flush();
	}
	
	public void close() throws Exception {
		arduino.close();
	}

	private void setMotor(int motor, int direction, int value)
			throws Exception {
		arduino.setDigital(MOTOR_PINS[motor], direction != 0);
		arduino.setAnalog(PWM_PINS[motor], value);
	}

	private Pair rotate(Pair p) {
		double x = p.x * cos(PI_4) - p.y * sin(PI_4);
		double y = p.x * sin(PI_4) + p.y * cos(PI_4);
		return new Pair(x, y);
	}

	private Pair diskToSquare(Pair p) {
		double r = sqrt(p.x * p.x + p.y * p.y);
		double phi = atan2(p.y, p.x);
		if (phi < PI_4) {
			phi += 2 * PI;
		}
		double a, b;

		if (phi < PI_4) {
			a = r;
			b = phi * a / PI_4;
		} else if (phi < 3 * PI_4) {
			b = r;
			a = -(phi - PI / 2) * b / PI_4;
		} else if (phi < 5 * PI_4) {
			a = -r;
			b = (phi - PI) * a / PI_4;
		} else {
			b = -r;
			a = -(phi - 3 * PI / 2) * b / PI_4;
		}
		return new Pair(a, b);
	}

	private static class Pair {
		private final double x, y;

		private Pair(double x, double y) {
			this.x = x;
			this.y = y;
		}
	}
}
