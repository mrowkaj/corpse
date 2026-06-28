package mrowkaj.corpse;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class CorpseMenu extends ChestMenu {
    private final ICorpse corpse;
    public CorpseMenu(final int containerId, final Inventory inventory, final Container container, ICorpse corpse) {
        super(MenuType.GENERIC_9x6, containerId, inventory, container, 6);
        this.corpse = corpse;
    }

    @Override
    public void clicked(int slotId, int buttonNum, ContainerInput clickType, Player player) {
        if (slotId < 0 || slotId >= slots.size()) {
            return;
        }

        Slot targetSlot = getSlot(slotId);
        if (!(targetSlot.container == player.getInventory())) {
            if (clickType == ContainerInput.PICKUP) {
                if (!getCarried().isEmpty()) {
                    return;
                } else {
                    corpse.corpse$getCorpseInventory().set(slotId, ItemStack.EMPTY);
                    corpse.corpse$checkEmpty();
                }
            }

            if(clickType == ContainerInput.QUICK_MOVE) {
                corpse.corpse$getCorpseInventory().set(slotId, ItemStack.EMPTY);
                corpse.corpse$checkEmpty();
            }

            if (clickType == ContainerInput.QUICK_CRAFT) {
                return;
            }

            if (clickType == ContainerInput.SWAP) {
                return;
            }
        } else {
            if (clickType == ContainerInput.QUICK_MOVE) {
                return;
            }
        }
        super.clicked(slotId, buttonNum, clickType, player);
    }
}
