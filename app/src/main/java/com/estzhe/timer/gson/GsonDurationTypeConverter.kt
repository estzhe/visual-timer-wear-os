package com.estzhe.timer.gson

import com.google.gson.*
import java.lang.reflect.Type
import java.time.Duration

class GsonDurationTypeConverter : JsonSerializer<Duration>, JsonDeserializer<Duration>
{
    override fun serialize(
        src: Duration,
        typeOfSrc: Type,
        context: JsonSerializationContext): JsonElement
    {
        return JsonPrimitive(src.toMillis())
    }

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext): Duration
    {
        return Duration.ofMillis(json.asLong)
    }
}