package com.calmed.calmedtics.repository

import com.calmed.calmedtics.http.IAppApi
import com.calmed.calmedtics.model.dto.response.HomeDto



class HomeRepository(
    private val api: IAppApi
) {
    suspend fun getHome(year: Int, month: Int): HomeDto? {
        return api.getHome(year, month)
    }
}