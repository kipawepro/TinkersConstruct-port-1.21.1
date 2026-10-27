package modernmods.modernfoundry.plugin.jei.modifiers;

import modernmods.modernfoundry.plugin.jei.util.TooltipWidget;
import mezz.jei.api.gui.placement.HorizontalAlignment;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import net.minecraft.network.chat.Component;
import modernmods.modernfoundry.TConstruct;
import modernmods.modernfoundry.library.recipe.tinkerstation.IDisplayToolTinkering;
import modernmods.modernfoundry.plugin.jei.TConstructJEIConstants;
import modernmods.modernfoundry.tables.TinkerTables;

import java.awt.Color;

/** Displays tool modifications that are not adding modifiers, such as part swapping or tool damaging. */
public class ToolTinkeringCategory extends AbstractTinkerStationCategory<IDisplayToolTinkering> {
  private static final Component TITLE = TConstruct.makeTranslation("jei", "tool_tinkering.title");

  public ToolTinkeringCategory(IGuiHelper helper) {
    super(helper, TConstructJEIConstants.TOOL_MODIFICATION, TITLE, helper.createDrawableItemLike(TinkerTables.tinkersAnvil));
  }

  @Override
  protected boolean isToolCatalyst(IDisplayToolTinkering recipe) {
    return recipe.isToolCatalyst();
  }

  @Override
  public void createRecipeExtras(IRecipeExtrasBuilder builder, IDisplayToolTinkering recipe, IFocusGroup focuses) {
    super.createRecipeExtras(builder, recipe, focuses);

    // add title and tooltip
    builder.addText(recipe.getTitle(), 124, 10)
      .setShadow(true).setColor(Color.WHITE.getRGB())
      .setTextAlignment(HorizontalAlignment.CENTER).setPosition(3, 3);
    TooltipWidget.area(builder, 3, 3, 124, 10).setTooltip(recipe.getTooltip());
  }
}
