package me.ayydxn.orbit.gui.screens;

import me.ayydxn.orbit.utils.LibraryIntegrityStatus;
import me.ayydxn.orbit.utils.OrbitConstants;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.joml.Vector2f;

import java.awt.*;

public class FailedIntegrityCheckScreen extends Screen
{
    private static final int SCREEN_PADDING = 32;
    private static final int BUTTON_WIDTH = 140;
    private static final int BUTTON_HEIGHT = 20;

    private final LibraryIntegrityStatus libraryIntegrityStatus;
    private final Minecraft client;

    public FailedIntegrityCheckScreen(LibraryIntegrityStatus libraryIntegrityStatus)
    {
        super(Component.translatable("orbit.texts.native_library_integrity_check_failed.title"));

        this.libraryIntegrityStatus = libraryIntegrityStatus;

        // (Ayydxn) I know the Screen class provides a variable of the current instance of the game.
        // I just want to get around that potential NullPointerException IDE warning.
        this.client = Minecraft.getInstance();
    }

    @Override
    protected void init()
    {
        int yPos = this.height - SCREEN_PADDING - BUTTON_HEIGHT;

        this.addRenderableWidget(Button.builder(Component.literal("Open Orbit Folder"), button -> Util.getPlatform().openFile(OrbitConstants.ORBIT_DIRECTORY.toFile()))
                .bounds(SCREEN_PADDING + 270, yPos - 130, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build());

        this.addRenderableWidget(Button.builder(Component.literal("Close Game"), button -> this.client.stop())
                .bounds(this.width - SCREEN_PADDING - BUTTON_WIDTH - 280, yPos - 130, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick)
    {
        this.renderBackground(guiGraphics);

        float scale = 1.5f;
        Vector2f titleTextPos = this.getTitleTextPosition(scale);

        MutableComponent libraryFailureText = switch (this.libraryIntegrityStatus.getFailureReason())
        {
            case JNI_BRIDGE_INAUTHENTIC -> Component.translatable("orbit.texts.native_library_integrity_check_failed.jni_failed");
            case ULTRALIGHT_LIBRARIES_INAUTHENTIC -> Component.translatable("orbit.texts.native_library_integrity_check_failed.ultralight_failed");
            default -> Component.literal("-"); // (Ayydxn) Maybe replace with something more descriptive in the future, but this should never happen for now.
        };

        MutableComponent bodyTextComponent = libraryFailureText.append("\n\n")
                .append(Component.translatable("orbit.texts.native_library_integrity_check_failed.reason"))
                .append("\n\n")
                .append(Component.translatable("orbit.texts.native_library_integrity_check_failed.fix"))
                .append("\n\n")
                .append(Component.translatable("orbit.texts.native_library_integrity_check_failed.advancedUsers"))
                .append("\n\n")
                .append(Component.translatable("orbit.texts.native_library_integrity_check_failed.report"));
        MultiLineLabel bodyText = MultiLineLabel.create(this.font, bodyTextComponent, this.width - 50);

        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(scale, scale, 1.0f);

        guiGraphics.drawCenteredString(this.font, this.title, (int) titleTextPos.x, (int) titleTextPos.y, Color.RED.getRGB());

        guiGraphics.pose().popPose();

        bodyText.renderLeftAligned(guiGraphics, 25, this.height / 4, this.client.font.lineHeight * 2, Color.WHITE.getRGB());

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean shouldCloseOnEsc()
    {
        return false;
    }

    private Vector2f getTitleTextPosition(float scale)
    {
        float titleTextXPos = (float) this.width / 2;
        float titleTextYPos = (float) this.height / 4 - 60 + 20;

        return new Vector2f(titleTextXPos / scale, titleTextYPos / scale);
    }
}
