package forgebiosphere;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiCreateFlatWorld;
import net.minecraft.client.gui.GuiCreateWorld;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.WorldChunkManager;
import net.minecraft.world.chunk.IChunkProvider;

public class WorldTypeBiosphere extends WorldType {

    /**
     * Creates a new world type, the ID is hidden and should not be referenced by modders.
     * It will automatically expand the underlying workdType array if there are no IDs left.
     *
     * @param name
     */
    public WorldTypeBiosphere(String name) {
        super(name);
    }

    @Override
	public WorldChunkManager getChunkManager(World world) {
		return new ChunkManagerBiosphere(world);
	}

	@Override
	public IChunkProvider getChunkGenerator(World world, String generatorOptions) {
		return new ChunkProviderBiosphere(world, world.getSeed(), world.getWorldInfo().isMapFeaturesEnabled(), generatorOptions);
	}

	@Override
	public int getMinimumSpawnHeight(World world) {
		return 4;
	}

	@Override
	public double getHorizon(World world) {
		return 0;
	}

	@Override
	public boolean hasVoidParticles(boolean flag) {
		return false;
	}

	@Override
	public double voidFadeMagnitude() {
		return 1.0D;
	}

	// カスタマイズボタンを表示する
	@Override
	public boolean isCustomizable() {
		return true;
	}

	// カスタマイズボタンからスーパーフラットのカスタマイズ画面を表示する
	@Override
	@SideOnly(Side.CLIENT)
	public void onCustomizeButton(Minecraft instance, GuiCreateWorld guiCreateWorld) {
		instance.displayGuiScreen(new GuiCreateFlatWorld(guiCreateWorld, guiCreateWorld.field_146334_a));
	}

}
