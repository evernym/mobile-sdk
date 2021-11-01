package msdk.java.handlers;

import androidx.annotation.experimental.Experimental;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.CLASS)
@Target({ElementType.METHOD})
@Experimental(level = Experimental.Level.ERROR)
public @interface ExperimentalWalletBackup {
}
