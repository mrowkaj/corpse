package mrowkaj.corpse;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;

public interface ICorpse {
    NonNullList<ItemStack> corpse$getCorpseInventory();
    void corpse$setCorpseInventory(NonNullList<ItemStack> corpseInventory);
    boolean corpse$tick();
}
