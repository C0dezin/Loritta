package net.perfectdreams.loritta.cinnamon.discord.utils.correios.entities

import kotlinx.datetime.Instant
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

@Serializable
data class CorreiosResponse(val objetos: List<CorreiosObjeto>)

@Serializable(CorreiosObjeto.Serializer::class)
sealed class CorreiosObjeto {
    abstract val codObjeto: String

    object Serializer : JsonContentPolymorphicSerializer<CorreiosObjeto>(CorreiosObjeto::class) {
        override fun selectDeserializer(element: JsonElement) = when {
            element.jsonObject.containsKey("mensagem") && element.jsonObject["mensagem"]?.jsonPrimitive?.content == "SRO-020: Objeto não encontrado na base de dados dos Correios." -> CorreiosUnknownObjeto.serializer()
            else -> CorreiosFoundObjeto.serializer()
        }
    }
}

@Serializable
data class CorreiosFoundObjeto(
    override val codObjeto: String,
    @SerialName("eventos")
    val events: List<CorreiosEvento>
) : CorreiosObjeto()

@Serializable
data class CorreiosUnknownObjeto(override val codObjeto: String) : CorreiosObjeto()

@Serializable
data class CorreiosEvento(
    @Serializable(EventType.Serializer::class)
    val codigo: EventType,
    val tipo: String,
    @Serializable(CorreiosCreationDateSerializer::class)
    val dtHrCriado: Instant,
    val descricao: String,
    val detalhe: String? = null,
    val recebedor: JsonObject? = null,
    val unidade: CorreiosUnidade,
    val postagem: CorreiosPostagem? = null,
    val destino: List<CorreiosDestino>? = null,
    val cepDestino: Int? = null,
    val prazoGuarda: Int? = null,
    val diasUteis: Int? = null,
    val dataPostagem: String? = null,
    val detalheOEC: CorreiosDetalheOEC? = null
)

@Serializable
data class CorreiosDetalheOEC(
    val carteiro: String,
    val distrito: String,
    val lista: String,
    val unidade: String
)

@Serializable(CorreiosUnidade.Serializer::class)
sealed class CorreiosUnidade {
    object Serializer : JsonContentPolymorphicSerializer<CorreiosUnidade>(CorreiosUnidade::class) {
        override fun selectDeserializer(element: JsonElement) = when {
            element.jsonObject["tipo"]!!.jsonPrimitive.content == "País" -> CorreiosUnidadeExterior.serializer()
            else -> CorreiosUnidadeBrasil.serializer()
        }
    }

    abstract val codSro: String?
    abstract val tipo: String
}

@Serializable
data class CorreiosUnidadeBrasil(
    override val codSro: String? = null,
    override val tipo: String,
    val endereco: CorreiosEnderecoBrasil
) : CorreiosUnidade()

@Serializable
data class CorreiosUnidadeExterior(
    override val codSro: String? = null,
    override val tipo: String,
    val endereco: CorreiosEnderecoExterior
) : CorreiosUnidade()

@Serializable
data class CorreiosDestino(
    val local: String,
    val codigo: String,
    val cidade: String? = null,
    val uf: String,
    val bairro: String? = null,
    val endereco: CorreiosEnderecoBrasil
)

@Serializable
data class CorreiosEnderecoBrasil(
    val cep: String? = null,
    val logradouro: String? = null,
    val complemento: String? = null,
    val numero: String? = null,
    val cidade: String? = null,
    val uf: String,
    val bairro: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null
)

@Serializable
data class CorreiosEnderecoExterior(
    val codigo: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null
)

@Serializable
data class CorreiosPostagem(
    val cepdestino: Int?,
    val ar: String,
    val mp: String,
    val dh: String,
    val peso: Double,
    val volume: Double,
    val dataprogramada: String,
    val datapostagem: String,
    val prazotratamento: Int
)

@Serializable(with = EventType.Serializer::class)
public sealed class EventType(public val value: String) {
    public class Unknown(value: String) : EventType(value) {
        override fun getStatusById(status: Int) = StatusType.Unknown(status)
    }

    public object PackagePosted : EventType("PO") {
        object PackagePosted : StatusType(1)

        override fun getStatusById(status: Int) = when (status) {
            PackagePosted.status -> PackagePosted
            else -> StatusType.Unknown(status)
        }
    }

    // Not really sure the difference between both
    public object PackageInTransitFromTreatmentUnitToDistributionUnit : EventType("RO") {
        // Objeto em trânsito - por favor aguarde
        object PackageInTransit : StatusType(1)

        override fun getStatusById(status: Int) = when (status) {
            PackageInTransit.status -> PackageInTransit
            else -> StatusType.Unknown(status)
        }
    }

    public object PackageInTransitToTreatmentUnit : EventType("DO") {
        // Objeto em trânsito - por favor aguarde
        object PackageInTransit : StatusType(1)

        override fun getStatusById(status: Int) = when (status) {
            PackageInTransit.status -> PackageInTransit
            else -> StatusType.Unknown(status)
        }
    }

    // Objeto saiu para entrega ao destinatário
    public object PackageInDeliveryRouteToRecipient : EventType("OEC") {
        // Objeto saiu para entrega ao destinatário
        object PackageInDeliveryRouteToRecipient : StatusType(1)

        override fun getStatusById(status: Int) = when (status) {
            PackageInDeliveryRouteToRecipient.status -> PackageInDeliveryRouteToRecipient
            else -> StatusType.Unknown(status)
        }
    }

    public object PackageDeliveredToRecipient : EventType("BDE") {
        // Objeto entregue ao destinatário
        object PackageDeliveredToRecipient : StatusType(1)

        override fun getStatusById(status: Int) = when (status) {
            PackageDeliveredToRecipient.status -> PackageDeliveredToRecipient
            else -> StatusType.Unknown(status)
        }
    }

    // Objeto ainda não chegou à unidade
    public object ObjectHasNotYetArrivedAtTheUnit : EventType("BDE") {
        override fun getStatusById(status: Int) = StatusType.Unknown(status)
    }

    // This can be two things:
    // O endereço indicado para entrega contém inconsistências. Poderá ocorrer atraso ou devolução ao remetente
    // Corrigimos um equívoco no encaminhamento do seu objeto. Por favor aguarde
    public object IssuesInPackageDelivery : EventType("FC") {
        // Objeto em correção de rota
        object PackageInRouteCorrection : StatusType(3)
        // Objeto não entregue - endereço incorreto
        object PackageNotDeliveredIncorrectAddress : StatusType(4)

        override fun getStatusById(status: Int) = when (status) {
            PackageInRouteCorrection.status -> PackageInRouteCorrection
            PackageNotDeliveredIncorrectAddress.status -> PackageNotDeliveredIncorrectAddress
            else -> StatusType.Unknown(status)
        }
    }

    // This can be two things:
    // Fiscalização aduaneira finalizada
    // Objeto recebido pelos Correios do Brasil
    // Objeto recebido na unidade de exportação no país de origem
    public object ExternalPackageUpdate : EventType("PAR") {
        // Objeto recebido na unidade de exportação no país de origem
        object PackageReceivedInTheExportFacility : StatusType(18)
        // Objeto recebido pelos Correios do Brasil
        object PackageReceivedByCorreiosBrasil : StatusType(16)
        // Encaminhado para fiscalização aduaneira
        object ForwardedForCustomsInspection : StatusType(21)
        // Aguardando pagamento
        object WaitingForPayment : StatusType(17)
        // Pagamento confirmado
        object PaymentConfirmed : StatusType(31)
        // Fiscalização aduaneira finalizada
        object CustomsInspectionFinished : StatusType(10)

        override fun getStatusById(status: Int) = when (status) {
            PackageReceivedInTheExportFacility.status -> PackageReceivedInTheExportFacility
            PackageReceivedByCorreiosBrasil.status -> PackageReceivedByCorreiosBrasil
            ForwardedForCustomsInspection.status -> ForwardedForCustomsInspection
            WaitingForPayment.status -> WaitingForPayment
            PaymentConfirmed.status -> PaymentConfirmed
            CustomsInspectionFinished.status -> CustomsInspectionFinished
            else -> StatusType.Unknown(status)
        }
    }

    abstract fun getStatusById(status: Int): StatusType

    internal object Serializer : KSerializer<EventType> {
        override val descriptor: SerialDescriptor
            get() = PrimitiveSerialDescriptor("type", PrimitiveKind.INT)

        override fun deserialize(decoder: Decoder): EventType = when (val value = decoder.decodeString()) {
            PackagePosted.value -> PackagePosted
            PackageDeliveredToRecipient.value -> PackageDeliveredToRecipient
            PackageInTransitFromTreatmentUnitToDistributionUnit.value -> PackageInTransitFromTreatmentUnitToDistributionUnit
            PackageInTransitToTreatmentUnit.value -> PackageInTransitToTreatmentUnit
            PackageInDeliveryRouteToRecipient.value -> PackageInDeliveryRouteToRecipient
            IssuesInPackageDelivery.value -> IssuesInPackageDelivery
            ExternalPackageUpdate.value -> ExternalPackageUpdate

            else -> Unknown(value)
        }

        override fun serialize(encoder: Encoder, value: EventType) {
            encoder.encodeString(value.value)
        }
    }
}

public sealed class StatusType(public val status: Int) {
    public class Unknown(value: Int) : StatusType(value)
}

val CorreiosEvento.eventTypeWithStatus
    get() = EventTypeWithStatus(
        this.codigo,
        this.codigo.getStatusById(this.tipo.toInt())
    )

data class EventTypeWithStatus(val event: EventType, val status: StatusType)

private object CorreiosCreationDateSerializer : KSerializer<Instant> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("CorreiosCreationDateSerializer", PrimitiveKind.STRING)

    // Example: 2022-11-18T14:31:50

    override fun serialize(encoder: Encoder, value: Instant) {
        // Actually this could be ".removeSuffix" but we made some mistakes and there are some serialized objects that have "*timestampHere*ZZ"
        encoder.encodeString(value.toString().replace("Z", ""))
    }

    override fun deserialize(decoder: Decoder): Instant {
        val input = decoder.decodeString()
        // Correios does not include the "Z" at the end of the input, but if they end up including it in the future, then we are already "future-proofed" (sort of)
        // The real reason: We mistakenly included the "Z" when serializing, when in reality it shouldn't be included since Correios does not include it, whoops!
        return Instant.parse((input.replace("Z", "")) + "Z")
    }
}