package edu.olin.droideka;

import android.os.Bundle;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.view.Menu;
import android.widget.TextView;

public class AccelActivity extends Activity {
	private TextView debugBox;
	private final static int REQUEST_ENABLE_BT = 1;

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


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.accel, menu);
        return true;
    }
    
}
