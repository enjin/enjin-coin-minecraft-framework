package com.enjin.enjincraft.spigot;

import com.enjin.enjincraft.spigot.dependencies.DependencyConfig;
import com.enjin.enjincraft.spigot.dependencies.DependencyManager;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;

public class EnjPlugin extends JavaPlugin {

    private SpigotBootstrap bootstrap;

    @Override
    public void onEnable() {
        DependencyManager dependencyManager = new DependencyManager(this);
        YamlConfiguration dependencyYamlConfig = loadYamlResource("dependencies.yml");
        DependencyConfig dependencyConfig = DependencyConfig.create(dependencyYamlConfig);
        dependencyManager.loadDependencies(dependencyConfig);
        bootstrap = new SpigotBootstrap(this);
        EnjinCraft.register(bootstrap);
        bootstrap.setUp();
    }

    @Override
    public void onDisable() {
        bootstrap.tearDown();
        EnjinCraft.unregister();
    }

    public YamlConfiguration loadYamlResource(String resource, Charset charset) {
        InputStream is = getResource(resource);

        if (is == null)
            return null;

        return YamlConfiguration.loadConfiguration(new InputStreamReader(is, charset));
    }

    public YamlConfiguration loadYamlResource(String resource) {
        return loadYamlResource(resource, StandardCharsets.UTF_8);
    }

    public SpigotBootstrap bootstrap() {
        return bootstrap;
    }

    public void log(Throwable throwable) {
        getLogger().log(Level.WARNING, "Exception Caught", throwable);
    }
}
