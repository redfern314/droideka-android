package edu.olin.droideka;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

import android.os.Bundle;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.TextView;

public class AccelActivity extends Activity implements SensorEventListener {
	private TextView debugBox;
	private final static int REQUEST_ENABLE_BT = 1;
	private BluetoothDevice droidekaDevice = null;
	BluetoothSocket droidekaSocket = null;
	OutputStream droidekaStream = null;
	private SensorManager mSensorManager = null;
	private Sensor mAccel = null;
	
	private Button btnLaser = null;

	private TextView accelXbox;
	private TextView accelYbox;
	private TextView accelZbox;
	
	private TextView directionBox;
	private TextView turnBox;
	private TextView speedBox;
	
	private static final int LEFT = 0;
	private static final int RIGHT = 1;
	private static final int STRAIGHT = 2;
	private int turn = STRAIGHT;
	
	private static final int FORWARD = 0;
	private static final int BACKWARD = 1;
	private int direction = FORWARD;
	
	private static final int LASERS_OFF = 0;
	private static final int LASERS_ON = 1;
	private int lasers = LASERS_OFF;
	
	private static final int ROLLING = 0;
	private static final int STANDING = 1;
	private int roll = ROLLING;
	
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
	    public void onReceive(Context context, Intent intent) {
	        String action = intent.getAction();
	        // When discovery finds a device
	        if (BluetoothDevice.ACTION_FOUND.equals(action)) {
	            // Get the BluetoothDevice object from the Intent
	            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
	            // Add the name and address to an array adapter to show in a ListView
//	            debugBox.append(device.getName() + "\n" + device.getAddress() + "\n");
//	            debugBox.append("["+device.getAddress()+"]\n");
	            if(device.getAddress().equals("00:13:12:16:60:56")) {
	            	debugBox.append("DROIDEKA found!\n");
	            	droidekaDevice = device;
	            	droidekaFound();
	            }
	        }
	    }
	};
	
	protected void droidekaFound() {
		// Get a BluetoothSocket to connect with the given BluetoothDevice
		final UUID SERIAL_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
		
		try {
		    droidekaSocket = droidekaDevice.createRfcommSocketToServiceRecord(SERIAL_UUID); 
		} catch (IOException e) {}

		try {           
			// open a socket and a stream to the BT serial port
		    droidekaSocket.connect(); 
		    droidekaStream = droidekaSocket.getOutputStream();
		    debugBox.append("DROIDEKA connected!\n");
		    System.out.println("connected");
//		    while(true) {
//		    	transmitData(0,0,0,1,0);
//		    	System.out.println("transmit");
//		    	try {
//					Thread.sleep(500);
//				} catch (InterruptedException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//		    	transmitData(0,0,0,0,0);
//		    	try {
//					Thread.sleep(500);
//				} catch (InterruptedException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//		    }
		    //now you can use out to send output via out.write
		} catch (IOException e) {}
	}
	
	private int scaleAccelToSpeed(float accelVal) {
		// min accel value: 1
		// max accel value: 6.5
		// min speed: 0
		// max speed: 255
		
		// converts the accel value to a float between 0 and 1
		float normalized = (float) ((accelVal-1)/6.5);
		
		// converts the value to a float between 0 and 255
		float speed = (float) normalized*255;
				
		// cast to int and coerce into proper range
		int speedInt = (int)speed;
		if (speedInt>255) {
			speedInt=255;
		}
		if (speedInt<0) {
			speedInt=0;
		}
		
		return speedInt;
	}
	
	public void onSensorChanged(SensorEvent event){
		float x = event.values[0]; // left/right
		float y = event.values[1]; // forward/back
		float z = event.values[2];
		
		int speed = 0;
		
		accelXbox.setText("Accel X: "+Float.toString(x));
		accelYbox.setText("Accel Y: "+Float.toString(y));
		accelZbox.setText("Accel Z: "+Float.toString(z));
		
		if (x<-2) {
			turn = RIGHT;
			turnBox.setText("right");
		} else if (x>2) {
			turn = LEFT;
			turnBox.setText("left");
		} else {
			turn = STRAIGHT;
			turnBox.setText("straight");
		}
		
		float absY = Math.abs(y);
		
		if (y<=-1) {
			direction = FORWARD;
			directionBox.setText("forward");
			speed = scaleAccelToSpeed(absY);
		} else if (y>=1) {
			direction = BACKWARD;
			directionBox.setText("backward");
			speed = scaleAccelToSpeed(absY);
		}
		
		speedBox.setText("Speed: "+Integer.toString(speed));
		
		boolean laserStatus = btnLaser.isPressed();
		if (laserStatus) {
			lasers = LASERS_ON;
		} else {
			lasers = LASERS_OFF;
		}
		
		// TODO: laser and stand
		transmitData(speed,turn,direction,lasers,0);
	}
	
	protected void transmitData(int speed, int turn, int direction, int laser, int stand) {
		// Q speed(0-255) L/R/C(0/1/2) F/R(0/1) laseroff/laseron(0,1) rolling/standing(0/1)
		byte[] transmitArray = {'q',(byte)speed,(byte)turn,(byte)direction,(byte)laser,(byte)stand};
		System.out.print(transmitArray);
		try {
			droidekaStream.write(transmitArray);
		} catch (IOException e) { } catch (NullPointerException e) {}
	}

	protected void startBluetooth() {
		// Does this phone have bluetooth?
		BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mBluetoothAdapter == null) {
		    // Device does not support Bluetooth
			debugBox.append("No bluetooth on your phone!\n");
		} else {
			debugBox.append("Congrats, your phone has bluetooth.\n");
		}
		
		// Is bluetooth on?
		if (!mBluetoothAdapter.isEnabled()) {
		    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
		    debugBox.append("Bluetooth has been manually enabled\n");
		} else {
			debugBox.append("Bluetooth already enabled\n");
		}
		
		// mReceiver will get called asynchronously whenever a new device is found
		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		registerReceiver(mReceiver, filter); // Don't forget to unregister during onDestroy
		
		// Find all nearby devices!
		mBluetoothAdapter.startDiscovery();
		debugBox.append("Searching for devices...\n");
	}
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
    	mAccel = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    	mSensorManager.registerListener(this,mAccel,SensorManager.SENSOR_DELAY_NORMAL);
		
        setContentView(R.layout.activity_accel);
    }
    
    @Override
    protected void onStart() {
    	super.onStart();
    	btnLaser = (Button)findViewById(R.id.btnLaser);
    	
    	debugBox=(TextView)findViewById(R.id.debugBox); 
    	debugBox.setText("");
    	
    	accelXbox = (TextView)findViewById(R.id.accelX);
    	accelYbox = (TextView)findViewById(R.id.accelY);
    	accelZbox = (TextView)findViewById(R.id.accelZ);
    	
    	directionBox = (TextView)findViewById(R.id.directionBox);
    	turnBox = (TextView)findViewById(R.id.turnBox);
    	directionBox.setText("forward");
		turnBox.setText("straight");
		direction = FORWARD;
		turn = STRAIGHT;
		
		speedBox = (TextView)findViewById(R.id.speedBox);
		
    	startBluetooth();
    }
    
    protected void onDestroy() {
    	super.onDestroy();
    	unregisterReceiver(mReceiver);
    	try{
    		if (droidekaStream != null)
    			droidekaStream.close();
    		if (droidekaSocket != null)
    			droidekaSocket.close();
    	} catch(IOException e) {}
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.accel, menu);
        return true;
    }

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}
    
}
