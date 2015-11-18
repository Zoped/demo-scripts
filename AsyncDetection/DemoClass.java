package scripts;

import org.tribot.api.types.generic.Condition;
import org.tribot.api2007.Players;
import org.tribot.api2007.types.RSPlayer;
import org.tribot.script.Script;
import org.tribot.script.ScriptManifest;

import java.util.ArrayList;

/**
 * Created by Zope on 10/21/2015.
 */
public class DemoClass extends Script implements DangerListener {

    private ArrayList<RSPlayer> dangerousPlayers = new ArrayList<>();
    private boolean firstRun = true;

    // Action that occurs when a danger is found
    @Override
    public void dangerFound(RSPlayer player) {
        if (!dangerousPlayers.contains(player)) {
            println("Danger Found: " + player.getName());
            dangerousPlayers.add(player);
        }
    }

    // Action that occurs when a danger is lost
    @Override
    public void dangerLost(RSPlayer player) {
        if (dangerousPlayers.contains(player)) {
            println("Danger Lost: " + player.getName());
            dangerousPlayers.remove(player);
        }
    }

    @Override
    public void run() {

        // This is true on initialization
        if (firstRun)
            init();

        while (true) {
            // Print the name of every found dangerous player
            sleep(250);
        }
    }

    private void init() {
        // Create instance of observer class
        DangerObserver dangerObserver = new DangerObserver(new Condition() {
            @Override
            public boolean active() {
                return true;
            }
        });

        // Add this script as a listener for the observer
        dangerObserver.addListener(this);

        // Start the DangerObserver thread
        dangerObserver.start();

        // Set firstRun to false after this has been ran once
        firstRun = false;
    }
}
