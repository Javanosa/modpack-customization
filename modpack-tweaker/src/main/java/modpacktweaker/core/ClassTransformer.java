package modpacktweaker.core;

import net.minecraft.launchwrapper.IClassTransformer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.logging.log4j.Logger;
import cpw.mods.fml.common.FMLLog;

public class ClassTransformer implements IClassTransformer {
	static final String coreprefix = "ModpackTweaker-Core";
	static boolean obf = true;
	static final Logger log = FMLLog.getLogger();
	
	static List<String> classes = new ArrayList<>(0); // dummy list to avoid NPE
	
	public static void setup() {
		final String[] m = new String[2];
		
		m[0 ] = "net.minecraft.client.Minecraft";
		m[1 ] = "metweaks.PreLoad";

		classes = Arrays.asList(m);
		
		System.out.println(classes);
	}
	
	public byte[] transform(final String name, final String transformedName, final byte[] classBytes) {
		
		if(transformedName == null) System.err.println("transformedName is null");
		int i = classes.indexOf(transformedName);
    	return i == -1 ? classBytes : ClassHandler.transform(classBytes, i);
    }
}
