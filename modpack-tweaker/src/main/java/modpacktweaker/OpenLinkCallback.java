package modpacktweaker;

import java.net.URI;

import cpw.mods.fml.common.FMLLog;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiYesNoCallback;

public class OpenLinkCallback implements GuiYesNoCallback {
	final GuiScreen prevGUI;
	final String link;
	
	public OpenLinkCallback(String link, GuiScreen prev) {
		this.prevGUI = prev;
		this.link = link;
	}
	
	 @SuppressWarnings({ "rawtypes", "unchecked" })
	public void confirmClicked(boolean confirm, int id){
         if (confirm) {
            	try {
                    Class oclass = Class.forName("java.awt.Desktop");
                    Object object = oclass.getMethod("getDesktop", new Class[0]).invoke(null, new Object[0]);
                    oclass.getMethod("browse", new Class[] {URI.class}).invoke(object, new Object[] {new URI(link)});
                }
                catch (Throwable throwable) {
                    FMLLog.getLogger().error("Couldn\'t open link", throwable);
                }
            }
	        Minecraft.getMinecraft().displayGuiScreen(prevGUI);
	        
	}

}
