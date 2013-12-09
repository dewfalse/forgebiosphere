package forgebiosphere;

import java.util.List;
import java.util.Random;

import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.WorldChunkManager;
import net.minecraft.world.gen.layer.GenLayer;

public class ChunkManagerBiosphere extends WorldChunkManager {

	private Random rand;
	private long seed;

	private World worldObj;
	private int midX;
	private int midZ;
	private final Random rndSphere = new Random();

	public ChunkManagerBiosphere() {
	}

	public ChunkManagerBiosphere(long par1, WorldType par3WorldType) {
		super(par1, par3WorldType);
		GenLayer[] agenlayer = GenLayer.initializeAllBiomeGenerators(par1, par3WorldType);
		agenlayer = getModdedBiomeGenerators(par3WorldType, par1, agenlayer);
		this.seed = par1;
		this.rand = new Random(this.seed);
	}

	public ChunkManagerBiosphere(World par1World) {
		this(par1World.getSeed(), ForgeBiosphere.worldTypeBiosphere);
		this.worldObj = par1World;
	}

	@Override
	public List getBiomesToSpawnIn() {
		return super.getBiomesToSpawnIn();
	}

	@Override
	public BiomeGenBase getBiomeGenAt(int par1, int par2) {
		if (Config.NORMAL_BIOME) {
			return super.getBiomeGenAt(par1, par2);
		} else {
			// 1680,1702
			int x = par1 >> 4;
			int z = par2 >> 4;
			setRand(x, z);
			BiomeGenBase biome = BiosphereBiomeManager.getRandomBiome(this.rndSphere);
			return biome;
		}
	}

	@Override
	public float[] getRainfall(float[] par1ArrayOfFloat, int par2, int par3, int par4, int par5) {
		return super.getRainfall(par1ArrayOfFloat, par2, par3, par4, par5);
	}

	@Override
	public float getTemperatureAtHeight(float par1, int par2) {
		return super.getTemperatureAtHeight(par1, par2);
	}

	@Override
	public float[] getTemperatures(float[] par1ArrayOfFloat, int par2, int par3, int par4, int par5) {
		return super.getTemperatures(par1ArrayOfFloat, par2, par3, par4, par5);
	}

	@Override
	public BiomeGenBase[] getBiomesForGeneration(BiomeGenBase[] par1ArrayOfBiomeGenBase, int par2, int par3, int par4, int par5) {
		if (Config.NORMAL_BIOME) {
			return super.getBiomesForGeneration(par1ArrayOfBiomeGenBase, par2, par3, par4, par5);
		} else {
			// 418,422
			int x = (par2 + 2) >> 2;
			int z = (par3 + 2) >> 2;
			setRand(x, z);
			BiomeGenBase biome = BiosphereBiomeManager.getRandomBiome(this.rndSphere);
			BiomeGenBase[] ret = new BiomeGenBase[256];
			for (int i = 0; i < ret.length; ++i) {
				ret[i] = biome;
			}
			return ret;
		}
	}

	@Override
	public BiomeGenBase[] loadBlockGeneratorData(BiomeGenBase[] par1ArrayOfBiomeGenBase, int par2, int par3, int par4, int par5) {
		if (Config.NORMAL_BIOME) {
			return super.loadBlockGeneratorData(par1ArrayOfBiomeGenBase, par2, par3, par4, par5);
		} else {
			// 1680,1696
			int x = par2 >> 4;
			int z = par3 >> 4;
			setRand(x, z);
			BiomeGenBase biome = BiosphereBiomeManager.getRandomBiome(this.rndSphere);
			BiomeGenBase[] ret = new BiomeGenBase[256];
			for (int i = 0; i < ret.length; ++i) {
				ret[i] = biome;
			}
			return ret;
		}
	}

	@Override
	public BiomeGenBase[] getBiomeGenAt(BiomeGenBase[] par1ArrayOfBiomeGenBase, int par2, int par3, int par4, int par5, boolean par6) {
		if (Config.NORMAL_BIOME) {
			return super.getBiomeGenAt(par1ArrayOfBiomeGenBase, par2, par3, par4, par5, par6);
		} else {
			int x = par2;
			int z = par3;
			setRand(x, z);
			BiomeGenBase biome = BiosphereBiomeManager.getRandomBiome(this.rndSphere);
			BiomeGenBase[] ret = new BiomeGenBase[256];
			for (int i = 0; i < ret.length; ++i) {
				ret[i] = biome;
			}
			return ret;
		}
	}

	@Override
	public boolean areBiomesViable(int par1, int par2, int par3, List par4List) {
		if (Config.NORMAL_BIOME) {
			return super.areBiomesViable(par1, par2, par3, par4List);
		} else {
			// 1704,1544
			int x = par1 >> 4;
			int z = par2 >> 4;
			setRand(x, z);
			BiomeGenBase biome = BiosphereBiomeManager.getRandomBiome(this.rndSphere);
			if (!par4List.contains(biome)) {
				return false;
			}
			return true;
		}
	}

	@Override
	public ChunkPosition findBiomePosition(int par1, int par2, int par3, List par4List, Random par5Random) {
		return super.findBiomePosition(par1, par2, par3, par4List, par5Random);
	}

	@Override
	public void cleanupCache() {
		super.cleanupCache();
	}

	@Override
	public GenLayer[] getModdedBiomeGenerators(WorldType worldType, long seed, GenLayer[] original) {
		return super.getModdedBiomeGenerators(worldType, seed, original);
	}

	/*
	 * private BiomeGenBase getRandomBiome() { BiomeGenBase biome = null; while (biome == null) { biome = BiomeGenBase.biomeList[this.rndSphere.nextInt(BiomeGenBase.biomeList.length)]; } return biome;
	 * }
	 */
	private void setRand(int i, int j) {
		this.midX = ((i - (int) Math.floor(Math.IEEEremainder(i, Config.GRID_SIZE)) << 4) + 8);
		this.midZ = ((j - (int) Math.floor(Math.IEEEremainder(j, Config.GRID_SIZE)) << 4) + 8);
		this.rndSphere.setSeed(this.worldObj.getSeed());
		long l0 = this.rndSphere.nextLong() / 2L * 2L + 1L;
		long l1 = this.rndSphere.nextLong() / 2L * 2L + 1L;
		long l2 = (this.midX * l0 + this.midZ * l1) * 2512576L ^ this.worldObj.getSeed();
		this.rndSphere.setSeed(l2);
	}

}
