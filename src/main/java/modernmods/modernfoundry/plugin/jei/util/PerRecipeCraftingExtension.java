package modernmods.modernfoundry.plugin.jei.util;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.ICraftingGridHelper;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.ICraftingCategoryExtension;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class PerRecipeCraftingExtension<R extends CraftingRecipe, E extends PerRecipeCraftingExtension.Instance> implements ICraftingCategoryExtension<R> {
  private final Function<R, E> factory;
  private final Map<R, E> cache = new IdentityHashMap<>();

  public PerRecipeCraftingExtension(Function<R, E> factory) {
    this.factory = factory;
  }

  private E get(RecipeHolder<R> holder) {
    return cache.computeIfAbsent(holder.value(), factory);
  }

  @Override
  public void setRecipe(RecipeHolder<R> holder, IRecipeLayoutBuilder builder, ICraftingGridHelper craftingGridHelper, IFocusGroup focuses) {
    get(holder).setRecipe(builder, craftingGridHelper, focuses);
  }

  @Override
  public void createRecipeExtras(RecipeHolder<R> holder, IRecipeExtrasBuilder builder, ICraftingGridHelper craftingGridHelper, IFocusGroup focuses) {
    get(holder).createRecipeExtras(builder, craftingGridHelper, focuses);
  }

  @Override
  public void onDisplayedIngredientsUpdate(RecipeHolder<R> holder, List<IRecipeSlotDrawable> recipeSlots, IFocusGroup focuses) {
    get(holder).onDisplayedIngredientsUpdate(recipeSlots, focuses);
  }

  @Override
  public Optional<ResourceLocation> getRegistryName(RecipeHolder<R> holder) {
    return Optional.of(holder.id());
  }

  @Override
  public int getWidth(RecipeHolder<R> holder) {
    return get(holder).getWidth();
  }

  @Override
  public int getHeight(RecipeHolder<R> holder) {
    return get(holder).getHeight();
  }

  public interface Instance {
    void setRecipe(IRecipeLayoutBuilder builder, ICraftingGridHelper craftingGridHelper, IFocusGroup focuses);

    default void createRecipeExtras(IRecipeExtrasBuilder builder, ICraftingGridHelper craftingGridHelper, IFocusGroup focuses) {}

    default void onDisplayedIngredientsUpdate(List<IRecipeSlotDrawable> recipeSlots, IFocusGroup focuses) {}

    default int getWidth() {
      return 0;
    }

    default int getHeight() {
      return 0;
    }
  }
}
