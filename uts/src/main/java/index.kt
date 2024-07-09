@file:Suppress("UNCHECKED_CAST", "USELESS_CAST", "INAPPLICABLE_JVM_NAME")
package uts.sdk.modules.zwonbScan;
import android.content.Intent
import com.yidont.barcode.scan.BarcodeScanActivity
import io.dcloud.uts.UTSAndroid
import io.dcloud.uts.UTSCallback
import io.dcloud.uts.UTSJSONObject
import io.dcloud.uts.UTSObject

open class Result (
    open var success: (path: String) -> Unit,
    open var fail: ((res: Any) -> Unit)? = null,
) : UTSObject()
fun scan(result: Result) {
    UTSAndroid.onAppActivityResult(fun(requestCode: Int, resultCode: Int, data: Intent?){
        if (requestCode == 10001) {
            val scanResult = data?.getStringExtra("scan_result") ?: "";
            result.success(scanResult);
            UTSAndroid.offAppActivityResult();
        }
    });
    UTSAndroid.getUniActivity()!!.runOnUiThread {
        val intent = Intent(UTSAndroid.getAppContext()!!, BarcodeScanActivity().javaClass);
        UTSAndroid.getUniActivity()!!.startActivityForResult(intent, 10001);
    }
}
open class ResultJSONObject : UTSJSONObject() {
    open lateinit var success: UTSCallback;
    open var fail: UTSCallback? = null;
}
fun scanByJs(result: ResultJSONObject) {
    return scan(Result(success = fun(path: String): Unit {
        result.success(path);
    }
    , fail = fun(res: Any): Unit {
        result.fail?.invoke(res);
    }
    ));
}
