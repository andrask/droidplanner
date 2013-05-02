package com.droidplanner;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.InputDevice;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.droidplanner.helpers.RcOutput;
import com.droidplanner.widgets.joystick.DualJoystickView;
import com.droidplanner.widgets.joystick.JoystickMovedListener;

public class RCActivity extends SuperActivity implements
		 OnClickListener, SensorEventListener {


	private Button bTogleRC;

	MenuItem connectButton;

	private RcOutput rcOutput;

	@Override
	int getNavigationItem() {
		return 2;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.rc);
		
		DualJoystickView joystick = (DualJoystickView)findViewById(R.id.joystickView);
        
        joystick.setOnJostickMovedListener(lJoystick, rJoystick);

		bTogleRC = (Button) findViewById(R.id.bTogleRC);
		bTogleRC.setOnClickListener(this);

		SensorManager mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		Sensor mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		mSensorManager.registerListener((SensorEventListener) this, mSensor,SensorManager.SENSOR_DELAY_NORMAL);
		Log.d("SENSOR", "Listner");
		
		rcOutput = new RcOutput(app.MAVClient,this);
	}
	


	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_pid, menu);
		connectButton = menu.findItem(R.id.menu_connect);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		default:
			return super.onMenuItemSelected(featureId, item);
		}
	}


	@Override
	public void onClick(View v) {
		//printInputDevicesToLog();
		
		if (v == bTogleRC) {
			if (rcOutput.isRcOverrided()) {
				rcOutput.disableRcOverride();
				bTogleRC.setText(R.string.enable_rc_control);
			} else {
				rcOutput.enableRcOverride();
				bTogleRC.setText(R.string.disable_rc_control);
			}
		}
	}

	@SuppressWarnings("unused")
	private void printInputDevicesToLog() {
		int[] inputIds = InputDevice.getDeviceIds();
		Log.d("DEV", "Found " + inputIds.length);
		for (int i = 0; i < inputIds.length; i++) {
			InputDevice inputDevice = InputDevice.getDevice(inputIds[i]);
			Log.d("DEV","name:"+inputDevice.getName()+" Sources:"+inputDevice.getSources());	
		}
	}

	JoystickMovedListener lJoystick = new JoystickMovedListener() {
		@Override
		public void OnReturnedToCenter() {
		}
		@Override
		public void OnReleased() {
		}
		@Override
		public void OnMoved(double pan, double tilt) {
			rcOutput.setRcChannel(RcOutput.RUDDER, pan);
			rcOutput.setRcChannel(RcOutput.TROTTLE, tilt);
		}
	};
	JoystickMovedListener rJoystick = new JoystickMovedListener() {
		@Override
		public void OnReturnedToCenter() {
		}
		@Override
		public void OnReleased() {
		}
		@Override
		public void OnMoved(double pan, double tilt) {
			rcOutput.setRcChannel(RcOutput.AILERON, pan);
			rcOutput.setRcChannel(RcOutput.ELEVATOR, tilt);
			Log.d("RC","roll:"+pan+"\tpitch:"+tilt);
		}
	};

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {		
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		float roll = constrainValue(2 * event.values[0]/ SensorManager.GRAVITY_EARTH, -1f, 1f);
		float pitch = constrainValue(2 * event.values[1]/ SensorManager.GRAVITY_EARTH, -1f, 1f);
		rJoystick.OnMoved(roll, pitch);
	}

	private float constrainValue(float value, float minValue, float ainValue) {
		if (value<minValue) {
			return minValue;
		} else if (value>ainValue) {
			return ainValue;
		} else {
			return value;
		}
	}
}