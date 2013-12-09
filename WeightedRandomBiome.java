package forgebiosphere;

import net.minecraft.util.WeightedRandomItem;
import net.minecraft.world.biome.BiomeGenBase;

public class WeightedRandomBiome extends WeightedRandomItem {
	public int biomeID;

	public WeightedRandomBiome(int weight, int biomeID) {
		super(weight);
		this.biomeID = biomeID;
	}

	@Override
	public boolean equals(Object target) {
		return target instanceof BiomeGenBase && biomeID == ((BiomeGenBase) target).biomeID;
	}
}
