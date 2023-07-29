package net.itsthesky.warcrafted.api.game;

import net.itsthesky.warcrafted.internal.game.meta.entities.buildings.Building;
import net.itsthesky.warcrafted.internal.game.meta.entities.buildings.BuildingState;
import org.jetbrains.annotations.NotNull;

public interface GameBuilding {

	/**
	 * Get the meta building of this game building.
	 * @return The meta building of this game building.
	 */
	@NotNull Building getBuilding();

	/**
	 * Get the {@link GamePlayer player} who built this game building.
	 * @return The {@link GamePlayer player} who built this game building.
	 */
	@NotNull GamePlayer getBuilder();

	/**
	 * Get the state of this game building. It's based on its current state in the game.
	 * @return The state of this game building. It's based on its current state in the game.
	 */
	@NotNull BuildingState getState();

	/**
	 * Change the state of this game building.
	 * @param state The new state of this game building.
	 */
	void setState(@NotNull BuildingState state);

	/**
	 * Get the current health of this game building.
	 * @return The current health of this game building.
	 */
	int getHealth();

	/**
	 * Set the current health of this game building.
	 * @param health The new health of this game building.
	 */
	void setHealth(int health);

	/**
	 * Add the specified amount of health to this game building.
	 * @param health The amount of health to add.
	 */
	void addHealth(int health);

	/**
	 * Remove the specified amount of health to this game building.
	 * @param health The amount of health to remove.
	 */
	void removeHealth(int health);

	/**
	 * Get the date when the building is finished to be built.
	 * @return The date when the building is finished to be built, or -1 if the building is not being built.
	 */
	long getBuildDate();

	/**
	 * Set the date when the building is finished to be built.
	 * @param date The date when the building is finished to be built, or -1 if the building is not being built.
	 */
	void setBuildDate(long date);

	/**
	 * Get the repair date of this game building.
	 * @return The repair date of this game building, or -1 if the building is not being repaired.
	 */
	long getRepairDate();

	/**
	 * Start the repair of this game building.
	 */
	void startRepair();

	/**
	 * Called when the building is finished to be built, in order to manage {@link GamePlayer player's} stats.
	 */
	void onBuild();

	/**
	 * Called when the building is removed or destroyed to "reset" the {@link GamePlayer player's} stats.
	 */
	void onRemove();
}
