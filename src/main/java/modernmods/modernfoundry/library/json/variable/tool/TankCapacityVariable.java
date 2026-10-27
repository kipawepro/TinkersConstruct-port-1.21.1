package modernmods.modernfoundry.library.json.variable.tool;

import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import modernmods.hilt.data.loadable.record.RecordLoadable;
import modernmods.hilt.data.predicate.IJsonPredicate;
import modernmods.hilt.data.predicate.fluid.FluidPredicate;
import modernmods.modernfoundry.library.tools.capability.fluid.ToolTankHelper;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;

/**
 * Variable getting the current fluid capacity on the tool using the given tank helper.
 * @param helper  Tank helper to locate the fluid
 * @param fluid   Fluid filter. If the current fluid does not match the filter and is not empty, an amount of 0 is returned.
 */
public record TankCapacityVariable(ToolTankHelper helper, IJsonPredicate<Fluid> fluid) implements ToolVariable {
  public static final RecordLoadable<TankCapacityVariable> LOADER = RecordLoadable.create(
    ToolTankHelper.LOADABLE.defaultField("tank_helper", ToolTankHelper.TANK_HELPER, false, TankCapacityVariable::helper),
    FluidPredicate.LOADER.defaultField("fluid", TankCapacityVariable::fluid),
    TankCapacityVariable::new);

  public TankCapacityVariable() {
    this(ToolTankHelper.TANK_HELPER, FluidPredicate.ANY);
  }

  public TankCapacityVariable(ToolTankHelper helper) {
    this(helper, FluidPredicate.ANY);
  }

  public TankCapacityVariable(IJsonPredicate<Fluid> fluid) {
    this(ToolTankHelper.TANK_HELPER, fluid);
  }

  @Override
  public float getValue(IToolStackView tool) {
    FluidStack fluid = helper.getFluid(tool);
    return fluid.isEmpty() || this.fluid.matches(fluid.getFluid()) ? helper.getCapacity(tool) : 0;
  }

  @Override
  public RecordLoadable<TankCapacityVariable> getLoader() {
    return LOADER;
  }
}
