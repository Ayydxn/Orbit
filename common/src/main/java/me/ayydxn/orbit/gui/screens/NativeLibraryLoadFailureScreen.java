package me.ayydxn.orbit.gui.screens;

import me.ayydxn.orbit.utils.NativeLibraryLoadStatus;
import me.ayydxn.orbit.utils.OrbitConstants;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.Locale;

public class NativeLibraryLoadFailureScreen extends Screen
{
    private static final String TRANSLATION_KEY_PREFIX = "orbit.texts.native_load_failed.";

    private static final int SCREEN_PADDING = 32;
    private static final int BUTTON_WIDTH = 140;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_SPACING = 10;

    private static final float TITLE_SCALE = 1.5f;
    private static final int TITLE_COLOR = 0xFFFF0000;
    private static final int BODY_COLOR = 0xFFFFFFFF;

    private final NativeLibraryLoadStatus.FailureReason failureReason;

    private MultiLineLabel bodyText = MultiLineLabel.EMPTY;

    public NativeLibraryLoadFailureScreen(NativeLibraryLoadStatus nativeLoadStatus)
    {
        super(Component.translatable(TRANSLATION_KEY_PREFIX + "title"));

        this.failureReason = nativeLoadStatus.failureReason();
    }

    @Override
    protected void init()
    {
        this.bodyText = MultiLineLabel.create(this.font, this.buildBodyText(), this.width - SCREEN_PADDING * 2);

        int buttonsY = this.height - SCREEN_PADDING - BUTTON_HEIGHT;
        int buttonsX = (this.width - (BUTTON_WIDTH * 2 + BUTTON_SPACING)) / 2;

        this.addRenderableWidget(Button.builder(Component.translatable(TRANSLATION_KEY_PREFIX + "open_folder"), button -> Util.getPlatform().openFile(OrbitConstants.ORBIT_DIRECTORY.toFile()))
                .bounds(buttonsX, buttonsY, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build());

        this.addRenderableWidget(Button.builder(Component.translatable(TRANSLATION_KEY_PREFIX + "close_game"), button -> Minecraft.getInstance().stop())
                .bounds(buttonsX + BUTTON_WIDTH + BUTTON_SPACING, buttonsY, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick)
    {
        this.renderBackground(guiGraphics);

        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(TITLE_SCALE, TITLE_SCALE, 1.0f);

        guiGraphics.drawCenteredString(this.font, this.title, (int) ((this.width / 2.0f) / TITLE_SCALE), (int) (SCREEN_PADDING / TITLE_SCALE), TITLE_COLOR);

        guiGraphics.pose().popPose();

        int bodyY = SCREEN_PADDING + (int) (this.font.lineHeight * TITLE_SCALE) + 20;
        this.bodyText.renderLeftAligned(guiGraphics, SCREEN_PADDING, bodyY, this.font.lineHeight + 2, BODY_COLOR);

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean shouldCloseOnEsc()
    {
        return false;
    }

    private MutableComponent buildBodyText()
    {
        String reasonKey = TRANSLATION_KEY_PREFIX + this.failureReason.name().toLowerCase(Locale.ROOT);

        MutableComponent bodyText = Component.translatable(reasonKey + ".description")
                .append("\n\n")
                .append(Component.translatable(reasonKey + ".fix"));

        if (this.isIntegrityRelated())
            bodyText.append("\n\n").append(Component.translatable(TRANSLATION_KEY_PREFIX + "advanced_users"));

        return bodyText.append("\n\n").append(Component.translatable(TRANSLATION_KEY_PREFIX + "report"));
    }

    private boolean isIntegrityRelated()
    {
        return switch (this.failureReason)
        {
            case JNI_BRIDGE_INAUTHENTIC, ULTRALIGHT_LIBRARIES_INAUTHENTIC, INTEGRITY_CHECK_UNAVAILABLE -> true;
            default -> false;
        };
    }
}
