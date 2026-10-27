package modernmods.modernfoundry.tools.modifiers.traits.skull;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.bus.api.EventPriority;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import modernmods.modernfoundry.TConstruct;
import modernmods.modernfoundry.library.modifiers.impl.NoLevelsModifier;
import modernmods.modernfoundry.library.module.ModuleHookMap.Builder;
import modernmods.modernfoundry.library.tools.capability.TinkerDataCapability;
import modernmods.modernfoundry.library.tools.capability.TinkerDataCapability.ComputableDataKey;
import modernmods.modernfoundry.library.tools.context.EquipmentChangeContext;
import modernmods.modernfoundry.shared.TinkerAttributes;
import modernmods.modernfoundry.tools.modules.armor.GoldenAttributeModule;

import javax.annotation.Nullable;
import java.util.Optional;

/** @deprecated use {@link GoldenAttributeModule} with {@link TinkerAttributes#CHRYSOPHILITE} */
@Deprecated(forRemoval = true)
public class ChrysophiliteModifier extends NoLevelsModifier {
  /** @deprecated use {@link modernmods.modernfoundry.shared.TinkerAttributes#CHRYSOPHILITE} */
  @Deprecated(forRemoval = true)
  public static final ComputableDataKey<TotalGold> TOTAL_GOLD = TConstruct.createKey("chrysophilite", TotalGold::new);

  @Override
  protected void registerHooks(Builder hookBuilder) {
    hookBuilder.addModule(GoldenAttributeModule.builder(TinkerAttributes.CHRYSOPHILITE, Operation.ADD_VALUE).amount(1, 1));
  }

  /** @deprecated use {@link GoldenAttributeModule#hasGold(EquipmentChangeContext, EquipmentSlot)} */
  @Deprecated(forRemoval = true)
  public static boolean hasGold(EquipmentChangeContext context, EquipmentSlot slotType) {
    return GoldenAttributeModule.hasGold(context, slotType);
  }

  /** @deprecated use {@link modernmods.modernfoundry.shared.TinkerAttributes#CHRYSOPHILITE} */
  @Deprecated(forRemoval = true)
  public static int getTotalGold(@Nullable Entity entity) {
    return Optional.ofNullable(entity)
                   .flatMap(e -> TinkerDataCapability.getCapability(e).resolve())
                   .map(data -> data.get(ChrysophiliteModifier.TOTAL_GOLD))
                   .map(TotalGold::getTotalGold)
                   .orElse(0);
  }


  /** @deprecated use {@link GoldenAttributeModule.TotalGold} */
  @Deprecated(forRemoval = true)
  public static class TotalGold extends GoldenAttributeModule.TotalGold {}
}
