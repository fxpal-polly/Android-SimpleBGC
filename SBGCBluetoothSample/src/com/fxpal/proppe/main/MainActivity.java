package com.fxpal.proppe.main;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.fxpal.proppe.sbgc.SBGCConnector;

/**
 * @author Author: Patrick Proppe, FX Palo Alto Laboratory, Inc.
 * Copyright (c) 2014
 * All rights reserved.
 * @see SimpleBGC 2.4 serial protocol rev. 0.16
 * 
 */
public class MainActivity extends Activity {

	static TextView statusText;
	static SeekBar pitchSeekbar;
	static SeekBar yawSeekbar;
	int actualYawProgress;
	int actualPitchProgress;

	@Override
	public void onDestroy() {
		super.onDestroy();
		shutdownAll();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		statusText = (TextView) findViewById(R.id.statusText);

		pitchSeekbar = (SeekBar) findViewById(R.id.pitchSeekbar);
		yawSeekbar = (SeekBar) findViewById(R.id.yawSeekbar);

		pitchSeekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				actualPitchProgress = (progress - 50) * -1;
				SBGCConnector.serialProtocol.requestMoveGimbalTo(0,
						actualPitchProgress, actualYawProgress);
			}

			@Override
			public void onStartTrackingTouch(SeekBar arg0) {
			}

			@Override
			public void onStopTrackingTouch(SeekBar arg0) {
			}
		});
		yawSeekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				actualYawProgress = (progress - 180);
				SBGCConnector.serialProtocol.requestMoveGimbalTo(0,
						actualPitchProgress, actualYawProgress);

			}

			@Override
			public void onStartTrackingTouch(SeekBar arg0) {
			}

			@Override
			public void onStopTrackingTouch(SeekBar arg0) {
			}
		});

		BluetoothAdapter mBluetoothAdapter = BluetoothAdapter
				.getDefaultAdapter();
		if (!mBluetoothAdapter.isEnabled())
			mBluetoothAdapter.enable();

		ConnectThread conn = new ConnectThread();
		conn.start();

	}

	@Override
	protected void onResume() {
		super.onResume();

	}

	@Override
	protected void onPause() {
		super.onPause();

	}

	public void connectSBGC() {

		final boolean isAlexmosConnected = SBGCConnector.connectBluetooth();
		Handler h = new Handler(Looper.getMainLooper());
		h.post(new Runnable() {

			@Override
			public void run() {

				if (isAlexmosConnected) {
					statusText.setText("ALEXMOS connected!");
					statusText.setTextColor(Color.BLACK);

				} else {
					statusText.setText("ALEXMOS NOT connected!");
				}

			}
		});

	}

	private void shutdownAll() {

		SBGCConnector.disconnectBluetooth();
		finish();
		android.os.Process.killProcess(android.os.Process.myPid());
	}

	private class ConnectThread extends Thread {

		public ConnectThread() {

		}

		public void run() {
			Looper.prepare();
			connectSBGC();
			Looper.loop();

		}
	}

}
