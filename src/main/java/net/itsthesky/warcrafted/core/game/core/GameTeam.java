package net.itsthesky.warcrafted.core.game.core;

import lombok.Getter;

import java.util.List;

@Getter
public class GameTeam {

	public static final String[] COLORS = {
			"red", "blue", "gold", "green", "yellow", "aqua", "dark_aqua", "dark_blue", "dark_gray", "dark_green", "dark_purple", "dark_red", "light_purple", "white", "gray", "black"
	};

	private final Game game;

	private final String color;

	public GameTeam(Game game, String color) {
		if (!List.of(COLORS).contains(color))
			throw new IllegalArgumentException("Invalid color: " + color);

		this.game = game;
		this.color = color;
	}

}
