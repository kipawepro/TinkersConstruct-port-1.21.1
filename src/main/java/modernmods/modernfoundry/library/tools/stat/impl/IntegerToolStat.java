package modernmods.modernfoundry.library.tools.stat.impl;

import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import modernmods.hilt.data.predicate.IJsonPredicate;
import modernmods.modernfoundry.library.tools.stat.FloatToolStat;
import modernmods.modernfoundry.library.tools.stat.IToolStat;
import modernmods.modernfoundry.library.tools.stat.ToolStatId;

import javax.annotation.Nullable;

/**
 * Same as {@link FloatToolStat} but displays stat values as integers. Used for stats that are always read as ints.
 * @see modernmods.modernfoundry.library.tools.nbt.StatsNBT#getInt(IToolStat)
 */
public class IntegerToolStat extends FloatToolStat {
  public IntegerToolStat(ToolStatId name, int color, float defaultValue, float minValue, float maxValue, IJsonPredicate<Item> items) {
    super(name, color, defaultValue, minValue, maxValue, items);
  }

  public IntegerToolStat(ToolStatId name, int color, float defaultValue, float minValue, float maxValue, @Nullable TagKey<Item> tag) {
    super(name, color, defaultValue, minValue, maxValue, tag);
  }

  public IntegerToolStat(ToolStatId name, int color, float defaultValue, float minValue, float maxValue) {
    super(name, color, defaultValue, minValue, maxValue);
  }

  /** Formats the integer value */
  public Component formatValue(int value) {
    return IToolStat.formatNumber(getTranslationKey(), getColor(), value);
  }

  @Override
  public Component formatValue(Float value) {
    return formatValue(value.intValue());
  }

  @Override
  public Component formatValue(float value) {
    return formatValue((int) value);
  }
}
