public class Util {
	public static String bytesToString(byte[] b) {
		StringBuilder build = new StringBuilder(b.length);
		build.append(Character.toChars(b[0])[0]);
		return build.toString();
	}
}
