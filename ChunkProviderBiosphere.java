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

import cpw.mods.fml.common.eventhandler.Event;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSand;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Blocks;
import net.minecraft.util.IProgressUpdate;
import net.minecraft.util.MathHelper;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.SpawnerAnimals;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraft.world.gen.*;
import net.minecraft.world.gen.feature.WorldGenDungeons;
import net.minecraft.world.gen.feature.WorldGenLakes;
import net.minecraft.world.gen.structure.MapGenMineshaft;
import net.minecraft.world.gen.structure.MapGenScatteredFeature;
import net.minecraft.world.gen.structure.MapGenStronghold;
import net.minecraft.world.gen.structure.MapGenVillage;
import net.minecraftforge.common.MinecraftForge;
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
	private NoiseGeneratorPerlin noiseGen4;
	public NoiseGeneratorOctaves noiseGen5;
	public NoiseGeneratorOctaves noiseGen6;
	public NoiseGeneratorOctaves mobSpawnerNoise;

	private final boolean mapFeaturesEnabled;

	private double[] noiseArray = new double[825];
	private double[] stoneNoise = new double[256];

	private MapGenBase caveGenerator = new MapGenCaves();
	private MapGenStronghold strongholdGenerator = new MapGenStronghold();
	private MapGenVillage villageGenerator = new MapGenVillage();
	private MapGenMineshaft mineshaftGenerator = new MapGenMineshaft();
	private MapGenScatteredFeature scatteredFeatureGenerator = new MapGenScatteredFeature();
	private MapGenBase ravineGenerator = new MapGenRavine();
	private BiomeGenBase[] biomesForGeneration;

    double[] field_147427_d;
    double[] field_147428_e;
    double[] field_147425_f;
    double[] field_147426_g;
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
	private final Block[] cachedBlockIDs = new Block[256];
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
		this.noiseGen4 = new NoiseGeneratorPerlin(this.random, 4);
		this.noiseGen5 = new NoiseGeneratorOctaves(this.random, 10);
		this.noiseGen6 = new NoiseGeneratorOctaves(this.random, 16);
		this.mobSpawnerNoise = new NoiseGeneratorOctaves(this.random, 8);
        this.parabolicField = new float[25];

        for (int j = -2; j <= 2; ++j)
        {
            for (int k = -2; k <= 2; ++k)
            {
                float f = 10.0F / MathHelper.sqrt_float((float)(j * j + k * k) + 0.2F);
                this.parabolicField[j + 2 + (k + 2) * 5] = f;
            }
        }

        NoiseGenerator[] noiseGens = { noiseGen1, noiseGen2, noiseGen3, noiseGen4, noiseGen5, noiseGen6, mobSpawnerNoise };
		noiseGens = TerrainGen.getModdedNoiseGenerators(par1World, this.random, noiseGens);
		this.noiseGen1 = (NoiseGeneratorOctaves)noiseGens[0];
		this.noiseGen2 = (NoiseGeneratorOctaves)noiseGens[1];
		this.noiseGen3 = (NoiseGeneratorOctaves)noiseGens[2];
		this.noiseGen4 = (NoiseGeneratorPerlin)noiseGens[3];
		this.noiseGen5 = (NoiseGeneratorOctaves)noiseGens[4];
		this.noiseGen6 = (NoiseGeneratorOctaves)noiseGens[5];
		this.mobSpawnerNoise = (NoiseGeneratorOctaves)noiseGens[6];

		// スーパーフラットのカスタマイズを利用
		this.flatWorldGenInfo = FlatGeneratorInfo.createFlatGeneratorFromString(par5Str);
		if (par5Str.isEmpty()) {
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

        for(int i = 0; i < this.cachedBlockIDs.length; i++) {
            this.cachedBlockIDs[i] = Blocks.air;
        }
        if(!par5Str.isEmpty()) {
            while (iterator.hasNext()) {
                FlatLayerInfo flatlayerinfo = (FlatLayerInfo) iterator.next();

                for (int j = flatlayerinfo.getMinY(); j < flatlayerinfo.getMinY() + flatlayerinfo.getLayerCount(); ++j) {
                    this.cachedBlockIDs[j] = flatlayerinfo.func_151536_b();
                    this.cachedBlockMetadata[j] = (byte) flatlayerinfo.getFillBlockMeta();
                }
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
        Block[] ablock = new Block[65536];
        byte[] abyte = new byte[65536];
		this.generateTerrain(i, j, ablock);
		this.biomesForGeneration = this.worldObj.getWorldChunkManager().loadBlockGeneratorData(this.biomesForGeneration, i * 16, j * 16, 16, 16);
		for (int bi = 0; bi < biomesForGeneration.length; ++bi) {
			biomesForGeneration[bi] = biome;
		}
		this.replaceBlocksForBiome(i, j, ablock, abyte, this.biomesForGeneration);
		this.caveGenerator.func_151539_a(this, this.worldObj, i, j, ablock);
		this.ravineGenerator.func_151539_a(this, this.worldObj, i, j, ablock);

		if (this.mapFeaturesEnabled) {
			if (generateMineshaft) {
				this.mineshaftGenerator.func_151539_a(this, this.worldObj, i, j, ablock);
			}
			if (generateVillage) {
				this.villageGenerator.func_151539_a(this, this.worldObj, i, j, ablock);
			}
			if (generateStronghold) {
				this.strongholdGenerator.func_151539_a(this, this.worldObj, i, j, ablock);
			}
			if (generateScatteredFeature) {
				this.scatteredFeatureGenerator.func_151539_a(this, this.worldObj, i, j, ablock);
			}
		}

		Chunk chunk = new Chunk(this.worldObj, ablock, abyte, i, j);
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
						if (Config.BLOCK != null || Config.BLOCK != Blocks.air) {
							extendedblockstorage.func_150818_a(j1, k & 15, i1, Config.BLOCK);
							extendedblockstorage.setExtBlockMetadata(j1, k & 15, i1, 0);
						}
					} else if (outside || Math.abs(midY - k) > this.sphereRadius) {
                        if(this.cachedBlockIDs[k] != null) {
                            extendedblockstorage.func_150818_a(j1, k & 15, i1, this.cachedBlockIDs[k]);
                            extendedblockstorage.setExtBlockMetadata(j1, k & 15, i1, this.cachedBlockMetadata[k]);
                        }
					}

					if (Config.CREATE_BRIDGE) {

						int dx = midX - ((i << 4) + j1);
						int dz = midZ - ((j << 4) + i1);
						if (d >= this.sphereRadius) {
							if (k == Config.BRIDGE_HEIGHT && ((-2 <= dx && dx <= 2) || (-2 <= dz && dz <= 2))) {
								extendedblockstorage.func_150818_a(j1, k & 15, i1, Blocks.planks);
								extendedblockstorage.setExtBlockMetadata(j1, k & 15, i1, 0);
							} else if ((k == Config.BRIDGE_HEIGHT + 2 || k == Config.BRIDGE_HEIGHT + 3) && ((-2 <= dx && dx <= 2) || (-2 <= dz && dz <= 2))) {
								extendedblockstorage.func_150818_a(j1, k & 15, i1, Blocks.air);
								extendedblockstorage.setExtBlockMetadata(j1, k & 15, i1, 0);
							} else if (k == (Config.BRIDGE_HEIGHT + 1) && ((-2 == dx || dx == 2) || (-2 == dz || dz == 2))) {
								extendedblockstorage.func_150818_a(j1, k & 15, i1, Blocks.fence);
								extendedblockstorage.setExtBlockMetadata(j1, k & 15, i1, 0);
							} else if (k == (Config.BRIDGE_HEIGHT + 1) && ((-2 <= dx && dx <= 2) || (-2 <= dz && dz <= 2))) {
								extendedblockstorage.func_150818_a(j1, k & 15, i1, Blocks.air);
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
						if (Config.BLOCK != null || Config.BLOCK != Blocks.air) {
							extendedblockstorage.func_150818_a(j1, k & 15, i1, Config.BLOCK);
							extendedblockstorage.setExtBlockMetadata(j1, k & 15, i1, 0);
						}
					} else if (inside && (Math.abs(k - midY) == halfwidth)) {
						if (Config.BLOCK != null || Config.BLOCK != Blocks.air) {
							extendedblockstorage.func_150818_a(j1, k & 15, i1, Config.BLOCK);
							extendedblockstorage.setExtBlockMetadata(j1, k & 15, i1, 0);
						}
					} else if (outside || Math.abs(k - midY) > halfwidth) {
                        if(this.cachedBlockIDs[k] != null) {
                            extendedblockstorage.func_150818_a(j1, k & 15, i1, this.cachedBlockIDs[k]);
                            extendedblockstorage.setExtBlockMetadata(j1, k & 15, i1, this.cachedBlockMetadata[k]);
                        }
					}

					if (Config.CREATE_BRIDGE) {

						int dx = midX - x;
						int dz = midZ - z;
						if (outside) {
							if (k == Config.BRIDGE_HEIGHT && ((-2 <= dx && dx <= 2) || (-2 <= dz && dz <= 2))) {
								extendedblockstorage.func_150818_a(j1, k & 15, i1, Blocks.planks);
								extendedblockstorage.setExtBlockMetadata(j1, k & 15, i1, 0);
							} else if ((k == Config.BRIDGE_HEIGHT + 2 || k == Config.BRIDGE_HEIGHT + 3) && ((-2 <= dx && dx <= 2) || (-2 <= dz && dz <= 2))) {
								extendedblockstorage.func_150818_a(j1, k & 15, i1, Blocks.air);
								extendedblockstorage.setExtBlockMetadata(j1, k & 15, i1, 0);
							} else if (k == (Config.BRIDGE_HEIGHT + 1) && ((-2 == dx || dx == 2) || (-2 == dz || dz == 2))) {
								extendedblockstorage.func_150818_a(j1, k & 15, i1, Blocks.fence);
								extendedblockstorage.setExtBlockMetadata(j1, k & 15, i1, 0);
							} else if (k == (Config.BRIDGE_HEIGHT + 1) && ((-2 <= dx && dx <= 2) || (-2 <= dz && dz <= 2))) {
								extendedblockstorage.func_150818_a(j1, k & 15, i1, Blocks.air);
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
						if (Config.BLOCK != null || Config.BLOCK != Blocks.air) {
							extendedblockstorage.func_150818_a(j1, k & 15, i1, Config.BLOCK);
							extendedblockstorage.setExtBlockMetadata(j1, k & 15, i1, 0);
						}
					} else if (d > this.sphereRadius) {
                        if(this.cachedBlockIDs[k] != null) {
                            extendedblockstorage.func_150818_a(j1, k & 15, i1, this.cachedBlockIDs[k]);
                            extendedblockstorage.setExtBlockMetadata(j1, k & 15, i1, this.cachedBlockMetadata[k]);
                        }
					}

					if (Config.CREATE_BRIDGE) {

						int dx = midX - ((i << 4) + j1);
						int dz = midZ - ((j << 4) + i1);
						if (k == Config.BRIDGE_HEIGHT && ((-2 <= dx && dx <= 2) || (-2 <= dz && dz <= 2)) && d >= this.sphereRadius) {
							extendedblockstorage.func_150818_a(j1, k & 15, i1, Blocks.planks);
							extendedblockstorage.setExtBlockMetadata(j1, k & 15, i1, 0);
						} else if ((k == Config.BRIDGE_HEIGHT + 2 || k == Config.BRIDGE_HEIGHT + 3) && ((-2 <= dx && dx <= 2) || (-2 <= dz && dz <= 2)) && d >= this.sphereRadius) {
							extendedblockstorage.func_150818_a(j1, k & 15, i1, Blocks.air);
							extendedblockstorage.setExtBlockMetadata(j1, k & 15, i1, 0);
						} else if (k == (Config.BRIDGE_HEIGHT + 1) && ((-2 == dx || dx == 2) || (-2 == dz || dz == 2)) && d >= this.sphereRadius) {
							extendedblockstorage.func_150818_a(j1, k & 15, i1, Blocks.fence);
							extendedblockstorage.setExtBlockMetadata(j1, k & 15, i1, 0);
						} else if (k == (Config.BRIDGE_HEIGHT + 1) && ((-2 <= dx && dx <= 2) || (-2 <= dz && dz <= 2)) && d >= this.sphereRadius) {
							extendedblockstorage.func_150818_a(j1, k & 15, i1, Blocks.air);
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
				(new WorldGenLakes(Blocks.water)).generate(this.worldObj, this.random, k1, l1, i2);
			}
		}

		if (generateLavaLake) {
			if (TerrainGen.populate(ichunkprovider, worldObj, random, i, j, flag, LAVA) && !flag && this.random.nextInt(8) == 0) {
				k1 = k + this.random.nextInt(16) + 8;
				l1 = this.random.nextInt(this.random.nextInt(120) + 8);
				i2 = l + this.random.nextInt(16) + 8;

				if (l1 < 63 || this.random.nextInt(10) == 0) {
					(new WorldGenLakes(Blocks.lava)).generate(this.worldObj, this.random, k1, l1, i2);
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
					this.worldObj.setBlock(k1 + k, i2 - 1, l1 + l, Blocks.ice, 0, 2);
				}

				if (this.worldObj.func_147478_e(k1 + k, i2, l1 + l, true)) {
					this.worldObj.setBlock(k1 + k, i2, l1 + l, Blocks.snow_layer, 0, 2);
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
	public ChunkPosition func_147416_a(World world, String s, int i, int j, int k) {
		return "Stronghold".equals(s) && this.strongholdGenerator != null
				? this.strongholdGenerator.func_151545_a(world, i, j, k)
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
				this.mineshaftGenerator.func_151539_a(this, this.worldObj, i, j, (Block[]) null);
			}
			if (generateVillage) {
				this.villageGenerator.func_151539_a(this, this.worldObj, i, j, (Block[]) null);
			}
			if (generateStronghold) {
				this.strongholdGenerator.func_151539_a(this, this.worldObj, i, j, (Block[]) null);
			}
			if (generateScatteredFeature) {
				this.scatteredFeatureGenerator.func_151539_a(this, this.worldObj, i, j, (Block[]) null);
			}
		}
	}

	private void generateTerrain(int p_147424_1_, int p_147424_2_, Block[] p_147424_3_)
    {
        byte b0 = 63;
        this.biomesForGeneration = this.worldObj.getWorldChunkManager().getBiomesForGeneration(this.biomesForGeneration, p_147424_1_ * 4 - 2, p_147424_2_ * 4 - 2, 10, 10);
        this.initializeNoiseField(p_147424_1_ * 4, 0, p_147424_2_ * 4);

        for (int k = 0; k < 4; ++k)
        {
            int l = k * 5;
            int i1 = (k + 1) * 5;

            for (int j1 = 0; j1 < 4; ++j1)
            {
                int k1 = (l + j1) * 33;
                int l1 = (l + j1 + 1) * 33;
                int i2 = (i1 + j1) * 33;
                int j2 = (i1 + j1 + 1) * 33;

                for (int k2 = 0; k2 < 32; ++k2)
                {
                    double d0 = 0.125D;
                    double d1 = this.noiseArray[k1 + k2];
                    double d2 = this.noiseArray[l1 + k2];
                    double d3 = this.noiseArray[i2 + k2];
                    double d4 = this.noiseArray[j2 + k2];
                    double d5 = (this.noiseArray[k1 + k2 + 1] - d1) * d0;
                    double d6 = (this.noiseArray[l1 + k2 + 1] - d2) * d0;
                    double d7 = (this.noiseArray[i2 + k2 + 1] - d3) * d0;
                    double d8 = (this.noiseArray[j2 + k2 + 1] - d4) * d0;

                    for (int l2 = 0; l2 < 8; ++l2)
                    {
                        double d9 = 0.25D;
                        double d10 = d1;
                        double d11 = d2;
                        double d12 = (d3 - d1) * d9;
                        double d13 = (d4 - d2) * d9;

                        for (int i3 = 0; i3 < 4; ++i3)
                        {
                            int j3 = i3 + k * 4 << 12 | 0 + j1 * 4 << 8 | k2 * 8 + l2;
                            short short1 = 256;
                            j3 -= short1;
                            double d14 = 0.25D;
                            double d16 = (d11 - d10) * d14;
                            double d15 = d10 - d16;

                            for (int k3 = 0; k3 < 4; ++k3)
                            {
                                if ((d15 += d16) > 0.0D)
                                {
                                    p_147424_3_[j3 += short1] = Blocks.stone;
                                }
                                else if (k2 * 8 + l2 < b0)
                                {
                                    p_147424_3_[j3 += short1] = Blocks.water;
                                }
                                else
                                {
                                    p_147424_3_[j3 += short1] = null;
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


    private void replaceBlocksForBiome(int p_147422_1_, int p_147422_2_, Block[] p_147422_3_, byte[] p_147422_4_, BiomeGenBase[] p_147422_5_)
    {
        ChunkProviderEvent.ReplaceBiomeBlocks event = new ChunkProviderEvent.ReplaceBiomeBlocks(this, p_147422_1_, p_147422_2_, p_147422_3_, p_147422_4_, p_147422_5_, this.worldObj);
        MinecraftForge.EVENT_BUS.post(event);
        if (event.getResult() == Event.Result.DENY) return;

        double d0 = 0.03125D;
        this.stoneNoise = this.noiseGen4.func_151599_a(this.stoneNoise, (double)(p_147422_1_ * 16), (double)(p_147422_2_ * 16), 16, 16, d0 * 2.0D, d0 * 2.0D, 1.0D);

        for (int k = 0; k < 16; ++k)
        {
            for (int l = 0; l < 16; ++l)
            {
                BiomeGenBase biomegenbase = p_147422_5_[l + k * 16];
                biomegenbase.genTerrainBlocks(this.worldObj, this.random, p_147422_3_, p_147422_4_, p_147422_1_ * 16 + k, p_147422_2_ * 16 + l, this.stoneNoise[l + k * 16]);
            }
        }
    }

	private void initializeNoiseField(int p_147423_1_, int p_147423_2_, int p_147423_3_)
    {
        double d0 = 684.412D;
        double d1 = 684.412D;
        double d2 = 512.0D;
        double d3 = 512.0D;
        this.field_147426_g = this.noiseGen6.generateNoiseOctaves(this.field_147426_g, p_147423_1_, p_147423_3_, 5, 5, 200.0D, 200.0D, 0.5D);
        this.field_147427_d = this.noiseGen3.generateNoiseOctaves(this.field_147427_d, p_147423_1_, p_147423_2_, p_147423_3_, 5, 33, 5, 8.555150000000001D, 4.277575000000001D, 8.555150000000001D);
        this.field_147428_e = this.noiseGen1.generateNoiseOctaves(this.field_147428_e, p_147423_1_, p_147423_2_, p_147423_3_, 5, 33, 5, 684.412D, 684.412D, 684.412D);
        this.field_147425_f = this.noiseGen2.generateNoiseOctaves(this.field_147425_f, p_147423_1_, p_147423_2_, p_147423_3_, 5, 33, 5, 684.412D, 684.412D, 684.412D);
        boolean flag1 = false;
        boolean flag = false;
        int l = 0;
        int i1 = 0;
        double d4 = 8.5D;

        for (int j1 = 0; j1 < 5; ++j1)
        {
            for (int k1 = 0; k1 < 5; ++k1)
            {
                float f = 0.0F;
                float f1 = 0.0F;
                float f2 = 0.0F;
                byte b0 = 2;
                BiomeGenBase biomegenbase = this.biomesForGeneration[j1 + 2 + (k1 + 2) * 10];

                for (int l1 = -b0; l1 <= b0; ++l1)
                {
                    for (int i2 = -b0; i2 <= b0; ++i2)
                    {
                        BiomeGenBase biomegenbase1 = this.biomesForGeneration[j1 + l1 + 2 + (k1 + i2 + 2) * 10];
                        float f3 = biomegenbase1.rootHeight;
                        float f4 = biomegenbase1.heightVariation;

                        float f5 = this.parabolicField[l1 + 2 + (i2 + 2) * 5] / (f3 + 2.0F);

                        if (biomegenbase1.rootHeight > biomegenbase.rootHeight)
                        {
                            f5 /= 2.0F;
                        }

                        f += f4 * f5;
                        f1 += f3 * f5;
                        f2 += f5;
                    }
                }

                f /= f2;
                f1 /= f2;
                f = f * 0.9F + 0.1F;
                f1 = (f1 * 4.0F - 1.0F) / 8.0F;
                double d12 = this.field_147426_g[i1] / 8000.0D;

                if (d12 < 0.0D)
                {
                    d12 = -d12 * 0.3D;
                }

                d12 = d12 * 3.0D - 2.0D;

                if (d12 < 0.0D)
                {
                    d12 /= 2.0D;

                    if (d12 < -1.0D)
                    {
                        d12 = -1.0D;
                    }

                    d12 /= 1.4D;
                    d12 /= 2.0D;
                }
                else
                {
                    if (d12 > 1.0D)
                    {
                        d12 = 1.0D;
                    }

                    d12 /= 8.0D;
                }

                ++i1;
                double d13 = (double)f1;
                double d14 = (double)f;
                d13 += d12 * 0.2D;
                d13 = d13 * 8.5D / 8.0D;
                double d5 = 8.5D + d13 * 4.0D;

                for (int j2 = 0; j2 < 33; ++j2)
                {
                    double d6 = ((double)j2 - d5) * 12.0D * 128.0D / 256.0D / d14;

                    if (d6 < 0.0D)
                    {
                        d6 *= 4.0D;
                    }

                    double d7 = this.field_147428_e[l] / 512.0D;
                    double d8 = this.field_147425_f[l] / 512.0D;
                    double d9 = (this.field_147427_d[l] / 10.0D + 1.0D) / 2.0D;
                    double d10 = MathHelper.denormalizeClamp(d7, d8, d9) - d6;

                    if (j2 > 29)
                    {
                        double d11 = (double)((float)(j2 - 29) / 3.0F);
                        d10 = d10 * (1.0D - d11) + -10.0D * d11;
                    }

                    this.noiseArray[l] = d10;
                    ++l;
                }
            }
        }
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
