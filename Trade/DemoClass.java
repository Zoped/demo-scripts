package scripts;

import org.tribot.api.types.generic.Condition;
import org.tribot.api2007.Players;
import org.tribot.api2007.types.RSPlayer;
import org.tribot.script.Script;
import org.tribot.script.ScriptManifest;
import org.tribot.script.interfaces.MessageListening07;

import java.util.ArrayList;

/**
 * Created by Zope on 10/21/2015.
 */
public class DemoClass extends Script implements MessageListening07 {

    private ArrayList<RSPlayer> dangerousPlayers = new ArrayList<>();
    private boolean firstRun = true;

    @Override
    public void run() {
        while (true) {
            sleep(250);
        }
    }
}
