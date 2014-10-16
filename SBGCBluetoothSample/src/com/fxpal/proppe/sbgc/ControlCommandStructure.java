package com.fxpal.proppe.sbgc;

/**
 * @author Author: Patrick Proppe, FX Palo Alto Laboratory, Inc.
 * Copyright (c) 2014
 * All rights reserved.
 * @see SimpleBGC 2.4 serial protocol rev. 0.16
 * 
 */
public class ControlCommandStructure {

	public static final int MODE_NO_CONTROL = 0;
	public static final int MODE_SPEED = 1;
	public static final int MODE_ANGLE = 2;
	public static final int MODE_SPEED_ANGLE = 3;
	public static final int MODE_RC = 4;

	private static final float ANGLE_TO_DEGREE = 0.02197266F;
	private static int mode = 0;
	private static int speedRoll = 0;
	private static int angleRoll = 0;
	private static int speedPitch = 0;
	private static int anglePitch = 0;
	private static int speedYaw = 0;
	private static int angleYaw = 0;
	private static byte[] controlData = new byte[13];

	public byte[] getControlStructure() {
		return getCmdControlDataArray();
	}

	private static byte getFirstByte(int i) {
		return (byte) (i & 0xff);
	}

	private static byte getSecondByte(int i) {
		return (byte) (0xff & i >> 8);
	}

	private static int Degree2Angle(int i) {
		int x = (int) (i * (1.0f / ANGLE_TO_DEGREE));

		return x;
	}

	public static int getIntSigned(byte byte0, byte byte1) {
		return (byte0 & 0xff) + (byte1 << 8);
	}

	public static byte[] getCmdControlDataArray() {
		controlData[0] = (byte) (0xff & mode);
		if (mode == MODE_ANGLE || mode == MODE_SPEED) {
			controlData[1] = getFirstByte(Degree2Angle(speedRoll));
			controlData[2] = getSecondByte(Degree2Angle(speedRoll));
			controlData[3] = getFirstByte(Degree2Angle(angleRoll));
			controlData[4] = getSecondByte(Degree2Angle(angleRoll));
			controlData[5] = getFirstByte(Degree2Angle(speedPitch));
			controlData[6] = getSecondByte(Degree2Angle(speedPitch));
			controlData[7] = getFirstByte(Degree2Angle(anglePitch));
			controlData[8] = getSecondByte(Degree2Angle(anglePitch));
			controlData[9] = getFirstByte(Degree2Angle(speedYaw));
			controlData[10] = getSecondByte(Degree2Angle(speedYaw));
			controlData[11] = getFirstByte(Degree2Angle(angleYaw));
			controlData[12] = getSecondByte(Degree2Angle(angleYaw));
		} else if (mode == MODE_RC) {

			controlData[1] = getFirstByte(speedRoll);
			controlData[2] = getSecondByte(speedRoll);
			controlData[3] = getFirstByte(angleRoll);
			controlData[4] = getSecondByte(angleRoll);
			controlData[5] = getFirstByte(speedPitch);
			controlData[6] = getSecondByte(speedPitch);
			controlData[7] = getFirstByte(anglePitch);
			controlData[8] = getSecondByte(anglePitch);
			controlData[9] = getFirstByte(speedYaw);
			controlData[10] = getSecondByte(speedYaw);
			controlData[11] = getFirstByte(angleYaw);
			controlData[12] = getSecondByte(angleYaw);

		}

		return controlData;
	}

	public static int getMode() {
		return mode;
	}

	public void setMode(int m) {
		mode = m;
	}

	public int getSpeedRoll() {
		return speedRoll;
	}

	public void setSpeedRoll(int s) {
		speedRoll = s;
	}

	public int getAngleRoll() {
		return angleRoll;
	}

	public void setAngleRoll(int a) {
		angleRoll = a;
	}

	public int getSpeedPitch() {
		return speedPitch;
	}

	public void setSpeedPitch(int s) {
		speedPitch = s;
	}

	public int getAnglePitch() {
		return anglePitch;
	}

	public void setAnglePitch(int a) {
		anglePitch = a;
	}

	public int getSpeedYaw() {
		return speedYaw;
	}

	public void setSpeedYaw(int s) {
		speedYaw = s;
	}

	public int getAngleYaw() {
		return angleYaw;
	}

	public void setAngleYaw(int a) {
		angleYaw = a;
	}

}
