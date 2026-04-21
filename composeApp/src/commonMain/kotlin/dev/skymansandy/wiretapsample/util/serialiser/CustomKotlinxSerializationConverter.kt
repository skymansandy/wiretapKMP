/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretapsample.util.serialiser

import io.ktor.http.ContentType
import io.ktor.http.content.OutgoingContent
import io.ktor.serialization.ContentConverter
import io.ktor.serialization.JsonConvertException
import io.ktor.serialization.kotlinx.KotlinxSerializationConverter
import io.ktor.util.reflect.TypeInfo
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.charsets.Charset
import io.ktor.utils.io.readRemaining
import kotlinx.io.readString
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import kotlinx.serialization.serializerOrNull

internal class CustomKotlinxSerializationConverter(
    private val json: Json,
) : ContentConverter {

    private val defaultConverter = KotlinxSerializationConverter(json)

    override suspend fun serialize(
        contentType: ContentType,
        charset: Charset,
        typeInfo: TypeInfo,
        value: Any?,
    ): OutgoingContent = defaultConverter.serialize(contentType, charset, typeInfo, value)

    @OptIn(InternalSerializationApi::class, ExperimentalSerializationApi::class)
    override suspend fun deserialize(charset: Charset, typeInfo: TypeInfo, content: ByteReadChannel): Any? {
        val serializer = typeInfo.kotlinType?.let { type ->
            if (type.arguments.isEmpty()) null
            else json.serializersModule.serializerOrNull(type)
        } ?: json.serializersModule.getContextual(typeInfo.type)?.maybeNullable(typeInfo)
            ?: typeInfo.type.serializer().maybeNullable(typeInfo)

        val parsingBlock = suspend {
            val jsonText = content.readRemaining().readString()
            val jsonElement = json.parseToJsonElement(jsonText)
            json.decodeFromJsonElement(serializer, jsonElement)
        }

        return try {
            parsingBlock()
        } catch (cause: Throwable) {
            throw JsonConvertException("Illegal input: ${cause.message}", cause)
        }
    }

    private fun <T : Any> KSerializer<T>.maybeNullable(typeInfo: TypeInfo): KSerializer<*> =
        if (typeInfo.kotlinType?.isMarkedNullable == true) this.nullable else this
}
