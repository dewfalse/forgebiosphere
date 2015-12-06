package forgebiosphere;

import cpw.mods.fml.common.FMLLog;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraftforge.common.config.Configuration;
import org.apache.logging.log4j.Level;

import java.io.File;

public class Config {

	public static int WORLD_TYPE_ID = 7;

	// 球一個が占めるchunk一辺の大きさ
	public static int GRID_SIZE = 8;

	// 球の半径最小・最大値
	public static int RADIUS_MIN = 40;
	public static int RADIUS_MAX = 40;

	// 球の中心の高さ（Y座標）最小・最大値
	public static int CENTER_HEIGHT_MIN = 40;
	public static int CENTER_HEIGHT_MAX = 63;

	// 橋を作る
	public static boolean CREATE_BRIDGE = true;

	// 橋の高さ（Y座標）
	public static int BRIDGE_HEIGHT = 63;

	// 球を作るブロックID（デフォルトは20＝ガラス）
	public static Block BLOCK = Blocks.glass;

	// 球以外の立体を作るかどうか
	public static boolean SPHERE_ONLY = true;

	// グリッド内のバイオームを全て同じにするかどうか
	public static boolean NORMAL_BIOME = false;

	// 生成するバイオームリストの初期値。バイオーム名:頻度 の形式でカンマ区切りで指定する
	public static final String BIOMES_LIST_DEFAULT = "Plains:25,Forest:50,Taiga:40,Desert:25,Ice Plains:25,Jungle:25,Swampland:40,MushroomIsland:5,Ocean:15,River:10,Hell:1,Sky:1,FrozenOcean:10,FrozenRiver:10,Beach:10,Deep Ocean:10,Stone Beach:10,Cold Beach:10,Birch Forest:10,Roofed Forest:10,Cold Taiga:10,Mega Taiga:10,Savanna:10,Mesa:10,";

	// 生成するバイオームリスト
	public static String BIOMES_LIST = BIOMES_LIST_DEFAULT;

    public static void preInit(File file) {
		Configuration cfg = new Configuration(file);
		try {
			cfg.load();

//			BLOCK_ID = cfg.get(Configuration.CATEGORY_BLOCK, "BLOCK_ID", 20).getInt();
			WORLD_TYPE_ID = cfg.get(Configuration.CATEGORY_GENERAL, "WORLD_TYPE_ID", 7).getInt();
			GRID_SIZE = cfg.get(Configuration.CATEGORY_GENERAL, "GRID_SIZE", 8).getInt();
			RADIUS_MIN = cfg.get(Configuration.CATEGORY_GENERAL, "RADIUS_MIN", 40).getInt();
			RADIUS_MAX = cfg.get(Configuration.CATEGORY_GENERAL, "RADIUS_MAX", 40).getInt();
			CENTER_HEIGHT_MIN = cfg.get(Configuration.CATEGORY_GENERAL, "CENTER_HEIGHT_MIN", 40).getInt();
			CENTER_HEIGHT_MAX = cfg.get(Configuration.CATEGORY_GENERAL, "CENTER_HEIGHT_MAX", 63).getInt();
			BRIDGE_HEIGHT = cfg.get(Configuration.CATEGORY_GENERAL, "BRIDGE_HEIGHT", 63).getInt();
			CREATE_BRIDGE = cfg.get(Configuration.CATEGORY_GENERAL, "CREATE_BRIDGE", true).getBoolean(true);
			SPHERE_ONLY = cfg.get(Configuration.CATEGORY_GENERAL, "SPHERE_ONLY", true).getBoolean(true);
			NORMAL_BIOME = cfg.get(Configuration.CATEGORY_GENERAL, "NORMAL_BIOME", false).getBoolean(false);
			BIOMES_LIST = cfg.get(Configuration.CATEGORY_GENERAL, "BIOMES_LIST", BIOMES_LIST_DEFAULT).getString();
			cfg.save();
		} catch (Exception e) {
			FMLLog.log(Level.ERROR, e, ForgeBiosphere.modid + " load config exception");
		} finally {
			cfg.save();
		}

	}

}
