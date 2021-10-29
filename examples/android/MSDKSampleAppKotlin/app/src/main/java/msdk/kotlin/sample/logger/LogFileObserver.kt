package msdk.kotlin.sample.logger

import android.os.FileObserver
import java.io.File
import java.io.FileWriter
import java.io.IOException

class LogFileObserver//this.startLogcat(this.absolutePath); //     this.process.destroy();
// }
// public void startLogcat(String filename) {
//     try {
//         String cmd = "logcat -f "+filename;
//         this.process = Runtime.getRuntime().exec(cmd);
//     } catch (IOException e) {
//         e.printStackTrace();
//     }
// }
    (var absolutePath: String, var MAX_ALLOWED_FILE_BYTES: Int) :
    FileObserver(absolutePath, ALL_EVENTS) {
    var process: Process? = null
    override fun onEvent(event: Int, path: String?) {

        //data was written to a file
        if (MODIFY and event != 0) {
            val logFile = File(absolutePath)
            if (logFile.length() > MAX_ALLOWED_FILE_BYTES) {
                try {
                    FileWriter(absolutePath).close()
                } catch (ex: IOException) {
                    ex.printStackTrace()
                }
            }
        } else if (DELETE_SELF and event != 0) {
            stopWatching()
            try {
                FileWriter(absolutePath).close()
            } catch (ex: IOException) {
                ex.printStackTrace()
            }
            startWatching()
        }
    } // public void stopLogcat() {

}