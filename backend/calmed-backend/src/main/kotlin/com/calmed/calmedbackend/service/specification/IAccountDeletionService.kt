package com.calmed.calmedbackend.service.specification

import com.calmed.calmedbackend.model.AppResult
import java.util.UUID

interface IAccountDeletionService {
    /**
     * Permanently deletes a user's account and all associated personal data.
     *
     * Personal data (auth credential, refresh tokens, onboarding/tics info,
     * user program, exercise progress) is hard-deleted. Payment records are
     * anonymized (their [com.calmed.calmedbackend.model.raw.payment.Payment.userId]
     * link is detached) so that only non-PII financial/tax evidence is retained.
     * The profile image file, if any, is removed. All changes are applied
     * atomically within a single transaction.
     */
    suspend fun deleteAccount(userId: UUID): AppResult<Unit>
}

