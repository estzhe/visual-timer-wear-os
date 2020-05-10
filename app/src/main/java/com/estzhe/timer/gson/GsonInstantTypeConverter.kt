package com.estzhe.timer.gson

import com.google.gson.*
import java.lang.reflect.Type
import java.time.Instant

class GsonInstantTypeConverter : JsonSerializer<Instant>, JsonDeserializer<Instant>
{
    override fun serialize(
        src: Instant,
        typeOfSrc: Type,
        context: JsonSerializationContext): JsonElement
    {
        return JsonPrimitive(src.toEpochMilli())
    }

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext): Instant
    {
        return Instant.ofEpochMilli(json.asLong)
    }
}