package com.skd.enchantersbench.client.gui;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.skd.enchantersbench.EnchantersBench;
import com.skd.enchantersbench.EnchantmentLogic;
import com.skd.enchantersbench.ModObjects;
import com.skd.enchantersbench.api.event.InfoBoxEvent;
import com.skd.enchantersbench.inventory.AdvancedTableMenu;
import com.skd.enchantersbench.network.EnchantMessage;
import com.skd.enchantersbench.network.SliderUpdateMessage;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * The screen of the advanced enchanting table. It shows a list of every valid enchantment, where each entry has a
 * slider to pick the level, plus an enchant button.
 */
public class AdvancedTableScreen extends AbstractContainerScreen<AdvancedTableMenu> {

    static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(EnchantersBench.MODID, "textures/gui/enchant.png");

    static final int LABEL_WIDTH = 143;
    static final int LABEL_HEIGHT = 18;
    static final int VISIBLE_LABELS = 4;

    private final List<EnchantmentLabel> labels = new ArrayList<>();
    private final List<EnchantmentLabel> visibleLabels = new ArrayList<>();

    private EnchantmentLabel selected;

    private int listOffset;
    private boolean isSliding;

    private final EnchantButton enchantButton;
    private final ScrollButton scrollbar;

    private final String[] tips = { "description", "books", "treasure", "curse", "storage", "inventory", "armor" };

    public AdvancedTableScreen(AdvancedTableMenu menu, Inventory inventory, Component title) {

        super(menu, inventory, title);

        this.imageWidth = 235;
        this.imageHeight = 182;

        this.titleLabelX = 32;
        this.titleLabelY = 5;
        this.inventoryLabelX = 43;
        this.inventoryLabelY = 139;

        this.enchantButton = new EnchantButton();
        this.scrollbar = new ScrollButton(0);
    }

    /**
     * Gets the menu of the screen, for the widgets which need access to the logic.
     */
    public AdvancedTableMenu getTableMenu() {

        return this.menu;
    }

    @Override
    protected void init() {

        super.init();

        this.isSliding = false;

        this.enchantButton.setPosition(this.leftPos + 35, this.topPos + 38);
        this.scrollbar.setPosition(this.leftPos + 206, this.topPos + 16);

        this.addRenderableWidget(this.enchantButton);
        this.addRenderableWidget(this.scrollbar);

        this.populateLabels();
        this.updateVisibleLabels();
    }

    @Override
    protected void containerTick() {

        super.containerTick();

        this.populateLabels();
        this.updateVisibleLabels();
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {

        graphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);

        for (EnchantmentLabel label : this.visibleLabels) {

            label.draw(graphics, this.font);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {

        super.render(graphics, mouseX, mouseY, partialTick);

        this.enchantButton.renderTooltip(graphics, mouseX, mouseY);
    }

    /**
     * Rebuilds the list of enchantment labels. The list is sorted alphabetically to keep the order stable.
     */
    private void populateLabels() {

        final List<Holder<Enchantment>> valid = new ArrayList<>(this.menu.logic.getValidEnchantments());

        valid.sort(Comparator.comparing(AdvancedTableScreen::sortName));

        this.labels.clear();

        for (Holder<Enchantment> enchantment : valid) {

            final int level = this.menu.logic.getCurrentLevel(enchantment);
            final int x = this.leftPos + 61;
            final int y = this.topPos + 15 + this.labels.size() * (LABEL_HEIGHT + 1);

            this.labels.add(new EnchantmentLabel(this, enchantment, level, x, y));
        }

        this.lockLabels();
    }

    private static String sortName(Holder<Enchantment> enchantment) {

        final var key = EnchantmentLogic.enchantmentKey(enchantment);

        return key == null ? "" : key.toString();
    }

    /**
     * Updates which labels are visible, based on the current scroll position.
     */
    private void updateVisibleLabels() {

        this.visibleLabels.clear();

        this.listOffset = Math.max(0, Math.min(this.listOffset, Math.max(0, this.labels.size() - VISIBLE_LABELS)));

        for (int i = this.listOffset; i < Math.min(this.labels.size(), this.listOffset + VISIBLE_LABELS); i++) {

            final EnchantmentLabel label = this.labels.get(i);

            label.setyPos(this.topPos + 15 + this.visibleLabels.size() * (LABEL_HEIGHT + 1));
            this.visibleLabels.add(label);
        }

        this.lockLabels();
    }

    /**
     * Locks every label which can not be changed, either because it is already maxed out or because it is not
     * compatible with one of the enchantments already on the item.
     */
    private void lockLabels() {

        for (EnchantmentLabel label : this.labels) {

            label.setLocked(false);

            final Holder<Enchantment> holder = label.getHolder();

            for (var data : this.menu.logic.getCurrentEnchantments().entrySet()) {

                final boolean same = data.getKey().equals(holder);
                final boolean isIncompatible = !same && data.getValue() > 0 && !Enchantment.areCompatible(data.getKey(), holder);
                final boolean isOverLeveled = same && data.getValue() > label.getEnchantment().getMaxLevel();

                if (isOverLeveled || isIncompatible) {

                    label.setLocked(true);
                    break;
                }
            }
        }
    }

    private EnchantmentLabel getLabelUnderMouse(double mouseX, double mouseY) {

        for (EnchantmentLabel label : this.visibleLabels) {

            if (label.isMouseOver(mouseX, mouseY)) {

                return label;
            }
        }

        return null;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {

        super.mouseClicked(mouseX, mouseY, button);

        this.selected = this.getLabelUnderMouse(mouseX, mouseY);

        if (this.selected != null && !this.selected.isLocked()) {

            this.selected.setDragging(true);
            this.selected.updateSlider((int) mouseX - this.selected.getxPos());
            this.lockLabels();
        }

        else {

            this.selected = null;
        }

        if (this.labels.size() > VISIBLE_LABELS && this.scrollbar.isActive() && this.scrollbar.isMouseOver(mouseX, mouseY)) {

            this.isSliding = true;
        }

        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {

        final boolean result = super.mouseReleased(mouseX, mouseY, button);

        if (this.selected != null) {

            this.selected.setDragging(false);
            this.selected = null;
            this.lockLabels();
        }

        this.isSliding = false;

        return result;
    }

        @Override
        public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {

            final boolean result = super.mouseDragged(mouseX, mouseY, button, dragX, dragY);

            if (this.isSliding && this.scrollbar.isActive()) {

                this.scrollbar.onSliderDragged(mouseY);
                this.updateVisibleLabels();
            }

            if (this.selected != null) {

            if (mouseX < this.selected.getxPos() || mouseX > this.selected.getxPos() + this.selected.getWidth()) {

                this.selected.setDragging(false);
                this.selected = null;

                return result;
            }

            this.selected.updateSlider((int) mouseX - this.selected.getxPos());
            this.lockLabels();
        }

        return result;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {

        if (this.labels.size() <= VISIBLE_LABELS) {

            return false;
        }

        final int previous = this.listOffset;

        if (scrollY > 0) {

            this.listOffset--;
        }

        else if (scrollY < 0) {

            this.listOffset++;
        }

        this.listOffset = Math.max(0, Math.min(this.listOffset, this.labels.size() - VISIBLE_LABELS));

        if (this.listOffset != previous) {

            this.scrollbar.updateSliderPosition(this.listOffset, this.labels.size() - VISIBLE_LABELS);
        }

        return true;
    }

    /**
     * Applies the enchantment to the item, telling the server to do the same.
     */
    public void enchantItem() {

        if (this.canAfford() && this.menu.logic.getCost() > 0) {

            PacketDistributor.sendToServer(new EnchantMessage());
            this.menu.logic.enchantItem();
        }
    }

    public boolean canAfford() {

        return this.menu.logic.getCost() <= EnchantmentLogic.getExperience(this.menu.logic.getPlayer()) || this.menu.logic.getPlayer().isCreative();
    }

    public EnchantmentLabel getSelected() {

        return this.selected;
    }

    public boolean isSliding() {

        return this.isSliding;
    }

    public int getListOffset() {

        return this.listOffset;
    }

    public int getLabelCount() {

        return this.labels.size();
    }

    public ScrollButton getScrollbar() {

        return this.scrollbar;
    }

    /**
     * Sets the scroll offset of the label list. Used by the scrollbar while dragging.
     */
    public void setListOffset(int sliderY) {

        final int maxOffset = Math.max(0, this.labels.size() - VISIBLE_LABELS);

        if (maxOffset <= 0) {

            this.listOffset = 0;
        }

        else {

            this.listOffset = Math.round((float) (sliderY - 1) / 56F * maxOffset);
            this.listOffset = Math.max(0, Math.min(this.listOffset, maxOffset));
        }
    }

    /**
     * Sends a slider update message to the server.
     */
    public void sendSliderUpdate(Holder<Enchantment> enchantment, int level) {

        PacketDistributor.sendToServer(new SliderUpdateMessage(enchantment, level));
    }

    /**
     * Renders the info box and the description of the label under the mouse.
     */
    @Override
    protected void renderTooltip(GuiGraphics graphics, int mouseX, int mouseY) {

        final List<Component> info = new ArrayList<>();

        if (this.menu.logic.getInventory().getEnchantingStack().isEmpty()) {

            info.add(Component.translatable("gui." + EnchantersBench.MODID + ".info.noitem"));
        }

        else if (this.labels.isEmpty()) {

            info.add(Component.translatable("gui." + EnchantersBench.MODID + ".info.noench"));
        }

        else {

            final boolean isCreative = this.menu.logic.getPlayer().isCreative();
            final int playerXP = isCreative ? Integer.MAX_VALUE : EnchantmentLogic.getExperience(this.menu.logic.getPlayer());
            final int cost = this.menu.logic.getCost();

            if (isCreative) {

                info.add(Component.translatable(EnchantersBench.MODID + ".info.infinity"));
            }

            else {

                info.add(Component.translatable(EnchantersBench.MODID + ".info.playerxp", playerXP));
            }

            info.add(Component.translatable(EnchantersBench.MODID + ".info.costxp", cost));
            info.add(Component.translatable(EnchantersBench.MODID + ".info.power", String.format("%.2f", this.menu.logic.getEnchantmentPower())));

            if (cost > playerXP) {

                info.add(CommonComponents.EMPTY);
                info.add(Component.translatable("gui." + EnchantersBench.MODID + ".info.tooexpensive").withStyle(ChatFormatting.RED));
            }
        }

        info.add(CommonComponents.EMPTY);
        info.add(Component.translatable(EnchantersBench.MODID + ".info.tip.prefix").withStyle(ChatFormatting.YELLOW)
                .append(Component.translatable(EnchantersBench.MODID + ".info.tip." + this.tips[0], this.minecraft.options.keyShift.getTranslatedKeyMessage())));

        final InfoBoxEvent event = new InfoBoxEvent(this, info);
        NeoForge.EVENT_BUS.post(event);

        final List<Component> infoText = event.getInfo();

        if (!infoText.isEmpty()) {

            graphics.renderTooltip(this.font, infoText, java.util.Optional.empty(), this.leftPos - 18, this.topPos + 10);
        }

        super.renderTooltip(graphics, mouseX, mouseY);

        if (this.minecraft.options.keyShift.isDown()) {

            final EnchantmentLabel label = this.getLabelUnderMouse(mouseX, mouseY);

            if (label != null) {

                graphics.renderTooltip(this.font, List.of(label.getDescription()), java.util.Optional.empty(), mouseX, mouseY);
            }
        }
    }

    /**
     * A button which renders an item stack.
     */
    public static class ItemStackButton extends AbstractWidget {

        protected ItemStack stack = ItemStack.EMPTY;

        public ItemStackButton(int x, int y, int width, int height, Component message) {

            super(x, y, width, height, message);
        }

        public void setStack(ItemStack stack) {

            this.stack = stack;
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {

            if (!this.stack.isEmpty()) {

                graphics.renderItem(this.stack, this.getX() + 1, this.getY() + 1);
            }

            if (this.isHovered()) {

                renderSlotHighlight(graphics, this.getX(), this.getY(), 0);
            }
        }

        @Override
        protected void updateWidgetNarration(net.minecraft.client.gui.narration.NarrationElementOutput output) {

            output.add(net.minecraft.client.gui.narration.NarratedElementType.TITLE, this.getMessage());
        }
    }

    /**
     * The button which applies the selected enchantments.
     */
    public class EnchantButton extends ItemStackButton {

        public EnchantButton() {

            super(0, 0, 18, 18, Component.empty());

            this.setStack(this.getDisplayStack());
        }

        private ItemStack getDisplayStack() {

            return EnchantmentLogic.isWikedNight(AdvancedTableScreen.this.menu.logic.getWorld())
                    ? new ItemStack(Items.BONE)
                    : new ItemStack(Items.ENCHANTED_BOOK);
        }

        @Override
        public void onClick(double mouseX, double mouseY) {

            AdvancedTableScreen.this.enchantItem();
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {

            this.setStack(this.getDisplayStack());

            super.renderWidget(graphics, mouseX, mouseY, partialTick);
        }

        public void renderTooltip(GuiGraphics graphics, int mouseX, int mouseY) {

            if (!this.isHovered) {

                return;
            }

            final List<Component> text = new ArrayList<>();

            if (!AdvancedTableScreen.this.canAfford()) {

                text.add(Component.translatable("gui." + EnchantersBench.MODID + ".tooltip.tooexpensive"));
            }

            else if (AdvancedTableScreen.this.menu.logic.getCost() == 0) {

                text.add(Component.translatable("gui." + EnchantersBench.MODID + ".tooltip.nochange"));
            }

            else {

                text.add(Component.translatable("gui." + EnchantersBench.MODID + ".tooltip.enchant"));
            }

            graphics.renderTooltip(AdvancedTableScreen.this.font, text, java.util.Optional.empty(), mouseX, mouseY);
        }
    }

    /**
     * The scrollbar of the enchantment list.
     */
    public class ScrollButton extends AbstractWidget {

        public int sliderY = 1;

        public ScrollButton(int unused) {

            super(0, 0, 12, 15, Component.empty());
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {

            if (!this.visible) {

                return;
            }

            if (AdvancedTableScreen.this.getLabelCount() <= VISIBLE_LABELS) {

                return;
            }

            final int centered = AdvancedTableScreen.this.isSliding() ? this.width : 0;

            graphics.blit(TEXTURE, this.getX(), this.getY() + this.sliderY, centered, 182, this.width, this.height);
        }

        /**
         * Positions the slider based on the scroll position. Used when the list is scrolled with the mouse wheel.
         */
        public void updateSliderPosition(int offset, int maxOffset) {

            final int range = this.height - 7;

            this.sliderY = maxOffset <= 0 ? 1 : 1 + (int) ((float) offset / (float) maxOffset * range);
        }

        /**
         * Handles the dragging of the slider.
         */
        public void onSliderDragged(double mouseY) {

            this.sliderY = (int) (mouseY - this.getY() - 7);
            this.sliderY = Math.max(1, this.sliderY);
            this.sliderY = Math.min(56, this.sliderY);

            AdvancedTableScreen.this.setListOffset(this.sliderY);
        }

        @Override
        protected void updateWidgetNarration(net.minecraft.client.gui.narration.NarrationElementOutput output) {

            output.add(net.minecraft.client.gui.narration.NarratedElementType.TITLE, this.getMessage());
        }
    }
}
