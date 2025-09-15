package com.example.vehicleequalizernative

data class CanMessage(val id: Int, // Identificador da mensagem CAN (ex: 0x123 para volume)
                      val data: ByteArray // Dados da mensagem (ex: nível de volume))
                    )
{
    // Sobrescreve equals e hashCode para que a comparação de ByteArray
    //funcione corretamente
    override fun equals(other: Any?): Boolean
    {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CanMessage

        if (id != other.id) return false
        if (!data.contentEquals(other.data)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + data.contentHashCode()
        return result
    }


}