package me.zombie_striker.qg.handlers;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.comphenix.protocol.*;
import com.comphenix.protocol.events.*;

import me.zombie_striker.pluginconstructor.reflection.ReflectionUtil;
import me.zombie_striker.qg.QAMain;
import me.zombie_striker.qg.api.QualityArmory;

public class ProtocolLibHandler {

	private static ProtocolManager protocolManager;

	private static Object enumArgumentAnchor_EYES = null;
	private static Class<?> class_ArgumentAnchor = null;
	// org.bukkit.craftbukkit.v1_14_R1.inventory.CraftItemStack;
	private static Class nbtFactClass = null;
	private static Method nbtFactmethod = null;

	public static void initRemoveArmswing() {
		if (protocolManager == null)
			protocolManager = ProtocolLibrary.getProtocolManager();
		protocolManager.addPacketListener(
				new PacketAdapter(QAMain.getInstance(), ListenerPriority.NORMAL, PacketType.Play.Client.ARM_ANIMATION) {

					@SuppressWarnings("deprecation")
					public void onPacketReceiving(PacketEvent event) {
						final Player player = event.getPlayer();
						if (event.getPacketType() == PacketType.Play.Client.ARM_ANIMATION
								&& player.getVehicle() != null) {
							try {

								byte state = event.getPacket().getBytes().readSafely(0);
								int entityID = event.getPacket().getIntegers().readSafely(0);
								Player targ = null;
								for (Player p : Bukkit.getOnlinePlayers()) {
									if (p.getEntityId() == entityID) {
										targ = p;
										break;
									}
								}
								if (targ == null) {
									Bukkit.broadcastMessage("The ID for the entity is incorrect");
									return;
								}
								if (state == 0) {
									if (QualityArmory.isGun(targ.getItemInHand())
											|| QualityArmory.isIronSights(targ.getItemInHand())) {
										event.setCancelled(true);
									}
								}
							} catch (Error | Exception e) {
							}
						}
					}
				});

	}

	public static void initAimBow() {
		if (protocolManager == null)
			protocolManager = ProtocolLibrary.getProtocolManager();
		protocolManager.addPacketListener(
				new PacketAdapter(QAMain.getInstance(), ListenerPriority.NORMAL, PacketType.Play.Server.ENTITY_EQUIPMENT) {
					@Override
					public void onPacketSending(PacketEvent event) {
						final Player sender = event.getPlayer();
						//for(Object o : event.getPacket().getModifier().getValues())
						int id = (int) event.getPacket().getModifier().read(0);
						Object slot = event.getPacket().getModifier().read(1);
						final Object ironsights = event.getPacket().getModifier().read(2);
						if ((id) == sender.getEntityId()) {
							return;
						}
						Player who = null;
						for (Player player : sender.getWorld().getPlayers()) {
							if (player.getEntityId() == (int) id) {
								who = player;
								break;
							}
						}
						if (who == null)
							return;
						if (!slot.toString().equals("MAINHAND")) {
							if (QualityArmory.isIronSights(who.getInventory().getItemInMainHand())) {
								event.setCancelled(true);
							}
							return;
						}
						if (who.getItemInHand() != null && who.getItemInHand().getType().name().equals("CROSSBOW") &&
								QualityArmory.isIronSights(who.getItemInHand()) &&
								ironsights.toString().contains("crossbow")) {

							Object is = null;
							try {
								if (!QualityArmory.getGun(who.getInventory().getItemInOffHand()).hasBetterAimingAnimations())
									return;
								is = getCraftItemStack(who.getInventory().getItemInOffHand());
								Object nbtTag = is.getClass().getMethod("getOrCreateTag").invoke(is, new Object[0]);
								//new NBTTagCompound().
								Class[] args = new Class[2];
								args[0] = String.class;
								args[1] = boolean.class;
								nbtTag.getClass().getMethod("setBoolean", args).invoke(nbtTag, "Charged", true);
								is.getClass().getMethod("setTag", nbtTag.getClass()).invoke(is, nbtTag);
							} catch (IllegalAccessException e) {
								e.printStackTrace();
							} catch (InvocationTargetException e) {
								e.printStackTrace();
							} catch (NoSuchMethodException e) {
								e.printStackTrace();
							}

							/*try{
								ironsights = ironsights.getClass().getMethod("cloneItemStack", new Class[0]).invoke(is,new Class[0]);
							}catch (Error|Exception e43){
								e43.printStackTrace();
							}*/

							event.getPacket().getModifier().write(2, is);

							new BukkitRunnable() {
								public void run() {
									try {
										PacketContainer pc2 = protocolManager
												.createPacket(PacketType.Play.Server.ENTITY_EQUIPMENT);

										//EnumItemSlot e = EnumItemSlot.OFFHAND;

										Object neededSlot = null;
										Object[] enums = slot.getClass().getEnumConstants();
										for (Object k : enums) {
											String name = (String) k.getClass().getMethod("name").invoke(k, new Class[0]);
											if (name.equals("OFFHAND")) {
												neededSlot = k;
												break;
											}
										}
										pc2.getModifier().write(0, id)
												.write(1, neededSlot)
												.write(2, ironsights);

										protocolManager.sendServerPacket(event.getPlayer(), pc2);
									} catch (Exception e) {
										e.printStackTrace();
									}


								}
							}.runTaskLater(QAMain.getInstance(), 1);
						}
					}
				});

	}

	private static Object getCraftItemStack(ItemStack is) throws NoSuchMethodException {
		if (nbtFactClass == null) {
			nbtFactClass = ReflectionsUtil.getCraftBukkitClass("inventory.CraftItemStack");
			Class[] c = new Class[1];
			c[0] = ItemStack.class;
			nbtFactmethod = nbtFactClass.getMethod("asNMSCopy", c);
		}
		try {
			return nbtFactmethod.invoke(nbtFactClass, is);
		} catch (InvocationTargetException | IllegalAccessException e) {
			return null;
		}
	}


	public static void sendYawChange(Player player, Vector newDirection) {
		if (protocolManager == null)
			protocolManager = ProtocolLibrary.getProtocolManager();
		final PacketContainer yawpack = protocolManager.createPacket(PacketType.Play.Server.LOOK_AT, false);
		if (enumArgumentAnchor_EYES == null) {
			class_ArgumentAnchor = ReflectionUtil.getNMSClass("ArgumentAnchor$Anchor");
			enumArgumentAnchor_EYES = ReflectionUtil.getEnumConstant(class_ArgumentAnchor, "EYES");
		}
		yawpack.getModifier().write(4, enumArgumentAnchor_EYES);
		yawpack.getDoubles().write(0, player.getEyeLocation().getX() + newDirection.getX());
		yawpack.getDoubles().write(1, player.getEyeLocation().getY() + newDirection.getY());
		yawpack.getDoubles().write(2, player.getEyeLocation().getZ() + newDirection.getZ());
		yawpack.getBooleans().write(0, false);
		try {
			protocolManager.sendServerPacket(player, yawpack);
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}
}
