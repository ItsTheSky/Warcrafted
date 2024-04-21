package net.itsthesky.warcrafted.util.gui;

import net.itsthesky.warcrafted.util.Pagination;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

import java.util.List;

/**
 * A pagination GUI is where a lot of items have to be put in, but 9 * 6 is not big enough for them.
 * <br> It comes with several utilities methods for managing player's pages.
 */
public interface PaginationGUI<T> extends GUI {

    /**
     * Get the pagination of the GUI.
     * @return the pagination of the GUI.
     */
    @UnknownNullability Pagination<T> getPagination();

    /**
     * Get the current page of a specific player.
     * <br> If no page were registered for it, this will return 0 and initialize the player's page to 0.
     * @param player The player to get the page from
     * @return The current player's page
     */
    int getPlayerPage(final @NotNull Player player);

    /**
     * Change the current page of a specific player.
     * @param player The player to change the page
     * @param page The page to set to the player
     */
    void setPlayerPage(final @NotNull Player player, int page);

    List<T> getPlayerPagination(final @NotNull Player player);

    /**
     * Make a specific player go to the next page.
     * @param player The player to go to the next page
     */
    default void nextPage(final @NotNull Player player) {
        setPlayerPage(player, getPlayerPage(player) + 1);
    };

    /**
     * Make a specific player go to the previous page.
     * @param player The player to go to the previous page
     */
    default void previousPage(final @NotNull Player player) {
        setPlayerPage(player, getPlayerPage(player) - 1);
    };

}
