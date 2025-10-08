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
import java.security.cert.CertificateException
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.security.spec.PKCS8EncodedKeySpec
import java.util.concurrent.TimeUnit
import javax.net.ssl.*
import com.example.testbioprocessor.BuildConfig

object NetworkModule {

    private val TAG = "NetworkModule"

    // Пути к файлам в assets
    private const val CERTIFICATE_ASSET_PATH = "certs/client.crt"
    private const val PRIVATE_KEY_ASSET_PATH = "certs/client.key"

    // Кэш для Retrofit
    private val retrofitCache = mutableMapOf<Context, Retrofit>()

    fun <T> createApi(context: Context, serviceClass: Class<T>): T {
        val baseUrl = getBaseUrl()
        Log.d(TAG, "Creating API with BASE_URL: $baseUrl")

        val retrofit = retrofitCache.getOrPut(context) {
            createRetrofit(context)
        }
        return retrofit.create(serviceClass)
    }

    private fun createRetrofit(context: Context): Retrofit {
        return Retrofit.Builder()
            .baseUrl(getBaseUrl())
            .client(createOkHttpClient(context))
            .addConverterFactory(GsonConverterFactory.create(createGson()))
            .build()
    }

    private fun getBaseUrl(): String {
        return BuildConfig.BASE_URL
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
        try {
            Log.d(TAG, "Creating mTLS client...")

            val certificate = loadCertificateFromAssets(context, CERTIFICATE_ASSET_PATH)
            val privateKey = loadPrivateKeyFromAssets(context)

            val keyManagers = createKeyManagers(certificate, privateKey)

            val trustManager = createUnsafeTrustManager()

            // Создаем SSLContext
            val sslContext = SSLContext.getInstance("TLS")
            sslContext.init(keyManagers, arrayOf(trustManager), null)

            Log.d(TAG, "mTLS client created successfully")

            return OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .sslSocketFactory(sslContext.socketFactory, trustManager)
                .hostnameVerifier { _, _ -> true }
                .build()

        } catch (e: Exception) {
            Log.e(TAG, "Error creating mTLS client", e)
            throw RuntimeException("Failed to create mTLS client: ${e.message}", e)
        }
    }

    private fun createUnsafeTrustManager(): X509TrustManager {
        return object : X509TrustManager {
            @Throws(CertificateException::class)
            override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {
                Log.d(TAG, "Client trusted: ${chain.firstOrNull()?.subjectDN}")
            }

            @Throws(CertificateException::class)
            override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
                Log.d(TAG, "Server trusted: ${chain.firstOrNull()?.subjectDN}")
            }

            override fun getAcceptedIssuers(): Array<X509Certificate> {
                return emptyArray()
            }
        }
    }

    private fun loadCertificateFromAssets(context: Context, path: String): X509Certificate {
        return try {
            context.assets.open(path).use { inputStream ->
                val certificateFactory = CertificateFactory.getInstance("X.509")
                val certificate = certificateFactory.generateCertificate(inputStream) as X509Certificate
                Log.d(TAG, "Certificate loaded: ${certificate.subjectDN}")
                Log.d(TAG, "Certificate issuer: ${certificate.issuerDN}")
                Log.d(TAG, "Certificate expiry: ${certificate.notAfter}")
                certificate
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load certificate from assets: $path", e)
            throw IllegalArgumentException("Failed to load certificate from assets: ${e.message}", e)
        }
    }

    private fun loadPrivateKeyFromAssets(context: Context): PrivateKey {
        return try {
            context.assets.open(PRIVATE_KEY_ASSET_PATH).use { inputStream ->
                val keyBytes = inputStream.readBytes()
                val privateKey = parsePrivateKey(keyBytes)
                Log.d(TAG, "Private key loaded successfully, algorithm: ${privateKey.algorithm}")
                privateKey
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load private key from assets", e)
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
        val keyManagers = keyManagerFactory.keyManagers
        Log.d(TAG, "KeyManagers created: ${keyManagers.size}")
        return keyManagers
    }

    private fun createKeyStoreWithClientCertificate(
        certificate: X509Certificate,
        privateKey: PrivateKey
    ): KeyStore {
        val keyStore = KeyStore.getInstance("PKCS12")
        keyStore.load(null, null)

        keyStore.setKeyEntry(
            "client",
            privateKey,
            "".toCharArray(),
            arrayOf(certificate)
        )

        Log.d(TAG, "KeyStore created with client certificate")
        return keyStore
    }

    private fun parsePrivateKey(keyBytes: ByteArray): PrivateKey {
        val keyString = String(keyBytes)

        // Убираем все возможные заголовки и форматирование
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
            // Пробуем PKCS8 формат (наиболее распространенный)
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
                Log.e(TAG, "Failed to parse private key as RSA or EC", e2)
                throw RuntimeException("Unsupported key format. Expected RSA or EC PKCS8", e2)
            }
        }
    }

    private fun checkAssetsFiles(context: Context) {
        try {
            Log.d(TAG, "Checking assets files...")

            val files = context.assets.list("")
            Log.d(TAG, "Root assets files: ${files?.joinToString()}")

            val certFiles = context.assets.list("certs")
            Log.d(TAG, "Files in certs folder: ${certFiles?.joinToString()}")

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

            if (!certificateExists) {
                throw IllegalArgumentException("Certificate file not found: $CERTIFICATE_ASSET_PATH")
            }
            if (!keyExists) {
                throw IllegalArgumentException("Private key file not found: $PRIVATE_KEY_ASSET_PATH")
            }

            Log.d(TAG, "All required assets files found")

        } catch (e: Exception) {
            Log.e(TAG, "Error checking assets files", e)
            throw e
        }
    }
}