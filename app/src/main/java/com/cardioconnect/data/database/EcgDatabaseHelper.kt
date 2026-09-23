package com.cardioconnect.data.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.cardioconnect.domain.model.EcgEventMarker
import com.cardioconnect.domain.model.EcgLead
import com.cardioconnect.domain.model.EcgSession
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.nio.ByteBuffer
import java.nio.ByteOrder

class EcgDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    private val gson = Gson()

    companion object {
        private const val DATABASE_NAME = "cardioconnect.db"
        private const val DATABASE_VERSION = 1

        private const val TABLE_SESSIONS = "ecg_sessions"
        private const val COL_ID = "id"
        private const val COL_TITLE = "title"
        private const val COL_START_TIME = "start_time"
        private const val COL_DURATION = "duration_seconds"
        private const val COL_AVG_HR = "avg_hr"
        private const val COL_MIN_HR = "min_hr"
        private const val COL_MAX_HR = "max_hr"
        private const val COL_LEAD = "lead"
        private const val COL_DEVICE_NAME = "device_name"
        private const val COL_SPO2 = "spo2"
        private const val COL_BP_SYS = "bp_sys"
        private const val COL_BP_DIA = "bp_dia"
        private const val COL_TEMP = "temperature"
        private const val COL_IS_DEMO = "is_demo"
        private const val COL_MARKERS_JSON = "markers_json"
        private const val COL_SAMPLES_BLOB = "samples_blob"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createQuery = """
            CREATE TABLE $TABLE_SESSIONS (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_TITLE TEXT NOT NULL,
                $COL_START_TIME INTEGER NOT NULL,
                $COL_DURATION INTEGER NOT NULL,
                $COL_AVG_HR INTEGER NOT NULL,
                $COL_MIN_HR INTEGER NOT NULL,
                $COL_MAX_HR INTEGER NOT NULL,
                $COL_LEAD TEXT NOT NULL,
                $COL_DEVICE_NAME TEXT NOT NULL,
                $COL_SPO2 INTEGER,
                $COL_BP_SYS INTEGER,
                $COL_BP_DIA INTEGER,
                $COL_TEMP REAL,
                $COL_IS_DEMO INTEGER NOT NULL,
                $COL_MARKERS_JSON TEXT,
                $COL_SAMPLES_BLOB BLOB
            )
        """.trimIndent()
        db.execSQL(createQuery)

        // Seed initial reference session for testing and immediate review
        seedInitialDemoSession(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_SESSIONS")
        onCreate(db)
    }

    private fun seedInitialDemoSession(db: SQLiteDatabase) {
        val now = System.currentTimeMillis() - (1000L * 60 * 60 * 4) // 4 hours ago
        val syntheticSamples = mutableListOf<Float>()
        // Generate 1500 points (~6 seconds of 250Hz ECG)
        for (i in 0 until 1500) {
            val phase = (i % 200) / 200.0
            val p = 0.15 * Math.exp(-Math.pow(phase - 0.20, 2.0) / 0.0008)
            val q = -0.18 * Math.exp(-Math.pow(phase - 0.35, 2.0) / 0.0001)
            val r = 1.35 * Math.exp(-Math.pow(phase - 0.38, 2.0) / 0.0002)
            val s = -0.32 * Math.exp(-Math.pow(phase - 0.41, 2.0) / 0.0002)
            val t = 0.28 * Math.exp(-Math.pow(phase - 0.65, 2.0) / 0.0025)
            syntheticSamples.add((p + q + r + s + t).toFloat())
        }

        val blob = floatListToByteArray(syntheticSamples)
        val markers = listOf(
            EcgEventMarker(now + 2000, "Deep Breath", 75),
            EcgEventMarker(now + 4500, "Marked Peak", 77)
        )

        val values = ContentValues().apply {
            put(COL_TITLE, "Baseline Clinical ECG Session")
            put(COL_START_TIME, now)
            put(COL_DURATION, 342L) // 05:42
            put(COL_AVG_HR, 76)
            put(COL_MIN_HR, 68)
            put(COL_MAX_HR, 88)
            put(COL_LEAD, EcgLead.LEAD_II.name)
            put(COL_DEVICE_NAME, "CardioConnect Pro-12")
            put(COL_SPO2, 98)
            put(COL_BP_SYS, 120)
            put(COL_BP_DIA, 80)
            put(COL_TEMP, 36.7f)
            put(COL_IS_DEMO, 1)
            put(COL_MARKERS_JSON, gson.toJson(markers))
            put(COL_SAMPLES_BLOB, blob)
        }
        db.insert(TABLE_SESSIONS, null, values)
    }

    suspend fun insertSession(session: EcgSession): Long = withContext(Dispatchers.IO) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_TITLE, session.title)
            put(COL_START_TIME, session.startTimeMs)
            put(COL_DURATION, session.durationSeconds)
            put(COL_AVG_HR, session.avgHeartRate)
            put(COL_MIN_HR, session.minHeartRate)
            put(COL_MAX_HR, session.maxHeartRate)
            put(COL_LEAD, session.lead.name)
            put(COL_DEVICE_NAME, session.deviceName)
            put(COL_SPO2, session.spo2)
            put(COL_BP_SYS, session.bpSystolic)
            put(COL_BP_DIA, session.bpDiastolic)
            put(COL_TEMP, session.temperature)
            put(COL_IS_DEMO, if (session.isDemoData) 1 else 0)
            put(COL_MARKERS_JSON, gson.toJson(session.eventMarkers))
            put(COL_SAMPLES_BLOB, floatListToByteArray(session.samplePoints))
        }
        db.insert(TABLE_SESSIONS, null, values)
    }

    fun getAllSessionsFlow(): Flow<List<EcgSession>> = callbackFlow {
        fun query(): List<EcgSession> {
            val list = mutableListOf<EcgSession>()
            val db = readableDatabase
            val cursor = db.query(
                TABLE_SESSIONS,
                null,
                null,
                null,
                null,
                null,
                "$COL_START_TIME DESC"
            )
            cursor.use {
                while (it.moveToNext()) {
                    list.add(cursorToSession(it, includeSamples = false))
                }
            }
            return list
        }

        trySend(query())
        // In a full Room architecture, changes trigger table invalidation. Here we poll or emit on mutations.
        awaitClose { }
    }.flowOn(Dispatchers.IO)

    suspend fun getAllSessionsList(): List<EcgSession> = withContext(Dispatchers.IO) {
        val list = mutableListOf<EcgSession>()
        val db = readableDatabase
        val cursor = db.query(TABLE_SESSIONS, null, null, null, null, null, "$COL_START_TIME DESC")
        cursor.use {
            while (it.moveToNext()) {
                list.add(cursorToSession(it, includeSamples = false))
            }
        }
        list
    }

    suspend fun getSessionById(sessionId: Long): EcgSession? = withContext(Dispatchers.IO) {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_SESSIONS,
            null,
            "$COL_ID = ?",
            arrayOf(sessionId.toString()),
            null,
            null,
            null
        )
        cursor.use {
            if (it.moveToFirst()) {
                cursorToSession(it, includeSamples = true)
            } else null
        }
    }

    suspend fun deleteSession(sessionId: Long) = withContext(Dispatchers.IO) {
        val db = writableDatabase
        db.delete(TABLE_SESSIONS, "$COL_ID = ?", arrayOf(sessionId.toString()))
    }

    suspend fun deleteAllSessions() = withContext(Dispatchers.IO) {
        val db = writableDatabase
        db.delete(TABLE_SESSIONS, null, null)
    }

    private fun cursorToSession(c: Cursor, includeSamples: Boolean): EcgSession {
        val id = c.getLong(c.getColumnIndexOrThrow(COL_ID))
        val title = c.getString(c.getColumnIndexOrThrow(COL_TITLE))
        val startTime = c.getLong(c.getColumnIndexOrThrow(COL_START_TIME))
        val duration = c.getLong(c.getColumnIndexOrThrow(COL_DURATION))
        val avgHr = c.getInt(c.getColumnIndexOrThrow(COL_AVG_HR))
        val minHr = c.getInt(c.getColumnIndexOrThrow(COL_MIN_HR))
        val maxHr = c.getInt(c.getColumnIndexOrThrow(COL_MAX_HR))
        val leadName = c.getString(c.getColumnIndexOrThrow(COL_LEAD))
        val deviceName = c.getString(c.getColumnIndexOrThrow(COL_DEVICE_NAME))

        val spo2 = if (c.isNull(c.getColumnIndexOrThrow(COL_SPO2))) null else c.getInt(c.getColumnIndexOrThrow(COL_SPO2))
        val bpSys = if (c.isNull(c.getColumnIndexOrThrow(COL_BP_SYS))) null else c.getInt(c.getColumnIndexOrThrow(COL_BP_SYS))
        val bpDia = if (c.isNull(c.getColumnIndexOrThrow(COL_BP_DIA))) null else c.getInt(c.getColumnIndexOrThrow(COL_BP_DIA))
        val temp = if (c.isNull(c.getColumnIndexOrThrow(COL_TEMP))) null else c.getFloat(c.getColumnIndexOrThrow(COL_TEMP))
        val isDemo = c.getInt(c.getColumnIndexOrThrow(COL_IS_DEMO)) == 1

        val markersJson = c.getString(c.getColumnIndexOrThrow(COL_MARKERS_JSON))
        val markerType = object : TypeToken<List<EcgEventMarker>>() {}.type
        val markers: List<EcgEventMarker> = if (!markersJson.isNullOrEmpty()) {
            try { gson.fromJson(markersJson, markerType) } catch (_: Exception) { emptyList() }
        } else emptyList()

        val samplePoints = if (includeSamples) {
            val blob = c.getBlob(c.getColumnIndexOrThrow(COL_SAMPLES_BLOB))
            byteArrayToFloatList(blob)
        } else emptyList()

        return EcgSession(
            id = id,
            title = title,
            startTimeMs = startTime,
            durationSeconds = duration,
            avgHeartRate = avgHr,
            minHeartRate = minHr,
            maxHeartRate = maxHr,
            lead = try { EcgLead.valueOf(leadName) } catch (_: Exception) { EcgLead.LEAD_II },
            deviceName = deviceName,
            spo2 = spo2,
            bpSystolic = bpSys,
            bpDiastolic = bpDia,
            temperature = temp,
            isDemoData = isDemo,
            eventMarkers = markers,
            samplePoints = samplePoints
        )
    }

    private fun floatListToByteArray(floats: List<Float>): ByteArray {
        val buffer = ByteBuffer.allocate(floats.size * 4).order(ByteOrder.LITTLE_ENDIAN)
        floats.forEach { buffer.putFloat(it) }
        return buffer.array()
    }

    private fun byteArrayToFloatList(bytes: ByteArray?): List<Float> {
        if (bytes == null || bytes.isEmpty()) return emptyList()
        val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
        val count = bytes.size / 4
        val result = ArrayList<Float>(count)
        for (i in 0 until count) {
            result.add(buffer.float)
        }
        return result
    }
}
