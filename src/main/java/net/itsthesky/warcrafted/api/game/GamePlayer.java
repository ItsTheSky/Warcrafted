package net.itsthesky.warcrafted.api.game;

import net.itsthesky.warcrafted.internal.game.meta.entities.buildings.BuildingState;
import net.itsthesky.warcrafted.internal.game.meta.entities.Resource;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Represents data of a player in a {@link net.itsthesky.warcrafted.internal.game.core.Game game}.
 */
public interface GamePlayer {

	/**
	 * Get the team of this player.
	 * @return The team of this player.
	 */
	@NotNull GameTeam getTeam();

	/**
	 * Get the amount of resource of the specified type.
	 * @param type The type of resource.
	 * @return The amount of resource of the specified type.
	 */
	int getResource(@NotNull Resource type);

	/**
	 * Set the amount of resource of the specified type.
	 * @param type The type of resource.
	 */
	void setResource(@NotNull Resource type, int amount);

	/**
	 * Add the specified amount of resource of the specified type.
	 * @param type The type of resource.
	 * @param amount The amount of resource to add.
	 */
	void addResource(@NotNull Resource type, int amount);

	/**
	 * Remove the specified amount of resource of the specified type.
	 * @param type The type of resource.
	 * @param amount The amount of resource to remove.
	 */
	void removeResource(@NotNull Resource type, int amount);

	/**
	 * Gets the current buildings of the player.
	 * It contains all buildings, even the destroyed ones ({@link GameBuilding#getState()} will return {@link BuildingState#DESTROYED}).
	 * @return The current buildings of the player.
	 */
	@NotNull List<GameBuilding> getBuildings();
}
