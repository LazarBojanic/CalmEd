package com.calmed.calmedbackend.di

import com.calmed.calmedbackend.service.implementation.MessageService
import com.calmed.calmedbackend.service.specification.IMessageService
import org.koin.dsl.module

val serviceModule = module {
	single<IMessageService>{ MessageService(get()) }
}