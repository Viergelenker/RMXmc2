package de.ccck.rmxmobile.communication;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * Stellt mehrere Hilfsmethoden zur Verwendung innerhalb des
 * communication-Pakets bereit.
 * 
 * @author Corinna Coels
 */
public class CommunicationUtils {
	
//	private final static Charset CHARSET = Charset.forName("DIN_66003");
	private final static Charset CHARSET = Charset.forName("UTF-8");

	/**
	 * Hilfsmethode. Gibt ein ByteArray in Hex32-Form auf der Konsole aus.
	 * Methode dient primaer Debugging-Zwecken.
	 * 
	 * @param toPrint
	 * @deprecated
	 */
	protected static void printByteArray(byte[] toPrint) {
		for (int i = 0; i < toPrint.length; i++) {
			System.out.print(String.format("0x%02X", ((byte) toPrint[i])) + " ");
		}
	}

	/**
	 * Hilfsmethode. Konvertiert die Daten aus 2 Bytes in einen int-Wert.
	 * 
	 * @param highByte
	 * @param lowByte
	 * @return int - Den Dezimalwert aus den beiden Bytes
	 */
	protected static int bytesToInt(byte highByte, byte lowByte) {
		return ((int) highByte << 8) | ((int) lowByte & 0xFF);
	}

	/**
	 * Hilfesmethode. Konvertiert einen dezimalen int-Wert in 2 Bytes um.
	 * Gesplittet in highByte und lowByte.
	 * 
	 * @param value
	 * @return byte[] - Byte-Array bestehend aus highByte und lowByte
	 */
	protected static byte[] intToBytes(int value) {
		byte[] byteValue = new byte[2];
	
		byteValue[1] = (byte) (value & 0xFF);
		byteValue[0] = (byte) ((value >> 8) & 0xFF);
	
		return byteValue;
	}

	/**
	 * Hilfsmethode. Bytes in Text uebersetzen. Wird genutzt um die Loknamen
	 * aufzuloesen.
	 * 
	 * @param bytes
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	protected static String bytesToString(byte[] bytes) {
		String string = new String(bytes, CHARSET);
		
		string = string.replace('@', '�');
		string = string.replace('[', '�');
		string = string.replace('\\', '�');
		string = string.replace(']', '�');
		string = string.replace('{', '�');
		string = string.replace('|', '�');
		string = string.replace('}', '�');
		string = string.replace('~', '�');

		return string; 
	}

	/**
	 * Hilfsmethode. Text in Bytes uebersetzen. Wird genutzt um die Loknamen
	 * aufzuloesen.
	 * 
	 * @param string
	 * @return
	 */
	protected static byte[] stringToBytes(String string) {
		
		string = string.replace('�', '@');
		string = string.replace('�', '[');
		string = string.replace('�', '\\');
		string = string.replace('�', ']');
		string = string.replace('�', '{');
		string = string.replace('�', '|');
		string = string.replace('�', '}');
		string = string.replace('�', '~');
		
		return string.getBytes(CHARSET);
	}

	/**
	 * Hilfsmethode. Prueft ob in einem byte das Bit an der Uebergebenen
	 * Position gesetzt ist oder nicht.
	 * 
	 * @param value Der zu ueberpruefende Byte-Wert
	 * @param bitIndex Zaehlung von 0-7
	 * @return true = gesetzt</br>false = nicht gesetzt
	 */
	protected static boolean bitIsSet(byte value, int bitIndex)
	{
	    return (value & (1 << bitIndex)) != 0;
	}

	/**
	 * Hilfsmethode. Setzt den Wert in einem Byte entweder auf 0 oder 1. 
	 * 
	 * @param value Das Byte, welches verarbeitet werden soll
	 * @param bitIndex Der Bit-Index, Zaehlung von 0-7
	 * @param bitValue true = gesetzt</br>false = nicht gesetzt
	 * @return 
	 */
	protected static byte setBitInByte(byte value, int bitIndex, boolean bitValue) {
		if (bitValue) {
			value = (byte) (value | (1 << bitIndex));
		} else {
			value = (byte) (value & ~(1 << bitIndex));
		}
		
		return value; 
	}

	/**
	 * Hilfesmethode. Fuegt zwei Arrays zu einem zusammen. 
	 * @param first - Erster Teil des neuen Arrays
	 * @param second - Zweiter Teil des Arrays
	 * @return Das zusammengefuegte Array
	 */
	protected static byte[] concatByteArrays(byte[] first, byte[] second) {
		byte[] result = Arrays.copyOf(first, first.length + second.length);
		
		System.arraycopy(second, 0, result, first.length, second.length);
		
		return result;
	}

}
