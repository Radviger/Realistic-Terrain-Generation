package rtg.world.biome.realistic.biomesoplenty;

import rtg.api.biome.BiomeConfig;
import rtg.world.gen.surface.biomesoplenty.SurfaceBOPMoor;
import rtg.world.gen.terrain.biomesoplenty.TerrainBOPMoor;
import biomesoplenty.api.biome.BOPBiomes;
import biomesoplenty.api.block.BOPBlocks;

import net.minecraft.block.Block;
import net.minecraft.world.biome.BiomeGenBase;

public class RealisticBiomeBOPMoor extends RealisticBiomeBOPBase
{	
	public static BiomeGenBase bopBiome = BOPBiomes.moor.get();
	
    public static Block topBlock = BOPBlocks.grass;
    public static Block fillerBlock = BOPBlocks.dirt;
	
	public RealisticBiomeBOPMoor(BiomeConfig config)
	{
		super(config, 
			bopBiome, BiomeGenBase.river,
			new TerrainBOPMoor(63f, 69f, 32f),
			new SurfaceBOPMoor(config, topBlock, fillerBlock)
		);
	}
}
