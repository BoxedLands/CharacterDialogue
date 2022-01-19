package me.iatog.characterdialogue.loader.cache;

import me.iatog.characterdialogue.CharacterDialoguePlugin;
import me.iatog.characterdialogue.dialogs.choice.ContinueChoice;
import me.iatog.characterdialogue.dialogs.choice.DestroyChoice;
import me.iatog.characterdialogue.dialogs.choice.SendChoice;
import me.iatog.characterdialogue.dialogs.method.BroadcastMethod;
import me.iatog.characterdialogue.dialogs.method.ChoiceMethod;
import me.iatog.characterdialogue.dialogs.method.CommandMethod;
import me.iatog.characterdialogue.dialogs.method.DispatchCommandMethod;
import me.iatog.characterdialogue.dialogs.method.EffectMethod;
import me.iatog.characterdialogue.dialogs.method.SendServerMethod;
import me.iatog.characterdialogue.dialogs.method.SendMethod;
import me.iatog.characterdialogue.dialogs.method.SoundMethod;
import me.iatog.characterdialogue.dialogs.method.TeleportMethod;
import me.iatog.characterdialogue.dialogs.method.WaitMethod;
import me.iatog.characterdialogue.loader.Loader;

public class CacheLoader implements Loader {
	
	private CharacterDialoguePlugin main;
	
	public CacheLoader(CharacterDialoguePlugin main) {
		this.main = main;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void load() {
		main.registerMethods(
				new SendMethod(),
				new SoundMethod(main),
				new BroadcastMethod(),
				new WaitMethod(main),
				new DispatchCommandMethod(),
				new CommandMethod(),
				new TeleportMethod(),
				new EffectMethod(main),
				new SendServerMethod(main),
				new ChoiceMethod(main)
				);
		
		main.registerChoices(
				new ContinueChoice(),
				new DestroyChoice(),
				new SendChoice()
				);
	}

	@Override
	public void unload() {
		main.getCache().clearAll();
	}
}
