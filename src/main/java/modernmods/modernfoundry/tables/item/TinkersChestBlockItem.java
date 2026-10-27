package modernmods.modernfoundry.tables.item;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import modernmods.modernfoundry.compat.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import modernmods.modernfoundry.tables.block.entity.chest.TinkersChestBlockEntity;

import javax.annotation.Nullable;

/** Dyeable chest block */
public class TinkersChestBlockItem extends BlockItem implements DyeableLeatherItem {
  public TinkersChestBlockItem(Block blockIn, Properties builder) {
    super(blockIn, builder);
  }

  @Override
  public int getColor(ItemStack stack) {
    return DyedItemColor.getOrDefault(stack, TinkersChestBlockEntity.DEFAULT_COLOR);
  }

  @Override
  protected boolean updateCustomBlockEntityTag(BlockPos pos, Level world, @Nullable Player player, ItemStack stack, BlockState state) {
    boolean result = super.updateCustomBlockEntityTag(pos, world, player, stack, state);
    if (hasCustomColor(stack) && world.getBlockEntity(pos) instanceof TinkersChestBlockEntity te) {
      te.setColor(getColor(stack));
    }
    return result;
  }
}
