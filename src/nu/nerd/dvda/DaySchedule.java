package nu.nerd.dvda;

import java.time.DayOfWeek;
import java.time.format.TextStyle;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeSet;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;

// ----------------------------------------------------------------------------
/**
 * Stores scheduled configuration adjustments for one day of the week or the
 * default day, which applies on any day when a specific overriding schedule has
 * not been specified.
 */
public class DaySchedule {
    // ------------------------------------------------------------------------
    /**
     * Return the default DaySchedule.
     *
     * @return the default DaySchedule.
     */
    public static DaySchedule getDefault() {
        return _default;
    }

    // ------------------------------------------------------------------------
    /**
     * Return the DaySchedule of the specified day of the week.
     *
     * @param day the day.
     * @return the DaySchedule.
     */
    public static DaySchedule of(DayOfWeek day) {
        return _dayToSchedule.getOrDefault(day, _default);
    }

    // ------------------------------------------------------------------------
    /**
     * Return a non-default schedule for the specified day.
     *
     * If a specific (not the default) schedule for the day already exists,
     * return that. Otherwise, create a new empty schedule and associate it with
     * the specified day.
     *
     * @param day the day of the week.
     */
    public static DaySchedule create(DayOfWeek day) {
        DaySchedule schedule = _dayToSchedule.get(day);
        if (schedule == null) {
            schedule = new DaySchedule();
            _dayToSchedule.put(day, schedule);
        }
        return schedule;
    }

    // ------------------------------------------------------------------------
    /**
     * Load the default schedule and the schedule overrides for all days
     * specified in the configuration section.
     *
     * @param sink the MessageSink to log to.
     * @param schedule the configuration section, which contains a string list
     *        called "default" for the default schedule, and optional overriding
     *        schedules for specific days, in the form of string lists named
     *        "Mon", "Tue", "Wed" etc.
     */
    public static void loadAll(MessageSink sink, ConfigurationSection schedule) {
        _default = new DaySchedule();
        _dayToSchedule.clear();

        _default.load(sink, schedule.getStringList("default"));
        for (DayOfWeek day : DayOfWeek.values()) {
            String key = day.getDisplayName(TextStyle.SHORT, Locale.US);
            List<String> entries = schedule.getStringList(key);
            if (!entries.isEmpty()) {
                create(day).load(sink, entries);
            }
        }
    }

    // ------------------------------------------------------------------------
    /**
     * List all schedules to the sink.
     *
     * @param sink the MessageSink to log to.
     */
    public static void listAll(MessageSink sink) {
        for (DayOfWeek day : DayOfWeek.values()) {
            DaySchedule schedule = _dayToSchedule.get(day);
            if (schedule != null) {
                sink.accept(day.getDisplayName(TextStyle.SHORT, Locale.US) + ":");
                schedule.list(sink);
            }
        }
        sink.accept("default:");
        getDefault().list(sink);
    }

    // ------------------------------------------------------------------------
    /**
     * Load the schedule from list of strings.
     *
     * @param sink the MessageSink to log errors to.
     * @param entries the string list describing the view distance changes.
     */
    public void load(MessageSink sink, List<String> entries) {
        for (String entry : entries) {
            try {
                _entries.add(new ViewDistanceChange(entry));
            } catch (NumberFormatException ex) {
                sink.accept("Could not parse schedule entry: " + entry);
            }
        }
    }

    // ------------------------------------------------------------------------
    /**
     * List the changes in the schedule to the sink.
     *
     * @param sink the sink.
     */
    public void list(MessageSink sink) {
        for (ViewDistanceChange change : _entries) {
            sink.accept(ChatColor.GOLD + change.getTime().toString() + " " +
                        ChatColor.YELLOW + change.getViewDistance());
        }
    }

    // ------------------------------------------------------------------------
    /**
     * Return an iterator that visits all entries in reverse order, from last to
     * first.
     *
     * @return the entries in reverse order.
     */
    public Iterator<ViewDistanceChange> reversed() {
        return _entries.descendingIterator();
    }

    // ------------------------------------------------------------------------
    /**
     * The default DaySchedule.
     */
    protected static DaySchedule _default = new DaySchedule();

    /**
     * Schedules that override the default schedule on specific days, indexed by
     * the day of the week.
     */
    protected static Map<DayOfWeek, DaySchedule> _dayToSchedule = new HashMap<DayOfWeek, DaySchedule>();

    /**
     * Entries in the schedule.
     */
    protected TreeSet<ViewDistanceChange> _entries = new TreeSet<ViewDistanceChange>();

} // class DaySchedule