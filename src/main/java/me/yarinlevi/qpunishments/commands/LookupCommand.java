package me.yarinlevi.qpunishments.commands;

import me.yarinlevi.qpunishments.history.QueryMode;
import me.yarinlevi.qpunishments.support.bungee.QBungeePunishments;
import me.yarinlevi.qpunishments.support.bungee.messages.MessagesUtils;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

import java.util.Arrays;
import java.util.regex.Pattern;

public class LookupCommand extends Command {
    static Pattern numberPattern = Pattern.compile("([0-9])+");

    public LookupCommand(String name, String permission, String... aliases) {
        super(name, permission, aliases);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {

        if (args.length != 0) {
            QueryMode mode;

            int limit = 3;
            int limitArgPos = 2;
            int modeArgPos = 1;
            boolean debug = false;

            String targetPlayer;

            if (args[0].equalsIgnoreCase("-debug")) {
                debug = true;
                targetPlayer = args[1];
                modeArgPos++;
                limitArgPos++;
            } else {
                targetPlayer = args[0];
            }

            if (args.length >= 2) {
                if (args.length == 3 && limitArgPos == 2 || args.length == 4 && limitArgPos == 3) {
                    if (numberPattern.matcher(args[limitArgPos]).matches()) {
                        limit = Integer.parseInt(args[limitArgPos]);
                    } else {
                        sender.sendMessage(MessagesUtils.getMessage("invalid_limit"));
                    }
                }

                final int pos = modeArgPos;

                mode = Arrays.stream(QueryMode.values()).anyMatch(x -> x.getKey().toLowerCase().startsWith(args[pos].toLowerCase()))
                        ? Arrays.stream(QueryMode.values()).filter(x -> x.getKey().toLowerCase().startsWith(args[pos].toLowerCase())).findFirst().get()
                        : QueryMode.ALL;

                //mode = QueryMode.valueOf(args[modeArgPos].toUpperCase());
            } else {
                mode = QueryMode.ALL;
            }

            final int limitTemp = limit;
            final boolean debugTemp = debug;

            QBungeePunishments.getInstance().getProxy().getScheduler().runAsync(QBungeePunishments.getInstance(), () -> LookupShared.printLookup(sender, targetPlayer, mode, limitTemp, debugTemp, false));
        }
    }


    /*
    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> list = new ArrayList<>();

        if (args.length == 1 && !args[0].equalsIgnoreCase("-debug") || args.length == 2 && args[0].equalsIgnoreCase("-debug")) {
            list.add("comment");
            list.add("ban");
            list.add("mute");
            list.add("kick");
        }

        return list;
    }
     */
}
