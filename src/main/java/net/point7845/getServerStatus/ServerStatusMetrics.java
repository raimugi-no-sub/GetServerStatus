package net.point7845.getServerStatus;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import github.scarsz.discordsrv.dependencies.jda.api.EmbedBuilder;

import me.lucko.spark.api.Spark;
import me.lucko.spark.api.statistic.StatisticWindow;
import me.lucko.spark.api.statistic.misc.DoubleAverageInfo;
import me.lucko.spark.api.statistic.types.DoubleStatistic;
import me.lucko.spark.api.statistic.types.GenericStatistic;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;

import com.sun.management.OperatingSystemMXBean;

public final class ServerStatusMetrics {

    private final double tps;
    private final double mspt;
    private final long usedMemoryBytes;
    private final long maxMemoryBytes;
    private final double cpu;

    private ServerStatusMetrics(double tps, double mspt, long usedMemoryBytes, long maxMemoryBytes, double cpu) {
        this.tps = tps;
        this.mspt = mspt;
        this.usedMemoryBytes = usedMemoryBytes;
        this.maxMemoryBytes = maxMemoryBytes;
        this.cpu = cpu;
    }

    public static ServerStatusMetrics collect() {
        Spark spark = Bukkit.getServicesManager().load(Spark.class);
        DoubleStatistic<StatisticWindow.TicksPerSecond> tpsStat = spark.tps();
        GenericStatistic<DoubleAverageInfo, StatisticWindow.MillisPerTick> msptStat = spark.mspt();
        OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

        double tps = tpsStat.poll(StatisticWindow.TicksPerSecond.SECONDS_10);
        double mspt = msptStat.poll(StatisticWindow.MillisPerTick.SECONDS_10).mean();

        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heap = memoryBean.getHeapMemoryUsage();
        long usedMemoryBytes = heap.getUsed();
        long maxMemoryBytes = heap.getMax();
        if (maxMemoryBytes < 0) {
            maxMemoryBytes = Runtime.getRuntime().maxMemory();
        }

        double cpu = osBean.getProcessCpuLoad();

        return new ServerStatusMetrics(tps, mspt, usedMemoryBytes, maxMemoryBytes, cpu);
    }

    public void applyToEmbed(EmbedBuilder embed, Lang lang) {
        embed.addField(lang.get("metrics.tps"), String.format("%.2f", tps), false);
        embed.setColor(colorForTps(tps));
        embed.addField(lang.get("metrics.mspt"), String.format("%.2f", mspt), false);
        embed.addField(lang.get("metrics.memory"), formatMemory(), false);
        embed.addField(lang.get("metrics.cpu"), formatCpu(lang), false);
    }

    public void sendTo(CommandSender sender, Lang lang) {
        sender.sendMessage(lang.format("chat.tps", tps));
        sender.sendMessage(lang.format("chat.mspt", mspt));
        sender.sendMessage(lang.format("chat.memory", formatMemory()));
        sender.sendMessage(lang.format("chat.cpu", formatCpu(lang)));
    }

    private String formatMemory() {
        double usedGb = usedMemoryBytes / (1024.0 * 1024 * 1024);
        double maxGb = maxMemoryBytes / (1024.0 * 1024 * 1024);
        return String.format("%.2f GB / %.2f GB", usedGb, maxGb);
    }

    private String formatCpu(Lang lang) {
        if (cpu < 0) {
            return lang.get("metrics.cpu-unavailable");
        }
        return String.format("%.2f%%", cpu * 100);
    }

    private static int colorForTps(double tps) {
        if (tps >= 18) {
            return 0x00FF00;
        } else if (tps >= 15) {
            return 0xFFFF00;
        } else if (tps >= 13) {
            return 0xFFA500;
        } else {
            return 0xFF0000;
        }
    }
}
