package com.gmail.berndivader.mythicdenizenaddon.drops;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.entity.Player;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.DurationTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ScriptTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.queues.ScriptQueue;
import com.denizenscript.denizencore.scripts.queues.core.InstantQueue;
import com.denizenscript.denizencore.scripts.queues.core.TimedQueue;
import com.gmail.berndivader.mythicdenizenaddon.Utils;
import com.gmail.berndivader.mythicdenizenaddon.context.MythicContextSource;

import io.lumine.xikage.mythicmobs.adapters.AbstractPlayer;
import io.lumine.xikage.mythicmobs.drops.Drop;
import io.lumine.xikage.mythicmobs.drops.DropMetadata;
import io.lumine.xikage.mythicmobs.drops.IIntangibleDrop;
import io.lumine.xikage.mythicmobs.io.MythicLineConfig;

public
class
DenizenScriptDrop
extends
Drop
implements
IIntangibleDrop
{
	final double amount;
	final String script_name;
	HashMap<String,String>attributes;
	
	public DenizenScriptDrop(String line,MythicLineConfig config,double amount) {
		super(line,config,amount);
		this.amount=amount;
		
		script_name=config.getString("script","");
		attributes=Utils.parse_attributes(line);
	}

	@Override
	public void giveDrop(AbstractPlayer abstract_player,DropMetadata drop_data) {
		List<ScriptEntry>entries=null;
		ScriptTag script=new ScriptTag(script_name);
		if(script!=null&&script.isValid()) {
			try {
				ScriptEntry entry=new ScriptEntry(script.getName(),new String[0],script.getContainer());
				entry.setScript(script_name);
				entries=script.getContainer().getBaseEntries(entry.entryData.clone());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		if(entries!=null) {
			ScriptQueue queue;
			if(script.getContainer().contains("SPEED")) {
				long ticks=DurationTag.valueOf(script.getContainer().getString("SPEED","0"), null).getTicks();
				queue=ticks>0?((TimedQueue)(new TimedQueue(script.getContainer().getName()).addEntries(entries))).setSpeed(ticks):new InstantQueue(script.getContainer().getName()).addEntries(entries);
			} else {
				queue=new TimedQueue(script.getContainer().getName()).addEntries(entries);
			}
			HashMap<String,ObjectTag>context=new HashMap<String,ObjectTag>();
			context.put("player",new PlayerTag((Player)abstract_player.getBukkitEntity()));
			context.put("amount",new ElementTag(drop_data.getAmount()));
			context.put("cause",drop_data.getCause().isPresent()?new EntityTag(drop_data.getCause().get().getBukkitEntity()):null);
			context.put("dropper",drop_data.getDropper().isPresent()?new EntityTag(drop_data.getDropper().get().getEntity().getBukkitEntity()):null);
			queue.setContextSource(new MythicContextSource(context));
			for(Map.Entry<String,String>item:attributes.entrySet()) {
				queue.addDefinition(item.getKey(),item.getValue());
			}
			queue.start();
		}
	}
}
