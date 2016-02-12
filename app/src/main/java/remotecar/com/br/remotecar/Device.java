package remotecar.com.br.remotecar;

import java.io.Serializable;

import android.bluetooth.BluetoothDevice;

public class Device implements Serializable{

	private static final long serialVersionUID = -634948583953486824L;

	private String name;

	private String macAddress;
	
	public BluetoothDevice device;
	
	public BluetoothDevice getDevice() {
		return device;
	}

	public void setDevice(BluetoothDevice device) {
		this.device = device;
	}
	
	public Device() {
		
	}

	public Device(String name, String macAddress) {
		super();
		this.name = name;
		this.macAddress = macAddress;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getMacAddress() {
		return macAddress;
	}

	public void setMacAddress(String macAddress) {
		this.macAddress = macAddress;
	}

}
