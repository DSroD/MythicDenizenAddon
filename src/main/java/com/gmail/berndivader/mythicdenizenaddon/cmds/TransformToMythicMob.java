package com.gmail.berndivader.mythicdenizenaddon.cmds;

import org.bukkit.entity.Entity;
import org.bukkit.metadata.FixedMetadataValue;

import com.gmail.berndivader.mythicdenizenaddon.MythicMobsAddon;
import com.gmail.berndivader.mythicdenizenaddon.Statics;
import com.gmail.berndivader.mythicdenizenaddon.obj.dActiveMob;

import io.lumine.xikage.mythicmobs.adapters.bukkit.BukkitAdapter;
import io.lumine.xikage.mythicmobs.mobs.ActiveMob;
import io.lumine.xikage.mythicmobs.mobs.MythicMob;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizencore.exceptions.CommandExecutionException;
import net.aufdemrand.denizencore.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.scripts.ScriptEntry;
import net.aufdemrand.denizencore.scripts.commands.AbstractCommand;

public class TransformToMythicMob extends AbstractCommand {

	@Override
	public void parseArgs(ScriptEntry entry) throws InvalidArgumentsException {
		for (aH.Argument arg:aH.interpret(entry.getArguments())) {
			if (!entry.hasObject(Statics.str_entity)&&arg.matchesPrefix(Statics.str_entity)&&arg.matchesArgumentType(dEntity.class)) {
				entry.addObject(Statics.str_entity,arg.asType(dEntity.class));
			} else if (!entry.hasObject(Statics.str_mobtype) && arg.matchesPrefix(Statics.str_mobtype)) {
				entry.addObject(Statics.str_mobtype, arg.asElement());
			} else if (!entry.hasObject(Statics.str_level) && arg.matchesPrefix(Statics.str_level)) {
				entry.addObject(Statics.str_level, arg.asElement());
			}
		}
		if (!entry.hasObject(Statics.str_level)) entry.addObject(Statics.str_level, 1);
	}
	
	@Override
	public void execute(ScriptEntry entry) throws CommandExecutionException {
		Entity entity = ((dEntity)entry.getdObject(Statics.str_entity)).getBukkitEntity();
		String mmName = entry.getElement(Statics.str_mobtype).asString();
		MythicMob mm = MythicMobsAddon.mythicmobs.getMobManager().getMythicMob(mmName);
		int level = entry.getElement(Statics.str_level).asInt();
		ActiveMob am=null;
		if (mm!=null) am=TransformToMythicMob.transformEntityToMythicMob(entity,mm,level);
		if(am!=null) {
			entry.addObject(Statics.str_activemob,new dActiveMob(am));
		} else {
			throw new CommandExecutionException("Failed to transfrom Entity to MythicMobs!");
		}
	}
	
	private static ActiveMob transformEntityToMythicMob(Entity l, MythicMob mm, int level) {
		ActiveMob am = new ActiveMob(l.getUniqueId(), BukkitAdapter.adapt((Entity)l), mm, level);
	    TransformToMythicMob.addActiveMobToFaction(mm,am);
	    TransformToMythicMob.registerActiveMob(am);
	    return am;
	}
	
	public static void addActiveMobToFaction(MythicMob mm, ActiveMob am) {
        if (mm.hasFaction()) {
            am.setFaction(mm.getFaction());
            am.getLivingEntity().setMetadata("Faction", new FixedMetadataValue(MythicMobsAddon.mythicmobs,mm.getFaction()));
        }
	}	
	
    public static void registerActiveMob(ActiveMob am) {
    	MythicMobsAddon.mythicmobs.getMobManager().registerActiveMob(am);
    }

}
