package modernmods.modernfoundry.library.tools.stat.impl;

import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import modernmods.hilt.data.predicate.IJsonPredicate;
import modernmods.modernfoundry.library.tools.stat.FloatToolStat;
import modernmods.modernfoundry.library.tools.stat.IToolStat;
import modernmods.modernfoundry.library.tools.stat.ToolStatId;

import javax.annotation.Nullable;

/** Extension of {@link FloatToolStat} that formats the value as a percentage. Expected range of tool stat is between 0 and 1 for 0% to 100%. */
public class PercentToolStat extends FloatToolStat {
  public PercentToolStat(ToolStatId name, int color, float defaultValue, float minValue, float maxValue, IJsonPredicate<Item> items) {
    super(name, color, defaultValue, minValue, maxValue, items);
  }

  public PercentToolStat(ToolStatId name, int color, float defaultValue, float minValue, float maxValue, @Nullable TagKey<Item> tag) {
    super(name, color, defaultValue, minValue, maxValue, tag);
  }

  public PercentToolStat(ToolStatId name, int color, float defaultValue, float minValue, float maxValue) {
    super(name, color, defaultValue, minValue, maxValue);
  }

  @Override
  public Component formatValue(float value) {
    return IToolStat.formatNumberPercent(getTranslationKey(), getColor(), value * 100);
  }
}
