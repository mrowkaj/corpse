package mrowkaj.corpse.mixin;

import mrowkaj.corpse.ICorpse;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
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
	boolean corpse$empty = false;

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
	public void corpse$checkEmpty() {
		if(corpse$empty)
			return;
		corpse$empty = true;
		for(ItemStack stack : corpse$corpseInventory) {
			if(!stack.isEmpty()) {
				corpse$empty = false;
				break;
			}
		}
		if(corpse$empty) {
			((ServerLevel)level()).sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.DIRT.defaultBlockState()), this.getX(), this.getY(), this.getZ(), 200, (double)0.3F, (double)0.3F, (double)0.3F, (double)0.15F);
			this.setCustomName(Component.empty());
			this.setCustomNameVisible(false);
		}
	}

	@Override
	public boolean corpse$tick() {
		if(!corpse$empty)
			return false;

		corpse$ticksEmpty++;
		BlockState state = getBlockStateOn();
		if(!state.is(Blocks.AIR)) {
			BlockParticleOption particleOption = new BlockParticleOption(ParticleTypes.BLOCK_CRUMBLE, state);
			ServerLevel serverLevel = (ServerLevel) level();
			serverLevel.sendParticles(
					particleOption,
					this.getX(), this.getY(), this.getZ(),
					20,
					0.25, 0.25, 0.25,
					0.15
			);
		}
		if(corpse$ticksEmpty >= 120) {
			this.setNoGravity(false);
			this.noPhysics = true;
		}
		if(corpse$ticksEmpty >= 140) {
			this.discard();
			return true;
		}
		return false;
	}

	@Override
	public boolean corpse$isEmpty() {
		return corpse$empty;
	}

	@Inject(method = "addAdditionalSaveData", at = @At("HEAD"))
	private void corpse$addCorpseInventorySaveData(ValueOutput output, CallbackInfo ci) {
		if(corpse$corpseInventory != null) {
			ValueOutput corpseOutput = output.child("Corpse");
			ContainerHelper.saveAllItems(corpseOutput, corpse$corpseInventory);
			corpseOutput.putInt("ticksEmpty", corpse$ticksEmpty);
		}
	}

	@Inject(method = "readAdditionalSaveData", at = @At("HEAD"))
	private void corpse$readCorpseInventorySaveData(ValueInput input, CallbackInfo ci) {
		Optional<ValueInput> optionalInput = input.child("Corpse");
		if(optionalInput.isEmpty())
			return;
		ValueInput corpseInput = optionalInput.get();
		corpse$ticksEmpty = corpseInput.getIntOr("ticksEmpty", 0);
		corpse$corpseInventory = NonNullList.withSize(36 + 8, ItemStack.EMPTY);
		ContainerHelper.loadAllItems(corpseInput, corpse$corpseInventory);
		corpse$checkEmpty();
	}
}