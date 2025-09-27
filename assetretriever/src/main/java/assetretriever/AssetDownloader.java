package assetretriever;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

public class AssetDownloader {
	public static void download(File file, boolean exists, String filename, String gameDir, int i) {
    	if(exists) {
			// invalid file, delete
			try {
    			if(!file.delete()) {
    				AssetRetriever.log.warn("Couldn't delete File "+file.getPath());
    				return;
    			}
			}
			catch (SecurityException e) {
				AssetRetriever.log.warn("Couldn't delete File "+file.getPath()+". error: "+e.getMessage());
    			return;
			}
		}

		if(AssetRetriever.futures == null) AssetRetriever.futures = new ArrayList<>();
	
		
		CompletableFuture<Path> future = downloadFileAsync(AssetRetriever.downloadURLs[i], gameDir, filename+".temp");
		AssetRetriever.futures.add(future);
		
        
		AssetRetriever.processing[i] = true;
    }
    
    public static void postProcess(String gameDir, boolean early) {

        CompletableFuture<Void> allOf = CompletableFuture.allOf(AssetRetriever.futures.toArray(new CompletableFuture[0]));
        
        allOf.join(); // wait for completion
        
        for(int i = 0; i < AssetRetriever.processing.length; i++) {
        	if(AssetRetriever.processing[i]) {
        		File dest = new File(gameDir+AssetRetriever.downloadDirs[i], AssetRetriever.downloadFiles[i]);
        		File folder = dest.getParentFile();
				if(!folder.exists()) {
					folder.mkdirs();
				}
        		try {
        			Files.move(new File(gameDir, AssetRetriever.downloadFiles[i]+".temp").toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
        		} catch (IOException e) {
        			AssetRetriever.log.warn("Couldn't move File "+AssetRetriever.downloadFiles[i]+".temp "+e.getClass().getName());
        			continue;
        		}
        		
        		
        		
        		if(AssetRetriever.loadjar[i])
        			AssetLoader.loadJar(dest);

        		if(dest.exists()) {
        			// file exists
        			if(AssetRetriever.verifyFileSize(dest, AssetRetriever.downloadSizes[i])) {
        				// valid file size
        				if(ChecksumUtil.verifyFileChecksum(dest, AssetRetriever.downloadCheckSums[i], "SHA-1")) {
        					// valid file checksum
        					AssetRetriever.success[i] = true;
        				}
        			}
        		}
        	}
        }
        
        
        AssetRetriever.futures = null;
        Arrays.fill(AssetRetriever.processing, false);
        AssetRetriever.finish(early);
    }

	private static CompletableFuture<Path> downloadFileAsync(String url, String directory, String filename) {
		return CompletableFuture.supplyAsync(() -> {
		try {
			AssetRetriever.log.info("Downloading "+url);
			Path dirPath = Paths.get(directory);
			Files.createDirectories(dirPath);
			Path filePath = dirPath.resolve(filename);

			java.net.URL urlObj = new java.net.URL(url);
			HttpURLConnection connection = (HttpURLConnection) urlObj.openConnection();
			connection.setConnectTimeout(4000);
			connection.setReadTimeout(4000);
			connection.setRequestMethod("GET");
			connection.setRequestProperty("User-Agent", 
			"Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
			"AppleWebKit/537.36 (KHTML, like Gecko) " +
			"Chrome/115.0.0.0 Safari/537.36");
			connection.setRequestProperty("Accept", 
			"text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
			if(connection.getResponseCode() != 200) {
				throw new IOException("Invalid Response "+connection.getResponseCode());
			}

			try (InputStream in = connection.getInputStream()) {
				Files.copy(in, filePath, StandardCopyOption.REPLACE_EXISTING);
			}

			connection.disconnect();
			AssetRetriever.log.info("Download completed "+url);

			return filePath;
		} catch (IOException e) {
			AssetRetriever.log.info("Failed to download "+url);
		}
		return null;
		});
	}
}
