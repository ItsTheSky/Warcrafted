package net.itsthesky.warcrafted.util.gui;

import net.itsthesky.warcrafted.util.Pagination;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

public abstract class AbstractPaginationGUI<T> extends AbstractGUI implements PaginationGUI<T> {

	private final Map<UUID, Integer> playerPages;
	private Pagination<T> pagination;
	private Predicate<T> filter = t -> false;

	public AbstractPaginationGUI(@Nullable AbstractGUI parent, @NotNull Component title, int rows) {
		super(parent, title, rows);
		playerPages = new HashMap<>();
	}

	public final void init(Pagination<T> pagination) {
		this.pagination = pagination;
	}

	public final void removeFilter(Predicate<T> filter) {
		this.filter = filter;
	}

	@Override
	public int getPlayerPage(@NotNull Player player) {
		return playerPages.getOrDefault(player.getUniqueId(), 0);
	}

	@Override
	public List<T> getPlayerPagination(@NotNull Player player) {
		final Pagination<T> copy = new Pagination<>(pagination);
		copy.removeIf(e -> filter.test(e));

		return copy.getPage(getPlayerPage(player));
	}

	public int totalPages() {
		final Pagination<T> copy = new Pagination<>(pagination);
		copy.removeIf(e -> filter.test(e));
		return copy.totalPages();
	}

	@Override
	public void open(Player... players) {
		for (Player player : players)
			refresh(player);
		super.open(players);
	}

	public void clear() {
		setItems(null, null, getInnerBorder());
	}

	@Override
	public void setPlayerPage(@NotNull Player player, int page) {
		playerPages.put(player.getUniqueId(), page);
	}

	@Override
	public Pagination<T> getPagination() {
		return pagination;
	}

	public abstract void refresh(@NotNull Player player);

	public void usePagination(@NotNull AbstractGUI gui) {
		if (!(gui instanceof final AbstractPaginationGUI<?> paginationGUI))
			throw new IllegalArgumentException("The GUI must be a PaginationGUI!");
		for (UUID uuid : paginationGUI.playerPages.keySet())
			playerPages.put(uuid, paginationGUI.playerPages.get(uuid));
	}
}
