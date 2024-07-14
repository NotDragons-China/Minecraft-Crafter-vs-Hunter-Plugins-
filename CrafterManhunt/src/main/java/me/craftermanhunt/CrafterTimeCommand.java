package me.craftermanhunt;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CrafterTimeCommand implements CommandExecutor, TabCompleter {
    private final JavaPlugin plugin;
    private BukkitRunnable activeTimer = null;
    private int secondsLeft = 0;
    private boolean isPaused = false;

    public CrafterTimeCommand(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (args.length == 2) {
                if (args[0].equalsIgnoreCase("time")) {
                    try {
                        int minutes = Integer.parseInt(args[1]);
                        startTimer(player, minutes);
                    } catch (NumberFormatException e) {
                        player.sendMessage("§c请输入一个有效的分钟数!");
                    }
                } else {
                    player.sendMessage("§c请输入有效指令!");
                }
            } else if (args.length == 1) {
                if (args[0].equalsIgnoreCase("stoptime")) {
                    stopTimer();
                } else if (args[0].equalsIgnoreCase("pausetime")) {
                    pauseTimer();
                } else if (args[0].equalsIgnoreCase("resumetime")) {
                    resumeTimer();
                } else if (args[0].equalsIgnoreCase("task")) {
                    handleTaskCommand(player);
                } else {
                    player.sendMessage("§c请输入有效指令!");
                }
            } else {
                player.sendMessage("§c请输入有效指令");
            }
        } else {
            sender.sendMessage("§c这个命令只能由玩家执行!");
        }
        return true;
    }

    private void startTimer(Player player, int minutes) {
        if (activeTimer != null) {
            player.sendMessage("§c已经有正在运行的计时器了!");
            return;
        }

        secondsLeft = minutes * 60;
        activeTimer = new BukkitRunnable() {
            @Override
            public void run() {
                if (secondsLeft <= 0) {
                    Bukkit.broadcastMessage("§a时限已到，恭喜猎人胜利！");
                    activeTimer = null;
                    this.cancel();
                    return;
                }

                if (secondsLeft <= 5) {
                    Bukkit.broadcastMessage("§c最后倒计时: " + secondsLeft + " 秒！");
                }

                int hours = secondsLeft / 3600;
                int minutes = (secondsLeft % 3600) / 60;
                int seconds = secondsLeft % 60;

                String timeFormat = String.format("%02d:%02d:%02d", hours, minutes, seconds);
                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                    onlinePlayer.sendActionBar("§a" + timeFormat);
                }

                secondsLeft--;
            }
        };

        activeTimer.runTaskTimer(plugin, 0L, 20L);
    }

    private void stopTimer() {
        if (activeTimer != null) {
            activeTimer.cancel();
            activeTimer = null;
            Bukkit.broadcastMessage("§c计时已停止!");
        } else {
            Bukkit.broadcastMessage("§c没有正在运行的计时器!");
        }
    }

    private void pauseTimer() {
        if (activeTimer != null && !isPaused) {
            activeTimer.cancel();
            isPaused = true;
            Bukkit.broadcastMessage("§e计时已暂停！");

            // 显示暂停时的倒计时
            int hours = secondsLeft / 3600;
            int minutes = (secondsLeft % 3600) / 60;
            int seconds = secondsLeft % 60;
            String timeFormat = String.format("%02d:%02d:%02d", hours, minutes, seconds);

            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!isPaused) {
                        this.cancel();
                        return;
                    }
                    for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                        onlinePlayer.sendActionBar("§a"+ timeFormat);
                    }
                }
            }.runTaskTimer(plugin, 0L, 20L);
        } else {
            Bukkit.broadcastMessage("§c没有正在运行的计时器或计时器已经暂停！");
        }
    }

    private void resumeTimer() {
        if (isPaused) {
            isPaused = false;
            activeTimer = new BukkitRunnable() {
                @Override
                public void run() {
                    if (secondsLeft <= 0) {
                        Bukkit.broadcastMessage("§a时限已到，恭喜猎人胜利！");
                        activeTimer = null;
                        this.cancel();
                        return;
                    }

                    if (secondsLeft <= 5) {
                        Bukkit.broadcastMessage("§c最后倒计时: " + secondsLeft + " 秒！");
                    }

                    int hours = secondsLeft / 3600;
                    int minutes = (secondsLeft % 3600) / 60;
                    int seconds = secondsLeft % 60;

                    String timeFormat = String.format("%02d:%02d:%02d", hours, minutes, seconds);
                    for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                        onlinePlayer.sendActionBar("§e" + timeFormat);
                    }

                    secondsLeft--;
                }
            };

            activeTimer.runTaskTimer(plugin, 0L, 20L); // 20 ticks = 1 second
            Bukkit.broadcastMessage("§e计时已恢复!");
        } else {
            Bukkit.broadcastMessage("§c没有正在运行的计时器或计时器未暂停！");
        }
    }

    private void handleTaskCommand(Player player) {
        int taskCount = plugin.getConfig().getInt("task-count", 1);
        List<String> taskMessages = plugin.getConfig().getStringList("task-messages");

        if (taskMessages.isEmpty()) {
            player.sendMessage("§c没有任务信息!请在配置文件中添加任务信息!");
            return;
        }

        if (taskCount > taskMessages.size()) {
            player.sendMessage("§e" + "任务信息数量不足，请添加更多内容或更改任务信息数量。");
            for (String message : taskMessages) {
                player.sendMessage("§a" + message);
            }
        } else if (taskCount < taskMessages.size()) {
            Collections.shuffle(taskMessages);
            List<String> selectedMessages = new ArrayList<>(taskMessages.subList(0, taskCount));
            for (String message : selectedMessages) {
                Bukkit.broadcastMessage("§a" + message);
            }
        } else {
            for (String message : taskMessages) {
                Bukkit.broadcastMessage("§a" + message);
            }
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            completions.add("time");
            completions.add("stoptime");
            completions.add("pausetime");
            completions.add("resumetime");
            completions.add("task");
        } else if (args.length == 2 && args[0].equalsIgnoreCase("time")) {
            for (int i = 1; i <= 60; i++) {
                completions.add(String.valueOf(i));
            }
        }
        return completions;
    }
}