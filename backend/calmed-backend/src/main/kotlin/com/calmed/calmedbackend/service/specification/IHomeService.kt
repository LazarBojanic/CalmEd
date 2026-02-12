package com.calmed.calmedbackend.service.specification

import com.calmed.calmedbackend.model.dto.response.HomeDto

interface IHomeService {
    suspend fun getHome(userId: String, year: Int, month: Int): HomeDto

}