package com.courtquestpro

import org.junit.Assert.*
import org.junit.Test

class PlayerCardRecordTest {
    @Test fun shootingUsesOnlyRecordedMakeAttemptsAndKeepsFrozenVocabulary() {
        val controlled = MissionMeasurement("drill-actions-v2",1,"Controlled shots","Shots attempted","+ CONTROLLED","")
        val save=SaveState(totalXp=425,longestStreak=3,drillResults=mutableListOf(
            DrillResult("2026-10-01","1","s1",3,5), DrillResult("2026-10-01","2","s2",8,10),
            DrillResult("2026-10-01","3","s1",1,4,measurementSnapshot=controlled),
            DrillResult("2026-10-02","4","unknown",0,6), DrillResult("2026-10-02","5","s1",6,1)),
            matches=mutableListOf(MatchRecord("","A","B",11,7,"old name",0),MatchRecord("","A","B",3,8,"B",0)))
        val r=PlayerCardRecord.from(save)
        assertEquals(mapOf(
            "Saved drills" to "4", "Attempts" to "25", "Successful reps" to "12",
            "Shots attempted" to "5", "Shots made" to "3", "Training days" to "2",
            "Shooting accuracy" to "60%", "Completed missions" to null
        ), r.training.associate { it.label to it.value })
        assertEquals(listOf("2","1 / 1"),r.games.map{it.value})
        assertEquals(listOf("425",rankFor(425),"3 d"),r.progress.map{it.value})
    }
    @Test fun missingHistoryAndRecordedZeroAreDistinct() {
        val empty=PlayerCardRecord.from(SaveState())
        assertTrue((empty.training+empty.games).all{it.value==null})
        assertEquals("0",empty.progress.first().value)
        assertFalse(empty.hasRecordedActivity)
        val recorded=PlayerCardRecord.from(SaveState(drillResults=mutableListOf(DrillResult("","s","s1",0,9))))
        assertEquals("0%",recorded.training.first{it.label=="Shooting accuracy"}.value)
        assertEquals("0",recorded.training.first{it.label=="Successful reps"}.value)
        assertEquals("9",recorded.training.first{it.label=="Shots attempted"}.value)
        assertEquals("0",recorded.training.first{it.label=="Shots made"}.value)
        assertTrue(recorded.hasRecordedActivity)
    }
}
