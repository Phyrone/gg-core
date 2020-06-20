@file:JvmName("Utils")

package de.phyrone.gg.bukkit.utils

import fr.mrmicky.fastparticle.FastParticle
import fr.mrmicky.fastparticle.ParticleType
import mkremins.fanciful.FancyMessage
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.chat.ComponentSerializer
import org.bukkit.Server
import org.bukkit.command.CommandMap
import org.bukkit.command.CommandSender
import org.bukkit.command.defaults.BukkitCommand
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import java.lang.reflect.Field

private var commandMapFieldCache: Field? = null
private fun Server.getCommandMapField(): Field =
    commandMapFieldCache ?: this::class.java.getDeclaredField("commandMap").also { commandMapField ->
        commandMapField.isAccessible = true
        commandMapFieldCache = commandMapField
    }

/**
 * this gives you the commandmap just in case there is no function defined in the bukkit api (f.e. non paperspigot servers)
 * @see Server.getCommandMap
 * @see CommandMap
 */
fun Server.getCommandMapOnNonPaperServers() = getCommandMapField().get(this) as CommandMap

@JvmOverloads
fun Plugin.registerCommand(command: BukkitCommand, fallbackString: String? = null) {
    val bukkitServer = server
    val fallback = fallbackString ?: description.name
    bukkitServer.getCommandMapOnNonPaperServers().register(fallback, command)

}

/**
 * that's dangerous use this carefully or just dont use it
 * @return nothing it freezes the target client
 */
fun Player.crash() {
    FastParticle.spawnParticle(this, ParticleType.LAVA, location, Int.MAX_VALUE)
}

fun FancyMessage.toBaseComponents(): Array<BaseComponent> = ComponentSerializer.parse(toJSONString())
fun CommandSender.sendMessage(fancyMessage: FancyMessage) {
    if (this is Player) {
        this.spigot().sendMessage(*fancyMessage.toBaseComponents())
    } else {
        sendMessage(fancyMessage.toOldMessageFormat())
    }
}

private const val DEFAULT_MAX_DISTANCE = 10.0

@JvmOverloads
fun Entity.getTargetPlayer(maxDistance: Double = DEFAULT_MAX_DISTANCE) =
    getTarget(this, this.world.players, maxDistance)

@JvmOverloads
fun LivingEntity.getTargetEntitySafe(maxDistance: Double = DEFAULT_MAX_DISTANCE) =
    getTarget(this, this.world.entities, maxDistance)

private fun <T : Entity> getTarget(
    entity: Entity,
    entities: List<T>,
    maxDistance: Double
): T? {
    var target: T? = null
    val threshold = 1.0
    for (other in entities) {
        val viewVector = other.location.toVector()
            .subtract(entity.location.toVector())

        if (entity.location.direction.normalize().crossProduct(viewVector)
                .lengthSquared() < threshold
            && viewVector.normalize().dot(
                entity.location.direction.normalize()
            ) >= 0
        ) {
            if (target == null
                || target.location.distanceSquared(
                    entity.location
                ) > other.location
                    .distanceSquared(entity.location)
            ) target = other
        }
    }
    return target?.takeUnless { entity.location.distance(it.location) > maxDistance }
}