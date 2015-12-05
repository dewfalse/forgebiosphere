package forgebiosphere;

import net.minecraft.util.WeightedRandom;
import net.minecraft.world.biome.BiomeGenBase;

import java.util.ArrayList;
import java.util.Random;

public class BiosphereBiomeManager {
	private static ArrayList<WeightedRandomBiome> biomes = new ArrayList<WeightedRandomBiome>();

	public static float addWeightedRandomBiome(int biomeID, int rarity) {
		if (rarity <= 0) {
			throw new IllegalArgumentException("Rarity must be greater then zero");
		}

		for (WeightedRandomBiome biome : biomes) {
			if (biomeID == biome.biomeID) {
				return biome.itemWeight += rarity;
			}
		}

		biomes.add(new WeightedRandomBiome(rarity, biomeID));
		return rarity;
	}

	public static int removeWeightedRandomBiome(int biomeID) {
		for (WeightedRandomBiome biome : biomes) {
			if (biomeID == biome.biomeID) {
				biomes.remove(biome);
				return biome.itemWeight;
			}
		}
		return 0;
	}

	public static BiomeGenBase getRandomBiome(Random rand) {
		WeightedRandomBiome biome = (WeightedRandomBiome) WeightedRandom.getRandomItem(rand, biomes);
		if (biome == null) {
			return BiomeGenBase.plains;
		}
		return BiomeGenBase.getBiomeGenArray()[biome.biomeID];
	}

	static {
		addWeightedRandomBiome(BiomeGenBase.plains.biomeID, 25);
		addWeightedRandomBiome(BiomeGenBase.forest.biomeID, 50);
		addWeightedRandomBiome(BiomeGenBase.taiga.biomeID, 40);
		addWeightedRandomBiome(BiomeGenBase.desert.biomeID, 25);
		addWeightedRandomBiome(BiomeGenBase.icePlains.biomeID, 25);
		addWeightedRandomBiome(BiomeGenBase.jungle.biomeID, 25);
		addWeightedRandomBiome(BiomeGenBase.swampland.biomeID, 40);
		addWeightedRandomBiome(BiomeGenBase.mushroomIsland.biomeID, 5);
		addWeightedRandomBiome(BiomeGenBase.ocean.biomeID, 10);
	}

}
