package de.guntram.mcmod.randomblockplacement;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod("randomblockplacement")

public class RandomBlockPlacement
{
    static final String MODID="randomblockplacement";
    static final String VERSION="@VERSION@";
    static KeyMapping onOff;
    
    private static RandomBlockPlacement instance;
    private boolean isActive;
    private int minSlot, maxSlot;
    
    public RandomBlockPlacement() {
        instance = this;
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.addListener(this::init);        
        isActive=false;
        minSlot=0;
        maxSlot=8;
    }
    
    public void init(FMLCommonSetupEvent event)
    {
        MinecraftForge.EVENT_BUS.register(this);
        onOff = new KeyMapping("key.randomblockplacement.toggle", 'R', "key.categories.randomblockplacement");
    }
    
    @SubscribeEvent
    public void keyPressed(final InputEvent.Key e) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (onOff.consumeClick()) {
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
        Minecraft.getInstance().player.displayClientMessage(Component.translatable("msg.inactive"), false);
        isActive=false;
    }

    public void setActive(int first, int last) {
        minSlot = first-1;
        maxSlot = last-1;
        setActive();
    }
    
    public void setActive() {
        isActive = true;
        Minecraft.getInstance().player.displayClientMessage(Component.translatable("msg.active", minSlot+1, maxSlot+1), false);
    }
    
   
    @SubscribeEvent
    public void onPlayerInteract(PlayerInteractEvent.RightClickBlock e) {
        if (e.getSide() != LogicalSide.CLIENT)
            return;
        Inventory inventory = Minecraft.getInstance().player.getInventory();
        int index=inventory.selected;
        if (isActive && index>=minSlot && index<=maxSlot
            && ( inventory.getItem(index).getItem() == Items.AIR
                || inventory.getItem(index).getItem() instanceof BlockItem)) {
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
            inventory.selected=targetSlot;
        }
    }
    
    private int getBlockCount(Inventory inventory, int targetSlot) {
        ItemStack stack = inventory.getItem(targetSlot);
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
                    Minecraft.getInstance().player.displayClientMessage(Component.translatable("msg.cmderr"), false);
                }
            }
            e.setCanceled(true);
        }
    }
}
