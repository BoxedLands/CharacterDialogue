package me.iatog.characterdialogue.listeners;

import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import me.iatog.characterdialogue.CharacterDialoguePlugin;
import me.iatog.characterdialogue.session.DialogSession;

public class PlayerQuitListener implements Listener {
	
	private CharacterDialoguePlugin main;
	
	public PlayerQuitListener(CharacterDialoguePlugin main) {
		this.main = main;
	}
	
	@EventHandler
	public void onQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		Map<UUID, DialogSession> cache = main.getCache().getDialogSessions();
		
		if(!cache.containsKey(player.getUniqueId())) {
			return;
		}
		
		DialogSession session = cache.remove(player.getUniqueId());
		session.cancel();
	}
	
}
