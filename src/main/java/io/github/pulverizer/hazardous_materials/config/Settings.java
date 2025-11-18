package io.github.pulverizer.hazardous_materials.config;

import io.leangen.geantyref.TypeToken;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.transaction.Operation;
import org.spongepowered.api.block.transaction.Operations;
import org.spongepowered.api.event.cause.entity.damage.DamageType;
import org.spongepowered.api.event.cause.entity.damage.DamageTypes;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.registry.RegistryEntry;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

//TODO - Switch to getters / setter 'load()'
public class Settings {

    // Main Settings
    public static boolean Debug = false;

    //   Ammo
    public static int AmmoDetonationMultiplier;

    private static final HashSet<HazardousItem> hazardousItems = new HashSet<>();

    public static void load(Logger logger, Path configDirectory) {

        Path mainConfigPath = configDirectory.resolve("hazardous_materials.cfg");
        boolean genDefaults = false;

        if (!mainConfigPath.toFile().exists()) {
            logger.info("Generating default config...");
            genDefaults = true;
        }

        YamlConfigurationLoader mainConfigLoader = YamlConfigurationLoader.builder().path(mainConfigPath).defaultOptions(ConfigurationOptions.defaults().serializers(Sponge.configManager().serializers())).build();

        try {
            ConfigurationNode mainConfigNode = mainConfigLoader.load();

            // Read in config
            Settings.Debug = mainConfigNode.node("Debug").getBoolean(false);

            loadHazardousItems(mainConfigNode);

            if (hazardousItems.isEmpty()) {
                HazardousItem hazardousItem = new HazardousItem(ItemTypes.TNT.get(), 4, 0.25f);

                // Add all registered DamageTypes
                DamageTypes.registry().stream().forEach(hazardousItem::addTrigger);

                // Add all registered Block Transaction Operations
                Operations.registry().stream().forEach(hazardousItem::addTrigger);

                hazardousItems.add(hazardousItem);
            }

            //TODO - Re-add when it doesn't break tidy configs
            if (genDefaults) {
                Map<String, ConfigurationNode> hazardmap = new HashMap<>();

                for (HazardousItem hazardousItem : hazardousItems) {
                    Optional<ResourceKey> itemType = ItemTypes.registry().findValueKey(hazardousItem.itemType());

                    if (itemType.isPresent()) {
                        String itemString = itemType.get().formatted();
                        ConfigurationNode configNode = mainConfigLoader.createNode();

                        configNode.node("explosiveValue").set(hazardousItem.explosionValue());
                        configNode.node("detonationChance").set(hazardousItem.detonationChance());

                        Set<String> entityTriggers = new HashSet<>();
                        for (DamageType entityTrigger : hazardousItem.entityTriggers()) {
                            DamageTypes.registry().findValueKey(entityTrigger).ifPresent(resourceKey -> entityTriggers.add(resourceKey.formatted()));
                        }
                        configNode.node("entityTriggers").setList(new TypeToken<String>() {}, List.copyOf(entityTriggers));

                        Set<String> blockTriggers = new HashSet<>();
                        for (Operation blockTrigger : hazardousItem.blockTriggers()) {
                            Operations.registry().findValueKey(blockTrigger).ifPresent(resourceKey -> blockTriggers.add(resourceKey.formatted()));
                        }
                        configNode.node("blockTriggers").setList(new TypeToken<String>() {}, List.copyOf(blockTriggers));

                        hazardmap.put(itemString, configNode);
                    }
                }

                mainConfigNode.node("hazardous-items").set(new TypeToken<Map<String, ? extends ConfigurationNode>>() {}, hazardmap);

                mainConfigLoader.save(mainConfigNode);
            }
        } catch (IOException error) {
            logger.error("IOException while loading config!");
            error.printStackTrace();
        }
    }

    private static void loadHazardousItems(ConfigurationNode mainConfigNode) throws SerializationException {
        @Nullable Map<String, ? extends ConfigurationNode> hazardousMap = mainConfigNode.node("hazardous-items").get(new TypeToken<Map<String, ? extends ConfigurationNode>>() {});

        if (hazardousMap != null) {
            for (Map.Entry<String, ? extends ConfigurationNode> entry : hazardousMap.entrySet()) {
                Optional<RegistryEntry<ItemType>> itemType = ItemTypes.registry().findEntry(ResourceKey.resolve(entry.getKey()));

                if (itemType.isPresent()) {

                    HazardousItem hazardousItem = new HazardousItem(itemType.get().value(), entry.getValue().node("explosiveValue").getFloat(0), entry.getValue().node("detonationChance").getFloat(0.5f));

                    List<String> damageTypes = entry.getValue().node("entityTriggers").getList(new TypeToken<String>() {});
                    if (damageTypes != null) {
                        damageTypes.forEach(damageType -> DamageTypes.registry().findEntry(ResourceKey.resolve(damageType)).ifPresent(key -> hazardousItem.addTrigger(key.value())));
                    }

                    List<String> blockOperations = entry.getValue().node("blockTriggers").getList(new TypeToken<String>() {});
                    if (blockOperations != null) {
                        blockOperations.forEach(blockOperation -> Operations.registry().findEntry(ResourceKey.resolve(blockOperation)).ifPresent(key -> hazardousItem.addTrigger(key.value())));
                    }

                    hazardousItems.add(hazardousItem);
                }
            }
        }
    }

    public static Set<HazardousItem> hazardousItems() {
        return Collections.unmodifiableSet(hazardousItems);

    }
}