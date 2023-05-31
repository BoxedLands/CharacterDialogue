package me.iatog.characterdialogue.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;

public class TextUtils {

	public static String colorizeOld(String message) {
		String version = Bukkit.getBukkitVersion().split("-")[0].split("\\.")[1];

		if(Integer.parseInt(version) >= 16) {
			Pattern pattern = Pattern.compile("&#[a-fA-F0-9]{6}");
			Matcher matcher = pattern.matcher(message);

			while (matcher.find()) {
				String color = message.substring(matcher.start(), matcher.end());
				message = message.replace(color, ChatColor.of(color.substring(1)) + "");
				matcher = pattern.matcher(message);
			}
		}

		return ChatColor.translateAlternateColorCodes('&', message);
	}

	public static Component colorize(String message) {
		String version = Bukkit.getBukkitVersion().split("-")[0].split("\\.")[1];
		
		if(Integer.parseInt(version) >= 16) {
			Pattern pattern = Pattern.compile("&#[a-fA-F0-9]{6}");
            Matcher matcher = pattern.matcher(message);

            while (matcher.find()) {
                String color = message.substring(matcher.start(), matcher.end());
                message = message.replace(color, String.valueOf(ChatColor.of(color.substring(1))));
                matcher = pattern.matcher(message);
            }
		}

		MiniMessage serializer = MiniMessage.builder()
				.tags(TagResolver.builder()
						.resolver(StandardTags.defaults())
						.build()
				)
				.build();

		return serializer.deserialize(org.bukkit.ChatColor.translateAlternateColorCodes('&', message));
	}
}
