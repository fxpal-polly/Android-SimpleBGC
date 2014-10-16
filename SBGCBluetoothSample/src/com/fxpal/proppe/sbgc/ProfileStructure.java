package com.fxpal.proppe.sbgc;

/**
 * @author Author: Patrick Proppe, FX Palo Alto Laboratory, Inc.
 * Copyright (c) 2014
 * All rights reserved.
 * @see SimpleBGC 2.4 serial protocol rev. 0.16
 * 
 */
public class ProfileStructure {
	private static final int ROLL_CHANNEL = 0;
	private static final int PITCH_CHANNEL = 1;
	private static final int YAW_CHANNEL = 2;

	public int profileID = 0;
	public int[] P = { 0, 0, 0 };
	public int[] I = { 0, 0, 0 }; // multiplied by 100
	public int[] D = { 0, 0, 0 };
	public int[] power = { 0, 0, 0 };
	public boolean[] invert = { false, false, false };
	public int[] poles = { 0, 0, 0 };
	public int accLimiter = 0;
	public int extFcGainRoll = 0;
	public int extFcGainPitch = 0;
	public int[] rcMinAngle = { -45, -50, -180 };
	public int[] rcMaxAngle = { 45, 50, 180 };
	public int[] rcMode = { 0, 0, 0 };
	public int[] rcLpf = { 0, 0, 0 };
	public int[] rcSpeed = { 0, 0, 0 };
	public int[] rcFollow = { 0, 0, 0 };
	public int gyroTrust = 0;
	public boolean useModel = false;
	public int pwmFreq = 0;
	public int serialSpeed = 0;
	public int rcTrimRoll = 0;
	public int rcTrimPitch = 0;
	public int rcTrimYaw = 0;
	public int rcDeadband = 10;
	public int rcExpoRate = 0;
	public int rcVirtMode = 0;
	public int rcMapRoll = 0;
	public int rcMapPitch = 0;
	public int rcMapYaw = 0;
	public int rcMapCmd = 0;
	public int rcMapFcRoll = 0;
	public int rcMapFcPitch = 0;
	public int rcMixFcRoll = 0;
	public int rcMixFcPitch = 0;
	public int followMode = 0;
	public int followDeadband = 0;
	public int followExpoRate = 0;
	public int followOffsetRoll = 0;
	public int followOffsetPitch = 0;
	public int followOffsetYaw = 0;
	public int axisTop = 0;
	public int axisRight = 0;
	// Board v3
	public int frameAxisTop = 0;
	public int frameAxisRight = 0;
	public int frameImuPos = 0;
	// Board v3 end
	public int gyroLpf = 0;
	public int gyroSens = 0;
	public boolean i2cInternalPullups = false;
	public boolean skipGyroCalib = false;
	public int rcCmdLow = 0;
	public int rcCmdMid = 0;
	public int rcCmdHigh = 0;
	public int[] menuCmd = { 0, 0, 0, 0, 0 };
	public int menuCmdLong = 0;
	public int outputRoll = 0;
	public int outputPitch = 0;
	public int outputYaw = 0;
	public int batThresholdAlarm = 0;
	public int batThresholdMotors = 0;
	public int batCompRef = 0;
	public int beeperModes = 0;
	public int followRollMixStart = 0;
	public int followRollMixRange = 0;
	public int boosterPowerRoll = 0;
	public int boosterPowerPitch = 0;
	public int boosterPowerYaw = 0;
	public int followSpeedRoll = 0;
	public int followSpeedPitch = 0;
	public int followSpeedYaw = 0;
	public boolean frameAngleFromMotors = false;
	// Board v3
	public int[] reservedBytes = new int[25];
	public int curIMU = 0;
	// Board v3 end
	public int curProfileId = 0; // current active profile

	// Getters
	// MIN
	public int getRcMinAngleRoll() {
		return rcMinAngle[ROLL_CHANNEL];
	}

	public int getRcMinAnglePitch() {
		return rcMinAngle[PITCH_CHANNEL];
	}

	public int getRcMinAngleYaw() {
		return rcMinAngle[YAW_CHANNEL];
	}

	public void setRcMinAngleRoll(int angle) {
		this.rcMinAngle[ROLL_CHANNEL] = angle;
	}

	public void setRcMinAnglePitch(int angle) {
		this.rcMinAngle[PITCH_CHANNEL] = angle;
	}

	public void setRcMinAngleYaw(int angle) {
		this.rcMinAngle[YAW_CHANNEL] = angle;
	}

	// MAX
	public int getRcMaxAngleRoll() {
		return rcMaxAngle[ROLL_CHANNEL];
	}

	public int getRcMaxAnglePitch() {
		return rcMaxAngle[PITCH_CHANNEL];
	}

	public int getRcMaxAngleYaw() {
		return rcMaxAngle[YAW_CHANNEL];
	}

	public void setRcMaxAngleRoll(int angle) {
		this.rcMaxAngle[ROLL_CHANNEL] = angle;
	}

	public void setRcMaxAnglePitch(int angle) {
		this.rcMaxAngle[PITCH_CHANNEL] = angle;
	}

	public void setRcMaxAngleYaw(int angle) {
		this.rcMaxAngle[YAW_CHANNEL] = angle;
	}

}
