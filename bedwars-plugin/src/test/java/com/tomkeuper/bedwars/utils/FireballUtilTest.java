package com.tomkeuper.bedwars.utils;

import org.bukkit.util.Vector;
import org.junit.jupiter.api.Test;

import java.util.function.BiFunction;

import static org.junit.jupiter.api.Assertions.*;

public class FireballUtilTest {

    private static final FireballUtil.SolidBlockCheck NO_BLOCKS = (x, y, z) -> false;

    private static FireballUtil.SolidBlockCheck singleBlock(int bx, int by, int bz) {
        return (x, y, z) -> x == bx && y == by && z == bz;
    }

    @Test
    void rayTraceReturnsMaxDistancePointWhenNoBlockHit() {
        Vector origin = new Vector(0.5, 64.5, 0.5);
        Vector dir = new Vector(1, 0, 0);
        Vector result = FireballUtil.rayTraceBlock(NO_BLOCKS, origin, dir, 64);
        assertEquals(new Vector(64.5, 64.5, 0.5), result);
    }

    @Test
    void rayTraceStopsAtFirstSolidBlock() {
        Vector origin = new Vector(0.5, 64.5, 0.5);
        Vector dir = new Vector(1, 0, 0);
        Vector result = FireballUtil.rayTraceBlock(singleBlock(3, 64, 0), origin, dir, 64);
        assertEquals(3.0, result.getX(), 1e-9);
        assertEquals(64.5, result.getY(), 1e-9);
        assertEquals(0.5, result.getZ(), 1e-9);
    }

    @Test
    void rayTraceReturnsOriginWhenOriginInsideSolidBlock() {
        Vector origin = new Vector(2.5, 64.5, 0.5);
        Vector dir = new Vector(1, 0, 0);
        Vector result = FireballUtil.rayTraceBlock(singleBlock(2, 64, 0), origin, dir, 64);
        assertEquals(origin, result);
    }

    @Test
    void rayTraceRespectsMaxDistanceWithBlockBeyondRange() {
        Vector origin = new Vector(0.5, 64.5, 0.5);
        Vector dir = new Vector(1, 0, 0);
        Vector result = FireballUtil.rayTraceBlock(singleBlock(30, 64, 0), origin, dir, 10);
        assertEquals(new Vector(10.5, 64.5, 0.5), result);
    }

    @Test
    void rayTraceDiagonalHitsCorrectBlock() {
        Vector origin = new Vector(0.2, 64.2, 0.2);
        Vector dir = new Vector(1, 1, 1);
        FireballUtil.SolidBlockCheck check = singleBlock(2, 66, 2);
        Vector result = FireballUtil.rayTraceBlock(check, origin, dir, 32);
        double t = (result.getX() - 0.2) / 1.0;
        assertTrue(t > 1.0 && t < 2.0, "hit should be between blocks 1 and 2 boundary, t=" + t);
        assertEquals(Math.floor(0.2 + t), Math.floor(result.getX()), 1e-9);
    }

    @Test
    void rayTraceDownwardHitsBlockBelow() {
        Vector origin = new Vector(0.5, 70.5, 0.5);
        Vector dir = new Vector(0, -1, 0);
        Vector result = FireballUtil.rayTraceBlock(singleBlock(0, 65, 0), origin, dir, 64);
        assertEquals(66.0, result.getY(), 1e-9);
    }

    @Test
    void rayTraceRejectsInvalidInput() {
        assertThrows(IllegalArgumentException.class, () -> FireballUtil.rayTraceBlock(null, new Vector(0, 0, 0), new Vector(1, 0, 0), 5));
        assertThrows(IllegalArgumentException.class, () -> FireballUtil.rayTraceBlock(NO_BLOCKS, null, new Vector(1, 0, 0), 5));
        assertThrows(IllegalArgumentException.class, () -> FireballUtil.rayTraceBlock(NO_BLOCKS, new Vector(0, 0, 0), null, 5));
        assertThrows(IllegalArgumentException.class, () -> FireballUtil.rayTraceBlock(NO_BLOCKS, new Vector(0, 0, 0), new Vector(1, 0, 0), 0));
        assertThrows(IllegalArgumentException.class, () -> FireballUtil.rayTraceBlock(NO_BLOCKS, new Vector(0, 0, 0), new Vector(0, 0, 0), 5));
    }

    @Test
    void computeSpawnPointOffsetsAlongAim() {
        Vector eye = new Vector(0.5, 64.5, 0.5);
        Vector dir = new Vector(1, 0, 0);
        Vector spawn = FireballUtil.computeSpawnPoint(eye, dir, NO_BLOCKS);
        assertEquals(eye.getX() + FireballUtil.SPAWN_OFFSET, spawn.getX(), 1e-9);
        assertEquals(eye.getY(), spawn.getY(), 1e-9);
    }

    @Test
    void computeSpawnPointClipsToWall() {
        Vector eye = new Vector(0.5, 64.5, 0.5);
        Vector dir = new Vector(1, 0, 0);
        Vector spawn = FireballUtil.computeSpawnPoint(eye, dir, singleBlock(1, 64, 0));
        assertEquals(1.0, spawn.getX(), 1e-9);
    }

    @Test
    void computeLaunchVelocityAimsAtTarget() {
        Vector spawn = new Vector(0, 64, 0);
        Vector aim = new Vector(30, 64, 40);
        Vector vel = FireballUtil.computeLaunchVelocity(spawn, aim, new Vector(1, 0, 0), 2.5);
        assertEquals(2.5, vel.length(), 1e-9);
        Vector expected = new Vector(30, 0, 40).normalize().multiply(2.5);
        assertEquals(expected.getX(), vel.getX(), 1e-9);
        assertEquals(expected.getZ(), vel.getZ(), 1e-9);
    }

    @Test
    void computeLaunchVelocityFallsBackToEyeDirectionWhenAimEqualsSpawn() {
        Vector spawn = new Vector(5, 64, 5);
        Vector vel = FireballUtil.computeLaunchVelocity(spawn, spawn.clone(), new Vector(0, 0, 1), 2.0);
        assertEquals(new Vector(0, 0, 2), vel);
    }

    @Test
    void computeLaunchVelocityRejectsInvalidInput() {
        Vector v = new Vector(1, 0, 0);
        assertThrows(IllegalArgumentException.class, () -> FireballUtil.computeLaunchVelocity(null, v, v, 1));
        assertThrows(IllegalArgumentException.class, () -> FireballUtil.computeLaunchVelocity(v, null, v, 1));
        assertThrows(IllegalArgumentException.class, () -> FireballUtil.computeLaunchVelocity(v, v, null, 1));
        assertThrows(IllegalArgumentException.class, () -> FireballUtil.computeLaunchVelocity(v, v, v, 0));
    }

    @Test
    void computeLaunchVelocityClampsToPaperLimit() {
        Vector spawn = new Vector(0, 64, 0);
        Vector vel = FireballUtil.computeLaunchVelocity(spawn, new Vector(0, -64, 0), new Vector(0, -1, 0), 10);
        assertEquals(4.0, Math.abs(vel.getY()), 1e-9);
        assertEquals(0.0, vel.getX(), 1e-9);
        assertEquals(0.0, vel.getZ(), 1e-9);
        assertEquals(4.0, vel.length(), 1e-9);
    }

    @Test
    void computeLaunchVelocityClampPreservesAimDirection() {
        Vector spawn = new Vector(0, 64, 0);
        Vector aim = new Vector(60, -16, -60);
        Vector toAim = aim.clone().subtract(spawn);
        Vector vel = FireballUtil.computeLaunchVelocity(spawn, aim, new Vector(0, -1, 0), 10);
        assertEquals(4.0, Math.abs(vel.getY()), 1e-9);
        assertEquals(vel.getX() / vel.getY(), toAim.getX() / toAim.getY(), 1e-9);
        assertEquals(vel.getZ() / vel.getY(), toAim.getZ() / toAim.getY(), 1e-9);
    }

    @Test
    void computeLaunchVelocityDoesNotClampLegalShots() {
        Vector spawn = new Vector(0, 64, 0);
        Vector vel = FireballUtil.computeLaunchVelocity(spawn, spawn.clone().add(new Vector(3.9, 3.9, 3.9)), new Vector(1, 1, 1), 4);
        assertEquals(4.0, vel.length(), 1e-9);
        assertEquals(4.0 / Math.sqrt(3), vel.getX(), 1e-9);
    }

    @Test
    void computeLaunchVelocityClampsEyeDirectionFallback() {
        Vector spawn = new Vector(5, 64, 5);
        Vector vel = FireballUtil.computeLaunchVelocity(spawn, spawn.clone(), new Vector(0, -1, 0), 10);
        assertEquals(4.0, vel.length(), 1e-9);
        assertEquals(-4.0, vel.getY(), 1e-9);
    }

    @Test
    void explosionKnockbackThrowsUpAndBackOn45DegreeDownThrow() {
        // player at (0,64,0) fires at 45° down; blast lands ~2 blocks in front at same height as feet
        Vector vel = FireballUtil.computeExplosionKnockback(
                new Vector(1.5, 64.2, 0), new Vector(0, 64, 0), new Vector(0, 64.62, 0), new Vector(0, 0, 0),
                3.0, 1.5, 0.0);
        assertTrue(vel.getY() > 0.1, "blast in front at foot level should launch up, y=" + vel.getY());
        assertTrue(vel.getX() < -0.1, "blast in front should push back, x=" + vel.getX());
    }

    @Test
    void explosionKnockbackThrowsDownWhenBlastAboveHead() {
        // fireball thrown straight up explodes above the player's head
        Vector vel = FireballUtil.computeExplosionKnockback(
                new Vector(0, 67.5, 0), new Vector(0, 64, 0), new Vector(0, 64.62, 0), new Vector(0, 0, 0),
                3.0, 1.5, 0.0);
        assertTrue(vel.getY() < -0.1, "blast above head should slam player down, y=" + vel.getY());
    }

    @Test
    void explosionKnockbackFallsOffWithDistance() {
        Vector close = FireballUtil.computeExplosionKnockback(
                new Vector(2, 64, 0), new Vector(0, 64, 0), new Vector(0, 64.62, 0), new Vector(0, 0, 0),
                3.0, 1.5, 0.0);
        Vector far = FireballUtil.computeExplosionKnockback(
                new Vector(8, 64, 0), new Vector(0, 64, 0), new Vector(0, 64.62, 0), new Vector(0, 0, 0),
                3.0, 1.5, 0.0);
        assertTrue(close.length() > far.length(), "closer blast must hit harder");
        assertTrue(close.length() > 0);
        assertTrue(far.length() > 0);
    }

    @Test
    void explosionKnockbackZeroBeyondQuadruplePowerRange() {
        // running-jump forward throws land 7-12 blocks away; must still push (was dead at 2x power)
        Vector twelve = FireballUtil.computeExplosionKnockback(
                new Vector(11, 64, 0), new Vector(0, 64, 0), new Vector(0, 64.62, 0), new Vector(0, 0, 0),
                3.0, 1.5, 0.0);
        assertTrue(twelve.length() > 0, "11-block blast must still push");
        Vector thirteen = FireballUtil.computeExplosionKnockback(
                new Vector(13, 64, 0), new Vector(0, 64, 0), new Vector(0, 64.62, 0), new Vector(0, 0, 0),
                3.0, 1.5, 0.0);
        assertEquals(0.0, thirteen.length(), 1e-9);
    }

    @Test
    void explosionKnockbackCarriesMomentum() {
        Vector standing = FireballUtil.computeExplosionKnockback(
                new Vector(1.5, 64.2, 0), new Vector(0, 64, 0), new Vector(0, 64.62, 0), new Vector(0, 0, 0),
                3.0, 1.5, 0.5);
        Vector running = FireballUtil.computeExplosionKnockback(
                new Vector(1.5, 64.2, 0), new Vector(0, 64, 0), new Vector(0, 64.62, 0), new Vector(0.8, 0, 0),
                3.0, 1.5, 0.5);
        assertTrue(running.getX() > standing.getX(), "forward sprint should add to knockback");
    }

    @Test
    void explosionKnockbackSamePositionBlastPushesUp() {
        Vector vel = FireballUtil.computeExplosionKnockback(
                new Vector(0, 64, 0), new Vector(0, 64, 0), new Vector(0, 64.62, 0), new Vector(0, 0, 0),
                3.0, 1.5, 0.0);
        assertTrue(vel.getY() > 0, "degenerate blast at feet resolves upward, not NaN");
        assertFalse(Double.isNaN(vel.getX()) || Double.isNaN(vel.getY()) || Double.isNaN(vel.getZ()));
    }

    @Test
    void explosionKnockbackClampsToPaperLimit() {
        Vector vel = FireballUtil.computeExplosionKnockback(
                new Vector(0.1, 64.05, 0), new Vector(0, 64, 0), new Vector(0, 64.62, 0), new Vector(0, 0, 0),
                3.0, 10.0, 0.0);
        assertTrue(Math.abs(vel.getY()) <= 4.0, "no axis over Paper limit, y=" + vel.getY());
        assertTrue(Math.abs(vel.getX()) <= 4.0);
    }

    @Test
    void explosionKnockbackRejectsInvalidInput() {
        Vector v = new Vector(1, 0, 0);
        assertThrows(IllegalArgumentException.class, () -> FireballUtil.computeExplosionKnockback(null, v, v, v, 3, 1, 0.5));
        assertThrows(IllegalArgumentException.class, () -> FireballUtil.computeExplosionKnockback(v, null, v, v, 3, 1, 0.5));
        assertThrows(IllegalArgumentException.class, () -> FireballUtil.computeExplosionKnockback(v, v, null, v, 3, 1, 0.5));
        assertThrows(IllegalArgumentException.class, () -> FireballUtil.computeExplosionKnockback(v, v, v, v, 0, 1, 0.5));
        assertThrows(IllegalArgumentException.class, () -> FireballUtil.computeExplosionKnockback(v, v, v, v, 3, -1, 0.5));
    }
}
