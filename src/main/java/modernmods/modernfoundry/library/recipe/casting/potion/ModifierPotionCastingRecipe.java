package modernmods.modernfoundry.library.recipe.casting.potion;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import modernmods.hilt.data.loadable.common.IngredientLoadable;
import modernmods.hilt.data.loadable.field.LoadableField;
import modernmods.hilt.recipe.helper.TypeAwareRecipeSerializer;
import modernmods.hilt.recipe.ingredient.FluidIngredient;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierId;
import modernmods.modernfoundry.library.recipe.casting.ICastingContainer;
import modernmods.modernfoundry.library.recipe.casting.PotionCastingRecipe;
import modernmods.modernfoundry.library.recipe.modifiers.adding.IDisplayModifierRecipe;
import modernmods.modernfoundry.library.tools.helper.ModifierUtil;
import modernmods.modernfoundry.library.tools.item.IModifiableDisplay;
import modernmods.modernfoundry.library.tools.nbt.ToolStack;

import java.util.List;

/** Common logic for {@link modernmods.modernfoundry.library.recipe.casting.TippingCastingRecipe} and {@link modernmods.modernfoundry.library.recipe.casting.TipClearingCastingRecipe} */
public abstract class ModifierPotionCastingRecipe extends PotionCastingRecipe {
  protected static final LoadableField<Ingredient, ModifierPotionCastingRecipe> TOOL_FIELD = IngredientLoadable.DISALLOW_EMPTY.requiredField("tools", r -> r.bottle);
  protected static final LoadableField<ModifierId, ModifierPotionCastingRecipe> MODIFIER_FIELD = ModifierId.PARSER.requiredField("modifier", r -> r.modifier);

  protected final ModifierId modifier;
  public ModifierPotionCastingRecipe(TypeAwareRecipeSerializer<?> serializer, ResourceLocation id, String group, Ingredient bottle, FluidIngredient fluid, Item result, int coolingTime, ModifierId modifier) {
    super(serializer, id, group, bottle, fluid, result, coolingTime);
    this.modifier = modifier;
  }

  @Override
  public boolean matches(ICastingContainer inv, Level level) {
    return super.matches(inv, level) && ModifierUtil.getModifierLevel(inv.getStack(), modifier) > 0;
  }


  /* JEI */

  /** Adds the modifier to the given stack */
  public ItemStack addModifier(ItemStack stack) {
    return IDisplayModifierRecipe.withModifiers(IModifiableDisplay.getDisplayStack(stack), List.of(new ModifierEntry(modifier, 1)));
  }

  /**
   * Adds the potion to the given stacks.
   * @param stack    Stack with the modifier.
   * @param potions  List of potions from {@link #getPotionIds()}
   * @return  Stacks with potions set.
   */
  public List<ItemStack> addPotion(ItemStack stack, List<String> potions) {
    return potions.stream().map(id -> {
      ToolStack tool = ToolStack.copyFrom(stack);
      tool.getPersistentData().putString(modifier, id);
      return tool.copyStack(stack);
    }).toList();
  }
}
