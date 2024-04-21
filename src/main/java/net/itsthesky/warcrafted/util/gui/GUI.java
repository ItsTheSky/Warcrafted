package net.itsthesky.warcrafted.util.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.IntStream;

/**
 * Represents a GUI that can be shown to a player.
 */
public interface GUI extends InventoryHolder {

    /**
     * Gets the possibly-null parent of this GUI.
     * @return The parent of this GUI.
     */
    @Nullable GUI getParent();

    @NotNull GUI createCopy(@NotNull Player player);

    /**
     * Set a specific item inside the GUI.
     * <br> It will also update it in the opened inventory, so viewers will see the refresh.
     * @param slot The slot of the item
     * @param item The possibly-null item to set. If It's null, then the slot will be cleared.
     * @param consumer The possibly-null code to run once a click happens on this slot.
     */
    void setItem(int slot, final @Nullable Supplier<ItemStack> item, final @Nullable Consumer<InventoryClickEvent> consumer);

    /**
     * Utility method. Set the same item with the same click event for multiple slots.
     * @param item The possibly-null item to set. If It's null, then the slots will be cleared.
     * @param consumer The possibly-null code to run once a click happens on any slot.
     * @param slots The slots to fill in
     */
    default void setItems(final @Nullable Supplier<ItemStack> item, final @Nullable Consumer<InventoryClickEvent> consumer, int... slots) {
        for (final int slot : slots)
            setItem(slot, item, consumer);
    }

    default void setColumns(final @Nullable Supplier<ItemStack> item, final @Nullable Consumer<InventoryClickEvent> consumer, int... columns) {
        for (final int column : columns)
            setItems(item, consumer, IntStream.range(column, getInventory().getSize()).filter(i -> i % 9 == column).toArray());
    }

    /**
     * Refresh the item in the specified slot.
     * @param slot The slot to refresh
     */
    void refreshSlot(int slot);

    /**
     * Set automatic refresh for a specific slot.
     * @param slot The slot to refresh
     * @param delay The delay between each refresh, in ticks
     */
    void setupRefresh(int slot, long delay);

    /**
     * Open this GUI to one or more players.
     * <br> The players have to be online in order to make them open the GUI!
     * @param players The players to open the GUI
     */
    void open(Player... players);

    /**
     * Get the bukkit {@link Inventory} instance of this GUI.
     * <br> Remember, one inventory can share multiple viewers!
     * @return The inventory represented for this GUI
     */
    @Override
    @NotNull Inventory getInventory();

    /**
     * Get borders of the inventory. If the inventory size is under 27, all slots are returned.
     *
     * @return inventory borders
     */
    default int[] getBorders() {
        int size = getInventory().getSize();
        return IntStream.range(0, size).filter(i -> size < 27 || i < 9 || i % 9 == 0 || (i - 8) % 9 == 0 || i > size - 9).toArray();
    }

    /**
     * Gets the inner slots of an inventory.
     * It represents the slots that are not borders.
     * @return The inner slots of the inventory.
     */
    default int[] getInnerBorder() {
        int size = getInventory().getSize();
        final List<Integer> ints = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            int finalI = i;
            if (IntStream.of(getBorders()).anyMatch(i2 -> i2 == finalI))
                continue;
            ints.add(i);
        }
        return ints.stream().mapToInt(i -> i).toArray();
    }

    /**
     * Get corners of the inventory.
     *
     * @return inventory corners
     */
    default int[] getCorners() {
        int size = getInventory().getSize();
        return IntStream.range(0, size).filter(i -> i < 2 || (i > 6 && i < 10) || i == 17 || i == size - 18 || (i > size - 11 && i < size - 7) || i > size - 3).toArray();
    }


    /**
     * Fired when any click event is happening, inside or outside this GUI.
     * @param event The click event
     * @param inside Either the click was inside the GUI or in the player's inventory
     */
    default void onClick(final @NotNull InventoryClickEvent event, boolean inside) { }

    /**
     * Fired when this GUI is opened by a player.
     * @param event The inventory open event
     */
    default void onOpen(final @NotNull InventoryOpenEvent event) { }

    /**
     * Fired when this GUI is closed by a player.
     * @param event The inventory close event
     */
    default void onClose(final @NotNull InventoryCloseEvent event) { }

}
