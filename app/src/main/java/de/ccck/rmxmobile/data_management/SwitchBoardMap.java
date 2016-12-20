package de.ccck.rmxmobile.data_management;

import java.util.HashMap;

/**
 * TrainDepotMap enthaellt alle Methoden zur Verwaltung aller Schaltpultobjekte.
 * 
 * @author Kientzle Claus, Coels Corinna
 */
public class SwitchBoardMap {

	private static SwitchBoardMap instance = null;

	/**
	 * Hashmap zur Speicherung aller SwitchBoards.
	 * Der Key der Hashmap setzt sich zusammen aus dem Buskanal ("RMX 1" etc)
	 * einem Trennzeichen "|" und der Adresse (0 - 111).
	 */
	private HashMap<String, SwitchBoard> SwitchBoardMap = new HashMap<String, SwitchBoard>();
	private String[] BusContainer = { "RMX 1", "VSX 2", "VSX 3", "VSX 4", "VSX 5", "VSX 6", "VSX 7", "VSX 8" };

	private SwitchBoardMap() {
	}

	public static synchronized SwitchBoardMap getSwitchBoardMap() {
		if (instance == null) {
			instance = new SwitchBoardMap();
		}
		return instance;
	}

	/**
	 * F�gt ein Switchboard zur SwitchboardMap hinzu.
	 * @param bus - Buskanal
	 * @param number - Adresse
	 * @param bytes - Byte: Stellung der Adresse
	 */
	private synchronized void addSwitchBoard(String bus, String number, byte bytes) {
		SwitchBoard switchBoardObject = new SwitchBoard();
		switchBoardObject.setBus(bus);
		switchBoardObject.setAddress(number);
		switchBoardObject.setBytes(bytes);
		String key = bus + "|" + number;
		SwitchBoardMap.put(key, switchBoardObject);
	}

	/**
	 * Gibt das Switchboard mit dem ausgew�hlten Bus-Kanal zur�ck
	 * @param key - z.B. ("RMX 1|110")
	 * @return
	 */
	protected synchronized SwitchBoard getSwitchBoardEntry(String key) {
		return SwitchBoardMap.get(key);
	}

	/**
	 * Erstellt alle Switchboards f�r alle Buskan�le und deren
	 * 112 Adressen.
	 */
	protected synchronized void generateMap() {
		for (int i = 0; i < BusContainer.length; i++) {
			for (int j = 0; j < 112; j++) {
				this.addSwitchBoard(BusContainer[i], Integer.toString(j), (byte) 0x00);
			}
		}
	}

	/**
	 * Gibt ein Array aller Buskan�le (RMX 1, etc...) zur�ck.
	 * @return StringArray aller Buskan�le
	 */
	public synchronized String[] getBusContainer() {
		return BusContainer;
	}
}
