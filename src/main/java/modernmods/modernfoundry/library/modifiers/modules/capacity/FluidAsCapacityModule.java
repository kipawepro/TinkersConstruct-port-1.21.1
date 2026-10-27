package modernmods.modernfoundry.library.modifiers.modules.capacity;

import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import modernmods.hilt.data.loadable.Loadables;
import modernmods.hilt.data.loadable.record.RecordLoadable;
import modernmods.modernfoundry.library.modifiers.ModifierEntry;
import modernmods.modernfoundry.library.modifiers.ModifierHooks;
import modernmods.modernfoundry.library.modifiers.hook.special.CapacityBarHook;
import modernmods.modernfoundry.library.modifiers.modules.ModifierModule;
import modernmods.modernfoundry.library.module.HookProvider;
import modernmods.modernfoundry.library.module.ModuleHook;
import modernmods.modernfoundry.library.tools.capability.fluid.ToolTankHelper;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;

import java.util.List;

/**
 * Module connecting a specific fluid to {@link CapacityBarHook}. Meant to be used on the specific modifier rather than an internal modifier.
 * @param helper  Tank helper to locate the fluid
 * @param fluid   Fluid to match. If the fluid does not match it will be treated as 0.
 */
public record FluidAsCapacityModule(ToolTankHelper helper, Fluid fluid) implements ModifierModule, CapacityBarHook {
  private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<FluidAsCapacityModule>defaultHooks(ModifierHooks.CAPACITY_BAR);
  public static final RecordLoadable<FluidAsCapacityModule> LOADER = RecordLoadable.create(
    ToolTankHelper.LOADABLE.defaultField("tank_helper", ToolTankHelper.TANK_HELPER, FluidAsCapacityModule::helper),
    Loadables.FLUID.requiredField("fluid", FluidAsCapacityModule::fluid),
    FluidAsCapacityModule::new);

  public FluidAsCapacityModule(Fluid fluid) {
    this(ToolTankHelper.TANK_HELPER, fluid);
  }

  @Override
  public RecordLoadable<FluidAsCapacityModule> getLoader() {
    return LOADER;
  }

  @Override
  public List<ModuleHook<?>> getDefaultHooks() {
    return DEFAULT_HOOKS;
  }

  @Override
  public int getAmount(IToolStackView tool) {
    FluidStack fluid = helper.getFluid(tool);
    return !fluid.isEmpty() && fluid.getFluid() == this.fluid ? fluid.getAmount() : 0;
  }

  @Override
  public int getCapacity(IToolStackView tool, ModifierEntry entry) {
    // if another fluid is present, act like the tank is full
    FluidStack fluid = helper.getFluid(tool);
    return fluid.isEmpty() || fluid.getFluid() == this.fluid ? helper.getCapacity(tool) : 0;
  }

  @Override
  public void setAmount(IToolStackView tool, ModifierEntry entry, int amount) {
    // don't allow setting amount if another fluid is present
    FluidStack fluid = helper.getFluid(tool);
    if (fluid.isEmpty() || fluid.getFluid() == this.fluid) {
      helper.setFluid(tool, amount == 0 ? FluidStack.EMPTY : new FluidStack(this.fluid, amount));
    }
  }

  @Override
  public void addAmount(IToolStackView tool, ModifierEntry modifier, int amount) {
    if (amount == 0) return;
    // don't allow setting amount if another fluid is present
    FluidStack fluid = helper.getFluid(tool);
    if (fluid.isEmpty() || fluid.getFluid() == this.fluid) {
      helper.setFluid(tool, new FluidStack(this.fluid, fluid.getAmount() + amount));
    }
  }

  @Override
  public void removeAmount(IToolStackView tool, ModifierEntry modifier, int amount) {
    if (amount == 0) return;
    // don't allow setting amount if another fluid is present
    FluidStack fluid = helper.getFluid(tool);
    if (fluid.isEmpty() || fluid.getFluid() == this.fluid) {
      int newAmount = fluid.getAmount() - amount;
      helper.setFluid(tool, newAmount == 0 ? FluidStack.EMPTY : new FluidStack(this.fluid, newAmount));
    }
  }
}
