package de.weinschenk.suflux.screen;

import de.weinschenk.suflux.menu.FluxInfuserMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class FluxInfuserScreen extends AbstractContainerScreen<FluxInfuserMenu> {

    // Furnace-Textur als Platzhalter bis eine eigene erstellt wird
    private static final ResourceLocation TEXTURE =
            new ResourceLocation("minecraft", "textures/gui/container/furnace.png");

    public FluxInfuserScreen(FluxInfuserMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth  = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = (this.width  - this.imageWidth)  / 2;
        int y = (this.height - this.imageHeight) / 2;

        // Hintergrund der Furnace-Textur zeichnen
        graphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);

        // Fortschritts-Pfeil (Furnace-Textur hat Arrow bei u=176, v=14)
        int progress    = this.menu.getProgress();
        int maxProgress = this.menu.getMaxProgress();
        int arrowWidth  = maxProgress > 0 ? (24 * progress / maxProgress) : 0;
        graphics.blit(TEXTURE, x + 79, y + 34, 176, 14, arrowWidth, 16);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, delta);
        this.renderTooltip(graphics, mouseX, mouseY);

        // Energie-Anzeige als Text (da keine eigene GUI-Textur)
        int x = (this.width  - this.imageWidth)  / 2;
        int y = (this.height - this.imageHeight) / 2;
        int energy    = this.menu.getEnergy();
        int maxEnergy = this.menu.getMaxEnergy();
        graphics.drawString(this.font,
                energy + " / " + maxEnergy + " FE",
                x + 8, y + 6, 0x404040, false);
    }
}
