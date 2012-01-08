package pl.net.newton.robot.server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

import pl.net.newton.robot.server.camera.PhotoProvider;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PreviewCallback;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.WindowManager;

public class RobotServerActivity extends Activity implements Callback,
		PhotoProvider, ServiceConnection {
	private SurfaceView sv;

	private SurfaceHolder sHolder;
	private Camera camera;
	private Parameters parameters;

	private AtomicBoolean cameraWorking = new AtomicBoolean(false);
	
    private int pictureHeight;
    private int pictureWidth;
    private int pictureFormat;
    
    private Rect r;
    
	private final Semaphore waitingForPhoto = new Semaphore(0);
	private byte[] newImage;
	private byte[] callbackBuffer;
	
	private boolean mIsBound = false;
	private ServersService service;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		startService(new Intent(this, ServersServiceImpl.class));
		doBindService();
		
		getWindow().addFlags(
				WindowManager.LayoutParams.FLAG_FULLSCREEN
						| WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		sv = (SurfaceView) findViewById(R.id.surfaceView);
		sHolder = sv.getHolder();
		sHolder.addCallback(this);
		sHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		service.unregisterPhotoProvider();
		doUnbindService();
	}

	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
		Log.d("Robot", "surface changed");
		
		parameters = camera.getParameters();

        pictureHeight = parameters.getPreviewSize().height;
        pictureWidth = parameters.getPreviewSize().width;
        pictureFormat = parameters.getPreviewFormat();
        parameters.setRotation(0);
        
        r = new Rect(0, 0, pictureWidth, pictureHeight);
        
        callbackBuffer = new byte[460800];

        camera.setPreviewCallbackWithBuffer(new PreviewCallback() {
            public void onPreviewFrame(byte[] imageData, Camera arg1) {
            	//Log.d("Robot", "new preview!");
            	newImage = imageData;
        		waitingForPhoto.release();

            }
        });
        camera.addCallbackBuffer(callbackBuffer);

		camera.setParameters(parameters);
		camera.startPreview();
		
		cameraWorking.set(true);
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		Log.d("Robot", "surface created");
		
		camera = Camera.open();
		try {
			camera.setPreviewDisplay(holder);

		} catch (IOException exception) {
			camera.release();
			camera = null;
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.d("Robot", "surface destroyed");
		cameraWorking.set(false);
		
		camera.stopPreview();
		camera.release();
		camera = null;
	}

	@Override
	public byte[] getPhoto() {
		try {
			waitingForPhoto.acquire();
		} catch (InterruptedException e) {
		}
		
		byte[] compressed = compress(newImage);
		if(camera != null) {
			camera.addCallbackBuffer(callbackBuffer);
		}
		return compressed;
	}
	
	private byte[] compress(byte[] photo) {
        YuvImage yuvImage = new YuvImage(photo, pictureFormat, pictureWidth, pictureHeight, null);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        yuvImage.compressToJpeg(r, 20, out); // Tweak the quality here - 20
        return out.toByteArray();
	}
	

	private void doBindService() {
	    bindService(new Intent(this, ServersServiceImpl.class), this, Context.BIND_AUTO_CREATE);
	    mIsBound = true;
	}

	private void doUnbindService() {
	    if (mIsBound) {
	        unbindService(this);
	        mIsBound = false;
	    }
	}

	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		this.service = ((ServersServiceImpl.LocalBinder)service).getService();
		this.service.registerPhotoProvider(this);
	}

	@Override
	public void onServiceDisconnected(ComponentName name) {
		this.service = null;
	}
}