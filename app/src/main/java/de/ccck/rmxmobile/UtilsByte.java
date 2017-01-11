package de.ccck.rmxmobile;

/**
 * Enthaellt Hilfsmethoden zur Bearbeitung von Bytes
 *
 * @author Kientzle Claus
 */
public class UtilsByte {

    /**
     * �berpr�ft, ob ein Bit gesetzt ist.
     * @param value
     * @param bitIndex
     * @return true if Bit is 1
     */
    public static synchronized boolean bitIsSet(byte value, int bitIndex) {
        return (value & (1 << bitIndex)) != 0;
    }

    /**
     * Set a bit to 1.
     * @param b - byte
     * @param i - bit
     * @return byte
     */
    public static synchronized byte setToOne(byte b, int i) {
        b = (byte) (b | (1 << i));
        return b;
    }

    /**
     * Set a bit to 0.
     * @param b - byte
     * @param i - bit
     * @return byte
     */
    public static synchronized byte setToZero(byte b, int i) {
        b = (byte) (b & ~(1 << i));
        return b;
    }

    /**
     * Creates a String of the bit informations of a byte.
     * Used for the "old SwitchboardButtons" for the format
     * "22|00011010".
     * @param bits
     * @return String
     */
    protected static synchronized String createString(byte bits) {
        String zero = "";
        String string = String.format("%8s", Integer.toBinaryString(bits & 0xFF)).replace(' ', '0');
        if (8 - string.length() > 0) {
            for (int i = 0; i < (8 - string.length()); i++) {
                zero = zero + "0";
            }
        }
        return zero + string;
    }

    /**
     * Wandelt ein Byte vom oder f�r den Server in einen
     * reverseByte um.
     * @param bytes - Byte das umgewandelt werden soll
     * @return reverseByte
     */
    public static synchronized byte getServerBytes(byte bytes) {
        byte reverseByte = 0;
        for (int i = 0; i < 8; i++)
            if ((bytes & (byte) (1 << i)) != 0)
                reverseByte += (byte) (1 << (7 - i));
        return reverseByte;
    }

}