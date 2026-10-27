package modernmods.modernfoundry.library.json.predicate.tool;

import net.neoforged.neoforge.common.ItemAbility;
import modernmods.hilt.data.loadable.Loadables;
import modernmods.hilt.data.loadable.record.RecordLoadable;
import modernmods.modernfoundry.library.tools.helper.ModifierUtil;
import modernmods.modernfoundry.library.tools.nbt.IToolStackView;

/** Predicate matching tools exposing a given tool action */
public record ToolActionPredicate(ItemAbility action) implements ToolStackPredicate {
  public static final RecordLoadable<ToolActionPredicate> LOADER = RecordLoadable.create(Loadables.TOOL_ACTION.requiredField("action", ToolActionPredicate::action), ToolActionPredicate::new);

  @Override
  public boolean matches(IToolStackView tool) {
    return ModifierUtil.canPerformAction(tool, action);
  }

  @Override
  public RecordLoadable<ToolActionPredicate> getLoader() {
    return LOADER;
  }
}
