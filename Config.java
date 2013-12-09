package forgebiosphere;

import java.io.File;
import java.util.logging.Level;

import net.minecraftforge.common.Configuration;
import cpw.mods.fml.common.FMLLog;

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
	public static int BLOCK_ID = 20;

	// 球以外の立体を作るかどうか
	public static boolean SPHERE_ONLY = true;

	// グリッド内のバイオームを全て同じにするかどうか
	public static boolean NORMAL_BIOME = false;

	// 生成するバイオームリストの初期値。バイオーム名:頻度 の形式でカンマ区切りで指定する
	public static final String BIOMES_LIST_DEFAULT = "Plains:25,Forest:50,Taiga:40,Desert:25,Ice Plains:25,Jungle:25,Swampland:40,MushroomIsland:5,Ocean:15";

	// 生成するバイオームリスト
	public static String BIOMES_LIST = BIOMES_LIST_DEFAULT;

	public static void preInit(File file) {
		Configuration cfg = new Configuration(file);
		try {
			cfg.load();

			BLOCK_ID = cfg.get(Configuration.CATEGORY_BLOCK, "BLOCK_ID", 20).getInt();
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
			FMLLog.log(Level.SEVERE, e, ForgeBiosphere.modid + " load config exception");
		} finally {
			cfg.save();
		}

	}

}
