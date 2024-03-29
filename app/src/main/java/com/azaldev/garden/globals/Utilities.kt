package com.azaldev.garden.globals
import android.app.AlertDialog
import android.app.Dialog
import android.provider.Settings
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.location.Location
import android.media.MediaPlayer
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.azaldev.garden.R
import com.google.zxing.ResultPoint
import com.google.zxing.integration.android.IntentIntegrator
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import com.journeyapps.barcodescanner.ViewfinderView
import java.net.HttpURLConnection
import java.net.InetAddress
import java.net.URL
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



    fun canConnectToApi(callback: (Boolean) -> Unit) {
        Thread {
            try {
                val url = URL(Globals.api_url + "/status")
                val connection = url.openConnection() as HttpURLConnection
                connection.connectTimeout = 5000

                val responseCode = connection.responseCode
                val hasConnection = responseCode == HttpURLConnection.HTTP_OK

                Log.i("devl|utils", "Connection to API status: $hasConnection with code $responseCode")
                callback(hasConnection)
            } catch (e: Exception) {
                Log.e("devl|utils", "Error while checking internet connection: ${e.message}")
                callback(false)
            }
        }.start()
    }
    fun hasInternetConnection(context: Context, callback: (Boolean) -> Unit) {
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

            val isConnected: Boolean = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val network = connectivityManager.activeNetwork
                val capabilities = connectivityManager.getNetworkCapabilities(network)

                capabilities != null &&
                        (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) &&
                                    Globals.ws_api_status
            } else {
                val networkInfo = connectivityManager.activeNetworkInfo
                networkInfo != null && networkInfo.isConnected && Globals.ws_api_status
            }

        callback(isConnected)
    }

    fun openGoogleMapsWithDirections(context: Context, latitude: Double, longitude: Double) {
        val gmmIntentUri = Uri.parse("google.navigation:q=$latitude,$longitude&mode=w")

        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        mapIntent.setPackage("com.google.android.apps.maps")

        if (mapIntent.resolveActivity(context.packageManager) != null)
            context.startActivity(mapIntent)
    }

    fun setBrightness(context : Context, brightnessValue: Int) {
        Settings.System.putInt(
            context.contentResolver,
            Settings.System.SCREEN_BRIGHTNESS,
            brightnessValue
        )
    }

    fun getBrightness(context: Context): Int {
        return Settings.System.getInt(
            context.contentResolver,
            Settings.System.SCREEN_BRIGHTNESS
        )
    }

    fun isLocationWithinRadius(
        currentLatitude: Double,
        currentLongitude: Double,
        targetLatitude: Double,
        targetLongitude: Double,
        radiusMeters: Float
    ): Boolean {
        val distance = calculateDistance(
            currentLatitude, currentLongitude,
            targetLatitude, targetLongitude
        )
        return distance <= radiusMeters
    }

    fun calculateDistance(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Float {
        val results = FloatArray(1)
        Location.distanceBetween(lat1, lon1, lat2, lon2, results)
        return results[0]
    }

    fun playSound(context: Context, resId: Int, callback: () -> Unit) {
        val mediaPlayer = MediaPlayer.create(context, resId)
        mediaPlayer.start()
        mediaPlayer.setOnCompletionListener { mediaPlayer.release(); callback() }
    }

    fun showToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun showToast(context: Context, message: String, durationMillis: Long) {
        val toast = Toast.makeText(context, message, Toast.LENGTH_LONG)
        toast.show()

        // Use a Handler to delay the dismissal of the Toast
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
            toast.cancel()
        }, durationMillis)
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


            try {
                // Start the target activity
                val intent = Intent(context, targetActivity)
                context.startActivity(intent)
            } catch (e: Exception) {
                Log.e("devl|utils", "Null pointer exception handled by doing nothing. ${e.message}")
            }

            // Use a Handler to delay re-enabling the button
            Handler().postDelayed({
                shouldStartActivity = true
            }, 500)
        }
    }

    fun showErrorAlert(context: Context, message: String, onAccept: () -> Unit) {
        AlertDialog.Builder(context)
            .setTitle("Error")
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
                onAccept.invoke()
            }
            .show()
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
        if (!PermissionUtils.checkAndRequestCameraPermission(activity)) callback(null)

        val fragment = QRCodeScannerDialogFragment()
        fragment.setOnScanResultCallback(callback)
        fragment.setPrompt(prompt)
        fragment.show(activity.supportFragmentManager, "QRCodeScannerDialogFragment")
    }

    fun isValidCode(scannedText: String?): Boolean {
        // Define the regex pattern for "word-word"
        val regexPattern = Regex("""^[a-zA-Z]+-[a-zA-Z]+$""")

        // Check if the scanned text matches the pattern
        return scannedText != null && regexPattern.matches(scannedText)
    }
}

class QRCodeScannerDialogFragment : DialogFragment(), BarcodeCallback {

    private lateinit var barcodeView: DecoratedBarcodeView
    private lateinit var scanResultCallback: (String?) -> Unit
    private var promptText: String = "Scan a barcode or QR Code"

    fun setOnScanResultCallback(callback: (String?) -> Unit) {
        this.scanResultCallback = callback
    }

    fun setPrompt(prompt: String) {
        this.promptText = prompt
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_qrcode_scanner, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val barcodeBg = view.findViewById<RelativeLayout>(R.id.barcodeBg);

        barcodeView = view.findViewById(R.id.barcodeScannerView)
        barcodeView.decodeSingle(this)


        // Set background color with opacity programmatically
//        val backgroundColor = ContextCompat.getColor(requireContext(), R.color.gray_transparent)
//        barcodeView.setBackgroundColor(backgroundColor)
//        barcodeBg.setBackgroundColor(backgroundColor)

        val viewfinderView: ViewfinderView = barcodeView.findViewById(R.id.zxing_viewfinder_view)
        val viewWrapperView: DecoratedBarcodeView = barcodeView.findViewById(R.id.barcodeScannerView)
        viewfinderView.visibility = View.VISIBLE

        barcodeView.setStatusText(promptText)

        // Set maximum width and height programmatically
        val maxWidth = resources.getDimensionPixelSize(R.dimen.max_qrcode_width)
        val maxHeight = resources.getDimensionPixelSize(R.dimen.max_qrcode_height)

        barcodeView.layoutParams.width = maxWidth
        barcodeView.layoutParams.height = maxHeight
    }

    override fun onResume() {
        super.onResume()
        barcodeView.resume()
    }

    override fun onPause() {
        super.onPause()
        barcodeView.pause()
    }

    override fun barcodeResult(result: BarcodeResult?) {
        result?.let {
            scanResultCallback(result.text)
            dismiss()
        }
    }

    override fun possibleResultPoints(resultPoints: MutableList<ResultPoint>?) {
        // Optional callback for possible result points
    }
}