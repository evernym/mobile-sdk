package msdk.kotlin.sample.types

import android.content.Context
import msdk.kotlin.sample.logger.Logger.Companion.instance
import msdk.kotlin.sample.utils.CommonUtils
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

object PoolConfig {
    fun builder(): PoolConfigBuilder {
        return PoolConfigBuilder()
    }

    fun writeGenesisFile(context: Context, genesisPoolResId: Int?): File {
        val genesisFile =
            File(CommonUtils.getRootDir(context), "pool_transactions_genesis")
        if (!genesisFile.exists()) {
            try {
                FileOutputStream(genesisFile).use { stream ->
                    instance.d("writing poolTxnGenesis to file: " + genesisFile.absolutePath)
                    if (genesisPoolResId != null) {
                        context.resources.openRawResource(genesisPoolResId)
                            .use { genesisStream ->
                                val buffer = ByteArray(8 * 1024)
                                var bytesRead: Int
                                while (genesisStream.read(buffer)
                                        .also { bytesRead = it } != -1
                                ) {
                                    stream.write(buffer, 0, bytesRead)
                                }
                            }
                    }
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
        return genesisFile
    }

    class PoolConfigBuilder {
        private var genesisPath: String? = null
        private var poolName: String? = null

        /**
         * Set genesis transactions path
         *
         * @param genesisPath path to genesis transactions
         * @return this
         */
        fun withgGenesisPath(genesisPath: String): PoolConfigBuilder {
            this.genesisPath = genesisPath
            return this
        }

        /**
         * Set pool name
         *
         * @param poolName pool name
         * @return this
         */
        fun withgPoolName(poolName: String): PoolConfigBuilder {
            this.poolName = poolName
            return this
        }

        /**
         * Creates pool initialization config
         *
         * @return pool initialization config
         */
        @Throws(JSONException::class)
        fun build(): String {
            val poolConfig = JSONObject()
            poolConfig.put("genesis_path", genesisPath)
            poolConfig.put("pool_name", poolName)
            return poolConfig.toString()
        }
    }
}