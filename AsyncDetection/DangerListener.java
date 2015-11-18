package scripts;

import org.tribot.api2007.types.RSPlayer;

/**
 * Created by Zope on 10/19/15.
 */
public interface DangerListener {
    void dangerFound(RSPlayer player);
    void dangerLost(RSPlayer player);
}
