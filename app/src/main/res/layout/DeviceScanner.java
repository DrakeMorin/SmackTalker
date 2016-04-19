package com.ded.smacktalker;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Set;

/**
 * Created by Emma Kate on 4/19/2016.
 */
class DeviceScanner {

// --Commented out by Inspection START (4/19/2016 8:37 AM):
// /**
// * Tag for Log
// com/example/drake/listviewtest/DeviceScanner.java:89
// private static final String TAG = "DeviceListActivity";
//
// /**
// * Return Intent extra
// */
//private static final String DDRESS = "device_address";
// --Commented out by Inspection STOP (4/19/2016 8:37 AM)

/**
 * Member fields
 */
private final ThreadLocal<BluetoothAdapter> mBtAdapter = new ThreadLocal<>();

    /**
 * NAME all of the local, newly discovered devices.  This will create the array.
 */
private ArrayAdapter<String> mNewDevicesArrayAdapter;

@Override
protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Setup the window
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_device_list);

        // Set result CANCELED  in the event that the user backs out
        setResult(Activity.RESULT_CANCELED);

        // Initialize the button to perform device discovery
        Button scanButton = (Button) findViewById(R.id.button_scan);
        scanButton.setOnClickListener(new View.OnClickListener() {
public void onClick(View v) {
        doDiscovery();
        v.setVisibility(View.GONE);
        }
        });

        // Initialize array adapters. One for already paired devices and
        // one for newly discovered devices
        ArrayAdapter<String> pairedDevicesArrayAdapter =
        new ArrayAdapter<>(this, R.layout.device_name);
        mNewDevicesArrayAdapter = new ArrayAdapter<>(this, R.layout.device_name);

        // Find all of the devices them complete a list of available devices.
        ListView pairedListView = (ListView) findViewById(R.id.paired_devices);
        pairedListView.setAdapter(pairedDevicesArrayAdapter);
        pairedListView.setOnItemClickListener(mDeviceClickListener);

        // Find and set up the ListView for newly discovered devices
        ListView newDevicesListView = (ListView) findViewById(R.id.new_devices);
        newDevicesListView.setAdapter(mNewDevicesArrayAdapter);
        newDevicesListView.setOnItemClickListener(mDeviceClickListener);

        // Register for broadcasts when a device is discovered
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mReceiver, filter);

        // Register for broadcasts when discovery has finished
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(mReceiver, filter);

        // Get the local Bluetooth adapter
        mBtAdapter.set(BluetoothAdapter.getDefaultAdapter());

        // Get a set of currently paired devices
        Set<BluetoothDevice> pairedDevices = mBtAdapter.get().getBondedDevices();

        // If there are paired devices, add each one to the ArrayAdapter
        if (pairedDevices.size() > 0) {
        findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);
        for (BluetoothDevice device : pairedDevices) {
        pairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
        }
        } else {
        String noDevices = getResources().getText(R.string.none_paired).toString();
        pairedDevicesArrayAdapter.add(noDevices);
        }
        }

@Override
protected void onDestroy() {
        super.onDestroy();

        // Cancel the discovery process immed.
        if (mBtAdapter.get() != null) {
        mBtAdapter.get().cancelDiscovery();
        }

        // Unregister broadcast listeners
        this.unregisterReceiver(mReceiver);
        }

/**
 * At this point, use the Bluetooth adapter to "Discover"
 */
private void doDiscovery() {
        Log.d(TAG, "doDiscovery()");

        // Indicate scanning in the title
        setProgressBarIndeterminateVisibility(true);
        setTitle(R.string.scanning);

        // Turn on sub-title for new devices
        findViewById(R.id.title_new_devices).setVisibility(View.VISIBLE);

        // If we're already discovering, stop it
        if (mBtAdapter.get().isDiscovering()) {
        mBtAdapter.get().cancelDiscovery();
        }

        // Request discover from BluetoothAdapter
        mBtAdapter.get().startDiscovery();
        }

/**
 * The on-click listener for all devices in the ListViews
 */
private final AdapterView.OnItemClickListener mDeviceClickListener
        = new AdapterView.OnItemClickListener() {
public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
        // Cancel discovery because it's costly and we're about to connect
        mBtAdapter.get().cancelDiscovery();

        // Get the device MAC address, which is the last 17 chars in the View
        String info = ((TextView) v).getText().toString();
        String address = info.substring(info.length() - 17);

        // Create the result Intent and include the MAC address
        Intent intent = new Intent();
        intent.putExtra(EXTRA_DEVICE_ADDRESS, address);

        // Set result and finish this Activity
        setResult(Activity.RESULT_OK, intent);
        finish();
        }
        };

/**
 * The BroadcastReceiver that listens for discovered devices and changes the title when
 * discovery is finished
 */
private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
@Override
public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        // When discovery finds a device
        if (BluetoothDevice.ACTION_FOUND.equals(action)) {
        // Get the BluetoothDevice object from the Intent
        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        // If it's already paired, skip it, because it's been listed already
        if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
        mNewDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
        }
        // When discovery is finished, change the Activity title
        } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
        setProgressBarIndeterminateVisibility(false);
        setTitle(R.string.select_device);
        if (mNewDevicesArrayAdapter.getCount() == 0) {
        String noDevices = getResources().getText(R.string.none_found).toString();
        mNewDevicesArrayAdapter.add(noDevices);
        }
        }
        }
        };

        }

