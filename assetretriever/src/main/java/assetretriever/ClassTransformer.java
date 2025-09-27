package assetretriever;

import net.minecraft.launchwrapper.IClassTransformer;

public class ClassTransformer implements IClassTransformer {
	
	private static boolean pass;

	@Override
	public byte[] transform(final String name, final String transformedName, final byte[] bytes) {
		if(!pass && "net.minecraft.client.renderer.entity.RenderManager".equals(transformedName)) {
			AssetRetriever.process(AssetRetrieverTweaker.c_gameDir, false);
			pass = true;
		}
		return bytes;
	}
	
	

}
