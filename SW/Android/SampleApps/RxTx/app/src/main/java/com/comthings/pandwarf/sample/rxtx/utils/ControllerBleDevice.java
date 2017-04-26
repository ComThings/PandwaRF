package com.comthings.pandwarf.sample.rxtx.utils;

import android.app.Activity;
import android.util.Log;

import com.comthings.gollum.api.gollumandroidlib.GollumDongle;
import com.comthings.gollum.api.gollumandroidlib.ble.GollumBleManagerCallbacks;

import java.util.HashMap;
import java.util.Observable;

/**
 * Created by Seb on 03/07/2015.
 */
public class ControllerBleDevice extends Observable implements GollumBleManagerCallbacks {

	private static final String TAG = "ControllerBleDevice";

	private Activity parentActivity;

	public ControllerBleDevice(Activity parentActivity) {
		this.parentActivity = parentActivity;
	}

	/**
	 * Hahsmap :  "action" -> "name_methode"
	 * "params" -> "Hashmap<"nomParam", value> "
	 */


	public void notifyAddrDevice(String addr) {
		Log.d(TAG, "notifyAddrDevice");

		HashMap<String, String> params = new HashMap<String, String>();
		params.put("addr", addr);

		HashMap<String, Object> data = new HashMap<String, Object>();
		data.put("action", "notifyAddrDevice");
		data.put("params", params);

		setChanged();
		notifyObservers(data);
	}

	public void notifyTypeDevice(int type) {
		Log.d(TAG, "notifyTypeDevice");

		HashMap<String, Integer> params = new HashMap<String, Integer>();
		params.put("type", type);

		HashMap<String, Object> data = new HashMap<String, Object>();
		data.put("action", "notifyTypeDevice");
		data.put("params", params);

		setChanged();
		notifyObservers(data);
	}

	@Override
	public void onDeviceNameValueReceived(String deviceName) {
		Log.d(TAG, "onDeviceNameValueReceived");

		HashMap<String, String> params = new HashMap<String, String>();
		params.put("deviceName", deviceName);

		HashMap<String, Object> data = new HashMap<String, Object>();
		data.put("action", "onDeviceNameValueReceived");
		data.put("params", params);

		setChanged();
		notifyObservers(data);
	}

	@Override
	public void onApparenceValueReceived(int apparence) {
		Log.d(TAG, "onApparenceValueReceived");

		HashMap<String, Integer> params = new HashMap<String, Integer>();
		params.put("apparence", apparence);

		HashMap<String, Object> data = new HashMap<String, Object>();
		data.put("action", "onApparenceValueReceived");
		data.put("params", params);

		setChanged();
		notifyObservers(data);
	}

	@Override
	public void onConnectionParamsValueReceived(int minConnectionInterval, int maxConnectionInterval, int slaveLatency, int superVisionTimeoutMultiplier) {
		Log.d(TAG, "onConnectionParamsValueReceived");

		HashMap<String, Integer> params = new HashMap<String, Integer>();
		params.put("minConnectionInterval", minConnectionInterval);
		params.put("maxConnectionInterval", maxConnectionInterval);
		params.put("slaveLatency", slaveLatency);
		params.put("superVisionTimeoutMultiplier", superVisionTimeoutMultiplier);


		HashMap<String, Object> data = new HashMap<String, Object>();
		data.put("action", "onConnectionParamsValueReceived");
		data.put("params", params);

		setChanged();
		notifyObservers(data);
	}

	@Override
	public void onManufacturerNameReceived(String company) {
		Log.d(TAG, "onManufacturerNameReceived");

		HashMap<String, String> params = new HashMap<String, String>();
		params.put("company", company);

		HashMap<String, Object> data = new HashMap<String, Object>();
		data.put("action", "onManufacturerNameReceived");
		data.put("params", params);

		setChanged();
		notifyObservers(data);
	}

	@Override
	public void onModelNumberReceived(String model) {
		Log.d(TAG, "onModelNumberReceived");

		HashMap<String, String> params = new HashMap<String, String>();
		params.put("model", model);

		HashMap<String, Object> data = new HashMap<String, Object>();
		data.put("action", "onModelNumberReceived");
		data.put("params", params);

		setChanged();
		notifyObservers(data);
	}

	@Override
	public void onSerialNumberReceived(String serial) {
		Log.d(TAG, "onSerialNumberReceived");

		HashMap<String, String> params = new HashMap<String, String>();
		params.put("serial", serial);

		HashMap<String, Object> data = new HashMap<String, Object>();
		data.put("action", "onSerialNumberReceived");
		data.put("params", params);

		setChanged();
		notifyObservers(data);
	}

	@Override
	public void onHardwareRevisionReceived(String hardware) {
		Log.d(TAG, "onHardwareRevisionReceived");

		HashMap<String, String> params = new HashMap<String, String>();
		params.put("hardware", hardware);

		HashMap<String, Object> data = new HashMap<String, Object>();
		data.put("action", "onHardwareRevisionReceived");
		data.put("params", params);

		setChanged();
		notifyObservers(data);
	}

	@Override
	public void onSoftwareRevisionReceived(String softwareId) {
		Log.d(TAG, "onSoftwareRevisionReceived");

		HashMap<String, String> params = new HashMap<String, String>();
		params.put("softwareId", softwareId);

		HashMap<String, Object> data = new HashMap<String, Object>();
		data.put("action", "onSoftwareRevisionReceived");
		data.put("params", params);

		setChanged();
		notifyObservers(data);
	}

	@Override
	public void onFirmwareRevisionReceived(String firmwareId) {
		Log.d(TAG, "onFirmwareRevisionReceived");

		HashMap<String, String> params = new HashMap<String, String>();
		params.put("firmwareId", firmwareId);

		HashMap<String, Object> data = new HashMap<String, Object>();
		data.put("action", "onFirmwareRevisionReceived");
		data.put("params", params);

		setChanged();
		notifyObservers(data);
	}

	@Override
	public void onSoftwareVersionReceived(String softwareVersion) {
		Log.d(TAG, "onSoftwareVersionReceived");

		HashMap<String, String> params = new HashMap<String, String>();
		params.put("softwareVersion", softwareVersion);

		HashMap<String, Object> data = new HashMap<String, Object>();
		data.put("action", "onSoftwareVersionReceived");
		data.put("params", params);

		setChanged();
		notifyObservers(data);
	}

	@Override
	public void onFirmwareVersionReceived(String firmwareVersion) {
		Log.d(TAG, "onFirmwareVersionReceived");

		HashMap<String, String> params = new HashMap<String, String>();
		params.put("firmwareVersion", firmwareVersion);

		HashMap<String, Object> data = new HashMap<String, Object>();
		data.put("action", "onFirmwareVersionReceived");
		data.put("params", params);

		setChanged();
		notifyObservers(data);
	}

	@Override
	public void onRxValueReceived(byte[] data) {
		Log.d(TAG, "onRxValueReceived");

		HashMap<String, byte[]> params = new HashMap<String, byte[]>();
		params.put("data", data);

		HashMap<String, Object> hm = new HashMap<String, Object>();
		hm.put("action", "onRxValueReceived");
		hm.put("params", params);

		setChanged();
		notifyObservers(hm);
	}

	@Override
	public void onLoopBackModeReceived(boolean result) {
		Log.d(TAG, "onLoopBackModeReceived ");

		HashMap<String, Boolean> params = new HashMap<String, Boolean>();
		params.put("result", result);

		HashMap<String, Object> data = new HashMap<String, Object>();
		data.put("action", "onLoopBackModeReceived");
		data.put("params", params);

		setChanged();
		notifyObservers(data);
	}

	@Override
	public void onButtonPushedReceived(int button) {
		Log.d(TAG, "onButtonPushedReceived ");

		HashMap<String, Integer> params = new HashMap<String, Integer>();
		params.put("button", button);

		HashMap<String, Object> data = new HashMap<String, Object>();
		data.put("action", "onButtonPushedReceived");
		data.put("params", params);

		setChanged();
		notifyObservers(data);

	}

	@Override
	public void onBusConfigReceived(String status) {
		Log.d(TAG, "onBusConfigReceived");

		HashMap<String, String> params = new HashMap<String, String>();
		params.put("status", status);

		HashMap<String, Object> data = new HashMap<String, Object>();
		data.put("action", "onBusConfigReceived");
		data.put("params", params);

		setChanged();
		notifyObservers(data);
	}

	@Override
	public void onBusConfigUsbAllowed(Boolean allowed) {
		Log.d(TAG, "onBusConfigUsbAllowed");

		HashMap<String, Boolean> params = new HashMap<String, Boolean>();
		params.put("allowed", allowed);

		HashMap<String, Object> data = new HashMap<String, Object>();
		data.put("action", "onBusConfigUsbAllowed");
		data.put("params", params);

		setChanged();
		notifyObservers(data);
	}

	@Override
	public void onButtonPushedNotificationStatusReceived(Boolean enabled) {
		Log.d(TAG, "onButtonPushedNotificationStatusReceived");

		HashMap<String, Boolean> params = new HashMap<String, Boolean>();
		params.put("enabled", enabled);

		HashMap<String, Object> data = new HashMap<String, Object>();
		data.put("action", "onButtonPushedNotificationStatusReceived");
		data.put("params", params);

		setChanged();
		notifyObservers(data);
	}

	@Override
	public void onBleErrorReceived(int lineNumber, int errorCode, String fileName) {
		Log.d(TAG, "onBleErrorReceived");

		HashMap<String, Object> data = new HashMap<String, Object>();

		if (lineNumber <= 0) {
			data.put("action", "bleErrorReceived");
			data.put("params", null);
		} else {
			HashMap<String, String> params = new HashMap<String, String>();
			params.put("line number", Integer.toBinaryString(lineNumber));
			params.put("error", Integer.toBinaryString(errorCode));
			params.put("file name", fileName);

			data.put("action", "bleErrorReceived");
			data.put("params", params);
		}

		setChanged();
		notifyObservers(data);
	}

	@Override
	public void onBatteryNotificationStatusReceived(Boolean enabled) {
		Log.d(TAG, "onBatteryNotificationStatusReceived");

		HashMap<String, Boolean> params = new HashMap<String, Boolean>();
		params.put("enabled", enabled);

		HashMap<String, Object> data = new HashMap<String, Object>();
		data.put("action", "onBatteryNotificationStatusReceived");
		data.put("params", params);

		setChanged();
		notifyObservers(data);
	}

	@Override
	public void onDeviceConnecting() {
		Log.d(TAG, "onDeviceConnecting");

		HashMap<String, Object> data = new HashMap<String, Object>();
		data.put("action", "onDeviceConnecting");

		setChanged();
		notifyObservers(data);
	}

	@Override
	public void onDeviceConnected() {
		Log.d(TAG, "onDeviceConnected");

		HashMap<String, Object> data = new HashMap<String, Object>();
		data.put("action", "onDeviceConnected");

		setChanged();
		notifyObservers(data);
	}

	@Override
	public void onDeviceDisconnecting() {
		Log.d(TAG, "onDeviceDisconnecting");

		HashMap<String, Object> data = new HashMap<String, Object>();
		data.put("action", "onDeviceDisconnecting");

		setChanged();
		notifyObservers(data);
	}

	@Override
	public void onDeviceDisconnected() {
		Log.d(TAG, "onDeviceDisconnected");

		HashMap<String, Object> data = new HashMap<String, Object>();
		data.put("action", "onDeviceDisconnected");

		setChanged();
		notifyObservers(data);
	}

	@Override
	public void onLinklossOccur() {
		Log.d(TAG, "onLinklossOccur");

		HashMap<String, Object> data = new HashMap<String, Object>();
		data.put("action", "onLinklossOccur");

		setChanged();
		notifyObservers(data);
	}

	@Override
	public void onServicesDiscovered(boolean optionalServicesFound) {
		Log.d(TAG, "onServicesDiscovered");

		HashMap<String, Object> data = new HashMap<String, Object>();
		data.put("action", "onServicesDiscovered");

		setChanged();
		notifyObservers(data);
	}

	@Override
	public void onDeviceReady() {
		Log.d(TAG, "onDeviceReady");

		HashMap<String, Object> data = new HashMap<String, Object>();
		data.put("action", "onDeviceReady");

		setChanged();
		notifyObservers(data);

		//Notification for RX
		GollumDongle.getInstance(parentActivity).writeNotifRXCharacteristic(true);
	}

	@Override
	public void onBatteryValueReceived(int value) {
		Log.d(TAG, "onBatteryValueReceived: " + value);

		HashMap<String, Integer> params = new HashMap<String, Integer>();
		params.put("value", value);

		HashMap<String, Object> data = new HashMap<String, Object>();
		data.put("action", "onBatteryValueReceived");
		data.put("params", params);

		setChanged();
		notifyObservers(data);
	}

	@Override
	public void onRssiValueReceived(int value) {
		Log.d(TAG, "onRssiValueReceived: " + value);

		HashMap<String, Integer> params = new HashMap<String, Integer>();
		params.put("value", value);

		HashMap<String, Object> data = new HashMap<String, Object>();
		data.put("action", "onRssiValueReceived");
		data.put("params", params);

		setChanged();
		notifyObservers(data);
	}

	@Override
	public void onBondingRequired() {
		Log.d(TAG, "onBondingRequired");

		HashMap<String, Object> data = new HashMap<String, Object>();
		data.put("action", "onBondingRequired");

		setChanged();
		notifyObservers(data);
	}

	@Override
	public void onBonded() {
		Log.d(TAG, "onBonded");

		HashMap<String, Object> data = new HashMap<String, Object>();
		data.put("action", "onBonded");

		setChanged();
		notifyObservers(data);
	}

	@Override
	public void onError(String message, int errorCode) {
		Log.d(TAG, "onError");

		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("message", message);
		params.put("errorCode", errorCode);

		HashMap<String, Object> data = new HashMap<String, Object>();
		data.put("action", "onError");
		data.put("params", params);

		setChanged();
		notifyObservers(data);
	}

	@Override
	public void onDeviceNotSupported() {
		Log.d(TAG, "onDeviceNotSupported");

		HashMap<String, Object> data = new HashMap<String, Object>();
		data.put("action", "onDeviceNotSupported");

		setChanged();
		notifyObservers(data);
	}


}
