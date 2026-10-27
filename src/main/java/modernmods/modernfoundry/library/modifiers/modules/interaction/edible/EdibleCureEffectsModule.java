package modernmods.modernfoundry.library.modifiers.modules.interaction.edible;

import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.EffectCures;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import modernmods.hilt.data.loadable.common.ItemStackLoadable;
import modernmods.hilt.data.loadable.record.RecordLoadable;
import modernmods.hilt.data.predicate.IJsonPredicate;
import modernmods.hilt.data.predicate.entity.LivingEntityPredicate;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.interaction.EdibleEffectHook;
import modernmods.modernfoundry.library.modifiers.modules.ModifierModule;
import modernmods.modernfoundry.library.modifiers.modules.util.ModifierCondition;
import modernmods.modernfoundry.library.modifiers.modules.util.ModifierCondition.ConditionalModule;
import modernmods.modernfoundry.library.module.HookProvider;
import modernmods.modernfoundry.library.module.ModuleHook;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;

import java.util.List;

/** Module for making eating a tool remove all effects using an item. */
public record EdibleCureEffectsModule(ItemStack curativeItem, IJsonPredicate<LivingEntity> holder, ModifierCondition<IToolStackView> condition) implements ModifierModule, EdibleEffectHook, ConditionalModule<IToolStackView> {
  private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<EdibleCureEffectsModule>defaultHooks(ModifierHooks.EDIBLE_EFFECT);
  public static final RecordLoadable<EdibleCureEffectsModule> LOADER = RecordLoadable.create(
    ItemStackLoadable.REQUIRED_ITEM_NBT.requiredField("curative_item", EdibleCureEffectsModule::curativeItem),
    LivingEntityPredicate.LOADER.defaultField("holder", EdibleCureEffectsModule::holder),
    ModifierCondition.TOOL_FIELD, EdibleCureEffectsModule::new);

  public EdibleCureEffectsModule(ItemLike item) {
    this(new ItemStack(item), LivingEntityPredicate.ANY, ModifierCondition.ANY_TOOL);
  }

  @Override
  public RecordLoadable<EdibleCureEffectsModule> getLoader() {
    return LOADER;
  }

  @Override
  public List<ModuleHook<?>> getDefaultHooks() {
    return DEFAULT_HOOKS;
  }

  @Override
  public void onToolEaten(IToolStackView tool, ModifierEntry modifier, Player player, EquipmentSlot eatenSlot, int hunger, float saturation, List<ItemStack> representativeItems) {
    if (condition.matches(tool, modifier) && holder.matches(player)) {
      player.removeEffectsCuredBy(curativeItem.is(Items.HONEY_BOTTLE) ? EffectCures.HONEY : EffectCures.MILK);
    }
  }
}
