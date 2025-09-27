package modpacktweaker;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.imageio.ImageIO;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.Level;
import org.lwjgl.opengl.Display;

import com.google.common.collect.ImmutableList;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.ReflectionHelper;
import cpw.mods.fml.relauncher.Side;
import lotr.common.LOTRMod;
import lotr.common.entity.npc.LOTREntityGandalf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiConfirmOpenLink;
import net.minecraft.client.gui.GuiCreateWorld;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSelectWorld;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.world.storage.SaveFormatComparator;
import net.minecraftforge.client.event.GuiScreenEvent.ActionPerformedEvent;
import net.minecraftforge.client.event.GuiScreenEvent.InitGuiEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;

public class Setup {
	
	static Field field_currentWorldType;
	static Field field_worlds;
	static Field field_prevScreen;
	
	public static void startup() {
		if(FMLCommonHandler.instance().getSide() != Side.CLIENT) {
			return;
		}
		
		File configFile = new File(Minecraft.getMinecraft().mcDataDir, "config/splash.properties");
        FileReader r = null;
        Properties config = new Properties();
        try
        {
            r = new FileReader(configFile);
            config.load(r);
        }
        catch(IOException e)
        {
            FMLLog.info("Could not load splash.properties, will create a default one");
        }
        finally
        {
            IOUtils.closeQuietly(r);
        }
        
        if(config.getProperty("enabled").equals("true")) {
        	config.setProperty("enabled", "false");
            
            FileWriter w = null;
            try
            {
                w = new FileWriter(configFile);
                config.store(w, "Splash screen properties");
            }
            catch(IOException e)
            {
                FMLLog.log(Level.ERROR, e, "Could not save the splash.properties file");
            }
            finally
            {
                IOUtils.closeQuietly(w);
            }
        }
        
		
		MinecraftForge.EVENT_BUS.register(new Setup());
		
		List<String> brandings = FMLCommonHandler.instance().getBrandings(true);
		List<String> list = new ArrayList<>(brandings);
		list.remove(0);
		
		ReflectionHelper.setPrivateValue(FMLCommonHandler.class, FMLCommonHandler.instance(), ImmutableList.copyOf(list), "brandings");
		 // remove Minecraft 1.7.10
		
		
		field_currentWorldType = ReflectionHelper.findField(GuiCreateWorld.class, "field_146331_K");
		field_worlds = ReflectionHelper.findField(GuiSelectWorld.class, "field_146639_s");
		field_prevScreen = ReflectionHelper.findField(GuiSelectWorld.class, "field_146632_a");
		
		
	}
	
	@SubscribeEvent
	public void onGuiInit(InitGuiEvent.Pre event) {
		if(event.gui.getClass() == GuiCreateWorld.class) {
			try {
				field_currentWorldType.setInt(event.gui, LOTRMod.worldTypeMiddleEarth.getWorldTypeID());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	@SubscribeEvent
	public void onGuiInit(InitGuiEvent.Post event) {
		if(event.gui.getClass() == GuiSelectWorld.class) {
			try {
				List<SaveFormatComparator> worlds = (List<SaveFormatComparator>) field_worlds.get(event.gui);
			
				if(worlds.size() == 0) {
					GuiScreen prevScreen = (GuiScreen) field_prevScreen.get(event.gui);
					Minecraft.getMinecraft().displayGuiScreen(new GuiCreateWorld(prevScreen));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		else if(event.gui.getClass() == GuiConfirmOpenLink.class) {
			List<GuiButton> buttons = event.buttonList;
			int left = (event.gui.width / 2) - buttons.size() * 52;
			for(GuiButton btn : buttons) {
				btn.xPosition = left;
				left += 105;
			}
		}
		else if(event.gui instanceof GuiMainMenu) {
			Iterator<GuiButton> btns = event.buttonList.iterator();
			while(btns.hasNext()) {
				GuiButton btn = btns.next();
				
				if(btn.id == 14) {
					btn.displayString = "Join Discord";
				}
				// we trust the ids isnt used by anyone else, otherwise compare the Realms name.
				/*if(btn.id == 14) {
					btns.remove();
				}
				// make Mods button bigger
				if(btn.id == 6) {
					btn.width = event.gui.width / 2 - 100;
				}*/
			}
			
		}
		
	}
	
	@SubscribeEvent
	public void onActionPerformed(ActionPerformedEvent.Pre event) {
		if(event.gui instanceof GuiMainMenu) {
			if(event.button.id == 14) { // used to be realms button
				event.setCanceled(true);
				final String link = "https://discord.com/invite/QXkZzKU";
				GuiConfirmOpenLink guiLink = new GuiConfirmOpenLink(new OpenLinkCallback(link, event.gui), link, 13, true);
				guiLink.func_146358_g();
				event.button.func_146113_a(Minecraft.getMinecraft().getSoundHandler());
				Minecraft.getMinecraft().displayGuiScreen(guiLink);
				
			}
		}
	}
	
	@SubscribeEvent
	public void onSetAttackTarget(LivingSetAttackTargetEvent event) {
		if(event.entity.getClass() == LOTREntityGandalf.class) {
			if(event.target instanceof EntityPlayer) {
				event.entityLiving.setRevengeTarget(null);
			}
		}
	}
}
