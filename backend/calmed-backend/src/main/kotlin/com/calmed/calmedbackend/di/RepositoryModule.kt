package com.calmed.calmedbackend.di

import com.calmed.calmedbackend.repository.implementation.MessageRepository
import com.calmed.calmedbackend.repository.specification.IMessageRepository
import org.koin.dsl.module

val repositoryModule = module {
	single<IMessageRepository> { MessageRepository() }
}