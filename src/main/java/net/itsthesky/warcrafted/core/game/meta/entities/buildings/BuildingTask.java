package net.itsthesky.warcrafted.core.game.meta.entities.buildings;

import lombok.Getter;
import net.itsthesky.warcrafted.Warcrafted;
import net.itsthesky.warcrafted.core.game.core.GameBuilding;
import net.itsthesky.warcrafted.core.game.meta.entities.Resource;
import net.itsthesky.warcrafted.core.game.meta.entities.troops.Troop;
import net.itsthesky.warcrafted.util.Lazy;
import net.kyori.adventure.text.Component;
import org.bukkit.configuration.ConfigurationSection;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Those are mainly auto-generated. You can edit them, but be careful!
 */
public abstract class BuildingTask {

	private static final Map<String, Class<? extends BuildingTask>> TASKS = new HashMap<>();

	static {
		TASKS.put("recruit-troop", RecruitTroop.class);
	}

	public static BuildingTask parse(ConfigurationSection section) {
		final String type = section.getString("type");
		final Class<? extends BuildingTask> clazz = TASKS.get(type);
		if (clazz == null) {
			Warcrafted.getInstance().getLogger().warning("Cannot parse task: type not found!");
			return null;
		}

		try {
			final Method method = clazz.getMethod("parse", ConfigurationSection.class);
			return (BuildingTask) method.invoke(null, section);
		} catch (Exception e) {
			throw new RuntimeException("Cannot parse task: " + e.getMessage(), e);
		}
	}

	// ##############################################

	@Getter private final Type type;
	@Getter private final long time; // in MILLIS !!!

	protected BuildingTask(Type type, long time) {
		this.type = type;
		this.time = time;
	}

	public abstract void onFinish(GameBuilding building);

	public abstract ActivableState getActivableState(GameBuilding building);

	public abstract String getBase64Icon();

	public abstract Component getName();

	public abstract Map<Resource, Integer> getCost();

	public abstract List<Building> getRequirements();

	public List<Component> getDescription(GameBuilding building) {
		final ActivableState state = getActivableState(building);
		final List<String> rawLore = Warcrafted.lang().rawKeys("gui.building.specific.tasks.task.lore",
				Warcrafted.lang().key("gui.building.specific.tasks.task.lore." + state.name().toLowerCase().replace("_", "-")));
		final List<Component> lore = new ArrayList<>();

		// Resources
		final List<Component> resources = new ArrayList<>();
		for (final var entry : getCost().entrySet()) {
			final boolean hasEnough = building.getPlayer().getResource(entry.getKey()) >= entry.getValue();
			final String key = hasEnough
					? "gui.building.specific.upgrade.has-resource-item"
					: "gui.building.specific.upgrade.has-not-resource-item";
			resources.add(Warcrafted.lang().key(key, entry.getKey().getName(), building.getPlayer().getResource(entry.getKey()), entry.getValue()));
		}

		// Requirements
		final List<Component> requirements = new ArrayList<>();
		for (final var req : getRequirements()) {
			final boolean hasEnough = building.getPlayer().hasBuilding(req);

			final String key = hasEnough
					? "gui.building.specific.upgrade.has-requirements-item"
					: "gui.building.specific.upgrade.has-not-requirements-item";
			requirements.add(Warcrafted.lang().key(key, req.getName()));
		}
		if (requirements.isEmpty())
			requirements.add(Warcrafted.lang().key("gui.building.specific.upgrade.no-requirements"));

		// final lore
		for (String line : rawLore) {

			if (line.contains("%2")) {
				lore.addAll(resources);
				continue;
			}

			if (line.contains("%3")) {
				lore.addAll(requirements);
				continue;
			}

			lore.add(Warcrafted.lang().parse(line));
		}

		return lore;
	};

	public enum Type {
		RECRUIT_TROOP,
		CREATE_SPELL
	}

	public enum ActivableState {
		AVAILABLE,
		LOCKED,
		NOT_ENOUGH_RESOURCES,
	}

	// ##############################################

	public static class RecruitTroop extends BuildingTask {

		// Parser
		public static RecruitTroop parse(ConfigurationSection section) {
			final Troop troop = Warcrafted.getInstance().getTroopManager().getEntityById(section.getString("troop"));
			if (troop == null) {
				Warcrafted.getInstance().getLogger().warning("Cannot parse troop task: troop not found!");
				return null;
			}

			final long time = section.getLong("time") * 1000L;

			// Resource cost
			final Map<Resource, Integer> cost = new HashMap<>();
			if (section.isConfigurationSection("cost")) {
				final ConfigurationSection costSection = section.getConfigurationSection("cost");
				assert costSection != null;

				for (String key : costSection.getKeys(false)) {
					final var resource = Warcrafted.getInstance().getResourceManager().getEntityById(key);
					if (resource == null) {
						Warcrafted.getInstance().getLogger().warning("Cannot parse troop task: resource not found!");
						return null;
					}

					cost.put(resource, costSection.getInt(key));
				}
			}

			// Requirements
			final List<Lazy<Building>> requirements = new ArrayList<>();
			if (section.isList("requirements")) {
				for (String key : section.getStringList("requirements"))
					requirements.add(new Lazy<>(() -> Warcrafted.getInstance().getBuildingManager().getEntityById(key)));
			}


			return new RecruitTroop(troop, time, cost, requirements);
		}

		// ++++++++++ Class itself

		private final Troop troop;
		private final Map<Resource, Integer> cost = new HashMap<>();
		private final List<Lazy<Building>> requirements = new ArrayList<>();

		public RecruitTroop(Troop troop, long time, Map<Resource, Integer> cost, List<Lazy<Building>> requirements) {
			super(Type.RECRUIT_TROOP, time);
			this.troop = troop;
			this.cost.putAll(cost);
			this.requirements.addAll(requirements);
		}

		@Override
		public void onFinish(GameBuilding building) {
			Warcrafted.lang().sendKey(building.getPlayer().getPlayer().getPlayer(), "tasks.recruit-troop", troop.getName());

			building.getPlayer().addTroop(building, troop);
		}

		@Override
		public ActivableState getActivableState(GameBuilding building) {

			for (final var entry : getCost().entrySet()) {
				if (building.getPlayer().getResource(entry.getKey()) < entry.getValue())
					return ActivableState.NOT_ENOUGH_RESOURCES;
			}

			// Requirements
			for (final var req : getRequirements()) {
				if (!building.getPlayer().hasBuilding(req))
					return ActivableState.LOCKED;
			}

			return ActivableState.AVAILABLE;
		}

		@Override
		public String getBase64Icon() {
			return troop.getHead();
		}

		@Override
		public Component getName() {
			return Component.text("Recruiting ").append(troop.getName());
		}

		@Override
		public Map<Resource, Integer> getCost() {
			return cost;
		}

		@Override
		public List<Building> getRequirements() {
			return requirements.stream().map(Lazy::get).collect(Collectors.toList());
		}
	}

}
