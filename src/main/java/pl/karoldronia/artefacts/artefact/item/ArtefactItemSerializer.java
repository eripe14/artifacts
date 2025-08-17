package pl.karoldronia.artefacts.artefact.item;

import eu.okaeri.configs.schema.GenericsDeclaration;
import eu.okaeri.configs.serdes.DeserializationData;
import eu.okaeri.configs.serdes.ObjectSerializer;
import eu.okaeri.configs.serdes.SerializationData;
import lombok.NonNull;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;

public class ArtefactItemSerializer implements ObjectSerializer<ArtefactItem> {
    @Override
    public boolean supports(@NonNull Class<? super ArtefactItem> type) {
        return ArtefactItem.class.isAssignableFrom(type);
    }

    @Override
    public void serialize(@NonNull ArtefactItem object, @NonNull SerializationData data, @NonNull GenericsDeclaration generics) {
        data.add("customModelData", object.getCustomModelData());
        data.add("material", object.getMaterial());
        data.add("name", object.getName());
        data.add("lore", object.getLore());
        data.add("flags", object.getItemFlags());
    }

    @Override
    public ArtefactItem deserialize(@NonNull DeserializationData data, @NonNull GenericsDeclaration generics) {
        return new ArtefactItem(
                data.get("customModelData", Integer.class),
                data.get("material", Material.class),
                data.get("name", String.class),
                data.getAsList("lore", String.class),
                data.getAsList("flags", ItemFlag.class)
        );
    }
}