package de.ccck.rmxmobile.data_management;

/**
 * ConfigObject enthaellt alle Eigenschaften einer Konfiguration.
 * 
 * @author Kientzle Claus, Coels Corinna
 */
public class ConfigObject {

	// Singleton-Vorarbeiten:

	/**
	 * Speichert die Systemeinstellungen wie IP-Adresse und Port.
	 * Singleton-Objekt.
	 */
	private static ConfigObject configuration;

	/**
	 * Konstruktor. Private. Stellt sicher das niemand das Objekt instanzieren
	 * kann, au�er der configObject-Klasse.
	 */
	private ConfigObject() {

	}

	/**
	 * Gibt das configObject zurueck. Synchronisiert um bei parallelen Zugriffen
	 * ueber Threads Probleme zu vermeiden.
	 * 
	 * @return
	 */
	protected static synchronized ConfigObject getConfigObject() {
		if (configuration == null) {
			configuration = new ConfigObject();
		}
		return configuration;
	}

	/**
	 * Klonen des Objects via Vererbung nicht erlaubt. Nur eine weitere
	 * Sicherheit, dass die Singleton-Eigenschaft nicht umgangen wird.
	 */
	protected Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}

	// Ende der Singleton-Vorarbeiten. Beginn der restlichen
	// Klasseneigenschaften
	private String accountname = "";

	/**
	 * Systemeinstellungen. IP-Adresse.
	 */
	private String ipAdress = "";

	/**
	 * Systemeinstellungen. Port.
	 */
	private String port = "";

	private int theme;

	// weitere Variablen/Infos die wir noch nicht geklaert haben...
	protected synchronized String getAccountName() {
		return accountname;
	}

	protected synchronized void setAccountName(String accountname) {
		this.accountname = accountname;
	}

	protected synchronized String getIpAdress() {
		return ipAdress;
	}

	protected synchronized void setIpAdress(String ipAdress) {
		this.ipAdress = ipAdress;
	}

	protected synchronized String getPort() {
		return port;
	}

	protected synchronized void setPort(String port) {
		this.port = port;
	}

	protected synchronized int getTheme() {
		return theme;
	}

	protected synchronized void setTheme(int theme) {
		this.theme = theme;
	}
}
