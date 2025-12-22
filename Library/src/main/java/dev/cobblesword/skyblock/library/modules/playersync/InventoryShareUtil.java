package dev.cobblesword.skyblock.library.modules.playersync;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectOutputStream;
import java.io.ByteArrayOutputStream;
import java.util.Base64;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import java.io.ByteArrayInputStream;

public class InventoryShareUtil
{
    public static String inventoryToBase64(Player player) {
        try {
            // Get the player's inventory
            ItemStack[] inventoryContents = player.getInventory().getContents();

            // Create a ByteArrayOutputStream to hold the data
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);

            // Write the size of the inventory
            dataOutput.writeInt(inventoryContents.length);

            // Save every element in the list
            for (int i = 0; i < inventoryContents.length; i++) {
                dataOutput.writeObject(inventoryContents[i]);
            }

            // Close the output streams
            dataOutput.close();

            // Convert the byte array to a Base64 string
            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (Exception e) {
            throw new IllegalStateException("Unable to save item stacks.", e);
        }
    }

    public static void inventoryFromBase64(Player player, String data) {
        try {
            // Decode the Base64 string to a byte array
            byte[] byteArray = Base64.getDecoder().decode(data);

            // Create a ByteArrayInputStream to read the data
            ByteArrayInputStream inputStream = new ByteArrayInputStream(byteArray);
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);

            // Read the size of the inventory
            ItemStack[] inventoryContents = new ItemStack[dataInput.readInt()];

            // Read and reconstruct the ItemStacks
            for (int i = 0; i < inventoryContents.length; i++) {
                inventoryContents[i] = (ItemStack) dataInput.readObject();
            }

            // Close the input streams
            dataInput.close();

            // Set the player's inventory
            player.getInventory().setContents(inventoryContents);
        } catch (Exception e) {
            e.printStackTrace();  // Handle exceptions appropriately
        }
    }

}
