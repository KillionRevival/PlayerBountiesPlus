package com.tcoded.playerbountiesplus.listener;

import com.tcoded.playerbountiesplus.PlayerBountiesPlus;
import com.tcoded.playerbountiesplus.hook.team.AbstractTeamHook;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class DeathListener implements Listener {

    private PlayerBountiesPlus plugin;

    public DeathListener(PlayerBountiesPlus plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        if (killer != null) {
            ConcurrentHashMap<UUID, Integer> bounties = this.plugin.getBountyDataManager().getBounties();
            UUID victimId = victim.getUniqueId();
            Integer bounty = bounties.get(victimId);

            // Bounty check
            if (bounty == null || bounty == 0) {
                return;
            }

            // Check if killer has the no_bounty_claim permission
            if (killer.hasPermission("playerbountiesplus.no_bounty_claim")) {
                killer.sendMessage(plugin.getLang().getColoredOrDefault("death.no-claim-permission", "You do not have permission to claim bounties."));
                return;
            }

            // Clan check
            AbstractTeamHook teamHook = this.plugin.getTeamHook();
            if (teamHook != null && teamHook.isFriendly(killer, victim)) {
                killer.sendMessage(plugin.getLang().getColoredOrDefault("death.same-team", "You cannot claim bounties on your team members."));
                return;
            }

            // Give reward!
            if (plugin.getConfig().getBoolean("bounty-claimable", true)) {
                double claimMultiplier = plugin.getConfig().getDouble("bounty-claim-multiplier", 1.0);
                double awardedAmount = bounty * claimMultiplier;

                if (awardedAmount > 0) {
                    this.plugin.getVaultHook().addMoney(killer, awardedAmount);
                } else if (awardedAmount < 0) {
                    this.plugin.getVaultHook().takeMoney(killer, Math.abs(awardedAmount));
                }
            }

            // Optional - Take from victim as punishment
            if (plugin.getConfig().getBoolean("bounty-take-from-victim", false)) {
                this.plugin.getVaultHook().takeMoney(victim, bounty, true);
            }

            // Announce Reward
            if (plugin.getConfig().getBoolean("bounty-claimed-announce", true)) {
                String announceMessage = plugin.getLang().getColoredOrDefault("death.announce-claimed",
                                "{killer} has claimed a bounty of {bounty} on {victim}!")
                        .replace("{killer}", killer.getName())
                        .replace("{victim}", victim.getName())
                        .replace("{bounty}", bounty.toString());
                this.plugin.getServer().broadcastMessage(announceMessage);
            }

            // Remove bounty
            bounties.remove(victimId);
            this.plugin.getBountyDataManager().saveBountiesAsync();
        }
    }
}