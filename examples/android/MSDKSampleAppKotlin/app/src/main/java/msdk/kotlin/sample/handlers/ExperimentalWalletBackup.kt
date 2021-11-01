package msdk.kotlin.sample.handlers

import androidx.annotation.experimental.Experimental
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

@Retention(RetentionPolicy.CLASS)
@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER
)
@Experimental(level = Experimental.Level.ERROR)
annotation class ExperimentalWalletBackup