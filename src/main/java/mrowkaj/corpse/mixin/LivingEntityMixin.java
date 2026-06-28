package mrowkaj.corpse.mixin;

import mrowkaj.corpse.ICorpse;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @Shadow
    public abstract @Nullable ItemEntity drop(ItemStack itemStack, boolean randomly, boolean thrownFromHand);

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void corpse$drop(CallbackInfo ci) {
        if(this instanceof ICorpse corpse && corpse.corpse$tick())
            ci.cancel();
    }

    @Inject(method = "remove", at = @At("HEAD"))
    private void corpse$dropItems(Entity.RemovalReason reason, CallbackInfo ci) {
        if(this instanceof ICorpse corpse) {
            NonNullList<ItemStack> items = corpse.corpse$getCorpseInventory();
            if(items == null)
                return;
            for(int i = 0; i < items.size(); ++i) {
                ItemStack itemStack = items.get(i);
                if (!itemStack.isEmpty()) {
                    drop(itemStack, true, false);
                    items.set(i, ItemStack.EMPTY);
                }
            }
        }
    }
}
