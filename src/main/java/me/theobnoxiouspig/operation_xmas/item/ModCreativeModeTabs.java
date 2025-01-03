package me.theobnoxiouspig.operation_xmas.item;

import me.theobnoxiouspig.operation_xmas.Operation_xmas;
import me.theobnoxiouspig.operation_xmas.block.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Operation_xmas.MOD_ID);

    public static final RegistryObject<CreativeModeTab> MAIN_TAB = CREATIVE_MODE_TABS.register("main_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModItems.XMAS_PRESENT.get()))
                    .title(Component.translatable("creativetab.main_tab"))
                    .displayItems((pParameters, pOutput) -> {
                        pOutput.accept(ModItems.XMAS_PRESENT.get());
                        pOutput.accept(ModItems.BOX_CUTTERS.get());
                        pOutput.accept(Items.SNOWBALL);
                        pOutput.accept(ModItems.METAL_DETECTOR.get());
                        pOutput.accept(ModItems.ANCIENT_GEM.get());
                        pOutput.accept(ModItems.ICECREAM_SANDWICH.get());
                        pOutput.accept(ModItems.SMALL_TIMBER.get());

                        pOutput.accept(ModBlocks.TEST_BLOCK.get());
                        pOutput.accept(ModBlocks.BRASS_BLOCK.get());
                        pOutput.accept(ModBlocks.SAPPHIRE_ORE.get());
                        pOutput.accept(ModBlocks.SPEAKER_BLOCK.get());
                        pOutput.accept(ModBlocks.NETHER_SAPPHIRE_ORE.get());

                    })
                    .build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
