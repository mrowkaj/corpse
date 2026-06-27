package mrowkaj.corpse.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.authlib.GameProfile;
import mrowkaj.corpse.ICorpse;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Player.class)
public abstract class PlayerMixin extends Avatar implements ContainerUser {
    @Shadow
    public abstract GameProfile getGameProfile();

    @Shadow
    @Final
    private GameProfile gameProfile;

    protected PlayerMixin(EntityType<? extends LivingEntity> type, Level level) {
        super(type, level);
    }
    @WrapOperation(method = "dropEquipment",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Inventory;dropAll()V"))
    private void corpse$createCorpse(Inventory instance, Operation<Void> original) {
        Level level = level();
        if(level.isClientSide()) {
            original.call(instance);
            return;
        }
        double position = this.getY();
        if(position <= -64) {
            position = 321.0;
            BlockPos pos = new BlockPos((int) this.getX(), 319, (int) this.getZ());
            boolean found = false;
            while (pos.getY() >= -64) {
                if(!level.getBlockState(pos).is(Blocks.AIR)) {
                    found = true;
                    break;
                }
                pos = pos.below();
                position--;
            }
            if(!found)
                position = 70.0;
        }
        ArmorStand corpse = new ArmorStand(level, this.getX(), position, this.getZ());
        corpse.setNoGravity(true);
        corpse.setShowArms(true);
        corpse.setCustomName(Component.literal(gameProfile.name()));
        corpse.setCustomNameVisible(true);
        corpse.setInvulnerable(true);
        corpse.setYRot(getYRot());

        ItemStack headStack = new ItemStack(Items.PLAYER_HEAD);

        GameProfile profile = this.getGameProfile();

        ResolvableProfile resolvableProfile = ResolvableProfile.createResolved(profile);

        headStack.set(DataComponents.PROFILE, resolvableProfile);
        corpse.setItemSlot(EquipmentSlot.HEAD, headStack);

        ICorpse corpseInventory = (ICorpse)corpse;
        NonNullList<ItemStack> corpseInventoryStacks = NonNullList.withSize(36 + 8, ItemStack.EMPTY);
        corpseInventory.corpse$setCorpseInventory(corpseInventoryStacks);

        int index = 0;
        for(ItemStack stack : instance) {
            corpseInventoryStacks.set(index, stack.copy());
            index++;
        }
        level.addFreshEntity(corpse);
    }
}
