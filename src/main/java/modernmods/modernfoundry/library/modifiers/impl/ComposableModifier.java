package modernmods.modernfoundry.library.modifiers.impl;

import com.google.common.collect.ImmutableList;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.resources.ResourceLocation;
import modernmods.hilt.data.loadable.ErrorFactory;
import modernmods.hilt.data.loadable.primitive.IntLoadable;
import modernmods.hilt.data.loadable.primitive.StringLoadable;
import modernmods.hilt.data.loadable.record.RecordLoadable;
import modernmods.modernfoundry.TConstruct;
import modernmods.modernfoundry.library.modifiers.Modifier;
import modernmods.modernfoundry.library.modifiers.modules.ModifierModule;
import modernmods.modernfoundry.library.modifiers.util.ModifierLevelDisplay;
import modernmods.modernfoundry.library.modifiers.util.ModifierTooltip;
import modernmods.modernfoundry.library.modifiers.util.ModifierTooltip.ShowInTooltips;
import modernmods.modernfoundry.library.modifiers.util.ModifierTooltipsField;
import modernmods.modernfoundry.library.module.ModuleHook;
import modernmods.modernfoundry.library.module.ModuleHookMap;
import modernmods.modernfoundry.library.module.WithHooks;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/** Modifier consisting of many composed hooks, used in datagen as a serialized modifier. */
public class ComposableModifier extends BasicModifier {
  public static final RecordLoadable<ComposableModifier> LOADER = RecordLoadable.create(
    ModifierLevelDisplay.LOADER.defaultField("level_display", true, m -> m.levelDisplay),
    new ModifierTooltipsField<>("show_in_tooltips", "tooltip_display", m -> m.showInTooltips),
    IntLoadable.ANY_FULL.defaultField("priority", Integer.MIN_VALUE, m -> m.priority),
    ModifierModule.WITH_HOOKS.list(0).defaultField("modules", List.of(), m -> m.modules),
    StringLoadable.DEFAULT.nullableField("translation_key", m -> m.translationKey),
    ErrorFactory.FIELD,
    (level, tooltip, priority, modules, descriptionKey, error) -> new ComposableModifier(level, tooltip, priority == Integer.MIN_VALUE ? computePriority(modules) : priority, modules, descriptionKey, error));

  private final List<WithHooks<ModifierModule>> modules;

  /**
   * Creates a new instance
   * @param levelDisplay     Level display
   * @param showInTooltips   Tooltip display
   * @param priority         If the value is {@link Integer#MIN_VALUE}, assumed unset for datagen
   * @param modules          Modules for this modifier
   * @param translationKey   Translation key override. If empty, generates key from the modifier ID.
   */
  protected ComposableModifier(ModifierLevelDisplay levelDisplay, ShowInTooltips showInTooltips, int priority, List<WithHooks<ModifierModule>> modules, @Nullable String translationKey, ErrorFactory error) {
    super(ModuleHookMap.createMap(modules, error), levelDisplay, showInTooltips, priority);
    this.modules = modules;
    if (translationKey != null) {
      this.translationKey = translationKey;
    }
  }

  /** @deprecated use {@link #ComposableModifier(ModifierLevelDisplay, ShowInTooltips, int, List, String, ErrorFactory)} */
  @SuppressWarnings("removal")
  @Deprecated(forRemoval = true)
  protected ComposableModifier(ModifierLevelDisplay levelDisplay, TooltipDisplay tooltipDisplay, int priority, List<WithHooks<ModifierModule>> modules, @Nullable String translationKey, ErrorFactory error) {
    super(ModuleHookMap.createMap(modules, error), levelDisplay, tooltipDisplay, priority);
    this.modules = modules;
    if (translationKey != null) {
      this.translationKey = translationKey;
    }
  }

  /** @deprecated use {@link #ComposableModifier(ModifierLevelDisplay, ShowInTooltips, int, List, String, ErrorFactory)} */
  @SuppressWarnings("removal")
  @Deprecated(forRemoval = true)
  protected ComposableModifier(ModifierLevelDisplay levelDisplay, TooltipDisplay tooltipDisplay, int priority, List<WithHooks<ModifierModule>> modules, ErrorFactory error) {
    this(levelDisplay, tooltipDisplay, priority, modules, "", error);
  }

  /** Creates a builder instance for datagen */
  public static Builder builder() {
    return new Builder();
  }

  /** Computes the recommended priority for a set of modifier modules */
  private static int computePriority(List<WithHooks<ModifierModule>> modules) {
    // poll all modules to find who has a priority preference
    List<ModifierModule> priorityModules = new ArrayList<>();
    for (WithHooks<ModifierModule> module : modules) {
      if (module.module().getPriority() != null) {
        priorityModules.add(module.module());
      }
    }
    if (!priorityModules.isEmpty()) {
      //noinspection ConstantConditions  validated nonnull above
      int firstPriority = priorityModules.get(0).getPriority();

      // check if any module disagrees with the first priority, if so we need a warning (but not more than one warning)
      for (int i = 1; i < priorityModules.size(); i++) {
        //noinspection ConstantConditions  validated nonnull above
        if (priorityModules.get(i).getPriority() != firstPriority) {
          TConstruct.LOG.warn("Multiple modules disagree on the preferred priority for composable modifier, choosing priority {}. Set the priority manually to silence this warning. All opinions: \n{}", firstPriority,
                              priorityModules.stream()
                                             .map(module -> "* " + module + ": " + module.getPriority())
                                             .collect(Collectors.joining("\n")));
          break;
        }
      }
      return firstPriority;
    }
    return Modifier.DEFAULT_PRIORITY;
  }

  /** Builder for a composable modifier instance */
  @SuppressWarnings("UnusedReturnValue")  // it's a builder
  @Setter
  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  @Accessors(fluent = true)
  public static class Builder {
    private final ImmutableList.Builder<WithHooks<ModifierModule>> modules = ImmutableList.builder();
    private ModifierLevelDisplay levelDisplay = ModifierLevelDisplay.DEFAULT;
    /** Whether to show this modifier in tooltips */
    private ShowInTooltips showInTooltips = ShowInTooltips.ALWAYS;
    /** {@link Integer#MIN_VALUE} is an internal value used to represent unset for datagen, to distinguish unset from {@link Modifier#DEFAULT_PRIORITY} */
    private int priority = Integer.MIN_VALUE;
    /** Translation key. If not empty, will use instead of the modifier ID for tooltip and color. */
    private String translationKey;

    /** Adds a module to the builder */
    public final Builder addModule(ModifierModule module) {
      modules.add(new WithHooks<>(module, Collections.emptyList()));
      return this;
    }

    /** Adds a module to the builder */
    public final Builder addModules(ModifierModule... modules) {
      for (ModifierModule module : modules) {
        addModule(module);
      }
      return this;
    }

    /** Adds a module to the builder */
    @SuppressWarnings("UnusedReturnValue")
    @SafeVarargs
    public final <T extends ModifierModule> Builder addModule(T object, ModuleHook<? super T>... hooks) {
      modules.add(new WithHooks<>(object, List.of(hooks)));
      return this;
    }

    /** Overrides the translation key. */
    public Builder translationKey(String key) {
      this.translationKey = key;
      return this;
    }

    /** Overrides the description key prefix using the given modifier. */
    public Builder translationKey(ResourceLocation modifier) {
      return translationKey("modifier." + modifier.toLanguageKey());
    }


    /* Tooltip display */

    /** Sets the tooltips this modifier shows in */
    public Builder showInTooltips(ShowInTooltips showInTooltips) {
      this.showInTooltips = showInTooltips;
      return this;
    }

    /** Sets the tooltips this modifier shows in */
    public Builder showInTooltips(ModifierTooltip... tooltips) {
      return showInTooltips(ShowInTooltips.match(tooltips));
    }

    /** @deprecated use {@link #showInTooltips(ShowInTooltips)} */
    @SuppressWarnings("removal")
    @Deprecated(forRemoval = true)
    public Builder tooltipDisplay(TooltipDisplay display) {
      return showInTooltips(display.getShowInTooltips());
    }

    /** Builds the final instance */
    public ComposableModifier build() {
      List<WithHooks<ModifierModule>> modules = this.modules.build();
      if (priority == Integer.MIN_VALUE) {
        // call computePriority if we did not set one so we get the warning if multiple modules wish to set the priority
        computePriority(modules);
      }
      return new ComposableModifier(levelDisplay, showInTooltips, priority, modules, translationKey, ErrorFactory.RUNTIME);
    }
  }
}
