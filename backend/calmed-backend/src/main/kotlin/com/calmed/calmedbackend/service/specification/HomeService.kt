package com.calmed.calmedbackend.service.specification

import com.calmed.calmedbackend.model.dto.response.HomeDto

interface HomeService {
    suspend fun getHome(userId: String): HomeDto
}