Dynamic View Distance Adjustment (DVDA)
=======================================
Adjust the view distance in configured worlds on server stop, according to a
schedule.

At the time of writing, Spigot does not support adjusting the view distance on
a running server.  The server has to restart to change the view distance.

DVDA adjusts the view distance according to a weekly schedule loaded from the
configuration, on the assumption that the player count a particular time on a
particular day of the week will be about the same from one week to the next.
A future version of DVDA may instead adjust the view distance on the basis of
the peak player count at a particular time of day (if sustained for more than
a threshold duration).
 

Configuration
-------------
 * `debug.config` - If true, the configuration is logged to the console when
   loaded.
 * `worlds` - A list of the names of worlds whose view distance will be
   adjusted.
 * `schedule.<key>` - Set the view distance adjustment schedule for the day
   corresponding to `<key>`.
   * `<key>` is either `default`, or the three-letter abbreviated day of the 
     week, beginning with a capital letter, e.g. `Mon`.
   * The schedule listed under `schedule.default` applies to any day of the
     week for which there is no specific schedule for that day.
     * For instance, [`examples/config.yml`](https://raw.github.com/NerdNu/DVDA/master/examples/config.yml)
       defines a `default` schedule that is overridden on Sundays only.
   * A schedule is a list of strings of the form `'<time> <view-distance>'`,
     where `<time>` is a time of the form `HH:mm` or `HH:mm:ss` and 
     `<view-distance>` is just the integer view distance to use after that 
     time.
   * A schedule may define multiple view distance changes within the period 
     between the server start and stop times, but only the last of these changes
     will be applied when the server stops.  DVDA has no knowledge of the server
     restart schedule.

       
Commands
--------
 * `/dvda help` - Show usage help for `/dvda`.
 * `/dvda reload` - Reload the configuration.
 * `/dvda list-all` - List the default schedule and the schedule on all days
   with specific overrides.
 * `/view-distance` - Show the current configured view distance in all worlds
   where the view distance is set to be adjusted, and if a view distance
   adjustment is scheduled between the server start time and the current time,
   show the new view distance.  If the server stops immediately after 
   `/view-distance` is run, then the command will return the new view 
   distance that will be set.  However, it does not take into account any 
   pending view distance adjustments that are still in the future (remember, 
   DVDA does not know the server's restart schedule).


Permissions
-----------
 * `dvda.admin` - Permission to use the `/dvda` command.
 * `dvda.viewdistance` - Permission to use the `/view-distance` command.
