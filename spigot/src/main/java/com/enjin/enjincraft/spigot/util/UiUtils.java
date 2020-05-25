package com.enjin.enjincraft.spigot.util;

import com.enjin.minecraft_commons.spigot.ui.Component;
import com.enjin.minecraft_commons.spigot.ui.Dimension;
import com.enjin.minecraft_commons.spigot.ui.Position;
import com.enjin.minecraft_commons.spigot.ui.menu.component.SimpleMenuComponent;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public final class UiUtils {

    private UiUtils() {
        throw new IllegalStateException();
    }

    public static Component createSeparator(Dimension dimension) {
        SimpleMenuComponent component = new SimpleMenuComponent(dimension);
        for (int x = 0; x < component.getDimension().getWidth(); x++) {
            for (int y = 0; y < component.getDimension().getHeight(); y++) {
                component.setItem(Position.of(x, y), createSeparatorItemStack());
            }
        }

        return component;
    }

    public static ItemStack createSeparatorItemStack() {
        ItemStack stack = new ItemStack(Material.IRON_BARS);
        ItemMeta meta = stack.getItemMeta();
        meta.setDisplayName(ChatColor.DARK_PURPLE + "|");
        stack.setItemMeta(meta);
        return stack;
    }

}
