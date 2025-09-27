package assetretriever;

import java.io.File;
import java.util.List;

import net.minecraft.launchwrapper.ITweaker;
import net.minecraft.launchwrapper.LaunchClassLoader;

public class AssetRetrieverTweaker implements ITweaker {
	
	static List<String> c_args;
	static String c_profile;
	static String c_gameDir;

	@Override
	public void acceptOptions(List<String> args, File gameDir, File assetsDir, String profile) {
		c_args = args;
		c_profile = profile;
		c_gameDir = gameDir.getPath();
		AssetRetriever.read(c_gameDir);
		AssetRetriever.process(c_gameDir, true);
	}

	@Override
	public void injectIntoClassLoader(LaunchClassLoader classLoader) {
		classLoader.registerTransformer(ClassTransformer.class.getName());
	}

	@Override
	public String getLaunchTarget() {
		return "net.minecraft.client.main.Main";
	}

	@Override
	public String[] getLaunchArguments() {
		return new String[0];
	}
	
}
