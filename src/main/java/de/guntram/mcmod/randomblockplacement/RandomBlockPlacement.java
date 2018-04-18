package de.guntram.mcmod.randomblockplacement;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod(modid = RandomBlockPlacement.MODID, 
        version = RandomBlockPlacement.VERSION,
	clientSideOnly = true, 
	guiFactory = "de.guntram.mcmod.randomblockplacement.GuiFactory",
	acceptedMinecraftVersions = "[1.12]",
	updateJSON="https://raw.githubusercontent.com/gbl/RandomBlockPlacement/master/versioncheck.json"
)

public class RandomBlockPlacement implements ICommand
{
    static final String MODID="randomblockplacement";
    static final String VERSION="@VERSION@";
    
    private boolean isActive;
    private int minSlot, maxSlot;
    
    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        MinecraftForge.EVENT_BUS.register(this);
        ClientCommandHandler.instance.registerCommand(this);
    }

    @EventHandler
    public void preInit(final FMLPreInitializationEvent event) {
        ConfigurationHandler confHandler = ConfigurationHandler.getInstance();
        confHandler.load(event.getSuggestedConfigurationFile());
        MinecraftForge.EVENT_BUS.register(confHandler);
        
        isActive=false;
        minSlot=maxSlot=-1;
    }
    
    @SubscribeEvent
    public void onPlayerInteract(PlayerInteractEvent.RightClickBlock e) {
        if (e.getSide() != Side.CLIENT)
            return;
        InventoryPlayer inventory = Minecraft.getMinecraft().player.inventory;
        int index=inventory.currentItem;
        if (isActive && index>=minSlot && index<=maxSlot) {
            int totalItems=0;
            for (int i=minSlot; i<=maxSlot; i++) {
                totalItems+=inventory.getStackInSlot(i).getCount();
            }
            int target=(int) (Math.random()*totalItems);
            int targetSlot=minSlot;
            while (targetSlot<maxSlot && target>=inventory.getStackInSlot(targetSlot).getCount()) {
                target-=inventory.getStackInSlot(targetSlot).getCount();
                targetSlot++;
            }
            inventory.currentItem=targetSlot;
        }
    }

    @Override
    public String getName() {
        return "rblock";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "rblock firstslot secondslot or rblock off";
    }

    @Override
    public List<String> getAliases() {
        return new ArrayList<>();
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        int first=0, second=0;
        if (args.length==1 && args[0].equals("off")) {
            isActive=false;
        }
        if (args.length>=2) {
            boolean parsed=false;
            try {
                first=Integer.parseInt(args[0]);
                second=Integer.parseInt(args[1]);
                parsed=true;
            } catch (NumberFormatException ex) {
                
            }
            if (parsed && first>=1 && second>=1 && first<=9 && second<=9) {
                isActive=true;
                minSlot=first-1;
                maxSlot=second-1;
            } else {
                isActive=false;
            }
        }
        if (isActive) {
            player.sendMessage(new TextComponentString("Randomly switching between inventory positions "+(minSlot+1)+" and "+(maxSlot+1)));
        } else {
            player.sendMessage(new TextComponentString("RandomBlockPlacement inactive"));
        }
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        return true;
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos targetPos) {
        return new ArrayList<String>();
    }

    @Override
    public boolean isUsernameIndex(String[] args, int index) {
        return false;
    }

    @Override
    public int compareTo(ICommand o) {
        return getName().compareTo(o.getName());
    }
}
