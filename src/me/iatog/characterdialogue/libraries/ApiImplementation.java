package me.iatog.characterdialogue.libraries;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;

import me.iatog.characterdialogue.CharacterDialoguePlugin;
import me.iatog.characterdialogue.api.CharacterDialogueAPI;
import me.iatog.characterdialogue.api.dialog.DialogHologram;
import me.iatog.characterdialogue.api.dialog.Dialogue;
import me.iatog.characterdialogue.api.events.ExecuteMethodEvent;
import me.iatog.characterdialogue.dialogs.DialogMethod;
import me.iatog.characterdialogue.enums.ClickType;
import me.iatog.characterdialogue.placeholders.Placeholders;
import me.iatog.characterdialogue.session.DialogSession;
import me.iatog.characterdialogue.session.EmptyDialogSession;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;

public class ApiImplementation implements CharacterDialogueAPI {

	private CharacterDialoguePlugin main;

	public ApiImplementation(CharacterDialoguePlugin main) {
		this.main = main;
	}

	@Override
	public void reloadHolograms() {
		YamlFile config = main.getFileFactory().getConfig();
		if (!Bukkit.getPluginManager().isPluginEnabled("HolographicDisplays")) {
			return;
		}

		for (Hologram hologram : HologramsAPI.getHolograms(main)) {
			hologram.delete();
		}

		config.getConfigurationSection("npc").getKeys(false).forEach((id) -> {
			this.loadHologram(Integer.parseInt(id));
		});
	}

	@Override
	public void loadHologram(int npcId) {
		if (!Bukkit.getPluginManager().isPluginEnabled("HolographicDisplays")) {
			return;
		}
		NPC citizensNpc = CitizensAPI.getNPCRegistry().getById(npcId);

		if (citizensNpc == null) {
			return;
		}

		Dialogue dialogue = getNPCDialogue(npcId);
		
		if(dialogue == null) {
			return;
		}
		
		DialogHologram hologram = dialogue.getHologram();

		if (hologram != null && hologram.isEnabled()) {
			Location location = citizensNpc.getStoredLocation();
			location.add(0, 2 + hologram.getY(), 0);
			Hologram holo = HologramsAPI.createHologram(main, location);
			String npcName = dialogue.getDisplayName();
			List<String> lines = hologram.getLines();

			for (String line : lines) {
				holo.appendTextLine(ChatColor.translateAlternateColorCodes('&', line.replace("%npc_name%", npcName)));
			}

			citizensNpc.setAlwaysUseNameHologram(false);
		}
	}

	@Override
	public Dialogue getDialogue(String name) {
		return main.getCache().getDialogues().get(name);
	}

	@Override
	public boolean readDialogBy(Player player, String dialog) {
		String path = "players." + player.getUniqueId();
		YamlFile playerCache = main.getFileFactory().getPlayerCache();
		List<String> readedDialogues = playerCache.getStringList(path + ".readed-dialogues");

		if (!playerCache.contains(path)) {
			readedDialogues = new ArrayList<>();
		}

		if (readedDialogues.contains(dialog)) {
			return false;
		}

		readedDialogues.add(dialog);
		playerCache.set(path + ".readed-dialogues", readedDialogues);
		playerCache.save();
		return true;
	}

	@Override
	public boolean wasReadedBy(Player player, String dialog) {
		YamlFile playerCache = main.getFileFactory().getPlayerCache();
		String path = "players." + player.getUniqueId();
		List<String> readedDialogues = playerCache.getStringList(path + ".readed-dialogues");

		return playerCache.contains(path) && readedDialogues.contains(dialog);
	}

	@Override
	public Dialogue getNPCDialogue(int id) {
		return getDialogue(getNPCDialogueName(id));
	}

	@Override
	public String getNPCDialogueName(int id) {
		YamlFile config = main.getFileFactory().getConfig();
		return config.getString("npc." + id);
	}

	@Override
	public boolean readDialogBy(Player player, Dialogue dialog) {
		return readDialogBy(player, dialog.getName());
	}

	@Override
	public boolean wasReadedBy(Player player, Dialogue dialog) {
		return wasReadedBy(player, dialog.getName());
	}

	@Override
	public void runDialogue(Player player, Dialogue dialogue) {
		if (main.getCache().getDialogSessions().containsKey(player.getUniqueId())) {
			return;
		}
		YamlFile playerCache = main.getFileFactory().getPlayerCache();
		String path = "players." + player.getUniqueId();
		DialogSession session = new DialogSession(main, player, dialogue);
		
		if(!dialogue.isMovementAllowed()) {
			playerCache.set(path + ".last-speed", player.getWalkSpeed());
			playerCache.set(path + ".remove-effect", true);
			playerCache.save();
			
			player.setWalkSpeed(0);
			player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, 128));
		}
		
		main.getCache().getDialogSessions().put(player.getUniqueId(), session);
		session.start(0);
	}

	@Override
	public void runDialogue(Player player, String dialogueName) {
		runDialogue(player, getDialogue(dialogueName));
	}

	@Override
	public void runDialogueExpression(Player player, String dialog) {
		runDialogueExpression(player, dialog, "Dummy");
	}

	@Override
	public void runDialogueExpression(Player player, String dialog, String npcName) {
		String[] splitted = dialog.split(":");
		String methodName = splitted[0].toUpperCase().trim();
		String arg = dialog.substring(methodName.length() + 1).trim();

		if (!main.getCache().getMethods().containsKey(methodName)) {
			main.getLogger().warning("The method \"" + methodName + "\" doesn't exists");
			return;
		}

		arg = Placeholders.translate(player, arg);
		arg = arg.replace("%npc_name%", npcName);

		DialogMethod<? extends JavaPlugin> method = main.getCache().getMethods().get(methodName);
		ExecuteMethodEvent event = new ExecuteMethodEvent(player, method, ClickType.LEFT, -39, npcName);
		Bukkit.getPluginManager().callEvent(event);

		if (!event.isCancelled()) {
			method.execute(player, arg, new EmptyDialogSession(main, player, Arrays.asList(dialog), npcName));
		}
	}

	@Override
	public void runDialogueExpressions(Player player, List<String> lines, ClickType clickType, int npcId, String displayName) {
		if (main.getCache().getDialogSessions().containsKey(player.getUniqueId())) {
			return;
		}
		
		DialogSession session = new DialogSession(main, player, lines, clickType, npcId,
				displayName == null ? "Dummy" : displayName);
		
		main.getCache().getDialogSessions().put(player.getUniqueId(), session);
		session.start(0);
	}

	@Override
	public void runDialogueExpressions(Player player, List<String> lines, String displayName) {
		runDialogueExpressions(player, lines, ClickType.ALL, -999, displayName);
	}
}
