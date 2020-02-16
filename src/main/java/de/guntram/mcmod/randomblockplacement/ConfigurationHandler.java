package de.guntram.mcmod.randomblockplacement;

import de.guntram.mcmod.fabrictools.ConfigChangedEvent;
import de.guntram.mcmod.fabrictools.Configuration;
import de.guntram.mcmod.fabrictools.ModConfigurationHandler;
import java.io.File;

public class ConfigurationHandler implements ModConfigurationHandler {

    private static ConfigurationHandler instance;

    private Configuration config;
    private String configFileName;

    public static ConfigurationHandler getInstance() {
        if (instance==null)
            instance=new ConfigurationHandler();
        return instance;
    }

    public void load(final File configFile) {
        if (config == null) {
            config = new Configuration(configFile);
            configFileName=configFile.getPath();
            loadConfig();
        }
    }

    @Override
    public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (event.getModID().equalsIgnoreCase(RandomBlockPlacement.MODID)) {
            loadConfig();
        }
    }
    
    private void loadConfig() {
        if (config.hasChanged())
            config.save();
    }
    
    @Override
    public Configuration getConfig() {
        return getInstance().config;
    }
    
    public static String getConfigFileName() {
        return getInstance().configFileName;
    }
}
