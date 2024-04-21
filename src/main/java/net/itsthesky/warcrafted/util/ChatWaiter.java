package net.itsthesky.warcrafted.util;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public final class ChatWaiter implements Listener {

	private static final Map<UUID, Consumer<AsyncPlayerChatEvent>> WAITING = new HashMap<>();

	public static void listen(Player player, Consumer<AsyncPlayerChatEvent> consumer) {
		WAITING.put(player.getUniqueId(), consumer);
	}

	public static void unlisten(Player player) {
		WAITING.remove(player.getUniqueId());
	}

	public static void unlistenAll() {
		WAITING.clear();
	}

	// ##########################################

	@EventHandler
	public void onChat(AsyncPlayerChatEvent e) {
		if (!WAITING.containsKey(e.getPlayer().getUniqueId())) return;
		e.setCancelled(true);
		WAITING.get(e.getPlayer().getUniqueId()).accept(e);
		WAITING.remove(e.getPlayer().getUniqueId());
	}

}
