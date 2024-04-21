package net.itsthesky.warcrafted.core.game.core;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.itsthesky.warcrafted.Warcrafted;
import net.itsthesky.warcrafted.core.game.GamesManager;
import net.itsthesky.warcrafted.core.game.meta.entities.buildings.Building;
import net.itsthesky.warcrafted.core.game.meta.entities.buildings.BuildingState;
import net.itsthesky.warcrafted.core.game.meta.entities.buildings.BuildingTask;
import net.itsthesky.warcrafted.core.gui.buildings.SpecificBuildingGUI;
import net.itsthesky.warcrafted.util.Util;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Display;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@RequiredArgsConstructor
public class GameBuilding {

	private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

	private UUID uuid = UUID.randomUUID();

	private final Game game;

	private final Building building;
	private final GamePlayer player;
	private final Location location;

	private Location troopSpawnLocation;
	private BuildingState state;
	
	private GameBuildingTask task;
	private List<GameBuildingTask> tasks = new ArrayList<>();
	
	private int health;
	private long buildDate;
	private long repairDate;

	private float buildingIconRotation = 0;
	private ItemDisplay buildingIcon;
	private TextDisplay indicator;

	public GameBuilding(@NotNull GamePlayer player,
						@NotNull Building building,
						@NotNull Location location,
						boolean instant) {
		this.location = Util.toBlockLocation(location);
		this.troopSpawnLocation = location.clone().add(0, 1, 0);
		this.game = player.getGame();
		this.building = building;
		this.player = player;
		this.repairDate = -1;

		this.indicator = createIndicator();
		this.buildingIcon = createBuildingIcon();

		if (instant) {
			this.health = building.getHealth();
			this.state = BuildingState.CONSTRUCTED;
			this.buildDate = -1;

			onBuild();
		} else {
			this.health = 0;
			this.state = BuildingState.UNDER_CONSTRUCTION;
			this.buildDate = System.currentTimeMillis() + building.getBuildTime() * 1000L;

			this.location.getBlock().setType(Material.SCAFFOLDING);
		}

		GamesManager.addBuilding(this);
	}

	// ##################
	// Methods
	// ##################

	/**
	 * Called every second
	 */
	public void update() {

		// ============== Repair/building update
		if (state == BuildingState.UNDER_CONSTRUCTION) {
			if (System.currentTimeMillis() >= buildDate) {
				state = BuildingState.CONSTRUCTED;
				health = building.getHealth();
				buildDate = -1;

				onBuild();
			}
		} else if (state == BuildingState.UNDER_REPAIR) {
			if (System.currentTimeMillis() >= repairDate) {
				state = BuildingState.CONSTRUCTED;
				health = building.getHealth();
				repairDate = -1;
				onRepair();

				getPlayer().send("game.messages.building.repaired", building.getName());
			}
		}

		// ============== Update tasks
		if (task != null)
			task.update();

		// ============== Update indicator
		final Location indicatorLocation = location.clone().toCenterLocation().add(0, 1.5f, 0);
		final List<Component> lines = new ArrayList<>();
		lines.add(Warcrafted.lang().key("game.building.indicator." + state.getIndicatorKey(), building.getName()));

		// Progress bar (representing health when built/destroyed and build progress when building/repairing)
		final String character = "█"; final String uncompletedColor = "<gray>";
		final double currentValue = state == BuildingState.UNDER_CONSTRUCTION ? buildDate - System.currentTimeMillis() : health;
		final double maxValue = state == BuildingState.UNDER_CONSTRUCTION ? building.getBuildTime() * 1000L : building.getHealth();
		final String gradient = switch (state) {
			case DESTROYED -> "<gradient:red:dark_red>";
			case UNDER_CONSTRUCTION, UNDER_REPAIR -> "<gradient:yellow:gold>";
			case CONSTRUCTED -> "<gradient:green:dark_green>";
		};

		final int barSize = 15;
		final int completed = (int) Math.round((currentValue / maxValue) * barSize);
		final int uncompleted = barSize - completed;

		// if under construction, the coutner is reversed
		final String healthBar;
		if (state != BuildingState.UNDER_CONSTRUCTION) {
			healthBar = gradient + character.repeat(completed) + uncompletedColor + character.repeat(uncompleted);
		} else {
			healthBar = gradient + character.repeat(uncompleted) + uncompletedColor + character.repeat(completed);
		}

		lines.add(MINI_MESSAGE.deserialize(healthBar));

		// Tasks
		if (state == BuildingState.CONSTRUCTED) {
			final List<Component> tasks = new ArrayList<>();

			if (task == null) {
				tasks.add(Warcrafted.lang().key("game.building.indicator.no-task"));
			} else {
				tasks.add(Warcrafted.lang().key("game.building.indicator.task", task.getTask().getName(),
						task.getRemainingSeconds()));

				final String taskGradient = "<gradient:yellow:gold>";
				final String bar = Util.getProgressBar(task.getProgress(), task.getFinalProgress(), "▬", "<gray>", taskGradient, false);
				tasks.add(MINI_MESSAGE.deserialize(bar));
			}

			lines.add(Component.join(JoinConfiguration.separator(Component.newline()), tasks));
		}

		final Component joined = Component.join(JoinConfiguration.separator(Component.newline()), lines);
		indicator.text(joined);
		final int lineCount = lines.size();
		indicatorLocation.add(0, -0.3 * (lineCount - 1), 0);
		indicator.teleport(indicatorLocation);

		// ============== Update building icon
		buildingIconRotation += 0.5f;
		buildingIcon.setRotation(buildingIconRotation, 0);
	}

	public TextDisplay createIndicator() {
		return location.getWorld().spawn(location.clone().toCenterLocation().add(0, 1.2f, 0), TextDisplay.class, display -> {
			display.setSeeThrough(false);
			display.setBillboard(Display.Billboard.CENTER);
			display.setAlignment(TextDisplay.TextAlignment.CENTER);
			display.setShadowed(true);
			display.text(Component.text("sample"));
		});
	}

	public ItemDisplay createBuildingIcon() {
		return location.getWorld().spawn(location.clone().toCenterLocation().add(0, 2.5f, 0), ItemDisplay.class, display -> {
			if (building.getIcon() == null) {
				display.setItemStack(new ItemStack(Material.STONE));
			} else {
				display.setItemStack(Util.createHeadItem(building.getIcon()));
			}

			display.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.GROUND);
			display.setTransformationMatrix(new Matrix4f().scale(2.5f));
		});
	}

	public long getRemainingTime() {
		if (state == BuildingState.UNDER_CONSTRUCTION)
			return buildDate - System.currentTimeMillis();
		else if (state == BuildingState.UNDER_REPAIR)
			return repairDate - System.currentTimeMillis();
		return -1;
	}

	public void startRepair() {
		if (state == BuildingState.UNDER_CONSTRUCTION || state == BuildingState.UNDER_REPAIR)
			return;

		final int missingHealth = building.getHealth() - health;
		if (missingHealth == 0)
			return;

		state = BuildingState.UNDER_REPAIR;
		repairDate = System.currentTimeMillis() + (
				(long) (building.getBuildTime() / building.getHealth()) * missingHealth
		) * 1000L;
	}

	public void onBuild() {
		location.getBlock().setType(Material.BRICKS);
		getPlayer().send("game.messages.building.finished", building.getName());
	}

	/**
	 * Called when the player itself wants to remove the building.
	 */
	public void unconstruct() {
		GamesManager.removeBuilding(this);
	}

	public void onRemove() {

	}
	
	public void nextTask() {
		task = tasks.isEmpty() ? null : tasks.remove(0);

		if (task != null)
			task.start();
	}

	public long getTaskProgress() {
		if (task == null)
			return 0;

		return task.getProgress();
	}

	public long getTotalTaskProgress() {
		if (task == null)
			return 0;

		long totalMS = 0;
		for (final GameBuildingTask task : tasks)
			totalMS += task.getTask().getTime();
		totalMS += task.getProgress();

		return totalMS;
	}

	public void addTask(BuildingTask task) {
		final GameBuildingTask gameTask = new GameBuildingTask(this, task);
		tasks.add(gameTask);
		if (this.task == null)
			nextTask();
	}

	public void onRepair() {
		location.getBlock().setType(Material.BRICKS);
	}

	public void onRightClick(PlayerInteractEvent event, Player player) {
		if (state == BuildingState.UNDER_CONSTRUCTION || state == BuildingState.UNDER_REPAIR) {
			Warcrafted.lang().sendKey(event.getPlayer(), "game.messages.building.not-ready");
			return;
		}

		if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			if (player.getInventory().getItemInMainHand().getType() == Material.AIR) {
				if (player.isSneaking()) {
					if (state == BuildingState.UNDER_CONSTRUCTION || state == BuildingState.UNDER_REPAIR) {
						Warcrafted.lang().sendKey(event.getPlayer(), "game.messages.building.not-ready");
						return;
					}
					final int missingHealth = building.getHealth() - health;
					if (missingHealth == 0) {
						Warcrafted.lang().sendKey(event.getPlayer(), "game.messages.building.already-full");
						return;
					}

					startRepair();
				} else {
					new SpecificBuildingGUI(null, this, player).open(player);
				}
			}
		}
	}

	public void clear() {
		indicator.remove();
		buildingIcon.remove();

		location.getBlock().setType(Material.AIR);
	}
}
