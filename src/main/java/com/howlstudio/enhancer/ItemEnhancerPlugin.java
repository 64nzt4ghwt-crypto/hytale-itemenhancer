package com.howlstudio.enhancer;
import com.hypixel.hytale.server.core.command.system.CommandManager;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
/**
 * ItemEnhancer — Upgrade system for items. Players spend coins to enhance items through
 * levels (+1 to +10). Each level improves stats with a configurable success rate.
 * Higher levels have lower success and higher cost — risk/reward system.
 */
public final class ItemEnhancerPlugin extends JavaPlugin {
    public ItemEnhancerPlugin(JavaPluginInit init){super(init);}
    @Override protected void setup(){
        System.out.println("[Enhancer] Loading...");
        EnhanceManager mgr=new EnhanceManager(getDataDirectory());
        CommandManager.get().register(mgr.getEnhanceCommand());
        CommandManager.get().register(mgr.getEnhanceInfoCommand());
        System.out.println("[Enhancer] Ready.");
    }
    @Override protected void shutdown(){System.out.println("[Enhancer] Stopped.");}
}
