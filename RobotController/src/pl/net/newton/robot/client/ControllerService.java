package pl.net.newton.robot.client;

import java.io.IOException;

import pl.net.newton.robot.client.transport.PreviewListener;

public interface ControllerService {
	void move(double x, double y);
	
	void registerPreviewListener(PreviewListener listener);
	void unregisterPreviewListener();
	
	void startPreview() throws IOException;
	void stopPreview() throws IOException;

	void restart();

	void setColor(char r, char g, char b);
}
