package mrowkaj.corpse;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Corpse implements ModInitializer {
	public static final String MOD_ID = "corpse";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		UseEntityCallback.EVENT.register(Corpse::openCorpseGUI);
	}

	public static InteractionResult openCorpseGUI(Player player, Level level, InteractionHand hand, Entity entity, EntityHitResult hitResult) {
		if(!(entity instanceof ArmorStand armorStand))
			return InteractionResult.PASS;

		ICorpse corpseInterface = (ICorpse) armorStand;
		if(corpseInterface.corpse$isEmpty())
			return InteractionResult.PASS;

		SimpleContainer container = new SimpleContainer(54);
		NonNullList<ItemStack> inv = corpseInterface.corpse$getCorpseInventory();
		if(inv == null)
			return InteractionResult.PASS;
		for(int i = 0; i < inv.size(); i++) {
			container.setItem(i, inv.get(i));
		}
		player.openMenu(new SimpleMenuProvider(
				(containerId, playerInventory, p) -> new CorpseMenu(containerId, playerInventory, container, corpseInterface),
				Component.literal("Corpse Items")
		));
		return InteractionResult.SUCCESS;
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
