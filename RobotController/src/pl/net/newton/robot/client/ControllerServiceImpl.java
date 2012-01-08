package pl.net.newton.robot.client;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import pl.net.newton.robot.client.transport.Arduino;
import pl.net.newton.robot.client.transport.ArduinoSerial;
import pl.net.newton.robot.client.transport.PreviewClient;
import pl.net.newton.robot.client.transport.PreviewListener;
import pl.net.newton.robot.client.transport.Robot;
import pl.net.newton.robot.client.transport.UdpSerial;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

public class ControllerServiceImpl extends Service implements ControllerService, PreviewListener {
	private ArduinoSerial serial;
	private Robot robot;
	private ExecutorService executor;
	private PreviewClient previewClient;
	private PreviewListener previewListener;
	private SharedPreferences pref;
	private String serverIp;

	private final IBinder mBinder = new LocalBinder();
	
	@Override
	public IBinder onBind(Intent arg0) {
		return mBinder;
	}

	public class LocalBinder extends Binder {
		public ControllerService getService() {
            return ControllerServiceImpl.this;
        }
	}
    
    @Override
    public void onCreate() {
    	super.onCreate();
    	Log.d("Robot", "service is created");
        executor = Executors.newSingleThreadExecutor();
        
		pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        startRobot();
    }


    @Override
    public void onDestroy() {
    	super.onDestroy();
    	Log.d("Robot", "service is destroyed");
    	try {
			stopPreview();
		} catch (IOException e) {
			Log.e("Robot", "can't stop previewclient", e);
		}
		previewListener = null;
		previewClient = null;
    }

	@Override
	public void move(final double x, final double y) {
		executor.execute(new Runnable() {
			@Override
			public void run() {
				try {
					robot.move(x, y);
				} catch (Exception e) {
					Log.d("Robot", "can't move the robot :(", e);
				}
			}
		});
	}

	@Override
	synchronized public void registerPreviewListener(PreviewListener listener) {
		this.previewListener = listener;
    	Log.d("Robot", "register listener");
	}

	@Override
	synchronized public void unregisterPreviewListener() {
		this.previewListener = null;
		Log.d("Robot", "unregister listener");
	}

	@Override
	synchronized public void startPreview() throws IOException {
		previewClient = new PreviewClient(this, serverIp);
		previewClient.start();
		Log.d("Robot", "start preview");
	}

	@Override
	synchronized public void stopPreview() throws IOException {
		if(previewClient != null) {
			previewClient.stop();
		}
		Log.d("Robot", "stop preview");
	}

	@Override
	public void gotPreview(byte[] image) {
		if(previewListener != null) {
			previewListener.gotPreview(image);
		}
	}

	@Override
	synchronized public void restart() {
		try {
			if(robot != null) {
				robot.close();
			}
			stopPreview();
			previewClient = null;
			startRobot();
		} catch(Exception e) {
			Log.e("Robot", "can't restart robot", e);
		}
	}
	
	private void startRobot() {
		serverIp = pref.getString("serverIp", "");
		if(serverIp.length() == 0) {
			return;
		}
        serial = new UdpSerial(serverIp);
        try {
        	serial.open();
        } catch(Exception e) {
        	Log.e("Robot", "can't open port", e);
        }
        Arduino arduino = new Arduino(serial);
        robot = new Robot(arduino);
	}


	@Override
	public void setColor(final char r, final char g, final char b) {
		executor.execute(new Runnable() {
			@Override
			public void run() {
				try {
					robot.led(r, g, b);
				} catch (Exception e) {
					Log.d("Robot", "can't sed led :(", e);
				}
			}
		});
	}
}
