package pl.net.newton.robot.server;

import pl.net.newton.robot.server.camera.PhotoProvider;

public interface ServersService {
	void registerPhotoProvider(PhotoProvider photoProvider);
	void unregisterPhotoProvider();
}
