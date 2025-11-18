package io.github.pulverizer.hazardous_materials;

import com.google.inject.Inject;
import io.github.pulverizer.hazardous_materials.config.Settings;
import io.github.pulverizer.hazardous_materials.listener.BlockListener;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.*;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;

import java.lang.invoke.MethodHandles;
import java.nio.file.Path;

/**
 * An example Sponge plugin.
 *
 * <p>All methods are optional -- some common event registrations are included as a jumping-off point.</p>
 */
@Plugin("example")
public class HazardousMaterials {

    private final PluginContainer container;
    private final Logger logger;

    @Inject
    @ConfigDir(sharedRoot = true)
    private Path configDirectory;

    @Inject
    HazardousMaterials(final PluginContainer container, final Logger logger) {
        this.container = container;
        this.logger = logger;
    }

    @Listener
    public void onConstructPlugin(final ConstructPluginEvent event) {
        // Perform any one-time setup

        //Sponge.eventManager().registerListeners(container, new EntityListener(), MethodHandles.lookup());
        Sponge.eventManager().registerListeners(container, new BlockListener(), MethodHandles.lookup());
    }

    @Listener
    public void onGameStart(final StartingEngineEvent<Server> event) {
        this.logger.info("Forgetting hazardous material stickers...");

        Settings.load(logger, configDirectory);
    }
/*
    @Listener
    private void onServerStarting(final StartingEngineEvent<Server> event) {
        // Any setup per-game instance. This can run multiple times when
        // using the integrated (singleplayer) server.
    }

    @Listener
    private void onServerStopping(final StoppingEngineEvent<Server> event) {
        // Any tear down per-game instance. This can run multiple times when
        // using the integrated (singleplayer) server.
    }

    @Listener
    private void onRegisterCommands(final RegisterCommandEvent<Command.Parameterized> event) {
        // Register a simple command
        // When possible, all commands should be registered within a command register event
        final Parameter.Value<String> nameParam = Parameter.string().key("name").build();
        event.register(this.container, Command.builder()
            .addParameter(nameParam)
            .permission("example.command.greet")
            .executor(ctx -> {
                final String name = ctx.requireOne(nameParam);
                ctx.sendMessage(Identity.nil(), LinearComponents.linear(
                    NamedTextColor.AQUA,
                    Component.text("Hello "),
                    Component.text(name, Style.style(TextDecoration.BOLD)),
                    Component.text("!")
                ));

                return CommandResult.success();
            })
            .build(), "greet", "wave");
    }
*/
}