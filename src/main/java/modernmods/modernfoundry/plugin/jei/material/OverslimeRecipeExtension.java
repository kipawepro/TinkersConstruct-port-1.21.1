package modernmods.modernfoundry.plugin.jei.material;

import modernmods.modernfoundry.plugin.jei.util.PerRecipeCraftingExtension;
import modernmods.modernfoundry.plugin.jei.util.TooltipWidget;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.ingredient.ICraftingGridHelper;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.ICraftingCategoryExtension;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import modernmods.hilt.plugin.jei.HiltJEIConstants;
import modernmods.modernfoundry.TConstruct;
import modernmods.modernfoundry.common.TinkerTags;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.modules.capacity.OverslimeModule;
import modernmods.modernfoundry.library.recipe.modifiers.adding.IDisplayModifierRecipe;
import modernmods.modernfoundry.library.recipe.modifiers.adding.OverslimeCraftingTableRecipe;
import modernmods.modernfoundry.library.recipe.modifiers.adding.OverslimeModifierRecipe;
import modernmods.modernfoundry.library.tools.helper.ModifierUtil;
import modernmods.modernfoundry.library.tools.nbt.ToolStack;
import modernmods.modernfoundry.tools.TinkerModifiers;

import java.awt.Color;
import java.util.Arrays;
import java.util.List;

import static modernmods.modernfoundry.library.recipe.modifiers.adding.IDisplayModifierRecipe.withModifiers;

/** Logic for showing the overslime recipe in the crafting table tab */
public class OverslimeRecipeExtension implements PerRecipeCraftingExtension.Instance {
  private static final String KEY_AMOUNT = TConstruct.makeTranslationKey("recipe", "modifier.amount");
  private static final String KEY_TOOLTIP = TConstruct.makeTranslationKey("recipe", "overslime.tooltip");

  private final OverslimeCraftingTableRecipe recipe;
  /** List of all inputs */
  private final List<List<ItemStack>> inputs;
  /** The ingredient that applies overslime, used on focusing */
  private final List<ItemStack> ingredient;
  /** Result for the recipe */
  private final List<ItemStack> result;

  public OverslimeRecipeExtension(OverslimeCraftingTableRecipe recipe) {
    List<ModifierEntry> result = List.of(OverslimeModifierRecipe.RESULT);

    this.recipe = recipe;
    Ingredient ingredient = recipe.getTools();
    // this recipe requires inputs to have overslime
    List<ItemStack> toolsWithoutOverslime = Arrays.stream(ingredient.getItems()).map(IDisplayModifierRecipe.MAP_TOOL_STACK_FOR_RENDERING)
      .map(stack -> withModifiers(stack, 1, result))
      .toList();
    // result is the same but with overslime
    int restoreAmount = recipe.getRestoreAmount();
    List<ItemStack> toolsWithOverslime = Arrays.stream(ingredient.getItems()).map(IDisplayModifierRecipe.MAP_TOOL_STACK_FOR_RENDERING)
      .map(stack -> withModifiers(stack, 1, result, data -> OverslimeModule.INSTANCE.setAmountRaw(data, restoreAmount)))
      .toList();
    this.ingredient = List.of(recipe.getIngredient().getItems());
    this.inputs = List.of(toolsWithoutOverslime, this.ingredient);
    this.result = toolsWithOverslime;
  }

  public ResourceLocation getRegistryName() {
    return recipe.getId();
  }

  @Override
  public void setRecipe(IRecipeLayoutBuilder builder, ICraftingGridHelper craftingGridHelper, IFocusGroup focuses) {
    builder.setShapeless();
    List<List<ItemStack>> inputStacks = this.inputs;
    List<ItemStack> resultStacks = this.result;

    // if focusing on a tool, make that tool the
    IFocus<ItemStack> focus = focuses.getItemStackFocuses().findFirst().orElse(null);
    if (focus != null && focus.getRole() != RecipeIngredientRole.OUTPUT) {
      ItemStack focusStack = focus.getTypedValue().getIngredient();
      if (focusStack.is(TinkerTags.Items.DURABILITY) && ModifierUtil.getModifierLevel(focusStack, TinkerModifiers.overslime.getId()) > 0) {
        inputStacks = List.of(List.of(focusStack.copyWithCount(1)), ingredient);
        ItemStack result = focusStack.copyWithCount(1);
        ToolStack tool = ToolStack.from(result);
        OverslimeModule.INSTANCE.addAmount(tool, recipe.getRestoreAmount());
        resultStacks = List.of(result);
      }
    }

    // build the grid
    // need to recreate a lot of this to change the slot types
    // TODO: worth considering making the first input slot a catalyst, just requires reimplementing the slot creation
    List<IRecipeSlotBuilder> inputs = craftingGridHelper.createAndSetInputs(builder, inputStacks, 2, 2);
    IRecipeSlotBuilder output = builder.addSlot(RecipeIngredientRole.RENDER_ONLY, 95, 19).setOutputSlotBackground().addItemStacks(resultStacks);
    if (inputs.size() < 9) {
      TConstruct.LOG.error("Failed to create focus link for {} as the layout {} is not 3x3", recipe.getId(), builder.getClass().getName());
    } else {
      builder.createFocusLink(inputs.get(HiltJEIConstants.getCraftingIndex(0, 2, 2)), output);
    }
  }

  @Override
  public void createRecipeExtras(IRecipeExtrasBuilder builder, ICraftingGridHelper craftingGridHelper, IFocusGroup focuses) {
    int restoreAmount = recipe.getRestoreAmount();
    builder.addText(Component.translatable(KEY_AMOUNT, restoreAmount), 57, 9)
      .setPosition(60, 44).setColor(Color.GRAY.getRGB());
    TooltipWidget.area(builder, 60, 44, 57, 9).setTooltip(Component.translatable(KEY_TOOLTIP, restoreAmount));
  }
}
