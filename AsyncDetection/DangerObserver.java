package scripts;

import org.tribot.api.General;
import org.tribot.api.types.generic.Condition;
import org.tribot.api.types.generic.Filter;
import org.tribot.api2007.Equipment;
import org.tribot.api2007.Login;
import org.tribot.api2007.Player;
import org.tribot.api2007.Players;
import org.tribot.api2007.types.RSItem;
import org.tribot.api2007.types.RSPlayer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Zope on 10/19/15.
 */
public class DangerObserver extends Thread {

    private static ArrayList<DangerListener> listeners;
    private Condition condition;

    private RSPlayer[] last_dangerous_players_found;
    private RSPlayer[] new_dangerous_players_found;

    private final long GAME_TICK = 600 / 10;

    public DangerObserver(Condition condition) {
        this.listeners = new ArrayList<DangerListener>();
        this.condition = condition;
        last_dangerous_players_found = getDangerousPlayers();
    }

    @Override
    public void run() {

        while (Login.getLoginState() != Login.STATE.INGAME) {
            General.sleep(500);
        }

        while (true) {
            if (Login.getLoginState() != Login.STATE.INGAME)
                continue;

            if (!condition.active()) {
                // If the condition is false we want to subtract the last known dangerous players
                if (last_dangerous_players_found.length > 0)
                    for (RSPlayer p : last_dangerous_players_found)
                        subtractedTrigger(p);

                continue;
            } else {

                // If the condition is true

                // Update new player list
                new_dangerous_players_found = getDangerousPlayers();

                // Create list of removed objects by removing all of the newly 
                // found players (Whatever is left are the removed players)
                List<RSPlayer> removed = new LinkedList<RSPlayer>(Arrays.asList(last_dangerous_players_found));
                removed.removeAll(Arrays.asList(new_dangerous_players_found));

                // Create list of added objects by removing all the last
                // found players (Whatever is left are the added players)
                List<RSPlayer> added = new LinkedList<RSPlayer>(Arrays.asList(new_dangerous_players_found));
                added.removeAll(Arrays.asList(last_dangerous_players_found));

                // Add newly found players
                for (RSPlayer p : added) {
                    addTrigger(p);
                }

                // Remove newly lost players
                for (RSPlayer p : removed) {
                    subtractedTrigger(p);
                }

                // End of cycle, old equals new...
                last_dangerous_players_found = new_dangerous_players_found;
                General.sleep(GAME_TICK);
            }
        }
    }

    public static RSPlayer[] getDangerousPlayers() {
        return Players.getAll(new Filter<RSPlayer>() {
            @Override
            public boolean accept(RSPlayer rsPlayer) {
                if (isPlayerDangerous(rsPlayer))
                    return true;
                else
                    return false;
            }
        });
    }

    public void addListener(DangerListener dangerListener) {
        listeners.add(dangerListener);
    }

    public static void addTrigger(RSPlayer player) {
        for (DangerListener l : listeners)
            l.dangerFound(player);
    }

    public void subtractedTrigger(RSPlayer player) {
        for (DangerListener l : listeners)
            l.dangerLost(player);
    }

    public static boolean isPlayerDangerous(RSPlayer player) {
        int ourCmbLevel = Player.getRSPlayer().getCombatLevel();
        int theirCmbLevel = player.getCombatLevel();

        if (!player.getName().equals(Player.getRSPlayer().getName()))
            if (Ids.obeliskArea.contains(player) || Ids.tradeArea.contains(player))
                if (playerWithinCombatRange(theirCmbLevel, ourCmbLevel)) {
                    // TODO: getSkullIcon native method is broken, await update / fix
                    // TODO: Make this check: playerIsSkulled(player)
                    if (!playerIsSkulled(player)) {
                        if (playerIsNotRunningOrbs(player)) {
                            addTrigger(player);
                            return true;
                        }
                    }
                }

        return false;
    }

    private static boolean playerWithinCombatRange(int theirCombat, int ourCombat) {
        int dangerMinLevel = ourCombat - 7;
        int dangerMaxLevel = ourCombat + 7;

        // If the players level is within dangerous range
        if (dangerMinLevel <= theirCombat && theirCombat <= dangerMaxLevel)
            return true;
        else
            return false;
    }

    private static boolean playerIsSkulled(RSPlayer player) {
        if (player.getSkullIcon() == 0)
            return true;
        else
            return false;
    }

    private static boolean playerIsNotRunningOrbs(RSPlayer player) {
        for (int i = 0; i < player.getDefinition().getEquipment().length; i++) {
            RSItem wornItem = player.getDefinition().getEquipment()[i];

            // Check weapon
            if (wornItem.getEquipmentSlot().equals(Equipment.SLOTS.WEAPON))
                if (wornItem.getDefinition().getName().toLowerCase().equals("staff of air")) {
                    // If it is a staff of air
                    return false; // We assume the player is skulled on accident or a bot that mis clicked and 'running orbs'
                }
        }

        // If the player is NOT wielding a staff of air we know they are NOT running air orbs
        return true;
    }
}