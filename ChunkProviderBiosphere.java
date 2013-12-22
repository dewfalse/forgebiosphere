package forgebiosphere;

import static net.minecraftforge.event.terraingen.InitMapGenEvent.EventType.CAVE;
import static net.minecraftforge.event.terraingen.InitMapGenEvent.EventType.MINESHAFT;
import static net.minecraftforge.event.terraingen.InitMapGenEvent.EventType.RAVINE;
import static net.minecraftforge.event.terraingen.InitMapGenEvent.EventType.SCATTERED_FEATURE;
import static net.minecraftforge.event.terraingen.InitMapGenEvent.EventType.STRONGHOLD;
import static net.minecraftforge.event.terraingen.InitMapGenEvent.EventType.VILLAGE;
import static net.minecraftforge.event.terraingen.PopulateChunkEvent.Populate.EventType.DUNGEON;
import static net.minecraftforge.event.terraingen.PopulateChunkEvent.Populate.EventType.ICE;
import static net.minecraftforge.event.terraingen.PopulateChunkEvent.Populate.EventType.LAKE;
import static net.minecraftforge.event.terraingen.PopulateChunkEvent.Populate.EventType.LAVA;

import java.util.Iterator;
import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockSand;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.util.IProgressUpdate;
import net.minecraft.util.MathHelper;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.SpawnerAnimals;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraft.world.gen.FlatGeneratorInfo;
import net.minecraft.world.gen.FlatLayerInfo;
import net.minecraft.world.gen.MapGenBase;
import net.minecraft.world.gen.MapGenCaves;
import net.minecraft.world.gen.MapGenRavine;
import net.minecraft.world.gen.NoiseGeneratorOctaves;
import net.minecraft.world.gen.feature.MapGenScatteredFeature;
import net.minecraft.world.gen.feature.WorldGenDungeons;
import net.minecraft.world.gen.feature.WorldGenLakes;
import net.minecraft.world.gen.structure.MapGenMineshaft;
import net.minecraft.world.gen.structure.MapGenStronghold;
import net.minecraft.world.gen.structure.MapGenVillage;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.Event.Result;
import net.minecraftforge.event.terraingen.ChunkProviderEvent;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import net.minecraftforge.event.terraingen.TerrainGen;

public class ChunkProviderBiosphere implements IChunkProvider {

	// ChunkProviderGenerator
	private final World worldObj;
	private final Random random;

	private NoiseGeneratorOctaves noiseGen1;
	private NoiseGeneratorOctaves noiseGen2;
	private NoiseGeneratorOctaves noiseGen3;
	private NoiseGeneratorOctaves noiseGen4;
	public NoiseGeneratorOctaves noiseGen5;
	public NoiseGeneratorOctaves noiseGen6;
	public NoiseGeneratorOctaves mobSpawnerNoise;

	private final boolean mapFeaturesEnabled;

	private double[] noiseArray;
	private double[] stoneNoise = new double[256];

	private MapGenBase caveGenerator = new MapGenCaves();
	private MapGenStronghold strongholdGenerator = new MapGenStronghold();
	private MapGenVillage villageGenerator = new MapGenVillage();
	private MapGenMineshaft mineshaftGenerator = new MapGenMineshaft();
	private MapGenScatteredFeature scatteredFeatureGenerator = new MapGenScatteredFeature();
	private MapGenBase ravineGenerator = new MapGenRavine();
	private BiomeGenBase[] biomesForGeneration;

	double[] noise1;
	double[] noise2;
	double[] noise3;
	double[] noise5;
	double[] noise6;
	float[] parabolicField;
	int[][] field_73219_j = new int[32][32];

	{
		caveGenerator = TerrainGen.getModdedMapGen(caveGenerator, CAVE);
		strongholdGenerator = (MapGenStronghold) TerrainGen.getModdedMapGen(strongholdGenerator, STRONGHOLD);
		villageGenerator = (MapGenVillage) TerrainGen.getModdedMapGen(villageGenerator, VILLAGE);
		mineshaftGenerator = (MapGenMineshaft) TerrainGen.getModdedMapGen(mineshaftGenerator, MINESHAFT);
		scatteredFeatureGenerator = (MapGenScatteredFeature) TerrainGen.getModdedMapGen(scatteredFeatureGenerator, SCATTERED_FEATURE);
		ravineGenerator = TerrainGen.getModdedMapGen(ravineGenerator, RAVINE);
	}

	// ChunkProviderFlat
	private final FlatGeneratorInfo flatWorldGenInfo;
	private final byte[] cachedBlockIDs = new byte[256];
	private final byte[] cachedBlockMetadata = new byte[256];
	private WorldGenLakes waterLakeGenerator;
	private WorldGenLakes lavaLakeGenerator;

	// Biosphere
	// 球の中心座標
	public int midX;
	public int midY;
	public int midZ;

	public double sphereRadius = 40.0D;
	public final Random rndSphere;

	// 生成する立体の形状
	enum SolidType {
		SPHERE, CUBE, COLUMN, PILLAR, PENTA_PILLAR, HEXA_PILLAR
	};

	public SolidType solid_type = SolidType.SPHERE;

	// 各種構造物の生成フラグ
	boolean generateMineshaft;
	boolean generateVillage;
	boolean generateStronghold;
	boolean generateScatteredFeature;
	boolean generateWaterLake;
	boolean generateLavaLake;
	boolean generateDungeon;
	boolean generateDecoration;

	public ChunkProviderBiosphere(World par1World, long par2, boolean par4, String par5Str) {
		this.worldObj = par1World;
		this.mapFeaturesEnabled = par4;
		this.random = new Random(par2);
		this.rndSphere = new Random(par2);
		this.noiseGen1 = new NoiseGeneratorOctaves(this.random, 16);
		this.noiseGen2 = new NoiseGeneratorOctaves(this.random, 16);
		this.noiseGen3 = new NoiseGeneratorOctaves(this.random, 8);
		this.noiseGen4 = new NoiseGeneratorOctaves(this.random, 4);
		this.noiseGen5 = new NoiseGeneratorOctaves(this.random, 10);
		this.noiseGen6 = new NoiseGeneratorOctaves(this.random, 16);
		this.mobSpawnerNoise = new NoiseGeneratorOctaves(this.random, 8);

		NoiseGeneratorOctaves[] noiseGens = { noiseGen1, noiseGen2, noiseGen3, noiseGen4, noiseGen5, noiseGen6, mobSpawnerNoise };
		noiseGens = TerrainGen.getModdedNoiseGenerators(par1World, this.random, noiseGens);
		this.noiseGen1 = noiseGens[0];
		this.noiseGen2 = noiseGens[1];
		this.noiseGen3 = noiseGens[2];
		this.noiseGen4 = noiseGens[3];
		this.noiseGen5 = noiseGens[4];
		this.noiseGen6 = noiseGens[5];
		this.mobSpawnerNoise = noiseGens[6];

		// スーパーフラットのカスタマイズを利用
		this.flatWorldGenInfo = FlatGeneratorInfo.createFlatGeneratorFromString(par5Str);
		if (par5Str.isEmpty()) {
			// カスタマイズなしならレイヤーなし
			this.flatWorldGenInfo.getFlatLayers().clear();
			// カスタマイズなしなら全て生成する
			generateMineshaft = true;
			generateVillage = true;
			generateStronghold = true;
			generateScatteredFeature = true;
			generateDecoration = true;
			generateWaterLake = true;
			generateLavaLake = true;
			generateDungeon = true;
		} else {
			generateMineshaft = this.flatWorldGenInfo.getWorldFeatures().containsKey("mineshaft");
			generateVillage = this.flatWorldGenInfo.getWorldFeatures().containsKey("village");
			generateStronghold = this.flatWorldGenInfo.getWorldFeatures().containsKey("stronghold");
			generateScatteredFeature = true;
			generateDecoration = this.flatWorldGenInfo.getWorldFeatures().containsKey("decoration");
			generateWaterLake = this.flatWorldGenInfo.getWorldFeatures().containsKey("lake");
			generateLavaLake = this.flatWorldGenInfo.getWorldFeatures().containsKey("lava_lake");
			generateDungeon = this.flatWorldGenInfo.getWorldFeatures().containsKey("dungeon");
		}

		Iterator iterator = this.flatWorldGenInfo.getFlatLayers().iterator();

		while (iterator.hasNext()) {
			FlatLayerInfo flatlayerinfo = (FlatLayerInfo) iterator.next();

			for (int j = flatlayerinfo.getMinY(); j < flatlayerinfo.getMinY() + flatlayerinfo.getLayerCount(); ++j) {
				this.cachedBlockIDs[j] = (byte) (flatlayerinfo.getFillBlock() & 255);
				this.cachedBlockMetadata[j] = (byte) flatlayerinfo.getFillBlockMeta();
			}
		}
	}

	@Override
	public boolean chunkExists(int i, int j) {
		return true;
	}

	@Override
	public Chunk provideChunk(int i, int j) {
		// 球の中心座標を計算し、球ごとに特定の乱数値を使用する
		setRand(i, j);
		BiomeGenBase biome = BiosphereBiomeManager.getRandomBiome(this.rndSphere);
		this.midY = (int) Math.round(Config.CENTER_HEIGHT_MIN + this.rndSphere.nextDouble() * (Config.CENTER_HEIGHT_MAX - Config.CENTER_HEIGHT_MIN));

		// 生成する立体の種類を変更。球のみの設定があった場合は無効
		this.solid_type = SolidType.values()[this.rndSphere.nextInt(SolidType.values().length)];
		if (Config.SPHERE_ONLY) {
			this.solid_type = SolidType.SPHERE;
		}
		this.sphereRadius = (Math.round(Config.RADIUS_MIN + this.rndSphere.nextDouble() * (Config.RADIUS_MAX - Config.RADIUS_MIN)) * 1.0F);

		// 通常の地形生成
		this.random.setSeed(i * 341873128712L + j * 132897987541L);
		byte[] abyte = new byte[32768];
		this.generateTerrain(i, j, abyte);
		this.biomesForGeneration = this.worldObj.getWorldChunkManager().loadBlockGeneratorData(this.biomesForGeneration, i * 16, j * 16, 16, 16);
		for (int bi = 0; bi < biomesForGeneration.length; ++bi) {
			biomesForGeneration[bi] = biome;
		}
		this.replaceBlocksForBiome(i, j, abyte, this.biomesForGeneration);
		this.caveGenerator.generate(this, this.worldObj, i, j, abyte);
		this.ravineGenerator.generate(this, this.worldObj, i, j, abyte);

		if (this.mapFeaturesEnabled) {
			if (generateMineshaft) {
				this.mineshaftGenerator.generate(this, this.worldObj, i, j, abyte);
			}
			if (generateVillage) {
				this.villageGenerator.generate(this, this.worldObj, i, j, abyte);
			}
			if (generateStronghold) {
				this.strongholdGenerator.generate(this, this.worldObj, i, j, abyte);
			}
			if (generateScatteredFeature) {
				this.scatteredFeatureGenerator.generate(this, this.worldObj, i, j, abyte);
			}
		}

		Chunk chunk = new Chunk(this.worldObj, abyte, i, j);
		chunk.generateSkylightMap();

		// 地形生成後に立体外のブロックを削ったりカスタマイズのレイヤーを生成したりする
		// 立体の形状により生成メソッドを切り替える。が、今のところ正方柱と円柱のみ
		switch (solid_type) {
		case CUBE:
			generateCube(i, j, chunk);
			break;
		case COLUMN:
			generateColumn(i, j, chunk);
			break;
		default:
			generateSphere(i, j, chunk);
		}

		byte[] abyte1 = chunk.getBiomeArray();

		for (int k = 0; k < abyte1.length; ++k) {
			abyte1[k] = (byte) this.biomesForGeneration[k].biomeID;
		}
		chunk.generateSkylightMap();
		return chunk;
	}

	// 円柱と橋を生成する
	private void generateColumn(int i, int j, Chunk chunk) {
		for (int k = 0; k < this.cachedBlockIDs.length; ++k) {
			int l = k >> 4;
			ExtendedBlockStorage extendedblockstorage = chunk.getBlockStorageArray()[l];

			if (extendedblockstorage == null) {
				extendedblockstorage = new ExtendedBlockStorage(k, !this.worldObj.provider.hasNoSky);
				chunk.getBlockStorageArray()[l] = extendedblockstorage;
			}

			for (int i1 = 0; i1 < 16; ++i1) {
				for (int j1 = 0; j1 < 16; ++j1) {
					int x = (i << 4) + j1;
					int z = (j << 4) + i1;

					double d = getSphereDistance(x, this.midY, z);
					boolean inside = d < this.sphereRadius;
					boolean side = d == this.sphereRadius;
					boolean outside = inside == false && side == false;
					if ((side && Math.abs(midY - k) <= this.sphereRadius) || (inside && Math.abs(midY - k) == this.sphereRadius)) {
						if (Config.BLOCK_ID != 0) {
							extendedblockstorage.setExtBlockID(j1, k & 15, i1, Config.BLOCK_ID);
							extendedblockstorage.setExtBlockMetadata(j1, k & 15, i1, 0);
						}
					} else if (outside || Math.abs(midY - k) > this.sphereRadius) {
						extendedblockstorage.setExtBlockID(j1, k & 15, i1, this.cachedBlockIDs[k] & 255);
						extendedblockstorage.setExtBlockMetadata(j1, k & 15, i1, this.cachedBlockMetadata[k]);
					}

					if (Config.CREATE_BRIDGE) {

						int dx = midX - ((i << 4) + j1);
						int dz = midZ - ((j << 4) + i1);
						if (d >= this.sphereRadius) {
							if (k == Config.BRIDGE_HEIGHT && ((-2 <= dx && dx <= 2) || (-2 <= dz && dz <= 2))) {
								extendedblockstorage.setExtBlockID(j1, k & 15, i1, (byte) Block.planks.blockID);
								extendedblockstorage.setExtBlockMetadata(j1, k & 15, i1, 0);
							} else if ((k == Config.BRIDGE_HEIGHT + 2 || k == Config.BRIDGE_HEIGHT + 3) && ((-2 <= dx && dx <= 2) || (-2 <= dz && dz <= 2))) {
								extendedblockstorage.setExtBlockID(j1, k & 15, i1, 0);
								extendedblockstorage.setExtBlockMetadata(j1, k & 15, i1, 0);
							} else if (k == (Config.BRIDGE_HEIGHT + 1) && ((-2 == dx || dx == 2) || (-2 == dz || dz == 2))) {
								extendedblockstorage.setExtBlockID(j1, k & 15, i1, (byte) Block.fence.blockID);
								extendedblockstorage.setExtBlockMetadata(j1, k & 15, i1, 0);
							} else if (k == (Config.BRIDGE_HEIGHT + 1) && ((-2 <= dx && dx <= 2) || (-2 <= dz && dz <= 2))) {
								extendedblockstorage.setExtBlockID(j1, k & 15, i1, (byte) 0);
								extendedblockstorage.setExtBlockMetadata(j1, k & 15, i1, 0);
							}
						}
					}
				}
			}
		}
	}

	// 正方柱と橋を生成する
	private void generateCube(int i, int j, Chunk chunk) {
		for (int k = 0; k < this.cachedBlockIDs.length; ++k) {
			int l = k >> 4;
			ExtendedBlockStorage extendedblockstorage = chunk.getBlockStorageArray()[l];

			if (extendedblockstorage == null) {
				extendedblockstorage = new ExtendedBlockStorage(k, !this.worldObj.provider.hasNoSky);
				chunk.getBlockStorageArray()[l] = extendedblockstorage;
			}

			for (int i1 = 0; i1 < 16; ++i1) {
				for (int j1 = 0; j1 < 16; ++j1) {

					int x = (i << 4) + j1;
					int z = (j << 4) + i1;
					// xz平面上で正方形との位置関係を計算
					double halfwidth = this.sphereRadius;
					boolean inside = (Math.abs(x - midX) < halfwidth && (Math.abs(z - midZ) < halfwidth));
					boolean side = ((Math.abs(x - midX) == halfwidth && Math.abs(z - midZ) <= halfwidth) || (Math.abs(x - midX) <= halfwidth && Math.abs(z - midZ) == halfwidth));
					boolean outside = inside == false && side == false;
					if (side && Math.abs(k - midY) <= halfwidth) {
						if (Config.BLOCK_ID != 0) {
							extendedblockstorage.setExtBlockID(j1, k & 15, i1, Config.BLOCK_ID);
							extendedblockstorage.setExtBlockMetadata(j1, k & 15, i1, 0);
						}
					} else if (inside && (Math.abs(k - midY) == halfwidth)) {
						if (Config.BLOCK_ID != 0) {
							extendedblockstorage.setExtBlockID(j1, k & 15, i1, Config.BLOCK_ID);
							extendedblockstorage.setExtBlockMetadata(j1, k & 15, i1, 0);
						}
					} else if (outside || Math.abs(k - midY) > halfwidth) {
						extendedblockstorage.setExtBlockID(j1, k & 15, i1, this.cachedBlockIDs[k] & 255);
						extendedblockstorage.setExtBlockMetadata(j1, k & 15, i1, this.cachedBlockMetadata[k]);
					}

					if (Config.CREATE_BRIDGE) {

						int dx = midX - x;
						int dz = midZ - z;
						if (outside) {
							if (k == Config.BRIDGE_HEIGHT && ((-2 <= dx && dx <= 2) || (-2 <= dz && dz <= 2))) {
								extendedblockstorage.setExtBlockID(j1, k & 15, i1, (byte) Block.planks.blockID);
								extendedblockstorage.setExtBlockMetadata(j1, k & 15, i1, 0);
							} else if ((k == Config.BRIDGE_HEIGHT + 2 || k == Config.BRIDGE_HEIGHT + 3) && ((-2 <= dx && dx <= 2) || (-2 <= dz && dz <= 2))) {
								extendedblockstorage.setExtBlockID(j1, k & 15, i1, 0);
								extendedblockstorage.setExtBlockMetadata(j1, k & 15, i1, 0);
							} else if (k == (Config.BRIDGE_HEIGHT + 1) && ((-2 == dx || dx == 2) || (-2 == dz || dz == 2))) {
								extendedblockstorage.setExtBlockID(j1, k & 15, i1, (byte) Block.fence.blockID);
								extendedblockstorage.setExtBlockMetadata(j1, k & 15, i1, 0);
							} else if (k == (Config.BRIDGE_HEIGHT + 1) && ((-2 <= dx && dx <= 2) || (-2 <= dz && dz <= 2))) {
								extendedblockstorage.setExtBlockID(j1, k & 15, i1, (byte) 0);
								extendedblockstorage.setExtBlockMetadata(j1, k & 15, i1, 0);
							}
						}
					}
				}
			}
		}
	}

	// 球と橋を生成する
	private void generateSphere(int i, int j, Chunk chunk) {
		for (int k = 0; k < this.cachedBlockIDs.length; ++k) {
			int l = k >> 4;
			ExtendedBlockStorage extendedblockstorage = chunk.getBlockStorageArray()[l];

			if (extendedblockstorage == null) {
				extendedblockstorage = new ExtendedBlockStorage(k, !this.worldObj.provider.hasNoSky);
				chunk.getBlockStorageArray()[l] = extendedblockstorage;
			}

			for (int i1 = 0; i1 < 16; ++i1) {
				for (int j1 = 0; j1 < 16; ++j1) {

					double d = getSphereDistance((i << 4) + j1, k, (j << 4) + i1);
					if (d == this.sphereRadius || (d < this.sphereRadius && k == 0)) {
						if (Config.BLOCK_ID != 0) {
							extendedblockstorage.setExtBlockID(j1, k & 15, i1, Config.BLOCK_ID);
							extendedblockstorage.setExtBlockMetadata(j1, k & 15, i1, 0);
						}
					} else if (d > this.sphereRadius) {
						extendedblockstorage.setExtBlockID(j1, k & 15, i1, this.cachedBlockIDs[k] & 255);
						extendedblockstorage.setExtBlockMetadata(j1, k & 15, i1, this.cachedBlockMetadata[k]);
					}

					if (Config.CREATE_BRIDGE) {

						int dx = midX - ((i << 4) + j1);
						int dz = midZ - ((j << 4) + i1);
						if (k == Config.BRIDGE_HEIGHT && ((-2 <= dx && dx <= 2) || (-2 <= dz && dz <= 2)) && d >= this.sphereRadius) {
							extendedblockstorage.setExtBlockID(j1, k & 15, i1, (byte) Block.planks.blockID);
							extendedblockstorage.setExtBlockMetadata(j1, k & 15, i1, 0);
						} else if ((k == Config.BRIDGE_HEIGHT + 2 || k == Config.BRIDGE_HEIGHT + 3) && ((-2 <= dx && dx <= 2) || (-2 <= dz && dz <= 2)) && d >= this.sphereRadius) {
							extendedblockstorage.setExtBlockID(j1, k & 15, i1, 0);
							extendedblockstorage.setExtBlockMetadata(j1, k & 15, i1, 0);
						} else if (k == (Config.BRIDGE_HEIGHT + 1) && ((-2 == dx || dx == 2) || (-2 == dz || dz == 2)) && d >= this.sphereRadius) {
							extendedblockstorage.setExtBlockID(j1, k & 15, i1, (byte) Block.fence.blockID);
							extendedblockstorage.setExtBlockMetadata(j1, k & 15, i1, 0);
						} else if (k == (Config.BRIDGE_HEIGHT + 1) && ((-2 <= dx && dx <= 2) || (-2 <= dz && dz <= 2)) && d >= this.sphereRadius) {
							extendedblockstorage.setExtBlockID(j1, k & 15, i1, (byte) 0);
							extendedblockstorage.setExtBlockMetadata(j1, k & 15, i1, 0);
						}
					}
				}
			}
		}
	}

	@Override
	public Chunk loadChunk(int i, int j) {
		return this.provideChunk(i, j);
	}

	// Overworldと同様の生成をするが、スーパーフラットのカスタマイズ設定で構造物等の生成を制御する
	@Override
	public void populate(IChunkProvider ichunkprovider, int i, int j) {

		BlockSand.fallInstantly = true;
		int k = i * 16;
		int l = j * 16;
		BiomeGenBase biomegenbase = this.worldObj.getBiomeGenForCoords(k + 16, l + 16);
		this.random.setSeed(this.worldObj.getSeed());
		long i1 = this.random.nextLong() / 2L * 2L + 1L;
		long j1 = this.random.nextLong() / 2L * 2L + 1L;
		this.random.setSeed(i * i1 + j * j1 ^ this.worldObj.getSeed());
		boolean flag = false;

		MinecraftForge.EVENT_BUS.post(new PopulateChunkEvent.Pre(ichunkprovider, worldObj, random, i, j, flag));

		if (this.mapFeaturesEnabled) {
			if (generateMineshaft) {
				this.mineshaftGenerator.generateStructuresInChunk(this.worldObj, this.random, i, j);
			}
			if (generateVillage) {
				flag = this.villageGenerator.generateStructuresInChunk(this.worldObj, this.random, i, j);
			}
			if (generateStronghold) {
				this.strongholdGenerator.generateStructuresInChunk(this.worldObj, this.random, i, j);
			}
			if (generateScatteredFeature) {
				this.scatteredFeatureGenerator.generateStructuresInChunk(this.worldObj, this.random, i, j);
			}
		}

		int k1;
		int l1;
		int i2;

		if (generateWaterLake) {
			if (biomegenbase != BiomeGenBase.desert && biomegenbase != BiomeGenBase.desertHills && !flag && this.random.nextInt(4) == 0 && TerrainGen.populate(ichunkprovider, worldObj, random, i, j, flag, LAKE)) {
				k1 = k + this.random.nextInt(16) + 8;
				l1 = this.random.nextInt(128);
				i2 = l + this.random.nextInt(16) + 8;
				(new WorldGenLakes(Block.waterStill.blockID)).generate(this.worldObj, this.random, k1, l1, i2);
			}
		}

		if (generateLavaLake) {
			if (TerrainGen.populate(ichunkprovider, worldObj, random, i, j, flag, LAVA) && !flag && this.random.nextInt(8) == 0) {
				k1 = k + this.random.nextInt(16) + 8;
				l1 = this.random.nextInt(this.random.nextInt(120) + 8);
				i2 = l + this.random.nextInt(16) + 8;

				if (l1 < 63 || this.random.nextInt(10) == 0) {
					(new WorldGenLakes(Block.lavaStill.blockID)).generate(this.worldObj, this.random, k1, l1, i2);
				}
			}
		}

		if (generateDungeon) {
			boolean doGen = TerrainGen.populate(ichunkprovider, worldObj, random, i, j, flag, DUNGEON);
			for (k1 = 0; doGen && k1 < 8; ++k1) {
				l1 = k + this.random.nextInt(16) + 8;
				i2 = this.random.nextInt(128);
				int j2 = l + this.random.nextInt(16) + 8;
				(new WorldGenDungeons()).generate(this.worldObj, this.random, l1, i2, j2);
			}
		}

		if (generateDecoration) {
			biomegenbase.decorate(this.worldObj, this.random, k, l);
		}
		SpawnerAnimals.performWorldGenSpawning(this.worldObj, biomegenbase, k + 8, l + 8, 16, 16, this.random);
		k += 8;
		l += 8;

		boolean doGen = TerrainGen.populate(ichunkprovider, worldObj, random, i, j, flag, ICE);
		for (k1 = 0; doGen && k1 < 16; ++k1) {
			for (l1 = 0; l1 < 16; ++l1) {
				i2 = this.worldObj.getPrecipitationHeight(k + k1, l + l1);

				if (this.worldObj.isBlockFreezable(k1 + k, i2 - 1, l1 + l)) {
					this.worldObj.setBlock(k1 + k, i2 - 1, l1 + l, Block.ice.blockID, 0, 2);
				}

				if (this.worldObj.canSnowAt(k1 + k, i2, l1 + l)) {
					this.worldObj.setBlock(k1 + k, i2, l1 + l, Block.snow.blockID, 0, 2);
				}
			}
		}

		MinecraftForge.EVENT_BUS.post(new PopulateChunkEvent.Post(ichunkprovider, worldObj, random, i, j, flag));

		BlockSand.fallInstantly = false;
	}

	@Override
	public boolean saveChunks(boolean flag, IProgressUpdate iprogressupdate) {
		return true;
	}

	@Override
	public void saveExtraData() {
	}

	@Override
	public boolean unloadQueuedChunks() {
		return false;
	}

	@Override
	public boolean canSave() {
		return true;
	}

	@Override
	public String makeString() {
		return "BiosphereLevelSource";
	}

	@Override
	public List getPossibleCreatures(EnumCreatureType enumcreaturetype, int i, int j, int k) {
		BiomeGenBase biomegenbase = this.worldObj.getBiomeGenForCoords(i, k);
		return biomegenbase == null
				? null
				: (enumcreaturetype == EnumCreatureType.monster && this.scatteredFeatureGenerator.func_143030_a(i, j, k)
						? this.scatteredFeatureGenerator.getScatteredFeatureSpawnList()
						: biomegenbase.getSpawnableList(enumcreaturetype));
	}

	@Override
	public ChunkPosition findClosestStructure(World world, String s, int i, int j, int k) {
		return "Stronghold".equals(s) && this.strongholdGenerator != null
				? this.strongholdGenerator.getNearestInstance(world, i, j, k)
				: null;
	}

	@Override
	public int getLoadedChunkCount() {
		return 0;
	}

	@Override
	public void recreateStructures(int i, int j) {
		if (this.mapFeaturesEnabled) {
			if (generateMineshaft) {
				this.mineshaftGenerator.generate(this, this.worldObj, i, j, (byte[]) null);
			}
			if (generateVillage) {
				this.villageGenerator.generate(this, this.worldObj, i, j, (byte[]) null);
			}
			if (generateStronghold) {
				this.strongholdGenerator.generate(this, this.worldObj, i, j, (byte[]) null);
			}
			if (generateScatteredFeature) {
				this.scatteredFeatureGenerator.generate(this, this.worldObj, i, j, (byte[]) null);
			}
		}
	}

	private void generateTerrain(int par1, int par2, byte[] par3ArrayOfByte) {
		byte b0 = 4;
		byte b1 = 16;
		// 水面は球中心の高さとする
		byte b2 = (byte) (midY);
		int k = b0 + 1;
		byte b3 = 17;
		int l = b0 + 1;
		this.biomesForGeneration = this.worldObj.getWorldChunkManager().getBiomesForGeneration(this.biomesForGeneration, par1 * 4 - 2, par2 * 4 - 2, k + 5, l + 5);
		this.noiseArray = this.initializeNoiseField(this.noiseArray, par1 * b0, 0, par2 * b0, k, b3, l);

		for (int i1 = 0; i1 < b0; ++i1) {
			for (int j1 = 0; j1 < b0; ++j1) {
				for (int k1 = 0; k1 < b1; ++k1) {
					double d0 = 0.125D;
					double d1 = this.noiseArray[((i1 + 0) * l + j1 + 0) * b3 + k1 + 0];
					double d2 = this.noiseArray[((i1 + 0) * l + j1 + 1) * b3 + k1 + 0];
					double d3 = this.noiseArray[((i1 + 1) * l + j1 + 0) * b3 + k1 + 0];
					double d4 = this.noiseArray[((i1 + 1) * l + j1 + 1) * b3 + k1 + 0];
					double d5 = (this.noiseArray[((i1 + 0) * l + j1 + 0) * b3 + k1 + 1] - d1) * d0;
					double d6 = (this.noiseArray[((i1 + 0) * l + j1 + 1) * b3 + k1 + 1] - d2) * d0;
					double d7 = (this.noiseArray[((i1 + 1) * l + j1 + 0) * b3 + k1 + 1] - d3) * d0;
					double d8 = (this.noiseArray[((i1 + 1) * l + j1 + 1) * b3 + k1 + 1] - d4) * d0;

					for (int l1 = 0; l1 < 8; ++l1) {
						double d9 = 0.25D;
						double d10 = d1;
						double d11 = d2;
						double d12 = (d3 - d1) * d9;
						double d13 = (d4 - d2) * d9;

						for (int i2 = 0; i2 < 4; ++i2) {
							int j2 = i2 + i1 * 4 << 11 | 0 + j1 * 4 << 7 | k1 * 8 + l1;
							short short1 = 128;
							j2 -= short1;
							double d14 = 0.25D;
							double d15 = (d11 - d10) * d14;
							double d16 = d10 - d15;

							for (int k2 = 0; k2 < 4; ++k2) {
								if ((d16 += d15) > 0.0D) {
									par3ArrayOfByte[j2 += short1] = (byte) Block.stone.blockID;
								} else if (k1 * 8 + l1 < b2) {
									par3ArrayOfByte[j2 += short1] = (byte) Block.waterStill.blockID;
								} else {
									par3ArrayOfByte[j2 += short1] = 0;
								}
							}

							d10 += d12;
							d11 += d13;
						}

						d1 += d5;
						d2 += d6;
						d3 += d7;
						d4 += d8;
					}
				}
			}
		}
	}

	private void replaceBlocksForBiome(int par1, int par2, byte[] par3ArrayOfByte, BiomeGenBase[] par4ArrayOfBiomeGenBase) {
		ChunkProviderEvent.ReplaceBiomeBlocks event = new ChunkProviderEvent.ReplaceBiomeBlocks(this, par1, par2, par3ArrayOfByte, par4ArrayOfBiomeGenBase);
		MinecraftForge.EVENT_BUS.post(event);
		if (event.getResult() == Result.DENY)
			return;

		byte b0 = (byte) (midY);
		double d0 = 0.03125D;
		this.stoneNoise = this.noiseGen4.generateNoiseOctaves(this.stoneNoise, par1 * 16, par2 * 16, 0, 16, 16, 1, d0 * 2.0D, d0 * 2.0D, d0 * 2.0D);

		for (int k = 0; k < 16; ++k) {
			for (int l = 0; l < 16; ++l) {
				BiomeGenBase biomegenbase = par4ArrayOfBiomeGenBase[l + k * 16];
				float f = biomegenbase.getFloatTemperature();
				int i1 = (int) (this.stoneNoise[k + l * 16] / 3.0D + 3.0D + this.random.nextDouble() * 0.25D);
				int j1 = -1;
				byte b1 = biomegenbase.topBlock;
				byte b2 = biomegenbase.fillerBlock;

				for (int k1 = 127; k1 >= 0; --k1) {
					int l1 = (l * 16 + k) * 128 + k1;

					byte b3 = par3ArrayOfByte[l1];

					if (b3 == 0) {
						j1 = -1;
					} else if (b3 == Block.stone.blockID) {
						if (j1 == -1) {
							if (i1 <= 0) {
								b1 = 0;
								b2 = (byte) Block.stone.blockID;
							} else if (k1 >= b0 - 4 && k1 <= b0 + 1) {
								b1 = biomegenbase.topBlock;
								b2 = biomegenbase.fillerBlock;
							}

							if (k1 < b0 && b1 == 0) {
								if (f < 0.15F) {
									b1 = (byte) Block.ice.blockID;
								} else {
									b1 = (byte) Block.waterStill.blockID;
								}
							}

							j1 = i1;

							if (k1 >= b0 - 1) {
								par3ArrayOfByte[l1] = b1;
							} else {
								par3ArrayOfByte[l1] = b2;
							}
						} else if (j1 > 0) {
							--j1;
							par3ArrayOfByte[l1] = b2;

							if (j1 == 0 && b2 == Block.sand.blockID) {
								j1 = this.random.nextInt(4);
								b2 = (byte) Block.sandStone.blockID;
							}
						}
					}
				}
			}
		}
	}

	private double[] initializeNoiseField(double[] par1ArrayOfDouble, int par2, int par3, int par4, int par5, int par6, int par7) {
		ChunkProviderEvent.InitNoiseField event = new ChunkProviderEvent.InitNoiseField(this, par1ArrayOfDouble, par2, par3, par4, par5, par6, par7);
		MinecraftForge.EVENT_BUS.post(event);
		if (event.getResult() == Result.DENY)
			return event.noisefield;

		if (par1ArrayOfDouble == null) {
			par1ArrayOfDouble = new double[par5 * par6 * par7];
		}

		if (this.parabolicField == null) {
			this.parabolicField = new float[25];

			for (int k1 = -2; k1 <= 2; ++k1) {
				for (int l1 = -2; l1 <= 2; ++l1) {
					float f = 10.0F / MathHelper.sqrt_float((k1 * k1 + l1 * l1) + 0.2F);
					this.parabolicField[k1 + 2 + (l1 + 2) * 5] = f;
				}
			}
		}

		double d0 = 684.412D;
		double d1 = 684.412D;
		this.noise5 = this.noiseGen5.generateNoiseOctaves(this.noise5, par2, par4, par5, par7, 1.121D, 1.121D, 0.5D);
		this.noise6 = this.noiseGen6.generateNoiseOctaves(this.noise6, par2, par4, par5, par7, 200.0D, 200.0D, 0.5D);
		this.noise3 = this.noiseGen3.generateNoiseOctaves(this.noise3, par2, par3, par4, par5, par6, par7, d0 / 80.0D, d1 / 160.0D, d0 / 80.0D);
		this.noise1 = this.noiseGen1.generateNoiseOctaves(this.noise1, par2, par3, par4, par5, par6, par7, d0, d1, d0);
		this.noise2 = this.noiseGen2.generateNoiseOctaves(this.noise2, par2, par3, par4, par5, par6, par7, d0, d1, d0);
		boolean flag = false;
		boolean flag1 = false;
		int i2 = 0;
		int j2 = 0;

		for (int k2 = 0; k2 < par5; ++k2) {
			for (int l2 = 0; l2 < par7; ++l2) {
				float f1 = 0.0F;
				float f2 = 0.0F;
				float f3 = 0.0F;
				byte b0 = 2;
				BiomeGenBase biomegenbase = this.biomesForGeneration[k2 + 2 + (l2 + 2) * (par5 + 5)];

				for (int i3 = -b0; i3 <= b0; ++i3) {
					for (int j3 = -b0; j3 <= b0; ++j3) {
						BiomeGenBase biomegenbase1 = this.biomesForGeneration[k2 + i3 + 2 + (l2 + j3 + 2) * (par5 + 5)];
						float f4 = this.parabolicField[i3 + 2 + (j3 + 2) * 5] / (biomegenbase1.minHeight + 2.0F);

						if (biomegenbase1.minHeight > biomegenbase.minHeight) {
							f4 /= 2.0F;
						}

						f1 += biomegenbase1.maxHeight * f4;
						f2 += biomegenbase1.minHeight * f4;
						f3 += f4;
					}
				}

				f1 /= f3;
				f2 /= f3;
				f1 = f1 * 0.9F + 0.1F;
				f2 = (f2 * 4.0F - 1.0F) / 8.0F;
				double d2 = this.noise6[j2] / 8000.0D;

				if (d2 < 0.0D) {
					d2 = -d2 * 0.3D;
				}

				d2 = d2 * 3.0D - 2.0D;

				if (d2 < 0.0D) {
					d2 /= 2.0D;

					if (d2 < -1.0D) {
						d2 = -1.0D;
					}

					d2 /= 1.4D;
					d2 /= 2.0D;
				} else {
					if (d2 > 1.0D) {
						d2 = 1.0D;
					}

					d2 /= 8.0D;
				}

				++j2;

				for (int k3 = 0; k3 < par6; ++k3) {
					double d3 = f2;
					double d4 = f1;
					d3 += d2 * 0.2D;
					d3 = d3 * par6 / 16.0D;
					double d5 = par6 / 2.0D + d3 * 4.0D;
					double d6 = 0.0D;
					double d7 = (k3 - d5) * 12.0D * 128.0D / 128.0D / d4;

					if (d7 < 0.0D) {
						d7 *= 4.0D;
					}

					double d8 = this.noise1[i2] / 512.0D;
					double d9 = this.noise2[i2] / 512.0D;
					double d10 = (this.noise3[i2] / 10.0D + 1.0D) / 2.0D;

					if (d10 < 0.0D) {
						d6 = d8;
					} else if (d10 > 1.0D) {
						d6 = d9;
					} else {
						d6 = d8 + (d9 - d8) * d10;
					}

					d6 -= d7;

					if (k3 > par6 - 4) {
						double d11 = ((k3 - (par6 - 4)) / 3.0F);
						d6 = d6 * (1.0D - d11) + -10.0D * d11;
					}

					par1ArrayOfDouble[i2] = d6;
					++i2;
				}
			}
		}

		return par1ArrayOfDouble;
	}

	// 球の中心からの距離
	private double getSphereDistance(int i, int j, int k) {
		return Math.round(getDistance(i, j, k, this.midX, this.midY, this.midZ));
	}

	// 二点間の距離
	private static final double getDistance(double d, double d1, double d2, double d3, double d4, double d5) {
		return Math.sqrt(Math.pow(d4 - d1, 2.0D) + Math.pow(d3 - d, 2.0D) + Math.pow(d5 - d2, 2.0D));
	}

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
