package nu.nerd.dvda;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;

// ----------------------------------------------------------------------------
/**
 * Configuration wrapper.
 */
public class Configuration {
    // ------------------------------------------------------------------------
    /**
     * If true, log configuration loading.
     */
    public boolean DEBUG_CONFIG;

    /**
     * Worlds where the view distance is adjusted.
     */
    public List<World> WORLDS = new ArrayList<World>();

    // ------------------------------------------------------------------------
    /**
     * Reload the configuration file.
     */
    public void reload() {
        DVDA.PLUGIN.reloadConfig();

        DEBUG_CONFIG = getConfig().getBoolean("debug.config");
        WORLDS.clear();
        for (String worldName : getConfig().getStringList("worlds")) {
            World world = Bukkit.getWorld(worldName);
            if (world == null) {
                getLogger().warning("Invalid world name: " + worldName);
            } else {
                WORLDS.add(world);
            }
        }
        DaySchedule.loadAll(MessageSink.from(getLogger()), getConfig().getConfigurationSection("schedule"));

        if (DEBUG_CONFIG) {
            getLogger().info("Configuration:");
            getLogger().info("WORLDS: " + WORLDS.stream().map(World::getName).collect(Collectors.joining(", ")));
            getLogger().info("Schedule:");
            DaySchedule.listAll(MessageSink.from(getLogger()));
        }
    }

    // ------------------------------------------------------------------------
    /**
     * Return the plugin's FileConfiguration.
     *
     * @return the plugin's FileConfiguration.
     */
    protected FileConfiguration getConfig() {
        return DVDA.PLUGIN.getConfig();
    }

    // ------------------------------------------------------------------------
    /**
     * Return the plugin's Logger.
     *
     * @return the plugin's Logger.
     */
    protected Logger getLogger() {
        return DVDA.PLUGIN.getLogger();
    }
} // class Configuration