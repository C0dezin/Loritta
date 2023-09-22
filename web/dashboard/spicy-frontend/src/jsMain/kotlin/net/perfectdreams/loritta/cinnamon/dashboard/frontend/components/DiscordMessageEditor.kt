package net.perfectdreams.loritta.cinnamon.dashboard.frontend.components

import androidx.compose.runtime.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.dashboard.common.responses.GetUserIdentificationResponse
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.LorittaDashboardFrontend
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.lorilike.FieldWrapper
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.*
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.discordcdn.DiscordCdn
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.discordcdn.Image
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.embeds.MutableDiscordMessage
import net.perfectdreams.loritta.common.utils.embeds.DiscordComponent
import net.perfectdreams.loritta.common.utils.embeds.DiscordEmbed
import net.perfectdreams.loritta.common.utils.embeds.DiscordMessage
import net.perfectdreams.loritta.common.utils.placeholders.JoinMessagePlaceholders
import net.perfectdreams.loritta.common.utils.placeholders.LeaveMessagePlaceholders
import net.perfectdreams.loritta.common.utils.placeholders.PlaceholderSectionType
import net.perfectdreams.loritta.common.utils.placeholders.SectionPlaceholders
import net.perfectdreams.loritta.serializable.DiscordGuild
import net.perfectdreams.loritta.serializable.DiscordUser
import net.perfectdreams.loritta.serializable.dashboard.requests.DashGuildScopedRequest
import net.perfectdreams.loritta.serializable.dashboard.requests.LorittaDashboardRPCRequest
import net.perfectdreams.loritta.serializable.dashboard.responses.DashGuildScopedResponse
import net.perfectdreams.loritta.serializable.dashboard.responses.LorittaDashboardRPCResponse
import org.jetbrains.compose.web.attributes.disabled
import org.jetbrains.compose.web.dom.*
import kotlin.random.Random

val JsonForDiscordMessages = Json {
    prettyPrint = true
    ignoreUnknownKeys = true
}

@Composable
fun DiscordMessageEditor(
    m: LorittaDashboardFrontend,
    i18nContext: I18nContext,
    templates: List<LorittaMessageTemplate>?,
    placeholderSectionType: PlaceholderSectionType,
    targetGuild: DiscordGuild,
    targetChannel: TargetChannelResult,
    exampleUser: GetUserIdentificationResponse,
    selfUser: DiscordUser,
    messagesToBeRenderedBeforeTargetMessage: List<DiscordMessageWithAuthor>,
    messagesToBeRenderedAfterTargetMessage: List<DiscordMessageWithAuthor>,
    rawMessage: String,
    onMessageContentChange: (String) -> (Unit)
) {
    var editorType by remember { mutableStateOf(EditorType.INTERACTIVE) }

    Div(attrs = {
        classes("message-editor")
    }) {
        val parsedMessage = try {
            JsonForDiscordMessages.decodeFromString<DiscordMessage>(rawMessage)
        } catch (e: SerializationException) {
            null
        }
        val mutableMessage = MutableDiscordMessage(
            parsedMessage ?: DiscordMessage(
                content = rawMessage
            ),
            onMessageContentChange
        )

        Div(attrs = {
            classes("message-editor-buttons")
        }) {
            DiscordButton(
                DiscordButtonType.PRIMARY,
                attrs = {
                    if (templates != null) {
                        onClick {
                            m.globalState.openCloseOnlyModal(
                                "Templates de Mensagens",
                                true
                            ) {
                                Text("Sem criatividade? Então pegue um template!")

                                VerticalList {
                                    for (template in templates) {
                                        DiscordButton(
                                            DiscordButtonType.PRIMARY,
                                            attrs = {
                                                onClick {
                                                    m.globalState.openModalWithCloseButton(
                                                        "Você realmente quer substituir?",
                                                        true,
                                                        {
                                                            Text(
                                                                "Ao aplicar o template, a sua mensagem atual será perdida! A não ser se você tenha copiado ela para outro lugar, aí vida que segue né."
                                                            )
                                                        },
                                                        { modal ->
                                                            DiscordButton(
                                                                DiscordButtonType.PRIMARY,
                                                                attrs = {
                                                                    onClick {
                                                                        when (template) {
                                                                            is LorittaMessageTemplate.LorittaDiscordMessageTemplate -> {
                                                                                onMessageContentChange.invoke(
                                                                                    JsonForDiscordMessages.encodeToString(
                                                                                        template.message
                                                                                    )
                                                                                )
                                                                            }

                                                                            is LorittaMessageTemplate.LorittaRawMessageTemplate -> onMessageContentChange.invoke(
                                                                                template.content
                                                                            )
                                                                        }
                                                                        modal.close()
                                                                        m.globalState.showToast(
                                                                            Toast.Type.SUCCESS,
                                                                            "Template importado!"
                                                                        )
                                                                    }
                                                                }
                                                            ) {
                                                                Text("Aplicar")
                                                            }
                                                        }
                                                    )
                                                }
                                            }
                                        ) {
                                            Text(template.name)
                                        }
                                    }
                                }
                            }
                        }
                    } else disabledWithSoundEffect(m)
                }
            ) {
                ButtonWithIconWrapper(SVGIconManager.bars, {}) {
                    Text("Template de Mensagens")
                }
            }

            Div {
                DiscordButton(
                    DiscordButtonType.PRIMARY,
                    attrs = {
                        onClick {
                            m.globalState.openCloseOnlyModal(
                                "Importar",
                                true,
                            ) {
                                Text("Qual mensagem você deseja importar?")

                                VerticalList {
                                    DiscordButton(
                                        DiscordButtonType.PRIMARY,
                                        attrs = {
                                            onClick {
                                                var embed by mutableStateOf<DiscordEmbed?>(null)

                                                m.globalState.openModalWithCloseButton(
                                                    "Embed do Carl-bot (Embed em JSON)",
                                                    true,
                                                    {
                                                        TextArea {
                                                            onInput {
                                                                embed = try {
                                                                    JsonForDiscordMessages.decodeFromString<DiscordEmbed>(
                                                                        it.value
                                                                    )
                                                                } catch (e: SerializationException) {
                                                                    // If the embed couldn't be deserialized, set it to null!
                                                                    null
                                                                }
                                                            }
                                                        }
                                                    },
                                                    { modal ->
                                                        DiscordButton(
                                                            DiscordButtonType.PRIMARY,
                                                            attrs = {
                                                                if (embed == null)
                                                                    disabledWithSoundEffect(m)
                                                                else
                                                                    onClick {
                                                                        onMessageContentChange.invoke(
                                                                            JsonForDiscordMessages.encodeToString(
                                                                                DiscordMessage(
                                                                                    "",
                                                                                    embed
                                                                                )
                                                                            ).also {
                                                                                println(it)
                                                                            }
                                                                        )
                                                                        modal.close()
                                                                        m.globalState.showToast(
                                                                            Toast.Type.SUCCESS,
                                                                            "Mensagem importada!"
                                                                        )
                                                                    }
                                                            }
                                                        ) {
                                                            Text("Importar")
                                                        }
                                                    }
                                                )
                                            }
                                        }
                                    ) {
                                        Text("Embed do Carl-bot (Embed em JSON)")
                                    }
                                }
                            }
                        }
                    }
                ) {
                    ButtonWithIconWrapper(SVGIconManager.fileImport, {}) {
                        Text("Importar")
                    }
                }
            }

            Div {
                DiscordButton(
                    DiscordButtonType.PRIMARY,
                    attrs = {
                        onClick {
                            editorType = when (editorType) {
                                EditorType.INTERACTIVE -> EditorType.RAW
                                EditorType.RAW -> EditorType.INTERACTIVE
                            }
                        }
                    }
                ) {
                    ButtonWithIconWrapper(SVGIconManager.pencil, {}) {
                        Text("Alterar modo de edição")
                    }
                }
            }

            Div {
                DiscordButton(
                    DiscordButtonType.PRIMARY,
                    attrs = {
                        if (targetChannel is TargetChannelResult.ChannelNotSelected) {
                            disabledWithSoundEffect(m)
                        } else {
                            onClick {
                                GlobalScope.launch {
                                    m.globalState.showToast(Toast.Type.INFO, "Enviando mensagem...")

                                    val dashResponse = m.makeRPCRequest<LorittaDashboardRPCResponse.ExecuteDashGuildScopedRPCResponse>(
                                        LorittaDashboardRPCRequest.ExecuteDashGuildScopedRPCRequest(
                                            targetGuild.id,
                                            DashGuildScopedRequest.SendMessageRequest(
                                                when (targetChannel) {
                                                    TargetChannelResult.ChannelNotSelected -> error("Tried to send a message, but the channel is not selected!")
                                                    is TargetChannelResult.DirectMessageTarget -> null
                                                    is TargetChannelResult.GuildMessageChannelTarget -> targetChannel.id
                                                },
                                                rawMessage, // the message is a raw JSON string, or a content
                                                placeholderSectionType
                                            )
                                        )
                                    ).dashResponse as DashGuildScopedResponse.SendMessageResponse

                                    when (dashResponse) {
                                        is DashGuildScopedResponse.SendMessageResponse.FailedToSendMessage -> {
                                            m.globalState.showToast(
                                                Toast.Type.WARN,
                                                "Algo deu errado ao enviar a mensagem!"
                                            ) {
                                                Text("Não foi possível enviar a mensagem.")
                                            }
                                        }

                                        is DashGuildScopedResponse.SendMessageResponse.UnknownChannel -> {
                                            m.globalState.showToast(
                                                Toast.Type.WARN,
                                                "Algo deu errado ao enviar a mensagem!"
                                            ) {
                                                Text("O canal que você selecionou não existe.")
                                            }
                                        }

                                        is DashGuildScopedResponse.SendMessageResponse.TooManyMessages -> {
                                            m.globalState.showToast(
                                                Toast.Type.WARN,
                                                "Algo deu errado ao enviar a mensagem!"
                                            ) {
                                                Text("Você já enviou uma mensagem recentemente! Espere um pouco antes de tentar enviar uma nova mensagem.")
                                            }
                                        }

                                        is DashGuildScopedResponse.SendMessageResponse.Success -> {
                                            m.globalState.showToast(Toast.Type.SUCCESS, "Mensagem enviada!")
                                        }
                                    }
                                }
                            }
                        }
                    }
                ) {
                    ButtonWithIconWrapper(SVGIconManager.paperPlane, {}) {
                        Text("Testar Mensagem")
                    }
                }
            }

            Div(attrs = {
                classes("change-message-preview-direction")
            }) {
                DiscordButton(
                    DiscordButtonType.PRIMARY,
                    {
                        onClick {
                            m.globalState.messageEditorRenderDirection = when (m.globalState.messageEditorRenderDirection) {
                                DiscordMessageUtils.RenderDirection.VERTICAL -> DiscordMessageUtils.RenderDirection.HORIZONTAL
                                DiscordMessageUtils.RenderDirection.HORIZONTAL -> DiscordMessageUtils.RenderDirection.VERTICAL
                            }
                        }
                    }
                ) {
                    ButtonWithIconWrapper(SVGIconManager.diagramNext, {
                        if (m.globalState.messageEditorRenderDirection == DiscordMessageUtils.RenderDirection.VERTICAL)
                            attr("style", "transform: rotate(270deg);")
                        else
                            attr("style", "transform: initial;")
                    }) {
                        when (m.globalState.messageEditorRenderDirection) {
                            DiscordMessageUtils.RenderDirection.VERTICAL -> Text("Visualização na Horizontal")
                            DiscordMessageUtils.RenderDirection.HORIZONTAL -> Text("Visualização na Vertical")
                        }
                    }
                }
            }

            Div {
                DiscordButton(
                    DiscordButtonType.PRIMARY,
                    attrs = {
                        // Automatically disable the format JSON button if you are editing it raw
                        if (parsedMessage != null && editorType == EditorType.RAW) {
                            onClick {
                                onMessageContentChange.invoke(JsonForDiscordMessages.encodeToString(parsedMessage))
                            }
                        } else disabledWithSoundEffect(m)
                    }
                ) {
                    ButtonWithIconWrapper(SVGIconManager.sparkles, {}) {
                        Text("Formatar JSON")
                    }
                }
            }
        }


        val avatarId = exampleUser.avatarId

        val avatarUrl = if (avatarId != null) {
            DiscordCdn.userAvatar(
                exampleUser.id.value,
                avatarId
            )
                .toUrl()
        } else {
            DiscordCdn.defaultAvatar(exampleUser.id.value)
                .toUrl {
                    format =
                        Image.Format.PNG // For some weird reason, the default avatars aren't available in webp format (why?)
                }
        }

        val placeholders = when (val sectionPlaceholders = SectionPlaceholders.sections.first { it.type == placeholderSectionType }) {
            is JoinMessagePlaceholders -> {
                sectionPlaceholders.placeholders.map {
                    RenderableMessagePlaceholder(
                        it,
                        when (it) {
                            JoinMessagePlaceholders.UserMentionPlaceholder -> "@${exampleUser.globalName ?: exampleUser.username}"
                            JoinMessagePlaceholders.UserNamePlaceholder -> exampleUser.globalName ?: exampleUser.username
                            JoinMessagePlaceholders.UserDiscriminatorPlaceholder -> exampleUser.discriminator
                            JoinMessagePlaceholders.UserTagPlaceholder -> "@${exampleUser.username}"
                            JoinMessagePlaceholders.UserIdPlaceholder -> exampleUser.id.value.toString()
                            JoinMessagePlaceholders.UserAvatarUrlPlaceholder -> avatarUrl
                            JoinMessagePlaceholders.GuildNamePlaceholder -> targetGuild.name
                            JoinMessagePlaceholders.GuildSizePlaceholder -> "100" // TODO: Fix this!
                        }
                    )
                }
            }

            is LeaveMessagePlaceholders -> {
                sectionPlaceholders.placeholders.map {
                    RenderableMessagePlaceholder(
                        it,
                        when (it) {
                            LeaveMessagePlaceholders.UserMentionPlaceholder -> "@${exampleUser.globalName ?: exampleUser.username}"
                            LeaveMessagePlaceholders.UserNamePlaceholder -> exampleUser.globalName ?: exampleUser.username
                            LeaveMessagePlaceholders.UserDiscriminatorPlaceholder -> exampleUser.discriminator
                            LeaveMessagePlaceholders.UserTagPlaceholder -> "@${exampleUser.username}"
                            LeaveMessagePlaceholders.UserIdPlaceholder -> exampleUser.id.value.toString()
                            LeaveMessagePlaceholders.UserAvatarUrlPlaceholder -> avatarUrl
                            LeaveMessagePlaceholders.GuildNamePlaceholder -> targetGuild.name
                            LeaveMessagePlaceholders.GuildSizePlaceholder -> "100" // TODO: Fix this!
                        }
                    )
                }
            }
        }

        Div(attrs = {
            classes("message-textarea-and-preview")

            if (m.globalState.messageEditorRenderDirection == DiscordMessageUtils.RenderDirection.VERTICAL)
                classes("vertical-render")
        }) {
            when (editorType) {
                EditorType.INTERACTIVE -> {
                    VerticalList {
                        FieldWrapper {
                            FieldLabel("Conteúdo da Mensagem")

                            TextArea {
                                value(mutableMessage.content)
                                onInput {
                                    mutableMessage.content = it.value
                                    mutableMessage.triggerUpdate()
                                }
                            }
                        }

                        @Composable
                        fun CreateEmbedButton() {
                            DiscordButton(
                                DiscordButtonType.PRIMARY,
                                attrs = {
                                    onClick {
                                        mutableMessage.embed = MutableDiscordMessage.MutableDiscordEmbed(
                                            DiscordEmbed(
                                                description = "A Loritta é muito fofa!"
                                            )
                                        )
                                        mutableMessage.triggerUpdate()
                                    }
                                }
                            ) {
                                Text("Adicionar Embed")
                            }
                        }

                        @Composable
                        fun CreateActionRowButton() {
                            DiscordButton(
                                DiscordButtonType.PRIMARY,
                                attrs = {
                                    if (mutableMessage.components.size >= 5)
                                        disabledWithSoundEffect(m)
                                    else
                                        onClick {
                                            mutableMessage.components.add(
                                                MutableDiscordMessage.MutableDiscordComponent.MutableActionRow(
                                                    DiscordComponent.DiscordActionRow(components = listOf())
                                                )
                                            )
                                            mutableMessage.triggerUpdate()
                                        }
                                }
                            ) {
                                Text("Adicionar Linha de Botões (${mutableMessage.components.size}/5)")
                            }
                        }

                        val embed = mutableMessage.embed
                        // We check for the components to make it look BETTER, putting two buttons side by side
                        if (embed == null) {
                            if (mutableMessage.components.isNotEmpty()) {
                                // If an embed is NOT present...
                                // Add embed button
                                CreateEmbedButton()
                            }
                        } else {
                            VerticalList(attrs = {
                                val embedColor = embed.color
                                val hex = if (embedColor != null) {
                                    ColorUtils.convertFromColorToHex(embedColor)
                                } else "#e3e5e8"

                                attr("style", "border: 1px solid var(--input-border-color);\n" +
                                        "  border-radius: var(--first-level-border-radius);\n" +
                                        "  padding: 1em; border-left: 4px solid $hex;\n")
                            }) {
                                FieldWrapper {
                                    FieldLabel("Cor")

                                    ColorPicker(
                                        m,
                                        embed.color?.let {
                                            val red = it shr 16 and 0xFF
                                            val green = it shr 8 and 0xFF
                                            val blue = it and 0xFF

                                            net.perfectdreams.loritta.common.utils.Color(red, green, blue)
                                        }
                                    ) {
                                        mutableMessage.embed?.color = it?.rgb
                                        mutableMessage.triggerUpdate()
                                    }
                                }

                                FieldWrapper {
                                    FieldLabel("Nome do Autor")

                                    TextInput(embed.author?.name ?: "") {
                                        onInput {
                                            val author = embed.author
                                            if (author != null) {
                                                val newValue = it.value.ifEmpty { null }
                                                if (newValue == null) {
                                                    if (author.url != null || author.iconUrl != null) {
                                                        // If the author text is null BUT there's an icon or URL set, tell the user that they must delete both before deleting the text
                                                        m.globalState.showToast(Toast.Type.WARN, "Embed Inválida") {
                                                            Text("Você não pode ter um ícone ou URL de autor sem ter um texto! Apague o ícone e a URL antes de deletar o texto do autor.")
                                                        }
                                                        m.soundEffects.error.play(1.0)
                                                        return@onInput
                                                    }
                                                    embed.author = null
                                                } else {
                                                    author.name = it.value.ifEmpty { null }
                                                }
                                            } else {
                                                embed.author = MutableDiscordMessage.MutableDiscordEmbed.MutableAuthor(
                                                    DiscordEmbed.Author(
                                                        it.value,
                                                        null,
                                                        null
                                                    )
                                                )
                                            }

                                            mutableMessage.triggerUpdate()
                                        }
                                    }
                                }

                                FieldWrapper {
                                    FieldLabel("URL do Autor")

                                    TextInput(embed.author?.url ?: "") {
                                        if (embed.author?.name == null)
                                            disabled()
                                        else {
                                            onInput {
                                                val author = embed.author
                                                // Should NEVER be null here, but smart cast ain't that smart to figure this out...
                                                author?.url = it.value.ifEmpty { null }

                                                mutableMessage.triggerUpdate()
                                            }
                                        }
                                    }
                                }

                                FieldWrapper {
                                    FieldLabel("URL do Ícone do Autor")

                                    TextInput(embed.author?.iconUrl ?: "") {
                                        if (embed.author?.name == null)
                                            disabled()
                                        else {
                                            onInput {
                                                val author = embed.author
                                                // Should NEVER be null here, but smart cast ain't that smart to figure this out...
                                                author?.iconUrl = it.value.ifEmpty { null }

                                                mutableMessage.triggerUpdate()
                                            }
                                        }
                                    }
                                }

                                FieldWrapper {
                                    FieldLabel("Título")

                                    TextInput(embed.title ?: "") {
                                        onInput {
                                            embed.title = it.value.ifEmpty { null }
                                            mutableMessage.triggerUpdate()
                                        }
                                    }
                                }

                                FieldWrapper {
                                    FieldLabel("URL do Título")

                                    TextInput(embed.url ?: "") {
                                        onInput {
                                            embed.url = it.value.ifEmpty { null }
                                            mutableMessage.triggerUpdate()
                                        }
                                    }
                                }

                                FieldWrapper {
                                    FieldLabel("Descrição")

                                    TextArea(embed.description ?: "") {
                                        onInput {
                                            mutableMessage.embed?.description = it.value.ifEmpty { null }
                                            mutableMessage.triggerUpdate()
                                        }
                                    }
                                }

                                Div {
                                    FieldLabel("Fields")

                                    VerticalList(attrs = {
                                        attr(
                                            "style", "border: 1px solid var(--input-border-color);\n" +
                                                    "  border-radius: var(--first-level-border-radius);\n" +
                                                    "  padding: 1em;"
                                        )
                                    }) {
                                        for ((index, field) in embed.fields.withIndex()) {
                                            Div(attrs = {
                                                attr(
                                                    "style", "border: 1px solid var(--input-border-color);\n" +
                                                            "  border-radius: var(--first-level-border-radius);\n" +
                                                            "  padding: 1em;"
                                                )
                                            }) {
                                                FieldLabel("Field ${index + 1}")
                                                Div {
                                                    FieldLabel("Nome")

                                                    TextInput(field.name) {
                                                        onInput {
                                                            field.name = it.value
                                                            mutableMessage.triggerUpdate()
                                                        }
                                                    }
                                                }

                                                Div {
                                                    FieldLabel("Valor")

                                                    TextArea(field.value) {
                                                        onInput {
                                                            field.value = it.value
                                                            mutableMessage.triggerUpdate()
                                                        }
                                                    }
                                                }

                                                DiscordToggle(
                                                    "inline-field-$index",
                                                    "Field Inline",
                                                    null,
                                                    field.inline,
                                                    onChange = { newValue ->
                                                        field.inline = newValue
                                                        mutableMessage.triggerUpdate()
                                                    }
                                                )

                                                DiscordButton(
                                                    DiscordButtonType.DANGER,
                                                    attrs = {
                                                        onClick {
                                                            embed.fields.removeAt(index)
                                                            mutableMessage.triggerUpdate()
                                                        }
                                                    }
                                                ) {
                                                    Text("Remover Field")
                                                }
                                            }
                                        }

                                        DiscordButton(
                                            DiscordButtonType.PRIMARY,
                                            attrs = {
                                                onClick {
                                                    embed.fields.add(
                                                        MutableDiscordMessage.MutableDiscordEmbed.MutableField(
                                                            DiscordEmbed.Field(
                                                                "Loritta Morenitta",
                                                                "Ela é muito fofa!",
                                                                true
                                                            )
                                                        )
                                                    )
                                                    mutableMessage.triggerUpdate()
                                                }
                                            }
                                        ) {
                                            Text("Adicionar Field")
                                        }
                                    }
                                }

                                FieldWrapper {
                                    FieldLabel("URL da Imagem")

                                    TextInput(embed.imageUrl ?: "") {
                                        onInput {
                                            embed.imageUrl = it.value.ifEmpty { null }
                                            mutableMessage.triggerUpdate()
                                        }
                                    }
                                }

                                FieldWrapper {
                                    FieldLabel("URL da Thumbnail")

                                    TextInput(embed.thumbnailUrl ?: "") {
                                        onInput {
                                            embed.thumbnailUrl = it.value.ifEmpty { null }
                                            mutableMessage.triggerUpdate()
                                        }
                                    }
                                }

                                val mutableFooter = embed.footer
                                FieldWrapper {
                                    FieldLabel("Texto do Rodapé")

                                    TextInput(embed.footer?.text ?: "") {
                                        onInput {
                                            val footer = embed.footer
                                            if (footer != null) {
                                                val newValue = it.value.ifEmpty { null }
                                                if (newValue == null) {
                                                    if (footer.iconUrl != null) {
                                                        // If the footer text is null BUT there's an icon set, tell the user that they must delete the icon before deleting the text
                                                        m.globalState.showToast(Toast.Type.WARN, "Embed Inválida") {
                                                            Text("Você não pode ter um ícone de rodapé sem ter um texto! Apague o ícone antes de deletar o texto do rodapé.")
                                                        }
                                                        m.soundEffects.error.play(1.0)
                                                        return@onInput
                                                    }
                                                    embed.footer = null
                                                } else {
                                                    footer.text = it.value.ifEmpty { null }
                                                }
                                            } else {
                                                embed.footer = MutableDiscordMessage.MutableDiscordEmbed.MutableFooter(
                                                    DiscordEmbed.Footer(
                                                        it.value,
                                                        null
                                                    )
                                                )
                                            }

                                            mutableMessage.triggerUpdate()
                                        }
                                    }
                                }

                                FieldWrapper {
                                    FieldLabel("URL do Ícone do Rodapé")

                                    TextInput(embed.footer?.iconUrl ?: "") {
                                        // Only allow setting the Icon URL if the footer text is present
                                        if (mutableFooter?.text == null)
                                            disabled()
                                        else {
                                            onInput {
                                                val footer = embed.footer
                                                // Should NEVER be null here, but smart cast ain't that smart to figure this out...
                                                footer?.iconUrl = it.value.ifEmpty { null }

                                                mutableMessage.triggerUpdate()
                                            }
                                        }
                                    }
                                }

                                // Remove embed button
                                DiscordButton(
                                    DiscordButtonType.DANGER,
                                    attrs = {
                                        onClick {
                                            mutableMessage.embed = null
                                            mutableMessage.triggerUpdate()
                                        }
                                    }
                                ) {
                                    Text("Remover Embed")
                                }
                            }
                        }

                        VerticalList {
                            val components = mutableMessage.components

                            for (component in components) {
                                ComponentEditor(m, null, component, mutableMessage)
                            }

                            if (embed == null && components.isEmpty()) {
                                HorizontalList(attrs = {
                                    classes("child-flex-grow")
                                }) {
                                    CreateEmbedButton()
                                    CreateActionRowButton()
                                }
                            } else {
                                CreateActionRowButton()
                            }
                        }
                    }
                }

                EditorType.RAW -> {
                    VerticalList {
                        FieldWrapper {
                            FieldLabel("Conteúdo da Mensagem em JSON")

                            TextArea(rawMessage) {
                                onInput {
                                    onMessageContentChange.invoke(it.value)
                                }
                            }

                            Div {
                                // Before we render the message as a normal message, we will check if the user *tried* to do a JSON message
                                // We check if it starts AND ends with {} because we don't want to trigger checks in "{@user} hello"
                                if (parsedMessage == null && (rawMessage.startsWith("{") && rawMessage.endsWith("}"))) {
                                    Div {
                                        Text("Você tentou fazer uma mensagem em JSON? Se sim, tem algo errado nela!")
                                    }
                                }
                            }
                        }
                    }
                }
            }

            val rId = Random.nextLong()

            Div(attrs = {
                classes("message-preview-section")
            }) {
                FieldLabel("Pré-visualização da Mensagem")

                Div(attrs = {
                    id("message-preview-wrapper-$rId")
                    classes("message-preview-wrapper")
                }) {
                    Div(attrs = {
                        id("message-preview-$rId")
                        classes("message-preview")
                    }) {
                        for (message in messagesToBeRenderedBeforeTargetMessage) {
                            DiscordMessageRenderer(
                                message.author,
                                message.message,
                                null,
                                placeholders,
                            )
                        }

                        if (parsedMessage != null) {
                            DiscordMessageRenderer(
                                RenderableDiscordUser.fromDiscordUser(selfUser)
                                    .copy(name = "Loritta Morenitta \uD83D\uDE18"),
                                parsedMessage,
                                null,
                                placeholders,
                            )
                        } else {
                            // If the message couldn't be parsed, render it as a normal message
                            DiscordMessageRenderer(
                                RenderableDiscordUser.fromDiscordUser(selfUser)
                                    .copy(name = "Loritta Morenitta \uD83D\uDE18"),
                                DiscordMessage(
                                    content = rawMessage
                                ),
                                null,
                                placeholders
                            )
                        }

                        for (message in messagesToBeRenderedAfterTargetMessage) {
                            DiscordMessageRenderer(
                                message.author,
                                message.message,
                                null,
                                placeholders,
                            )
                        }
                    }
                }
            }
        }

        Div {
            FancyDetails(
                {},
                {
                    Text("Quais são as variáveis/placeholders que eu posso usar?")
                },
                {
                    Table {
                        Thead {
                            Tr {
                                Th {
                                    Text("Placeholder")
                                }
                                Th {
                                    Text("Significado")
                                }
                            }
                        }

                        Tbody {
                            placeholders.forEach {
                                Tr {
                                    Td {
                                        var isFirst = true
                                        for (placeholder in it.placeholder.names.filter { !it.hidden }) {
                                            if (!isFirst)
                                                Text(", ")

                                            Code {
                                                Text(placeholder.placeholder.asKey)
                                            }
                                            isFirst = false
                                        }
                                    }

                                    Td {
                                        val description = it.placeholder.description
                                        if (description != null)
                                            Text(i18nContext.get(description))
                                    }
                                }
                            }
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun ActionRowEditor(m: LorittaDashboardFrontend, component: MutableDiscordMessage.MutableDiscordComponent.MutableActionRow, message: MutableDiscordMessage) {
    FieldWrapper {
        FieldLabel("Linha de Botões")

        VerticalList(attrs = {
            attr(
                "style", "border: 1px solid var(--input-border-color);\n" +
                        "  border-radius: var(--first-level-border-radius);\n" +
                        "  padding: 1em;"
            )
        }) {
            for (childComponent in component.components) {
                Div(attrs = {
                    attr(
                        "style", "border: 1px solid var(--input-border-color);\n" +
                                "  border-radius: var(--first-level-border-radius);\n" +
                                "  padding: 1em;"
                    )
                }) {
                    ComponentEditor(m, component, childComponent, message)
                }
            }

            HorizontalList(attrs = {
                classes("child-flex-grow")
            }) {
                DiscordButton(
                    DiscordButtonType.PRIMARY,
                    attrs = {
                        if (component.components.size >= 5)
                            disabledWithSoundEffect(m)
                        else
                            onClick {
                                component.components.add(
                                    MutableDiscordMessage.MutableDiscordComponent.MutableButton(
                                        DiscordComponent.DiscordButton(
                                            label = "Website da Loritta",
                                            style = 5,
                                            url = "https://loritta.website/"
                                        )
                                    )
                                )
                                message.triggerUpdate()
                            }
                    }
                ) {
                    Text("Adicionar Botão (${component.components.size}/5)")
                }

                DiscordButton(
                    DiscordButtonType.DANGER,
                    attrs = {
                        onClick {
                            message.components.remove(component)
                            message.triggerUpdate()
                        }
                    }
                ) {
                    Text("Remover Linha")
                }
            }
        }
    }
}

@Composable
fun ButtonEditor(parentComponent: MutableDiscordMessage.MutableDiscordComponent.MutableActionRow, component: MutableDiscordMessage.MutableDiscordComponent.MutableButton, message: MutableDiscordMessage) {
    VerticalList {
        FieldWrapper {
            FieldLabel("Label")

            TextInput(component.label ?: "") {
                onInput {
                    component.label = it.value.ifEmpty { null }
                    message.triggerUpdate()
                }
            }
        }

        FieldWrapper {
            FieldLabel("URL")

            TextInput(component.url ?: "") {
                onInput {
                    component.url = it.value.ifEmpty { null }
                    message.triggerUpdate()
                }
            }
        }

        DiscordButton(
            DiscordButtonType.DANGER,
            attrs = {
                onClick {
                    parentComponent.components.remove(component)
                    message.triggerUpdate()
                }
            }
        ) {
            Text("Remover Botão")
        }
    }
}

@Composable
fun ComponentEditor(
    m: LorittaDashboardFrontend,
    parentComponent: MutableDiscordMessage.MutableDiscordComponent.MutableActionRow?,
    component: MutableDiscordMessage.MutableDiscordComponent,
    message: MutableDiscordMessage
) {
    when (component) {
        is MutableDiscordMessage.MutableDiscordComponent.MutableActionRow -> ActionRowEditor(m, component, message)
        is MutableDiscordMessage.MutableDiscordComponent.MutableButton -> ButtonEditor(
            parentComponent ?: error("Button on the root component is not allowed!"), component, message
        )
    }
}

sealed class TargetChannelResult {
    class GuildMessageChannelTarget(val id: Long) : TargetChannelResult()
    data object DirectMessageTarget : TargetChannelResult()
    data object ChannelNotSelected : TargetChannelResult()
}

sealed class LorittaMessageTemplate(val name: String) {
    class LorittaDiscordMessageTemplate(name: String, val message: DiscordMessage) : LorittaMessageTemplate(name)
    class LorittaRawMessageTemplate(name: String, val content: String) : LorittaMessageTemplate(name)
}

data class DiscordMessageWithAuthor(
    val author: RenderableDiscordUser,
    val message: DiscordMessage
)

enum class EditorType {
    INTERACTIVE,
    RAW
}