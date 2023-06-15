package net.perfectdreams.loritta.cinnamon.discord.interactions

import net.perfectdreams.loritta.common.locale.LanguageManager
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CinnamonSlashCommandDeclarationWrapper
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.discord.declarations.*
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.economy.declarations.*
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.`fun`.declarations.*
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.images.declarations.*
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.moderation.declarations.BanCommand
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.moderation.declarations.PredefinedReasonsCommand
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.roblox.declarations.RobloxCommand
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.social.declarations.*
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.undertale.declarations.UndertaleCommand
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.utils.declarations.*
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.videos.declarations.*
import net.perfectdreams.loritta.morenitta.interactions.vanilla.`fun`.VieirinhaCommand

/**
 * Public Loritta Commands
 *
 * They are in a separate class instead of staying within the [InteractionsManager] because this is also used in Showtime's Backend module to
 * get Loritta's command list!
 */
class PublicLorittaCommands(val languageManager: LanguageManager) {
    fun commands(): List<CinnamonSlashCommandDeclarationWrapper> {
        val wrapper = RegistryWrapper()
        with(wrapper) {
            // ===[ DISCORD ]===
            register(ServerCommand(languageManager))
            register(InviteCommand(languageManager))
            register(EmojiCommand(languageManager))

            // ===[ MODERATION ]===
            register(BanCommand(languageManager))
            register(PredefinedReasonsCommand(languageManager))

            // ===[ FUN ]===
            register(RateCommand(languageManager))
            register(SummonCommand(languageManager))
            register(TextTransformCommand(languageManager))
            register(JankenponCommand(languageManager))
            register(HungerGamesCommand(languageManager))
            register(SoundboxCommand(languageManager))

            // ===[ IMAGES ]===
            register(DrakeCommand(languageManager))
            register(SonicCommand(languageManager))
            register(ArtCommand(languageManager))
            register(BobBurningPaperCommand(languageManager))
            register(BRMemesCommand(languageManager))
            register(BuckShirtCommand(languageManager))
            register(LoriSignCommand(languageManager))
            register(PassingPaperCommand(languageManager))
            register(PepeDreamCommand(languageManager))
            register(PetPetCommand(languageManager))
            register(WolverineFrameCommand(languageManager))
            register(RipTvCommand(languageManager))
            register(SustoCommand(languageManager))
            register(GetOverHereCommand(languageManager))
            register(NichijouYuukoPaperCommand(languageManager))
            register(TrumpCommand(languageManager))
            register(TerminatorAnimeCommand(languageManager))
            register(ToBeContinuedCommand(languageManager))
            register(InvertColorsCommand(languageManager))
            register(MemeMakerCommand(languageManager))
            register(MarkMetaCommand(languageManager))
            register(DrawnMaskCommand(languageManager))

            // ===[ VIDEOS ]===
            register(CarlyAaahCommand(languageManager))
            register(AttackOnHeartCommand(languageManager))
            register(FansExplainingCommand(languageManager))
            register(GigaChadCommand(languageManager))
            register(ChavesCommand(languageManager))

            // ===[ UTILS ]===
            register(MoneyCommand(languageManager))
            register(MorseCommand(languageManager))
            register(DictionaryCommand(languageManager))
            register(ChooseCommand(languageManager))
            register(PackageCommand(languageManager))
            register(ColorInfoCommand(languageManager))
            register(NotificationsCommand(languageManager))
            register(TranslateCommand(languageManager))
            register(OCRCommand(languageManager))

            // ===[ ECONOMY ]===
            register(SonhosCommand(languageManager))
            // register(BrokerCommand(languageManager))
            register(BetCommand(languageManager))

            // ===[ SOCIAL ]===
            register(AchievementsCommand(languageManager))

            register(AfkCommand(languageManager))
            register(GenderCommand(languageManager))

            // ===[ UNDERTALE ]===
            register(UndertaleCommand(languageManager))

            // ===[ ROBLOX ]===
            register(RobloxCommand(languageManager))
        }
        return wrapper.commands
    }

    class RegistryWrapper {
        val commands = mutableListOf<CinnamonSlashCommandDeclarationWrapper>()

        fun register(declarationWrapper: CinnamonSlashCommandDeclarationWrapper) {
            commands.add(declarationWrapper)
        }
    }
}