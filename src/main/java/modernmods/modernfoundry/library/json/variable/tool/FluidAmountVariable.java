package modernmods.modernfoundry.library.json.variable.tool;

import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import modernmods.hilt.data.loadable.record.RecordLoadable;
import modernmods.hilt.data.predicate.IJsonPredicate;
import modernmods.hilt.data.predicate.fluid.FluidPredicate;
import modernmods.modernfoundry.library.tools.capability.fluid.ToolTankHelper;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;

/**
 * Variable getting the current fluid amount on the tool using the given tank helper.
 * @param helper  Tank helper to locate the fluid
 * @param fluid   Fluid filter. If the current fluid does not match the filter, an amount of 0 is returned.
 */
public record FluidAmountVariable(ToolTankHelper helper, IJsonPredicate<Fluid> fluid) implements ToolVariable {
  public static final RecordLoadable<FluidAmountVariable> LOADER = RecordLoadable.create(
    ToolTankHelper.LOADABLE.defaultField("tank_helper", ToolTankHelper.TANK_HELPER, false, FluidAmountVariable::helper),
    FluidPredicate.LOADER.defaultField("fluid", FluidAmountVariable::fluid),
    FluidAmountVariable::new);

  public FluidAmountVariable(IJsonPredicate<Fluid> fluid) {
    this(ToolTankHelper.TANK_HELPER, fluid);
  }

  @Override
  public float getValue(IToolStackView tool) {
    FluidStack fluid = helper.getFluid(tool);
    return !fluid.isEmpty() && this.fluid.matches(fluid.getFluid()) ? fluid.getAmount() : 0;
  }

  @Override
  public RecordLoadable<FluidAmountVariable> getLoader() {
    return LOADER;
  }
}
