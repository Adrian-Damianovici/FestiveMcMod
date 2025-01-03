package me.theobnoxiouspig.operation_xmas.datagen;

import me.theobnoxiouspig.operation_xmas.Operation_xmas;
import me.theobnoxiouspig.operation_xmas.item.ModItems;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.model.generators.ItemModelBuilder;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;

public class ModItemModelProvider extends ItemModelProvider {
    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, Operation_xmas.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        simpleItem(ModItems.ANCIENT_GEM);
        simpleItem(ModItems.BOX_CUTTERS);

        simpleItem(ModItems.METAL_DETECTOR);
        simpleItem(ModItems.ICECREAM_SANDWICH);
        simpleItem(ModItems.XMAS_PRESENT);
        simpleItem(ModItems.SMALL_TIMBER);
    }

    private ItemModelBuilder simpleItem(RegistryObject<Item> item) {
        return withExistingParent(item.getId().getPath(),
                new ResourceLocation("item/generated")).texture("layer0",
                new ResourceLocation(Operation_xmas.MOD_ID,"item/" + item.getId().getPath()));
    }
}