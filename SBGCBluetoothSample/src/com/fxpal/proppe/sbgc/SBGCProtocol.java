package com.fxpal.proppe.sbgc;

import android.util.Log;

/**
 * @author Author: Patrick Proppe, FX Palo Alto Laboratory, Inc.
 * Copyright (c) 2014
 * All rights reserved.
 * @see SimpleBGC 2.4 serial protocol rev. 0.16
 * 
 */
public class SBGCProtocol extends SBGCProtocolUtils {
	private static final String TAG = "SBGCProtocol";
	static String boardFirmware = "unknown";
	static int boardVersion = 0;

	/**
	 * Requests the board information (firmware version)
	 */
	public static boolean initSBGCProtocol() {

		while (boardFirmware == "unknown") {
			requestBoardInfo();
			wait(100);
			// TODO Add a timeout
		}
		wait(50);
		requestBoardParams();
		wait(100);

		// we are starting in RC Mode
		setCurrentMode(MODE_RC);

		return (boardFirmware != "unknown");

	}

	/**
	 * helper function for waiting a certain time
	 * */
	public static void wait(int ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Handles and delegates the received data
	 * 
	 * WORK IN PROGRESS: Most commands are not yet implemented!
	 * 
	 * @param data
	 *            received data with header
	 * @return true if checksum of 'data' is valid
	 */
	public boolean handleData(byte[] data) {
		boolean retVar = false;

		if (data.length > 0 && verifyChecksum(data)) {
			retVar = true;
			char val;
			switch (data[1]) {
			case CMD_READ_PARAMS:
				Log.d(TAG, "CMD_READ_PARAMS command recv");
				break;
			case CMD_WRITE_PARAMS:
				Log.d(TAG, "CMD_WRITE_PARAMS command recv");
				break;
			case CMD_REALTIME_DATA:
				Log.d(TAG, "CMD_REALTIME_DATA command recv");
				setVersion3(false);
				parseRealTimeData(data);

				break;
			case CMD_BOARD_INFO:
				String boardI[] = getFirmwareVersion(data).split("v");

				boardFirmware = boardI[0];
				boardVersion = Integer.parseInt(boardI[1]);
				if (boardVersion == 3)
					setVersion3(true);

				Log.d(TAG, "CMD_BOARD_INFO command recv: " + boardFirmware
						+ " boardVersion:" + boardVersion);
				break;
			case CMD_CALIB_ACC:
				Log.d(TAG, "CMD_CALIB_ACC command recv");
				break;
			case CMD_CALIB_GYRO:
				Log.d(TAG, "CMD_CALIB_GYRO command recv");
				break;
			case CMD_CALIB_EXT_GAIN:
				Log.d(TAG, "CMD_CALIB_EXT_GAIN command recv");
				break;
			case CMD_USE_DEFAULTS:
				Log.d(TAG, "CMD_USE_DEFAULTS command recv");
				break;
			case CMD_CALIB_POLES:
				Log.d(TAG, "CMD_CALIB_POLES command recv");
				break;
			case CMD_RESET:
				Log.d(TAG, "CMD_RESET command recv");
				break;
			case CMD_HELPER_DATA:
				Log.d(TAG, "CMD_HELPER_DATA command recv");
				break;
			case CMD_CALIB_OFFSET:
				Log.d(TAG, "CMD_CALIB_OFFSET command recv");
				break;
			case CMD_CALIB_BAT:
				Log.d(TAG, "CMD_CALIB_BAT command recv");
				break;
			case CMD_MOTORS_ON:
				Log.d(TAG, "CMD_MOTORS_ON command recv");
				break;
			case CMD_MOTORS_OFF:
				Log.d(TAG, "CMD_MOTORS_OFF command recv");
				break;
			case CMD_TRIGGER_PIN:
				Log.d(TAG, "CMD_TRIGGER_PIN command recv");
				break;
			case CMD_EXECUTE_MENU:
				Log.d(TAG, "CMD_EXECUTE_MENU command recv");
				break;
			case CMD_GET_ANGLES:
				val = getConfirmValue(data);
				Log.d(TAG, "CMD_GET_ANGLES command recv: " + val);
				break;
			case CMD_CONFIRM:
				val = getConfirmValue(data);
				if (val == CMD_MOTORS_OFF)
					Log.d(TAG, "CMD_CONFIRM: Motors OFF");
				else if (val == CMD_MOTORS_ON)
					Log.d(TAG, "CMD_CONFIRM: Motors ON");
				else
					Log.d(TAG, "CMD_CONFIRM: " + val);
				break;
			case CMD_BOARD_INFO_3:
				Log.d(TAG, "CMD_BOARD_INFO_3 command recv");
				break;
			case CMD_READ_PARAMS_3:
				Log.d(TAG, "CMD_READ_PARAMS_3 command recv");
				break;
			case CMD_WRITE_PARAMS_3:
				Log.d(TAG, "CMD_WRITE_PARAMS_3 command recv");
				break;
			case CMD_REALTIME_DATA_3:
				Log.d(TAG, "CMD_REALTIME_DATA_3 command recv");
				setVersion3(true);
				parseRealTimeData(data);
				break;
			case CMD_SELECT_IMU_3:
				Log.d(TAG, "CMD_SELECT_IMU_3 command recv");
				break;
			case CMD_ERROR:
				Log.d(TAG, "CMD_ERROR command recv");
				break;
			default:
				Log.d(TAG, "ERROR - unknown command");
			}
		}
		return retVar;
	}

	/**
	 * [WRAPPER FOR turnTo()] Sends a control command (angle mode) to the board
	 * with the given parameters, movement speed uses default settings [30
	 * degree/sec].
	 * 
	 * Maps via 'turnTo(roll, pitch, yaw)' the yaw range from 0 to 360 to the
	 * board's own mapping which is -720 to 720. 720 are two full clockwise
	 * rotations, -720 are two full counterclockwise rotations.
	 * 
	 * @param roll
	 *            [-90 to 90]
	 * @param pitch
	 *            [-90 to 90]
	 * @param yaw
	 *            [0 to 360]
	 */
	public void requestMoveGimbalTo(int roll, int pitch, int yaw) {
		turnTo(roll, pitch, yaw, currentMode);
	}

	/**
	 * [WRAPPER FOR turnTo()] Sends a control command (angle mode) to the board
	 * with the given parameters and given movement speed [degree/sec].
	 * 
	 * Maps via 'turnTo(roll, pitch, yaw, rollSpeed, pitchSpeed, yawSpeed)' the
	 * yaw range from 0 to 360 to the board's own mapping which is -720 to 720.
	 * 720 are two full clockwise rotations, -720 are two full counterclockwise
	 * rotations.
	 * 
	 * @param roll
	 *            [-90 to 90]
	 * @param pitch
	 *            [-90 to 90]
	 * @param yaw
	 *            [0 to 360]
	 * @param rollSpeed
	 *            [0-???]
	 * @param pitchSpeed
	 *            [0-???]
	 * @param yawSpeed
	 *            [0-???]
	 */
	public void requestMoveGimbalTo(int roll, int pitch, int yaw,
			int rollSpeed, int pitchSpeed, int yawSpeed, int mode) {
		turnTo(roll, pitch, yaw, rollSpeed, pitchSpeed, yawSpeed, mode);
	}

	/**
	 * Requests board information like firmware
	 */
	public static void requestBoardInfo() {
		sendCommand(CMD_BOARD_INFO);

	}

	/**
	 * Requests board params like profiles
	 */
	public static void requestBoardParams() {
		sendCommand(CMD_READ_PARAMS);

	}

	/**
	 * Returns the current active board profile
	 * 
	 * @return active profile
	 */
	public int getActiveProfile() {
		return getRealtimeDataStructure().getCurrentProfile();
	}

	/**
	 * Sends a command to the board to switch to the given profile number
	 * 
	 * @param profileID
	 *            profile number [1, 2 or 3]
	 */
	public void requestSwitchToProfile(int profileID) {
		changeProfile(profileID);
		Log.d(TAG, "ProfileChange");
	}

	/**
	 * Sends a command to the board to switch to the first profile
	 */
	public void requestSwitchToFirstProfile() {
		changeProfile(1);
	}

	/**
	 * Sends a command to the board to switch to the second profile
	 */
	public void requestSwitchToSecondProfile() {
		changeProfile(2);
	}

	/**
	 * Sends a command to the board to switch to the third profile
	 */
	public void requestSwitchToThirdProfile() {
		changeProfile(3);
	}

	/**
	 * Sends a command to the board to turn on the motors
	 */
	public void requestMotorOn() {
		sendCommand(CMD_MOTORS_ON);
	}

	/**
	 * Sends a command to the board to turn off the motors
	 */
	public void requestMotorOff() {
		sendCommand(CMD_MOTORS_OFF);
	}

	/**
	 * Requests the board current parameters
	 */
	public void requestBoardParameters() {
		sendCommand(CMD_READ_PARAMS);
	}

	/**
	 * Requests real-time sensor data, should be polled in a continuous loop at
	 * a specific frequency
	 */
	public void requestRealtimeData() {
		sendCommand(CMD_REALTIME_DATA);
	}

}
