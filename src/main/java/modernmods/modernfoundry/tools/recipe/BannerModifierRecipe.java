package modernmods.modernfoundry.tools.recipe;

import lombok.Getter;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import modernmods.hilt.data.loadable.common.IngredientLoadable;
import modernmods.hilt.data.loadable.field.ContextKey;
import modernmods.hilt.data.loadable.record.RecordLoadable;
import modernmods.modernfoundry.TConstruct;
import net.minecraft.world.level.Level;
import modernmods.hilt.recipe.IMultiRecipe;
import modernmods.hilt.util.RegistryHelper;
import modernmods.modernfoundry.common.TinkerTags;
import modernmods.modernfoundry.library.json.IntRange;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierId;
import modernmods.modernfoundry.library.recipe.RecipeResult;
import modernmods.modernfoundry.library.recipe.modifiers.ModifierRecipeLookup;
import modernmods.modernfoundry.library.recipe.modifiers.adding.IDisplayModifierRecipe;
import modernmods.modernfoundry.library.recipe.tinkerstation.ITinkerStationContainer;
import modernmods.modernfoundry.library.recipe.tinkerstation.ITinkerStationRecipe;
import modernmods.modernfoundry.library.tools.item.IModifiableDisplay;
import modernmods.modernfoundry.library.tools.nbt.LazyToolStack;
import modernmods.modernfoundry.library.tools.nbt.ModDataNBT;
import modernmods.modernfoundry.library.tools.nbt.ToolStack;
import modernmods.modernfoundry.tools.TinkerModifiers;
import modernmods.modernfoundry.tools.modules.cosmetic.BannerModule;

import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Recipe to add a banner to a shield */
public class BannerModifierRecipe implements ITinkerStationRecipe, IMultiRecipe<IDisplayModifierRecipe> {
  private static final RecipeResult<LazyToolStack> NO_PATTERNS = RecipeResult.failure(TConstruct.makeTranslationKey("recipe", "banner.clear.no_patterns"));
  public static final RecordLoadable<BannerModifierRecipe> LOADER = RecordLoadable.create(
    ContextKey.ID.requiredField(),
    IngredientLoadable.ALLOW_EMPTY.defaultField("clear_input", Ingredient.EMPTY, false, r -> r.clearInput),
    BannerModifierRecipe::new);

  @Getter
  private final ResourceLocation id;
  private final Ingredient clearInput;

  public BannerModifierRecipe(ResourceLocation id, Ingredient clearInput) {
    this.id = id;
    this.clearInput = clearInput;
    ModifierRecipeLookup.addRecipeModifier(null, TinkerModifiers.banner);
  }

  @Override
  public boolean matches(ITinkerStationContainer inv, Level world) {
    // ensure this modifier can be applied
    if (!inv.getTinkerableStack().is(TinkerTags.Items.BANNER)) {
      return false;
    }
    boolean found = false;
    boolean clear = false;
    for (int i = 0; i < inv.getInputCount(); i++) {
      ItemStack input = inv.getInput(i);
      if (!input.isEmpty()) {
        if (!clearInput.isEmpty() && clearInput.test(input)) {
          if (clear) {
            return false;
          }
          clear = true;
        } else if (input.getItem() instanceof BannerItem) {
          if (found) {
            return false;
          }
          found = true;
        } else {
          // non-banner input
          return false;
        }
      }
    }
    return found;
  }

  @Override
  public RecipeResult<LazyToolStack> getValidatedResult(ITinkerStationContainer inv, RegistryAccess access) {
    ToolStack tool = inv.getTinkerable().copy();

    ModDataNBT persistentData = tool.getPersistentData();
    ModifierId key = TinkerModifiers.banner.getId();

    // locate the banner
    ItemStack banner = ItemStack.EMPTY;
    DyeColor dye = DyeColor.BLACK;
    boolean clear = false;
    for (int i = 0; i < inv.getInputCount(); i++) {
      ItemStack stack = inv.getInput(i);
      if (stack.isEmpty()) {
        continue;
      }
      if (!clearInput.isEmpty() && clearInput.test(stack)) {
        clear = true;
      } else if (stack.getItem() instanceof BannerItem bannerItem) {
        banner = stack;
        dye = bannerItem.getColor();
      }
    }

    // should never happen
    if (banner.isEmpty()) {
      return RecipeResult.pass();
    }

    ListTag patterns = getBannerPatterns(banner);
    if (clear && patterns.isEmpty()) {
      return NO_PATTERNS;
    }
    BannerModule.copyPatterns(tool.getPersistentData(), key, clear ? null : dye, patterns);

    // add the modifier if missing
    if (tool.getModifierLevel(key) == 0) {
      tool.addModifier(key, 1);
    }
    return ITinkerStationRecipe.success(tool, inv);
  }

  private static ListTag getBannerPatterns(ItemStack banner) {
    BannerPatternLayers layers = banner.get(DataComponents.BANNER_PATTERNS);
    if (layers != null) {
      ListTag patterns = new ListTag();
      for (BannerPatternLayers.Layer layer : layers.layers()) {
        layer.pattern().unwrapKey().ifPresent(patternKey -> {
          CompoundTag tag = new CompoundTag();
          tag.putString("Pattern", patternKey.location().toString());
          tag.putInt("Color", layer.color().getId());
          patterns.add(tag);
        });
      }
      return patterns;
    }
    CustomData blockEntityData = banner.get(DataComponents.BLOCK_ENTITY_DATA);
    return blockEntityData == null ? new ListTag() : blockEntityData.copyTag().getList("Patterns", Tag.TAG_COMPOUND);
  }

  @Override
  public RecipeSerializer<?> getSerializer() {
    return TinkerModifiers.bannerModifierSerializer.get();
  }


  /* JEI */

  @Nullable
  private List<IDisplayModifierRecipe> displayRecipes;

  @Override
  public List<IDisplayModifierRecipe> getRecipes(RegistryAccess access) {
    if (displayRecipes == null) {
      List<ItemStack> toolInputs = RegistryHelper.getTagValueStream(BuiltInRegistries.ITEM, TinkerTags.Items.BANNER)
        .map(item -> {
          ItemStack stack = IModifiableDisplay.getDisplayStack(item);
          if (stack.getMaxStackSize() > 1) {
            stack = stack.copyWithCount(Math.min(stack.getMaxStackSize(), DEFAULT_TOOL_STACK_SIZE));
          }
          return stack;
        }).toList();
      if (!toolInputs.isEmpty()) {
        ResourceLocation id = getId();
        displayRecipes = RegistryHelper.getTagValueStream(BuiltInRegistries.ITEM, ItemTags.BANNERS)
          .flatMap(item -> {
            if (item instanceof BannerItem banner) {
              return Stream.of(new DisplayRecipe(id, toolInputs, banner));
            }
            return Stream.empty();
          }).collect(Collectors.toList());
      } else {
        displayRecipes = List.of();
      }
    }
    return displayRecipes;
  }

  /** Display recipe instance */
  private static class DisplayRecipe implements IDisplayModifierRecipe {
    private static final IntRange LEVELS = new IntRange(1, 1);
    private final ModifierEntry RESULT = new ModifierEntry(TinkerModifiers.banner, 1);

    @Getter
    private final ResourceLocation recipeId;
    private final List<ItemStack> banner;
    @Getter
    private final List<ItemStack> toolWithoutModifier;
    @Getter
    private final List<ItemStack> toolWithModifier;
    @Getter
    private final Component variant;
    public DisplayRecipe(ResourceLocation recipeId, List<ItemStack> tools, BannerItem banner) {
      this.recipeId = recipeId;
      this.toolWithoutModifier = tools;
      this.banner = List.of(new ItemStack(banner));
      DyeColor dye = banner.getColor();
      this.variant = Component.translatable("color.minecraft." + dye.getSerializedName());

      ModifierId key = RESULT.getId();
      ListTag patterns = new ListTag();
      List<ModifierEntry> results = List.of(RESULT);
      toolWithModifier = tools.stream().map(stack -> IDisplayModifierRecipe.withModifiers(stack, DEFAULT_TOOL_STACK_SIZE, results, data -> BannerModule.copyPatterns(data, key, dye, patterns))).toList();
    }

    @Override
    public ModifierEntry getDisplayResult() {
      return RESULT;
    }

    @Override
    public int getInputCount() {
      return 1;
    }

    @Override
    public List<ItemStack> getDisplayItems(int slot) {
      if (slot == 0) {
        return banner;
      }
      return List.of();
    }

    @Override
    public IntRange getLevel() {
      return LEVELS;
    }
  }
}
