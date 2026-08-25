package com.calmed.calmedbackend.service.specification

import com.calmed.calmedbackend.model.AppResult
import java.util.UUID

interface IAccountDeletionService {

    suspend fun deleteAccount(userId: UUID): AppResult<Unit>
}

