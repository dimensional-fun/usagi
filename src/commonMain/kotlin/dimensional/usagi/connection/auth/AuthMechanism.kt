package dimensional.usagi.connection.auth

import dimensional.usagi.protocol.type.LongString

/**
 * Represents an authentication mechanism.
 */
public interface AuthMechanism {
    /**
     * The name of this mechanism, e.g. `PLAIN`
     */
    public val name: String

    /**
     * Handle a single round of challenge-response.
     *
     * @param challenge The challenge this round, or null on first round.
     * @param username  The name of the current user.
     * @param password  Password for [username]
     */
    public suspend fun handleChallenge(challenge: LongString?, username: String, password: String): LongString
}
