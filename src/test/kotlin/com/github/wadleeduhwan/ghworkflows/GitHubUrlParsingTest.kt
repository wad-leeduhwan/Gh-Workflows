package com.github.wadleeduhwan.ghworkflows

import com.github.wadleeduhwan.ghworkflows.git.GitHubRepo
import junit.framework.TestCase

class GitHubUrlParsingTest : TestCase() {

    fun testSshUrl() {
        val repo = parseTestUrl("git@github.com:owner/repo.git")
        assertNotNull(repo)
        assertEquals("owner", repo!!.owner)
        assertEquals("repo", repo.name)
    }

    fun testHttpsUrl() {
        val repo = parseTestUrl("https://github.com/owner/repo.git")
        assertNotNull(repo)
        assertEquals("owner", repo!!.owner)
        assertEquals("repo", repo.name)
    }

    fun testHttpsUrlWithoutGit() {
        val repo = parseTestUrl("https://github.com/owner/repo")
        assertNotNull(repo)
        assertEquals("owner", repo!!.owner)
        assertEquals("repo", repo.name)
    }

    fun testNonGithubUrl() {
        val repo = parseTestUrl("https://gitlab.com/owner/repo.git")
        assertNull(repo)
    }

    /**
     * Uses the same regex logic as GitRepositoryHelper for unit testing without IDE dependencies.
     */
    private fun parseTestUrl(url: String): GitHubRepo? {
        val sshPattern = Regex("""git@github\.com:([^/]+)/([^/.]+)(?:\.git)?""")
        sshPattern.find(url)?.let {
            return GitHubRepo(it.groupValues[1], it.groupValues[2])
        }
        val httpsPattern = Regex("""https?://github\.com/([^/]+)/([^/.]+)(?:\.git)?""")
        httpsPattern.find(url)?.let {
            return GitHubRepo(it.groupValues[1], it.groupValues[2])
        }
        return null
    }
}
