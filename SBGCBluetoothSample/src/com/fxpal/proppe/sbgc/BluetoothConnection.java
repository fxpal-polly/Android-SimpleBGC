package com.fxpal.proppe.sbgc;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.util.Log;

/**
 * @author Author: Patrick Proppe, FX Palo Alto Laboratory, Inc.
 * Copyright (c) 2014
 * All rights reserved.
 * @see SimpleBGC 2.4 serial protocol rev. 0.16
 * 
 */
public class BluetoothConnection extends Activity {

	static BluetoothAdapter mBluetoothAdapter;
	static BluetoothSocket btSocket;
	static BluetoothDevice btDevice;
	static OutputStream btOutputStream;
	static InputStream btInputStream;
	static String btStatus = "";
	static String btAddress = "";

	static ReentrantLock call_lock;
	private static final String TAG = "BluetoothConnection";

	public boolean connectAlexmosDevice() throws IOException,
			InterruptedException {
		boolean isConnected = false;

		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mBluetoothAdapter == null) {
			btStatus = "disconnected";
		}

		if (!mBluetoothAdapter.isEnabled()) {
			Intent enableBluetooth = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBluetooth, 0);

		}

		Set<BluetoothDevice> pairedDevices = mBluetoothAdapter
				.getBondedDevices();

		if (pairedDevices.size() > 0) {

			for (BluetoothDevice device : pairedDevices) {
				btAddress = device.getAddress();
				Log.e(TAG,
						"Paired device: " + btAddress + " " + device.getName());
				if (device.getName().contains("ALEXMOS")) {
					btDevice = device;
					isConnected = openBluetoothConnection(btDevice);
					if (isConnected) {
						return isConnected;

					}
				}
			}
		}

		return isConnected;
	}

	public static boolean openBluetoothConnection(BluetoothDevice device) {

		try {
			// Standard SerialPortService ID
			if (btSocket != null)
				btSocket.close();
			UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
			btSocket = device.createRfcommSocketToServiceRecord(uuid);

			btSocket.connect();

			btOutputStream = btSocket.getOutputStream();
			btInputStream = btSocket.getInputStream();
			call_lock = new ReentrantLock();
			if (btSocket.isConnected()) {
				btStatus = "connected";
			}
		} catch (Exception e) {
			Log.e(TAG, "openBluetoothConnection() failed!");
		}
		return btSocket.isConnected();

	}

	public boolean isConnected() {
		return btSocket.isConnected();
	}

	public void closeBluetoothConnection() throws IOException {
		SBGCConnector.setStopReading(true);
		SBGCConnector.setStopRealtimeData(true);

		if (btStatus == "connected") {
			btOutputStream.close();
			btInputStream.close();
			btSocket.close();
			btStatus = "disconnected";
		}

	}

	public void sendViaBT(byte data[]) {
		if (btStatus == "connected") {
			try {
				call_lock.lock();
				btOutputStream.write(data);
				call_lock.unlock();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	public int availableBytes() {
		int bytesAvailable = 0;
		try {
			call_lock.lock();
			bytesAvailable = btInputStream.available();
			call_lock.unlock();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return bytesAvailable;
	}

	public byte[] read() {
		byte[] packetBytes = new byte[availableBytes()];
		try {
			call_lock.lock();
			if (btStatus == "connected" && btInputStream.available() > 0) {
				btInputStream.read(packetBytes);
			}
			call_lock.unlock();

		} catch (IOException e) {
			e.printStackTrace();
		}
		return packetBytes;
	}

}
