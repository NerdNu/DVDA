package nu.nerd.dvda;

import java.util.function.Consumer;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

// ----------------------------------------------------------------------------
/**
 * Accepts messages to be sent to standard output, Loggers and CommandSenders
 * with a unified interface.
 */
public abstract class MessageSink implements Consumer<String> {
    // ------------------------------------------------------------------------
    /**
     * A MessageSink that writes to standard output.
     */
    public static MessageSink STDOUT = new MessageSink() {
        /**
         * @see java.util.function.Consumer#accept(java.lang.Object)
         */
        @Override
        public void accept(String msg) {
            System.out.println(ChatColor.stripColor(msg));
        }
    };

    // ------------------------------------------------------------------------
    /**
     * Return a MessageSink that writes to the specified Logger.
     *
     * @param logger the Logger.
     * @return a MessageSink that writes to the specified Logger.
     */
    public static MessageSink from(Logger logger) {
        return new MessageSink() {
            /**
             * @see java.util.function.Consumer#accept(java.lang.Object)
             */
            @Override
            public void accept(String msg) {
                logger.info(msg);
            }
        };
    }

    // ------------------------------------------------------------------------
    /**
     * Return a MessageSink that writes to the specified CommandSender.
     *
     * @param sender the command sender.
     * @return a MessageSink that writes to the specified CommandSender.
     */
    public static MessageSink from(CommandSender sender) {
        return new MessageSink() {
            /**
             * @see java.util.function.Consumer#accept(java.lang.Object)
             */
            @Override
            public void accept(String msg) {
                sender.sendMessage(msg);
            }
        };
    }
} // class MessageSink