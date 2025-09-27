package assetretriever;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarFile;

import org.apache.logging.log4j.Level;

import com.google.common.base.Strings;
import com.google.common.primitives.Ints;

import cpw.mods.fml.common.asm.transformers.ModAccessTransformer;
import cpw.mods.fml.relauncher.CoreModManager;
import cpw.mods.fml.relauncher.FMLRelaunchLog;
import cpw.mods.fml.relauncher.ReflectionHelper;
import net.minecraft.launchwrapper.ITweaker;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraft.launchwrapper.LogWrapper;

public class AssetLoader {
	
	private static final List<String> loadedCoremods;
    
    private static final Method method_handleCascadingTweak;
    
    static {
		loadedCoremods = ReflectionHelper.getPrivateValue(CoreModManager.class, null, "loadedCoremods");
		method_handleCascadingTweak = ReflectionHelper.findMethod(CoreModManager.class, null, new String[] {"handleCascadingTweak"}, new Class[] {File.class, JarFile.class, String.class, LaunchClassLoader.class, Integer.class});
    }
	
	@SuppressWarnings("unchecked")
	private static void loadAndCallTweaker(String tweakName) {
		((List<String>) Launch.blackboard.get("TweakClasses")).remove(tweakName);
		LaunchClassLoader classLoader = Launch.classLoader;

		LogWrapper.log(Level.INFO, "Loading tweak class name %s", new Object[] { tweakName });
		classLoader.addClassLoaderExclusion(tweakName.substring(0, tweakName.lastIndexOf('.')));
		ITweaker tweaker = null;
		try {
			tweaker = (ITweaker) Class.forName(tweakName, true, classLoader).newInstance();
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			e.printStackTrace();
		}

		LogWrapper.log(Level.INFO, "Calling tweak class %s", new Object[] { tweaker.getClass().getName() });
		tweaker.acceptOptions(AssetRetrieverTweaker.c_args, Launch.minecraftHome, Launch.assetsDir, AssetRetrieverTweaker.c_profile);
		tweaker.injectIntoClassLoader(classLoader);
    }
	
	public static void loadJar(File coreMod) {
		LaunchClassLoader classLoader = Launch.classLoader;
		FMLRelaunchLog.fine("Examining for coremod candidacy %s", coreMod.getName());
		JarFile jar = null;
		Attributes mfAttributes;
		try {
			jar = new JarFile(coreMod);
			if (jar.getManifest() == null) {
				return;
			}
			ModAccessTransformer.addJar(jar);
			mfAttributes = jar.getManifest().getMainAttributes();
		}
		catch (IOException ioe) {
			FMLRelaunchLog.log(Level.ERROR, ioe, "Unable to read the jar file %s - ignoring", coreMod.getName());
			return;
		}
		finally {
			if (jar != null) {
				try {
					jar.close();
				}
				catch (IOException e) {}
			}
		}
		String cascadedTweaker = mfAttributes.getValue("TweakClass");
		if (cascadedTweaker != null) {
			FMLRelaunchLog.info("Loading tweaker %s from %s", cascadedTweaker, coreMod.getName());
			Integer sortOrder = Ints.tryParse(Strings.nullToEmpty(mfAttributes.getValue("TweakOrder")));
			sortOrder = (sortOrder == null ? Integer.valueOf(0) : sortOrder);
			try {
				method_handleCascadingTweak.invoke(null, coreMod, jar, cascadedTweaker, classLoader, sortOrder);
			}
			catch(Exception e) {
				e.printStackTrace();
			}
			loadedCoremods.add(coreMod.getName());

			loadAndCallTweaker(cascadedTweaker);
			return;
		}
	}
}
