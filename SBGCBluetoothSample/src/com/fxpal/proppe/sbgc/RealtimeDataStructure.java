package com.fxpal.proppe.sbgc;


/**
 * @author Author: Patrick Proppe, FX Palo Alto Laboratory, Inc.
 * Copyright (c) 2014
 * All rights reserved.
 * @see SimpleBGC 2.4 serial protocol rev. 0.16
 * 
 */
public class RealtimeDataStructure {

	private static RealtimeDataStructure realtimeData = new RealtimeDataStructure();
	private static final int ROLL_CHANNEL = 0;
	private static final int PITCH_CHANNEL = 1;
	private static final int YAW_CHANNEL = 2;
	private static final int RC_UNDEF = -8500;

	private int[] acc = new int[3];// {Roll, Pitch, Yaw}
	private int[] gyro = new int[3];// {Roll, Pitch, Yaw}
	private int[] debug = new int[4];
	private int[] rcData = { RC_UNDEF, RC_UNDEF, RC_UNDEF, RC_UNDEF, RC_UNDEF, RC_UNDEF };
	private float[] angle = new float[3]; // {Roll, Pitch, Yaw} - Actual angle
											// in degrees
	private float[] frameAngle = new float[3]; // {Roll, Pitch, Yaw}
	private float[] rc_angle = new float[3];// {Roll, Pitch, Yaw}
	private int cycleTime;
	private int i2cErrorCount;
	private int errorCode;
	private float batteryValue;
	private boolean isPowered;
	private int currentProfile = 0;
	private int currentIMU = 1;
	private int[] power = new int[3];

	public static RealtimeDataStructure getRealtimeData() {
		return realtimeData;
	}

	public static void setCurrentRealtimeData(RealtimeDataStructure curRealtimeData) {
		realtimeData = curRealtimeData;
	}

	public int[] getAcc() {
		return acc;
	}

	public void setAcc(int acc, int position) {

		if (position < 3)
			this.acc[position] = acc;
	}

	public int[] getGyro() {
		return gyro;
	}

	public void setGyro(int gyro, int position) {
		this.gyro[position] = gyro;
	}

	public int[] getDebug() {
		return debug;
	}

	public void setDebug(int debug, int position) {
		if (position < 4)
			this.debug[position] = debug;
	}

	public int[] getRcData() {
		return rcData;
	}

	public void setRcData(int rc, int position) {
		if (position < 3)
			this.rcData[position] = rc;
	}

	public float[] getAngle() {
		return angle;
	}

	public float getRoll() {
		return angle[ROLL_CHANNEL];
	}

	public void setRoll(float f) {
		this.angle[ROLL_CHANNEL] = f;
	}

	public float getPitch() {
		return angle[PITCH_CHANNEL];
	}

	public void setPitch(float f) {
		this.angle[PITCH_CHANNEL] = f;
	}

	public float getYaw() {
		return angle[YAW_CHANNEL];
	}

	public void setYaw(float f) {
		this.angle[YAW_CHANNEL] = f;
	}

	public void setAngle(float angle, int position) {
		if (position < 3)
			this.angle[position] = angle;
	}

	public void setFrameAngle(float frameAngle, int position) {
		if (position < 3)
			this.frameAngle[position] = frameAngle;
	}

	public float[] getFrameAngle() {
		return frameAngle;
	}

	public float[] getRc_angle() {
		return rc_angle;
	}

	public void setRc_angle(float rc_angle, int position) {
		if (position < 3)
			this.rc_angle[position] = rc_angle;
	}

	public int getCycleTime() {
		return cycleTime;
	}

	public void setCycleTime(int cycleTime) {
		this.cycleTime = cycleTime;
	}

	public int getI2cErrorCount() {
		return i2cErrorCount;
	}

	public void setI2cErrorCount(int i2cErrorCount) {
		this.i2cErrorCount = i2cErrorCount;
	}

	public int getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}

	public float getBatteryValue() {
		return batteryValue;
	}

	public void setBatteryValue(int val) {
		if (val > 0)
			this.batteryValue = val / 100;
	}

	public boolean isPowered() {
		return isPowered;
	}

	public void setPowered(boolean isPowered) {
		this.isPowered = isPowered;
	}

	public int getCurrentProfile() {

		return currentProfile;
	}

	public void setCurrentProfile(int profile) {

		if (profile >= 0 && profile < 5) {

			this.currentProfile = profile;
		}
	}

	public int getCurrentIMU() {
		return currentIMU;
	}

	public void setCurrentIMU(int imu) {
		this.currentIMU = imu;
	}

	public int[] getPower() {
		return power;
	}

	public void setPower(int power, int position) {
		if (position < 3)
			this.power[position] = power;
	}

}
