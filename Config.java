package forgebiosphere;

import java.io.File;
import java.util.logging.Level;

import net.minecraftforge.common.Configuration;
import cpw.mods.fml.common.FMLLog;

public class Config {

	public static int WORLD_TYPE_ID = 7;

	// �������߂�chunk��ӂ̑傫��
	public static int GRID_SIZE = 9;

	// ���̔��a�ŏ��E�ő�l
	public static int RADIUS_MIN = 40;
	public static int RADIUS_MAX = 40;

	// ���̒��S�̍����iY���W�j�ŏ��E�ő�l
	public static int CENTER_HEIGHT_MIN = 40;
	public static int CENTER_HEIGHT_MAX = 63;

	// �������
	public static boolean CREATE_BRIDGE = true;

	// ���̍����iY���W�j
	public static int BRIDGE_HEIGHT = 63;

	// �������u���b�NID�i�f�t�H���g��20���K���X�j
	public static int BLOCK_ID = 20;

	// ���ȊO�̗��̂���邩�ǂ���
	public static boolean SPHERE_ONLY = true;

	// �O���b�h���̃o�C�I�[����S�ē����ɂ��邩�ǂ���
	public static boolean NORMAL_BIOME = true;

	public static void preInit(File file) {
		Configuration cfg = new Configuration(file);
		try {
			cfg.load();

			BLOCK_ID = cfg.get(Configuration.CATEGORY_BLOCK, "BLOCK_ID", 20).getInt();
			WORLD_TYPE_ID = cfg.get(Configuration.CATEGORY_GENERAL, "WORLD_TYPE_ID", 7).getInt();
			GRID_SIZE = cfg.get(Configuration.CATEGORY_GENERAL, "GRID_SIZE", 9).getInt();
			RADIUS_MIN = cfg.get(Configuration.CATEGORY_GENERAL, "RADIUS_MIN", 40).getInt();
			RADIUS_MAX = cfg.get(Configuration.CATEGORY_GENERAL, "RADIUS_MAX", 40).getInt();
			CENTER_HEIGHT_MIN = cfg.get(Configuration.CATEGORY_GENERAL, "CENTER_HEIGHT_MIN", 40).getInt();
			CENTER_HEIGHT_MAX = cfg.get(Configuration.CATEGORY_GENERAL, "CENTER_HEIGHT_MAX", 63).getInt();
			BRIDGE_HEIGHT = cfg.get(Configuration.CATEGORY_GENERAL, "BRIDGE_HEIGHT", 63).getInt();
			CREATE_BRIDGE = cfg.get(Configuration.CATEGORY_GENERAL, "CREATE_BRIDGE", true).getBoolean(true);
			SPHERE_ONLY = cfg.get(Configuration.CATEGORY_GENERAL, "SPHERE_ONLY", true).getBoolean(true);

			// �����ł��ĂȂ��̂ŃR�����g�A�E�g
			// NORMAL_BIOME = cfg.get(Configuration.CATEGORY_GENERAL, "NORMAL_BIOME", true).getBoolean(true);
			cfg.save();
		} catch (Exception e) {
			FMLLog.log(Level.SEVERE, e, ForgeBiosphere.modid + " load config exception");
		} finally {
			cfg.save();
		}

	}

}
