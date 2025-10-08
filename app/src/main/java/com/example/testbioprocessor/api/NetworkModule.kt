import android.content.ContentValues.TAG
import android.content.Context
import android.util.Base64
import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.*
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.security.spec.PKCS8EncodedKeySpec
import java.util.concurrent.TimeUnit
import javax.net.ssl.*

object NetworkModule {

    private const val BASE_URL = "http://10.0.2.2:8000"

    // Пути к файлам в assets
    private const val CERTIFICATE_ASSET_PATH = "certs/client.crt"
    private const val PRIVATE_KEY_ASSET_PATH = "certs/client.key"

    // Кэш для Retrofit (по контексту)
    private val retrofitCache = mutableMapOf<Context, Retrofit>()

    fun <T> createApi(context: Context, serviceClass: Class<T>): T {
        val retrofit = retrofitCache.getOrPut(context) {
            createRetrofit(context)
        }
        return retrofit.create(serviceClass)
    }

    private fun createRetrofit(context: Context): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(createOkHttpClient(context))
            .addConverterFactory(GsonConverterFactory.create(createGson()))
            .build()
    }

    private fun createGson(): Gson {
        return GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
            .create()
    }

    private fun createOkHttpClient(context: Context): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        checkAssetsFiles(context)
        // Создаем базовый клиент с mTLS
        val baseClient = createOkHttpClientWithMTLS(context)

        // Добавляем интерцепторы к базовому клиенту
        return baseClient.newBuilder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor { chain ->
                val original = chain.request()
                val requestBuilder = original.newBuilder()
                    .header("Content-Type", "application/json")
                    .method(original.method, original.body)
                chain.proceed(requestBuilder.build())
            }
            .build()
    }

    private fun createOkHttpClientWithMTLS(context: Context): OkHttpClient {
        // Загружаем сертификат и ключ напрямую из assets
        val certificate = loadCertificateFromAssets(context)
        val privateKey = loadPrivateKeyFromAssets(context)

        // Создаем KeyManager который содержит наш клиентский сертификат и ключ
        val keyManagers = createKeyManagers(certificate, privateKey)

        // Создаем SSLContext с нашими KeyManager'ами
        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(
            keyManagers, // наши клиентские сертификаты
            getTrustManagers(), // доверенные сертификаты (системные по умолчанию)
            null // SecureRandom по умолчанию
        )

        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .sslSocketFactory(sslContext.socketFactory, getX509TrustManager())
            .build()
    }

    private fun loadCertificateFromAssets(context: Context): X509Certificate {
        return try {
            context.assets.open(CERTIFICATE_ASSET_PATH).use { inputStream ->
                val certificateFactory = CertificateFactory.getInstance("X.509")
                certificateFactory.generateCertificate(inputStream) as X509Certificate
            }
        } catch (e: Exception) {
            throw IllegalArgumentException("Failed to load certificate from assets: ${e.message}", e)
        }
    }

    private fun loadPrivateKeyFromAssets(context: Context): PrivateKey {
        return try {
            context.assets.open(PRIVATE_KEY_ASSET_PATH).use { inputStream ->
                val keyBytes = inputStream.readBytes()
                parsePrivateKey(keyBytes)
            }
        } catch (e: Exception) {
            throw IllegalArgumentException("Failed to load private key from assets: ${e.message}", e)
        }
    }

    private fun createKeyManagers(
        certificate: X509Certificate,
        privateKey: PrivateKey
    ): Array<KeyManager> {
        val keyStore = createKeyStoreWithClientCertificate(certificate, privateKey)
        val keyManagerFactory = KeyManagerFactory.getInstance(
            KeyManagerFactory.getDefaultAlgorithm()
        )
        keyManagerFactory.init(keyStore, "".toCharArray())
        return keyManagerFactory.keyManagers
    }

    private fun createKeyStoreWithClientCertificate(
        certificate: X509Certificate,
        privateKey: PrivateKey
    ): KeyStore {
        // Создаем KeyStore и добавляем в него ключ и сертификат
        val keyStore = KeyStore.getInstance("PKCS12")
        keyStore.load(null, null)

        keyStore.setKeyEntry(
            "client", // алиас
            privateKey, // приватный ключ
            "".toCharArray(), // пароль для ключа (пустой)
            arrayOf(certificate) // цепочка сертификатов (в нашем случае один)
        )

        return keyStore
    }

    private fun parsePrivateKey(keyBytes: ByteArray): PrivateKey {
        val keyString = String(keyBytes)
        val privateKeyPEM = keyString
            .replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "")
            .replace("-----BEGIN RSA PRIVATE KEY-----", "")
            .replace("-----END RSA PRIVATE KEY-----", "")
            .replace("-----BEGIN EC PRIVATE KEY-----", "")
            .replace("-----END EC PRIVATE KEY-----", "")
            .replace("\\s".toRegex(), "")

        val encoded = Base64.decode(privateKeyPEM, Base64.DEFAULT)

        return try {
            // Пробуем PKCS8 формат
            val keySpec = PKCS8EncodedKeySpec(encoded)
            val keyFactory = KeyFactory.getInstance("RSA")
            keyFactory.generatePrivate(keySpec)
        } catch (e: Exception) {
            try {
                // Пробуем EC ключ
                val keySpec = PKCS8EncodedKeySpec(encoded)
                val keyFactory = KeyFactory.getInstance("EC")
                keyFactory.generatePrivate(keySpec)
            } catch (e2: Exception) {
                throw RuntimeException("Unsupported key format", e2)
            }
        }
    }

    private fun getTrustManagers(): Array<TrustManager> {
        return arrayOf(getX509TrustManager())
    }

    private fun getX509TrustManager(): X509TrustManager {
        val trustManagerFactory = TrustManagerFactory.getInstance(
            TrustManagerFactory.getDefaultAlgorithm()
        )
        trustManagerFactory.init(null as KeyStore?)
        return trustManagerFactory.trustManagers
            .first { it is X509TrustManager } as X509TrustManager
    }

    private fun checkAssetsFiles(context: Context) {
        try {
            // Получаем список всех файлов в assets
            val files = context.assets.list("")
            Log.d(TAG, "Root assets files: ${files?.joinToString()}")

            files?.forEach { file ->
                try {
                    val subFiles = context.assets.list(file)
                    Log.d(TAG, "Folder '$file': ${subFiles?.joinToString()}")
                } catch (e: Exception) {
                    Log.d(TAG, "'$file' is a file")
                }
            }

            // Проверяем папку certs
            val certFiles = context.assets.list("certs")
            Log.d(TAG, "Files in certs folder: ${certFiles?.joinToString()}")

            // Проверяем существование конкретных файлов
            val certificateExists = try {
                context.assets.open(CERTIFICATE_ASSET_PATH).close()
                true
            } catch (e: Exception) {
                false
            }

            val keyExists = try {
                context.assets.open(PRIVATE_KEY_ASSET_PATH).close()
                true
            } catch (e: Exception) {
                false
            }

            Log.d(TAG, "Certificate exists: $certificateExists")
            Log.d(TAG, "Private key exists: $keyExists")

            if (!certificateExists || !keyExists) {
                throw IllegalArgumentException("Required files not found in assets. Certificate: $certificateExists, Key: $keyExists")
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error checking assets files", e)
            throw e
        }
    }
}