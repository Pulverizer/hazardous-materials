package io.github.pulverizer.hazardous_materials.config;

import org.spongepowered.api.block.transaction.Operation;
import org.spongepowered.api.event.cause.entity.damage.DamageType;
import org.spongepowered.api.item.ItemType;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class HazardousItem {

    private final ItemType itemType;
    private final float explosiveValue;
    private final float detonationChance;
    private final HashSet<DamageType> entityTriggers = new HashSet<>();
    private final HashSet<Operation> blockTriggers = new HashSet<>();

    HazardousItem(ItemType itemType, float explosiveValue, float detonationChance) {
        this.itemType = itemType;
        this.explosiveValue = explosiveValue;
        this.detonationChance = detonationChance;
    }

    public ItemType itemType() {
        return itemType;
    }

    public float explosionValue() {
        return explosiveValue;
    }

    public float detonationChance() {
        return detonationChance;
    }

    public Set<DamageType> entityTriggers() {
        return Collections.unmodifiableSet(entityTriggers);
    }

    public Set<Operation> blockTriggers() {
        return  Collections.unmodifiableSet(blockTriggers);
    }

    void addTrigger(DamageType damageType) {
        entityTriggers.add(damageType);
    }

    void addTrigger(Operation blockOperation) {
        blockTriggers.add(blockOperation);
    }
}
