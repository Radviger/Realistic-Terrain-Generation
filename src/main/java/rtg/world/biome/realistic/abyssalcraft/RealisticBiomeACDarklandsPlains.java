package rtg.world.biome.realistic.abyssalcraft;

import java.util.Random;

import com.shinoow.abyssalcraft.api.biome.ACBiomes;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.ChunkPrimer;
import rtg.api.config.BiomeConfig;
import rtg.api.world.RTGWorld;
import rtg.api.world.surface.SurfaceBase;
import rtg.api.world.terrain.TerrainBase;
import rtg.api.world.terrain.heighteffect.GroundEffect;


public class RealisticBiomeACDarklandsPlains extends RealisticBiomeACBase {

    public static Biome biome = ACBiomes.darklands_plains;
    public static Biome river = Biomes.RIVER;

    public RealisticBiomeACDarklandsPlains() {

        super(biome);
    }

    @Override
    public void initConfig() {}

    @Override
    public TerrainBase initTerrain() {

        return new TerrainACDarklandsPlains();
    }

    public static class TerrainACDarklandsPlains extends TerrainBase {

        private GroundEffect groundEffect = new GroundEffect(4f);

        public TerrainACDarklandsPlains() {

        }

        @Override
        public float generateNoise(RTGWorld rtgWorld, int x, int y, float border, float river) {
            //return terrainPlains(x, y, simplex, river, 160f, 10f, 60f, 200f, 66f);
            return riverized(65f + groundEffect.added(rtgWorld, x, y), river);
        }
    }

    @Override
    public SurfaceBase initSurface() {

        return new SurfaceACDarklandsPlains(getConfig(), biome.topBlock, biome.fillerBlock);
    }

    public static class SurfaceACDarklandsPlains extends SurfaceACBase {

        public SurfaceACDarklandsPlains(BiomeConfig config, IBlockState top, IBlockState filler) {

            super(config, top, filler);
        }

        @Override
        public void paintTerrain(ChunkPrimer primer, int i, int j, int x, int z, int depth, RTGWorld rtgWorld, float[] noise, float river, Biome[] base) {

            Random rand = rtgWorld.rand();
            float c = TerrainBase.calcCliff(x, z, noise);
            boolean cliff = c > 1.4f;

            for (int k = 255; k > -1; k--) {
                Block b = primer.getBlockState(x, k, z).getBlock();
                if (b == Blocks.AIR) {
                    depth = -1;
                }
                else if (b == Blocks.STONE) {
                    depth++;

                    if (cliff) {
                        if (depth > -1 && depth < 2) {
                            if (rand.nextInt(3) == 0) {

                                primer.setBlockState(x, k, z, hcCobble());
                            }
                            else {

                                primer.setBlockState(x, k, z, hcStone());
                            }
                        }
                        else if (depth < 10) {
                            primer.setBlockState(x, k, z, hcStone());
                        }
                    }
                    else {
                        if (depth == 0 && k > 61) {
                            primer.setBlockState(x, k, z, topBlock);
                        }
                        else if (depth < 4) {
                            primer.setBlockState(x, k, z, fillerBlock);
                        }
                    }
                }
            }
        }
    }
}
