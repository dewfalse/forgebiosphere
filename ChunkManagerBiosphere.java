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
	private float scale;
	private int scaledGrid;

	public ChunkManagerBiosphere() {
	}

	public ChunkManagerBiosphere(long par1, WorldType par3WorldType) {
		super(par1, par3WorldType);
		GenLayer[] agenlayer = GenLayer.initializeAllBiomeGenerators(par1, par3WorldType);
		agenlayer = getModdedBiomeGenerators(par3WorldType, par1, agenlayer);
		this.seed = par1;
		this.scale = 1.0F;
		this.scaledGrid = (int) (Config.GRID_SIZE * this.scale);
		this.rand = new Random(this.seed);
	}

	public ChunkManagerBiosphere(World par1World) {
		this(par1World.getSeed(), ForgeBiosphere.worldTypeBiosphere);
	}

	@Override
	public List getBiomesToSpawnIn() {
		return super.getBiomesToSpawnIn();
	}

	@Override
	public BiomeGenBase getBiomeGenAt(int par1, int par2) {
		return super.getBiomeGenAt(par1, par2);
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
		return super.getBiomesForGeneration(par1ArrayOfBiomeGenBase, par2, par3, par4, par5);
	}

	@Override
	public BiomeGenBase[] loadBlockGeneratorData(BiomeGenBase[] par1ArrayOfBiomeGenBase, int par2, int par3, int par4, int par5) {
		return super.loadBlockGeneratorData(par1ArrayOfBiomeGenBase, par2, par3, par4, par5);
	}

	@Override
	public BiomeGenBase[] getBiomeGenAt(BiomeGenBase[] par1ArrayOfBiomeGenBase, int par2, int par3, int par4, int par5, boolean par6) {
		return super.getBiomeGenAt(par1ArrayOfBiomeGenBase, par2, par3, par4, par5, par6);
	}

	@Override
	public boolean areBiomesViable(int par1, int par2, int par3, List par4List) {
		return super.areBiomesViable(par1, par2, par3, par4List);
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

	// グリッド内を均一なバイオームにするときに使おうと思ってるメソッド
	public int[] getGridCenter(int par1, int par2) {
		int[] position = new int[2];
		if (Config.NORMAL_BIOME) {
			position[0] = par1;
			position[1] = par2;
		} else {
			// chunk単位に変更
			int i = par1 >> 4;
			int j = par2 >> 4;
			i = ((i + Config.GRID_SIZE / 2) / Config.GRID_SIZE) * Config.GRID_SIZE;
			j = ((j + Config.GRID_SIZE / 2) / Config.GRID_SIZE) * Config.GRID_SIZE;
			// 通常の座標に戻す
			position[0] = i << 4;
			position[1] = j << 4;
		}
		return position;
	}
}
