package com.georgev22.hunter.inventories.actions;

import com.georgev22.api.inventory.ItemBuilder;
import com.georgev22.api.inventory.utils.actions.Action;
import com.georgev22.api.inventory.utils.actions.ActionManager;
import com.georgev22.api.maps.ObjectMap;
import com.georgev22.api.utilities.exceptions.ActionRunException;
import com.georgev22.hunter.hooks.Vault;
import com.georgev22.hunter.utilities.MessagesUtil;
import com.georgev22.hunter.utilities.configmanager.FileManager;
import com.georgev22.hunter.utilities.player.UserData;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;

public class InventoryClickAction extends Action {

    private final ActionManager action;

    public InventoryClickAction(ActionManager action) {
        this.action = action;
    }

    public void runAction(OfflinePlayer offlinePlayer) throws ActionRunException {
        if (action.getActionName().equalsIgnoreCase("TAKE")) {
            if (action.getData().length < 1) {
                throw new ActionRunException("Insufficient arguments!");
            }

            double takeAmount = Double.parseDouble(String.valueOf(action.getData()[0]));
            UserData userData = UserData.getUser(offlinePlayer);

            if (Vault.isHooked()) {
                if (Vault.getEconomy().has(offlinePlayer, takeAmount)) {
                    Vault.getEconomy().withdrawPlayer(offlinePlayer, takeAmount);
                    MessagesUtil.TRANSACTION_WITHDRAW_SUCCESS.msg(offlinePlayer.getPlayer(), userData.user().placeholders().append("%transaction%", String.valueOf(takeAmount)), true);
                } else {
                    MessagesUtil.TRANSACTION_ERROR.msg(offlinePlayer.getPlayer(), userData.user().placeholders().append("%transaction%", String.valueOf(takeAmount)), true);
                    throw new ActionRunException("Player does not have the required funds!");
                }
            } else {
                throw new ActionRunException("Vault is not hooked!");
            }
        }

        if (action.getActionName().equalsIgnoreCase("PRESTIGE")) {
            if (action.getData().length < 2) {
                throw new ActionRunException("Insufficient arguments!");
            }

            int prestige = Integer.parseInt(String.valueOf(action.getData()[0]));
            UserData userData = UserData.getUser(offlinePlayer);
            boolean broadcast = Boolean.parseBoolean(String.valueOf(action.getData()[1]));
            userData.setPrestige(prestige);

            if (broadcast) {
                MessagesUtil.PRESTIGE.msgAll(userData.user().placeholders(), true);
            } else {
                MessagesUtil.PRESTIGE.msg(offlinePlayer.getPlayer(), userData.user().placeholders(), true);
            }
        }

        if (action.getActionName().equalsIgnoreCase("MULTIPLIER")) {
            if (action.getData().length < 1) {
                throw new ActionRunException("Insufficient arguments!");
            }

            double multiplier = Double.parseDouble(String.valueOf(action.getData()[0]));
            UserData userData = UserData.getUser(offlinePlayer);
            userData.setMultiplier(multiplier);
        }

        if (action.getActionName().equalsIgnoreCase("SELL")) {
            if (action.getData().length < 2) {
                throw new ActionRunException("Insufficient arguments!");
            }

            String item = String.valueOf(action.getData()[0]);

            double itemSellMoney = Double.parseDouble(String.valueOf(action.getData()[1]));

            UserData userData = UserData.getUser(offlinePlayer);

            FileManager fileManager = FileManager.getInstance();
            ItemBuilder itemBuilder = ItemBuilder.buildSimpleItemFromConfig(fileManager.getItems().getFileConfiguration(), "sell." + item);
            ItemStack itemStack = itemBuilder.build();
            if (Vault.isHooked()) {
                for (ItemStack i : offlinePlayer.getPlayer().getInventory().getContents()) {
                    if (i == null || i.equals(new ItemStack(Material.AIR))) {
                        continue;
                    }
                    if (i.getType().equals(itemStack.getType())) {
                        if (i.getAmount() >= itemStack.getAmount()) {
                            if (i.getAmount() == 1) {
                                offlinePlayer.getPlayer().getInventory().removeItem(i);
                            } else {
                                i.setAmount(i.getAmount() - 1);
                            }
                            Vault.getEconomy().depositPlayer(offlinePlayer, itemSellMoney);
                            MessagesUtil.TRANSACTION_DEPOSIT_SUCCESS.msg(offlinePlayer.getPlayer(), ObjectMap.newHashObjectMap().append("%transaction%", String.valueOf(itemSellMoney)).append(userData.user().placeholders()), true);
                        } else {
                            throw new ActionRunException("Player does not have enough items to sell!");
                        }
                        break;
                    } else {
                        throw new ActionRunException("Item does not exist in the player inventory!");
                    }
                }
            } else {
                throw new ActionRunException("Vault is not hooked!");
            }
        }

        if (action.getActionName().equalsIgnoreCase("BUY")) {
            if (action.getData().length < 2) {
                throw new ActionRunException("Insufficient arguments!");
            }

            String item = String.valueOf(action.getData()[0]);

            double itemBuyMoney = Double.parseDouble(String.valueOf(action.getData()[1]));

            UserData userData = UserData.getUser(offlinePlayer);

            FileManager fileManager = FileManager.getInstance();

            if (Vault.isHooked()) {
                if (Vault.getEconomy().has(offlinePlayer, itemBuyMoney)) {
                    if (offlinePlayer.getPlayer().getInventory().firstEmpty() != -1) {
                        ItemBuilder itemBuilder = ItemBuilder.buildSimpleItemFromConfig(fileManager.getItems().getFileConfiguration(), "buy." + item, userData.user().placeholders(), userData.user().placeholders());
                        ItemStack itemStack = itemBuilder.build();
                        offlinePlayer.getPlayer().getInventory().addItem(itemStack);
                        Vault.getEconomy().withdrawPlayer(offlinePlayer, itemBuyMoney);
                        MessagesUtil.TRANSACTION_WITHDRAW_SUCCESS.msg(offlinePlayer.getPlayer(), ObjectMap.newHashObjectMap().append("%transaction%", String.valueOf(itemBuyMoney)).append(userData.user().placeholders()), true);
                    } else {
                        MessagesUtil.TRANSACTION_FULL_INVENTORY.msg(offlinePlayer.getPlayer(), ObjectMap.newHashObjectMap().append("%transaction%", String.valueOf(itemBuyMoney)), true);
                        throw new ActionRunException("Player inventory is full!");
                    }
                } else {
                    MessagesUtil.TRANSACTION_ERROR.msg(offlinePlayer.getPlayer(), ObjectMap.newHashObjectMap().append("%transaction%", String.valueOf(itemBuyMoney)), true);
                    throw new ActionRunException("Player does not have the required funds!");
                }
            } else {
                throw new ActionRunException("Vault is not hooked!");
            }
        }
    }
}
