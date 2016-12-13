package de.ccck.rmxmobile.data_management;

/**
 * TrainObject enthaellt alle Eigenschaften eines Zuges
 * 
 * @author Kientzle Claus, Coels Corinna
 */
public class TrainObject {

	/**
	 * Eindeutige Zugnummer. Entspricht der Lok-Nr. bzw. AdrLing. Realisation
	 * via Hash um individuelle Kennzeichnung sicher zu stellen?
	 */
	private int trainNumber = 0;

	/**
	 * Fuer einige Decodertypen benoetigt. Wert zwischen trainNumber und
	 * adrShort oft gleich, aber MANCHMAL eben auch abweichend!
	 */
	private int adrShort = 0;

	/**
	 * Opmode des Zuges. Bestimmt Details wie Decodertyp, Adressierung, Anzahl
	 * der Fahrstufen.
	 */
	private byte opmode = 0;

	/**
	 * Der RMX-Kanal auf welchem das TrainObject angesprochen wird. Vermutlich
	 * nur rein informativ?
	 */
	private byte rmxChannel = 0;

	/**
	 * Name des Zuges
	 */
	private String trainName = "";

	/**
	 * Funktion 0-7
	 */
	private byte modeF0F7 = 0;

	/**
	 * Funktion 8-15
	 */
	private byte modeF8F15 = 0;

	/**
	 * Funktion 8-15
	 */
	private byte modeF16F23 = 0;

	/**
	 * Fahrtrichtung
	 */
	private byte direction = 0;

	/**
	 * Aktuell gefahrene Geschwindigkeit des Zuges.
	 */
	private int runningNotch = 0;

	/**
	 * Maximale Anzahl der Fahrstufen des Zuges.
	 */
	private int maxRunningNotch = 0;

	protected synchronized int getMaxRunningNotch() {
		return maxRunningNotch;
	}

	protected synchronized void setMaxRunningNotch(int maxRunningNotch) {
		this.maxRunningNotch = maxRunningNotch;
	}

	// weitere Variablen/Infos die wir noch nicht geklaert haben...
	protected synchronized int getTrainNumber() {
		return trainNumber;
	}

	protected synchronized void setTrainNumber(int trainNumber) {
		this.trainNumber = trainNumber;
	}

	protected synchronized byte getRmxChannel() {
		return rmxChannel;
	}

	protected synchronized void setRmxChannel(byte rmxChannel) {
		this.rmxChannel = rmxChannel;
	}

	protected synchronized String getTrainName() {
		return trainName;
	}

	protected synchronized void setTrainName(String trainName) {
		this.trainName = trainName;
	}

	protected synchronized byte getModeF0F7() {
		return modeF0F7;
	}

	protected synchronized void setModeF0F7(byte modeF0F7) {
		this.modeF0F7 = modeF0F7;
	}

	protected synchronized byte getModeF8F15() {
		return modeF8F15;
	}

	protected synchronized void setModeF8F15(byte modeF8F15) {
		this.modeF8F15 = modeF8F15;
	}

	protected synchronized byte getModeF16F23() {
		return modeF16F23;
	}

	protected synchronized void setModeF16F23(byte modeF16F23) {
		this.modeF16F23 = modeF16F23;
	}

	protected synchronized byte getDirection() {
		return direction;
	}

	protected synchronized void setDirection(byte direction) {
		this.direction = direction;
	}

	protected synchronized int getRunningNotch() {
		return runningNotch;
	}

	protected synchronized void setRunningNotch(int runningNotch) {
		this.runningNotch = runningNotch;
	}

	protected synchronized int getAdrShort() {
		return adrShort;
	}

	protected synchronized void setAdrShort(int adrShort) {
		this.adrShort = adrShort;
	}

	protected synchronized byte getOpmode() {
		return opmode;
	}

	protected synchronized void setOpmode(byte opmode) {
		this.opmode = opmode;
	}
}
