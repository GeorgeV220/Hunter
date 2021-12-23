package com.georgev22.killstreak.inventories;

import com.georgev22.api.configmanager.CFG;
import com.georgev22.api.inventory.CustomItemInventory;
import com.georgev22.api.inventory.IPagedInventory;
import com.georgev22.api.inventory.ItemBuilder;
import com.georgev22.api.inventory.NavigationRow;
import com.georgev22.api.inventory.handlers.PagedInventoryClickHandler;
import com.georgev22.api.inventory.handlers.PagedInventoryCustomNavigationHandler;
import com.georgev22.api.inventory.navigationitems.*;
import com.georgev22.api.inventory.utils.actions.Action;
import com.georgev22.api.inventory.utils.actions.ActionManager;
import com.georgev22.api.maps.ObjectMap;
import com.georgev22.api.utilities.MinecraftUtils;
import com.georgev22.api.utilities.Utils;
import com.georgev22.api.utilities.Utils.Cooldown;
import com.georgev22.killstreak.Main;
import com.georgev22.killstreak.inventories.actions.InventoryClickAction;
import com.georgev22.killstreak.utilities.MessagesUtil;
import com.georgev22.killstreak.utilities.OptionsUtil;
import com.georgev22.killstreak.utilities.configmanager.FileManager;
import com.georgev22.killstreak.utilities.player.UserData;
import com.google.common.collect.Lists;
import com.google.gson.reflect.TypeToken;
import de.tr7zw.changeme.nbtapi.NBTItem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class PrestigeInventory {

    private final Main mainPlugin = Main.getInstance();

    public void openInventory(Player player) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        UserData userData = UserData.getUser(player);

        List<Integer> prestigeListClosest = Lists.newArrayList();

        Arrays.stream(new File(mainPlugin.getDataFolder(), "inventories" + File.separator + "prestige").listFiles((dir, name) -> name.startsWith("prestige") && name.endsWith(".yml"))).forEach(file -> {
            String fileName = file.getName();
            String[] split = fileName.split(Pattern.quote("_"));
            Integer number = Integer.parseInt(split[1].replace(".yml", ""));
            prestigeListClosest.add(number);
        });

        int prestigeClosest = Files.exists(new File(mainPlugin.getDataFolder(), "inventories" + File.separator + "prestige" + File.separator + "prestige_" + userData.getPrestige() + ".yml").toPath()) ? userData.getPrestige() : prestigeListClosest.stream().min(Comparator.comparingInt(i -> Math.abs(i - userData.getPrestige())))
                .orElseThrow(() -> new NoSuchElementException("No value present"));

        MinecraftUtils.debug(mainPlugin, String.valueOf(prestigeClosest));

        CFG prestigeConfig = new CFG(mainPlugin, "inventories" + File.separator + "prestige" + File.separator + "prestige_" + prestigeClosest, false);

        List<NavigationItem> navigationItemList = Lists.newArrayList();

        ObjectMap<Integer, ItemStack> objectMap = ObjectMap.newHashObjectMap();

        final FileManager fileManager = FileManager.getInstance();

        if (prestigeConfig.getFileConfiguration().getConfigurationSection("custom item.navigation") != null)
            for (String s : prestigeConfig.getFileConfiguration().getConfigurationSection("custom item.navigation").getKeys(false)) {
                ItemStack itemStack = ItemBuilder.buildItemFromConfig(prestigeConfig.getFileConfiguration(), "custom item.navigation." + s)
                        .build();
                CustomNavigationItem navigationItem = new CustomNavigationItem(itemStack, Integer.parseInt(s)) {
                    @Override
                    public void handleClick(PagedInventoryCustomNavigationHandler handler) {
                        for (String command : prestigeConfig.getFileConfiguration().getStringList("custom item.navigation." + s + ".commands")) {
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%player%", handler.getPlayer().getName()));
                        }
                    }
                };
                navigationItemList.add(navigationItem);
            }

        if (prestigeConfig.getFileConfiguration().getConfigurationSection("custom item.gui") != null)
            for (String s : prestigeConfig.getFileConfiguration().getConfigurationSection("custom item.gui").getKeys(false)) {
                ItemStack itemStack = ItemBuilder.buildItemFromConfig(prestigeConfig.getFileConfiguration(), "custom item.gui." + s,
                                userData.user().placeholders(),
                                userData.user().placeholders())
                        .customNBT("actions", ItemBuilder.buildActionsFromConfig(prestigeConfig.getFileConfiguration(), "custom item.gui." + s, InventoryClickAction.class))
                        .build();
                objectMap.append(Integer.parseInt(s), itemStack);
            }

        IPagedInventory pagedInventory = mainPlugin.getInventoryAPI()
                .createPagedInventory(
                        new NavigationRow(
                                new NextNavigationItem(ItemBuilder.buildItemFromConfig(prestigeConfig.getFileConfiguration(), "navigation.next").build(), prestigeConfig.getFileConfiguration().getInt("navigation.next.slot", 6)),
                                new PreviousNavigationItem(ItemBuilder.buildItemFromConfig(prestigeConfig.getFileConfiguration(), "navigation.back").build(), prestigeConfig.getFileConfiguration().getInt("navigation.back.slot", 2)),
                                new CloseNavigationItem(ItemBuilder.buildItemFromConfig(prestigeConfig.getFileConfiguration(), "navigation.cancel").build(), prestigeConfig.getFileConfiguration().getInt("navigation.cancel.slot", 4)),
                                navigationItemList.toArray(new NavigationItem[0])));

        CustomItemInventory customItemInventory = new CustomItemInventory(Utils.placeHolder(MinecraftUtils.colorize(prestigeConfig.getFileConfiguration().getString("name")), userData.user().placeholders().append("%prestige_inventory%", String.valueOf(prestigeClosest)).append("%prestige_inventory_roman%", Utils.toRoman(prestigeClosest)), true), objectMap, 54);

        Inventory inventory = customItemInventory.getInventory();

        pagedInventory.addPage(inventory);

        pagedInventory.open(player, 0, prestigeConfig.getFileConfiguration().getBoolean("animation.enabled"));
        pagedInventory.addHandler(new PagedInventoryClickHandler() {
            @Override
            public void handle(ClickHandler clickHandler) {
                ItemStack itemStack = clickHandler.getCurrentItem();
                if (itemStack == null || itemStack.getType().equals(Material.AIR)) {
                    return;
                }

                Player clickHandlerPlayer = clickHandler.getPlayer();

                NBTItem nbtItem = new NBTItem(itemStack);

                Cooldown cooldown;

                String name = MinecraftUtils.stripColor(itemStack.getItemMeta().getDisplayName());

                if (nbtItem.hasKey("actions")) {
                    List<InventoryClickAction> actions = Utils.deserialize(nbtItem.getString("actions"), new TypeToken<List<InventoryClickAction>>() {
                    }.getType());
                    List<Action> actionList = Lists.newArrayList(actions.toArray(new Action[0]));
                    if (!actions.isEmpty())
                        ActionManager.runActions(player, mainPlugin, OptionsUtil.DEBUG_ERROR.getBooleanValue(), actionList);
                }

                if (nbtItem.hasKey("commands")) {
                    List<ItemBuilder.ItemCommand> itemCommands = Utils.deserialize(nbtItem.getString("commands"), new TypeToken<List<ItemBuilder.ItemCommand>>() {
                    }.getType());
                    if (clickHandler.isRightClick()) {
                        if (itemCommands.stream().anyMatch(lamba -> lamba.getType().equals(ItemBuilder.ItemCommandType.RIGHT))) {
                            ItemBuilder.ItemCommand itemCommand = itemCommands.stream().filter(lamba -> lamba.getType().equals(ItemBuilder.ItemCommandType.RIGHT)).collect(Collectors.toList()).get(0);
                            if (Cooldown.isInCooldown(clickHandlerPlayer.getUniqueId(), "cooldown-" + name + "-commandsR")) {
                                MessagesUtil.ITEM_ON_COOLDOWN.msg(clickHandlerPlayer,
                                        ObjectMap.newHashObjectMap()
                                                .append("%seconds%", String.valueOf(Cooldown.getTimeLeft(clickHandlerPlayer.getUniqueId(), "cooldown-" + name + "-commandsR")))
                                                .append("%item%", name),
                                        true);
                            } else {
                                cooldown = new Cooldown(clickHandlerPlayer.getUniqueId(), "cooldown-" + name + "-commandsR", itemCommand.getCooldown());
                                cooldown.start();
                                for (String commands : itemCommand.getCommands()) {
                                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), commands.replace("%player%", clickHandlerPlayer.getName()));
                                }
                            }
                        }
                    } else if (clickHandler.isLeftClick()) {
                        if (itemCommands.stream().anyMatch(lamba -> lamba.getType().equals(ItemBuilder.ItemCommandType.LEFT))) {
                            ItemBuilder.ItemCommand itemCommand = itemCommands.stream().filter(lamba -> lamba.getType().equals(ItemBuilder.ItemCommandType.LEFT)).collect(Collectors.toList()).get(0);
                            if (Cooldown.isInCooldown(clickHandlerPlayer.getUniqueId(), "cooldown-" + name + "-commandsL")) {
                                MessagesUtil.ITEM_ON_COOLDOWN.msg(clickHandlerPlayer,
                                        ObjectMap.newHashObjectMap()
                                                .append("%seconds%", String.valueOf(Cooldown.getTimeLeft(clickHandlerPlayer.getUniqueId(), "cooldown-" + name + "-commandsL")))
                                                .append("%item%", name),
                                        true);
                            } else {
                                cooldown = new Cooldown(clickHandlerPlayer.getUniqueId(), "cooldown-" + name + "-commandsL", itemCommand.getCooldown());
                                cooldown.start();
                                for (String commands : itemCommand.getCommands()) {
                                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), commands.replace("%player%", clickHandlerPlayer.getName()));
                                }
                            }
                        }
                    } else if (clickHandler.isMiddleClick()) {
                        if (itemCommands.stream().anyMatch(lamba -> lamba.getType().equals(ItemBuilder.ItemCommandType.MIDDLE))) {
                            ItemBuilder.ItemCommand itemCommand = itemCommands.stream().filter(lamba -> lamba.getType().equals(ItemBuilder.ItemCommandType.MIDDLE)).collect(Collectors.toList()).get(0);
                            if (Cooldown.isInCooldown(clickHandlerPlayer.getUniqueId(), "cooldown-" + name + "-commandsM")) {
                                MessagesUtil.ITEM_ON_COOLDOWN.msg(clickHandlerPlayer,
                                        ObjectMap.newHashObjectMap()
                                                .append("%seconds%", String.valueOf(Cooldown.getTimeLeft(clickHandlerPlayer.getUniqueId(), "cooldown-" + name + "-commandsM")))
                                                .append("%item%", name),
                                        true);
                            } else {
                                cooldown = new Cooldown(clickHandlerPlayer.getUniqueId(), "cooldown-" + name + "-commandsM", itemCommand.getCooldown());
                                cooldown.start();
                                for (String commands : itemCommand.getCommands()) {
                                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), commands.replace("%player%", clickHandlerPlayer.getName()));
                                }
                            }
                        }
                    }
                }
            }
        });
    }

}
