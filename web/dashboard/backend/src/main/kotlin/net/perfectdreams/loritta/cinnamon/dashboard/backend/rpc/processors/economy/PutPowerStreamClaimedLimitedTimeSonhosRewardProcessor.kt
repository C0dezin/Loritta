package net.perfectdreams.loritta.cinnamon.dashboard.backend.rpc.processors.economy

import io.ktor.server.application.*
import net.perfectdreams.loritta.cinnamon.dashboard.backend.LorittaDashboardBackend
import net.perfectdreams.loritta.cinnamon.dashboard.backend.rpc.processors.LorittaDashboardRpcProcessor
import net.perfectdreams.loritta.cinnamon.pudding.tables.Profiles
import net.perfectdreams.loritta.cinnamon.pudding.tables.SonhosTransactionsLog
import net.perfectdreams.loritta.cinnamon.pudding.tables.transactions.PowerStreamClaimedLimitedTimeSonhosRewardSonhosTransactionsLog
import net.perfectdreams.loritta.serializable.dashboard.requests.LorittaDashboardRPCRequest
import net.perfectdreams.loritta.serializable.dashboard.responses.LorittaDashboardRPCResponse
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.update
import java.time.Instant

class PutPowerStreamClaimedLimitedTimeSonhosRewardProcessor(val m: LorittaDashboardBackend) : LorittaDashboardRpcProcessor<LorittaDashboardRPCRequest.PutPowerStreamClaimedLimitedTimeSonhosRewardRequest, LorittaDashboardRPCResponse.PutPowerStreamClaimedLimitedTimeSonhosRewardResponse> {
    override suspend fun process(call: ApplicationCall, request: LorittaDashboardRPCRequest.PutPowerStreamClaimedLimitedTimeSonhosRewardRequest): LorittaDashboardRPCResponse.PutPowerStreamClaimedLimitedTimeSonhosRewardResponse {
        when (validateDashboardToken(m, call)) {
            LorittaDashboardRpcProcessor.DashboardTokenResult.InvalidTokenAuthorization -> {
                return LorittaDashboardRPCResponse.PutPowerStreamClaimedLimitedTimeSonhosRewardResponse.Unauthorized()
            }
            LorittaDashboardRpcProcessor.DashboardTokenResult.Success -> {
                val result = m.pudding.transaction {
                    val changedProfilesCount = Profiles.update({ Profiles.id eq request.userId }) {
                        with(SqlExpressionBuilder) {
                            it[money] = money + request.quantity
                        }
                    }

                    if (changedProfilesCount == 0)
                        return@transaction false

                    val transactionLogId = SonhosTransactionsLog.insertAndGetId {
                        it[user] = request.userId
                        it[timestamp] = Instant.now()
                    }

                    PowerStreamClaimedLimitedTimeSonhosRewardSonhosTransactionsLog.insert {
                        it[timestampLog] = transactionLogId
                        it[sonhos] = request.quantity
                        it[streamId] = request.streamId
                        it[rewardId] = request.rewardId
                        it[liveId] = request.liveId
                    }

                    return@transaction true
                }

                return when (result) {
                    true -> LorittaDashboardRPCResponse.PutPowerStreamClaimedLimitedTimeSonhosRewardResponse.Success()
                    false -> LorittaDashboardRPCResponse.PutPowerStreamClaimedLimitedTimeSonhosRewardResponse.UnknownUser()
                }
            }
        }
    }
}