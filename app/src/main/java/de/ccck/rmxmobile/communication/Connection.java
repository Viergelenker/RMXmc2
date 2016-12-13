package de.ccck.rmxmobile.communication;

import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import de.ccck.rmxmobile.data_management.DataToComInterface;

/**
 * Beherbergt den ConnectionThread, welcher sich via Zustandsverwaltung der
 * Verbindung um alle Verbindungszustaende kuemmert. Zentrale Klasse des
 * communication-Pakets.</BR></BR>
 * 
 * Zum herstellen einer Verbindung:</BR> Connection myConnection =
 * Connection.getConnection();</BR> myConnection.setConnecting();</BR></BR>
 * 
 * Zum trennen einer Verbindung: </BR>
 * myConnection.setDisconnecting();</BR></BR>
 * 
 * 
 * 
 * @author Corinna Coels
 */
public class Connection {

	// Singleton-Vorarbeiten: =======================================

	/**
	 * Verwaltet die TCP-Verbindung. Singleton-Objekt.
	 */
	private static Connection connectionInstance;

	/**
	 * Objekt fuer den ConnectionThread. Wird benutzt um den Thread zu starten,
	 * zu terminieren und bei Bedarf erneut starten zu koennen.
	 */
	private static Thread cThread = null;

	/**
	 * Konstruktor. Private. Stellt sicher das niemand das Objekt instanzieren
	 * kann, au�er der Connection-Klasse.
	 */
	private Connection() {

	}

	/**
	 * Gibt die Connection zurueck. Synchronisiert um bei parallelen Zugriffen
	 * ueber Threads Probleme zu vermeiden.
	 * 
	 * @return Connection - Singleton-Objekt
	 */
	public static synchronized Connection getConnection() {
		if (connectionInstance == null) {
			connectionInstance = new Connection();
		}
		return connectionInstance;
	}

	/**
	 * Klonen des Objects via Vererbung nicht erlaubt. Nur eine weitere
	 * Sicherheit, dass die Singleton-Eigenschaft nicht umgangen wird.
	 */
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}

	// Ende der Singleton-Vorarbeiten. ==============================
	// Beginn der restlichen Klasseneigenschaften ===================

	protected static Socket socket;
	private static InputStream input;
	/**
	 * Hilfsvariable. Wird vor jedem ausgehend verschickten Paket auf false
	 * gesetzt. Erst nach eingehender Positivquittung/sonstiger Bestaetigung
	 * wieder true. Erst bei true-Wert kann wieder ein weiteres ausgehender
	 * Befehl versandt werden.</br> Faustregel: </br> Send-Commands setzen Wert
	 * auf false. </br> Receive-Commands koennen auf true setzen (sofern
	 * Bestaetigungsnachricht). </br> Connection: Prueft den aktuellen Wert und
	 * trifft somit Entscheidungen.
	 */
	protected static boolean lastCommandFinished = false;

	// Konstanten des RMX-Protokolls
	public static final byte HEADBYTE = 0x7c;
	public static final byte RMXVERSION = 0x01;

	// Wichtige RMX-Default-Pakete bzw. statische Pakete
	public static final byte[] MSG_POSITIVEHANDSHAKE = new byte[] { HEADBYTE,
			0x04, 0x00, 0x00 };
	public static final byte[] MSG_INITIALIZE = new byte[] { HEADBYTE, 0x05,
			0x03, 0x02, RMXVERSION };
	public static final byte[] MSG_READALLTRAININFOS = new byte[] { HEADBYTE,
			0x04, 0x08, 0x01 };
	public static final byte[] MSG_READSTATUS = new byte[] { HEADBYTE, 0x03,
			0x04 };
	public static final byte[] MSG_POWERON = new byte[] { HEADBYTE, 0x04, 0x03,
			(byte) 128 };
	public static final byte[] MSG_POWEROFF = new byte[] { HEADBYTE, 0x04,
			0x03, 0x40 };
	public static final byte[] MSG_PANIC = new byte[] { HEADBYTE, 0x04, 0x03,
			0x08 };

	// Konstanten des Verbindungsstatus
	private static final int NULL = 0;
	private static final int CONNECTING = 1;
	private static final int CONNECTED = 2;
	private static final int DISCONNECTING = 3;
	private static final int DISCONNECTED = 4;

	/**
	 * Hilfsvariable zur Steuerung des Verbindungsstatus.</BR>Thread startet im
	 * nicht verbundenen Zustand, erst durch Aufruf der setConnecting()-Methode
	 * wird versucht eine Verbindung zum angegebenen Server herzustellen.
	 */
	private static int connectionStatus = DISCONNECTED;

	/**
	 * Gibt den aktuellen Verbindungszustand des Clients zurueck. Kann benutzt
	 * werden, damit externe Pakete die Konnektivitaet bzw. den Zustand abfragen
	 * und/oder beobachten koennen. </BR></BR>
	 * 
	 * Zustandswerte: </BR> 0 - NULL - Nicht verbunden, Thread ist dabei zu
	 * terminieren!</BR> 1 - CONNECTING - Verbindungsaufbau, Einrichten der
	 * Verbindung usw. </BR> 2 - CONNECTED - Verbunden, Normalbetrieb der
	 * Verbindung</BR> 3 - DISCONNECTING - Verbindungsabbau, Schliessen der
	 * Verbindung usw. </BR> 4 - DISCONNECTED - Nicht verbunden</BR>
	 * 
	 * @return int
	 */
	public static int getConnectionStatus() {
		return connectionStatus;
	}

	/**
	 * Hilfsvariable. Prueft ob der ConnectionThread terminiert werden soll.
	 */
	private static boolean askToTerminate = false;

	/**
	 * Alle aufgetretenen Exceptions und Fehler werden in der Liste abgelegt.
	 * Diese kann dann ueber den Getter von extern beobachtet werden. Wenn die
	 * Liste einen Inhalt hat, kann der Fehlerstring auf der GUI ausgegeben
	 * werden. Danach sollte die Liste ueber die clear-Methode geleert werden.
	 */
	protected static List<String> errorList = new ArrayList<String>();

	/**
	 * Gibt die Fehlerliste zurueck, welche alle bis dahin aufgetretenen
	 * Fehlermeldungen enthaelt (z.B. ConnectionTimeouts usw.)
	 * 
	 * @return Liste aus Strings
	 */
	public static List<String> getErrorList() {
		return errorList;
	}

	/**
	 * Leert die Fehlerliste. Es wird empfohlen die Liste nach dem Auslesen zu
	 * leeren.
	 */
	public static void clearErrorList() {
		errorList.clear();
	}

	/**
	 * Veranlasst den ConnectionThread die Verbindung zu initialisieren.
	 */
	public void setConnecting() {
		// Falls kein aktueller Thread besteht... neuen starten

		if (cThread == null) {
			cThread = new Thread(new ConnectionThread());

			// Exception Handler fuer potentiell nicht gefangene Exceptions:
			Thread.UncaughtExceptionHandler h = new Thread.UncaughtExceptionHandler() {
				public void uncaughtException(Thread th, Throwable ex) {
					Connection.errorList.add("Exception in ConnectionThread: "
							+ ex);
				}
			};
			cThread.setUncaughtExceptionHandler(h);
			cThread.start();
		}
		if (connectionStatus == DISCONNECTED || connectionStatus == NULL) {
			connectionStatus = CONNECTING;
			askToTerminate = false;
		}
	}

	/**
	 * Veranlasst den ConnectionThread die Verbindung zu abzubauen.
	 */
	public void setDisconnecting() {
		if (connectionStatus == CONNECTED || connectionStatus == CONNECTING) {
			connectionStatus = DISCONNECTING;
		}
	}

	/**
	 * Veranlasst den ConnectionThread die Verbindung abzubauen und danach sich
	 * selbst zu terminieren. Wird in aller Regel nur benoetigt, wenn die
	 * zugehoerige Anwendung geschlossen wird.
	 */
	public void terminateThread() {
		setDisconnecting();
		askToTerminate = true;
	}

	/**
	 * Stellt die Verbindung her. Socket-Objekt in Connections-Klasse wird
	 * dadurch initialisiert.
	 * 
	 * @param timeout
	 *            - int - Timeout fuer den Verbindungsversuch in Millisekunden.
	 *            0 steht fuer unendlich.
	 * 
	 * @throws UnknownHostException
	 * @throws IOException
	 * @throws ConnectException
	 */
	private static void initSocket(int timeout) throws UnknownHostException,
			IOException, ConnectException {
		// Verbindungsinformationen aus ConfigObject holen:
		String ipAddress = DataToComInterface.getIpAdress();
		int port = Integer.parseInt(DataToComInterface.getPort());

		// Socket mit uebergebenem Timeout-Wert initialisieren.
		socket = new Socket();
		socket.connect(new InetSocketAddress(ipAddress, port), timeout);

		// Timeout wird gesetzt, um Exception zu werfen falls
		// Verbindung bei read() warum auch immer "stecken" bleibt
		socket.setSoTimeout(10000);

		// Falls die Ausgangswarteschlange wegen einer vorherigen Verbindung
		// noch
		// Elemente enthaelt werden diese entfernt...
		Send.clearOutgoingList();

		// Server bei erfolgreichem Connect als empfangsbereit deklarieren
		lastCommandFinished = true;
	}

	/**
	 * Verbindungsthread. Steuert und verwaltet die TCP-Verbindung. Kuemmert
	 * sich um Aufbau, laufende Verbindung, Trennung der Verbindung und nicht
	 * verbundenen Zustand. Kuemmert sich um Empfang und Versand der TCP-Daten.
	 */
	private class ConnectionThread implements Runnable {

		boolean finished = false;

		int temp;
		Long start = Long.valueOf(System.currentTimeMillis());
		Long checkTime = Long.valueOf(System.currentTimeMillis());

		@Override
		public void run() {

			while (!finished) {

				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					errorList.add("While connection is sleeping: "
							+ e.getMessage());
				}

				switch (connectionStatus) {

				// ConnectionInit durchfuehren
				case CONNECTING:

					// Timer-Variable ruecksetzen, wichtig falls
					// Disconnect mit darauf folgendem Reconnect
					start = Long.valueOf(System.currentTimeMillis());
					try {
						// Socket vorbereiten
						initSocket(1000);

						connectionStatus = CONNECTED;

						// Einreihen der ersten Befehle die ausgefuehrt
						// werden muessen
						Send.sendPositiveHandshake();
						Send.sendReadAllTrainInfos();
						Send.sendInitialize();

					} catch (Exception e) {

						// Im Fehlerfall disconnecten/aufraeumen
						if (Connection.socket == null) {
							Connection.lastCommandFinished = false;
							connectionStatus = DISCONNECTED;

						} else {
							connectionStatus = DISCONNECTING;

						}

						// aufgetretene ExceptionMessage der Liste hinzufuegen
						errorList.add("While connecting: " + e.getMessage());
					}

					break;

				// Normalbetrieb der Verbindung
				case CONNECTED:

					try {
						input = Connection.socket.getInputStream();

						// Wenn Server empfangsbereit, sende naechstes
						// Element in der Warteliste
						if (lastCommandFinished) {
							Send.sendNextInList();
						}

						checkTime = Long.valueOf(System.currentTimeMillis());

						// Pruefen ob Daten im InputStream bereit liegen
						// wenn ja, alle auslesen und verarbeiten
						while (input.available() > 0) {
							start = checkTime;
							temp = input.read();
							if (temp == Connection.HEADBYTE) {
								byte[] tempByte = Receive.receive();

								Receive.processIncoming(tempByte);
							}
						}

						if ((checkTime - start) >= 10000) {
							errorList
									.add("While connected: connection timed out. Closing connection.");
							connectionStatus = DISCONNECTING;
						}
					} catch (IOException e) {
						// Im Fehlerfall disconnecten/aufraeumen
						if (Connection.socket == null) {
							Connection.lastCommandFinished = false;
							connectionStatus = DISCONNECTED;
						} else {
							connectionStatus = DISCONNECTING;
						}
						// aufgetretene ExceptionMessage der Liste hinzufuegen
						errorList.add("While connected: " + e.getMessage());
					}

					break;

				// Verbindung trennen, Cleanup usw.
				case DISCONNECTING:

					Connection.lastCommandFinished = false;
					try {
						if (Connection.socket != null) {
							Connection.socket.close();
						}
						connectionStatus = DISCONNECTED;
					} catch (IOException e) {
						// Im Fehlerfall disconnecten/aufraeumen
						connectionStatus = DISCONNECTED;

						// aufgetretene ExceptionMessage der Liste hinzufuegen
						errorList.add("While disconnecting: " + e.getMessage());
					}
					break;

				// Startzustand, Verbindungsversuch kann via
				// setConnecting durchgefuehrt werden
				case DISCONNECTED:

					// Pruefen ob der Thread terminiert werden soll
					if (askToTerminate == true) {
						connectionStatus = NULL;
					}
					break;
				// Endzustand, nach Abschluss aller CleanUp-Taetigkeiten,
				// kann der Thread terminiert werden. While-Schleife wird
				// verlassen.
				case NULL:
					finished = true;
					// Thread-Objekt null setzen, damit bei Bedarf neuer Thread
					// gestartet werden kann.
					cThread = null;
					break;
				}
			}
		}
	}
}