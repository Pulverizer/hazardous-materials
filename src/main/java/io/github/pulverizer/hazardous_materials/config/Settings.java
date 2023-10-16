package io.github.pulverizer.hazardous_materials.config;

import org.apache.logging.log4j.Logger;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.IOException;
import java.nio.file.Path;

//TODO - Switch to getters / setter 'load()'
public class Settings {

    // Main Settings
    public static boolean Debug = false;
    public static String LOCALE;

    //   Ammo
    public static int AmmoDetonationMultiplier;

    public static void load(Logger logger, Path configDirectory) {

        Path mainConfigPath = configDirectory.resolve("hazardous_materials.cfg");
        boolean genDefaults = false;

        if (!mainConfigPath.toFile().exists()) {
            logger.info("Generating default config...");
            genDefaults = true;
        }

        YamlConfigurationLoader mainConfigLoader = YamlConfigurationLoader.builder().path(mainConfigPath).defaultOptions(ConfigurationOptions.defaults()).build();

        try {
            ConfigurationNode mainConfigNode = mainConfigLoader.load();

            // Read in config
            Settings.LOCALE = mainConfigNode.node("Locale").getString("en");
            Settings.Debug = mainConfigNode.node("Debug").getBoolean(false);

            Settings.AmmoDetonationMultiplier = mainConfigNode.node("AmmoDetonationMultiplier").getInt(0);

            //TODO - Re-add when it doesn't break tidy configs
            if (genDefaults) {
                mainConfigLoader.save(mainConfigNode);
            }
        } catch (IOException error) {
            logger.error("Error loading config!");
            error.printStackTrace();
        }
    }
}