package me.ayydxn.orbit.forge;

import dev.architectury.platform.forge.EventBuses;
import me.ayydxn.orbit.OrbitMod;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(OrbitMod.MOD_ID)
public final class OrbitModForge
{
    public OrbitModForge(FMLJavaModLoadingContext modLoadingContext)
    {
        EventBuses.registerModEventBus(OrbitMod.MOD_ID, modLoadingContext.getModEventBus());

        OrbitMod.initialize();
    }
}
