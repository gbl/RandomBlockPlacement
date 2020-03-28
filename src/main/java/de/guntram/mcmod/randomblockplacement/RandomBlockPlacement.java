package de.guntram.mcmod.randomblockplacement;

import com.mojang.brigadier.CommandDispatcher;
import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static io.github.cottonmc.clientcommands.ArgumentBuilders.argument;
import static io.github.cottonmc.clientcommands.ArgumentBuilders.literal;
import io.github.cottonmc.clientcommands.ClientCommandPlugin;
import io.github.cottonmc.clientcommands.CottonClientCommandSource;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.FabricKeyBinding;
import net.fabricmc.fabric.api.client.keybinding.KeyBindingRegistry;
import net.fabricmc.fabric.api.event.client.ClientTickCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_R;

public class RandomBlockPlacement implements ClientModInitializer, ClientCommandPlugin
{
    static final String MODID="randomblockplacement";
    static final String MODNAME="RandomBlockPlacement";
    static final String VERSION="@VERSION@";

    private static RandomBlockPlacement instance;
    private boolean isActive;
    private int minSlot, maxSlot;
    private FabricKeyBinding onOff;
    
    @Override
    public void onInitializeClient() {
        instance = this;
        setKeyBindings();
        isActive=false;
        minSlot=0;
        maxSlot=8;
    }
    
    private void setKeyBindings() {
        final String category = "key.categories.randomblockplacement";
        KeyBindingRegistry.INSTANCE.addCategory(category);
        KeyBindingRegistry.INSTANCE.register(
            onOff = FabricKeyBinding.Builder
                .create(new Identifier("randomblockplacement:toggle"), InputUtil.Type.KEYSYM, GLFW_KEY_R, category)
                .build());
        ClientTickCallback.EVENT.register(e->processKeyBinds());
    }
    
    private void processKeyBinds() {
        if (onOff.wasPressed()) {
            if (isActive) {
                instance.setInactive();
            } else {
                instance.setActive();
            }
        }
    }

    public static RandomBlockPlacement getInstance() {
        return instance;
    }

    public void setInactive() {
        MinecraftClient.getInstance().player.addMessage(new TranslatableText("msg.inactive"), false);
        isActive=false;
    }

    public void setActive(int first, int last) {
        minSlot = first-1;
        maxSlot = last-1;
        setActive();
    }
    
    public void setActive() {
        isActive = true;
        MinecraftClient.getInstance().player.addMessage(new TranslatableText("msg.active", minSlot+1, maxSlot+1), false);
    }

    public void onPlayerInteract() {
        PlayerInventory inventory = MinecraftClient.getInstance().player.inventory;
        int index=inventory.selectedSlot;
        if (isActive && index>=minSlot && index<=maxSlot
            && ( inventory.getStack(index).getItem() == Items.AIR
                || inventory.getStack(index).getItem() instanceof BlockItem)) {
            int totalBlocks=0;
            for (int i=minSlot; i<=maxSlot; i++) {
                totalBlocks+=getBlockCount(inventory, i);
            }
            int targetCount=(int) (Math.random()*totalBlocks);
            int targetSlot=minSlot;
            while (targetSlot<maxSlot && targetCount>=getBlockCount(inventory, targetSlot)) {
                targetCount-=getBlockCount(inventory, targetSlot);
                targetSlot++;
            }
            inventory.selectedSlot=targetSlot;
        }
    }
    
    private int getBlockCount(PlayerInventory inventory, int targetSlot) {
        ItemStack stack = inventory.getStack(targetSlot);
        Item item = stack.getItem();
        if (item instanceof BlockItem) {
            return stack.getCount();
        } else {
            return 0;
        }
    }
    
    @Override
    public void registerCommands(CommandDispatcher<CottonClientCommandSource> cd) {

        cd.register(
            literal("rblock")
                .then(
                    literal("off").executes(c->{
                        instance.setInactive();
                        return 1;
                    })
                )
                .then(
                    argument("b1", integer(1, 9)).then (
                        argument("b2", integer(1, 9)).executes(c->{
                            instance.setActive(getInteger(c, "b1"), getInteger(c, "b2"));
                            return 1;
                        })
                    )
                )
        );
    }
}
