package net.itsthesky.warcrafted.langs;

import net.itsthesky.warcrafted.Warcrafted;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public final class Language {

	private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

	private final Properties properties;
	private final String code;

	public Language(@NotNull JavaPlugin plugin, @NotNull String code) throws IOException {
		this.code = code;

		final File langFile = new File(plugin.getDataFolder(), "lang/" + code + ".properties");
		final InputStream langStream = plugin.getResource("lang/" + code + ".properties");

		if (!langFile.exists() && langStream == null)
			plugin.saveResource("lang/" + code + ".properties", false);

		final InputStreamReader reader = new InputStreamReader(langStream);

		properties = new Properties();
		properties.load(reader);

		final Properties defaultConfig = new Properties();
		defaultConfig.load(reader);

		reader.close();
		langStream.close();

		final List<String> missingKeys = new ArrayList<>();
		for (String key : defaultConfig.stringPropertyNames()) {
			if (!properties.containsKey(key)) {
				properties.setProperty(key, defaultConfig.getProperty(key));
				missingKeys.add(key);
			}
		}

		if (!missingKeys.isEmpty()) {
			plugin.getLogger().info("Added " + missingKeys.size() + " missing keys to the language file '" + code + "'!");
			properties.store(Files.newOutputStream(langFile.toPath()), "Added missing keys");
		}

		plugin.getLogger().info("Loaded the language file '" + code + "'!");
	}

	public @NotNull String rawKey(@NotNull String key, @Nullable Object... replacements) {
		String rawKey = properties.getProperty(key, key);
		if (replacements == null || replacements.length == 0)
			return rawKey;

		int i = 1;
		for (Object replacement : replacements) {
			if (replacement instanceof Component)
				replacement = MINI_MESSAGE.serialize((Component) replacement);

			assert replacement != null;
			rawKey = rawKey.replace("%" + i, replacement.toString());
			i++;
		}

		return rawKey;
	}

	public @NotNull List<String> rawKeys(@NotNull String key, @Nullable Object... replacements) {
		List<String> rawKeys = new ArrayList<>();
		String rawKey = properties.getProperty(key, key);
		for (String rawKey1 : rawKey.split("\n")) {
			if (replacements == null || replacements.length == 0) {
				rawKeys.add(rawKey1);
				continue;
			}

			int i = 1;
			for (Object replacement : replacements) {
				if (replacement instanceof Component)
					replacement = MINI_MESSAGE.serialize((Component) replacement);

				assert replacement != null;
				rawKey1 = rawKey1.replace("%" + i, replacement.toString());
				i++;
			}

			rawKeys.add(rawKey1);
		}

		return rawKeys;
	}

	public @NotNull Component key(@NotNull String key, @Nullable Object... replacements) {
		return MINI_MESSAGE.deserialize(rawKey(key, replacements));
	}

	public @NotNull List<Component> keys(@NotNull String key, @Nullable Object... replacements) {
		List<Component> keys = new ArrayList<>();
		for (String rawKey : rawKeys(key, replacements)) {
			keys.add(MINI_MESSAGE.deserialize(rawKey));
		}
		return keys;
	}

	public void sendKey(@NotNull CommandSender sender, @NotNull String key, @Nullable Object... replacements) {
		final Component component = key(key, replacements);
		final Audience audience = Warcrafted.getInstance().adventure().sender(sender);

		audience.sendMessage(component);
	}

	public void sendKeys(@NotNull CommandSender sender, @NotNull String key, @Nullable Object... replacements) {
		final List<Component> components = keys(key, replacements);
		final Audience audience = Warcrafted.getInstance().adventure().sender(sender);

		for (Component component : components) {
			audience.sendMessage(component);
		}
	}

	public String getCode() {
		return code;
	}

	public Component parse(String text) {
		return MINI_MESSAGE.deserialize(text);
	}
}
