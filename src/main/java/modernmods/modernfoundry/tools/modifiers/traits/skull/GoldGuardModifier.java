package modernmods.modernfoundry.tools.modifiers.traits.skull;

import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import modernmods.modernfoundry.library.modifiers.impl.NoLevelsModifier;
import modernmods.modernfoundry.library.module.ModuleHookMap.Builder;
import modernmods.modernfoundry.tools.modules.armor.GoldenAttributeModule;

/** @deprecated use {@link GoldenAttributeModule} with {@link Attributes#MAX_HEALTH} */
@Deprecated(forRemoval = true)
public class GoldGuardModifier extends NoLevelsModifier {
  @Override
  protected void registerHooks(Builder hookBuilder) {
    hookBuilder.addModule(GoldenAttributeModule.builder(Attributes.MAX_HEALTH, Operation.ADD_VALUE).amount(4, 4));
  }
}
