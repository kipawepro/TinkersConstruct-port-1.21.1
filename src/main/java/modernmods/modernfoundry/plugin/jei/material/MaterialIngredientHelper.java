package modernmods.modernfoundry.plugin.jei.material;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import modernmods.modernfoundry.common.TinkerTags;
import modernmods.modernfoundry.library.client.materials.MaterialTooltipCache;
import modernmods.modernfoundry.library.materials.MaterialRegistry;
import modernmods.modernfoundry.library.materials.definition.IMaterial;
import modernmods.modernfoundry.library.materials.definition.MaterialVariant;
import modernmods.modernfoundry.library.materials.definition.MaterialVariantId;
import modernmods.modernfoundry.library.recipe.material.MaterialRecipe;
import modernmods.modernfoundry.library.recipe.material.MaterialRecipeCache;
import modernmods.modernfoundry.library.tools.part.IMaterialItem;
import modernmods.modernfoundry.plugin.jei.TConstructJEIConstants;
import modernmods.modernfoundry.tools.TinkerToolParts;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Stream;

/** Logic for working with materials as ingredients */
public class MaterialIngredientHelper implements IIngredientHelper<MaterialVariant> {
  private static final Comparator<MaterialRecipe> COMPARATOR = Comparator.comparing(MaterialRecipe::getValue).thenComparing(MaterialRecipe::getNeeded);

  @Override
  public IIngredientType<MaterialVariant> getIngredientType() {
    return TConstructJEIConstants.MATERIAL_TYPE;
  }

  @Override
  public String getDisplayName(MaterialVariant material) {
    return MaterialTooltipCache.getDisplayName(material.getVariant()).getString();
  }

  @Override
  public MaterialVariant copyIngredient(MaterialVariant material) {
    return material;
  }

  @Override
  public Iterable<Integer> getColors(MaterialVariant material) {
    return List.of(0xFF000000 | MaterialTooltipCache.getColor(material.getVariant()).getValue());
  }

  @Override
  public ItemStack getCheatItemStack(MaterialVariant material) {
    MaterialVariantId variant = material.getVariant();
    MaterialRecipe recipe = MaterialRecipeCache.getRecipes(variant).stream().min(COMPARATOR).orElse(null);
    if (recipe != null) {
      ItemStack[] items = recipe.getIngredient().getItems();
      if (items.length > 0) {
        return items[0].copy();
      }
    }
    // nothing? try repair kit
    IMaterialItem repairKit = TinkerToolParts.repairKit.get();
    if (repairKit.canUseMaterial(material.getId())) {
      return repairKit.withMaterialForDisplay(variant);
    }
    // no repair kit? give up
    return ItemStack.EMPTY;
  }


  /* IDs */

  @Override
  public String getUniqueId(MaterialVariant material, UidContext context) {
    if (context == UidContext.Recipe) {
      return material.getId().toString();
    }
    return material.getVariant().toString();
  }

  @Override
  public Object getUid(MaterialVariant material, UidContext context) {
    if (context == UidContext.Recipe) {
      return material.getId();
    }
    return material.getVariant();
  }

  @Override
  public String getWildcardId(MaterialVariant material) {
    return material.getId().toString();
  }

  @Override
  public ResourceLocation getResourceLocation(MaterialVariant material) {
    return material.getId();
  }

  @Override
  public String getErrorInfo(@Nullable MaterialVariant material) {
    return material == null ? "null" : material.getVariant().toString();
  }


  /* Tags */

  @Override
  public Stream<ResourceLocation> getTagStream(MaterialVariant material) {
    return MaterialRegistry.getInstance().getTags(material.getId()).map(TagKey::location);
  }

  @Override
  public boolean isHiddenFromRecipeViewersByTags(MaterialVariant material) {
    return MaterialRegistry.getInstance().isInTag(material.getId(), TinkerTags.Materials.HIDDEN_FROM_RECIPE_VIEWERS);
  }

  @Override
  public Optional<TagKey<?>> getTagKeyEquivalent(Collection<MaterialVariant> entries) {
    List<IMaterial> values = entries.stream().map(MaterialVariant::get).toList();
    return MaterialRegistry.getInstance().getAllTags()
      .filter(entry -> entry.getValue().equals(values))
      .<TagKey<?>>map(Entry::getKey).findFirst();
  }
}
