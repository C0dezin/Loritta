package net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.browser.window
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.i18nhelper.core.Language
import net.perfectdreams.i18nhelper.core.keydata.StringI18nData
import net.perfectdreams.loritta.cinnamon.dashboard.common.responses.GetSpicyInfoResponse
import net.perfectdreams.loritta.cinnamon.dashboard.common.responses.GetUserIdentificationResponse
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.LorittaDashboardFrontend
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.CloseModalButton

class GlobalState(val m: LorittaDashboardFrontend) {
    private var userInfoResource = mutableStateOf<Resource<GetUserIdentificationResponse>>(Resource.Loading())
    var userInfo by userInfoResource
    var i18nContext by mutableStateOf<Resource<I18nContext>>(Resource.Loading())
    private var spicyInfoResource = mutableStateOf<Resource<GetSpicyInfoResponse>>(Resource.Loading())
    var spicyInfo by spicyInfoResource
    var isSidebarOpenState = mutableStateOf(false)
    var isSidebarOpen by isSidebarOpenState
    var activeModal by mutableStateOf<Modal?>(null)
    var isLoading = false

    private val jobs = mutableListOf<Job>()

    suspend fun updateSelfUserInfo() {
        m.makeApiRequestAndUpdateState(userInfoResource, HttpMethod.Get, "/api/v1/users/@me")
    }

    suspend fun updateSpicyInfo() {
        m.makeApiRequestAndUpdateState(spicyInfoResource, HttpMethod.Get, "/api/v1/spicy")
    }

    suspend fun updateI18nContext() {
        val i18nContext = retrieveI18nContext()
        this@GlobalState.i18nContext = Resource.Success(i18nContext)
    }

    suspend fun retrieveI18nContext(): I18nContext {
        val languageId = window.location.pathname.split("/")[1]
        val result = m.http.get("${window.location.origin}/api/v1/languages/$languageId").bodyAsText()

        val language = Json.decodeFromString<Language>(result)

        return I18nContext(
            IntlMFFormatter(language.info.formattingLanguageId),
            Json.decodeFromString(result)
        )
    }

    fun openModal(
        title: StringI18nData,
        body: @Composable () -> (Unit),
        vararg buttons: @Composable () -> (Unit)
    ) {
        launch {
            // I think it should always be "Success" here
            val state = i18nContext
            val i18nContext: I18nContext

            // Super hacky! Waits until the state is success and then opens the modal
            while (true) {
                val temp = (state as? Resource.Success)?.value
                if (temp != null) {
                    i18nContext = temp
                    break
                }

                delay(100)
            }

            activeModal = Modal(
                i18nContext.get(title),
                body,
                buttons.toMutableList()
            )
        }
    }

    fun openCloseOnlyModal(
        title: StringI18nData,
        body: @Composable () -> (Unit)
    ) = openModal(
        title,
        body,
        {
            CloseModalButton(this)
        }
    )

    fun openModal(
        title: String,
        body: @Composable () -> (Unit),
        vararg buttons: @Composable () -> (Unit)
    ) {
        activeModal = Modal(
            title,
            body,
            buttons.toMutableList()
        )
    }

    fun openCloseOnlyModal(
        title: String,
        body: @Composable () -> (Unit)
    ) = openModal(
        title,
        body,
        {
            CloseModalButton(this)
        }
    )

    fun launch(block: suspend CoroutineScope.() -> Unit): Job {
        val job = GlobalScope.launch(block = block)
        jobs.add(job)
        job.invokeOnCompletion {
            jobs.remove(job)
        }
        return job
    }

    fun <T> async(block: suspend CoroutineScope.() -> T): Deferred<T> {
        val job = GlobalScope.async(block = block)
        jobs.add(job)
        job.invokeOnCompletion {
            jobs.remove(job)
        }
        return job
    }

    fun test() = flow<String> {

    }
}