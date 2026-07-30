package xyz.nachaos.memosyou.data.local

import androidx.room.TypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
import xyz.nachaos.memosyou.data.model.MemoLocation
import xyz.nachaos.memosyou.data.model.MemoRelation
import xyz.nachaos.memosyou.data.model.MemoRelationRef
import xyz.nachaos.memosyou.data.model.RelationType
import xyz.nachaos.memosyou.data.model.MemoVisibility
import java.time.Instant

class Converters {
    private val json = Json { ignoreUnknownKeys = true }

    @TypeConverter
    fun fromTimestamp(value: Long?): Instant? {
        return value?.let { Instant.ofEpochMilli(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Instant?): Long? {
        return date?.toEpochMilli()
    }

    @TypeConverter
    fun toMemoVisibility(value: String) = enumValueOf<MemoVisibility>(value)

    @TypeConverter
    fun fromMemoVisibility(value: MemoVisibility) = value.name

    @TypeConverter
    fun fromRelationType(value: RelationType): String = value.name

    @TypeConverter
    fun toRelationType(value: String): RelationType = enumValueOf(value)

    @TypeConverter
    fun fromMemoRelationRef(ref: MemoRelationRef?): String? {
        return ref?.let {
            json.encodeToString(buildJsonObject {
                put("name", it.name)
                put("snippet", it.snippet)
            })
        }
    }

    @TypeConverter
    fun toMemoRelationRef(value: String?): MemoRelationRef? {
        return value?.let {
            val obj = json.parseToJsonElement(it) as JsonObject
            MemoRelationRef(
                name = obj["name"]?.toString()?.trim('"') ?: "",
                snippet = obj["snippet"]?.toString()?.trim('"') ?: ""
            )
        }
    }

    @TypeConverter
    fun fromMemoRelation(relation: MemoRelation?): String? {
        return relation?.let {
            json.encodeToString(buildJsonObject {
                putJsonObject("memo") {
                    put("name", it.memo.name)
                    put("snippet", it.memo.snippet)
                }
                putJsonObject("relatedMemo") {
                    put("name", it.relatedMemo.name)
                    put("snippet", it.relatedMemo.snippet)
                }
                put("type", it.type.name)
            })
        }
    }

    @TypeConverter
    fun toMemoRelation(value: String?): MemoRelation? {
        return value?.let {
            val obj = json.parseToJsonElement(it) as JsonObject
            MemoRelation(
                memo = MemoRelationRef(
                    name = obj["memo"]?.let { it as JsonObject }?.get("name")?.toString()?.trim('"') ?: "",
                    snippet = obj["memo"]?.let { it as JsonObject }?.get("snippet")?.toString()?.trim('"') ?: ""
                ),
                relatedMemo = MemoRelationRef(
                    name = obj["relatedMemo"]?.let { it as JsonObject }?.get("name")?.toString()?.trim('"') ?: "",
                    snippet = obj["relatedMemo"]?.let { it as JsonObject }?.get("snippet")?.toString()?.trim('"') ?: ""
                ),
                type = obj["type"]?.toString()?.trim('"')?.let { typeName ->
                    try { enumValueOf<RelationType>(typeName) } catch (e: Exception) { RelationType.UNKNOWN }
                } ?: RelationType.UNKNOWN
            )
        }
    }

    @TypeConverter
    fun fromMemoRelationList(relations: List<MemoRelation>?): String? {
        return relations?.let { json.encodeToString(it) }
    }

    @TypeConverter
    fun toMemoRelationList(value: String?): List<MemoRelation>? {
        return value?.let {
            try {
                json.decodeFromString<List<MemoRelation>>(it)
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    @TypeConverter
    fun fromMemoLocation(location: MemoLocation?): String? {
        return location?.let {
            json.encodeToString(buildJsonObject {
                put("placeholder", it.placeholder)
                put("latitude", it.latitude)
                put("longitude", it.longitude)
            })
        }
    }

    @TypeConverter
    fun toMemoLocation(value: String?): MemoLocation? {
        return value?.let {
            val obj = json.parseToJsonElement(it) as JsonObject
            MemoLocation(
                placeholder = obj["placeholder"]?.toString()?.trim('"'),
                latitude = obj["latitude"]?.toString()?.toDoubleOrNull(),
                longitude = obj["longitude"]?.toString()?.toDoubleOrNull()
            )
        }
    }
} 