package rtg.api.world.deco.collection;

import java.util.ArrayList;

import net.minecraft.block.state.IBlockState;
import rtg.api.config.BiomeConfig;
import rtg.api.world.deco.DecoBase;
import rtg.api.world.gen.feature.tree.rtg.TreeRTG;


/**
 * @author WhichOnesPink
 */
// TODO: [1.12] The DecoCollection* classes should be removed and replaced by utility methods that return a Collection<DecoBase> that
//              are added the same as in IRealisticBiome#addDeco
@Deprecated
public class DecoCollectionBase {

    public ArrayList<DecoBase> decos;
    public ArrayList<TreeRTG> rtgTrees;
    protected BiomeConfig config;

    public DecoCollectionBase(BiomeConfig config) {

        this.config = config;
        this.decos = new ArrayList<>();
        this.rtgTrees = new ArrayList<>();
    }

    public DecoCollectionBase addDeco(DecoBase deco) {

        if (!deco.properlyDefined()) {
            throw new RuntimeException();
        }
        this.decos.add(deco);
        return this;
    }

    public DecoCollectionBase addDeco(DecoBase deco, boolean allowed) {

        if (allowed) {
            if (!deco.properlyDefined()) {
                throw new RuntimeException();
            }
            this.decos.add(deco);
        }
        return this;
    }

    /**
     * Adds a tree to the list of RTG trees associated with this collection.
     * The 'allowed' parameter allows us to pass biome config booleans dynamically when configuring the trees in the collection.
     *
     * @param tree
     * @param allowed
     */
    public void addTree(TreeRTG tree, boolean allowed) {

        if (allowed) {

            this.rtgTrees.add(tree);
        }
    }

    /**
     * Convenience method for addTree() where 'allowed' is assumed to be true.
     *
     * @param tree
     */
    public void addTree(TreeRTG tree) {

        this.addTree(tree, true);
    }

    public ArrayList<IBlockState> treeLogs() {

        ArrayList<IBlockState> logBlocks = new ArrayList<>();

        for (TreeRTG rtgTree : rtgTrees) {
            logBlocks.add(rtgTree.getLogBlock());
        }

        return logBlocks;
    }

    public ArrayList<IBlockState> treeLeaves() {

        ArrayList<IBlockState> leafBlocks = new ArrayList<>();

        for (TreeRTG rtgTree : rtgTrees) {
            leafBlocks.add(rtgTree.getLeavesBlock());
        }

        return leafBlocks;
    }
}
