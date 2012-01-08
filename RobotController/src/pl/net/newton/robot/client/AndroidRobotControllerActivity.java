package pl.net.newton.robot.client;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import pl.net.newton.robot.client.transport.PreviewListener;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageView;

public class AndroidRobotControllerActivity extends Activity implements
		OnTouchListener, OnClickListener, PreviewListener, ServiceConnection {
	private ImageView imageView;
	private ControllerService service;

	private AtomicBoolean previewEnabled = new AtomicBoolean();
	private boolean mIsBound;
	private Handler mHandler;
	
	private static final int[][] colors = new int[][] {{255,0,0}, {0,255,0}, {0,0,255}, {255,255,255}};
	private int colorIndex = 0;
	private float brightness = 0;

	private static final int SHOW_PREFERENCES = 1;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		mHandler = new Handler();
		startService(new Intent(this, ControllerServiceImpl.class));
		doBindService();

		Button left = (Button) findViewById(R.id.left);
		Button right = (Button) findViewById(R.id.right);
		Button up = (Button) findViewById(R.id.up);
		Button down = (Button) findViewById(R.id.down);
		Button color = (Button) findViewById(R.id.color);
		Button bright = (Button) findViewById(R.id.bright);

		imageView = (ImageView) findViewById(R.id.image);
		imageView.setOnClickListener(this);

		left.setOnTouchListener(this);
		right.setOnTouchListener(this);
		down.setOnTouchListener(this);
		up.setOnTouchListener(this);
		color.setOnClickListener(this);
		bright.setOnClickListener(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		if (service != null) {
			service.unregisterPreviewListener();
			try {
				service.stopPreview();
			} catch (IOException e) {
			}
		}
		doUnbindService();
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		double x = 0;
		double y = 0;

		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			switch (v.getId()) {
			case R.id.left:
				x = -1;
				break;

			case R.id.right:
				x = 1;
				break;

			case R.id.up:
				y = 1;
				break;

			case R.id.down:
				y = -1;
				break;
			}
		} else if (event.getAction() == MotionEvent.ACTION_UP) {
			x = 0;
			y = 0;
		} else {
			return false;
		}

		Log.d("Robot", x + " " + y);
		if (service != null) {
			service.move(x, y);
		}

		return true;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent;
		switch (item.getItemId()) {

		case R.id.showSettings:
			intent = new Intent(this, Preferences.class);
			startActivityForResult(intent, SHOW_PREFERENCES);
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case SHOW_PREFERENCES:
			Log.d("Makler", "preferences set");
			previewEnabled.set(false);
			service.restart();
			break;
		}
	}

	@Override
	public void onClick(View arg0) {
		switch(arg0.getId()) {
		case R.id.image:
			try {
				turnPreview();
			} catch(Exception e) {
				Log.e("Robot", "can't switch preview", e);
			}
			break;
			
		case R.id.color:
			nextColor();
			break;
			
		case R.id.bright:
			nextBright();
			break;
		}
		
	}

	private void nextBright() {
		if(brightness > 0) {
			brightness = 0;
		} else {
			brightness = 0.15f;
		}
		setColor();
	}

	private void nextColor() {
		colorIndex++;
		if(colorIndex >= colors.length) {
			colorIndex = 0;
		}
		setColor();
	}
	
	private void setColor() {
		char r, g, b;
		int[] color = colors[colorIndex];
		r = (char) (brightness * color[0]);
		g = (char) (brightness * color[1]);
		b = (char) (brightness * color[2]);
		service.setColor(r, g, b);
	}

	private void turnPreview() throws IOException {
		if (service == null) {
			return;
		}
		if (previewEnabled.get()) {
			service.stopPreview();
			previewEnabled.set(false);
		} else {
			service.startPreview();
			previewEnabled.set(true);
		}

	}

	@Override
	public void gotPreview(final byte[] image) {
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				// Log.d("Robot", "get new image, size:" + image.length);
				Bitmap bm = BitmapFactory.decodeByteArray(image, 0,
						image.length);
				imageView.setImageBitmap(bm);
			}
		});
	}

	@Override
	public void onServiceConnected(ComponentName arg0, IBinder arg1) {
		this.service = ((ControllerServiceImpl.LocalBinder) arg1).getService();
		service.registerPreviewListener(this);
	}

	@Override
	public void onServiceDisconnected(ComponentName arg0) {
		this.service = null;
	}

	private void doBindService() {
		bindService(new Intent(this, ControllerServiceImpl.class), this,
				Context.BIND_AUTO_CREATE);
		mIsBound = true;
	}

	private void doUnbindService() {
		if (mIsBound) {
			// Detach our existing connection.
			unbindService(this);
			mIsBound = false;
		}
	}

}