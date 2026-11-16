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
}
