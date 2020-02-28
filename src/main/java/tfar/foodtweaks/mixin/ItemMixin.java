package tfar.foodtweaks.mixin;

import net.minecraft.item.FoodComponent;
import net.minecraft.item.Item;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import tfar.foodtweaks.ItemInterface;

import javax.annotation.Nullable;

@Mixin(Item.class)
public class ItemMixin implements ItemInterface {

	@Shadow @Final @Mutable @Nullable private FoodComponent foodComponent;

	@Override
	public void setFood(FoodComponent foodComponent) {
		this.foodComponent = foodComponent;
	}
}
