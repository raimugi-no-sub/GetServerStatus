package net.point7845.getServerStatus;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.stream.Collectors;

public class GetStatusCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Lang lang = GetServerStatus.getInstance().getLang();
        int onlinePlayers = Bukkit.getOnlinePlayers().size();
        sender.sendMessage(lang.format("player.count", onlinePlayers));

        if (onlinePlayers == 0) {
            sender.sendMessage(lang.get("player.none"));
        } else {
            String playerNames = Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .collect(Collectors.joining(", "));
            sender.sendMessage(lang.format("player.list", playerNames));
        }

        ServerStatusMetrics.collect().sendTo(sender, lang);
        return true;
    }
}
