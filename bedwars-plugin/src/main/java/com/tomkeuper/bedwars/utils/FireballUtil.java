package com.tomkeuper.bedwars.utils;

import org.bukkit.util.Vector;

public final class FireballUtil {

    public static final double MAX_AIM_DISTANCE = 64.0D;
    public static final double SPAWN_OFFSET = 1.5D;
    // Paper 1.8.8 CraftEntity#setVelocity logs "Excessive velocity set detected" above 4.0 per axis
    public static final double MAX_VELOCITY = 4.0D;

    public interface SolidBlockCheck {
        boolean isSolid(int x, int y, int z);
    }

    private FireballUtil() {
    }

    public static Vector rayTraceBlock(SolidBlockCheck check, Vector origin, Vector direction, double maxDistance) {
        if (check == null) throw new IllegalArgumentException("check");
        if (origin == null) throw new IllegalArgumentException("origin");
        if (direction == null) throw new IllegalArgumentException("direction");
        if (maxDistance <= 0) throw new IllegalArgumentException("maxDistance");
        if (direction.lengthSquared() < 1.0E-12D) throw new IllegalArgumentException("direction");

        Vector dir = direction.clone().normalize();

        int bx = floor(origin.getX());
        int by = floor(origin.getY());
        int bz = floor(origin.getZ());

        if (check.isSolid(bx, by, bz)) return origin.clone();

        int stepX = dir.getX() > 0 ? 1 : -1;
        int stepY = dir.getY() > 0 ? 1 : -1;
        int stepZ = dir.getZ() > 0 ? 1 : -1;

        double tDeltaX = dir.getX() == 0 ? Double.POSITIVE_INFINITY : Math.abs(1.0D / dir.getX());
        double tDeltaY = dir.getY() == 0 ? Double.POSITIVE_INFINITY : Math.abs(1.0D / dir.getY());
        double tDeltaZ = dir.getZ() == 0 ? Double.POSITIVE_INFINITY : Math.abs(1.0D / dir.getZ());

        double tMaxX = dir.getX() == 0 ? Double.POSITIVE_INFINITY : (dir.getX() > 0 ? (bx + 1 - origin.getX()) : (origin.getX() - bx)) * tDeltaX;
        double tMaxY = dir.getY() == 0 ? Double.POSITIVE_INFINITY : (dir.getY() > 0 ? (by + 1 - origin.getY()) : (origin.getY() - by)) * tDeltaY;
        double tMaxZ = dir.getZ() == 0 ? Double.POSITIVE_INFINITY : (dir.getZ() > 0 ? (bz + 1 - origin.getZ()) : (origin.getZ() - bz)) * tDeltaZ;

        double t = 0.0D;
        while (t <= maxDistance) {
            if (tMaxX < tMaxY && tMaxX < tMaxZ) {
                bx += stepX;
                t = tMaxX;
                tMaxX += tDeltaX;
            } else if (tMaxY < tMaxZ) {
                by += stepY;
                t = tMaxY;
                tMaxY += tDeltaY;
            } else {
                bz += stepZ;
                t = tMaxZ;
                tMaxZ += tDeltaZ;
            }
            if (t > maxDistance) break;
            if (check.isSolid(bx, by, bz)) return origin.clone().add(dir.clone().multiply(t));
        }
        return origin.clone().add(dir.clone().multiply(maxDistance));
    }

    public static Vector computeSpawnPoint(Vector eye, Vector direction, SolidBlockCheck check) {
        if (eye == null || direction == null) throw new IllegalArgumentException("null argument");
        Vector dir = direction.clone().normalize();
        return rayTraceBlock(check, eye, dir, SPAWN_OFFSET);
    }

    public static Vector computeLaunchVelocity(Vector spawn, Vector aimPoint, Vector eyeDirection, double speed) {
        if (spawn == null || aimPoint == null || eyeDirection == null) throw new IllegalArgumentException("null argument");
        if (speed <= 0) throw new IllegalArgumentException("speed");
        Vector toAim = aimPoint.clone().subtract(spawn);
        Vector velocity = toAim.lengthSquared() < 1.0E-4D
                ? eyeDirection.clone().normalize()
                : toAim.normalize();
        velocity.multiply(speed);
        clampToPaperLimit(velocity);
        return velocity;
    }

    private static void clampToPaperLimit(Vector velocity) {
        double maxAxis = Math.max(Math.abs(velocity.getX()),
                Math.max(Math.abs(velocity.getY()), Math.abs(velocity.getZ())));
        if (maxAxis > MAX_VELOCITY) {
            velocity.multiply(MAX_VELOCITY / maxAxis);
        }
    }

    /**
     * Explosion knockback (vanilla JE model, minecraft.wiki/w/Explosion):
     * direction = explosion center -> player eye (full 3D), magnitude = (1 - distance/range) * knockbackScale,
     * then the player's current velocity is carried over (momentum).
     * Range is 4x the explosion power (vanilla's 2x zeroes out on typical forward throws, which feel dead).
     */
    public static Vector computeExplosionKnockback(Vector explosion, Vector playerPos, Vector playerEye, Vector currentVelocity,
                                                   double power, double knockbackScale, double momentumCarry) {
        if (explosion == null || playerPos == null || playerEye == null) throw new IllegalArgumentException("null argument");
        if (power <= 0) throw new IllegalArgumentException("power");
        if (knockbackScale < 0) throw new IllegalArgumentException("knockbackScale");
        Vector direction = playerEye.clone().subtract(explosion);
        double distance = direction.length();
        if (distance < 1.0E-4D) direction = new Vector(0, 1, 0);
        double magnitude = Math.max(0, (1 - distance / (4 * power))) * knockbackScale;
        if (magnitude <= 0) return new Vector(0, 0, 0);
        Vector velocity = direction.normalize().multiply(magnitude);
        if (currentVelocity != null && momentumCarry > 0) {
            velocity.add(currentVelocity.clone().multiply(momentumCarry));
        }
        clampToPaperLimit(velocity);
        return velocity;
    }

    private static int floor(double v) {
        int i = (int) v;
        return v < i ? i - 1 : i;
    }
}
