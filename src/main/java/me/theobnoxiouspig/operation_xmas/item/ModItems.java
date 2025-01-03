package me.theobnoxiouspig.operation_xmas.item;

import me.theobnoxiouspig.operation_xmas.Operation_xmas;
import me.theobnoxiouspig.operation_xmas.item.custom.FuelItem;
import me.theobnoxiouspig.operation_xmas.item.custom.MetalDetectorItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, Operation_xmas.MOD_ID);

    public static final RegistryObject<Item> BOX_CUTTERS = ITEMS.register("box_cutters",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> XMAS_PRESENT = ITEMS.register("xmas_present",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> ANCIENT_GEM = ITEMS.register("ancient_gem",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> METAL_DETECTOR = ITEMS.register("metal_detector",
            () -> new MetalDetectorItem(new Item.Properties().durability(200)));

    public static final RegistryObject<Item> ICECREAM_SANDWICH = ITEMS.register("icecream_sandwich",
            () -> new Item(new Item.Properties().food(ModFoods.ICECREAM_SANDWICH)));

    public static final RegistryObject<Item> SMALL_TIMBER = ITEMS.register("small_timber",
            () -> new FuelItem(new Item.Properties(), 200));




    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
