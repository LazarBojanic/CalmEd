package com.calmed.calmedfrontendtourettes.util

import com.calmed.calmedfrontendtourettes.http.IAppApi
import kotlinx.coroutines.withTimeoutOrNull

suspend fun isBackendReachable(
    appApi: IAppApi,
    timeoutMillis: Long = 3_500L
): Boolean {
    val reachable = withTimeoutOrNull(timeoutMillis) {
        runCatching { appApi.ping() }.isSuccess
    }
    return reachable == true
}
