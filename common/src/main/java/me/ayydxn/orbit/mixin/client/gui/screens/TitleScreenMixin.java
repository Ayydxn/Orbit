package me.ayydxn.orbit.mixin.client.gui.screens;

import me.ayydxn.orbit.gui.screens.NativeLibraryLoadFailureScreen;
import me.ayydxn.orbit.utils.NativeLibraryLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public class TitleScreenMixin extends Screen
{
	@Unique
	private static boolean hasFailureScreenBeenShown = false;

    public TitleScreenMixin(Component title)
    {
        super(title);
    }

    @Inject(method = { "init", "tick" }, at = @At("TAIL"))
    public void displayFailedIntegrityCheckScreenIfNeeded(CallbackInfo callbackInfo)
    {
		if (NativeLibraryLoader.getLoadStatus().isFailed() && !hasFailureScreenBeenShown)
		{
			Minecraft.getInstance().setScreen(new NativeLibraryLoadFailureScreen(NativeLibraryLoader.getLoadStatus()));

			hasFailureScreenBeenShown = true;
		}
    }
}
