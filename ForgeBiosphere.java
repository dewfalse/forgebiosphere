package forgebiosphere;

import net.minecraft.world.WorldType;
import net.minecraftforge.common.MinecraftForge;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.LanguageRegistry;

@Mod(modid = ForgeBiosphere.modid, name = ForgeBiosphere.modid, version = "1.0")
public class ForgeBiosphere {
	public static final String modid = "forgebiosphere";

	public static WorldType worldTypeBiosphere;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		Config.preInit(event.getSuggestedConfigurationFile());
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		LanguageRegistry.instance().addStringLocalization("generator.biosphere", "Biospheres");
		worldTypeBiosphere = new WorldTypeBiosphere(Config.WORLD_TYPE_ID, "biosphere");
		MinecraftForge.EVENT_BUS.register(new PopulateChunkEventHandler());
		MinecraftForge.EVENT_BUS.register(new ChunkProviderEventHandler());
	}
}
