package net.perfectdreams.loritta.cinnamon.dashboard.frontend.screen

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.*
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.i18nhelper.core.keydata.StringI18nData
import net.perfectdreams.loritta.cinnamon.dashboard.common.responses.LorittaResponse
import net.perfectdreams.loritta.cinnamon.dashboard.common.responses.NotEnoughSonhosErrorResponse
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.LorittaDashboardFrontend
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.DiscordButton
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.DiscordButtonType
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.LocalizedText
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.Resource
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.paths.ScreenPathWithArguments
import net.perfectdreams.loritta.i18n.I18nKeysData
import org.jetbrains.compose.web.attributes.disabled
import org.jetbrains.compose.web.css.textAlign
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Img
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Text

sealed class Screen(val m: LorittaDashboardFrontend) {
    private var rejectNewJobs = false
    private val jobs = mutableListOf<Job>()

    abstract fun createPathWithArguments(): ScreenPathWithArguments
    abstract fun createTitle(): StringI18nData

    open fun onLoad() {}

    fun dispose() {
        rejectNewJobs = true
        println("Disposing ${jobs.size} jobs...")
        jobs.forEach { it.cancel() }
        jobs.clear()
    }

    fun launch(block: suspend CoroutineScope.() -> Unit): Job {
        if (rejectNewJobs)
            error("All new jobs are being rejected!")

        val job = GlobalScope.launch(block = block)
        jobs.add(job)
        job.invokeOnCompletion {
            jobs.remove(job)
        }
        return job
    }

    fun <T> async(block: suspend CoroutineScope.() -> T): Deferred<T> {
        if (rejectNewJobs)
            error("All new jobs are being rejected!")

        val job = GlobalScope.async(block = block)
        jobs.add(job)
        job.invokeOnCompletion {
            jobs.remove(job)
        }
        return job
    }

    inline fun <reified T : LorittaResponse> openConfirmPurchaseModal(
        i18nContext: I18nContext,
        price: Long,
        crossinline purchaseBlock: suspend () -> LorittaResponse,
        crossinline fallbackBlock: suspend (LorittaResponse) -> Boolean = { false },
        crossinline onSuccess: suspend (T) -> (Unit)
    ) {
        var disablePurchaseButton by mutableStateOf(false)
        val sonhos = (m.globalState.userInfo as Resource.Success).value

        m.globalState.openModalWithCloseButton(
            i18nContext.get(I18nKeysData.Website.Dashboard.PurchaseModal.Title),
            true,
            {
                Div(attrs = { style { textAlign("center") }}) {
                    Img("https://stuff.loritta.website/lori-nota-fiscal.png") {
                        attr("width", "300")
                    }

                    Div(attrs = { style { textAlign("center") }}) {
                        i18nContext.get(I18nKeysData.Website.Dashboard.PurchaseModal.Description(price, sonhos.money)).forEach {
                            P {
                                Text(it)
                            }
                        }
                    }
                }
            },
            { modal ->
                DiscordButton(
                    DiscordButtonType.SUCCESS,
                    attrs = {
                        if (disablePurchaseButton) {
                            disabled()
                        } else {
                            onClick {
                                disablePurchaseButton = true

                                launch {
                                    val result = purchaseBlock.invoke()

                                    modal.close()

                                    when (result) {
                                        is T -> { onSuccess.invoke(result) }
                                        is NotEnoughSonhosErrorResponse -> {
                                            openNotEnoughSonhosModal(i18nContext, price)
                                        }
                                        else -> {
                                            val r = fallbackBlock.invoke(result)
                                            if (!r)
                                                error("I don't know how to handle a ${result::class}!")
                                        }
                                    }
                                }
                            }
                        }
                    }
                ) {
                    Text(i18nContext.get(I18nKeysData.Website.Dashboard.PurchaseModal.Buy))
                }
            }
        )
    }

    fun openNotEnoughSonhosModal(i18nContext: I18nContext, sonhos: Long) {
        // Uh oh...
        m.globalState.openCloseOnlyModal(
            i18nContext.get(I18nKeysData.Website.Dashboard.YouDontHaveEnoughSonhosModal.Title),
            true
        ) {
            LocalizedText(i18nContext, I18nKeysData.Website.Dashboard.YouDontHaveEnoughSonhosModal.Description(sonhos))
        }
    }
}