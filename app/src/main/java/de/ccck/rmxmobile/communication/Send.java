package de.ccck.rmxmobile.communication;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Bietet eine Schnittstelle fuer andere Pakete an um Daten an den RMX-Server zu
 * schicken. Beinhaltet ausserdem die Warteliste der Daten, welche an den
 * PC-Server verschickt werden sollen.
 * 
 * @author Corinna Coels
 */
public class Send {

	/**
	 * Liste mit allen ausgehenden Versandauftraegen. Hier alle byte[]-Arrays
	 * anhaengen die an den PC-Server verschickt werden sollen.
	 */
	private static List<byte[]> outgoingList = new ArrayList<byte[]>();

	/**
	 * Getter fuer die Liste mit allen ausgehenden Versandauftraegen. Hier alle
	 * byte[]-Arrays anhaengen die an den PC-Server verschickt werden sollen.
	 * 
	 * @return Liste aus byte[]
	 */
	protected static List<byte[]> getOutgoingList() {
		return outgoingList;
	}

	/**
	 * Leert die outgoingList. Wird z.B. bei einem Disconnect und Reconnect
	 * gemacht, um sicher zu stellen, dass keine Altauftraege verschickt werden.
	 */
	protected static void clearOutgoingList() {
		outgoingList.clear();
	}

	/**
	 * Versendet ein DatenArray an den angegebenen Zielsocket. Setzt ausserdem
	 * die lastCommandFinished der Connection-Klasse auf false.
	 * 
	 * @param message
	 *            byte-Array welches die Daten enthaelt
	 * @throws IOException
	 */
	private static void sendByte(byte[] message) throws IOException {
		DataOutputStream outToServer = new DataOutputStream(
				Connection.socket.getOutputStream());
		// Bool-Variable direkt vor Versand auf false setzen
		Connection.lastCommandFinished = false;
		outToServer.write(message);
	}

	/**
	 * Nur der naechste Befehl der Warteschlange wird versandt. Diese Methode
	 * Sollte eigentlich in fast allen Faellen vewendet werden, da im Normalfall
	 * nach jedem Befehl auf Server-Rueckmeldung zu warten ist und deswegen
	 * nicht die ganze Liste oder mehrere Befehle auf einmal versandt werden
	 * sollten!
	 * 
	 * @throws IOException
	 */
	protected static void sendNextInList() throws IOException {
		if (outgoingList != null) {
			if (outgoingList.size() > 0) {

				byte[] outMessage;
				outMessage = outgoingList.remove(0);
				sendByte(outMessage);
				// Geschwindigkeitspakete werden alle verschickt,
				// damit Fluessigkeit bzw Live-Feeling erhalten bleibt
				if (outMessage[2] == 0x24) {
					sendNextInList();
				}
			}
		}
	}

	/**
	 * Kann benutzt werden um eine Custom-Nachricht an den Server zu versenden,
	 * welche ueber die anderen angebotenen Methoden nicht abgedeckt wird.
	 * Sollte im Normalfall nicht benoetigt werden!
	 * 
	 * @param message
	 */
	public static void addSendJob(byte[] message) {
		outgoingList.add(message);
	}

	/**
	 * Sendet eine Nachricht an den RMX-Server mit folgendem Befehlsinhalt:</BR>
	 * NOTHALT</BR> OPCODE: 0x03</BR> Nothalt wird an erste Stelle der
	 * Warteliste gesetzt und somit vor allen anderen potentiell existierenden
	 * Befehlen in der Liste priorisiert!</BR> Damit eventuell darauf folgende
	 * Geschwindigkeitsaenderungen den Nothalt nicht wieder "ueberschreiben"
	 * bzw. unnuetz machen, werden alle OPCODE 0x24 Auftraege aus der
	 * Warteschlange entfernt.
	 */
	public static void sendPanic() {

		// Pruefen ob 0x24-Jobs vorliegen
		Iterator<byte[]> it = outgoingList.iterator();
		// durch iterieren und 0x24-Jobs entfernen
		while (it.hasNext()) {
			byte[] current = it.next();
			if (current[2] == 0x24) {
				it.remove();
			}
		}
		// Nothalt-Signal ASAP senden
		outgoingList.add(0, Connection.MSG_PANIC);
	}

	/**
	 * Sendet eine Nachricht an den RMX-Server mit folgendem Befehlsinhalt:</BR>
	 * Fahrstrom EIN</BR> OPCODE: 0x03
	 */
	public static void sendPowerOn() {
		outgoingList.add(Connection.MSG_POWERON);
	}

	/**
	 * Sendet eine Nachricht an den RMX-Server mit folgendem Befehlsinhalt:</BR>
	 * Fahrstrom AUS</BR> OPCODE: 0x03
	 */
	public static void sendPowerOff() {
		outgoingList.add(Connection.MSG_POWEROFF);
	}

	/**
	 * Sendet eine Nachricht an den RMX-Server mit folgendem Befehlsinhalt:</BR>
	 * Auslesen der Lok-DB aus der PC-Zentrale</BR> OPCODE: 0x08
	 */
	public static void sendReadAllTrainInfos() {
		outgoingList.add(Connection.MSG_READALLTRAININFOS);
	}

	/**
	 * Sendet eine Nachricht an den RMX-Server mit folgendem Befehlsinhalt:</BR>
	 * Initialisierung der Verbindung</BR> OPCODE: 0x03
	 */
	public static void sendInitialize() {
		outgoingList.add(Connection.MSG_INITIALIZE);
	}

	/**
	 * Sendet eine Nachricht an den RMX-Server mit folgendem Befehlsinhalt:</BR>
	 * Anfrage einer Positivquittung</BR> OPCODE: 0x00
	 */
	public static void sendPositiveHandshake() {
		outgoingList.add(Connection.MSG_POSITIVEHANDSHAKE);
	}

	/**
	 * Sendet eine Nachricht an den RMX-Server mit folgendem Befehlsinhalt:</BR>
	 * Statusabfrage</BR> OPCODE: 0x04</BR> Da der PC-Server in der aktuellen
	 * Version automatisch eine Statusinformation an den Client sendet, sollte
	 * es eigentlich nicht noetig sein eine manuelle Statusabfrage anzustossen.
	 * Das Paket bietet diese Funktion jedoch der Vollstaendigkeit halber an, da
	 * das RMX-Protokoll diese Moeglichkeit vorsieht.
	 */
	public static void sendReadStatus() {
		outgoingList.add(Connection.MSG_READSTATUS);
	}

	/**
	 * Sendet eine Nachricht an den RMX-Server mit folgendem Befehlsinhalt:</BR>
	 * Wert in RMX1-Adresse schreiben</BR> OPCODE: 0x05
	 * 
	 * @param address
	 *            - byte - Die Adresse, in welche geschrieben werden soll
	 * @param value
	 *            - byte - Der Wert, welcher geschrieben werden soll
	 */
	public static void send0x05(byte address, byte value) {
		// Beispiel: Bus1, Adresse $10, Wert $aa:
		// <0x7c><0x06><0x05><0x01><0x10> <0xaa>
		// [Bus] [Adresse][Wert]

		outgoingList.add(new byte[] { Connection.HEADBYTE, 0x06, 0x05, 0x01,
				address, value });
	}

	/**
	 * Sendet eine Nachricht an den RMX-Server mit folgendem Befehlsinhalt:</BR>
	 * Wert in RMX-Adresse lesen</BR> OPCODE: 0x06
	 * 
	 * @param rmx
	 *            - int - Der RMX-Bus auf dem gelesen werden soll
	 * @param address
	 *            - byte - Die Adresse aus welcher gelesen werden soll
	 */
	public static void send0x06(int rmx, byte address) {
		// Beispiel: Bus1, Adresse $10, Wert $aa:
		// <0x7c><0x05><0x06><0x01><0x10>
		// [Bus] [Adresse]

		outgoingList.add(new byte[] { Connection.HEADBYTE, 0x05, 0x06,
				(byte) rmx, address });
	}

	/**
	 * Sendet eine Nachricht an den RMX-Server mit folgendem Befehlsinhalt:</BR>
	 * Eingabe Lokinformation in Zentrale</BR> OPCODE: 0x08</BR></BR>
	 * 
	 * Die Funktion der Lokeingabe wird wohl nicht in der App unterstuetzt. Das
	 * Paket bietet diese Funktion jedoch der Vollstaendigkeit halber an, da das
	 * RMX-Protokoll diese Moeglichkeit vorsieht. </BR></BR>
	 * 
	 * Spezielle OPMODES: (andere siehe Spezifikation)</BR> 31 - L�schen einer
	 * Lok �ber Loknummer: Angabe der alphanumerische Zeichen nicht erlaubt.
	 * <COUNT> = 7. Wird beim L�schen die Loknummer 0000 eingegeben, wird die
	 * gesamte Datenbank gel�scht.</BR> 32 - Lesen eines Datensatzes �ber
	 * Loknummer: Angabe der alphanumerische Zeichen nicht erlaubt. <COUNT> =
	 * 7.</BR>
	 * 
	 * @param trainNumber
	 *            - int
	 * @param opmode
	 *            - int - Vom Server akzeptierte Werte: 2, 3, 5, 6, 7, 9, 10,
	 *            12, 13, 15, 16, 17, 31, 32
	 * @param name
	 *            - String - Zu lange Namen, die vom Server nicht angenommen
	 *            werden, werden abgeschnitten. </BR>Soll kein Name an den
	 *            Server uebermittelt werden, einfach name = "" uebergeben.
	 * */
	public static void send0x08(int trainNumber, int opmode, String name) {
		// <0x7c><COUNT><0x08><ADRH><ADRL><ADRK><OPMODE><n*AN>
		// [0] [1] [2] [3] [4] [5] [6] [7-26]

		// zweiter Array-Teil, Name der Lok, <n*AN>
		// temp als Hilfsvariable, falls der angegebene Name 20 Bytes
		// ueberschreitet
		byte[] temp = CommunicationUtils.stringToBytes(name);
		byte[] second;
		if (temp.length > 20) {
			second = Arrays.copyOf(CommunicationUtils.stringToBytes(name), 20);
		} else {
			second = Arrays.copyOf(temp, temp.length);
		}

		// erster Array-Teil: (wird wegen der second.length erst als zweites
		// bearbeitet)
		byte[] first = new byte[]
		// <0x7c> <COUNT>
		{ Connection.HEADBYTE, (byte) (7 + second.length),
				// <0x08><ADRH><ADRL><ADRK><OPMODE>
				0x08, 0x00, 0x00, 0x00, (byte) opmode };

		// Byte korrekter Laenge und Inhalt bilden.
		byte[] byteToSend = CommunicationUtils.concatByteArrays(first, second);

		// Loknummer konvertieren
		byte[] trainAddress = CommunicationUtils.intToBytes(trainNumber);

		// <0x7c><COUNT><0x08><ADRH><ADRL><ADRK><OPMODE><n*AN>
		// [0] [1] [2] [3] [4] [5] [6] [8-26]
		switch (opmode) {
		case 2: // Hex: 0x02
			// SX1: kurze Adresse, Steuerung �ber Loknummer

			// Loknummer schreiben
			byteToSend[3] = trainAddress[0];
			byteToSend[4] = trainAddress[1];
			byteToSend[5] = trainAddress[1];
			outgoingList.add(byteToSend);
			break;
		case 3: // Hex: 0x03
			// SX1: Adressdynamik, lange Adresse

			// Loknummer schreiben
			byteToSend[3] = trainAddress[0];
			byteToSend[4] = trainAddress[1];
			outgoingList.add(byteToSend);
			break;
		case 5: // Hex: 0x05
			// SX1: kurze Adresse, Steuerung �ber Loknummer mit
			// Zusatzfunktionsadresse (+1)

			// Loknummer schreiben
			byteToSend[3] = trainAddress[0];
			byteToSend[4] = trainAddress[1];
			byteToSend[5] = trainAddress[1];
			outgoingList.add(byteToSend);
			break;
		case 6: // Hex: 0x06
			// SX1: Adressdynamik mit Zusatzfunktionsadresse

			// Loknummer schreiben
			byteToSend[3] = trainAddress[0];
			byteToSend[4] = trainAddress[1];
			outgoingList.add(byteToSend);
			break;
		case 7: // Hex: 0x07
			// SX2: 127 Fahrstufen, lange Adresse

			// Loknummer schreiben
			byteToSend[3] = trainAddress[0];
			byteToSend[4] = trainAddress[1];
			outgoingList.add(byteToSend);
			break;
		case 9: // Hex: 0x09
			// DCC: 14 Fahrstufen, kurze Adresse, Steuerung �ber Loknummer

			// Loknummer schreiben
			byteToSend[3] = trainAddress[0];
			byteToSend[4] = trainAddress[1];
			byteToSend[5] = trainAddress[1];
			outgoingList.add(byteToSend);
			break;
		case 10: // Hex: 0x0a
			// DCC: 14 Fahrstufen, lange Adresse

			// Loknummer schreiben
			byteToSend[3] = trainAddress[0];
			byteToSend[4] = trainAddress[1];
			outgoingList.add(byteToSend);
			break;
		case 12: // Hex: 0x0c
			// DCC: 28 Fahrstufen, kurze Adresse, Steuerung �ber Loknummer

			// Loknummer schreiben
			byteToSend[3] = trainAddress[0];
			byteToSend[4] = trainAddress[1];
			byteToSend[5] = trainAddress[1];
			outgoingList.add(byteToSend);
			break;
		case 13: // Hex: 0x0d
			// DCC: 28 Fahrstufen, lange Adresse

			// Loknummer schreiben
			byteToSend[3] = trainAddress[0];
			byteToSend[4] = trainAddress[1];
			outgoingList.add(byteToSend);
			break;
		case 15: // Hex: 0x0f
			// DCC: 126 Fahrstufen, kurze Adresse, Steuerung �ber Loknummer

			// Loknummer schreiben
			byteToSend[3] = trainAddress[0];
			byteToSend[4] = trainAddress[1];
			byteToSend[5] = trainAddress[1];
			outgoingList.add(byteToSend);
			break;
		case 16: // Hex: 0x10
			// DCC: 126 Fahrstufen, lange Adresse

			// Loknummer schreiben
			byteToSend[3] = trainAddress[0];
			byteToSend[4] = trainAddress[1];
			outgoingList.add(byteToSend);
			break;
		case 17: // Hex: 0x11
			// SX2: 31 Fahrstufen, lange Adresse

			// Loknummer schreiben
			byteToSend[3] = trainAddress[0];
			byteToSend[4] = trainAddress[1];
			outgoingList.add(byteToSend);
			break;
		case 31: // Hex: 0x1f
			/*
			 * L�schen einer Lok �ber Loknummer: Angabe der alphanumerische
			 * Zeichen nicht erlaubt. <COUNT> = 7. Wird beim L�schen die
			 * Loknummer 0000 eingegeben, wird die gesamte Datenbank gel�scht.
			 */
			// Loknummer schreiben
			first[3] = trainAddress[0];
			first[4] = trainAddress[1];

			first[1] = 7;
			outgoingList.add(first);
			break;
		case 32: // Hex: 0x20
			/*
			 * Lesen eines Datensatzes �ber Loknummer: Angabe der
			 * alphanumerische Zeichen nicht erlaubt. <COUNT> = 7.
			 */
			first[3] = trainAddress[0];
			first[4] = trainAddress[1];

			first[1] = 7;
			outgoingList.add(first);
			break;
		}
	}

	/**
	 * Sendet eine Nachricht an den RMX-Server mit folgendem Befehlsinhalt:</BR>
	 * Loksteuerung Geschwindigkeit</BR> OPCODE: 0x24
	 * 
	 * @param trainNumber
	 *            - int - Nummer der Lok, welche gesteuert werden soll
	 * @param runningNotch
	 *            - int - Die Fahrstufe, welche eingestellt werden soll
	 * @param direction
	 *            - byte - Die Richtung, in welche sich die Lok bewegen
	 *            soll.</BR> Bit 0 = 0: Fahrtrichtung vorw�rts</BR> Bit 0 = 1:
	 *            Fahrtrichtung r�ckw�rts
	 * */
	public static void send0x24(int trainNumber, int runningNotch,
			byte direction) {
		// <0x7c><0x07><0x24><ADRH><ADRL><SPEED><DIR>
		// [0] [1] [2] [3] [4] [5] [6]

		// Rumpf formen
		byte[] byteToSend = new byte[] { Connection.HEADBYTE, 0x07, 0x24, 0x00,
				0x00, (byte) runningNotch, direction };

		// Loknummer konvertieren
		byte[] trainAddress = CommunicationUtils.intToBytes(trainNumber);

		// Loknummer schreiben
		byteToSend[3] = trainAddress[0];
		byteToSend[4] = trainAddress[1];

		// Befehl in Queue einreihen
		outgoingList.add(byteToSend);
	}

	/**
	 * Sendet eine Nachricht an den RMX-Server mit folgendem Befehlsinhalt:</BR>
	 * Loksteuerung Funktionen</BR> OPCODE: 0x28</BR> Pro Funktionsbyte:</BR>
	 * Bit nicht gesetzt (0): aus</BR> Bit gesetzt (1): ein</BR>
	 * 
	 * @param trainNumber
	 *            - int - Nummer der Lok, welche gesteuert werden soll
	 * @param modeF0F7
	 *            - byte - Funktionen 0 (Licht) bis 7
	 * @param modeF8F15
	 *            - byte - Funktionen 8 bis 15
	 * @param modeF16F23
	 *            - byte - Funktionen 16 bis 23
	 */
	public static void send0x28(int trainNumber, byte modeF0F7, byte modeF8F15,
			byte modeF16F23) {
		// <0x7c><0x08><0x28><ADRH><ADRL><F0F7><F8F15><F16F23>
		// [0] [1] [2] [3] [4] [5] [6] [7]

		// Rumpf formen
		byte[] byteToSend = new byte[] { Connection.HEADBYTE, 0x08, 0x28, 0x00,
				0x00, modeF0F7, modeF8F15, modeF16F23 };

		// Loknummer konvertieren
		byte[] trainAddress = CommunicationUtils.intToBytes(trainNumber);

		// Loknummer schreiben
		byteToSend[3] = trainAddress[0];
		byteToSend[4] = trainAddress[1];

		// Befehl in Queue einreihen
		outgoingList.add(byteToSend);
	}

	/**
	 * Sendet eine Nachricht an den RMX-Server mit folgendem Befehlsinhalt:</BR>
	 * Loksteuerung Funktionen</BR> OPCODE: 0x28</BR>
	 * 
	 * Die Funktion der Decoder-Programmierung wird wohl nicht in der App
	 * unterstuetzt. Das Paket bietet diese Funktion jedoch der Vollstaendigkeit
	 * halber an, da das RMX-Protokoll diese Moeglichkeit vorsieht.
	 * 
	 * @param adrrmx
	 *            - int</BR>Aktiver RMX-Kanal, nur bei Hauptgleisprogrammierung,
	 *            sonst 0x00
	 * @param cvh
	 *            - byte</BR>Bit 8 bis Bit 13 der CV bei DCC bzw. der
	 *            Parameternummer bei SX2</BR> Bit 0 bis Bit 6: Decoderadresse 1
	 *            bis 103; Bit7 Halteabschnitte bei SX1
	 * @param cvl
	 *            - byte</BR>Bit 0 bis Bit 7 der CV bei DCC bzw. der
	 *            Parameternummer bei SX2</BR> Bit 0 bis Bit 2:
	 *            H�chstgeschwindigkeit 1 bis 7</BR>Bit 3 bis Bit 5:
	 *            Brems-/Beschleunigungsverhalten 1 bis 7</BR>Bit 6 und Bit 7:
	 *            Impulsbreite 0 bis 3 bei SX1
	 * @param mode
	 *            - byte</BR> 00: Lesen SX1 </BR> 04: Lesen SX2</BR> 08: Lesen
	 *            DCC</BR> 02: Schreiben SX1</BR> 06: Schreiben SX2</BR> 0a:
	 *            Schreiben DCC</BR> Bit 0 = 0: Programmierung auf dem
	 *            Programmiergleis</BR> Bit 0 = 1: Programmierung auf dem
	 *            Hauptgleis</BR>
	 * @param value
	 *            - byte</BR> CV- oder Parameterwert (bei SX1 0x00)
	 */
	public static void send0xc0(int adrrmx, byte cvh, byte cvl, byte mode,
			byte value) {
		// <0x7c><0x08><0xc0><ADRRMX><CVH><CVL><MODE><VALUE>
		// [0] [1] [2] [3] [4] [5] [6] [7]

		// Rumpf formen
		byte[] byteToSend = new byte[] { Connection.HEADBYTE, 0x08, (byte) 192,
				(byte) adrrmx, cvh, cvl, mode, value };

		// Befehl in Queue einreihen
		outgoingList.add(byteToSend);
	}
}