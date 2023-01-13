package dimensional.usagi.connection.auth

import dimensional.usagi.connection.auth.AuthMechanisms.External
import dimensional.usagi.connection.auth.AuthMechanisms.Plain
import dimensional.usagi.protocol.LongString

/**
 * Some of our built-in auth mechanisms.
 *
 * @see Plain
 * @see External
 */
public object AuthMechanisms {
    /**
     * Represents the `PLAIN` authentication mechanism.
     */
    public object Plain : AuthMechanism {
        override val name: String = "PLAIN"

        override suspend fun handleChallenge(challenge: LongString?, username: String, password: String): LongString {
            return LongString("\u0000$username\u0000$password")
        }
    }

    /**
     * Represents the `EXTERNAL` authentication mechanism.
     */
    public object External : AuthMechanism {
        override val name: String = "EXTERNAL"

        override suspend fun handleChallenge(challenge: LongString?, username: String, password: String): LongString {
            return LongString(byteArrayOf())
        }
    }
}
