public class MyByte {

	public static String toHexString(byte b) {
		int i;

		i = (int) b & 0xff;
		return Integer.toHexString(i);

	}

	public static byte parseHexByte(String s) throws OutRangeNumberException {
		int i;
		byte b;

		i = Integer.parseInt(s, 16);
		if (s.length() > 2) {
			throw new OutRangeNumberException();
		}

		return (byte) i;

	}

}