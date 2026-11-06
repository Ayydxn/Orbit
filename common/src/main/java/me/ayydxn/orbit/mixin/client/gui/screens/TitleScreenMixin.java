package me.ayydxn.orbit.mixin.client.gui.screens;

import dev.architectury.platform.Platform;
import me.ayydxn.orbit.gui.screens.FailedIntegrityCheckScreen;
import me.ayydxn.orbit.utils.NativeLibraryLoader;
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
    public void displayFailedIntegrityCheckScreenIfNeeded(CallbackInfo ci)
    {
        if (!Platform.isDevelopmentEnvironment() && !NativeLibraryLoader.getLibraryIntegrityStatus().isValid())
        {
            // (Ayydxn) Obviously, the Minecraft instance is never going to be null. I just want IntelliJ to shut up about not doing a null check here.
            if (this.minecraft != null)
                this.minecraft.setScreen(new FailedIntegrityCheckScreen(NativeLibraryLoader.getLibraryIntegrityStatus()));
        }
    }
}
