package rtg.event;

import java.util.ArrayList;
import java.util.Random;
import java.util.WeakHashMap;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.terraingen.BiomeEvent;
import net.minecraftforge.event.terraingen.DecorateBiomeEvent;
import net.minecraftforge.event.terraingen.InitMapGenEvent;
import net.minecraftforge.event.terraingen.OreGenEvent;
import net.minecraftforge.event.terraingen.SaplingGrowTreeEvent;
import net.minecraftforge.event.terraingen.WorldTypeEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.WorldEvent;
import rtg.RTG;
import rtg.config.rtg.ConfigRTG;
import rtg.util.Acceptor;
import rtg.util.Logger;
import rtg.util.RandomUtil;
import rtg.world.WorldTypeRTG;
import rtg.world.biome.WorldChunkManagerRTG;
import rtg.world.biome.realistic.RealisticBiomeBase;
import rtg.world.gen.MapGenCavesRTG;
import rtg.world.gen.MapGenRavineRTG;
import rtg.world.gen.feature.tree.rtg.TreeRTG;
import rtg.world.gen.genlayer.RiverRemover;
import rtg.world.gen.structure.MapGenScatteredFeatureRTG;
import rtg.world.gen.structure.MapGenVillageRTG;
import cpw.mods.fml.common.eventhandler.Event.Result;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class EventManagerRTG
{

	public EventManagerRTG.LoadChunkRTG loadChunkRTG = new EventManagerRTG.LoadChunkRTG();
	public EventManagerRTG.WorldLoadRTG worldLoadRTG = new EventManagerRTG.WorldLoadRTG();
	public EventManagerRTG.WorldUnloadRTG worldUnloadRTG = new EventManagerRTG.WorldUnloadRTG();
	public EventManagerRTG.GenerateMinableRTG generateMinableRTG = new EventManagerRTG.GenerateMinableRTG();
	public EventManagerRTG.GetVillageBlockRTG getVillageBlockRTG = new EventManagerRTG.GetVillageBlockRTG();
	public EventManagerRTG.PreDecorateBiomeRTG preDecorateBiomeRTG = new EventManagerRTG.PreDecorateBiomeRTG();
	public EventManagerRTG.InitMapGenRTG initMapGenRTG = new EventManagerRTG.InitMapGenRTG();
	public EventManagerRTG.SaplingGrowTreeRTG saplingGrowTreeRTG = new EventManagerRTG.SaplingGrowTreeRTG();
	public EventManagerRTG.InitBiomeGensRTG initBiomeGensRTG = new EventManagerRTG.InitBiomeGensRTG();
	
	private RealisticBiomeBase biome = null;
	
    private WeakHashMap<Integer,Acceptor<ChunkEvent.Load>> chunkLoadEvents =
            new WeakHashMap<Integer,Acceptor<ChunkEvent.Load>> ();
    
    public EventManagerRTG()
    {

    }
    
    public void registerEventHandlers()
    {
    	MinecraftForge.EVENT_BUS.register(loadChunkRTG);
    	MinecraftForge.EVENT_BUS.register(worldLoadRTG);
    	MinecraftForge.EVENT_BUS.register(worldUnloadRTG);
    	MinecraftForge.ORE_GEN_BUS.register(generateMinableRTG);
    	MinecraftForge.TERRAIN_GEN_BUS.register(getVillageBlockRTG);
    	MinecraftForge.TERRAIN_GEN_BUS.register(preDecorateBiomeRTG);
    	MinecraftForge.TERRAIN_GEN_BUS.register(initMapGenRTG);
    	MinecraftForge.TERRAIN_GEN_BUS.register(saplingGrowTreeRTG);
    	MinecraftForge.TERRAIN_GEN_BUS.register(initBiomeGensRTG);
    }
    
    public void unRegisterEventHandlers()
    {

    	/**
    	 * The onWorldLoad and onWorldUnload handlers must always be registered.
    	 * 
    	 * MinecraftForge.EVENT_BUS.unregister(worldLoadRTG);
    	 * MinecraftForge.EVENT_BUS.unregister(worldUnloadRTG);
    	 */

    	MinecraftForge.EVENT_BUS.unregister(loadChunkRTG);
    	MinecraftForge.ORE_GEN_BUS.unregister(generateMinableRTG);
    	MinecraftForge.TERRAIN_GEN_BUS.unregister(getVillageBlockRTG);
    	MinecraftForge.TERRAIN_GEN_BUS.unregister(preDecorateBiomeRTG);
    	MinecraftForge.TERRAIN_GEN_BUS.unregister(initMapGenRTG);
    	MinecraftForge.TERRAIN_GEN_BUS.unregister(saplingGrowTreeRTG);
    	MinecraftForge.TERRAIN_GEN_BUS.unregister(initBiomeGensRTG);
    }

    public void setDimensionChunkLoadEvent(int dimension, Acceptor<ChunkEvent.Load> action) {
        chunkLoadEvents.put(dimension, action);
    }
    
    public class GenerateMinableRTG
    {
        @SubscribeEvent
        public void onGenerateMinable(OreGenEvent.GenerateMinable event) {

            switch (event.type) {
                
                case COAL:
                    if (!ConfigRTG.generateOreCoal) { event.setResult(Result.DENY); }
                    return;
                
                case IRON:
                    if (!ConfigRTG.generateOreIron) { event.setResult(Result.DENY); }
                    return;

                case REDSTONE:
                    if (!ConfigRTG.generateOreRedstone) { event.setResult(Result.DENY); }
                    return;
                
                case GOLD:
                    if (!ConfigRTG.generateOreGold) { event.setResult(Result.DENY); }
                    return;
                    
                case LAPIS:
                    if (!ConfigRTG.generateOreLapis) { event.setResult(Result.DENY); }
                    return;
                    
                case DIAMOND:
                    if (!ConfigRTG.generateOreDiamond) { event.setResult(Result.DENY); }
                    return;
                
                default:
                	return;
            }
        }
    }
    
    public class GetVillageBlockRTG
    {
        @SubscribeEvent
        public void onGetVillageBlockID(BiomeEvent.GetVillageBlockID event)
        {

            // Use event.biome, if that's null, fall back to our own copy
            if (this.isDesertVillageBiome((event.biome == null) ? RTG.eventMgr.biome.baseBiome : event.biome)) {

                Block originalBlock = event.original;

                if (originalBlock == Blocks.cobblestone || originalBlock == Blocks.planks || originalBlock == Blocks.log
                        || originalBlock == Blocks.log2 || originalBlock == Blocks.gravel) {

                    event.replacement = Blocks.sandstone;
                } else if (originalBlock == Blocks.oak_stairs || originalBlock == Blocks.stone_stairs) {

                    event.replacement = Blocks.sandstone_stairs;
                }

                // The event has to be cancelled in order to override the original block.
                if (event.replacement != null) {

                    event.setResult(Result.DENY);
                }
            }
        }
        
        @SubscribeEvent
        public void onGetVillageBlockMeta(BiomeEvent.GetVillageBlockMeta event)
        {
            boolean replaced = false;

            // Use event.biome, if that's null, fall back to our own copy
            if (this.isDesertVillageBiome((event.biome == null) ? RTG.eventMgr.biome.baseBiome : event.biome)) {

                Block originalBlock = event.original;

                if (originalBlock == Blocks.log || originalBlock == Blocks.log2 || originalBlock == Blocks.cobblestone) {

                    event.replacement = 0;
                    replaced = true;
                }

                if (originalBlock == Blocks.planks) {

                    event.replacement = 2;
                    replaced = true;
                }
            }

            // The event has to be cancelled in order to override the original block.
            if (replaced) {

                event.setResult(Result.DENY);
            }
        }
        
        private boolean isDesertVillageBiome(BiomeGenBase biome)
        {
            if(biome == null) return false;
            if (
                BiomeDictionary.isBiomeOfType(biome, Type.HOT)
                &&
                BiomeDictionary.isBiomeOfType(biome, Type.DRY)
                &&
                BiomeDictionary.isBiomeOfType(biome, Type.SANDY)
            ) {
                return true;
            }

            return false;
        }
    }
    
    public class InitBiomeGensRTG
    {
        @SubscribeEvent
        public void onBiomeGenInit(WorldTypeEvent.InitBiomeGens event) {
            
            // only handle RTG world type
            if (!event.worldType.getWorldTypeName().equalsIgnoreCase("RTG")) return;

            if (event.newBiomeGens[0].getClass().getName().contains("GenLayerEB")) return;
            boolean stripRivers = true; // This used to be a config option. Hardcoding until we have a need for the option.
            
            if (stripRivers) {
                try {
                    event.newBiomeGens = new RiverRemover().riverLess(event.originalBiomeGens);
                } catch (ClassCastException ex) {
                    //throw ex;
                    // failed attempt because the GenLayers don't end with GenLayerRiverMix
                }
            }
        }
    }
    
    public class InitMapGenRTG
    {
    	@SubscribeEvent(priority = EventPriority.LOW)
    	public void onInitMapGen(InitMapGenEvent event) {
    	    
            Logger.debug("event type = %s", event.type.toString());
            Logger.debug("event originalGen = %s", event.originalGen.toString());
    	    
    		if (event.type == InitMapGenEvent.EventType.SCATTERED_FEATURE) {
    			event.newGen = new MapGenScatteredFeatureRTG();
    		}
            else if (event.type == InitMapGenEvent.EventType.VILLAGE) {
                
                if (ConfigRTG.enableVillageModifications) {
                    event.newGen = new MapGenVillageRTG();
                }
            }
            else if (event.type == InitMapGenEvent.EventType.CAVE) {
                
                if (ConfigRTG.enableCaveModifications) {
                    
                    event.newGen = new MapGenCavesRTG();
                }
            }
            else if (event.type == InitMapGenEvent.EventType.RAVINE) {
                
                if (ConfigRTG.enableRavineModifications) {
                    
                    event.newGen = new MapGenRavineRTG();
                }
            }
    		
            Logger.debug("event newGen = %s", event.newGen.toString());
    	}
    }
    
    public class LoadChunkRTG
    {
        @SubscribeEvent
        public void onChunkLoadEvent(ChunkEvent.Load loadEvent)  {
            Integer dimension = loadEvent.world.provider.dimensionId;
            Acceptor<ChunkEvent.Load> acceptor = chunkLoadEvents.get(dimension);
            if (acceptor != null) {
                acceptor.accept(loadEvent);
            }
        }
    }
    
    public class PreDecorateBiomeRTG
    {
        @SubscribeEvent
        public void preBiomeDecorate(DecorateBiomeEvent.Pre event)
        {

            //Are we in an RTG world? Do we have RTG's chunk manager?
            if (event.world.getWorldInfo().getTerrainType() instanceof WorldTypeRTG && event.world.getWorldChunkManager() instanceof WorldChunkManagerRTG) {
                
                WorldChunkManagerRTG cmr = (WorldChunkManagerRTG) event.world.getWorldChunkManager();
                RTG.eventMgr.biome = cmr.getBiomeDataAt(event.chunkX, event.chunkZ);
            }
        }
    }
    
    public class SaplingGrowTreeRTG
    {
    	@SubscribeEvent
    	public void onSaplingGrowTree(SaplingGrowTreeEvent event)
    	{
    		
    		// Are RTG saplings enabled?
    		if (!ConfigRTG.enableRTGSaplings) {
    			return;
    		}
    		
    		// Are we in an RTG world? Do we have RTG's chunk manager?
    		if (!(event.world.getWorldInfo().getTerrainType() instanceof WorldTypeRTG) || !(event.world.getWorldChunkManager() instanceof WorldChunkManagerRTG)) {
    			return;
    		}
    		
    		Random rand = event.rand;
    		
    		// Should we generate a vanilla tree instead?
    		if (rand.nextInt(ConfigRTG.rtgTreeChance) != 0) {
    			Logger.debug("Skipping RTG tree generation.");
    			return;
    		}		
    		
    		World world = event.world;
    		int x = event.x;
    		int y = event.y;
    		int z = event.z;

    		Block saplingBlock = world.getBlock(x, y, z);
    		byte saplingMeta = (byte) saplingBlock.getDamageValue(world, x, y, z);

    		WorldChunkManagerRTG cmr = (WorldChunkManagerRTG) world.getWorldChunkManager();
    		//BiomeGenBase bgg = cmr.getBiomeGenAt(x, z);
    		BiomeGenBase bgg = world.getBiomeGenForCoords(x, z);
    		RealisticBiomeBase rb = RealisticBiomeBase.getBiome(bgg.biomeID);
    		ArrayList<TreeRTG> biomeTrees = rb.rtgTrees;
    		
    		Logger.debug("Biome = %s", rb.baseBiome.biomeName);
    		Logger.debug("Ground Sapling Block = %s", saplingBlock.getLocalizedName());
    		Logger.debug("Ground Sapling Meta = %d", saplingMeta);

    		if (biomeTrees.size() > 0) {
    			
    			// First, let's get all of the trees in this biome that match the sapling on the ground.
    			ArrayList<TreeRTG> validTrees = new ArrayList<TreeRTG>();
    			
    			for (int i = 0; i < biomeTrees.size(); i++) {
    				
    				Logger.debug("Biome Tree #%d = %s", i, biomeTrees.get(i).getClass().getName());
    				Logger.debug("Biome Tree #%d Sapling Block = %s", i, biomeTrees.get(i).saplingBlock.getClass().getName());
    				Logger.debug("Biome Tree #%d Sapling Meta = %d", i, biomeTrees.get(i).saplingMeta);
    				
    				if (saplingBlock == biomeTrees.get(i).saplingBlock && saplingMeta == biomeTrees.get(i).saplingMeta) {
    					validTrees.add(biomeTrees.get(i));
    					Logger.debug("Valid tree found!");
    				}
    			}
    			
    			// If there are valid trees, then proceed; otherwise, let's get out here.
    			if (validTrees.size() > 0) {
    				
    				// Get a random tree from the list of valid trees.
    				TreeRTG tree = validTrees.get(rand.nextInt(validTrees.size()));
    				
    				Logger.debug("Tree = %s", tree.getClass().getName());

    				// Set the trunk size if min/max values have been set.
    				if (tree.minTrunkSize > 0 && tree.maxTrunkSize > tree.minTrunkSize) {
    					tree.trunkSize = RandomUtil.getRandomInt(rand, tree.minTrunkSize, tree.maxTrunkSize);
    				}
    				
    				// Set the crown size if min/max values have been set.
    				if (tree.minCrownSize > 0 && tree.maxCrownSize > tree.minCrownSize) {
    					tree.crownSize = RandomUtil.getRandomInt(rand, tree.minCrownSize, tree.maxCrownSize);
    				}

    				/**
    				 * Set the generateFlag to what it needs to be for growing trees from saplings,
    				 * generate the tree, and then set it back to what it was before.
    				 * 
    				 * TODO: Does this affect the generation of normal RTG trees?
    				 */
    				int oldFlag = tree.generateFlag;
    				tree.generateFlag = 3;
    				boolean generated = tree.generate(world, rand, x, y, z);
    				tree.generateFlag = oldFlag;

    				if (generated) {
    					
    					// Prevent the original tree from generating.
    					event.setResult(Result.DENY);
    					
    					// Sometimes we have to remove the sapling manually because some trees grow around it, leaving the original sapling.
    					if (world.getBlock(x, y, z) == saplingBlock) {
    						world.setBlock(x, y, z, Blocks.air, (byte)0, 2);
    					}
    				}
    			}
    			else {
    				
    				Logger.debug("There are no RTG trees associated with the sapling on the ground. Generating a vanilla tree instead.");
    			}
    		}
    	}
    }
    
    public class WorldLoadRTG
    {
        @SubscribeEvent
        public void onWorldLoad(WorldEvent.Load event) {

            if (!(event.world.getWorldInfo().getTerrainType() instanceof WorldTypeRTG)) {
                
            	Logger.info("[onWorldLoad] Unregistering RTG's event handlers...");
            	RTG.eventMgr.unRegisterEventHandlers();
                Logger.info("[onWorldLoad] RTG's event handlers have been unregistered successfully.");
                
                return;
            }
            
        	Logger.info("[onWorldLoad] Re-registering RTG's event handlers...");
        	RTG.eventMgr.registerEventHandlers();
        	Logger.info("[onWorldLoad] RTG's event handlers have been re-registered successfully.");
            
            if (event.world.provider.dimensionId == 0) {
            	
                Logger.info("World Seed: %d", event.world.getSeed());
            }
        }
    }
    
    public class WorldUnloadRTG
    {
        @SubscribeEvent
        public void onWorldUnload(WorldEvent.Unload event) {

            if (event.world.getWorldInfo().getTerrainType() instanceof WorldTypeRTG) {
                
            	Logger.info("[onWorldUnload] Unregistering RTG's event handlers...");
            	RTG.eventMgr.unRegisterEventHandlers();
                Logger.info("[onWorldUnload] RTG's event handlers have been unregistered successfully.");
            }
            else {
            	
            	Logger.info("[onWorldUnload] Re-registering RTG's event handlers...");
            	RTG.eventMgr.registerEventHandlers();
            	Logger.info("[onWorldUnload] RTG's event handlers have been re-registered successfully.");
            }
        }
    }
}
