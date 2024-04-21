package net.itsthesky.warcrafted.util;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import de.tr7zw.changeme.nbtapi.NBTCompound;
import de.tr7zw.changeme.nbtapi.NBTContainer;
import de.tr7zw.changeme.nbtapi.NBTItem;
import de.tr7zw.changeme.nbtapi.NBTListCompound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.bukkit.ChatColor.COLOR_CHAR;

public class ItemBuilder {

	// ############ Global Values ############ //
	private final List<Component> lore;
	private final List<ItemFlag> flags;
	private final Map<Enchantment, Integer> enchantments;
	private final Multimap<Attribute, AttributeModifier> attributes;
	private final Map<String, String> properties;
	private final NBTCompound compound;
	private Component name;
	private int amount;
	private String customId;
	private Material material;
	private NBTCompound skullData;

	// ############ Specific Values ############ //
	private final List<org.bukkit.block.banner.Pattern> patterns;
	private OfflinePlayer owner;
	private Color color;

	// ############ Constructors ############ //

	/**
	 * Constructs a new ItemBuilder with the given {@link Material}.
	 * @param material The {@link Material} to use.
	 */
	public ItemBuilder(@NotNull Material material) {
		this.material = material;
		this.amount = 1;

		this.lore = new LinkedList<>();
		this.flags = new LinkedList<>();
		this.patterns = new LinkedList<>();
		this.enchantments = new HashMap<>();
		this.properties = new HashMap<>();
		this.attributes = ArrayListMultimap.create();
		this.compound = new NBTContainer();
	}

	public ItemBuilder(@NotNull ItemBuilder other) {
		this.material = other.material;
		this.amount = other.amount;
		this.name = other.name;
		this.customId = other.customId;
		this.lore = new LinkedList<>(other.lore);
		this.flags = new LinkedList<>(other.flags);
		this.patterns = new LinkedList<>(other.patterns);
		this.enchantments = new HashMap<>(other.enchantments);
		this.properties = new HashMap<>(other.properties);
		this.attributes = ArrayListMultimap.create(other.attributes);
		this.compound = new NBTContainer(other.compound.toString());
		this.owner = other.owner;
		this.color = other.color;
	}

	/**
	 * Constructs a new ItemBuilder with the given {@link ItemStack}.
	 * @param item The {@link ItemStack} to use.
	 */
	public ItemBuilder(@NotNull ItemStack item) {
		item = item.clone();
		this.amount = item.getAmount();
		this.material = item.getType();
		final ItemMeta meta = item.getItemMeta();
		if (meta == null)
			throw new IllegalArgumentException("ItemStack must have an ItemMeta!");

		this.lore = new LinkedList<>(meta.hasLore() ? meta.lore() : new LinkedList<>());
		this.flags = new LinkedList<>(meta.getItemFlags());
		this.properties = new HashMap<>();

		this.attributes = meta.hasAttributeModifiers() ? ArrayListMultimap.create(meta.getAttributeModifiers()) : ArrayListMultimap.create();

		if (meta instanceof final BannerMeta bannerMeta)
			this.patterns = new LinkedList<>(bannerMeta.getPatterns());
		else
			this.patterns = new LinkedList<>();

		this.enchantments = new HashMap<>(meta.getEnchants());
		this.compound = new NBTItem(item);

		if (meta instanceof final SkullMeta skullMeta)
			this.owner = skullMeta.getOwningPlayer();
		else
			this.owner = null;

		if (meta.hasDisplayName())
			this.name = meta.displayName();
		else
			this.name = null;

		if (meta instanceof final LeatherArmorMeta leatherArmorMeta)
			this.color = leatherArmorMeta.getColor();
		else
			this.color = null;
	}

	public static boolean areEquals(ItemStack item1, ItemStack item2) {
		if (item1 == null || item1.getType().isAir())
			return item2 == null || item2.getType().isAir();
		if (item2 == null || item2.getType().isAir())
			return false;
		final NBTItem nbtItem1 = new NBTItem(item1);
		final NBTItem nbtItem2 = new NBTItem(item2);

		if (nbtItem1.hasKey("custom-id") && nbtItem2.hasKey("custom-id"))
			return nbtItem1.getString("custom-id").equals(nbtItem2.getString("custom-id"));

		return item1.isSimilar(item2);
	}

	// ############ Utilities ############ //

	/**
	 * Either this {@link ItemBuilder} is similar to another {@link ItemStack} or not.
	 * <br> If the provided item was an {@link ItemBuilder} and its custom id was set, it will be compared to the custom id of this {@link ItemBuilder}.
	 * <br> Else, the built items stack will be compared, Bukkit will that using {@link ItemStack#isSimilar(ItemStack)}.
	 * @param other The other {@link ItemStack} to compare to.
	 * @return Whether this {@link ItemBuilder} is similar to the other {@link ItemStack}.
	 */
	public boolean isEqual(@NotNull ItemStack other) {
		if (other == null)
			return true;
		if (other.getType().isAir())
			return material.isAir();
		final NBTItem nbtItem = new NBTItem(other);
		final String customId = nbtItem.hasKey("custom-id") ? nbtItem.getString("custom-id") : null;
		if (customId == null || this.customId == null)
			return this.build().isSimilar(other);
		return customId.equals(this.customId);
	}

	public boolean isEqual(@NotNull ItemBuilder other) {
		return this.isEqual(other.build());
	}

	// ############ Changers ############ //

	/**
	 * Adds a new NBT value with a specific key. Because on how NBT are managed, you have to define yourself the setter method.
	 * @param changer The changer to change the NBT compound.
	 * @return The ItemBuilder instance.
	 */
	public ItemBuilder withNBTValue(@NotNull Consumer<NBTCompound> changer) {
		changer.accept(this.compound);
		return this;
	}

	/**
	 * Sets a specific amount for this item. The input amount mus tbe greater than 0.
	 * <br> This will only apply once the item is built!
	 * @param amount The amount to set.
	 * @return The ItemBuilder instance.
	 */
	public ItemBuilder withAmount(int amount) {
		this.amount = amount;
		return this;
	}

	/**
	 * Sets the name of this item.
	 * @param name The name to set.
	 * @return The ItemBuilder instance.
	 */
	public ItemBuilder withName(@NotNull Component name) {
		this.name = name;
		return this;
	}

	/**
	 * Sets the name of the item.
	 * <br><b>Deprecated: Use {@link #withName(Component)} instead.</b>
	 * @param name The name of the item.
	 * @return The ItemBuilder instance.
	 */
	@Deprecated
	public ItemBuilder withName(@NotNull String name) {
		return withName(Component.text(name));
	}

	/**
	 * Adds a {@link org.bukkit.block.banner.Pattern} to the item.
	 * <b>This only works with banners!</b>
	 * @param pattern The {@link org.bukkit.block.banner.Pattern} to add.
	 * @return The ItemBuilder instance.
	 */
	public ItemBuilder withPattern(@NotNull org.bukkit.block.banner.Pattern pattern) {
		this.patterns.add(pattern);
		return this;
	}

	/**
	 * Sets a specific Minecraft Texture UUID for the head. This will override the default head.
	 * <b>The material of the ItemBuilder MUST be a {@link Material#PLAYER_HEAD}!</b>
	 * <br> This will basically format the input texture into the correct JSON format, then encode it in Base64.
	 * @param texture The texture to set.
	 * @return The ItemBuilder instance.
	 */
	public ItemBuilder withTexture(@NotNull String texture) {
		return withBase64(buildBase64(texture));
	}

	public static String buildBase64(String texture) {
		final String rawTexture = "{\"textures\":{\"SKIN\":{\"url\":\"http://textures.minecraft.net/texture/"+texture+"\"}}}";
		return Base64.getEncoder().encodeToString(rawTexture.getBytes());
	}

	/**
	 * Sets a specific Base64 value to the head. This will be used to set the skin of the head.
	 * <b>The material of the ItemBuilder MUST be a {@link Material#PLAYER_HEAD}!</b>
	 * @param base64 The Base64 value to set.
	 * @return The ItemBuilder instance.
	 */
	public ItemBuilder withBase64(@NotNull String base64) {
		final NBTCompound skull = this.compound.addCompound("SkullOwner");
		skull.setString("Name", "Default Name");
		final UUID uuid = new UUID(base64.substring(base64.length() - 20).hashCode(),
				base64.substring(base64.length() - 10).hashCode());
		skull.setString("Id", uuid.toString());
		final NBTListCompound texture = skull.addCompound("Properties").getCompoundList("textures").addCompound();
		texture.setString("Value",  base64);
		return this;
	}

	/**
	 * Sets the custom ID of this item, or remove the current one.
	 * <br> This allows a better checking for {@link ItemBuilder#isEqual(ItemStack)}.
	 * @param customId The custom ID to set.
	 * @return The ItemBuilder instance.
	 */
	public ItemBuilder withCustomId(@Nullable String customId) {
		this.customId = customId;
		if (customId == null)
			compound.removeKey("custom-id");
		else
			compound.setString("custom-id", customId);
		return this;
	}

	/**
	 * Adds a new {@link Attribute} with a specific {@link AttributeModifier}.
	 * <br> Quicker and cleaner way to add attributes than {@link ItemBuilder#withAttribute(Attribute, AttributeModifier)}
	 * @param attribute The {@link Attribute} to add.
	 * @param name The name of the {@link AttributeModifier}.
	 * @param value The value of the {@link AttributeModifier}.
	 * @param operation The {@link AttributeModifier.Operation} of the {@link AttributeModifier}.
	 * @return The ItemBuilder instance.
	 */
	public ItemBuilder withAttribute(Attribute attribute, String name, double value, AttributeModifier.Operation operation) {
		return withAttribute(attribute, new AttributeModifier(name, value, operation));
	}

	/**
	 * Adds a new {@link Attribute} with a specific {@link AttributeModifier}.
	 * @param attribute The {@link Attribute} to add.
	 * @param modifier The {@link AttributeModifier} to add.
	 * @return The ItemBuilder instance.
	 */
	public ItemBuilder withAttribute(Attribute attribute, AttributeModifier modifier) {
		this.attributes.put(attribute, modifier);
		return this;
	}

	/**
	 * Chance the color of the {@link LeatherArmorMeta leather armor}.
	 * @param color The color to set.
	 * @return The ItemBuilder instance.
	 */
	public ItemBuilder withColor(Color color) {
		this.color = color;
		return this;
	}

	/**
	 * Adds multiple pattern to the item.
	 * <br> The values provided must be in the following format: {@link org.bukkit.DyeColor}, {@link org.bukkit.block.banner.PatternType}
	 * <b>This only works with banners!</b>
	 * @param values The values to add.
	 * @return The ItemBuilder instance.
	 */
	public ItemBuilder withPatterns(@NotNull Object... values) {
		if (values.length % 2 != 0)
			throw new IllegalArgumentException("The values must be in the following format: {@link org.bukkit.DyeColor}, {@link org.bukkit.block.banner.PatternType}");
		for (int i = 0; i < values.length; i += 2)
			this.patterns.add(new org.bukkit.block.banner.Pattern((org.bukkit.DyeColor) values[i], (org.bukkit.block.banner.PatternType) values[i + 1]));
		return this;
	}

	/**
	 * Sets the material of the item.
	 *
	 * @param material The material of the item.
	 * @return The ItemBuilder instance.
	 */
	public ItemBuilder withMaterial(@NotNull Material material) {
		this.material = material;
		return this;
	}

	/**
	 * Clear the current lore of the item.
	 * @return The ItemBuilder instance.
	 */
	public ItemBuilder clearLore() {
		this.lore.clear();
		return this;
	}

	/**
	 * Sets the lore of the item.
	 * @param lines The lore of the item.
	 * @return The ItemBuilder instance.
	 */
	public ItemBuilder withLore(@NotNull Component... lines) {
		this.lore.addAll(Arrays.asList(lines));
		return this;
	}

	/**
	 * Sets the lore of the item
	 * @param lines The lore of the item.
	 * @return The ItemBuilder instance.
	 */
	public ItemBuilder withLore(@NotNull List<Component> lines) {
		this.lore.addAll(lines);
		return this;
	}

	/**
	 * Adds lines to the lore of the item.
	 * <br><b>Deprecated: Use {@link #withLore(Component...)} instead.</b>
	 * @param lines The lines of lore to add.
	 * @return The ItemBuilder instance.
	 */
	@Deprecated
	public ItemBuilder withLore(@NotNull String... lines) {
		return withLore(Stream.of(lines).map(Component::text).toArray(Component[]::new));
	}

	/**
	 * Adds one or more {@link ItemFlag} to the item.
	 *
	 * @param flags The {@link ItemFlag} to add.
	 * @return The ItemBuilder instance.
	 */
	public ItemBuilder withFlags(@NotNull ItemFlag... flags) {
		this.flags.addAll(List.of(flags));
		return this;
	}

	/**
	 * Adds a glowing effect to the current item.
	 * <br>It will basically add {@link Enchantment#LURE} with level 1, and enable {@link ItemFlag#HIDE_ENCHANTS} flag.
	 * @return The ItemBuilder instance.
	 */
	public ItemBuilder withGlowing() {
		this.flags.add(ItemFlag.HIDE_ENCHANTS);
		this.enchantments.put(Enchantment.LURE, 1);
		return this;
	}

	/**
	 * Adds multiple {@link Enchantment} followed by their respective levels to the item.
	 * Example:
	 * <br>
	 * {@code withEnchantments(Enchantment.DAMAGE_ALL, 1, Enchantment.DURABILITY, 2);}
	 * @param values The {@link Enchantment}s & their level to add.
	 * @return The ItemBuilder instance.
	 */
	public ItemBuilder withEnchantments(@NotNull Object... values) {
		if (values.length % 2 != 0)
			throw new IllegalArgumentException("The enchantments values must be in pairs.");
		for (int i = 0; i < values.length; i += 2) {
			Enchantment enchantment = (Enchantment) values[i];
			if (enchantment == null)
				throw new IllegalArgumentException("The enchantment " + values[i] + " is not valid.");
			int level = (Integer) values[i + 1];
			if (level < 1 || level > enchantment.getMaxLevel())
				throw new IllegalArgumentException("The level " + values[i + 1] + " is not valid.");
			this.enchantments.put(enchantment, level);
		}
		return this;
	}

	/**
	 * Properties are like tag, that will be replaced by a specific value.
	 * <br> It will replace these in the name and the lore of the item. If none were set before, this will do nothing.
	 * <br> Provide your properties using this format: name, value.
	 * <br> the property's name will be surrounded by {@code %}.
	 * <br> Example: {@code withProperties("name", "Hey");}
	 * @param properties The properties to replace.
	 * @return The ItemBuilder instance.
	 */
	public ItemBuilder withProperties(Object... properties) {
		if (properties.length % 2 != 0)
			throw new IllegalArgumentException("The properties values must be in pairs.");
		for (int i = 0; i < properties.length; i += 2) {
			final String property = (String) properties[i];
			if (property == null)
				throw new IllegalArgumentException("The property " + properties[i] + " is not valid.");
			final String value = properties[i + 1] == null ? null : properties[i + 1] + "";
			if (value == null)
				throw new IllegalArgumentException("The value " + properties[i + 1] + " is not valid.");
			this.properties.put(property, value);
		}
		return this;
	}

	/**
	 * Adds an {@link Enchantment} with a specific level to the item.
	 *
	 * @param enchantment The {@link Enchantment} to add.
	 * @param level The level of the {@link Enchantment}.
	 * @return The ItemBuilder instance.
	 */
	public ItemBuilder withEnchantment(@NotNull Enchantment enchantment, int level) {
		this.enchantments.put(enchantment, level);
		return this;
	}

	/**
	 * Sets a specific {@link OfflinePlayer} to be the owner of the item.
	 * <b>This only works with skull!</b>
	 * @param owner The {@link OfflinePlayer} to set as the owner.
	 *              If null, the owner will be removed.
	 * @return The ItemBuilder instance.
	 */
	public ItemBuilder withOwner(@NotNull OfflinePlayer owner) {
		this.owner = owner;
		return this;
	}

	/**
	 * Give the built item to a specific {@link Player}.
	 * @param player The {@link Player} to give the item to.
	 */
	public void give(@NotNull Player player) {
		player.getInventory().addItem(build());
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		ItemBuilder that = (ItemBuilder) o;

		if (!lore.equals(that.lore)) return false;
		if (!flags.equals(that.flags)) return false;
		if (!enchantments.equals(that.enchantments)) return false;
		if (!attributes.equals(that.attributes)) return false;
		if (!properties.equals(that.properties)) return false;
		if (!compound.equals(that.compound)) return false;
		if (name != null ? !name.equals(that.name) : that.name != null) return false;
		if (material != that.material) return false;
		if (!patterns.equals(that.patterns)) return false;
		return owner != null ? owner.equals(that.owner) : that.owner == null;
	}

	@Override
	public String toString() {
		return customId == null ? "ItemBuilder{" +
				"lore=" + lore +
				", flags=" + flags +
				", enchantments=" + enchantments +
				", attributes=" + attributes +
				", properties=" + properties +
				", compound=" + compound +
				", name='" + name + '\'' +
				", amount=" + amount +
				", customId='" + customId + '\'' +
				", material=" + material +
				", patterns=" + patterns +
				", owner=" + owner +
				'}' : "ItemBuilder{customId='" + customId + '\'' + '}';
	}

	@Override
	public int hashCode() {
		int result = lore.hashCode();
		result = 31 * result + flags.hashCode();
		result = 31 * result + enchantments.hashCode();
		result = 31 * result + attributes.hashCode();
		result = 31 * result + properties.hashCode();
		result = 31 * result + compound.hashCode();
		result = 31 * result + (name != null ? name.hashCode() : 0);
		result = 31 * result + material.hashCode();
		result = 31 * result + patterns.hashCode();
		result = 31 * result + (owner != null ? owner.hashCode() : 0);
		return result;
	}

	/**
	 * Builds the item.
	 * @return The built item.
	 */
	@NotNull
	public ItemStack build() {
		ItemStack item = new ItemStack(material, amount);

		if (item.getType().isAir())
			return new ItemStack(Material.AIR);

		final ItemMeta meta = item.getItemMeta();
		if (meta == null)
			throw new IllegalArgumentException("The item meta of " + material + " is not valid.");

		if (meta instanceof final LeatherArmorMeta armorMeta && color != null)
			armorMeta.setColor(color);

		if (!flags.isEmpty()) meta.addItemFlags(flags.toArray(new ItemFlag[0]));
		if (!enchantments.isEmpty()) enchantments.forEach((enchantment, level) -> meta.addEnchant(enchantment, level, true));

		if (meta instanceof BannerMeta && !patterns.isEmpty())
			for (org.bukkit.block.banner.Pattern pattern : patterns)
				((BannerMeta) meta).addPattern(pattern);

		attributes.asMap().forEach((attribute, modifiers) -> {
			for (AttributeModifier modifier : modifiers)
				meta.addAttributeModifier(attribute, modifier);
		});

		if (meta instanceof SkullMeta && owner != null)
			((SkullMeta) meta).setOwningPlayer(owner);

		item.setItemMeta(meta);
		item.setAmount(amount);

		final NBTItem nbtItem = new NBTItem(item);
		nbtItem.mergeCompound(this.compound);
		item = nbtItem.getItem();

		final ItemMeta meta1 = item.getItemMeta();
		if (name != null) meta1.displayName(name);
		meta1.lore(this.lore);
		item.setItemMeta(meta1);

		return item;
	}

	public static String translateHexColorCodes(String startTag, String endTag, String message)
	{
		final Pattern hexPattern = Pattern.compile(startTag + "([A-Fa-f0-9]{6})" + endTag);
		final Matcher matcher = hexPattern.matcher(message);
		final StringBuilder buffer = new StringBuilder(message.length() + 4 * 8);
		while (matcher.find())
		{
			String group = matcher.group(1);
			matcher.appendReplacement(buffer, COLOR_CHAR + "x"
					+ COLOR_CHAR + group.charAt(0) + COLOR_CHAR + group.charAt(1)
					+ COLOR_CHAR + group.charAt(2) + COLOR_CHAR + group.charAt(3)
					+ COLOR_CHAR + group.charAt(4) + COLOR_CHAR + group.charAt(5)
			);
		}
		return matcher.appendTail(buffer).toString();
	}

}
