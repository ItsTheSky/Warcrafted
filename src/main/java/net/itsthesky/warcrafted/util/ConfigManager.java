package net.itsthesky.warcrafted.util;

import lombok.Getter;
import net.itsthesky.warcrafted.Warcrafted;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Getter
public final class ConfigManager<T> {

	private final JavaPlugin plugin;
	private final Map<String, T> entities;
	private final Class<T> clazz;
	private final String fileName;

	public ConfigManager(JavaPlugin plugin, Class<T> clazz, String fileName) {
		this.plugin = plugin;
		this.entities = new HashMap<>();
		this.clazz = clazz;
		this.fileName = fileName;

		reload();
	}

	public void reload() {
		final File config = new File(plugin.getDataFolder(), fileName);
		if (!config.exists())
			plugin.saveResource(fileName, false);

		final YamlConfiguration yaml = YamlConfiguration.loadConfiguration(config);
		for (String key : yaml.getKeys(false))
		{
			try {

				final Constructor<T> constructor = clazz.getConstructor(ConfigManager.class, ConfigurationSection.class);
				constructor.setAccessible(true);

				entities.put(key, constructor.newInstance(this, Objects.requireNonNull(yaml.getConfigurationSection(key))));

			} catch (Exception ex) {
				throw new RuntimeException("Cannot load " + clazz.getSimpleName() + " with id " + key, ex);
			}
		}

		plugin.getLogger().info("Loaded " + entities.size() + " " + clazz.getSimpleName().toLowerCase() + "!");
	}

	public T getEntityById(String id) {
		return entities.get(id);
	}

	public Warcrafted getPlugin() {
		return (Warcrafted) plugin;
	}

	public List<String> getEntityIds() {
		return List.copyOf(entities.keySet());
	}

	public List<T> getEntities() {
		return List.copyOf(entities.values());
	}
}
