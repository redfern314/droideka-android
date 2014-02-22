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
import android.view.Menu;
import android.widget.TextView;

public class AccelActivity extends Activity {
	private TextView debugBox;
	private final static int REQUEST_ENABLE_BT = 1;
	private BluetoothDevice droidekaDevice = null;
	BluetoothSocket droidekaSocket = null;
	OutputStream droidekaStream = null;
	
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
		    while(true) {
		    	transmitData(0,0,0,1,0);
		    	System.out.println("transmit");
		    	try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		    	transmitData(0,0,0,0,0);
		    	try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		    }
		    //now you can use out to send output via out.write
		} catch (IOException e) {}
	}
	
	protected void transmitData(int speed, int turn, int direction, int laser, int stand) {
		// Q speed(0-255) L/R/C(0/1/2) F/R(0/1) laseroff/laseron(0,1) rolling/standing(0/1)
		byte[] transmitArray = {'q',(byte)speed,(byte)turn,(byte)direction,(byte)laser,(byte)stand};
		System.out.print(transmitArray);
		try {
			droidekaStream.write(transmitArray);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	protected void startBluetooth() {
		debugBox=(TextView)findViewById(R.id.debugBox); 
		debugBox.setText("");
		
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
        setContentView(R.layout.activity_accel);
    }
    
    @Override
    protected void onStart() {
    	super.onStart();
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
    
}
