package modernmods.modernfoundry.tables.recipe;

import modernmods.modernfoundry.library.utils.TagUtil;
import lombok.Getter;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import modernmods.hilt.data.loadable.common.IngredientLoadable;
import modernmods.hilt.data.loadable.field.ContextKey;
import modernmods.hilt.data.loadable.primitive.IntLoadable;
import modernmods.hilt.data.loadable.record.RecordLoadable;
import modernmods.hilt.util.RegistryHelper;
import modernmods.modernfoundry.TConstruct;
import modernmods.modernfoundry.common.TinkerTags;
import modernmods.modernfoundry.library.recipe.RecipeResult;
import modernmods.modernfoundry.library.recipe.modifiers.adding.IncrementalModifierRecipe;
import modernmods.modernfoundry.library.recipe.tinkerstation.IDisplayToolTinkering;
import modernmods.modernfoundry.library.recipe.tinkerstation.IMutableTinkerStationContainer;
import modernmods.modernfoundry.library.recipe.tinkerstation.ITinkerStationContainer;
import modernmods.modernfoundry.library.recipe.tinkerstation.ITinkerStationRecipe;
import modernmods.modernfoundry.library.tools.helper.ToolDamageUtil;
import modernmods.modernfoundry.library.tools.item.IModifiableDisplay;
import modernmods.modernfoundry.library.tools.nbt.LazyToolStack;
import modernmods.modernfoundry.library.tools.nbt.StatsNBT;
import modernmods.modernfoundry.library.tools.nbt.ToolStack;
import modernmods.modernfoundry.library.tools.stat.ToolStats;
import modernmods.modernfoundry.tables.TinkerTables;

import java.util.List;

/** Recipe for damaging a tool in the tinker station. */
public class TinkerStationDamagingRecipe implements ITinkerStationRecipe, IDisplayToolTinkering {
  public static final RecordLoadable<TinkerStationDamagingRecipe> LOADER = RecordLoadable.create(
    ContextKey.ID.requiredField(),
    IngredientLoadable.DISALLOW_EMPTY.requiredField("ingredient", r -> r.ingredient),
    IntLoadable.FROM_ONE.requiredField("damage_amount", r -> r.damageAmount),
    TinkerStationDamagingRecipe::new);
  private static final RecipeResult<LazyToolStack> BROKEN = RecipeResult.failure(TConstruct.makeTranslationKey("recipe", "damaging.broken"));
  private static final Component TITLE = TConstruct.makeTranslation("recipe", "tool_damaging");
  private static final Component TOOLTIP = TConstruct.makeTranslation("recipe", "tool_damaging.tooltip");
  private static final String KEY_AMOUNT = TConstruct.makeTranslationKey("recipe", "modifier.amount");

  @Getter
  private final ResourceLocation id;
  private final Ingredient ingredient;
  private final int damageAmount;
  private final Component amountText;

  public TinkerStationDamagingRecipe(ResourceLocation id, Ingredient ingredient, int damageAmount) {
    this.id = id;
    this.ingredient = ingredient;
    this.damageAmount = damageAmount;
    this.amountText = Component.translatable(KEY_AMOUNT, damageAmount);
  }

  @Override
  public boolean matches(ITinkerStationContainer inv, Level world) {
    if (!inv.getTinkerableStack().is(TinkerTags.Items.DURABILITY)) {
      return false;
    }
    // must find at least one input, but multiple is fine, as is empty slots
    return IncrementalModifierRecipe.containsOnlyIngredient(inv, ingredient);
  }

  @Override
  public RecipeResult<LazyToolStack> getValidatedResult(ITinkerStationContainer inv, RegistryAccess access) {
    ToolStack tool = inv.getTinkerable();
    if (tool.isBroken()) {
      return BROKEN;
    }
    // simply damage the tool directly
    tool = tool.copy();
    int maxDamage = IncrementalModifierRecipe.getAvailableAmount(inv, ingredient, damageAmount);
    ItemStack tinkerable = inv.getTinkerableStack();
    ToolDamageUtil.directDamage(tool, maxDamage, null, tinkerable);
    return LazyToolStack.successCopy(tool, 1, tinkerable);
  }

  @Override
  public int shrinkToolSlotBy() {
    return 1;
  }

  @Override
  public void updateInputs(LazyToolStack result, IMutableTinkerStationContainer inv, boolean isServer) {
    // how much did we actually consume?
    int damageTaken = result.getTool().getDamage() - inv.getTinkerable().getDamage();
    IncrementalModifierRecipe.updateInputs(inv, ingredient, damageTaken, damageAmount, ItemStack.EMPTY);
  }

  @Override
  public RecipeSerializer<?> getSerializer() {
    return TinkerTables.tinkerStationDamagingSerializer.get();
  }


  /* JEI */

  /** Tools for display in JEI */
  private List<ItemStack> toolWithoutModifier, toolWithModifier;

  @Override
  public ResourceLocation getRecipeId() {
    return getId();
  }

  @Override
  public Component getTitle() {
    return TITLE;
  }

  @Override
  public Component getTooltip() {
    return TOOLTIP;
  }

  @Override
  public boolean isToolCatalyst() {
    return true;
  }

  @Override
  public Component getVariant() {
    return amountText;
  }

  @Override
  public int getMaxToolSize() {
    return 1;
  }

  @Override
  public int getInputCount() {
    return 1;
  }

  @Override
  public List<ItemStack> getDisplayItems(int slot) {
    return slot == 0 ? List.of(ingredient.getItems()) : List.of();
  }

  @Override
  public List<ItemStack> getToolWithoutModifier() {
    if (toolWithoutModifier == null) {
      // set durability on each tool to 1000, covers most instances
      CompoundTag stats = StatsNBT.builder().set(ToolStats.DURABILITY, 1000).build().serializeToNBT();
      toolWithoutModifier = RegistryHelper.getTagValueStream(BuiltInRegistries.ITEM, TinkerTags.Items.DURABILITY)
        .map(item -> {
          if (item instanceof IModifiableDisplay modifiable) {
            ItemStack stack = modifiable.getRenderTool().copy();
            CompoundTag tag = TagUtil.getOrCreateTag(stack);
            tag.put(ToolStack.TAG_STATS, stats);
            TagUtil.setTag(stack, tag);
            return stack;
          }
          return ItemStack.EMPTY;
        })
        .filter(stack -> !stack.isEmpty())
        .toList();
    }
    return toolWithoutModifier;
  }

  @Override
  public List<ItemStack> getToolWithModifier() {
    if (toolWithModifier == null) {
      toolWithModifier = getToolWithoutModifier().stream()
        .map(stack -> {
          stack = stack.copy();
          CompoundTag tag = TagUtil.getOrCreateTag(stack);
          tag.putInt(ToolStack.TAG_DAMAGE, damageAmount);
          TagUtil.setTag(stack, tag);
          return stack;
        }).toList();
    }
    return toolWithModifier;
  }

  @Override
  public boolean isTool(ItemStack check) {
    return check.is(TinkerTags.Items.DURABILITY);
  }

  @Override
  public RecipeResult<ItemStack> onFocused(ItemStack focus) {
    ToolStack tool = ToolStack.copyFrom(focus);
    ToolDamageUtil.directDamage(tool, damageAmount, null, focus);
    return RecipeResult.success(tool.copyStack(focus));
  }
}
