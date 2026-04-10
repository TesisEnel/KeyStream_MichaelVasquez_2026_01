package com.phantomshard.keystream.data.common

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import retrofit2.Response

object ApiErrorMapper {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    fun <T> toUserMessage(response: Response<T>): String {
        val statusCode = response.code()
        val rawBody = response.errorBody()?.string().orEmpty()
        val apiErrorCode = extractApiErrorCode(rawBody)

        return when {
            statusCode == 401 && apiErrorCode == "MISSING_API_KEY" ->
                "No se proporciono la API key en el header X-API-Key."
            statusCode == 401 && apiErrorCode == "INVALID_API_KEY" ->
                "La API key no fue encontrada o fue revocada."
            statusCode == 401 && apiErrorCode == "EXPIRED_API_KEY" ->
                "La API key ha superado su fecha de expiracion."
            statusCode == 403 && apiErrorCode == "INSUFFICIENT_SCOPE" ->
                "La API key no tiene el alcance requerido para esta accion."
            apiErrorCode != null ->
                "Error HTTP $statusCode ($apiErrorCode). ${meaningForStatus(statusCode)}"
            else ->
                "Error HTTP $statusCode. ${meaningForStatus(statusCode)}"
        }
    }

    private fun extractApiErrorCode(rawBody: String): String? {
        if (rawBody.isBlank()) return null

        return runCatching {
            findCode(json.parseToJsonElement(rawBody))
        }.getOrNull()
    }

    private fun findCode(element: JsonElement): String? {
        return when (element) {
            is JsonObject -> {
                val directCode = (element["code"] as? JsonPrimitive)?.content
                if (!directCode.isNullOrBlank()) {
                    directCode
                } else {
                    element.values.firstNotNullOfOrNull(::findCode)
                }
            }
            else -> null
        }
    }

    private fun meaningForStatus(statusCode: Int): String {
        return when (statusCode) {
            400 -> "La solicitud no es valida."
            401 -> "La autenticacion fallo o falta una credencial valida."
            403 -> "El servidor entendio la solicitud, pero no autoriza esta operacion."
            404 -> "El recurso solicitado no existe."
            405 -> "El metodo HTTP usado no esta permitido para este endpoint."
            408 -> "El servidor tardo demasiado en responder."
            409 -> "La solicitud entra en conflicto con el estado actual del recurso."
            422 -> "El servidor recibio la solicitud, pero no pudo procesarla por datos invalidos."
            429 -> "Se excedio el limite de solicitudes permitidas."
            500 -> "Ocurrio un error interno del servidor."
            501 -> "El servidor no soporta esta operacion."
            502 -> "El servidor recibio una respuesta invalida de otro servicio."
            503 -> "El servicio no esta disponible temporalmente."
            504 -> "El servidor agoto el tiempo esperando otro servicio."
            else -> "Ocurrio un error desconocido en el servidor."
        }
    }
}
