package com.mac.expensee.core.security.di

import com.mac.expensee.core.security.biometric.BiometricAuthenticator
import com.mac.expensee.core.security.keystore.SecureKeyStore
import com.mac.expensee.core.security.password.PasswordHasher
import org.koin.dsl.module

val securityModule = module {
    single { PasswordHasher() }
    single { SecureKeyStore(context = get()) }
    single { BiometricAuthenticator() }
}
