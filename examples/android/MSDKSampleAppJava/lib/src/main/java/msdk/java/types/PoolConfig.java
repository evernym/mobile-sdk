package msdk.java.types;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.RawRes;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import msdk.java.logger.Logger;
import msdk.java.utils.CommonUtils;

public class PoolConfig {
    public static PoolConfigBuilder builder() {
        return new PoolConfigBuilder();
    }

    public static File writeGenesisFile(Context context, Integer genesisPoolResId) {
        File genesisFile = new File(CommonUtils.getRootDir(context), "pool_transactions_genesis");
        if (!genesisFile.exists()) {
            try (FileOutputStream stream = new FileOutputStream(genesisFile)) {
                Logger.getInstance().d("writing poolTxnGenesis to file: " + genesisFile.getAbsolutePath());
                if (genesisPoolResId != null) {
                    try (InputStream genesisStream = context.getResources().openRawResource(genesisPoolResId)) {
                        byte[] buffer = new byte[8 * 1024];
                        int bytesRead;
                        while ((bytesRead = genesisStream.read(buffer)) != -1) {
                            stream.write(buffer, 0, bytesRead);
                        }
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return genesisFile;
    }

    public static final class PoolConfigBuilder {
        private String genesisPath;
        private String poolName;

        private PoolConfigBuilder() {
        }

        /**
         * Set genesis transactions path
         *
         * @param genesisPath path to genesis transactions
         * @return this
         */
        public @NonNull
        PoolConfigBuilder withgGenesisPath(@NonNull String genesisPath) {
            this.genesisPath = genesisPath;
            return this;
        }

        /**
         * Set pool name
         *
         * @param poolName pool name
         * @return this
         */
        public @NonNull
        PoolConfigBuilder withgPoolName(@NonNull String poolName) {
            this.poolName = poolName;
            return this;
        }

        /**
         * Creates pool initialization config
         *
         * @return pool initialization config
         */
        public @NonNull
        String build() throws JSONException {
            JSONObject poolConfig = new JSONObject();
            poolConfig.put("genesis_path", this.genesisPath);
            poolConfig.put("pool_name", this.poolName);
            return poolConfig.toString();
        }
    }
}
