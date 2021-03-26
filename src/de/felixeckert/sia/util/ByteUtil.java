package de.felixeckert.sia.util;

public class ByteUtil {
	public static int shiftTo255(byte byt) {
		return byt & 0xFF;
	}
}
