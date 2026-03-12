package com.howlstudio.enhancer;
import com.hypixel.hytale.component.Ref; import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.nio.file.*; import java.util.*;
public class EnhanceManager {
    private final Path dataDir;
    private final Map<UUID, Map<String, Integer>> enhancements = new HashMap<>();
    private final Random rng = new Random();

    // Level -> [success%, cost]
    private static final int[][] LEVELS = {
        {100, 100}, // +1
        {90, 200},  // +2
        {80, 400},  // +3
        {70, 700},  // +4
        {60, 1100}, // +5
        {50, 1600}, // +6
        {40, 2200}, // +7
        {30, 3000}, // +8
        {20, 4000}, // +9
        {10, 5000}, // +10
    };

    public EnhanceManager(Path d){this.dataDir=d;try{Files.createDirectories(d);}catch(Exception e){}load();}

    public int getLevel(UUID uid, String item){
        return enhancements.getOrDefault(uid,Map.of()).getOrDefault(item.toLowerCase(),0);
    }

    public void save(){try{StringBuilder sb=new StringBuilder();for(Map.Entry<UUID,Map<String,Integer>> e:enhancements.entrySet())for(Map.Entry<String,Integer> ie:e.getValue().entrySet())sb.append(e.getKey()+"|"+ie.getKey()+"|"+ie.getValue()+"\n");Files.writeString(dataDir.resolve("enhance.txt"),sb.toString());}catch(Exception ex){}}
    private void load(){try{Path f=dataDir.resolve("enhance.txt");if(!Files.exists(f))return;for(String l:Files.readAllLines(f)){String[]p=l.split("\\|");if(p.length<3)continue;UUID uid=UUID.fromString(p[0]);enhancements.computeIfAbsent(uid,k->new HashMap<>()).put(p[1],Integer.parseInt(p[2]));}}catch(Exception e){}}

    public AbstractPlayerCommand getEnhanceCommand(){
        return new AbstractPlayerCommand("enhance","Enhance an item. /enhance <item_name>"){
            @Override protected void execute(CommandContext ctx,Store<EntityStore> store,Ref<EntityStore> ref,PlayerRef playerRef,World world){
                String item=ctx.getInputString().trim();
                if(item.isEmpty()){playerRef.sendMessage(Message.raw("Usage: /enhance <item_name>"));return;}
                UUID uid=playerRef.getUuid();
                int currentLevel=getLevel(uid,item);
                if(currentLevel>=10){playerRef.sendMessage(Message.raw("[Enhance] §6"+item+" is at maximum level +10!"));return;}
                int[] data=LEVELS[currentLevel];
                int successPct=data[0]; int cost=data[1];
                // Check balance (simulated)
                playerRef.sendMessage(Message.raw("[Enhance] §6"+item+" §r[+"+currentLevel+" → +"+(currentLevel+1)+"] | Cost: §e"+cost+" coins§r | Success: §a"+successPct+"%"));
                // Roll
                boolean success=rng.nextInt(100)<successPct;
                if(success){
                    enhancements.computeIfAbsent(uid,k->new HashMap<>()).put(item.toLowerCase(),currentLevel+1);
                    save();
                    String stars="★".repeat(currentLevel+1)+"☆".repeat(9-currentLevel);
                    playerRef.sendMessage(Message.raw("§a[Enhance] SUCCESS! §6"+item+" §ris now §e+"+(currentLevel+1)+" §r["+stars+"]"));
                    System.out.println("[Enhancer] "+playerRef.getUsername()+" enhanced "+item+" to +"+(currentLevel+1));
                }else{
                    // Fail doesn't drop level until +7 (just loses coins)
                    if(currentLevel>=6){enhancements.computeIfAbsent(uid,k->new HashMap<>()).merge(item.toLowerCase(),-1,Integer::sum);}
                    save();
                    playerRef.sendMessage(Message.raw("§c[Enhance] FAILED. §6"+item+" §r["+(currentLevel>=6?"dropped to +"+(currentLevel-1):"stays at +"+currentLevel)+"]"));
                }
            }
        };
    }

    public AbstractPlayerCommand getEnhanceInfoCommand(){
        return new AbstractPlayerCommand("enhanceinfo","View enhance rates and costs. /enhanceinfo"){
            @Override protected void execute(CommandContext ctx,Store<EntityStore> store,Ref<EntityStore> ref,PlayerRef playerRef,World world){
                playerRef.sendMessage(Message.raw("=== Enhancement Table ==="));
                playerRef.sendMessage(Message.raw("  Level | Success | Cost     | On Fail"));
                for(int i=0;i<LEVELS.length;i++){
                    String fail=i>=6?"drop to +"+(i):"keep +"+i;
                    playerRef.sendMessage(Message.raw(String.format("  +%2d    | %3d%%     | %,6d c | %s",i+1,LEVELS[i][0],LEVELS[i][1],fail)));
                }
            }
        };
    }
}
