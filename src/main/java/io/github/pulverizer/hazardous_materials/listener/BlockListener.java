package io.github.pulverizer.hazardous_materials.listener;

import io.github.pulverizer.hazardous_materials.config.HazardousItem;
import io.github.pulverizer.hazardous_materials.config.Settings;
import org.spongepowered.api.block.entity.BlockEntityArchetype;
import org.spongepowered.api.block.entity.carrier.CarrierBlockEntity;
import org.spongepowered.api.block.transaction.BlockTransactionReceipt;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.item.inventory.query.QueryTypes;
import org.spongepowered.api.world.explosion.Explosion;
import org.spongepowered.api.world.explosion.ExplosionBlockInteractions;

import java.util.Optional;

import static org.spongepowered.api.event.Order.POST;

public class BlockListener {

    @Listener(order = POST)
    public void blockEvent(final ChangeBlockEvent.Post event) {
        for (BlockTransactionReceipt transaction : event.receipts()) {
            Optional<BlockEntityArchetype> blockEntity = transaction.originalBlock().createArchetype();

            if (blockEntity.isPresent()) {
                if (blockEntity.get() instanceof CarrierBlockEntity carrier) {

                    float explosiveMass = 0;
                    float detonationChance = 0;
                    int totalHazardCount = 0;

                    boolean flammable = false;

                    for (HazardousItem hazardousItem : Settings.hazardousItems()) {
                        if (/*hazardousItem.blockTriggers().contains(transaction.operation()) ||*/ (event.cause().containsType(Entity.class) && event.cause().first(Entity.class).get().type().equals(EntityTypes.TNT.get()))) {
                            int hazardCount = carrier.inventory().query(QueryTypes.ITEM_TYPE.get().of(hazardousItem.itemType())).totalQuantity();

                            explosiveMass = explosiveMass + (hazardCount * hazardousItem.explosionValue());
                            detonationChance = detonationChance + (hazardCount * hazardousItem.detonationChance());
                            totalHazardCount = totalHazardCount + hazardCount;
                        }
                    }

                    detonationChance = detonationChance / totalHazardCount;

                    if (transaction.originalBlock().location().get().world().random().nextFloat() <= detonationChance) {
                        float size = Math.min(explosiveMass, 16);

                        Explosion explosion = Explosion.builder()
                                .location(transaction.originalBlock().location().get())
                                .blockInteraction(ExplosionBlockInteractions.DESTROY.get())
                                .shouldDamageEntities(true)
                                .shouldPlaySmoke(true)
                                .radius(size)
                                .resolution((int) (size * 2))
                                .knockback(1)
                                .canCauseFire(flammable)
                                .build();

                        for (HazardousItem hazardousItem : Settings.hazardousItems()) {
                            carrier.inventory().query(QueryTypes.ITEM_TYPE.get().of(hazardousItem.itemType())).poll(carrier.inventory().query(QueryTypes.ITEM_TYPE.get().of(hazardousItem.itemType())).totalQuantity());
                        }

                        transaction.originalBlock().location().get().world().triggerExplosion(explosion);
                    }
                }
            }
        }
    }
}