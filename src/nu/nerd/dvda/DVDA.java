package nu.nerd.dvda;

import java.io.File;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.Arrays;
import java.util.Iterator;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

// ----------------------------------------------------------------------------
/**
 * Dynamic View Distance Adjustment plugin class.
 *
 * A synchronously repeating task updates the Spigot configuration according to
 * a schedule.
 */
public class DVDA extends JavaPlugin {
    // ------------------------------------------------------------------------
    /**
     * Plugin instance.
     */
    public static DVDA PLUGIN;

    /**
     * Configuration as a singleton.
     */
    public static Configuration CONFIG = new Configuration();

    // ------------------------------------------------------------------------
    /**
     * @see org.bukkit.plugin.java.JavaPlugin#onCommand(org.bukkit.command.CommandSender,
     *      org.bukkit.command.Command, java.lang.String, java.lang.String[])
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase(getName())) {
            if (args.length == 1 && args[0].equalsIgnoreCase("help")) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6Usage: &e/dvda &f[&ehelp&f|&ereload&f|&elist-all&f]"));
                return true;
            } else if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
                CONFIG.reload();
                sender.sendMessage(ChatColor.GOLD + getName() + " configuration reloaded.");
                return true;
            } else if (args.length == 1 && args[0].equalsIgnoreCase("list-all")) {
                getLogger().info(ChatColor.GOLD + "Schedule:");
                DaySchedule.listAll(MessageSink.from(sender));
                return true;
            }
        } else if (command.getName().equalsIgnoreCase("view-distance")) {
            StringBuilder msg = new StringBuilder();
            msg.append(ChatColor.GOLD.toString()).append("Current view distance in adjusted worlds: ");
            String sep = "";
            for (World world : CONFIG.WORLDS) {
                msg.append(sep);
                msg.append(ChatColor.YELLOW.toString()).append(world.getName());
                msg.append(ChatColor.WHITE.toString()).append(": ");
                msg.append(getViewDistance(world)).append(ChatColor.GOLD.toString());
                sep = ", ";
            }
            sender.sendMessage(msg.toString());
            ViewDistanceChange change = getChange(_startTime, Instant.now());
            if (change != null) {
                sender.sendMessage(ChatColor.GOLD + "Pending change from " +
                                   ChatColor.YELLOW + change.getTime() +
                                   ChatColor.WHITE + ": " + change.getViewDistance());
            }
            return true;
        }

        sender.sendMessage(ChatColor.RED + "Try /dvda help.");
        return true;
    }

    // ------------------------------------------------------------------------
    /**
     * @see org.bukkit.plugin.java.JavaPlugin#onEnable()
     *
     *      When the plugin is enabled, take note of the current time so that we
     *      can work out what view distance adjustment fell due before the next
     *      restart.
     */
    @Override
    public void onEnable() {
        PLUGIN = this;
        _startTime = Instant.now();

        saveDefaultConfig();
        CONFIG.reload();
    }

    // ------------------------------------------------------------------------
    /**
     * @see org.bukkit.plugin.java.JavaPlugin#onDisable()
     */
    @Override
    public void onDisable() {
        ViewDistanceChange change = getChange(_startTime, Instant.now());
        if (change != null) {
            applyChange(change);
        }
    }

    // ------------------------------------------------------------------------
    /**
     * Apply the specified {@link ViewDistanceChange} to the Spigot
     * configuration.
     */
    private void applyChange(ViewDistanceChange change) {
        getLogger().info("Setting the view distance to " + change.getViewDistance());
        YamlConfiguration config = Bukkit.getServer().spigot().getConfig();
        try {
            File serverDir = getDataFolder().getParentFile().getParentFile();
            File configFile = new File(serverDir, "spigot.yml");
            File backupFile = new File(serverDir, "spigot.yml.dvda-backup");
            if (!backupFile.exists()) {
                configFile.renameTo(backupFile);
                config.save(configFile);
            }

            for (World world : CONFIG.WORLDS) {
                config.set("world-settings." + world.getName() + ".view-distance", change.getViewDistance());
            }
            config.save(configFile);

        } catch (Exception ex) {
            getLogger().info("Exception updating view distance: " + ex.getMessage());
        }
    }

    // ------------------------------------------------------------------------
    /**
     * Apply the last view distance change between the specified Instants.
     */
    private static ViewDistanceChange getChange(Instant startTime, Instant endTime) {
        ZonedDateTime zonedStartTime = startTime.atZone(ZoneId.systemDefault());
        ZonedDateTime zonedEndTime = endTime.atZone(ZoneId.systemDefault());
        LocalDate endDate = LocalDate.from(zonedEndTime);
        DayOfWeek startDay = zonedStartTime.getDayOfWeek();
        DayOfWeek endDay = zonedEndTime.getDayOfWeek();
        DaySchedule endSchedule = DaySchedule.of(endDay);
        ViewDistanceChange change = findLastChange(endSchedule, endDate, zonedStartTime, zonedEndTime);
        if (change == null && startDay != endDay) {
            DaySchedule startSchedule = DaySchedule.of(startDay);
            LocalDate startDate = LocalDate.from(zonedStartTime);
            change = findLastChange(startSchedule, startDate, zonedStartTime, zonedEndTime);
        }
        return change;
    }

    // ------------------------------------------------------------------------
    /**
     * Find the last entry in the schedule that falls between the start and end
     * time, or null if there is no matching entry.
     */
    private static ViewDistanceChange findLastChange(DaySchedule schedule, LocalDate date,
                                                     ZonedDateTime startTime, ZonedDateTime endTime) {
        Iterator<ViewDistanceChange> it = schedule.reversed();
        while (it.hasNext()) {
            ViewDistanceChange entry = it.next();
            ZonedDateTime entryTime = ZonedDateTime.of(date, entry.getTime(), ZoneId.systemDefault());
            if (entryTime.isBefore(endTime)) {
                if (entryTime.isAfter(startTime)) {
                    return entry;
                }
                break;
            }
        }
        return null;
    }

    // ------------------------------------------------------------------------
    /**
     * Return the view distance in the specified world.
     *
     * @param world the World.
     * @return the view distance.
     */
    private int getViewDistance(World world) {
        YamlConfiguration spigotConfig = Bukkit.getServer().spigot().getConfig();
        int defaultViewDistance = spigotConfig.getInt("world-settings.default.view-distance", 12);
        return spigotConfig.getInt("world-settings." + world.getName() + ".view-distance", defaultViewDistance);
    }

    // ------------------------------------------------------------------------
    /**
     * Time when this plugin was enabled.
     */
    protected Instant _startTime;

    // ------------------------------------------------------------------------
    /**
     * Interactive test.
     */
    public static void main(String[] args) {
        Instant now = Instant.now();
        ZonedDateTime zonedNow = ZonedDateTime.ofInstant(now, ZoneId.systemDefault());
        LocalDate todaysDate = LocalDate.from(zonedNow);

        LocalDate startDate = todaysDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
        LocalDate endDate = startDate.plusDays(1);

        DaySchedule.getDefault().load(MessageSink.STDOUT, Arrays.asList("02:30 10", "16:00 6", "23:30 4"));
        DaySchedule.create(DayOfWeek.MONDAY).load(MessageSink.STDOUT, Arrays.asList("02:00 12", "16:00 6"));
        DaySchedule.listAll(MessageSink.STDOUT);
        ZonedDateTime startTime = ZonedDateTime.of(startDate, LocalTime.of(23, 0), ZoneId.systemDefault());
        ZonedDateTime endTime = ZonedDateTime.of(endDate, LocalTime.of(03, 0), ZoneId.systemDefault());

        System.out.println("Start: " + startTime.format(DateTimeFormatter.RFC_1123_DATE_TIME));
        System.out.println("End: " + endTime.format(DateTimeFormatter.RFC_1123_DATE_TIME));
        System.out.println("Change: " + getChange(startTime.toInstant(), endTime.toInstant()));
    }
} // class DVDA