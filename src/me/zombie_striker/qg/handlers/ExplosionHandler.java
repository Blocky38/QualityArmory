package me.zombie_striker.qg.handlers;

import org.bukkit.Location;

import me.zombie_striker.qg.Main;

public class ExplosionHandler {

	// private static List<Material> indestruct = Arrays.asList(Material.OBSIDIAN,
	// Material.BEDROCK, Material.OBSERVER,
	// Material.FURNACE, Material.WATER, Material.STATIONARY_LAVA, Material.LAVA,
	// Material.STATIONARY_WATER,
	// Material.COMMAND, Material.COMMAND_CHAIN, Material.COMMAND_MINECART,
	// Material.COMMAND_REPEATING);

	public static void handleExplosion(Location origin, int radius, int power) {
			origin.getWorld().createExplosion(origin, power);
		/**
		 * for (int x = origin.getBlockX() - radius; x < origin.getBlockX() + radius;
		 * x++) { for (int y = origin.getBlockY() - radius; y < origin.getBlockY() +
		 * radius; y++) { for (int z = origin.getBlockZ() - radius; z <
		 * origin.getBlockZ() + radius; z++) { Location temp = new
		 * Location(origin.getWorld(), x, y, z); if (temp.distance(origin) <= radius &&
		 * !indestruct.contains(temp.getBlock().getType())) { if
		 * (Main.enableExplosionDamageDrop) for (ItemStack drop :
		 * origin.getBlock().getDrops()) origin.getWorld().dropItem(origin, drop);
		 * origin.getBlock().breakNaturally(); } } } }
		 */
	}
}
