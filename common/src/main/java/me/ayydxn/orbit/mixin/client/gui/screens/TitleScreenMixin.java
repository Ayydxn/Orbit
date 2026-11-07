package me.ayydxn.orbit.mixin.client.gui.screens;

import me.ayydxn.orbit.gui.screens.NativeLibraryLoadFailureScreen;
import me.ayydxn.orbit.utils.NativeLibraryLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public class TitleScreenMixin extends Screen
{
    public TitleScreenMixin(Component title)
    {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    public void displayFailedIntegrityCheckScreenIfNeeded(CallbackInfo callbackInfo)
    {
		if (NativeLibraryLoader.getLoadStatus().isFailed())
			Minecraft.getInstance().setScreen(new NativeLibraryLoadFailureScreen(NativeLibraryLoader.getLoadStatus()));
    }
}
