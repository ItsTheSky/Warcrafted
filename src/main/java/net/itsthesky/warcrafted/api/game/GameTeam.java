package net.itsthesky.warcrafted.api.game;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface GameTeam {

	/**
	 * Get the team's color.
	 * @return The team's color.
	 */
	@NotNull String getColor();

	/**
	 * Get the current players of the team.
	 * @return The current players of the team.
	 */
	@NotNull List<GamePlayer> getPlayers();

}
