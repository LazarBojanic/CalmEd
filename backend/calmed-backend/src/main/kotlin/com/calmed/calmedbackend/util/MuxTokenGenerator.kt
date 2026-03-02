import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import java.security.KeyFactory
import java.security.interfaces.RSAPrivateKey
import java.security.spec.PKCS8EncodedKeySpec
import java.time.Instant
import java.util.Base64
import java.util.Date


object MuxTokenGenerator {

    private fun loadPrivateKeyPkcs8(pem: String): RSAPrivateKey {
        val normalizedPem = pem.replace("\\n", "\n").replace("\r", "")
        var cleaned = normalizedPem
            .replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "")
            .replace("\\n", "")
            .replace("\n", "")
            .replace("\r", "")
            .replace(" ", "")

        cleaned = cleaned.replace(Regex("[^A-Za-z0-9+/=]"), "")

        while (cleaned.length % 4 != 0) {
            cleaned += "="
        }

        val bytes = Base64.getDecoder().decode(cleaned)
        val spec = PKCS8EncodedKeySpec(bytes)

        val key = KeyFactory.getInstance("RSA").generatePrivate(spec)
        return key as RSAPrivateKey
    }

    fun generatePlaybackToken(
        playbackId: String,
        kid: String,
        privateKeyPemPkcs8: String
    ): String {
        val privateKey = loadPrivateKeyPkcs8(privateKeyPemPkcs8)
        val alg = Algorithm.RSA256(null, privateKey)

        val exp = Date(System.currentTimeMillis() + 60 * 60 * 1000)

        return JWT.create()
            .withKeyId(kid)
            .withAudience("v")
            .withSubject(playbackId)
            .withExpiresAt(exp)
            .sign(alg)
    }
}