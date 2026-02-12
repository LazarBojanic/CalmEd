package com.calmed.calmedfrontendtourettes.repository

import com.calmed.calmedfrontendtourettes.http.IAppApi
import com.calmed.calmedfrontendtourettes.model.dto.response.HomeDto


class HomeRepository(
    private val api: IAppApi
) {
    suspend fun getHome(year: Int, month: Int): HomeDto? {
        return api.getHome(year, month)
    }
}