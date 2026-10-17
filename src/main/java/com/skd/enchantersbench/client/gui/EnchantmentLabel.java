package com.skd.enchantersbench.client.gui;



import com.skd.enchantersbench.EnchantersBench;
import com.skd.enchantersbench.EnchantmentLogic;

import net.minecraft.client.gui.GuiGraphics;

import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.enchantment.Enchantment;

/**
 * A single row of the enchantment list. It shows the name of the enchantment and a slider to pick the level.
 */
public class EnchantmentLabel {

    private static final int HEIGHT = 18;
    private static final int WIDTH = AdvancedTableScreen.LABEL_WIDTH;

    private static final int COLOR_BACKGROUND_LOCKED = 0x44d10841;
    private static final int COLOR_BACKGROUND_AVAILABLE = 0x445aaeae;

    private final AdvancedTableScreen parent;

    private final Holder<Enchantment> holder;
    private final Enchantment enchantment;
    private final int initialLevel;

    private int currentLevel;

    private int xPos;
    private int yPos;

    private int sliderX;

    private boolean dragging;
    private boolean visible = true;
    private boolean locked;

    protected EnchantmentLabel(AdvancedTableScreen parent, Holder<Enchantment> enchantment, int currentLevel, int x, int y) {

        this.parent = parent;
        this.holder = enchantment;
        this.enchantment = enchantment.value();
        this.currentLevel = currentLevel;
        this.initialLevel = currentLevel;

        this.xPos = x;
        this.yPos = y;

        this.sliderX = this.xPos + 1;

        if (this.currentLevel > this.enchantment.getMaxLevel()) {

            this.locked = true;
        }
    }

    public Holder<Enchantment> getHolder() {

        return this.holder;
    }

    public Enchantment getEnchantment() {

        return this.enchantment;
    }

    public int getInitialLevel() {

        return this.initialLevel;
    }

    public int getWidth() {

        return WIDTH;
    }

    public int getHeight() {

        return HEIGHT;
    }

    public int getxPos() {

        return this.xPos;
    }

    public int getyPos() {

        return this.yPos;
    }

    public void setyPos(int yPos) {

        this.yPos = yPos;
    }

    public boolean isLocked() {

        return this.locked;
    }

    public void setLocked(boolean locked) {

        if (this.currentLevel > this.enchantment.getMaxLevel()) {

            this.locked = true;
        }

        else {

            this.locked = locked;
        }
    }

    public void setDragging(boolean dragging) {

        this.dragging = dragging;
    }

    public boolean isMouseOver(double mouseX, double mouseY) {

        return this.getxPos() <= mouseX && this.getxPos() + this.getWidth() >= mouseX
                && this.getyPos() <= mouseY && this.getyPos() + this.getHeight() >= mouseY;
    }

    /**
     * Draws the label.
     */
    public void draw(GuiGraphics graphics, net.minecraft.client.gui.Font font) {

        if (!this.visible) {

            return;
        }

        final int indexX = this.dragging ? this.sliderX
                : (int) (this.xPos + 1 + (WIDTH - 6) * (this.currentLevel / (double) this.enchantment.getMaxLevel()));

        graphics.fill(this.xPos + 1, this.yPos + 2, this.xPos + WIDTH, this.yPos + HEIGHT, this.locked ? COLOR_BACKGROUND_LOCKED : COLOR_BACKGROUND_AVAILABLE);
        graphics.blit(AdvancedTableScreen.TEXTURE, indexX, this.yPos + 2, this.isSelected() ? 5 : 0, 197, 5, 16);

        final Component name = this.getDisplayName();

        graphics.drawString(font, name, this.xPos + 7, this.yPos + 6, 0x55aaff00, false);
    }

    private boolean isSelected() {

        return this.parent.getSelected() != null && this.parent.getSelected().enchantment == this.enchantment;
    }

    /**
     * Gets the translated name of the enchantment, with the level attached to it.
     */
    public Component getDisplayName() {

        Component name = Component.translatable(this.enchantment.description().getString());

        if (this.holder.is(net.minecraft.tags.EnchantmentTags.CURSE)) {

            name = name.copy().withStyle(net.minecraft.ChatFormatting.RED);
        }

        if (this.currentLevel > 0) {

            name = name.copy().append(Component.literal(" " + this.currentLevel));
        }

        return name;
    }

    /**
     * Updates the position of the slider, and with it the level of the enchantment.
     */
    public void updateSlider(int sliderPos) {

        if (this.locked) {

            return;
        }

        final int min = this.xPos + 1;
        final int max = min + WIDTH - 6;

        this.sliderX = min + sliderPos - 2;

        if (this.sliderX < min) {

            this.sliderX = min;
        }

        else if (this.sliderX > max) {

            this.sliderX = max;
        }

        final float index = sliderPos / (float) (WIDTH - 10);
        final int updatedLevel = Math.round(index * this.enchantment.getMaxLevel());

        if (updatedLevel > this.initialLevel || !this.parent.getTableMenu().logic.getInventory().getEnchantingStack().isDamaged()) {

            this.currentLevel = updatedLevel;
        }

        this.currentLevel = Math.max(0, Math.min(this.currentLevel, this.enchantment.getMaxLevel()));

        this.parent.sendSliderUpdate(this.holder, this.currentLevel);
        this.parent.getTableMenu().logic.updateEnchantment(this.holder, this.currentLevel);
    }

    /**
     * Gets the description of the enchantment, if the enchantment provides one.
     */
    public Component getDescription() {

        final var key = EnchantmentLogic.enchantmentKey(this.holder);

        final String keyString = key == null ? "" : key.getNamespace() + ":" + key.getPath();
        final Component description = Component.translatable("enchantment." + keyString + ".desc");

        if (description.getString().contains(keyString)) {

            return Component.translatable("tooltip." + EnchantersBench.MODID + ".missing", key.getNamespace(), keyString);
        }

        return description;
    }

    /**
     * Handles the mouse drag of the slider.
     */
    public void onDragged(double mouseX) {

        this.updateSlider((int) mouseX - this.xPos);
    }
}
