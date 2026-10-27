package modernmods.modernfoundry.library.recipe.ingredient;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Instrument;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;
import modernmods.modernfoundry.TConstruct;
import modernmods.modernfoundry.shared.TinkerCommons;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

public class InstrumentIngredient implements ICustomIngredient {
  public static final ResourceLocation ID = TConstruct.getResource("instrument");

  private final Item item;
  @Nullable
  private final ResourceKey<Instrument> instrument;
  @Nullable
  private final TagKey<Instrument> ignore;

  protected InstrumentIngredient(Item item, @Nullable ResourceKey<Instrument> instrument, @Nullable TagKey<Instrument> ignore) {
    this.item = item;
    this.instrument = instrument;
    this.ignore = ignore;
  }

  public static Ingredient of(ItemLike item, ResourceKey<Instrument> instrument) {
    return new InstrumentIngredient(item.asItem(), instrument, null).toVanilla();
  }

  public static Ingredient of(ItemLike item, TagKey<Instrument> ignore) {
    return new InstrumentIngredient(item.asItem(), null, ignore).toVanilla();
  }

  @Override
  public boolean isSimple() {
    return false;
  }

  @Override
  public boolean test(ItemStack stack) {
    if (!stack.is(item)) {
      return false;
    }
    Holder<Instrument> held = stack.get(DataComponents.INSTRUMENT);
    if (held != null) {
      if (this.instrument != null) {
        return held.is(this.instrument);
      }
      return ignore == null || !held.is(ignore);
    }
    return this.instrument == null;
  }

  @Override
  public Stream<ItemStack> getItems() {
    ItemStack stack = new ItemStack(item);
    if (instrument != null) {
      BuiltInRegistries.INSTRUMENT.getHolder(instrument).ifPresent(holder -> stack.set(DataComponents.INSTRUMENT, holder));
    }
    return Stream.of(stack);
  }

  @Override
  public IngredientType<?> getType() {
    return TinkerCommons.instrumentIngredient.get();
  }

  @Override
  public boolean equals(Object object) {
    return this == object || object instanceof InstrumentIngredient that && item == that.item && Objects.equals(instrument, that.instrument) && Objects.equals(ignore, that.ignore);
  }

  @Override
  public int hashCode() {
    return Objects.hash(item, instrument, ignore);
  }

  public static final MapCodec<InstrumentIngredient> CODEC = RecordCodecBuilder.<InstrumentIngredient>mapCodec(inst -> inst.group(
    BuiltInRegistries.ITEM.byNameCodec().fieldOf("item").forGetter(i -> i.item),
    ResourceKey.codec(Registries.INSTRUMENT).optionalFieldOf("instrument").forGetter(i -> Optional.ofNullable(i.instrument)),
    TagKey.codec(Registries.INSTRUMENT).optionalFieldOf("ignore").forGetter(i -> Optional.ofNullable(i.ignore))
  ).apply(inst, (item, instrument, ignore) -> new InstrumentIngredient(item, instrument.orElse(null), ignore.orElse(null))))
    .validate(i -> i.instrument != null || i.ignore != null ? DataResult.success(i) : DataResult.error(() -> "Invalid InstrumentIngredient: must set either 'instrument' or 'ignore'"));

  public static final StreamCodec<RegistryFriendlyByteBuf, InstrumentIngredient> STREAM_CODEC = StreamCodec.composite(
    ByteBufCodecs.registry(Registries.ITEM), i -> i.item,
    ByteBufCodecs.optional(ResourceKey.streamCodec(Registries.INSTRUMENT)), i -> Optional.ofNullable(i.instrument),
    ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC), i -> Optional.ofNullable(i.ignore).map(TagKey::location),
    (item, instrument, ignore) -> new InstrumentIngredient(item, instrument.orElse(null), ignore.map(id -> TagKey.create(Registries.INSTRUMENT, id)).orElse(null)));
}
