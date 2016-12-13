package de.ccck.rmxmobile.communication;

import java.io.IOException;
import java.io.InputStream;

import de.ccck.rmxmobile.data_management.DataToComInterface;

/**
 * Bietet eine Schnittstelle fuer andere Pakete an um Daten an des RMX-Server zu
 * empfangen.
 * 
 * @author Corinna Coels
 */
public class Receive {

	/**
	 * Empfaengt die Antwort des PC-Servers und gibt sie inklusive Kopfdaten
	 * zurueck. Sollte im Normalfall nicht verwendet werden, da fuer die weitere
	 * Bearbeitung zu viele ueberfluessige Informationen gespeichert werden.
	 * 
	 * @return byte[] - Antwortarray korrekter Laenge inklusive Kopfdaten
	 */
	@Deprecated
	protected static byte[] receiveWithHeader() throws IOException {
		InputStream is;

		is = Connection.socket.getInputStream();

		int header;
		int commandLength;

		// Hole Kopfinfos des RMX-Pakets...
		header = is.read(); // sollte immer 124 sein
		commandLength = is.read(); // Gesamtlaenge inkl. Kopfinfos

		// Antwortarray korrekter Laenge initialisieren
		byte[] response = new byte[commandLength];

		// 2 Bytes an Kopfinfos in Reply festhalten
		// (brauch man theoretisch vermutlich nicht?)
		response[0] = (byte) header;
		response[1] = (byte) commandLength;

		// eigentliche Daten empfangen und speichern
		for (int i = 0 + 2; i < response.length; i++) {
			response[i] = (byte) is.read();
		}

		// Fertiges Antwortarray rueckgeben
		return response;
	}

	/**
	 * Empfaengt die Antwort des PC-Servers. Sofern noch ein HEADBYTE 0x7c vorne
	 * am Byte-Array haengt, wird dieses ignoriert.
	 * 
	 * @return byte[] - Antwortarray korrekter Laenge ohne Kopfdaten
	 */
	protected static byte[] receive() throws IOException {
		InputStream is;

		is = Connection.socket.getInputStream();

		int commandLength;

		// Erstes Byte einlesen, pruefen ob es sich um ein HEADBYTE handelt
		// Wenn ja, ueberspringen und die Laenge danach einlesen
		commandLength = is.read();
		while (commandLength == Connection.HEADBYTE) {
			commandLength = is.read();
		}

		// Antwortarray korrekter Laenge initialisieren
		byte[] response = new byte[commandLength - 2];

		// eigentliche Daten empfangen und speichern
		for (int i = 0; i < response.length; i++) {
			response[i] = (byte) is.read();
		}

		// Fertiges Antwortarray rueckgeben
		return response;
	}

	/**
	 * Verarbeitung eingehender Daten (mit bereits entfernten Kopfdaten).
	 * Verarbeitung der Daten erfolgt entsprechend ihrem OPCODE. Setzt
	 * lastCommandFinished der Connection-Klasse auf true, sofern es sich um
	 * eine eingehende Bestaetigungsnachricht handelt.
	 * 
	 * @param message
	 *            Datenpaket, welches verarbeitet werden soll. Form: </br>
	 *            -OPCODE- -DATA-
	 */
	protected static void processIncoming(byte[] message) {

		int opcode = -1;

		// Bearbeitung nur moeglich, wenn das Array auch etwas enthaelt
		if (message.length > 0) {

			opcode = message[0];

			switch (opcode) {
			case 0:
				// Positivquittung

				// finale Positivquittung
				if (message[1] == 0) {
					Connection.lastCommandFinished = true;
					// Positivquittung: Bearbeitung laeuft
				} else if (message[1] == 1) {
					// Am besten abwarten bis Server abgearbeitet hat und erst
					// am Ende
					// bei Empfang der finalen Quittung auf true setzen
					Connection.lastCommandFinished = true;
				}
				break;
			case 1:
				// Negativquittung
				read0x01(message);
				Connection.lastCommandFinished = true;
				break;
			case 3:
				// Antwort der Initialisierung, RMX-Version-Info
				if (message[2] != Connection.RMXVERSION) {
					Connection.errorList
							.add("Incompatible RMX-version! Please make sure "
									+ "that server and client use the same "
									+ "version of RMXnet!");
					Connection.getConnection().terminateThread();
				}
				Connection.lastCommandFinished = true;
				break;
			case 4:
				// Statusausgabe
				read0x04(message);
				Connection.lastCommandFinished = true;
				break;
			case 6:
				// Wert aus RMX-Adresse
				read0x06(message);
				break;
			case 8:
				// Lokinformation aus Zentrale
				read0x08(message);
				break;
			case 32: // Hex: 0x20
				// Info RMX-Kanal
				read0x20(message);
				break;
			case 36: // Hex: 0x24
				// Info Loksteuerung Geschwindigkeit
				read0x24(message);
				Connection.lastCommandFinished = true;
				break;
			case 40: // Hex: 0x28
				// Info Loksteuerung Funktion
				read0x28(message);
				Connection.lastCommandFinished = true;
				break;
			case 192: // Hex: 0xc0
				// Lesen Lokdecoder
				break;
			default:
				// Fehlerfall, OPCODE aus message nicht gelesen?
				// oder anderen OPCODE gelesen?
				Connection.errorList
						.add("Server sent data with unknown OPCODE");
				break;
			}
		}
	}

	/**
	 * Verarbeitung einer Negativquittung des RMX-Servers. Fehlernachrichten vom
	 * Server werden an die Connection.errorList angehaengt und koennen so von
	 * der allgemeinen Fehlerbehandlung mit ausgelesen/verarbeitet werden.
	 * 
	 * @param message
	 */
	private static void read0x01(byte[] message) {
		// Negativquittung
		// <0x01><0x0?>

		switch (message[1]) {
		case 1: // Hex: 0x01
			// 0x01: unbekannter OPCODE
			Connection.errorList.add("Received from Server: unknown OPCODE");
			break;
		case 3: // Hex: 0x03
			// Loknummer nicht in Datenbank
			Connection.errorList
					.add("Received from Server: train number not in database");
			break;
		case 4: // Hex: 0x04
			// Eingabefehler
			Connection.errorList.add("Received from Server: input-error");
			break;
		case 5: // Hex: 0x05
			// Mode ungleich 0x01
			Connection.errorList.add("Received from Server: mode is not 0x01");
			break;
		case 7: // Hex: 0x07
			// Eingabe Lokomotiven Datenbank voll
			Connection.errorList
					.add("Received from Server: train data base full");
			break;
		case 8: // Hex: 0x08
			// Steuerkan�le belegt
			Connection.errorList
					.add("Received from Server: control channel occupied");
			break;
		}
	}

	/**
	 * Verarbeitung empfangener Statusnachrichten vom RMX-Server.
	 * 
	 * @param message
	 */
	private static void read0x04(byte[] message) {
		// <0x04><STATUS>
		/*
		 * Bit 5 und 6: 1 Initialisierung erfolgreich
		 * 
		 * Bit 7: 0 Zentrale Aus Bit 7: 1 Zentrale Ein
		 */

		if (CommunicationUtils.bitIsSet(message[1], 5)
				&& CommunicationUtils.bitIsSet(message[1], 6)) {
			// Initialisierung erfolgreich
		}
		if (CommunicationUtils.bitIsSet(message[1], 7)) {
			DataToComInterface.changeEnergyStatus(true);
		} else {
			DataToComInterface.changeEnergyStatus(false);
		}
	}

	/**
	 * Verarbeitung empfangener RMX-Adressinformationen.
	 * 
	 * @param message
	 */
	private static void read0x06(byte[] message) {
		// Wert aus RMX-Adresse
		// <0x06><RMX><ADRRMX><VALUE>
		// [0] [1] [2] [3]

		DataToComInterface.updateBusAdress(message[1], message[2], message[3]);
	}

	/**
	 * Verarbeitung von empfangenen Lokinfos des RMX-Servers.
	 * 
	 * @param message
	 */
	private static void read0x08(byte[] message) {
		// Lokinfos aus Zentrale: empfangen
		// 0x08 0x00 0x01 0x01 0x02 0x49 0x43 0x45 0x31
		// <0x08><ADRH><ADRL><ADRK><OPMODE><n*AN>
		// [0] [1] [2] [3] [4] [5 bis i]

		int adrLong = CommunicationUtils.bytesToInt(message[1], message[2]);
		int adrShort = message[3];
		byte opmode = message[4];

		String trainName = "";

		// Falls vorhanden, Name der Lok auslesen
		if (message.length > 5) {
			byte[] byteTrainName = new byte[message.length - 5];

			for (int i = 0; i < byteTrainName.length; i++) {
				byteTrainName[i] = message[i + 5];
			}

			trainName = CommunicationUtils.bytesToString(byteTrainName);
		}

		switch (opmode) {
		case 2: // Hex: 0x02
			// SX1: kurze Adresse, Steuerung �ber Loknummer
			// Fahrstufen: 31
			DataToComInterface.addTrain(adrLong, adrShort, opmode, (byte) 0,
					trainName, (byte) 0, (byte) 0, (byte) 0, (byte) 0, 31);
			break;
		case 3: // Hex: 0x03
			// SX1: Adressdynamik, lange Adresse
			// Fahrstufen: 31 ???
			DataToComInterface.addTrain(adrLong, adrShort, opmode, (byte) 0,
					trainName, (byte) 0, (byte) 0, (byte) 0, (byte) 0, 31);
			break;
		case 5: // Hex: 0x05
			// SX1: kurze Adresse, Steuerung �ber Loknummer mit
			// Zusatzfunktionsadresse (+1)
			// Fahrstufen: 31 ???
			DataToComInterface.addTrain(adrLong, adrShort, opmode, (byte) 0,
					trainName, (byte) 0, (byte) 0, (byte) 0, (byte) 0, 31);
			break;
		case 6: // Hex: 0x06
			// SX1: Adressdynamik mit Zusatzfunktionsadresse
			// Fahrstufen: 31 ???
			DataToComInterface.addTrain(adrLong, adrShort, opmode, (byte) 0,
					trainName, (byte) 0, (byte) 0, (byte) 0, (byte) 0, 31);
			break;
		case 7: // Hex: 0x07
			// SX2: 127 Fahrstufen, lange Adresse
			// Fahrstufen: 127
			DataToComInterface.addTrain(adrLong, adrShort, opmode, (byte) 0,
					trainName, (byte) 0, (byte) 0, (byte) 0, (byte) 0, 127);
			break;
		case 9: // Hex: 0x09
			// DCC: 14 Fahrstufen, kurze Adresse, Steuerung �ber Loknummer
			DataToComInterface.addTrain(adrLong, adrShort, opmode, (byte) 0,
					trainName, (byte) 0, (byte) 0, (byte) 0, (byte) 0, 14);
			break;
		case 10: // Hex: 0x0a
			// DCC: 14 Fahrstufen, lange Adresse
			DataToComInterface.addTrain(adrLong, adrShort, opmode, (byte) 0,
					trainName, (byte) 0, (byte) 0, (byte) 0, (byte) 0, 14);

			break;
		case 12: // Hex: 0x0c
			// DCC: 28 Fahrstufen, kurze Adresse, Steuerung �ber Loknummer
			DataToComInterface.addTrain(adrLong, adrShort, opmode, (byte) 0,
					trainName, (byte) 0, (byte) 0, (byte) 0, (byte) 0, 28);
			break;
		case 13: // Hex: 0x0d
			// DCC: 28 Fahrstufen, lange Adresse
			DataToComInterface.addTrain(adrLong, adrShort, opmode, (byte) 0,
					trainName, (byte) 0, (byte) 0, (byte) 0, (byte) 0, 28);
			break;
		case 15: // Hex: 0x0f
			// DCC: 126 Fahrstufen, kurze Adresse, Steuerung �ber Loknummer
			DataToComInterface.addTrain(adrLong, adrShort, opmode, (byte) 0,
					trainName, (byte) 0, (byte) 0, (byte) 0, (byte) 0, 126);
			break;
		case 16: // Hex: 0x10
			// DCC: 126 Fahrstufen, lange Adresse
			DataToComInterface.addTrain(adrLong, adrShort, opmode, (byte) 0,
					trainName, (byte) 0, (byte) 0, (byte) 0, (byte) 0, 126);
			break;
		case 17: // Hex: 0x11
			// SX2: 31 Fahrstufen, lange Adresse
			DataToComInterface.addTrain(adrLong, adrShort, opmode, (byte) 0,
					trainName, (byte) 0, (byte) 0, (byte) 0, (byte) 0, 31);
			break;
		case 31: // Hex: 0x1f
			/*
			 * L�schen einer Lok �ber Loknummer: Angabe der alphanumerische
			 * Zeichen nicht erlaubt. <COUNT> = 7. Wird beim L�schen die
			 * Loknummer 0000 eingegeben, wird die gesamte Datenbank gel�scht.
			 */
			DataToComInterface.deleteTrain(adrLong);
			break;
		case 32: // Hex: 0x20
			/*
			 * Lesen eines Datensatzes �ber Loknummer: Angabe der
			 * alphanumerische Zeichen nicht erlaubt. <COUNT> = 7.
			 * 
			 * Sollte eigentlich nicht vom Server kommen...?
			 */
			DataToComInterface.deleteAllTrains();
			break;
		default: // unsupported OPMODE
			Connection.errorList.add("Server sent data with unknown OPMODE");
			break;
		}
	}

	/**
	 * Verarbeitung der RMX-Kanalinfo-Nachrichten.
	 * 
	 * @param message
	 */
	private static void read0x20(byte[] message) {
		// <0x20><ADRH><ADRL><ADRRMX><OPMODE><FS>
		// [0] [1] [2] [3] [4] [5]

		int adrLong = CommunicationUtils.bytesToInt(message[1], message[2]);
		byte adrRmx = message[3];

		DataToComInterface.setTrainRmx(adrLong, adrRmx);
	}

	/**
	 * Verarbeitung der Informationen ueber Lokgeschwindigkeiten.
	 * 
	 * @param message
	 */
	private static void read0x24(byte[] message) {
		// <0x24><ADRH><ADRL><SPEED><DIR>

		int adrLong = CommunicationUtils.bytesToInt(message[1], message[2]);

		// Aktuelle Richtung des Zuges setzen
		DataToComInterface.setTrainDirection(adrLong, message[4]);
		// Aktuelle Fahrstufe/Geschwindigkeit des Zuges setzen
		DataToComInterface.setTrainSpeed(adrLong, message[3]);
	}

	/**
	 * Verarbeitung der Informationen ueber Loksteuerungs-Funktionen.
	 * 
	 * @param message
	 */
	private static void read0x28(byte[] message) {
		// <0x28><ADRH><ADRL><F0F7><F8F15><F16F23>
		// [0] [1] [2] [3] [4] [5]

		int adrLong = CommunicationUtils.bytesToInt(message[1], message[2]);

		// F-Infos verarbeiten..
		DataToComInterface.setTrainMode0to7(adrLong, message[3]);
		DataToComInterface.setTrainMode8to15(adrLong, message[4]);
		DataToComInterface.setTrainMode16to23(adrLong, message[5]);
	}
}