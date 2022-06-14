package de.guntram.mcmod.randomblockplacement.mixins;

import de.guntram.mcmod.randomblockplacement.RandomBlockPlacement;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerInteractionManager.class)
public class PlayerInteractBlockMixin {
    
    @Inject(method="interactBlock", at=@At(value="RETURN",
            target="Lnet/minecraft/client/network/ClientPlayerInteractionManager;sendSequencedPacket(Lnet/minecraft/client/world/ClientWorld;Lnet/minecraft/client/network/SequencedPacketCreator;)V"))
    public void onPlayerInteractBlockSuccessfully(ClientPlayerEntity player, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable cir) {
        RandomBlockPlacement.getInstance().onPlayerInteract();
    }
    
}
