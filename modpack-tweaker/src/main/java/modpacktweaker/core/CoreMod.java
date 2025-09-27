package modpacktweaker.core;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import java.util.Map;

@IFMLLoadingPlugin.MCVersion("1.7.10")
@IFMLLoadingPlugin.TransformerExclusions({"modpacktweaker.core"})
public class CoreMod implements IFMLLoadingPlugin{
    @Override
    public String[] getASMTransformerClass() {
        return new String[] {ClassTransformer.class.getName()};
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(final Map<String, Object> data) {
    	ClassTransformer.obf = (boolean) data.get("runtimeDeobfuscationEnabled");
    	FMLLog.info(ClassTransformer.coreprefix+": CoreMod Startup - Obfuscation=" + ClassTransformer.obf);
    	ClassTransformer.setup();
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}