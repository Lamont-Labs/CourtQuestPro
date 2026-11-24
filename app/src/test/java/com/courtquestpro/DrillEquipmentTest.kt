package com.courtquestpro

import org.junit.Assert.*
import org.junit.Test

class DrillEquipmentTest {
    @Test fun everyBuiltInHasEquipment() {
        assertTrue(drills.all { DrillEquipment.label(it.id) != null })
    }
    @Test fun equipmentIsSearchable() {
        assertTrue(DrillDiscovery.matches(drills.first { it.id == "s1" }, "hoop"))
        assertTrue(DrillDiscovery.matches(drills.first { it.id == "p2" }, "wall"))
    }
    @Test fun customEquipmentIsNotInferred() {
        assertNull(DrillEquipment.label("custom_example"))
    }
}
