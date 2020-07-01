package de.guntram.mcmod.randomblockplacement;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod("randomblockplacement")

public class RandomBlockPlacement
{
    static final String MODID="randomblockplacement";
    static final String VERSION="@VERSION@";
    static KeyBinding onOff;
    
    private static RandomBlockPlacement instance;
    private boolean isActive;
    private int minSlot, maxSlot;
    
    public RandomBlockPlacement() {
        instance = this;
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.addListener(this::init);        
    }
    
    public void init(FMLCommonSetupEvent event)
    {
        MinecraftForge.EVENT_BUS.register(this);
        // ClientCommandHandler.instance.registerCommand(this);
        ClientRegistry.registerKeyBinding(onOff =
                new KeyBinding("key.randomblockplacement.toggle", 'R', "key.categories.randomblockplacement"));
    }
    
    @SubscribeEvent
    public void keyPressed(final InputEvent.KeyInputEvent e) {
        ClientPlayerEntity player = Minecraft.getInstance().player;
        if (onOff.isPressed()) {
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
        Minecraft.getInstance().player.sendStatusMessage(new TranslationTextComponent("msg.inactive"), false);
        isActive=false;
    }

    public void setActive(int first, int last) {
        minSlot = first-1;
        maxSlot = last-1;
        setActive();
    }
    
    public void setActive() {
        isActive = true;
        Minecraft.getInstance().player.sendStatusMessage(new TranslationTextComponent("msg.active", minSlot+1, maxSlot+1), false);
    }
    
   
    @SubscribeEvent
    public void onPlayerInteract(PlayerInteractEvent.RightClickBlock e) {
        if (e.getSide() != LogicalSide.CLIENT)
            return;
        PlayerInventory inventory = Minecraft.getInstance().player.inventory;
        int index=inventory.currentItem;
        if (isActive && index>=minSlot && index<=maxSlot
            && ( inventory.getStackInSlot(index).getItem() == Items.AIR
                || inventory.getStackInSlot(index).getItem() instanceof BlockItem)) {
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
            inventory.currentItem=targetSlot;
        }
    }
    
    private int getBlockCount(PlayerInventory inventory, int targetSlot) {
        ItemStack stack = inventory.getStackInSlot(targetSlot);
        Item item = stack.getItem();
        if (item instanceof BlockItem) {
            return stack.getCount();
        } else {
            return 0;
        }
    }

    @SubscribeEvent
    public void chatEvent(final ClientChatEvent e) {
        if (e.getOriginalMessage().startsWith("/rblock")) {
            String[] parms = e.getOriginalMessage().split(" ");
            if (parms.length > 1 && parms[1].equals("off")) {
                instance.setInactive();
            }
            if (parms.length > 2) {
                try {
                    int b1 = Integer.parseInt(parms[1]);
                    int b2 = Integer.parseInt(parms[2]);
                    if (b1 < 1 || b1 > 9 || b2 < 1 || b2 > 9) {
                        throw new NumberFormatException("need values between 1 and 9");
                    }
                    instance.setActive(b1, b2);
                } catch (NumberFormatException ex) {
                    Minecraft.getInstance().player.sendStatusMessage(new TranslationTextComponent("msg.cmderr"), false);
                }
            }
            e.setCanceled(true);
        }
    }
}
