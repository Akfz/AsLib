package n.paradox.aslib.register.util;

import n.paradox.aslib.register.RegisterObject;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;

public class FastBlockItem extends BlockItem implements RegisterObject {
    private final String name;
    private final String modId;
    public FastBlockItem(Block block, Settings settings, String name, String modId) {
        super(block, settings);
        this.name = name;
        this.modId = modId;
    }

    @Override
    public String getModId() {
        return modId;
    }
    @Override
    public String getRegisterName() {
        return name;
    }
}
