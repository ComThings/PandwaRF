package com.comthings.pandwarf.sample.SpectrumAnalyzer.utils;

import android.app.Activity;

import com.comthings.gollum.api.gollumandroidlib.GollumDongle;
import com.comthings.gollum.api.gollumandroidlib.ble.GollumBleManagerCallbacks;

/**
 * Created by Amine on 14/04/2017.
 */
public class ControllerBleDevice implements GollumBleManagerCallbacks {

	private Activity parentActivity;

	public ControllerBleDevice(Activity parentActivity) {
		this.parentActivity = parentActivity;
	}

	@Override
	public void onDeviceNameValueReceived(String d) {

	}

	@Override
	public void onApparenceValueReceived(int a) {

	}

	@Override
	public void onConnectionParamsValueReceived(int min, int max, int s, int sp) {


	}

	@Override
	public void onManufacturerNameReceived(String c) {

	}

	@Override
	public void onModelNumberReceived(String m) {

	}

	@Override
	public void onSerialNumberReceived(String s) {

	}

	@Override
	public void onHardwareRevisionReceived(String h) {

	}

	@Override
	public void onSoftwareRevisionReceived(String s) {
	}

	@Override
	public void onFirmwareRevisionReceived(String f) {
	}

	@Override
	public void onSoftwareVersionReceived(String sv) {
	}

	@Override
	public void onFirmwareVersionReceived(String fv) {
	}

	@Override
	public void onRxValueReceived(byte[] d) {
	}

	@Override
	public void onLoopBackModeReceived(boolean r) {
	}

	@Override
	public void onButtonPushedReceived(int b) {

	}

	@Override
	public void onBusConfigReceived(String s) {
	}

	@Override
	public void onBusConfigUsbAllowed(Boolean a) {
	}

	@Override
	public void onButtonPushedNotificationStatusReceived(Boolean e) {
		;
	}

	@Override
	public void onBleErrorReceived(int var1, int var2, String f) {
	}

	@Override
	public void onBatteryNotificationStatusReceived(Boolean e) {
	}

	@Override
	public void onDeviceConnecting() {
	}

	@Override
	public void onDeviceConnected() {
	}

	@Override
	public void onDeviceDisconnecting() {
	}

	@Override
	public void onDeviceDisconnected() {
	}

	@Override
	public void onLinklossOccur() {
	}

	@Override
	public void onServicesDiscovered(boolean op) {
	}

	@Override
	public void onDeviceReady() {

		//Notification for RX
		GollumDongle.getInstance(parentActivity).writeNotifRXCharacteristic(true);
	}

	@Override
	public void onBatteryValueReceived(int var1) {

	}

	@Override
	public void onRssiValueReceived(int var1) {

	}

	@Override
	public void onBondingRequired() {

	}

	@Override
	public void onBonded() {

	}

	@Override
	public void onError(String m, int e) {

	}

	@Override
	public void onDeviceNotSupported() {

	}

}
