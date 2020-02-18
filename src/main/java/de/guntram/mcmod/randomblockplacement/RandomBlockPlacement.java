package de.guntram.mcmod.randomblockplacement;

import com.mojang.brigadier.CommandDispatcher;
import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import de.guntram.mcmod.fabrictools.ConfigurationProvider;
import static io.github.cottonmc.clientcommands.ArgumentBuilders.argument;
import static io.github.cottonmc.clientcommands.ArgumentBuilders.literal;
import io.github.cottonmc.clientcommands.ClientCommandPlugin;
import io.github.cottonmc.clientcommands.CottonClientCommandSource;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.FabricKeyBinding;
import net.fabricmc.fabric.api.client.keybinding.KeyBindingRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerInventory;

import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_R;

public class RandomBlockPlacement implements ClientModInitializer, ClientCommandPlugin
{
    static final String MODID="randomblockplacement";
    static final String MODNAME="RandomBlockPlacement";
    static final String VERSION="@VERSION@";

    private static RandomBlockPlacement instance;
    private static ConfigurationHandler confHandler;
    private boolean isActive;
    private int minSlot, maxSlot;
    private FabricKeyBinding onOff;
    
    @Override
    public void onInitializeClient() {
        instance = this;
        confHandler = ConfigurationHandler.getInstance();
        ConfigurationProvider.register(MODNAME, confHandler);
        confHandler.load(ConfigurationProvider.getSuggestedFile(MODID));
        
        isActive=false;
        minSlot=maxSlot=-1;
    }
    
    private void setKeyBindings() {
        final String category = "key.categories.randomblockplacement";
        KeyBindingRegistry.INSTANCE.addCategory(category);
        KeyBindingRegistry.INSTANCE.register(
            onOff = FabricKeyBinding.Builder
                .create(new Identifier("randomblockplacement:toggle"), InputUtil.Type.KEYSYM, GLFW_KEY_R, category)
                .build());
    }
    
    public static RandomBlockPlacement getInstance() {
        return instance;
    }
    
    public void onPlayerInteract() {
        PlayerInventory inventory = MinecraftClient.getInstance().player.inventory;
        int index=inventory.selectedSlot;
        if (isActive && index>=minSlot && index<=maxSlot) {
            int totalItems=0;
            for (int i=minSlot; i<=maxSlot; i++) {
                totalItems+=inventory.getInvStack(i).getCount();
            }
            int target=(int) (Math.random()*totalItems);
            int targetSlot=minSlot;
            while (targetSlot<maxSlot && target>=inventory.getInvStack(targetSlot).getCount()) {
                target-=inventory.getInvStack(targetSlot).getCount();
                targetSlot++;
            }
            System.out.println("selecting slot "+targetSlot);
            inventory.selectedSlot=targetSlot;
        }
    }
    
    @Override
    public void registerCommands(CommandDispatcher<CottonClientCommandSource> cd) {

        cd.register(
            literal("rblock")
                .then(
                    literal("off").executes(c->{
                        instance.isActive=false;
                        MinecraftClient.getInstance().player.addChatMessage(new TranslatableText("msg.inactive"), false);
                        return 1;
                    })
                )
                .then(
                    argument("b1", integer(1, 9)).then (
                        argument("b2", integer(1, 9)).executes(c->{
                            instance.isActive = true;
                            System.out.println("try to get b1");
                            instance.minSlot = getInteger(c, "b1");
                            System.out.println("try to get b2");
                            instance.maxSlot = getInteger(c, "b2");
                            System.out.println("got both");
                            try {
                            MinecraftClient.getInstance().player.addChatMessage(new TranslatableText("msg.active", 
                                    instance.minSlot, instance.maxSlot), false);
                            } catch (Exception ex) {
                                ex.printStackTrace(System.out);
                            }
                            instance.minSlot--;
                            instance.maxSlot--;
                            return 1;
                        })
                    )
                )
        );
    }
}
