package mrowkaj.corpse.mixin;

import mrowkaj.corpse.ICorpse;
import net.minecraft.core.NonNullList;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(ArmorStand.class)
public abstract class ArmorStandMixin extends LivingEntity implements ICorpse {
	@Unique
	NonNullList<ItemStack> corpse$corpseInventory = null;

	@Unique
	int corpse$ticksEmpty = 0;

	protected ArmorStandMixin(EntityType<? extends LivingEntity> type, Level level) {
		super(type, level);
	}

	@Override
	public NonNullList<ItemStack> corpse$getCorpseInventory() {
		return corpse$corpseInventory;
	}

	@Override
	public void corpse$setCorpseInventory(NonNullList<ItemStack> corpseInventory) {
		this.corpse$corpseInventory = corpseInventory;
	}

	@Override
	public boolean corpse$tick() {
		if(corpse$corpseInventory == null)
			return false;
		boolean empty = true;
		for(ItemStack stack : corpse$corpseInventory) {
			if(!stack.isEmpty()) {
				empty = false;
				break;
			}
		}
		if(empty)
			corpse$ticksEmpty++;
		if(corpse$ticksEmpty >= 6000) {
			this.discard();
			return true;
		}
		return false;
	}

	@Inject(method = "addAdditionalSaveData", at = @At("HEAD"))
	private void corpse$addCorpseInventorySaveData(ValueOutput output, CallbackInfo ci) {
		if(corpse$corpseInventory != null) {
			ValueOutput corpseOutput = output.child("Corpse");
			ContainerHelper.saveAllItems(corpseOutput, corpse$corpseInventory);
			corpseOutput.putInt("corpseTickEmpty", corpse$ticksEmpty);
		}
	}

	@Inject(method = "readAdditionalSaveData", at = @At("HEAD"))
	private void corpse$readCorpseInventorySaveData(ValueInput input, CallbackInfo ci) {
		Optional<ValueInput> optionalInput = input.child("Corpse");
		if(optionalInput.isEmpty())
			return;
		ValueInput corpseInput = optionalInput.get();
		corpse$corpseInventory = NonNullList.withSize(36 + 8, ItemStack.EMPTY);
		ContainerHelper.loadAllItems(corpseInput, corpse$corpseInventory);
		Optional<Integer> optionalTicksEmpty = corpseInput.getInt("corpseTickEmpty");
        corpse$ticksEmpty = optionalTicksEmpty.orElse(0);
	}
}