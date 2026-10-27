package modernmods.modernfoundry.library.modifiers.modules.interaction.edible;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import modernmods.hilt.data.loadable.Loadables;
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

/** Module for making eating a tool remove a specific effect. */
public record EdibleRemoveEffectModule(MobEffect effect, IJsonPredicate<LivingEntity> holder, ModifierCondition<IToolStackView> condition) implements ModifierModule, EdibleEffectHook, ConditionalModule<IToolStackView> {
  private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<EdibleRemoveEffectModule>defaultHooks(ModifierHooks.EDIBLE_EFFECT);
  public static final RecordLoadable<EdibleRemoveEffectModule> LOADER = RecordLoadable.create(
    Loadables.MOB_EFFECT.requiredField("effect", EdibleRemoveEffectModule::effect),
    LivingEntityPredicate.LOADER.defaultField("holder", EdibleRemoveEffectModule::holder),
    ModifierCondition.TOOL_FIELD, EdibleRemoveEffectModule::new);

  public EdibleRemoveEffectModule(MobEffect effect) {
    this(effect, LivingEntityPredicate.ANY, ModifierCondition.ANY_TOOL);
  }

  @Override
  public RecordLoadable<EdibleRemoveEffectModule> getLoader() {
    return LOADER;
  }

  @Override
  public List<ModuleHook<?>> getDefaultHooks() {
    return DEFAULT_HOOKS;
  }

  @Override
  public void onToolEaten(IToolStackView tool, ModifierEntry modifier, Player player, EquipmentSlot eatenSlot, int hunger, float saturation, List<ItemStack> representativeItems) {
    if (condition.matches(tool, modifier) && holder.matches(player)) {
      player.removeEffect(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(effect));
    }
  }
}
