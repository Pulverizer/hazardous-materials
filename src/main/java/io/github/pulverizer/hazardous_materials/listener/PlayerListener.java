package io.github.pulverizer.hazardous_materials.listener;

import io.github.pulverizer.hazardous_materials.config.Settings;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.cause.entity.damage.DamageTypes;
import org.spongepowered.api.event.cause.entity.damage.source.DamageSource;
import org.spongepowered.api.event.entity.DamageEntityEvent;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.query.Query;
import org.spongepowered.api.item.inventory.query.QueryTypes;
import org.spongepowered.api.world.explosion.Explosion;

import java.util.Random;

public class PlayerListener {

    @Listener
    public void playerFireDamage(DamageEntityEvent event, @Getter("entity") ServerPlayer player, @Getter("source") DamageSource damageSource) {

        if (Settings.AmmoDetonationMultiplier > 0 && damageSource.type().equals(DamageTypes.FIRE.get())) {
            float tntCount = player.inventory().query(Query.orQueries(QueryTypes.ITEM_TYPE.get().of(ItemTypes.TNT.get()), QueryTypes.ITEM_TYPE.get().of(ItemTypes.TNT_MINECART.get()))).totalQuantity();
            float fireChargeCount = player.inventory().query(QueryTypes.ITEM_TYPE.get().of(ItemTypes.FIRE_CHARGE.get())).totalQuantity();
            float otherCount = player.inventory().query(Query.orQueries(QueryTypes.ITEM_TYPE.get().of(ItemTypes.FIREWORK_STAR.get()), QueryTypes.ITEM_TYPE.get().of(ItemTypes.FIREWORK_ROCKET.get()), QueryTypes.ITEM_TYPE.get().of(ItemTypes.GUNPOWDER.get()))).totalQuantity();

            float chance = ((tntCount / (Settings.AmmoDetonationMultiplier * 128)) + (fireChargeCount / (Settings.AmmoDetonationMultiplier * 512)) + (otherCount / (Settings.AmmoDetonationMultiplier * 1024)));

            int diceRolled = new Random().nextInt(100);

            if (diceRolled <= chance) {
                float size = Math.min(chance * 2, 16);

                Explosion explosion = Explosion.builder()
                        .location(player.serverLocation().add(0, 1.5, 0))
                        .shouldBreakBlocks(true)
                        .shouldDamageEntities(true)
                        .shouldPlaySmoke(true)
                        .radius(size)
                        .resolution((int) (size * 2))
                        .knockback(1)
                        .canCauseFire(fireChargeCount > 0)
                        .build();

                if (tntCount > 0) {
                    player.inventory().query(Query.orQueries(QueryTypes.ITEM_TYPE.get().of(ItemTypes.TNT.get()), QueryTypes.ITEM_TYPE.get().of(ItemTypes.TNT_MINECART.get()))).query(QueryTypes.REVERSE.get().toQuery()).poll((int) tntCount / 2);
                }

                if (fireChargeCount > 0) {
                    player.inventory().query(QueryTypes.ITEM_TYPE.get().of(ItemTypes.FIRE_CHARGE.get())).query(QueryTypes.REVERSE.get().toQuery()).poll((int) fireChargeCount / 2);
                }

                if (otherCount > 0) {
                    player.inventory().query(Query.orQueries(QueryTypes.ITEM_TYPE.get().of(ItemTypes.FIREWORK_STAR.get()), QueryTypes.ITEM_TYPE.get().of(ItemTypes.FIREWORK_ROCKET.get()), QueryTypes.ITEM_TYPE.get().of(ItemTypes.GUNPOWDER.get()))).query(QueryTypes.REVERSE.get().toQuery()).poll((int) otherCount / 2);
                }

                player.world().triggerExplosion(explosion);
            }
        }
    }
}