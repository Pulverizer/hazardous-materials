package io.github.pulverizer.hazardous_materials.listener;

import io.github.pulverizer.hazardous_materials.config.Settings;
import org.spongepowered.api.block.entity.BlockEntity;
import org.spongepowered.api.block.entity.carrier.CarrierBlockEntity;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.world.ExplosionEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.query.Query;
import org.spongepowered.api.item.inventory.query.QueryTypes;
import org.spongepowered.api.world.explosion.Explosion;
import org.spongepowered.api.world.server.ServerLocation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;
import java.util.Random;

import static org.spongepowered.api.event.Order.LAST;

public class TNTListener {
    private final HashMap<Explosion, HashSet<Explosion>> ammoDetonation = new HashMap<>();

    @Listener(order = LAST)
    public void explodeEvent(ExplosionEvent.Detonate event) {

        if (Settings.AmmoDetonationMultiplier > 0) {

            HashSet<Explosion> explosions = new HashSet<>();

            for (ServerLocation location : event.affectedLocations()) {

                Optional<? extends BlockEntity> tileEntity = location.blockEntity();

                if (tileEntity.isEmpty() || !(tileEntity.get() instanceof CarrierBlockEntity)) {
                    continue;
                }

                Inventory inventory = ((CarrierBlockEntity) tileEntity.get()).inventory();

                float tntCount = inventory.query(Query.orQueries(QueryTypes.ITEM_TYPE.get().of(ItemTypes.TNT.get()), QueryTypes.ITEM_TYPE.get().of(ItemTypes.TNT_MINECART.get()))).totalQuantity();
                float fireChargeCount = inventory.query(QueryTypes.ITEM_TYPE.get().of(ItemTypes.FIRE_CHARGE.get())).totalQuantity();
                float otherCount = inventory.query(Query.orQueries(QueryTypes.ITEM_TYPE.get().of(ItemTypes.FIREWORK_STAR.get()), QueryTypes.ITEM_TYPE.get().of(ItemTypes.FIREWORK_ROCKET.get()), QueryTypes.ITEM_TYPE.get().of(ItemTypes.GUNPOWDER.get()))).totalQuantity();

                float chance = ((tntCount / (Settings.AmmoDetonationMultiplier * 32)) + (fireChargeCount / (Settings.AmmoDetonationMultiplier * 128)) + (otherCount / (Settings.AmmoDetonationMultiplier * 256)));

                int diceRolled = new Random().nextInt(100);

                if (diceRolled <= chance) {
                    float size = Math.min(chance, 16);

                    Explosion explosion = Explosion.builder()
                            .location(location.add(0.5, 0.5, 0.5))
                            .shouldBreakBlocks(true)
                            .shouldDamageEntities(true)
                            .shouldPlaySmoke(true)
                            .radius(size)
                            .resolution((int) (size * 2))
                            .knockback(size)
                            .canCauseFire(fireChargeCount > 0)
                            .build();

                    explosions.add(explosion);
                }
            }

            ammoDetonation.put(event.explosion(), explosions);
        }
    }

    @Listener(order = LAST)
    public void explosionPOST(ChangeBlockEvent.Post event) {
        // Hope this is correct!
        if (event.cause().containsType(Explosion.class)) {
            Explosion explosion = event.cause().first(Explosion.class).get();

            if (ammoDetonation.containsKey(explosion)) {
                ammoDetonation.get(explosion).forEach(ammoExplosion -> ammoExplosion.serverLocation().world().triggerExplosion(ammoExplosion));
                ammoDetonation.remove(explosion);
            }
        }
    }
}
