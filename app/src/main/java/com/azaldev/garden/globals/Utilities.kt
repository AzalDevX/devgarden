package com.azaldev.garden.globals
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.azaldev.garden.R
import com.azaldev.garden.SettingsActivity
import com.google.zxing.ResultPoint
import com.google.zxing.integration.android.IntentIntegrator
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import java.security.MessageDigest
import java.util.*

object Utilities {

    fun setLocale(context: Context, language: String) {
        val locale = Locale(language)
        Locale.setDefault(locale)

        val resources = context.resources
        val configuration = Configuration(resources.configuration)
        configuration.setLocale(locale)

        context.createConfigurationContext(configuration)
        context.resources.updateConfiguration(configuration, resources.displayMetrics)
    }

    fun hasInternetConnection(context: Context, callback: (Boolean) -> Unit) {
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

            val isConnected: Boolean = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val network = connectivityManager.activeNetwork
                val capabilities = connectivityManager.getNetworkCapabilities(network)

                capabilities != null &&
                        (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR))
            } else {
                val networkInfo = connectivityManager.activeNetworkInfo
                networkInfo != null && networkInfo.isConnected
            }

        callback(isConnected)
    }

    fun showToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun sha256(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        val result = bytes.joinToString("") { "%02x".format(it) }
        return result
    }

    private var shouldStartActivity = true
    fun startActivity(context: Context, targetActivity: Class<out AppCompatActivity>) {
        if (shouldStartActivity) {
            shouldStartActivity = false

            // Start the target activity
            val intent = Intent(context, targetActivity)
            context.startActivity(intent)

            // Use a Handler to delay re-enabling the button
            Handler().postDelayed({
                shouldStartActivity = true
            }, 1000)
        }
    }

    fun scanQRCode(activity: AppCompatActivity, prompt: String) {
        IntentIntegrator(activity)
            .setOrientationLocked(true)
            .setBeepEnabled(false)
            .setBarcodeImageEnabled(true)
            .setPrompt(prompt ?: "Scan a barcode or QR Code")
            .initiateScan()
    }
    /**
     * @see how to get the result of the scanned qrcode
     * protected void onActivityResult(int requestCode, int resultCode, Intent data) {
     *     IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
     *     if(result != null) {
     *         if(result.getContents() == null) {
     *             Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
     *         } else {
     *             Toast.makeText(this, "Scanned: " + result.getContents(), Toast.LENGTH_LONG).show();
     *         }
     *     } else {
     *         super.onActivityResult(requestCode, resultCode, data);
     *     }
     *
     * }
     */

    fun scanQRCodePop(activity: AppCompatActivity, prompt: String, callback: (String?) -> Unit) {
        val scannerDialog = QRCodeScannerDialog(activity)
        scannerDialog.setOnScanResultCallback(callback)
        scannerDialog.setPrompt(prompt ?: "Scan a barcode or QR Code")
        scannerDialog.show()
    }
}

class QRCodeScannerDialog(context: Context) : Dialog(context) {

    private lateinit var barcodeView: DecoratedBarcodeView
    private lateinit var scanResultCallback: (String?) -> Unit
    private var promptText: String = "Scan a barcode or QR Code"

    fun setOnScanResultCallback(callback: (String?) -> Unit) {
        this.scanResultCallback = callback
    }

    fun setPrompt(prompt: String) {
        this.promptText = prompt
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_qrcode_scanner)

        window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )

        barcodeView = findViewById(R.id.barcodeScannerView)
        barcodeView.setStatusText(promptText)
        barcodeView.decodeContinuous(object : BarcodeCallback {
            override fun barcodeResult(result: BarcodeResult?) {
                result?.let {
                    // Handle the scanned result here
                    scanResultCallback(result.text)
                    dismiss()
                }
            }

            override fun possibleResultPoints(resultPoints: MutableList<ResultPoint>?) {
                // Optional callback for possible result points
            }
        })
    }

    // ...
}