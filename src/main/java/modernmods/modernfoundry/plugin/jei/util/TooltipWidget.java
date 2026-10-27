package modernmods.modernfoundry.plugin.jei.util;

import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.gui.widgets.IRecipeWidget;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;
import java.util.List;

public class TooltipWidget implements IRecipeWidget {
  private final ScreenPosition position;
  private final int width;
  private final int height;
  @Nullable
  private final IDrawable drawable;
  private List<Component> tooltip = List.of();

  private TooltipWidget(ScreenPosition position, int width, int height, @Nullable IDrawable drawable) {
    this.position = position;
    this.width = width;
    this.height = height;
    this.drawable = drawable;
  }

  public static TooltipWidget drawable(IRecipeExtrasBuilder builder, IDrawable drawable, int x, int y) {
    TooltipWidget widget = new TooltipWidget(new ScreenPosition(x, y), drawable.getWidth(), drawable.getHeight(), drawable);
    builder.addWidget(widget);
    return widget;
  }

  public static TooltipWidget area(IRecipeExtrasBuilder builder, int x, int y, int width, int height) {
    TooltipWidget widget = new TooltipWidget(new ScreenPosition(x, y), width, height, null);
    builder.addWidget(widget);
    return widget;
  }

  public TooltipWidget setTooltip(Component tooltip) {
    this.tooltip = List.of(tooltip);
    return this;
  }

  public TooltipWidget setTooltip(List<Component> tooltip) {
    this.tooltip = tooltip;
    return this;
  }

  @Override
  public ScreenPosition getPosition() {
    return position;
  }

  @Override
  public void drawWidget(GuiGraphics graphics, double mouseX, double mouseY) {
    if (drawable != null) {
      drawable.draw(graphics);
    }
  }

  @Override
  public void getTooltip(ITooltipBuilder builder, double mouseX, double mouseY) {
    if (!tooltip.isEmpty() && mouseX >= 0 && mouseY >= 0 && mouseX < width && mouseY < height) {
      builder.addAll(tooltip);
    }
  }
}
