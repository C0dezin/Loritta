package net.perfectdreams.loritta.cinnamon.showtime.backend.views

import kotlinx.html.DIV
import kotlinx.html.div
import kotlinx.html.h1
import kotlinx.html.h2
import kotlinx.html.p
import kotlinx.html.style
import net.perfectdreams.dokyo.WebsiteTheme
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.showtime.backend.ShowtimeBackend
import net.perfectdreams.loritta.cinnamon.showtime.backend.utils.DiscordInviteWrapper.lorittaCommunityServerInvite
import net.perfectdreams.loritta.cinnamon.showtime.backend.utils.DiscordInviteWrapper.lorittaSupportServerInvite
import net.perfectdreams.loritta.cinnamon.showtime.backend.utils.imgSrcSetFromEtherealGambi
import net.perfectdreams.loritta.cinnamon.showtime.backend.utils.innerContent
import net.perfectdreams.loritta.common.locale.BaseLocale

class SupportView(
    showtimeBackend: ShowtimeBackend,
    websiteTheme: WebsiteTheme,
    locale: BaseLocale,
    i18nContext: I18nContext,
    path: String
) : NavbarView(
    showtimeBackend,
    websiteTheme,
    locale,
    i18nContext,
    path
) {
    override fun getTitle() = locale["website.support.title"]

    override fun DIV.generateContent() {
        innerContent {
            div(classes = "odd-wrapper") {
                div(classes = "media") {
                    div(classes = "media-figure") {
                        imgSrcSetFromEtherealGambi(
                            showtimeBackend,
                            showtimeBackend.images.lorittaSupport,
                            "png",
                            "(max-width: 800px) 50vw, 15vw"
                        )
                    }

                    div(classes = "media-body") {
                        div {
                            style = "text-align: left;"

                            div {
                                style = "text-align: center;"
                                h1 {
                                    +locale["website.support.title"]
                                }
                            }

                            for (str in locale.getList("website.support.description")) {
                                p {
                                    +str
                                }
                            }
                        }
                    }
                }
            }
            div(classes = "even-wrapper wobbly-bg") {
                div(classes = "support-invites-wrapper") {
                    div(classes = "support-invite-wrapper") {
                        div(classes = "support-invite-content") {
                            h2 {
                                +locale["website.support.supportServer.title"]
                            }

                            locale.getList("website.support.supportServer.description").forEach {
                                p {
                                    +it
                                }
                            }
                        }

                        div(classes = "discord-support-invite-wrapper") {
                            lorittaSupportServerInvite(showtimeBackend, i18nContext)
                        }
                    }

                    div(classes = "support-invite-wrapper") {
                        div(classes = "support-invite-content") {
                            h2 {
                                +locale["website.support.communityServer.title"]
                            }

                            locale.getList("website.support.communityServer.description").forEach {
                                p {
                                    +it
                                }
                            }
                        }

                        div(classes = "discord-support-invite-wrapper") {
                            lorittaCommunityServerInvite(showtimeBackend, i18nContext)
                        }
                    }
                }
            }
        }
    }
}