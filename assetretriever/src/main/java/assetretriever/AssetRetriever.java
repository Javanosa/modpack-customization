package assetretriever;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AssetRetriever {
	public static String[] downloadFiles = new String[0];
	public static String[] downloadURLs;
	public static String[] downloadDirs;
	public static String[] downloadCheckSums;
	public static long[] downloadSizes;
	public static boolean[] loadjar;
	public static boolean[] handleEarly;
	
	public static Logger log = LogManager.getLogger("AssetRetriever");
	
	static boolean[] processing;
	static boolean[] success;
	static List<CompletableFuture<Path>> futures = null;
	static List<String> urls = null;
	static long startTime;
	
	public static void read(final String gameDir) {
		BufferedReader reader = null;
		String line;
		
		final File file = new File(gameDir+"/config/", "schedulefiles.txt");
		
		try {
			if(!file.exists()) {
				return;
			}
			
			try {
				reader = new BufferedReader(new FileReader(file), 8192);
				int lines = -1;
				int index = 0;
				while((line = reader.readLine()) != null) {
					if(lines == -1) {
						// first file line is number of files
						lines = Integer.parseInt(line);
						downloadFiles = new String[lines];
						downloadURLs = new String[lines];
						downloadDirs = new String[lines];
						downloadCheckSums = new String[lines];
						downloadSizes = new long[lines];
						loadjar = new boolean[lines];
						handleEarly = new boolean[lines];
						processing = new boolean[lines];
						success = new boolean[lines];
					}
					
					final String[] values = line.split(";");
					if(values.length >= 7 && index < lines) {
						downloadDirs[index] = values[0];
						downloadFiles[index] = values[1];
						downloadURLs[index] = values[2];
						downloadSizes[index] = Long.parseLong(values[3]);
						downloadCheckSums[index] = values[4];
						loadjar[index] = Boolean.parseBoolean(values[5]);
						handleEarly[index] = Boolean.parseBoolean(values[6]);
						index++;
					}
				}
				reader.close();
			}
			catch (IOException e) {
				if(reader != null)
					reader.close();
				e.printStackTrace();
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void finish(boolean early) {
		
		
		log.info((early ? "Early" : "Late")+": Done "+(System.currentTimeMillis()-startTime)+"ms");
		
		if(early) return; // not cleanup yet
		
		if(success != null) {
			delete();
		}
		
		
		success = null;
		downloadCheckSums = null;
		downloadSizes = null;
		downloadFiles = null;
		downloadURLs = null;
		handleEarly = null;
		loadjar = null;
		processing = null;
		urls = null;
		futures = null;
		log = null;
		AssetRetrieverTweaker.c_args = null;
		AssetRetrieverTweaker.c_profile = null;
		AssetRetrieverTweaker.c_gameDir = null;
	}
	
	public static void delete() {
		
		boolean anyFailure = false;
		for(boolean s : success) {
			if(!s) anyFailure = true;
		}
		
		if(!anyFailure) {
			try {
				Files.deleteIfExists(new File(AssetRetrieverTweaker.c_gameDir+"/config/", "schedulefiles.txt").toPath());
				log.info("Cleanup");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static boolean verifyFileSize(File file, long expected) {
		long size = file.length();
		boolean valid = file.length() == expected;
		if(!valid)
			log.warn("Expect size "+expected+" but got "+size+" for "+file.getName());
		return valid;
	}

    public static void process(String gameDir, boolean early) {
    	log.info((early ? "Early" : "Late")+": Init");
    	startTime = System.currentTimeMillis();
    	for(int i = 0; i < downloadFiles.length; i++) {
    		if(early != handleEarly[i]) continue;
    		
    		String filename = downloadFiles[i];
    		File file = new File(gameDir + downloadDirs[i], 	filename);
    		boolean exists = file.exists();

    		if(exists) {
    			// file exists
    			if(verifyFileSize(file, downloadSizes[i])) {
    				// valid file size
    				if(ChecksumUtil.verifyFileChecksum(file, downloadCheckSums[i], "SHA-1")) {
    					// valid file checksum
    					success[i] = true;
    					continue;
    				}
    			}
    		}
    		// schedule download
    		AssetDownloader.download(file, exists, filename, gameDir, i);
    	}
    	
    	if(futures == null) {
    		finish(early);
    		return; // we are done here
    	}
    	// process downloads and copy files
    	AssetDownloader.postProcess(gameDir, early);
    }
}