package modernmods.modernfoundry.library.modifiers.modules.interaction.edible;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import modernmods.hilt.data.loadable.record.RecordLoadable;
import modernmods.hilt.data.predicate.IJsonPredicate;
import modernmods.hilt.data.predicate.entity.LivingEntityPredicate;
import modernmods.modernfoundry.library.json.LevelingInt;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.interaction.EdibleEffectHook;
import modernmods.modernfoundry.library.modifiers.modules.ModifierModule;
import modernmods.modernfoundry.library.modifiers.modules.util.ModifierCondition;
import modernmods.modernfoundry.library.modifiers.modules.util.ModifierCondition.ConditionalModule;
import modernmods.modernfoundry.library.module.HookProvider;
import modernmods.modernfoundry.library.module.ModuleHook;
import modernmods.modernfoundry.library.tools.helper.ToolDamageUtil;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;

import java.util.List;

/** Module for making eating a tool consume durability. */
public record EdibleConsumeDurabilityModule(LevelingInt durabilityUsage, IJsonPredicate<LivingEntity> holder, ModifierCondition<IToolStackView> condition) implements ModifierModule, EdibleEffectHook, ConditionalModule<IToolStackView> {
  private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<EdibleConsumeDurabilityModule>defaultHooks(ModifierHooks.EDIBLE_EFFECT);
  public static final RecordLoadable<EdibleConsumeDurabilityModule> LOADER = RecordLoadable.create(
    LevelingInt.LOADABLE.requiredField("durability_usage", EdibleConsumeDurabilityModule::durabilityUsage),
    LivingEntityPredicate.LOADER.defaultField("holder", EdibleConsumeDurabilityModule::holder),
    ModifierCondition.TOOL_FIELD, EdibleConsumeDurabilityModule::new);

  public EdibleConsumeDurabilityModule(LevelingInt durabilityUsage) {
    this(durabilityUsage, LivingEntityPredicate.ANY, ModifierCondition.ANY_TOOL);
  }

  @Override
  public RecordLoadable<EdibleConsumeDurabilityModule> getLoader() {
    return LOADER;
  }

  @Override
  public List<ModuleHook<?>> getDefaultHooks() {
    return DEFAULT_HOOKS;
  }

  @Override
  public void onToolEaten(IToolStackView tool, ModifierEntry modifier, Player player, EquipmentSlot eatenSlot, int hunger, float saturation, List<ItemStack> representativeItems) {
    if (condition.matches(tool, modifier) && holder.matches(player)) {
      int damage = this.durabilityUsage.compute(modifier.getEffectiveLevel());
      ItemStack eaten = player.getItemBySlot(eatenSlot);
      if (damage > 0 && ToolDamageUtil.directDamage(tool, damage, player, eaten)) {
        player.onEquippedItemBroken(eaten.getItem(), eatenSlot);
      }
    }
  }
}
