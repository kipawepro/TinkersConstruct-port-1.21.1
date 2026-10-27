package modernmods.modernfoundry.library.tools.part;

import net.minecraft.world.item.ItemStack;
import modernmods.modernfoundry.common.recipe.RecipeCacheInvalidator;
import modernmods.modernfoundry.library.materials.MaterialRegistry;
import modernmods.modernfoundry.library.utils.SimpleCache;

import java.util.List;

/** Cache for lookups related to material items. */
public class MaterialItemCache {
  /** Cache of lists of all material variants for a given tool part */
  private static final SimpleCache<IMaterialItem, List<ItemStack>> ALL_MATERIALS_CACHE = new SimpleCache<>(item ->
    MaterialRegistry.getMaterials().stream().filter(item::canUseMaterial).map(material -> item.withMaterialForDisplay(material.getIdentifier())).toList());

  static {
    RecipeCacheInvalidator.addReloadListener(client -> ALL_MATERIALS_CACHE.clear());
  }

  /** Gets all materials for the given item stack. */
  public static List<ItemStack> getAllMaterials(IMaterialItem item) {
    return ALL_MATERIALS_CACHE.apply(item);
  }
}
