package net.perfectdreams.loritta.cinnamon.dashboard.frontend.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.browser.window
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.LorittaDashboardFrontend
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.screen.Screen
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.SVGIconManager
import net.perfectdreams.loritta.cinnamon.pudding.data.CachedUserInfo
import net.perfectdreams.loritta.cinnamon.pudding.data.UserId
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.attributes.builders.InputAttrsScope
import org.jetbrains.compose.web.attributes.placeholder
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Input

@Composable
fun DiscordUserInput(
    m: LorittaDashboardFrontend,
    i18nContext: I18nContext,
    screen: Screen,
    attrsScope: InputAttrsScope<String>.() -> (Unit),
    queriedUserResult: (CachedUserInfo?) -> (Unit)
) {
    var state by remember { mutableStateOf<DiscordUserInputState>(DiscordUserInputState.Loading) }
    var parseResult by remember { mutableStateOf<DiscordUserInputResult>(DiscordUserInputResult.Empty) }
    var job by remember { mutableStateOf<Job?>(null) }

    Input(
        InputType.Text
    ) {
        attrsScope.invoke(this)

        placeholder("Preencha com a Tag do Usuário (exemplo: MrPowerGamerBR#4185) ou ID do Usuário (exemplo: 123170274651668480)")

        onInput {
            // Validate input
            val value = it.value

            val result = DiscordUserInputResult.parse(value)
            parseResult = result

            queriedUserResult.invoke(null)

            if (result is DiscordUserInputResult.DiscordParseSuccess) {
                // Cancel previous job
                job?.cancel()
                // Start a new one!
                job = screen.launch {
                    state = DiscordUserInputState.Loading

                    // Wait one second before querying user, to avoid spamming the API
                    delay(1_000)

                    // Query the database
                    val response = m.http.get("${window.location.origin}/api/v1/users/search") {
                        when (result) {
                            is DiscordUserInputResult.DiscordIdInput -> parameter("id", result.userId.value.toString())
                            is DiscordUserInputResult.DiscordTagInput -> parameter("tag", result.tag)
                        }
                    }

                    if (response.status == HttpStatusCode.NotFound) {
                        println("Usuário desconhecido!")
                        queriedUserResult.invoke(null)
                        state = DiscordUserInputState.UnknownUser
                    } else {
                        val foundUser = Json.decodeFromString<CachedUserInfo>(response.bodyAsText())
                        println("Usuário... conhecido!")
                        println(foundUser)

                        val success = DiscordUserInputState.Success(foundUser)
                        state = success
                        queriedUserResult.invoke(success.user)
                    }
                }
            }
        }
    }

    if (parseResult is DiscordUserInputResult.DiscordParseSuccess) {
        when (val state = state) {
            DiscordUserInputState.Loading -> {
                ValidationMessage(ValidationMessageStatus.NEUTRAL) {
                    InlineLoadingSection(i18nContext)
                }
            }
            is DiscordUserInputState.Success -> {
                ValidationMessage(ValidationMessageStatus.SUCCESS) {
                    Div {
                        InlineUserDisplay(state.user)
                    }
                }
            }
            DiscordUserInputState.UnknownUser -> {
                ValidationMessageWithIcon(
                    ValidationMessageStatus.ERROR,
                    SVGIconManager.exclamationTriangle,
                    "Usuário desconhecido!"
                )
            }
        }
    } else {
        ValidationMessageWithIcon(
            ValidationMessageStatus.ERROR,
            SVGIconManager.exclamationTriangle,
            when (parseResult) {
                is DiscordUserInputResult.DiscordIdInput -> error("This should never happen!")
                is DiscordUserInputResult.DiscordTagInput -> error("This should never happen!")
                DiscordUserInputResult.Empty -> "Preencha com a Tag do Usuário (exemplo: MrPowerGamerBR#4185) ou ID do Usuário (exemplo: 123170274651668480)"
                DiscordUserInputResult.InvalidDiscriminator -> "Discriminator inválido!!"
                DiscordUserInputResult.MissingDiscriminator -> "Cadê o discriminador nn sei"
            }
        )
    }
}

sealed class DiscordUserInputResult {
    companion object {
        fun parse(input: String): DiscordUserInputResult {
            if (input.isBlank())
                return Empty

            val valueAsLong = input.toLongOrNull()
            if (valueAsLong == null) {
                val discriminator = input.substringAfter("#", missingDelimiterValue = "")

                if (discriminator.isBlank()) {
                    return MissingDiscriminator
                }

                if (discriminator.length != 4)
                    return InvalidDiscriminator

                val discriminatorAsInt = discriminator.toIntOrNull()
                if (discriminatorAsInt == null || discriminatorAsInt !in 1..9999)
                    return InvalidDiscriminator

                return DiscordTagInput(
                    input
                        .removePrefix("@")
                ) // If there is a "@" in the beginning, the user may have inserted it by mistake
            }

            return DiscordIdInput(UserId(valueAsLong))
        }
    }

    object Empty : DiscordUserInputResult()
    object MissingDiscriminator : DiscordUserInputResult()
    object InvalidDiscriminator : DiscordUserInputResult()
    sealed class DiscordParseSuccess : DiscordUserInputResult()
    class DiscordTagInput(val tag: String) : DiscordParseSuccess()
    class DiscordIdInput(val userId: UserId) : DiscordParseSuccess()
}

sealed class DiscordUserInputState {
    object Loading : DiscordUserInputState()
    class Success(val user: CachedUserInfo) : DiscordUserInputState()
    object UnknownUser : DiscordUserInputState()
}