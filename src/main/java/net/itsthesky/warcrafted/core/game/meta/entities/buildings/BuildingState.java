package net.itsthesky.warcrafted.core.game.meta.entities.buildings;

public enum BuildingState {
	UNDER_CONSTRUCTION("building"),
	CONSTRUCTED("built"),
	DESTROYED("destroyed"), // not actually destroyed, it's just not usable anymore and need to be repaired
	UNDER_REPAIR("building"),
	;

	private final String indicator;

	BuildingState(String indicator) {
		this.indicator = indicator;
	}

	public String getIndicatorKey() {
		return indicator;
	}
}
