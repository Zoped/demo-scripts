package scripts;

import org.tribot.api.DynamicClicking;
import org.tribot.api.General;
import org.tribot.api.Timing;
import org.tribot.api.input.Keyboard;
import org.tribot.api.types.generic.Condition;
import org.tribot.api.types.generic.Filter;
import org.tribot.api2007.*;
import org.tribot.api2007.types.RSInterface;
import org.tribot.api2007.types.RSInterfaceComponent;
import org.tribot.api2007.types.RSItem;
import org.tribot.api2007.types.RSPlayer;

import java.util.ArrayList;

public class TradeUtils {
    /**
     * Trade user
     *
     * @param name    Name of user to attempt to trade
     * @param timeout Time in milliseconds to wait
     * @return boolean representing if player was nearby or not
     */
    public static boolean tradeUser(final String name, int timeout) {
        // Get the player that matches the passed in name
        RSPlayer[] players = Players.find(new Filter<RSPlayer>() {
            @Override
            public boolean accept(RSPlayer p) {
                return p.getName().equalsIgnoreCase(name);
            }
        });

        // Disgusting null check to make sure a player was found
        if (players != null && players.length > 0 && players[0] != null) {
            RSPlayer p = players[0];
            // If the player is not on screen, walk to them
            if (!p.isOnScreen()) {
                Walking.walkScreenPath(Walking.generateStraightPath(p.getPosition()));
                General.sleep(100, 1000);
                Camera.turnToTile(p.getPosition());
            } else {
                // Initialize trade request
                p.hover();
                DynamicClicking.clickRSModel(p.getModel(), "Trade");
                Timing.timeFromMark(timeout);
                return true;
            }
        }
        return false;
    }

    /**
     * Accepts received trade request, request must have already been received
     * @param name Name of trade to accept
     * @return boolean representing if accept was successful or not
     */
    public static boolean acceptTrade(final String name) {
        RSInterfaceComponent[] chatLines = Interfaces.get(Ids.CHAT_PARENT, Ids.CHAT_LINE_CONTAINER).getChildren();
        for (RSInterfaceComponent i : chatLines) {
            System.out.println(i.getText());
            if (i.isHidden())
                return false;
            else if (i.getText().toLowerCase().contains("<col=7f007f>" + name.toLowerCase() + " wishes to trade with you.")) {
                if (i.click(""))
                    return true;
                else
                    return false;
            }
        }

        return false;
    }

    public static String getPartner() {
        if (tradeScreen() == 1)
            return Interfaces.get(Ids.TRADE_WINDOW_PARENT_1, Ids.TRADE_WINDOW_TITLE_1).getText().substring("Tradin With: ".length()).trim();
        else if (tradeScreen() == 2) {
            String tmp = Interfaces.get(Ids.TRADE_WINDOW_PARENT_2, Ids.TRADE_WINDOW_TITLE_2).getText();
            return tmp.substring(tmp.indexOf("<br>") + 4).trim();
        } else
            return "";
    }

    /**
     * Accept trade screen window, handles first or second confirmation
     * @return If interface is visible and clicked
     */
    public static boolean accept() {
        General.println("Trade screen = " + tradeScreen());
        if (tradeScreen() == 1)
            return Interfaces.get(Ids.TRADE_WINDOW_PARENT_1, Ids.TRADE_WINDOW_ACCEPT_1).click();
        else if (tradeScreen() == 2)
            return Interfaces.get(Ids.TRADE_WINDOW_PARENT_2, Ids.TRADE_WINDOW_ACCEPT_2).click();
        else
            return false;
    }

    public static boolean decline() {
        if (tradeScreen() == 1)
            return Interfaces.get(Ids.TRADE_WINDOW_PARENT_1, Ids.TRADE_WINDOW_DECLINE_1).click();
        else if (tradeScreen() == 2)
            return Interfaces.get(Ids.TRADE_WINDOW_PARENT_2, Ids.TRADE_WINDOW_DECLINE_2).click();
        else
            return false;
    }

    /**
     * Checks if the trade window is open
     * @return boolean representing if trade window is visible or not
     */
    public static boolean isOpen() {
        boolean tradeIsOpen = (Interfaces.get(Ids.TRADE_WINDOW_PARENT_1, Ids.TRADE_WINDOW_TITLE_1) != null || Interfaces.get(Ids.TRADE_WINDOW_PARENT_2, Ids.TRADE_WINDOW_TITLE_2) != null);
        return tradeIsOpen;
    }

    /**
     * @return int representing which trade window you are on
     */
    public static int tradeScreen() {
        if (Interfaces.get(Ids.TRADE_WINDOW_PARENT_1, Ids.TRADE_WINDOW_TITLE_1) != null)
            return 1;
        if (Interfaces.get(Ids.TRADE_WINDOW_PARENT_2, Ids.TRADE_WINDOW_TITLE_2) != null)
            return 2;
        return 0;
    }

    /**
     * Method used to offer items
     * @param id Item id
     * @param amount Amount to offer, 0 is the equivalent to "Offer All"
     * @return boolean representing if the item was offered or not
     */
    public static boolean addItems(int id, int amount) {
        if (tradeScreen() != 1)
            return false;
        if (Inventory.getCount(id) >= amount) {
            RSItem[] items = Inventory.find(id);
            if (items != null && items.length > 0 && items[0] != null) {
                if (amount == 0) {
                    if (items[0].click("offer-all"))
                        return true;
                } else if (amount == 1) {
                    if (items[0].click(""))
                        return true;
                } else if (amount == 5) {
                    if (items[0].click("offer-5"))
                        return true;
                } else if (amount == 10) {
                    if (items[0].click("offer-10"))
                        return true;
                } else {
                    items[0].click("offer-x");
                    if (Timing.waitCondition(new Condition() {
                        @Override
                        public boolean active() {
                            return offerXVisible();
                        }
                    }, 3000)) {
                        General.sleep(20, 100);
                        Keyboard.typeSend(amount + "");
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean verifyAndConfirmTrade() {
        boolean acceptedTrade = false;

        General.sleep(200, 600);
        if (tradeScreen() != 0) {
            accept();
        } else {
            acceptedTrade = true;
        }

        if (acceptedTrade) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean removeItems(int id, int amount) {
        if (tradeScreen() != 1)
            return false;
        RSItem item = getItem(id, amount);
        if (item != null && item.getStack() >= amount) {
            if (amount == 1) {
                item.click("");
            } else if (amount == 5) {
                item.click("remove-5");
            } else if (amount == 10) {
                item.click("remove-10");
            } else {
                item.click("remove-x");
                if (Timing.waitCondition(new Condition() {
                    @Override
                    public boolean active() {
                        return offerXVisible();
                    }
                }, 3000)) {
                    General.sleep(20, 100);
                    Keyboard.typeSend(amount + "");
                }
            }
        }
        return false;
    }

    public static int yourOfferQuantity(String name) {
        return getContainerQuantity(name, getYourItemContainer_1(), getYourItemContainer_2());
    }

    public static int theirOfferQuantity(String name) {
        return getContainerQuantity(name, getTheirItemContainer_1(), getTheirItemContainer_2());
    }

    public static boolean yourOfferContains(String name, int amount) {
        if (amount == 0)
            return false;
        General.println("Checking if my offer contains " + amount + " of " + name);
        return amount >= getContainerQuantity(name, getYourItemContainer_1(), getYourItemContainer_2());
    }

    public static boolean theirOfferContains(String name, int amount) {
        if (amount == 0)
            return false;
        General.println("Checking if their offer contains " + amount + " of " + name);
        return amount >= getContainerQuantity(name, getTheirItemContainer_1(), getTheirItemContainer_2());
    }

    public static Condition tradeOpen() {
        return new Condition() {
            @Override
            public boolean active() {
                return isOpen();
            }
        };
    }

    private static RSItem getItem(int id, int amount) {
        RSItem[] items = getYourItemContainer_1();
        if (items != null && items[0] != null)
            return items[0];
        return null;
    }

    private static boolean isHidden(final RSInterface inter) {
        if (inter == null || inter.isHidden())
            return true;

        final int parentID = inter.getParentID();
        if (parentID == -1)
            return false;

        final int uid = inter.getUID();
        if (uid == parentID) // Prevents an infinite loop
            return false;

        final RSInterface parent = Interfaces.getChild(parentID);
        if (parent == null)
            return false;

        return isHidden(parent);
    }

    private static int getContainerQuantity(String name, RSItem[] cont1, LofiItem[] cont2) {
        if (tradeScreen() == 1) {
            for (RSItem i : cont1)
                if (i.getDefinition().getName().equals(name))
                    return i.getStack();
        } else if (tradeScreen() == 2) {
            for (LofiItem i : cont2)
                if (i.getName().equals(name))
                    return i.getStackSize();
        }
        return 0;
    }

    private static RSItem[] itemListToArray(ArrayList<RSItem> arr) {
        RSItem[] out = new RSItem[arr.size()];
        for (int i = 0; i < out.length; i++) {
            out[i] = arr.get(i);
        }
        return out;
    }

    public static RSItem[] getYourItemContainer_1() {
        if (tradeScreen() == 1) {
            return rsInterfaceComponentArrToRSItemArr(Interfaces.get(Ids.TRADE_WINDOW_PARENT_1, Ids.TRADE_WINDOW_ITEM_CONTAINER_YOU_1).getChildren());
        }
        return null;
    }

    public static RSItem[] getTheirItemContainer_1() {
        if (tradeScreen() == 1) {
            return rsInterfaceComponentArrToRSItemArr(Interfaces.get(Ids.TRADE_WINDOW_PARENT_1, Ids.TRADE_WINDOW_ITEM_CONTAINER_THEM_1).getChildren());
        }
        return null;
    }

    private static RSItem[] rsInterfaceComponentArrToRSItemArr(RSInterfaceComponent[] arr) {
        ArrayList<RSItem> items = new ArrayList<RSItem>();
        for (RSInterfaceComponent i : arr) {
            if (i.getComponentItem() > 0) {
                RSItem item = new RSItem(i.getComponentName(), i.getActions(), i.getIndex(), i.getComponentItem(), i.getComponentStack(), RSItem.TYPE.OTHER);
                item.setArea(i.getAbsoluteBounds());
                items.add(item);
            }
        }
        return itemListToArray(items);
    }

    public static LofiItem[] getYourItemContainer_2() {
        if (tradeScreen() == 2) {
            return stringToLofiItem(Interfaces.get(Ids.TRADE_WINDOW_PARENT_2, Ids.TRADE_WINDOW_ITEM_LIST_YOU_2).getText().replaceAll("<br>", "\r\n").replaceAll("<[[a-zA-Z-0-9]|=]*>", "").split("\r\n"));
        }
        return null;
    }

    public static LofiItem[] getTheirItemContainer_2() {
        if (tradeScreen() == 2) {
            return stringToLofiItem(Interfaces.get(Ids.TRADE_WINDOW_PARENT_2, Ids.TRADE_WINDOW_ITEM_LIST_THEM_2).getText().replaceAll("<br>", "\r\n").replaceAll("<[[a-zA-Z-0-9]|=]*>", "").split("\r\n"));
        }
        return null;
    }

    private static LofiItem[] stringToLofiItem(String[] arr) {
        LofiItem[] out = new LofiItem[arr.length];
        for (int i = 0; i < arr.length; i++) {
            out[i] = new LofiItem(arr[i]);
        }
        return out;

    }

    private static boolean offerXVisible() {
        return !isHidden(Interfaces.get(162, 32));
    }

    public static boolean hasItemAndQuantityInInventory(String name, int qty) {
        RSItem[] item = Inventory.find(name);
        if (item != null) {
            if (item[0].getStack() == qty) {
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    public static class LofiItem {
        private String name;
        private int stackSize;

        public LofiItem(String line) {
            line = line.replaceAll(",", "");
            String[] str = line.split(" x ");
            this.name = str[0];
            if (str.length > 1) {
                if (str[1].indexOf("(") > 0)
                    str[1] = str[1].substring(str[1].indexOf("(") + 1, str[1].indexOf(")"));
                this.stackSize = Integer.parseInt(str[1]);
            } else
                this.stackSize = 1;
        }

        public String getName() {
            return this.name;
        }

        public int getStackSize() {
            return stackSize;
        }
    }
}