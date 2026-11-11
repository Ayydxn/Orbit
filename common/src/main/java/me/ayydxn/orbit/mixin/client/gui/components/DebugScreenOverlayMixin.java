package me.ayydxn.orbit.mixin.client.gui.components;

import com.google.common.collect.Lists;
import me.ayydxn.orbit.OrbitMod;
import me.ayydxn.orbit.natives.NativeBackendManager;
import me.ayydxn.orbit.ultralight.Ultralight;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.ArrayList;

@Mixin(DebugScreenOverlay.class)
public class DebugScreenOverlayMixin
{
	@Redirect(method = "getSystemInformation", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/Lists;newArrayList([Ljava/lang/Object;)Ljava/util/ArrayList;", remap = false))
	private ArrayList<String> addOrbitVersioningInformation(Object[] elements)
	{
		ArrayList<String> lines = Lists.newArrayList((String[]) elements);
		lines.add("");
		lines.add("Orbit Version: %s".formatted(OrbitMod.getInstance().getVersion()));
		lines.add("Ultralight Version: %s".formatted(Ultralight.versionString()));
		lines.add("WebKit Version: %s".formatted(Ultralight.webkitVersionString()));
		lines.add("Active Native Backend: %s".formatted(NativeBackendManager.getCurrentBackendInstance().getType().getName()));

		return lines;
	}
}
