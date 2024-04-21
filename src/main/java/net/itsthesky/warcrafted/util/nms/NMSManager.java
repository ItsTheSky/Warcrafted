package net.itsthesky.warcrafted.util.nms;

import lombok.Getter;
import net.itsthesky.warcrafted.Warcrafted;
import net.itsthesky.warcrafted.util.GlowingEntities;
import org.bukkit.Bukkit;

import java.lang.reflect.InvocationTargetException;

/**
 * Main manager class for NMS.
 * @author Sky
 */
public final class NMSManager {

	@Getter
	private static MobIA mobIA;
	@Getter
	private static GlowingEntities glowingEntities;


	// ########################################

	public static void loadClasses(Warcrafted warcrafted) {
		glowingEntities = new GlowingEntities(warcrafted);

		final String version = getVersion();

		mobIA = (MobIA) getNMSRepresentation(MobIA.class, version);

		warcrafted.getLogger().info("Loaded NMS classes for version " + version);
	}

	private static Object getNMSRepresentation(Class<?> baseClass, String version) {
		final String packageName = baseClass.getPackage().getName();
		final String className = baseClass.getSimpleName();
		final String fullName = packageName + "." + version + "." + className + "_" + version;

		try {
			final Class<?> clazz = Class.forName(fullName);
			return clazz.getConstructor().newInstance();
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | InvocationTargetException |
				 NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
	}

	public static String getVersion() {
		return Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
	}

}
