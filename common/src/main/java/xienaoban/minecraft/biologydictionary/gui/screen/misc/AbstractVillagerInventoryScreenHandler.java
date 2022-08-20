package xienaoban.minecraft.biologydictionary.gui.screen.misc;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class AbstractVillagerInventoryScreenHandler extends AbstractContainerMenu {
    protected AbstractVillagerInventoryScreenHandler(@Nullable MenuType<?> menuType, int i) {
        super(menuType, i);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int i) {
        return null;
    }

    @Override
    public boolean stillValid(Player player) {
        return false;
    }
}
