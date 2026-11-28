package hayo.features.processing_machine;

import hayo.Hayo;
import hayo.common.HayoSprites;
import hayo.common.IntRange;
import hayo.common.SkipMixinsAtReturnException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.data.AtlasIds;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;
import java.util.stream.Stream;

@Environment(EnvType.CLIENT)
public abstract class SlotAccessComponent<T extends AbstractContainerMenu> implements ScreenEvents.AfterExtract, Renderable, GuiEventListener, NarratableEntry {
    public static final int COLLAPSED_WIDTH = 26, COLLAPSED_HEIGHT = 24;
    public static final int EXPANDED_WIDTH = 98, EXPANDED_HEIGHT = 96;
    public static final int EXPAND_SPEED = 8;

    protected final T menu;
    protected final Screen screen;
    protected final IntRange upgradeSlots;

    private int screenLeft;
    private int screenTop;
    private int screenWidth;
    private int screenHeight;

    private boolean visible = false;
    private SlotAccessState state;
    private boolean open = false;
    private int width = COLLAPSED_WIDTH;
    private int height = COLLAPSED_HEIGHT;

    public SlotAccessComponent(AbstractContainerScreen<T> screen, IntRange upgradeSlots) {
        this.menu = screen.getMenu();
        this.screen = screen;
        this.upgradeSlots = upgradeSlots;
    }

    public void init(int screenLeft, int screenTop, int screenWidth, int screenHeight) {
        this.screenLeft = screenLeft;
        this.screenTop = screenTop;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        // drawing things in this event, and registering like this, to ensure we just render above any recipe viewers and alike
        // (instead of having to deal with multiple APIs, and I don't like how these "exclusion zones" tend to look like,
        // with item movement/animation being particularly distracting)
        Minecraft.getInstance().schedule(() -> ScreenEvents.afterExtract(this.screen).register(this));
    }

    public void tick() {
        if (this.open && this.width < EXPANDED_WIDTH) {
            this.width = Math.min(EXPANDED_WIDTH, this.width + EXPAND_SPEED);
        } else if (!this.open && this.width > COLLAPSED_WIDTH) {
            this.width = Math.max(COLLAPSED_WIDTH, this.width - EXPAND_SPEED);
        }
        if (this.open && this.height < EXPANDED_HEIGHT) {
            this.height = Math.min(EXPANDED_HEIGHT, this.height + EXPAND_SPEED);
        } else if (!this.open && this.height > COLLAPSED_HEIGHT) {
            this.height = Math.max(COLLAPSED_HEIGHT, this.height - EXPAND_SPEED);
        }

        ItemStack upgrade = null;
        for (int i = this.upgradeSlots.start(); i < this.upgradeSlots.end(); i++) {
            var stack = this.menu.getItems().get(i);
            if (stack.is(Hayo.Items.CONFIGURATION_UPGRADE)) {
                upgrade = stack;
                break;
            }
        }
        boolean hasUpgrade = upgrade != null;
        if (this.visible != hasUpgrade) {
            this.visible = hasUpgrade;
            if (hasUpgrade) {
                this.state = upgrade.getOrDefault(Hayo.Components.SLOT_ACCESS, SlotAccessState.DEFAULT);
            } else {
                this.open = false;
                this.state = null;
                this.width = COLLAPSED_WIDTH;
                this.height = COLLAPSED_HEIGHT;
            }
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float tickProgress) {
        if (!this.visible) {
            return;
        }

        if (this.isMouseOver(mouseX, mouseY)) {
            // prevent recipe viewers from rendering item tooltips
            graphics.setTooltipForNextFrame(this.screen.getFont(), Stream.of(Component.empty()).map(Component::getVisualOrderText).toList(), Optional.empty(), DefaultTooltipPositioner.INSTANCE, Integer.MIN_VALUE, Integer.MIN_VALUE, false, null);
        }
    }

    @Override
    public void afterExtract(Screen screen, GuiGraphicsExtractor graphics, int mouseX, int mouseY, float tickProgress) {
        if (!this.visible) {
            return;
        }

        int leftPos = this.screenLeft + this.screenWidth;
        int topPos = this.screenTop + 5;

        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, HayoSprites.PANEL_RIGHT_BACKGROUND, leftPos, topPos, this.width, this.height, 0xFF228866);
        graphics.fakeItem(new ItemStack(Hayo.Items.CONFIGURATION_UPGRADE), leftPos + 4, topPos + 4);

        if (this.width == EXPANDED_WIDTH && this.height == EXPANDED_HEIGHT) {
            graphics.text(this.screen.getFont(), Component.translatable("hayo.machine.configuration"), leftPos + 23, topPos + 9, 0xFF0b2d22, false);

            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, HayoSprites.PANEL_INSET, leftPos + 9, topPos + 26, 79, 60, 0xFF228866);

            var blockAtlas = Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.BLOCKS);
            // FIXME: change depending on the machine
            var frontSprite = blockAtlas.getSprite(Hayo.modId("block/electric_furnace_front"));
            var sideSprite = blockAtlas.getSprite(Hayo.modId("block/machine_side"));
            var topSprite = blockAtlas.getSprite(Hayo.modId("block/machine"));
            var bottomSprite = blockAtlas.getSprite(Hayo.modId("block/refined_iron_block"));

            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, topSprite, leftPos + 31, topPos + 29, 16, 16);
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sideSprite, leftPos + 12, topPos + 48, 16, 16);
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, frontSprite, leftPos + 31, topPos + 48, 16, 16);
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sideSprite, leftPos + 50, topPos + 48, 16, 16);
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sideSprite, leftPos + 69, topPos + 48, 16, 16);
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, bottomSprite, leftPos + 31, topPos + 67, 16, 16);

            for (var side : Side.values()) {
                var pos = this.getSidePos(side);
                var access = this.state.get(side);

                blitAccessTexture(access, graphics, pos.x(), pos.y());
            }

            var targettedSide = this.getTargetedSide(mouseX, mouseY);
            if (targettedSide != null) {
                var access = this.state.get(targettedSide);

                graphics.tooltip(
                        this.screen.getFont(),
                        Stream.of(
                                Component.translatable("hayo.machine.slot_access." + access.name),
                                Component.translatable("hayo.machine.side." + targettedSide.name).withStyle(ChatFormatting.GRAY)
                        ).map(Component::getVisualOrderText).map(ClientTooltipComponent::create).toList(),
                        mouseX,
                        mouseY,
                        DefaultTooltipPositioner.INSTANCE,
                        null,
                        true
                );
            }

        } else if (this.width == COLLAPSED_WIDTH && this.height == COLLAPSED_HEIGHT) {
            if (this.isMouseOver(mouseX, mouseY)) {
                graphics.tooltip(
                        this.screen.getFont(),
                        Stream.of(Component.translatable("hayo.machine.configuration")).map(Component::getVisualOrderText).map(ClientTooltipComponent::create).toList(),
                        mouseX,
                        mouseY,
                        DefaultTooltipPositioner.INSTANCE,
                        null,
                        false
                );
            }
        }
    }

    public static void blitAccessTexture(SlotAccess access, GuiGraphicsExtractor graphics, int x, int y) {
        var sprite = switch (access) {
            case INPUT -> HayoSprites.SLOT_ACCESS_INPUT;
            case OUTPUT -> HayoSprites.SLOT_ACCESS_OUTPUT;
            case OTHER -> HayoSprites.SLOT_ACCESS_OTHER;
            case INPUT_OUTPUT -> HayoSprites.SLOT_ACCESS_INPUT_OUTPUT;
            case ALL -> HayoSprites.SLOT_ACCESS_ALL;
            case NONE -> null;
        };
        if (sprite == null) {
            return;
        }
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, x, y, 16, 16);
    }

    @Override
    public void setFocused(boolean focused) {

    }

    @Override
    public boolean isFocused() {
        return false;
    }

    @Override
    public NarrationPriority narrationPriority() {
        return NarrationPriority.NONE;
    }

    @Override
    public void updateNarration(NarrationElementOutput output) {

    }

    @Override
    public ScreenRectangle getRectangle() {
        return this.visible ? new ScreenRectangle(this.screenLeft + this.screenWidth, this.screenTop + 5, this.width, this.height) : ScreenRectangle.empty();
    }


    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return this.visible && this.getRectangle().containsPoint((int) mouseX, (int) mouseY);
    }

    public ScreenPosition getSidePos(Side side) {
        int leftPos = this.screenLeft + this.screenWidth;
        int topPos = this.screenTop + 5;

        return switch (side) {
            case TOP -> new ScreenPosition(leftPos + 31, topPos + 29);
            case LEFT -> new ScreenPosition(leftPos + 12, topPos + 48);
            case FRONT -> new ScreenPosition(leftPos + 31, topPos + 48);
            case RIGHT -> new ScreenPosition(leftPos + 50, topPos + 48);
            case BACK -> new ScreenPosition(leftPos + 69, topPos + 48);
            case BOTTOM -> new ScreenPosition(leftPos + 31, topPos + 67);
        };
    }

    public Side getTargetedSide(int mouseX, int mouseY) {
        int leftPos = this.screenLeft + this.screenWidth;
        int topPos = this.screenTop + 5;
        if (topPos + 29 <= mouseY && mouseY <= topPos + 45) {
            if (leftPos + 31 <= mouseX && mouseX < leftPos + 47) {
                return Side.TOP;
            }
        } else if (topPos + 48 <= mouseY && mouseY < topPos + 64) {
            if (leftPos + 12 <= mouseX && mouseX < leftPos + 28) {
                return Side.LEFT;
            } else if (leftPos + 31 <= mouseX && mouseX < leftPos + 47) {
                return Side.FRONT;
            } else if (leftPos + 50 <= mouseX && mouseX < leftPos + 66) {
                return Side.FRONT;
            } else if (leftPos + 69 <= mouseX && mouseX < leftPos + 85) {
                return Side.BACK;
            }
        } else if (topPos + 64 <= mouseY && mouseY <= topPos + 80) {
            if (leftPos + 31 <= mouseX && mouseX < leftPos + 47) {
                return Side.BOTTOM;
            }
        }
        return null;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (!this.visible) {
            return false;
        }

        var targettedSide = this.getTargetedSide((int) event.x(), (int) event.y());
        if (targettedSide != null) {
            throw new SkipMixinsAtReturnException();
        }

        this.open = !this.open;
        // throw exception so the mouse click is not handled again by recipe viewers
        // (some of them ignore the return value of Screen#mouseClicked for some reason)
        throw new SkipMixinsAtReturnException();
    }
}
