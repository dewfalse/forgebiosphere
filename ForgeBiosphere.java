package forgebiosphere;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.LanguageRegistry;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.MinecraftForge;

@Mod(modid = ForgeBiosphere.modid, name = ForgeBiosphere.modid, version = "1.7.10.1")
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
		worldTypeBiosphere = new WorldTypeBiosphere("biosphere");
		MinecraftForge.EVENT_BUS.register(new PopulateChunkEventHandler());
		MinecraftForge.EVENT_BUS.register(new ChunkProviderEventHandler());

		for (String s : Config.BIOMES_LIST.split(",")) {
			if (s == null || s.trim().isEmpty()) {
				continue;
			}
			String[] pair = s.trim().split(":");
			if (pair.length != 2) {
				continue;
			}
			try {
				int biomeID = Integer.parseInt(pair[0]);
				int rarity = Integer.parseInt(pair[1]);
				BiosphereBiomeManager.addWeightedRandomBiome(biomeID, rarity);
			} catch (NumberFormatException e) {
				for (BiomeGenBase biome : BiomeGenBase.getBiomeGenArray()) {
					if (biome == null) {
						continue;
					}
					if (pair[0].equalsIgnoreCase(biome.biomeName)) {
						try {
							int rarity = Integer.parseInt(pair[1]);
							BiosphereBiomeManager.addWeightedRandomBiome(biome.biomeID, rarity);
						} catch (NumberFormatException e2) {
						}
					}
				}
			}
		}
	}
}
