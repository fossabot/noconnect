package org.chagolchana.noconnect.android.login;

import android.content.SharedPreferences;

import org.chagolchana.chagol.api.crypto.CryptoComponent;
import org.chagolchana.chagol.api.crypto.CryptoExecutor;
import org.chagolchana.chagol.api.crypto.PasswordStrengthEstimator;
import org.chagolchana.chagol.api.crypto.SecretKey;
import org.chagolchana.chagol.api.db.DatabaseConfig;
import org.chagolchana.chagol.api.nullsafety.NotNullByDefault;
import org.chagolchana.noconnect.android.controller.handler.ResultHandler;

import java.util.concurrent.Executor;

import javax.inject.Inject;

@NotNullByDefault
public class SetupControllerImpl extends PasswordControllerImpl
		implements SetupController {

	private final PasswordStrengthEstimator strengthEstimator;

	@Inject
	SetupControllerImpl(SharedPreferences briarPrefs,
			DatabaseConfig databaseConfig,
			@CryptoExecutor Executor cryptoExecutor, CryptoComponent crypto,
			PasswordStrengthEstimator strengthEstimator) {
		super(briarPrefs, databaseConfig, cryptoExecutor, crypto);
		this.strengthEstimator = strengthEstimator;
	}

	@Override
	public float estimatePasswordStrength(String password) {
		return strengthEstimator.estimateStrength(password);
	}

	@Override
	public void storeAuthorInfo(final String nickname, final String password,
			final ResultHandler<Void> resultHandler) {
		cryptoExecutor.execute(new Runnable() {
			@Override
			public void run() {
				databaseConfig.setLocalAuthorName(nickname);
				SecretKey key = crypto.generateSecretKey();
				databaseConfig.setEncryptionKey(key);
				String hex = encryptDatabaseKey(key, password);
				storeEncryptedDatabaseKey(hex);
				resultHandler.onResult(null);
			}
		});
	}

}
