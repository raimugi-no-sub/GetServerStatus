package net.point7845.getServerStatus;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import github.scarsz.discordsrv.DiscordSRV;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public final class GetServerStatus extends JavaPlugin {

    private static GetServerStatus instance;
    private Lang lang;
    private String trigger;
    private PluginCommand statusCommand;
    private boolean discordSubscribed;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        lang = new Lang(this, getConfig().getString("language", "en"));

        trigger = getConfig().getString("trigger", "getstatus").toLowerCase();
        PluginCommand command;
        try {
            Constructor<PluginCommand> constructor = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
            constructor.setAccessible(true);
            command = constructor.newInstance(trigger, this);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to create command: " + trigger, e);
        }
        command.setDescription(lang.get("command.description"));
        command.setUsage("/" + trigger);
        command.setExecutor(new GetStatusCommand());
        getCommandMap().register(getName().toLowerCase(), command);
        statusCommand = command;

        if (getServer().getPluginManager().isPluginEnabled("DiscordSRV")) {
            DiscordSRV.api.subscribe(new GetStatusDiscordListener());
            discordSubscribed = true;
        } else {
            getLogger().info(lang.get("log.discord-disabled"));
        }
    }

    @Override
    public void onDisable() {
        if (statusCommand != null) {
            statusCommand.unregister(getCommandMap());
            statusCommand = null;
        }
        if (discordSubscribed) {
            DiscordSRV.api.unsubscribe(GetStatusDiscordListener.class);
            discordSubscribed = false;
        }
        instance = null;
        lang = null;
    }

    private CommandMap getCommandMap() {
        try {
            Server server = Bukkit.getServer();
            Method method = server.getClass().getMethod("getCommandMap");
            return (CommandMap) method.invoke(server);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Could not access CommandMap", e);
        }
    }

    public static GetServerStatus getInstance() {
        return instance;
    }

    public Lang getLang() {
        return lang;
    }

    public boolean matchesTrigger(String text) {
        return text != null && text.equalsIgnoreCase(trigger);
    }
}
