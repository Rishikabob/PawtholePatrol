package com.example.pawtholepatrol

import android.content.Context
import android.util.Log
import com.example.pawtholepatrol.feature.geo.GeoPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object PotholeCsvStore {
    private const val CSV_FILE_NAME = "potholes.csv"

    suspend fun ensureSeeded(context: Context) = withContext(Dispatchers.IO) {
        val file = File(context.filesDir, CSV_FILE_NAME)
        if (file.exists()) return@withContext

        context.resources.openRawResource(R.raw.potholes).bufferedReader().use { reader ->
            file.outputStream().bufferedWriter().use { writer ->
                reader.forEachLine { line ->
                    val trimmed = line.trim()
                    if (trimmed.isNotEmpty()) {
                        writer.append(trimmed)
                        writer.newLine()
                    }
                }
            }
        }
        Log.d("PawtholePatrolLogs", "Seeded writable potholes.csv at ${file.absolutePath}")
    }

    suspend fun readAll(context: Context): List<GeoPoint> = withContext(Dispatchers.IO) {
        ensureSeeded(context)
        val file = File(context.filesDir, CSV_FILE_NAME)
        val result = mutableListOf<GeoPoint>()

        file.bufferedReader().useLines { lines ->
            lines.forEach { line ->
                val trimmed = line.trim()
                if (trimmed.isEmpty()) return@forEach
                val row = trimmed.split(",", limit = 2)
                if (row.size != 2) return@forEach

                val lat = row[0].toDoubleOrNull()
                val lon = row[1].toDoubleOrNull()
                if (lat != null && lon != null) {
                    result.add(GeoPoint(latitude = lat, longitude = lon))
                }
            }
        }

        result
    }

    suspend fun append(context: Context, point: GeoPoint) = withContext(Dispatchers.IO) {
        ensureSeeded(context)
        val file = File(context.filesDir, CSV_FILE_NAME)
        file.appendText("${point.latitude},${point.longitude}\n")
    }

    suspend fun replaceAll(context: Context, points: List<GeoPoint>) = withContext(Dispatchers.IO) {
        ensureSeeded(context)
        val file = File(context.filesDir, CSV_FILE_NAME)
        file.outputStream().bufferedWriter().use { writer ->
            points.forEach { point ->
                writer.append("${point.latitude},${point.longitude}")
                writer.newLine()
            }
        }
    }
}
