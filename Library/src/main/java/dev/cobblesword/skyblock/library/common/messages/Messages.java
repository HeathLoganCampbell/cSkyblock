package dev.cobblesword.skyblock.library.common.messages;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

/**
 * Pretty message utilities for player feedback
 */
public class Messages {

    // ═══════════════════════════════════════════════════════════════
    // Brand Colors
    // ═══════════════════════════════════════════════════════════════
    
    public static final String PRIMARY = ChatColor.AQUA.toString();
    public static final String PRIMARY_BOLD = ChatColor.AQUA.toString() + ChatColor.BOLD;
    public static final String SECONDARY = ChatColor.WHITE.toString();
    public static final String ACCENT = ChatColor.GOLD.toString();
    public static final String SUCCESS = ChatColor.GREEN.toString();
    public static final String ERROR = ChatColor.RED.toString();
    public static final String MUTED = ChatColor.GRAY.toString();
    public static final String DARK = ChatColor.DARK_GRAY.toString();
    
    // ═══════════════════════════════════════════════════════════════
    // Symbols
    // ═══════════════════════════════════════════════════════════════
    
    public static final String STAR = "★";
    public static final String ARROW = "»";
    public static final String DOT = "•";
    public static final String CHECK = "✔";
    public static final String CROSS = "✖";
    public static final String ISLAND = "⛰";
    public static final String SPARKLE = "✦";
    public static final String DIAMOND = "◆";
    
    // ═══════════════════════════════════════════════════════════════
    // Prefixes
    // ═══════════════════════════════════════════════════════════════
    
    public static final String PREFIX = DARK + "[" + PRIMARY + STAR + " Skyopoly" + DARK + "] " + MUTED;
    public static final String PREFIX_ISLAND = DARK + "[" + PRIMARY + ISLAND + DARK + "] " + MUTED;
    
    // ═══════════════════════════════════════════════════════════════
    // Chat Messages
    // ═══════════════════════════════════════════════════════════════
    
    public static void send(Player player, String message) {
        player.sendMessage(PREFIX + message);
    }
    
    public static void success(Player player, String message) {
        player.sendMessage(PREFIX + SUCCESS + CHECK + " " + MUTED + message);
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.2f);
    }
    
    public static void error(Player player, String message) {
        player.sendMessage(PREFIX + ERROR + CROSS + " " + MUTED + message);
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
    }
    
    public static void info(Player player, String message) {
        player.sendMessage(PREFIX + PRIMARY + ARROW + " " + MUTED + message);
    }
    
    public static void island(Player player, String message) {
        player.sendMessage(PREFIX_ISLAND + message);
    }
    
    // ═══════════════════════════════════════════════════════════════
    // Titles
    // ═══════════════════════════════════════════════════════════════
    
    public static void title(Player player, String title, String subtitle) {
        player.sendTitle(
            PRIMARY_BOLD + title,
            MUTED + subtitle,
            10, 40, 10
        );
    }
    
    public static void title(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        player.sendTitle(
            PRIMARY_BOLD + title,
            MUTED + subtitle,
            fadeIn, stay, fadeOut
        );
    }
    
    public static void successTitle(Player player, String title, String subtitle) {
        player.sendTitle(
            SUCCESS + ChatColor.BOLD + title,
            MUTED + subtitle,
            5, 40, 10
        );
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.7f, 1.2f);
    }
    
    public static void errorTitle(Player player, String title, String subtitle) {
        player.sendTitle(
            ERROR + ChatColor.BOLD + title,
            MUTED + subtitle,
            5, 40, 10
        );
        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 0.5f, 1.0f);
    }
    
    // ═══════════════════════════════════════════════════════════════
    // Action Bar
    // ═══════════════════════════════════════════════════════════════
    
    public static void actionBar(Player player, String message) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
    }
    
    public static void actionBarProgress(Player player, String label, double progress) {
        int bars = 20;
        int filled = (int) (progress * bars);
        
        StringBuilder bar = new StringBuilder();
        bar.append(MUTED).append(label).append(" ");
        bar.append(DARK).append("[");
        bar.append(PRIMARY);
        for (int i = 0; i < filled; i++) bar.append("▌");
        bar.append(DARK);
        for (int i = filled; i < bars; i++) bar.append("▌");
        bar.append(DARK).append("]");
        bar.append(SECONDARY).append(" ").append((int)(progress * 100)).append("%");
        
        actionBar(player, bar.toString());
    }
    
    // ═══════════════════════════════════════════════════════════════
    // Sounds
    // ═══════════════════════════════════════════════════════════════
    
    public static void playSuccess(Player player) {
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.2f);
    }
    
    public static void playError(Player player) {
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
    }
    
    public static void playTeleport(Player player) {
        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.8f, 1.0f);
    }
    
    public static void playLevelUp(Player player) {
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.7f, 1.2f);
    }
    
    public static void playClick(Player player) {
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
    }
    
    public static void playChime(Player player) {
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1.0f, 1.5f);
    }
    
    // ═══════════════════════════════════════════════════════════════
    // Special Effects
    // ═══════════════════════════════════════════════════════════════
    
    public static void welcome(Player player) {
        player.sendTitle(
            PRIMARY_BOLD + STAR + " SKYOPOLY " + STAR,
            ACCENT + "Welcome back, " + SECONDARY + player.getName() + ACCENT + "!",
            20, 60, 20
        );
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1.0f, 1.0f);
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1.0f, 1.25f);
    }
    
    public static void islandCreated(Player player) {
        player.sendTitle(
            SUCCESS + ChatColor.BOLD + ISLAND + " Island Created!",
            MUTED + "Your adventure begins now...",
            10, 60, 20
        );
        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.8f, 1.0f);
    }
    
    public static void teleporting(Player player, String destination) {
        player.sendTitle(
            PRIMARY_BOLD + SPARKLE + " Teleporting " + SPARKLE,
            MUTED + "to " + SECONDARY + destination,
            5, 30, 10
        );
        player.playSound(player.getLocation(), Sound.BLOCK_PORTAL_TRIGGER, 0.3f, 1.5f);
    }
    
    public static void arrived(Player player, String location) {
        player.sendTitle(
            "",
            PRIMARY + ARROW + " " + SECONDARY + location,
            5, 40, 10
        );
        playTeleport(player);
    }
    
    // ═══════════════════════════════════════════════════════════════
    // Formatting Helpers
    // ═══════════════════════════════════════════════════════════════
    
    public static String highlight(String text) {
        return SECONDARY + text + MUTED;
    }
    
    public static String gold(int amount) {
        return ACCENT + amount + "g" + MUTED;
    }
    
    public static String command(String cmd) {
        return PRIMARY + "/" + cmd + MUTED;
    }
}

