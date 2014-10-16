package com.fxpal.proppe.sbgc;

import java.io.IOException;

import android.os.AsyncTask;
import android.util.Log;

/**
 * @author Author: Patrick Proppe, FX Palo Alto Laboratory, Inc.
 * Copyright (c) 2014
 * All rights reserved.
 * @see SimpleBGC 2.4 serial protocol rev. 0.16
 * 
 */
public class SBGCConnector extends AsyncTask<Void, Void, Boolean> {
	private static final String TAG = "SBGCProtocol";
	private final static long REALTIME_DATA_FREQ = 200L;
	private volatile static boolean stopReading;
	private volatile static boolean stopRealtimeData = true;
	private static Thread realtimeDataThread;
	private static Thread readThread;
	static boolean deviceConnected = false;
	public static SBGCProtocol serialProtocol = new SBGCProtocol();
	public static BluetoothConnection bluetooth = new BluetoothConnection();

	/**
	 * Searches for an already paired ALEXMOS device and tries to connect to it
	 * No error handling on unpaired devices!
	 * 
	 * @return if ALEXMOS is connected
	 */
	public static boolean connectBluetooth() {

		try {
			if (!deviceConnected && bluetooth.connectAlexmosDevice()) {
				readSerialData();
				deviceConnected = SBGCProtocol.initSBGCProtocol();
				pollRealtimeData();
			} else {
				Log.e(TAG, "No ALEXMOS device found!");

			}
		} catch (IOException e) {
			Log.e(TAG, "connectBluetooth(): IO Exception: " + e);

		} catch (InterruptedException e) {
			Log.e(TAG, "connectBluetooth(): InterruptedException: " + e);
		}

		return deviceConnected;
	}

	/**
	 * Tries to disconnect the ALEXMOS device
	 * 
	 */
	public static void disconnectBluetooth() {

		try {
			bluetooth.closeBluetoothConnection();
			deviceConnected = false;
		} catch (Exception ex) {
			Log.e(TAG, "Could not disconnect BT connection: " + ex);

		}
		;
	}

	/**
	 * Polls the real-time Data at REALTIME_DATA_FREQ
	 */
	public static void pollRealtimeData() {
		stopRealtimeData = false;
		realtimeDataThread = new Thread(new Runnable() {
			public void run() {
				while (!Thread.currentThread().isInterrupted()
						&& !stopRealtimeData) {
					try {
						serialProtocol.requestRealtimeData();
						Thread.sleep(REALTIME_DATA_FREQ);
					} catch (InterruptedException e) {
						stopRealtimeData = true;

					}
				}
			}
		});
		realtimeDataThread.start();
	}

	/**
	 * Reads the received serial Data and delegates it to handleData()
	 */
	public static void readSerialData() {
		stopReading = false;
		readThread = new Thread(new Runnable() {
			public void run() {
				while (!Thread.currentThread().isInterrupted() && !stopReading) {

					try {
						int bytesAvailable = bluetooth.availableBytes();
						if (bytesAvailable > 0) {
							byte[] packetBytes = new byte[bytesAvailable];
							packetBytes = bluetooth.read();
							serialProtocol.handleData(packetBytes);
						} else {
							Thread.sleep(20L);
						}
					} catch (InterruptedException e) {
						stopReading = true;
					}
				}
			}
		});
		readThread.start();
	}

	/**
	 * Setter for reading serial data
	 * 
	 * @param s
	 */
	public static void setStopReading(boolean s) {
		stopReading = s;
	}

	/**
	 * Setter for real-time data
	 * 
	 * @param s
	 */
	public static void setStopRealtimeData(boolean s) {
		stopRealtimeData = s;
	}

	/**
	 * Getter for real-time data
	 * 
	 * @return
	 */
	public static boolean getStopRealtimeData() {
		return stopRealtimeData;
	}

	/**
	 * Getter for device is connected
	 * 
	 * @return
	 */
	public static boolean isDeviceConnected() {

		return deviceConnected;
	}

	/**
	 * Getter for device address. Returns device physical address, or empty
	 * string if no device is connected.
	 * 
	 * @return
	 */
	public static String deviceAddress() {
		if (deviceConnected && bluetooth != null) {
			return BluetoothConnection.btAddress;
		}
		return "";
	}

	@Override
	protected Boolean doInBackground(Void... paramVarArgs) {
		return connectBluetooth();
	}
}
