package net.itsthesky.warcrafted.core.game.core;

import lombok.Getter;
import net.itsthesky.warcrafted.core.game.meta.entities.buildings.BuildingTask;

@Getter
public class GameBuildingTask {

	private final GameBuilding building;
	private final BuildingTask task;

	private long finishDate;

	public GameBuildingTask(GameBuilding building, BuildingTask task) {
		this.building = building;
		this.task = task;
	}

	public void start() {
		finishDate = System.currentTimeMillis() + task.getTime();
	}

	public boolean isNotRunning() {
		return finishDate == -1;
	}

	public void update() {
		if (isNotRunning())
			return;

		if (finishDate != -1 && System.currentTimeMillis() >= finishDate) {
			task.onFinish(building);
			building.nextTask();
		}
	}

	public long getProgress() {
		if (isNotRunning())
			return 0;

		return System.currentTimeMillis() - (finishDate - task.getTime());
	}

	public int getRemainingSeconds() {
		if (isNotRunning())
			return 0;

		return (int) ((finishDate - System.currentTimeMillis()) / 1000);
	}

	public long getFinalProgress() {
		if (isNotRunning())
			return 0;

		return task.getTime();
	}
}
