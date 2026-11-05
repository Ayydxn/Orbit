package me.ayydxn.orbit.mixin.external;

import be.stef.arcana.extractor.SevenZExtractor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SevenZExtractor.class)
public class SevenZExtractorMixin
{
    @Mutable
    @Shadow
    @Final
    private int memoryLimitKiB;

    @Inject(method = "<init>([BI)V", at = @At("TAIL"))
    public void increaseMemoryLimit(byte[] password, int memoryLimitKiB, CallbackInfo callbackInfo)
    {
        // Forcefully bump the memory limit to 256KB so that we can extract the Ultralight binaries without the library throwing a SevenZMemoryLimitException
        // while still keeping safe against things like ZIP bombs.
        this.memoryLimitKiB = 262144;
    }
}
