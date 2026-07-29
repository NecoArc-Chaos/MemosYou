package xyz.nachaos.memosyou.data.constant

import org.junit.Assert.assertEquals
import org.junit.Test

class MemosVersionSupportTest {

    @Test
    fun `V0 version below minimum is too low`() {
        assertV0Policy("0.20.0", VersionPolicy.TOO_LOW)
    }

    @Test
    fun `V0 version at minimum is supported`() {
        assertV0Policy("0.21.0", VersionPolicy.SUPPORTED)
    }

    @Test
    fun `V0 version above minimum is supported`() {
        assertV0Policy("0.25.0", VersionPolicy.SUPPORTED)
    }

    @Test
    fun `V0 empty version is too low`() {
        assertV0Policy("", VersionPolicy.TOO_LOW)
    }

    @Test
    fun `V0 invalid semver is supported`() {
        assertV0Policy("not-a-version", VersionPolicy.SUPPORTED)
    }

    @Test
    fun `V1 version below minimum is too low`() {
        assertV1Policy("0.26.0", VersionPolicy.TOO_LOW)
    }

    @Test
    fun `V1 version at minimum is supported`() {
        assertV1Policy("0.27.0", VersionPolicy.SUPPORTED)
    }

    @Test
    fun `V1 version within range is supported`() {
        assertV1Policy("0.28.5", VersionPolicy.SUPPORTED)
    }

    @Test
    fun `V1 version at maximum is supported`() {
        assertV1Policy("0.30.0", VersionPolicy.SUPPORTED)
    }

    @Test
    fun `V1 version above maximum is V1_HIGHER`() {
        assertV1Policy("0.31.0", VersionPolicy.V1_HIGHER)
    }

    @Test
    fun `V1 empty version is too low`() {
        assertV1Policy("", VersionPolicy.TOO_LOW)
    }

    @Test
    fun `V1 invalid semver is V1_HIGHER`() {
        assertV1Policy("not-a-version", VersionPolicy.V1_HIGHER)
    }

    @Test
    fun `ACCOUNT_NOT_SET is too low`() {
        assertPolicy(UserData.AccountCase.ACCOUNT_NOT_SET, "0.28.0", VersionPolicy.TOO_LOW)
    }

    @Test
    fun `LOCAL account is too low`() {
        assertPolicy(UserData.AccountCase.LOCAL, "0.28.0", VersionPolicy.TOO_LOW)
    }

    private fun assertV0Policy(version: String, expected: VersionPolicy) {
        assertPolicy(UserData.AccountCase.MEMOS_V0, version, expected)
    }

    private fun assertV1Policy(version: String, expected: VersionPolicy) {
        assertPolicy(UserData.AccountCase.MEMOS_V1, version, expected)
    }

    private fun assertPolicy(accountCase: UserData.AccountCase, version: String, expected: VersionPolicy) {
        val actual = when (accountCase) {
            UserData.AccountCase.MEMOS_V0 -> {
                val versionName = version.trim()
                val semVer = SemVer.parseOrNull(versionName)
                when {
                    versionName.isEmpty() -> VersionPolicy.TOO_LOW
                    semVer == null -> VersionPolicy.SUPPORTED
                    semVer < MEMOS_V0_MIN_VERSION -> VersionPolicy.TOO_LOW
                    else -> VersionPolicy.SUPPORTED
                }
            }
            UserData.AccountCase.MEMOS_V1 -> {
                val versionName = version.trim()
                val semVer = SemVer.parseOrNull(versionName)
                when {
                    versionName.isEmpty() -> VersionPolicy.TOO_LOW
                    semVer == null -> VersionPolicy.V1_HIGHER
                    semVer < MEMOS_V1_MIN_VERSION -> VersionPolicy.TOO_LOW
                    semVer > MEMOS_V1_MAX_VERSION -> VersionPolicy.V1_HIGHER
                    else -> VersionPolicy.SUPPORTED
                }
            }
            else -> VersionPolicy.TOO_LOW
        }
        assertEquals(expected, actual)
    }
}
