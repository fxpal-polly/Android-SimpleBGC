package com.fxpal.proppe.sbgc;

import java.io.IOException;

import android.util.Log;

/**
 * @author Author: Patrick Proppe, FX Palo Alto Laboratory, Inc.
 * Copyright (c) 2014
 * All rights reserved.
 * @see SimpleBGC 2.4 serial protocol rev. 0.16
 * 
 */
public class SBGCProtocolUtils {
	private static final String TAG = "SBGCProtocolUtils";

	protected static final byte CMD_READ_PARAMS = 'R';
	protected static final byte CMD_WRITE_PARAMS = 'W';
	protected static final byte CMD_REALTIME_DATA = 'D';
	protected static final byte CMD_BOARD_INFO = 'V';
	protected static final byte CMD_CALIB_ACC = 'A';
	protected static final byte CMD_CALIB_GYRO = 'g';
	protected static final byte CMD_CALIB_EXT_GAIN = 'G';
	protected static final byte CMD_USE_DEFAULTS = 'F';
	protected static final byte CMD_CALIB_POLES = 'P';
	protected static final byte CMD_RESET = 'r';
	protected static final byte CMD_HELPER_DATA = 'H';
	protected static final byte CMD_CALIB_OFFSET = 'O';
	protected static final byte CMD_CALIB_BAT = 'B';
	protected static final byte CMD_MOTORS_ON = 'M';
	protected static final byte CMD_MOTORS_OFF = 'm';
	protected static final byte CMD_CONTROL = 'C';
	protected static final byte CMD_TRIGGER_PIN = 'T';
	protected static final byte CMD_EXECUTE_MENU = 'E';
	protected static final byte CMD_GET_ANGLES = 'I';
	protected static final byte CMD_CONFIRM = 'C';
	// Board v3.x only (not tested!)
	protected static final byte CMD_BOARD_INFO_3 = 20;
	protected static final byte CMD_READ_PARAMS_3 = 21;
	protected static final byte CMD_WRITE_PARAMS_3 = 22;
	protected static final byte CMD_REALTIME_DATA_3 = 23;
	protected static final byte CMD_SELECT_IMU_3 = 24;
	protected static final byte CMD_ERROR = (byte) 255;
	protected static final byte MAGIC_BYTE = '>';
	protected static boolean BOARD_VERSION_3 = false;
	protected static final float ANGLE_TO_DEGREE = 0.02197266F;

	public static final int MODE_NO_CONTROL = 0;
	public static final int MODE_SPEED = 1;
	public static final int MODE_ANGLE = 2;
	public static final int MODE_SPEED_ANGLE = 3;
	public static final int MODE_RC = 4;

	// fixed data[] positions
	protected static final int MAGIC_BYTE_POS = 0;
	protected static final int COMMAND_ID_POS = 1;
	protected static final int DATA_SIZE_POS = 2;
	protected static final int HEADER_CHECKSUM_POS = 3;
	protected static final int BODY_DATA_POS = 4;

	protected static RealtimeDataStructure rtD = new RealtimeDataStructure();

	protected static ProfileStructure[] profiles = { new ProfileStructure(),
			new ProfileStructure(), new ProfileStructure() };

	protected static int defaultTurnSpeed = 30;
	protected int turnCounter = 0;
	protected int oldYaw = 0;
	protected static int readPosition = BODY_DATA_POS; // must be 4 because of
														// the header

	protected static int currentMode = 0;

	/**
	 * Reads the next word in the data array
	 * 
	 * @param data
	 *            complete data array [header+body]
	 * @return read bytes or -1 on failure
	 */
	protected static int readWord(byte[] data) {
		if (data.length >= readPosition + 2) {
			return (data[(readPosition++)] & 0xFF)
					+ (data[(readPosition++)] << 8);
		}
		return -1;
	}

	/**
	 * Reads the next unsigned word in the data array
	 * 
	 * @param data
	 *            complete data array [header+body]
	 * @return read bytes or -1 on failure
	 */
	protected static int readWordUnsigned(byte[] data) {
		if (data.length >= readPosition + 2) {
			return (data[(readPosition++)] & 0xFF)
					+ ((data[(readPosition++)] & 0xFF) << 8);
		}
		return -1;

	}

	/**
	 * Reads the next byte in the data array
	 * 
	 * @param data
	 *            complete data array [header+body]
	 * @return read byte or -1 on failure
	 */
	protected static int readByte(byte[] data) {
		if (readPosition < data.length) {
			return data[(readPosition++)] & 0xFF;
		}
		return -1;

	}

	/**
	 * Reads the next signed byte in the data array
	 * 
	 * @param data
	 *            complete data array [header+body]
	 * @return read byte or -1 on failure
	 * @throws IOException
	 */
	protected int readByteSigned(byte[] data) {

		if (readPosition < data.length) {
			return data[(readPosition++)];
		}

		return -1;

	}

	protected boolean readBoolean(byte[] data) {
		return readByte(data) == 1;
	}

	/**
	 * Returns a (readable) String representation of the byte array
	 * 
	 * @param data
	 *            complete data array [header+body]
	 * @return bytes as a String
	 */
	static String byteArrayToString(byte[] data) {
		StringBuilder sb = new StringBuilder();
		for (byte b : data) {

			sb.append(Integer.toString(b));
		}
		return sb.toString();
	}

	/**
	 * Verifies the checksum of the given data array
	 * 
	 * @param data
	 *            complete data array [header+body]
	 * @return true if valid, else false
	 */
	static boolean verifyChecksum(byte[] data) {
		if (data.length <= 4)
			return false;

		boolean headerOK = false;
		boolean bodyOK = false;

		if (data[MAGIC_BYTE_POS] == MAGIC_BYTE
				&& ((int) (0xff & data[COMMAND_ID_POS]) + (int) (0xff & data[DATA_SIZE_POS])) % 256 == (0xff & data[HEADER_CHECKSUM_POS])) {
			headerOK = true;
		} else {
			Log.d(TAG, "verifyChecksum(): HEADER BAD");
		}

		int bodyChksm = 0;
		for (int i = 4; i < data.length - 1; i++) {
			bodyChksm += (0xff & data[i]);
		}

		if ((bodyChksm % 256) == (0xff & data[data.length - 1])) {
			bodyOK = true;
		} else {
			Log.d(TAG, "verifyChecksum(): BODY BAD");
		}

		return (headerOK && bodyOK);
	}

	/**
	 * This method should be used to send the commands it takes care of the
	 * whole header & checksum things
	 * 
	 * @param commandID
	 *            is the Command ID character
	 * 
	 * @param rawData
	 *            is the raw data / payload
	 * 
	 */
	protected static void sendCommand(byte commandID, byte rawData[]) {
		byte bodyDataSize = (byte) rawData.length;
		byte headerChecksum = (byte) (((int) commandID + (int) bodyDataSize) % 256);
		int rawBodyChecksum = 0;
		int cnt = 0;
		do {
			if (cnt >= bodyDataSize) {
				byte bodyChecksum = (byte) (rawBodyChecksum % 256);
				byte headerArray[] = new byte[4];
				headerArray[MAGIC_BYTE_POS] = MAGIC_BYTE;
				headerArray[COMMAND_ID_POS] = (byte) (commandID & 0xff);
				headerArray[DATA_SIZE_POS] = (byte) (bodyDataSize & 0xff);
				headerArray[HEADER_CHECKSUM_POS] = (byte) (headerChecksum & 0xff);

				byte headerAndBodyArray[] = new byte[1 + (headerArray.length + rawData.length)];
				System.arraycopy(headerArray, 0, headerAndBodyArray, 0,
						headerArray.length);

				System.arraycopy(rawData, 0, headerAndBodyArray,
						headerArray.length, rawData.length);
				headerAndBodyArray[headerArray.length + rawData.length] = (byte) (bodyChecksum & 0xff);

				if (verifyChecksum(headerAndBodyArray)) {
					SBGCConnector.bluetooth.sendViaBT(headerAndBodyArray);
				} else {
					Log.d(TAG, "Bad Checksum: "
							+ byteArrayToString(headerAndBodyArray));
				}
				return;
			}
			rawBodyChecksum += rawData[cnt];
			cnt++;
		} while (true);
	}

	/**
	 * Basic wrapper function for commands without payload
	 * 
	 * @param commandID
	 *            command to send
	 */
	protected static void sendCommand(byte commandID) {

		sendCommand(commandID, new byte[0]);
	}

	/**
	 * changes the profile
	 * 
	 * @param profileID
	 *            number of the profile [1,2,3]
	 * 
	 **/
	protected void changeProfile(int profileID) {
		byte profileByte[] = new byte[1];
		profileByte[0] = (byte) (profileID);
		Log.d(TAG, "changeProfile(" + profileID + ")");
		sendCommand(CMD_EXECUTE_MENU, profileByte);
	}

	/**
	 * Returns the confirmation value Not error safe, should only be called when
	 * a confirmation is received
	 * 
	 * @param data
	 *            complete data array [header+body]
	 * @return confirmation values
	 */
	protected char getConfirmValue(byte[] data) {

		char x = (char) (data[data.length - 2] & 0x000000FF);
		return x;
	}

	/**
	 * Returns the firmware version
	 * 
	 * @param data
	 *            complete data array [header+body]
	 * @return firmware versions
	 */
	protected String getFirmwareVersion(byte[] data) {
		if (data.length < 6)
			return "ERROR";

		int boardVersion = (data[4] & 0xFF) / 10;

		int index = 1;
		byte[] buffer = { data[5], data[6] };
		int first = ((int) (buffer[index--])) & 0x000000FF;
		int second = ((int) (buffer[index--])) & 0x000000FF;
		int ushort = ((int) first << 8 | second) & 0xFFFF;
		String rawVer = Integer.toString(ushort);

		String ver = rawVer.substring(0, 1) + "." + rawVer.substring(1, 3)
				+ "b" + rawVer.substring(3, 4) + "v" + boardVersion;
		return ver;
	}

	/**
	 * Parses the received real-time data and saves it in the
	 * RealtimeDataStructure
	 * 
	 * @param data
	 *            received data
	 * @return RealtimeDataStructure
	 */
	protected static RealtimeDataStructure parseRealTimeData(byte[] data) {

		for (int i = 0; i < 3; i++) {
			getRealtimeDataStructure().setAcc(readWord(data), i);
			getRealtimeDataStructure().setGyro(readWord(data), i);
		}

		for (int i = 0; i < getRealtimeDataStructure().getDebug().length; i++) {
			getRealtimeDataStructure().setDebug(readWord(data), i);
		}
		for (int i = 0; i < getRealtimeDataStructure().getRcData().length; i++) {
			getRealtimeDataStructure().setRcData(readWord(data), i);
		}
		if (BOARD_VERSION_3) {

			for (int i = 0; i < 3; i++) {
				getRealtimeDataStructure().setAngle(
						(readWord(data) * ANGLE_TO_DEGREE), i);

			}
			for (int i = 0; i < 3; i++) {
				getRealtimeDataStructure().setFrameAngle(
						(readWord(data) * ANGLE_TO_DEGREE), i);
			}
			for (int i = 0; i < 3; i++)
				getRealtimeDataStructure().setRc_angle(
						(readWord(data) * ANGLE_TO_DEGREE), i);
		} else {

			for (int i = 0; i < 3; i++) {
				getRealtimeDataStructure().setAngle(
						(readWord(data) * ANGLE_TO_DEGREE), i);
			}
			for (int i = 0; i < 3; i++) {
				getRealtimeDataStructure().setRc_angle(
						(readWord(data) * ANGLE_TO_DEGREE), i);
			}
		}

		getRealtimeDataStructure().setCycleTime(readWord(data));
		getRealtimeDataStructure().setI2cErrorCount(readWordUnsigned(data));
		getRealtimeDataStructure().setErrorCode(readByte(data));
		getRealtimeDataStructure().setBatteryValue(readWordUnsigned(data));
		getRealtimeDataStructure().setPowered(readByte(data) > 0);
		if (BOARD_VERSION_3) {
			getRealtimeDataStructure().setCurrentIMU(readByte(data));
		}
		getRealtimeDataStructure().setCurrentProfile(readByte(data));
		for (int i = 0; i < 3; i++) {
			getRealtimeDataStructure().setPower(readByte(data), i);
		}
		// Reset position to first Data-Byte
		readPosition = BODY_DATA_POS;
		return getRealtimeDataStructure();
	}

	/**
	 * Sends a control command to the board with the given values [UNMAPPED!]
	 * 
	 * @param roll
	 *            [-90 to 90]
	 * @param pitch
	 *            [-90 to 90]
	 * @param yaw
	 *            [-720 to 720]
	 * @param rollSpeed
	 *            [0-???]
	 * @param pitchSpeed
	 *            [0-???]
	 * @param yawSpeed
	 *            [0-???]
	 * @param mode
	 *            [0-4]
	 */

	protected void sendTurnCommand(int roll, int pitch, int yaw, int rollSpeed,
			int pitchSpeed, int yawSpeed, int mode) {
		ControlCommandStructure cCmd = new ControlCommandStructure();

		cCmd.setMode(mode);
		cCmd.setAnglePitch(pitch);
		cCmd.setAngleRoll(roll);
		cCmd.setAngleYaw(yaw);

		cCmd.setSpeedPitch(pitchSpeed);
		cCmd.setSpeedRoll(rollSpeed);
		cCmd.setSpeedYaw(yawSpeed);

		sendCommand(CMD_CONTROL, cCmd.getControlStructure());
	}

	/**
	 * Sends a control command to the board with the given values [UNMAPPED!]
	 * 
	 * @param roll
	 *            [-90 to 90]
	 * @param pitch
	 *            [-90 to 90]
	 * @param yaw
	 *            [-720 to 720]
	 */
	protected void turnTo(int roll, int pitch, int yaw, int mode) {
		turnTo(roll, pitch, yaw, defaultTurnSpeed, defaultTurnSpeed,
				defaultTurnSpeed, mode);

	}

	/**
	 * Sends a control command (angle mode) to the board with the given
	 * parameters and given movement speed [degree/sec].
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
	protected void turnTo(int roll, int pitch, int yaw, int rollSpeed,
			int pitchSpeed, int yawSpeed, int mode) {

		String md = (mode == MODE_ANGLE) ? "Angle" : "RC";
		Log.d(TAG, "Current Mode = " + md);
		// TODO decide on active profile, which mode to use
		if (mode == MODE_ANGLE) {

			int yawDelta = (yaw - oldYaw);
			int goToYaw = 0;

			// Border Fix && direction
			if (yawDelta <= -180) {
				yawDelta += 360;
			} else if (yawDelta > 180) {
				yawDelta -= 360;
			}

			if (yawDelta < 0) {
				turnCounter -= Math.abs(yawDelta);
			} else {
				turnCounter += Math.abs(yawDelta);
			}
			if (turnCounter >= 1440)
				turnCounter -= 1440;

			// check if even or odd
			if ((turnCounter & 1) == 0) {
				// even...
				if (turnCounter <= 720) {
					goToYaw = turnCounter % 720;
				} else {
					goToYaw = (-720 + (turnCounter % 720));
				}
			} else {
				// odd...
				if (turnCounter > 720) {
					goToYaw = (-720 + (turnCounter % 720));
				} else {
					goToYaw = turnCounter % 720;
				}

			}
			sendTurnCommand(roll, pitch, goToYaw, defaultTurnSpeed,
					defaultTurnSpeed, defaultTurnSpeed, mode);
			oldYaw = yaw;
		} else if (mode == MODE_RC) {

			int rcYaw = (int) (yaw * 500F / Math.abs((float) profiles[0]
					.getRcMaxAngleYaw()));
			// Pitch & Roll: assuming RcMin=RcMax
			int rcPitch = (int) (pitch * (500F / Math.abs((float) profiles[0]
					.getRcMaxAnglePitch())));
			int rcRoll = (int) (roll * (500F / Math.abs((float) profiles[0]
					.getRcMaxAngleRoll())));
			// 360deg mapping atm only for yaw
			if (yaw > 180) {
				// counterclockwise
				float yawAngleToRC = 500F / Math.abs((float) profiles[0]
						.getRcMinAngleYaw());
				float pitchAngleToRC = 500F / Math.abs((float) profiles[0]
						.getRcMinAnglePitch());
				rcYaw = (int) -((180 - (yaw - 180)) * yawAngleToRC);
				rcPitch = (int) (pitch * pitchAngleToRC);
			}

			Log.d(TAG, "RCYaw=" + rcYaw);
			sendTurnCommand(rcRoll, rcPitch, rcYaw, defaultTurnSpeed,
					defaultTurnSpeed, defaultTurnSpeed, mode);
		}
	}

	/**
	 * Getter for RealtimeDataStructure
	 * 
	 * @return RealtimeDataStructure
	 */
	public static RealtimeDataStructure getRealtimeDataStructure() {
		return rtD;
	}

	/**
	 * Setter for RealtimeDataStructure
	 * 
	 * @param rtD
	 *            RealtimeDataStructure
	 */
	public static void setRtD(RealtimeDataStructure struc) {
		rtD = struc;
	}

	public void parseBoardParams(byte[] data, int profile) {

		ProfileStructure p = profiles[profile];

		for (int i = 0; i < 3; i++) {
			p.P[i] = readByte(data);
			p.I[i] = readByte(data);
			p.D[i] = readByte(data);
			p.power[i] = readByte(data);
			p.invert[i] = readBoolean(data);
			p.poles[i] = readByte(data);
		}
		p.accLimiter = readByte(data);

		p.extFcGainPitch = readByteSigned(data);
		p.extFcGainPitch = readByteSigned(data);

		for (int i = 0; i < 3; i++) {
			p.rcMinAngle[i] = readWord(data);
			p.rcMaxAngle[i] = readWord(data);
			p.rcMode[i] = readByte(data);
			p.rcLpf[i] = readByte(data);
			p.rcSpeed[i] = readByte(data);
			p.rcFollow[i] = readByteSigned(data);
		}

		p.gyroTrust = readByte(data);
		p.useModel = readBoolean(data);
		p.pwmFreq = readByte(data);
		p.serialSpeed = readByte(data);
		p.rcTrimRoll = readByteSigned(data);
		p.rcTrimPitch = readByteSigned(data);
		p.rcTrimYaw = readByteSigned(data);

		p.rcDeadband = readByte(data);
		p.rcExpoRate = readByte(data);
		p.rcVirtMode = readByte(data);
		p.rcMapRoll = readByte(data);

		p.rcMapPitch = readByte(data);
		p.rcMapYaw = readByte(data);
		p.rcMapCmd = readByte(data);
		p.rcMapFcRoll = readByte(data);
		p.rcMapFcPitch = readByte(data);
		p.rcMixFcRoll = readByte(data);
		p.rcMixFcPitch = readByte(data);

		p.followMode = readByte(data);
		p.followDeadband = readByte(data);
		p.followExpoRate = readByte(data);

		p.followOffsetRoll = readByteSigned(data);
		p.followOffsetPitch = readByteSigned(data);
		p.followOffsetYaw = readByteSigned(data);

		p.axisTop = readByteSigned(data);
		p.axisRight = readByteSigned(data);
		if (BOARD_VERSION_3) {
			p.frameAxisTop = readByteSigned(data);
			p.frameAxisRight = readByteSigned(data);
			p.frameImuPos = readByte(data);
		}
		p.gyroLpf = readByte(data);
		p.gyroSens = readByte(data);
		p.i2cInternalPullups = readBoolean(data);
		p.skipGyroCalib = readBoolean(data);

		p.rcCmdLow = readByte(data);
		p.rcCmdMid = readByte(data);
		p.rcCmdHigh = readByte(data);

		for (int i = 0; i < p.menuCmd.length; i++) {
			p.menuCmd[i] = readByte(data);
		}
		p.menuCmdLong = readByte(data);

		p.outputRoll = readByte(data);
		p.outputPitch = readByte(data);
		p.outputYaw = readByte(data);

		p.batThresholdAlarm = readWord(data);
		p.batThresholdMotors = readWord(data);
		p.batCompRef = readWord(data);
		p.beeperModes = readByte(data);
		p.followRollMixStart = readByte(data);
		p.followRollMixRange = readByte(data);

		p.boosterPowerRoll = readByte(data);
		p.boosterPowerPitch = readByte(data);
		p.boosterPowerYaw = readByte(data);

		p.followSpeedRoll = readByte(data);
		p.followSpeedPitch = readByte(data);
		p.followSpeedYaw = readByte(data);

		p.frameAngleFromMotors = readBoolean(data);

		if (BOARD_VERSION_3) {
			for (int i = 0; i < 25; i++) {
				p.reservedBytes[i] = readByte(data);
			}
			p.curIMU = readByte(data);
		}

		p.curProfileId = readByte(data);

	}

	public static boolean isVersion3() {
		return BOARD_VERSION_3;
	}

	public static void setVersion3(boolean isVersion3) {
		BOARD_VERSION_3 = isVersion3;
	}

	public int getCurrentMode() {
		return currentMode;
	}

	public static void setCurrentMode(int mode) {
		currentMode = mode;
	}

	public static void setCurrentModeRC() {
		currentMode = MODE_RC;
	}

	public static void setCurrentModeAngle() {
		currentMode = MODE_ANGLE;
	}

}
