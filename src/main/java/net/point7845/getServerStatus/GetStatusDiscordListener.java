package net.point7845.getServerStatus;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.api.Subscribe;
import github.scarsz.discordsrv.api.events.DiscordGuildMessageReceivedEvent;
import github.scarsz.discordsrv.dependencies.jda.api.EmbedBuilder;
import github.scarsz.discordsrv.dependencies.jda.api.entities.MessageEmbed;

import java.util.stream.Collectors;

public class GetStatusDiscordListener {

    @Subscribe
    public void discordMessageReceived(DiscordGuildMessageReceivedEvent event) {
        if (event.getChannel().getId().equals(DiscordSRV.getPlugin().getMainTextChannel().getId())) {
            String message = event.getMessage().getContentRaw();
            if (GetServerStatus.getInstance().matchesTrigger(message)) {
                Lang lang = GetServerStatus.getInstance().getLang();
                EmbedBuilder embed = new EmbedBuilder();
                int onlinePlayers = Bukkit.getOnlinePlayers().size();
                embed.addField(
                        lang.get("embed.player-count"),
                        lang.format("embed.player-count-value", onlinePlayers),
                        false);
                String playerNames;
                if (onlinePlayers == 0) {
                    playerNames = lang.get("embed.none");
                } else {
                    playerNames = Bukkit.getOnlinePlayers().stream()
                            .map(Player::getName)
                            .collect(Collectors.joining(", "));
                }
                embed.addField(lang.get("embed.online-players"), playerNames, false);

                ServerStatusMetrics.collect().applyToEmbed(embed, lang);

                MessageEmbed builtEmbed = embed.build();
                event.getChannel().sendMessageEmbeds(builtEmbed).queue();
            }
        }
    }
}
