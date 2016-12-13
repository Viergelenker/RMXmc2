package de.ccck.rmxmobile.data_management;

/**
 * SwitchBoard enthaellt alle Eigenschaften eines Schaltpultes
 * 
 * @author Kientzle Claus, Coels Corinna
 */
public class SwitchBoard {

	/**
	 * Eindeutige Bus-Bezeichnung des Schaltpultes
	 */
	private String bus;

	/**
	 * Eindeutige Nummer des Schaltpultes
	 */
	private String address;

	/**
	 * Byte, das den Zustand des Schaltpultes beschreibt
	 */
	private byte bytes;

	protected synchronized String getBus() {
		return bus;
	}

	protected synchronized void setBus(String bus) {
		this.bus = bus;
	}

	protected synchronized String getAddress() {
		return address;
	}

	protected synchronized void setAddress(String number) {
		this.address = number;
	}

	protected synchronized byte getBytesForServer() {
		byte reverseByte = 0;
		for (int i = 0; i < 8; i++)
			if ((bytes & (byte) (1 << i)) != 0)
				reverseByte += (byte) (1 << (7 - i));
		return reverseByte;
	}

	protected synchronized void setBytesFromServer(byte bytes) {
		byte reverseByte = 0;
		for (int i = 0; i < 8; i++)
			if ((bytes & (byte) (1 << i)) != 0)
				reverseByte += (byte) (1 << (7 - i));
		this.bytes = reverseByte;
	}

	protected synchronized byte getBytes() {
		return bytes;
	}

	protected synchronized void setBytes(byte bytes) {
		this.bytes = bytes;
	}
}
