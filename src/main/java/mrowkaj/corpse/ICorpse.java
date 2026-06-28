package mrowkaj.corpse;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;

public interface ICorpse {
    NonNullList<ItemStack> corpse$getCorpseInventory();
    void corpse$setCorpseInventory(NonNullList<ItemStack> corpseInventory);
    void corpse$checkEmpty();
    boolean corpse$tick();
    boolean corpse$isEmpty();
}
