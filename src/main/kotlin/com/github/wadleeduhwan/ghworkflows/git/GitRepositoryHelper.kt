package com.github.wadleeduhwan.ghworkflows.git

import com.intellij.openapi.project.Project
import git4idea.repo.GitRepositoryManager

data class GitHubRepo(val owner: String, val name: String) {
    val fullName: String get() = "$owner/$name"
}

object GitRepositoryHelper {

    fun getGitHubRepo(project: Project): GitHubRepo? {
        val repositoryManager = GitRepositoryManager.getInstance(project)
        val repository = repositoryManager.repositories.firstOrNull() ?: return null

        for (remote in repository.remotes) {
            for (url in remote.urls) {
                parseGitHubUrl(url)?.let { return it }
            }
        }
        return null
    }

    fun getDefaultBranch(project: Project): String {
        val repositoryManager = GitRepositoryManager.getInstance(project)
        val repository = repositoryManager.repositories.firstOrNull() ?: return "main"
        return repository.currentBranchName ?: "main"
    }

    fun getBranches(project: Project): List<String> {
        val repositoryManager = GitRepositoryManager.getInstance(project)
        val repository = repositoryManager.repositories.firstOrNull() ?: return listOf("main")

        val branches = mutableListOf<String>()

        // 현재 브랜치를 맨 위에
        repository.currentBranchName?.let { branches.add(it) }

        // 로컬 브랜치
        for (branch in repository.branches.localBranches) {
            if (branch.name !in branches) {
                branches.add(branch.name)
            }
        }

        // 리모트 브랜치 (origin/ 접두사 제거, 중복 제외)
        for (branch in repository.branches.remoteBranches) {
            val shortName = branch.name.substringAfter("/")
            if (shortName !in branches) {
                branches.add(shortName)
            }
        }

        return branches.ifEmpty { listOf("main") }
    }

    fun getTags(project: Project): List<String> {
        val repositoryManager = GitRepositoryManager.getInstance(project)
        val repository = repositoryManager.repositories.firstOrNull() ?: return emptyList()
        return repository.tagHolder.getTags().keys.map { it.name }.sortedDescending()
    }

    private fun parseGitHubUrl(url: String): GitHubRepo? {
        // SSH: git@github.com:owner/repo.git
        val sshPattern = Regex("""git@github\.com:([^/]+)/([^/.]+)(?:\.git)?""")
        sshPattern.find(url)?.let {
            return GitHubRepo(it.groupValues[1], it.groupValues[2])
        }

        // HTTPS: https://github.com/owner/repo.git
        val httpsPattern = Regex("""https?://github\.com/([^/]+)/([^/.]+)(?:\.git)?""")
        httpsPattern.find(url)?.let {
            return GitHubRepo(it.groupValues[1], it.groupValues[2])
        }

        return null
    }
}
