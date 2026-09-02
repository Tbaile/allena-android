package it.bailettitommaso.allena.data.local.db

import it.bailettitommaso.allena.domain.model.Tag
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
private data class CachedTag(val id: Long, val name: String, val slug: String)

/** Serializes exercise tags to/from JSON for storage in [ExerciseEntity.tagsJson]. */
object TagsJsonConverter {
    fun toJson(tags: List<Tag>): String =
        Json.encodeToString(tags.map { CachedTag(it.id, it.name, it.slug) })

    fun fromJson(json: String): List<Tag> =
        Json.decodeFromString<List<CachedTag>>(json).map { Tag(it.id, it.name, it.slug) }
}
