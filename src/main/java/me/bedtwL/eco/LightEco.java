package me.bedtwL.eco;

import me.bedtwL.eco.database.IDataUtils;
import me.bedtwL.eco.database.LocalConfigDataUtils;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class LightEco extends JavaPlugin implements CommandExecutor, TabExecutor {
    public static Economy economy = new VaultEco();
    public static IDataUtils dataUtils;
    @Override
    public void onEnable() {
        // TODO: add switch between database and local config
        dataUtils = new LocalConfigDataUtils();

        if (Bukkit.getPluginManager().isPluginEnabled("Vault")) {

            getServer().getServicesManager().register(Economy.class, economy, this, ServicePriority.Normal);
            RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);

            if (rsp != null) {
                getServer().getPluginCommand("eco").setExecutor(this);
                getServer().getPluginCommand("eco").setTabCompleter(this);
                getLogger().info("Plugin enabled!");
            }
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().info("LightEco has became DarkEco (Disabled)");
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            player.sendMessage(ChatColor.RED + "Usage: /eco <bal|deposit|withdraw|set|reload>");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "bal":
                if (player.hasPermission("bedtwl.cmd.eco.bal")) {
                    double balance = economy.getBalance(player);
                    player.sendMessage(ChatColor.GREEN + "Your balance: $" + balance);
                } else {
                    player.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
                }
                break;

            case "deposit":
                if (player.hasPermission("bedtwl.cmd.eco.deposit")) {
                    if (args.length < 2) {
                        player.sendMessage(ChatColor.RED + "Usage: /eco deposit <amount>");
                    } else {
                        try {
                            double amount = Double.parseDouble(args[1]);
                            economy.depositPlayer(player, amount);
                            player.sendMessage(ChatColor.GREEN + "Deposited $" + amount);
                        } catch (NumberFormatException e) {
                            player.sendMessage(ChatColor.RED + "Invalid amount.");
                        }
                    }
                } else {
                    player.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
                }
                break;

            case "withdraw":
                if (player.hasPermission("bedtwl.cmd.eco.withdraw")) {
                    if (args.length < 2) {
                        player.sendMessage(ChatColor.RED + "Usage: /eco withdraw <amount>");
                    } else {
                        try {
                            double amount = Double.parseDouble(args[1]);
                            if (economy.withdrawPlayer(player, amount).transactionSuccess()) {
                                player.sendMessage(ChatColor.GREEN + "Withdrawn $" + amount);
                            } else {
                                player.sendMessage(ChatColor.RED + "Insufficient balance.");
                            }
                        } catch (NumberFormatException e) {
                            player.sendMessage(ChatColor.RED + "Invalid amount.");
                        }
                    }
                } else {
                    player.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
                }
                break;

            case "set":
                if (player.hasPermission("bedtwl.cmd.eco.set")) {
                    if (args.length < 3) {
                        player.sendMessage(ChatColor.RED + "Usage: /eco set <player> <amount>");
                    } else {
                        try {
                            Player target = player.getServer().getPlayer(args[1]);
                            if (target == null) {
                                player.sendMessage(ChatColor.RED + "Player not found.");
                            } else {
                                double amount = Double.parseDouble(args[2]);
                                DataUtils.setPlayerVaultCoins(target.getUniqueId(), amount);
                                player.sendMessage(ChatColor.GREEN + "Set " + target.getName() + "'s balance to $" + amount);
                            }
                        } catch (NumberFormatException e) {
                            player.sendMessage(ChatColor.RED + "Invalid amount.");
                        }
                    }
                } else {
                    player.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
                }
                break;
            default:
                player.sendMessage(ChatColor.RED + "Unknown command. Usage: /eco <bal|deposit|withdraw|set>");
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player)) {
            return null;
        }
        if (args.length == 1) {
            return Arrays.asList("bal", "deposit", "withdraw", "set","reload");
        } else if (args.length == 2 && args[0].equalsIgnoreCase("set")) {
            // Suggest player names if the command is "set"
            if (!sender.hasPermission("bedtwl.cmd.eco.set"))
                return null;
            List<String> playerNames = new ArrayList<>();

            for (Player player : sender.getServer().getOnlinePlayers()) {
                if (player.getName()==player.getDisplayName())
                {
                    if (player.getName().startsWith(args[1]))
                        playerNames.add(player.getName());
                }
                else
                {
                    if (player.getName().startsWith(args[1]))
                    {
                        playerNames.add(player.getName());
                    }
                    if (player.getDisplayName().startsWith(args[1]))
                    {
                        playerNames.add(player.getDisplayName());
                    }
                }
            }
            return playerNames;
        }
        return null; // No tab completion for other arguments
    }
}
