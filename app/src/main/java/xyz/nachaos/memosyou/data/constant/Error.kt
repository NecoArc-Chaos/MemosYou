package xyz.nachaos.memosyou.data.constant

import xyz.nachaos.memosyou.R
import xyz.nachaos.memosyou.ext.string
import retrofit2.HttpException

class MoeMemosException(string: String) : Exception(string) {
    companion object {
        val notLogin = MoeMemosException("NOT_LOGIN")
        val invalidAccessToken = MoeMemosException("INVALID_ACCESS_TOKEN")
        val accessTokenInvalid = MoeMemosException("ACCESS_TOKEN_INVALID")
        val invalidParameter = MoeMemosException("INVALID_PARAMETER")
        val invalidServer = MoeMemosException("INVALID_SERVER")
        val networkError = MoeMemosException("NETWORK_ERROR")
        val timeout = MoeMemosException("TIMEOUT")
        val serverError = MoeMemosException("SERVER_ERROR")
        val forbidden = MoeMemosException("FORBIDDEN")
        val notFound = MoeMemosException("NOT_FOUND")
        val conflict = MoeMemosException("CONFLICT")
        val tooManyRequests = MoeMemosException("TOO_MANY_REQUESTS")

        /**
         * Creates a MoeMemosException from an HttpException with localized user-facing message.
         */
        fun fromHttpException(e: HttpException): MoeMemosException {
            return when (e.code()) {
                400 -> MoeMemosException("BAD_REQUEST")
                401 -> invalidAccessToken
                403 -> forbidden
                404 -> notFound
                409 -> conflict
                429 -> tooManyRequests
                in 500..599 -> serverError
                else -> MoeMemosException("HTTP_ERROR_${e.code()}")
            }
        }
    }

    override fun getLocalizedMessage(): String? {
        return when (this) {
            invalidAccessToken -> R.string.invalid_access_token.string
            accessTokenInvalid -> R.string.access_token_invalid_relogin.string
            invalidServer -> R.string.invalid_server.string
            networkError -> R.string.network_error.string
            timeout -> R.string.timeout_error.string
            serverError -> R.string.server_error.string
            forbidden -> R.string.forbidden_error.string
            notFound -> R.string.not_found_error.string
            conflict -> R.string.conflict_error.string
            tooManyRequests -> R.string.too_many_requests_error.string
            else -> {
                super.getLocalizedMessage()
            }
        }
    }
}
