package me.ayydxn.orbit.fabric;

import me.ayydxn.orbit.OrbitMod;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class OrbitClientModFabric implements ClientModInitializer
{
    @Override
    public void onInitializeClient()
    {
        OrbitMod.initialize();
    }
}
