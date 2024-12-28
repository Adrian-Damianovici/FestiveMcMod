package me.theobnoxiouspig.operation_xmas.item;

import me.theobnoxiouspig.operation_xmas.Operation_xmas;
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

    public static  final RegistryObject<Item> XMAS_PRESENT = ITEMS.register("xmas_present",
            () -> new Item(new Item.Properties()));


    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
