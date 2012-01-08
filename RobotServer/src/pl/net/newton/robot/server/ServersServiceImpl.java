package pl.net.newton.robot.server;

import pl.net.newton.robot.server.camera.CameraServer;
import pl.net.newton.robot.server.camera.PhotoProvider;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class ServersServiceImpl extends Service implements ServersService, PhotoProvider {
	private PhotoProvider photoProvider;
	private SerialServer serialServer;
	private CameraServer cameraServer;

	private final IBinder mBinder = new LocalBinder();
	
	@Override
	public IBinder onBind(Intent arg0) {
		return mBinder;
	}

	public class LocalBinder extends Binder {
		public ServersService getService() {
            return ServersServiceImpl.this;
        }
	}
    
    @Override
    public void onCreate() {
    	Log.d("Robot", "service started");
    	super.onCreate();
        startServers();
    }

    private void startServers() {
		try {
			serialServer = new SerialServer();
			serialServer.start();
		} catch (Exception e) {
			Log.e("Robot", "Can't start serial server", e);
		}

		try {
			cameraServer = new CameraServer(this);
			cameraServer.start();
		} catch (Exception e) {
			Log.e("Robot", "Can't start camera server", e);
		}		
	}


	@Override
    public void onDestroy() {
		Log.d("Robot", "service destroyed");
    	super.onDestroy();
    	stopServers();
    }

	private void stopServers() {
		try {
			serialServer.stop();
		} catch (Exception e) {
			Log.e("Robot", "Can't stop server", e);
		}

		try {
			cameraServer.stop();
		} catch (Exception e) {
			Log.e("Robot", "Can't stop camera server", e);
		}		
	}

	@Override
	public void registerPhotoProvider(PhotoProvider photoProvider) {
		Log.d("Robot", "provider registered");
		this.photoProvider = photoProvider;
	}

	@Override
	public void unregisterPhotoProvider() {
		Log.d("Robot", "provider unregistered");
		this.photoProvider = null;
		
	}

	@Override
	public byte[] getPhoto() {
		if(photoProvider != null) {
			return photoProvider.getPhoto();
		} else {
			return null;
		}
	}
}
