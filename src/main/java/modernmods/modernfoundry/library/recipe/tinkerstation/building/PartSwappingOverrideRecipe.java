package modernmods.modernfoundry.library.recipe.tinkerstation.building;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import modernmods.hilt.data.loadable.field.ContextKey;
import modernmods.hilt.data.loadable.record.RecordLoadable;
import modernmods.hilt.recipe.IMultiRecipe;
import modernmods.hilt.recipe.ingredient.SizedIngredient;
import modernmods.modernfoundry.library.json.TinkerLoadables;
import modernmods.modernfoundry.library.materials.IMaterialRegistry;
import modernmods.modernfoundry.library.materials.MaterialRegistry;
import modernmods.modernfoundry.library.materials.definition.IMaterial;
import modernmods.modernfoundry.library.materials.definition.MaterialVariant;
import modernmods.modernfoundry.library.materials.definition.MaterialVariantId;
import modernmods.modernfoundry.library.materials.stats.MaterialStatsId;
import modernmods.modernfoundry.library.recipe.casting.material.MaterialCastingLookup;
import modernmods.modernfoundry.library.recipe.tinkerstation.IDisplayToolTinkering;
import modernmods.modernfoundry.library.tools.definition.module.material.ToolMaterialHook;
import modernmods.modernfoundry.library.tools.helper.ToolBuildHandler;
import modernmods.modernfoundry.library.tools.nbt.ToolStack;
import modernmods.modernfoundry.library.tools.part.IToolPart;
import modernmods.modernfoundry.tables.TinkerTables;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/** Recipe for swapping a single material on a tool given a specific tool part. Notably allows swapping a part into a tool on an index other than the first. */
public class PartSwappingOverrideRecipe extends MaterialIndexSwappingRecipe implements IMultiRecipe<IDisplayToolTinkering> {
  public static final RecordLoadable<PartSwappingOverrideRecipe> LOADER = RecordLoadable.create(
    ContextKey.ID.requiredField(), TOOLS_FIELD, STACK_SIZE_FIELD,
    TinkerLoadables.TOOL_PART_ITEM.requiredField("part", r -> r.part),
    INDICES_FIELD, EXTRA_REQUIREMENTS_FIELD,
    PartSwappingOverrideRecipe::new);

  /** Part to match allowing the swap */
  private final IToolPart part;

  protected PartSwappingOverrideRecipe(ResourceLocation id, Ingredient tools, int maxStackSize, IToolPart part, int[] indices, List<SizedIngredient> extraRequirements) {
    super(id, tools, maxStackSize, indices, extraRequirements);
    this.part = part;
  }

  @Override
  protected boolean isMaterial(ItemStack stack) {
    return stack.getItem() == part;
  }

  @Override
  protected MaterialVariantId getMaterial(ItemStack stack) {
    return part.getMaterial(stack);
  }

  @Override
  protected int getRepairValue() {
    return MaterialCastingLookup.getItemCost(part);
  }

  @Override
  public RecipeSerializer<?> getSerializer() {
    return TinkerTables.fixedMaterialSwapping.get();
  }


  /* JEI */
  private List<IDisplayToolTinkering> multiRecipes;

  @Override
  public List<IDisplayToolTinkering> getRecipes(RegistryAccess access) {
    if (multiRecipes == null) {
      IMaterialRegistry registry = MaterialRegistry.getInstance();
      MaterialStatsId statType = part.getStatType();
      // since we know the part ahead of time, only need to filter materials once
      List<IMaterial> materials = registry.getVisibleMaterials().stream().filter(mat -> registry.getMaterialStats(mat.getIdentifier(), statType).isPresent()).toList();
      // create a recipe per tool, then per index, matching standard part swapping
      multiRecipes = Arrays.stream(this.tools.getItems()).flatMap(stack -> {
        ToolStack tool = ToolStack.from(stack);
        // JEI only shows up to 5 slots
        if (ToolMaterialHook.stats(tool.getDefinition()).size() > MAX_SLOTS) {
          return Stream.empty();
        }
        List<MaterialVariant> variants = materials.stream().map(MaterialVariant::of).toList();
        return Arrays.stream(indices).filter(VALID_SLOT).<IDisplayToolTinkering>mapToObj(i -> {
          ToolStack copy = tool.copy();
          return new PartDisplayRecipe(i,
            // one part per material
            materials.stream().map(mat -> part.withMaterialForDisplay(mat.getIdentifier())).toList(),
            // single tool with the material to swap left blank
            List.of(withMaterial(copy, i, MaterialVariant.of(ToolBuildHandler.getRenderMaterial(0))).copy()),
            // one output per material
            materials.stream().map(mat -> withMaterial(copy, i, MaterialVariant.of(mat)).copy()).toList(),
            // part info
            variants, part
          );
        });
      }).toList();
    }
    return multiRecipes;
  }
}
