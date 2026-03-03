package com.github.wadleeduhwan.ghworkflows.auth

import com.intellij.credentialStore.CredentialAttributes
import com.intellij.credentialStore.generateServiceName
import com.intellij.ide.passwordSafe.PasswordSafe
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
import kotlinx.coroutines.runBlocking
import org.jetbrains.plugins.github.authentication.accounts.GHAccountManager

object GitHubTokenManager {

    private val LOG = logger<GitHubTokenManager>()

    private val credentialAttributes = CredentialAttributes(
        generateServiceName("GhWorkflows", "GitHubToken")
    )

    /**
     * 토큰 조회 우선순위:
     * 1. IntelliJ GitHub 계정 (Settings > Version Control > GitHub)
     * 2. 수동 입력 PAT (플러그인 Settings)
     */
    fun getToken(): String? {
        getIntelliJGitHubToken()?.let { return it }
        return getManualToken()
    }

    fun hasToken(): Boolean = !getToken().isNullOrBlank()

    /** IntelliJ 내장 GitHub 계정에서 토큰을 가져온다 */
    fun getIntelliJGitHubToken(): String? {
        return try {
            val accountManager = service<GHAccountManager>()
            val accounts = accountManager.accountsState.value
            val account = accounts.firstOrNull() ?: return null
            runBlocking { accountManager.findCredentials(account) }
        } catch (e: Exception) {
            LOG.debug("Failed to get token from IntelliJ GitHub accounts", e)
            null
        }
    }

    /** IntelliJ GitHub 계정이 연동되어 있는지 확인 */
    fun getIntelliJAccountName(): String? {
        return try {
            val accountManager = service<GHAccountManager>()
            accountManager.accountsState.value.firstOrNull()?.name
        } catch (_: Exception) {
            null
        }
    }

    fun getManualToken(): String? {
        return PasswordSafe.instance.getPassword(credentialAttributes)
    }

    fun setManualToken(token: String) {
        PasswordSafe.instance.setPassword(credentialAttributes, token)
    }

    fun clearManualToken() {
        PasswordSafe.instance.setPassword(credentialAttributes, null)
    }
}
