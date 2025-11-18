package io.github.pulverizer.hazardous_materials.listener;

import io.github.pulverizer.hazardous_materials.config.HazardousItem;
import io.github.pulverizer.hazardous_materials.config.Settings;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.entity.BlockEntity;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.cause.entity.damage.source.DamageSource;
import org.spongepowered.api.event.entity.DamageEntityEvent;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.item.inventory.Carrier;
import org.spongepowered.api.item.inventory.query.QueryTypes;
import org.spongepowered.api.world.explosion.Explosion;
import org.spongepowered.api.world.explosion.ExplosionBlockInteractions;

public class EntityListener {

    @Listener
    public void entityDamage(final DamageEntityEvent event, final @Getter("entity") Entity entity, final @Getter("source") DamageSource damageSource) {

        if (entity instanceof Carrier carrier && !Settings.hazardousItems().isEmpty()) {
            float explosiveMass = 0;
            float detonationChance = 0;
            int totalHazardCount = 0;

            boolean flammable = false;

            for (HazardousItem hazardousItem : Settings.hazardousItems()) {
                if (hazardousItem.entityTriggers().contains(damageSource.type())) {
                    int hazardCount = carrier.inventory().query(QueryTypes.ITEM_TYPE.get().of(hazardousItem.itemType())).totalQuantity();

                    explosiveMass = explosiveMass + (hazardCount * hazardousItem.explosionValue());
                    detonationChance = detonationChance + (hazardCount * hazardousItem.detonationChance());
                    totalHazardCount = totalHazardCount + hazardCount;
                }
            }

            detonationChance = detonationChance / totalHazardCount;

            if (entity.serverLocation().world().random().nextFloat() <= detonationChance) {
                float size = Math.min(explosiveMass, 16);

                Explosion explosion = Explosion.builder()
                        .location(entity.serverLocation().add(0, 1.5, 0))
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

                entity.serverLocation().world().triggerExplosion(explosion);
            }
        }

/*
        if (Settings.AmmoDetonationMultiplier > 0 && damageSource.type().equals(DamageTypes.FIRE.get())) {
            float tntCount = entity.inventory().query(Query.orQueries(QueryTypes.ITEM_TYPE.get().of(ItemTypes.TNT.get()), QueryTypes.ITEM_TYPE.get().of(ItemTypes.TNT_MINECART.get()))).totalQuantity();
            float fireChargeCount = entity.inventory().query(QueryTypes.ITEM_TYPE.get().of(ItemTypes.FIRE_CHARGE.get())).totalQuantity();
            float otherCount = entity.inventory().query(Query.orQueries(QueryTypes.ITEM_TYPE.get().of(ItemTypes.FIREWORK_STAR.get()), QueryTypes.ITEM_TYPE.get().of(ItemTypes.FIREWORK_ROCKET.get()), QueryTypes.ITEM_TYPE.get().of(ItemTypes.GUNPOWDER.get()))).totalQuantity();

            //float chance = ((tntCount / (Settings.AmmoDetonationMultiplier * 128)) + (fireChargeCount / (Settings.AmmoDetonationMultiplier * 512)) + (otherCount / (Settings.AmmoDetonationMultiplier * 1024)));

        }
 */
    }
}