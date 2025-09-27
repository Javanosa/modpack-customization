package assetretriever;

import java.io.File;
import java.io.FileInputStream;
import java.security.MessageDigest;

public class ChecksumUtil {

	private static String getFileChecksum(File file, String algorithm) {
		try {

			MessageDigest digest = MessageDigest.getInstance(algorithm);
			try (FileInputStream fis = new FileInputStream(file)) {
				byte[] buffer = new byte[8192]; // 8 kb buffer
				int bytesRead;
				while ((bytesRead = fis.read(buffer)) != -1) {
					digest.update(buffer, 0, bytesRead);
				}
			}
			
			byte[] hashBytes = digest.digest();
			return bytesToHex(hashBytes);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private static String bytesToHex(byte[] bytes) {
		StringBuilder sb = new StringBuilder();
		for (byte b : bytes) {
			sb.append(String.format("%02x", b));
		}
		return sb.toString();
	}

	public static boolean verifyFileChecksum(File file, String expectedChecksum, String algorithm) {
		String actualChecksum = getFileChecksum(file, algorithm);
		boolean valid = actualChecksum.equalsIgnoreCase(expectedChecksum);
		if(!valid)
			AssetRetriever.log.warn("Expect SHA-1 "+expectedChecksum+" but got "+actualChecksum+" for "+file.getName());
		return valid;
	}
}
