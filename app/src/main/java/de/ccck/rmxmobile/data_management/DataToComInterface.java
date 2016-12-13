package de.ccck.rmxmobile.data_management;

import de.tbjv.rmxmc2.Interface;

/**
 * Bietet eine Schnittstelle fuer das Kommunikationspaket um die Datenressourcen
 * des Clients zu veraendern und eine Veraenderung der grafischen Oberflaeche zu
 * initialisieren
 */
public class DataToComInterface {

	/**
	 * Gibt die IP der aktuell ausgewaehlten Konfiguration zurueck.
	 * 
	 * @return String - aktuelle IP-Adresse
	 */
	public static synchronized String getIpAdress() {
		return ConfigObject.getConfigObject().getIpAdress();
	}

	/**
	 * Gibt den Port der aktuell ausgewaehlten Konfiguration zurueck.
	 * 
	 * @return String - aktueller Port der Addresse
	 */
	public static synchronized String getPort() {
		return ConfigObject.getConfigObject().getPort();
	}

	/**
	 * Aktualisiert das Verbindungsstatus-Label
	 * 
	 * @param connectionStatus
	 *            - int - 1 = "getrennt" / 2 = "verbunden"
	 */
	public static synchronized void changeConnectionStatus(int connectionStatus) {
		// Interface.changeConnectionStatus(connectionStatus);
	}

	/**
	 * Aktualisiert das Energie-Label
	 * 
	 * @param energyOn
	 *            - boolean - true = "Energie an" / false = "Energie aus"
	 */
	public static synchronized void changeEnergyStatus(boolean energyOn) {
		// GuiInterface.changeEnergyStatus(energyOn);
	}

	/**
	 * Legt einen neuen Zug in der Hashmap an.
	 * 
	 * @param trainNumber
	 *            - int - entspricht der Lok-Nr. bzw. der adrLong
	 * @param adrShort
	 *            - int
	 * @param opmode
	 *            - byte - Fuer einige Decodertypen benoetigt. Wert zwischen
	 *            trainNumber und adrShort oft gleich, aber MANCHMAL eben auch
	 *            abweichend!
	 * @param rmxChannel
	 *            - byte - Der RMX-Kanal auf welchem das TrainObject
	 *            angesprochen wird.
	 * @param trainName
	 *            - String - Name des Zuges
	 * @param modeF0F7
	 *            - byte - Funktionsbits 0 bis 7
	 * @param modeF8F15
	 *            - byte - Funktionsbits 0 bis 7
	 * @param modeF16F23
	 *            - byte - Funktionsbits 0 bis 7
	 * @param direction
	 *            - byte - Aktuelle Fahrtrichtung des Zuges
	 * @param maxRunningNotch
	 *            - int - Anzahl der Fahrstufen
	 */
	public static synchronized void addTrain(int trainNumber, int adrShort,
			byte opmode, byte rmxChannel, String trainName, byte modeF0F7,
			byte modeF8F15, byte modeF16F23, byte direction, int maxRunningNotch) {
		TrainDepotMap.getTrainDepot().addTrain(trainNumber, adrShort, opmode,
				rmxChannel, trainName, modeF0F7, modeF8F15, modeF16F23,
				direction, maxRunningNotch);
		// GuiInterface.updateTrainList();
	}

	/**
	 * Loescht den Zug mit der uebergebenen Zugnummer aus der Hashmap.
	 * 
	 * @param trainNumber
	 *            - int - Zugnummer
	 */
	public static synchronized void deleteTrain(int trainNumber) {
		TrainDepotMap.getTrainDepot().removeTrain(trainNumber);
		// GuiInterface.updateTrainList();
	}

	/**
	 * Loescht alle Zuege aus der Hashmap.
	 */
	public static synchronized void deleteAllTrains() {
		TrainDepotMap.getTrainDepot().clearTrain();
		// GuiInterface.updateTrainList();
	}

	/**
	 * Aktualisiert den RMX-Kanal fuer den uebergebenen Zug.
	 * 
	 * @param trainNumber
	 *            - int - Die Lok-Nr. Identifier.
	 * @param rmx
	 *            - byte - der Wert fuer den RMX-Kanal, welcher gespeichert
	 *            werden soll
	 */
	public static synchronized void setTrainRmx(int trainNumber, byte rmx) {
		TrainDepotMap.getTrainDepot().getTrainMapEntry(trainNumber)
				.setRmxChannel(rmx);
	}

	/**
	 * Setzt die aktuelle Fahrstufe/Geschwindigkeit des angegebenen Zuges.
	 * 
	 * @param trainNumber
	 *            - int - Die Lok-Nr. Identifier.
	 * @param Speed
	 *            - int - Die aktuell gefahrene Geschwindigkeit.
	 */
	public static synchronized void setTrainSpeed(int trainNumber, int Speed) {
		TrainDepotMap.getTrainDepot().getTrainMapEntry(trainNumber)
				.setRunningNotch(Speed);
		// GuiInterface.setTrainSpeed(trainNumber);
	}

	/**
	 * Fahrtrichtung des Zuges setzen.
	 * 
	 * @param trainNumber
	 *            - int - Die Lok-Nr. Identifier.
	 */
	public static synchronized void setTrainDirection(int trainNumber,
			byte direction) {
		TrainDepotMap.getTrainDepot().getTrainMapEntry(trainNumber)
				.setDirection(direction);
		// GuiInterface.setTrainDirection(trainNumber);
	}

	/**
	 * Zugfunktionen Licht bis F7 setzen.
	 * 
	 * @param trainNumber
	 *            - int - Die Lok-Nr. Identifier.
	 */
	public static synchronized void setTrainMode0to7(int trainNumber,
			byte modeF0toF7) {
		TrainDepotMap.getTrainDepot().getTrainMapEntry(trainNumber)
				.setModeF0F7(modeF0toF7);
		// GuiInterface.setTrainMode0to7(trainNumber);
	}

	/**
	 * Zugfunktionen F8 bis F15 setzen.
	 * 
	 * @param trainNumber
	 *            - int - Die Lok-Nr. Identifier.
	 */
	public static synchronized void setTrainMode8to15(int trainNumber,
			byte modeF8toF15) {
		TrainDepotMap.getTrainDepot().getTrainMapEntry(trainNumber)
				.setModeF8F15(modeF8toF15);
		// GuiInterface.setTrainMode8to15(trainNumber);
	}

	/**
	 * Zugfunktion F16 setzen.
	 * 
	 * @param trainNumber
	 *            - int - Die Lok-Nr. Identifier.
	 */
	public static synchronized void setTrainMode16to23(int trainNumber,
			byte modeF16toF23) {
		TrainDepotMap.getTrainDepot().getTrainMapEntry(trainNumber)
				.setModeF16F23(modeF16toF23);
		// GuiInterface.setTrainMode16to23(trainNumber);
	}

	/**
	 * Aktualisiert den angegebenen Bus mit den uebermittelten Inhalten.
	 * Aktualisiert auch den Status aller betroffenen TrackLayoutElemente.
	 * 
	 * @param rmx
	 *            - byte - RMX-Bus
	 * @param address
	 *            - byte - RMX-Adresse
	 * @param value
	 *            - byte - Der neue Wert der Adresse
	 */
	public static synchronized void updateBusAdress(byte rmx, byte address,
			byte value) {
		String rmxString = SwitchBoardMap.getSwitchBoardMap().getBusContainer()[(rmx & 0xFF) - 1];
		String addressString = Integer.toString(address & 0xFF);
		String key = rmxString + "|" + addressString;
		SwitchBoardMap.getSwitchBoardMap().getSwitchBoardEntry(key)
				.setBytesFromServer(value);
		// aktualisiert die Daten der TrackLayoutElemente
		// TrackLayoutMap.getTrackLayoutMap().updateTrackLayoutElements(
		//		rmxString, addressString, value);
		
		// GuiInterface.updateTrackLayoutElements(rmx, addressString);
		// C&K Version
		// GuiInterface.updateBusAdress(rmx, key);
	}
}
