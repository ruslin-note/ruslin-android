package org.dianqk.ruslin.ui.component

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.res.AssetManager
import android.net.Uri
import android.util.Log
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.widget.Toast
import androidx.annotation.WorkerThread
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import com.google.accompanist.web.AccompanistWebViewClient
import com.google.accompanist.web.WebView
import com.google.accompanist.web.rememberWebViewStateWithHTMLData
import dagger.hilt.android.lifecycle.HiltViewModel
import org.dianqk.ruslin.data.NotesRepository
import java.io.*
import java.net.URLConnection
import java.util.zip.GZIPInputStream
import javax.inject.Inject

const val TAG = "MarkdownRichText"

data class LocalResource(
    val file: File,
    val mime: String,
)

@HiltViewModel
class MarkdownRichTextViewModel @Inject constructor(
    private val notesRepository: NotesRepository,
) : ViewModel() {

    fun loadLocalResource(id: String): LocalResource {
        val resource = notesRepository.loadResource(id = id)
        val file = notesRepository.resourceDir.resolve(buildString {
            append(resource.id)
            if (resource.fileExtension.isNotEmpty()) {
                append(".")
                append(resource.fileExtension)
            }
        })
        return LocalResource(file = file, mime = resource.mime)
    }

}

@Composable
fun MarkdownRichText(
    viewModel: MarkdownRichTextViewModel = hiltViewModel(),
    htmlBodyText: String
) {
    val context = LocalContext.current
    val webViewState = rememberWebViewStateWithHTMLData(
        data = htmlBodyText,
    )

    val client = remember {
        LocalContentWebViewClient(
            context = context,
            getResource = viewModel::loadLocalResource,
            handleUrl = { url ->
                try {
                    context.startActivity(Intent(Intent.ACTION_VIEW, url))
                } catch (e: ActivityNotFoundException) {
                    Toast.makeText(context, e.localizedMessage ?: e.toString(), Toast.LENGTH_SHORT).show()
                }
                true
            })
    }

    WebView(
        state = webViewState,
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        onCreated = { webView ->
            val webViewSettings = webView.settings
            webViewSettings.allowFileAccess = false
            webViewSettings.allowContentAccess = false
        },
        client = client
    )
}

private class LocalContentWebViewClient(
    context: Context,
    getResource: (String) -> LocalResource,
    private val handleUrl: (Uri) -> Boolean
) :
    AccompanistWebViewClient() {

    private val localContentPathHandler =
        LocalContentPathHandler(context = context, getResource = getResource)

    override fun shouldInterceptRequest(
        view: WebView,
        request: WebResourceRequest
    ): WebResourceResponse? {
        val url = request.url
        val path = url.path ?: return null
        if (url.scheme == ASSETS_SCHEME) {
            return localContentPathHandler.handleAsset(path = path)
        } else if (url.scheme == FILES_SCHEME) {
            return localContentPathHandler.handleFile(path = path)
        }
        return null
    }

    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        return request?.url?.let { handleUrl(it) } ?: super.shouldOverrideUrlLoading(view, request)
    }

    companion object {
        private const val ASSETS_SCHEME = "ruslin-assets"
        private const val FILES_SCHEME = "ruslin-files"
    }
}

private class LocalContentPathHandler(
    context: Context,
    private val getResource: (String) -> LocalResource
) {

    private var mAssetHelper: AssetHelper = AssetHelper(context)

    @WorkerThread
    fun handleAsset(path: String): WebResourceResponse? {
        return try {
            val `is` = mAssetHelper.openAsset(path)
            val mimeType = AssetHelper.guessMimeType(path)
            WebResourceResponse(mimeType, null, `is`)
        } catch (e: IOException) {
            Log.e(TAG, "Error opening asset path: $path", e)
            WebResourceResponse(null, null, null)
        }
    }

    @WorkerThread
    fun handleFile(path: String): WebResourceResponse? {
        return try {
            val resourceId = AssetHelper.removeLeadingSlash(path = path)
            val localResource = getResource(resourceId)
            val data = AssetHelper.openFile(localResource.file)
            val mimeType = localResource.mime
            WebResourceResponse(mimeType, null, data)
        } catch (e: IOException) {
            Log.e(TAG, "Error opening file path: $path", e)
            WebResourceResponse(null, null, null)
        }
    }
}


/**
 * A Utility class for opening resources, assets and files for
 * [androidx.webkit.WebViewAssetLoader].
 * Forked from the chromuim project org.chromium.android_webview.AndroidProtocolHandler.
 * See androidx.webkit.internal.AssetHelper
 */
class AssetHelper(private val mContext: Context) {
    /**
     * Open an InputStream for an Android asset.
     *
     * @param path Path to the asset file to load.
     * @return An [InputStream] to the Android asset.
     */
    @Throws(IOException::class)
    fun openAsset(path: String): InputStream {
        val removeLeadingSlashPath = removeLeadingSlash(path)
        val assets = mContext.assets
        return handleSvgzStream(
            removeLeadingSlashPath,
            assets.open(removeLeadingSlashPath, AssetManager.ACCESS_STREAMING)
        )
    }

    companion object {
        /**
         * Default value to be used as MIME type if guessing MIME type failed.
         */
        private const val DEFAULT_MIME_TYPE = "text/plain"

        @Throws(IOException::class)
        private fun handleSvgzStream(
            path: String,
            stream: InputStream
        ): InputStream {
            return if (path.endsWith(".svgz")) GZIPInputStream(stream) else stream
        }

        fun removeLeadingSlash(path: String): String {
            if (path.length > 1 && path[0] == '/') {
                return path.substring(1)
            }
            return path
        }

        /**
         * Open an `InputStream` for a file in application data directories.
         *
         * @param file The file to be opened.
         * @return An `InputStream` for the requested file.
         */
        @Throws(FileNotFoundException::class, IOException::class)
        fun openFile(file: File): InputStream {
            val fis = FileInputStream(file)
            return handleSvgzStream(file.path, fis)
        }

        /**
         * Use [URLConnection.guessContentTypeFromName] to guess MIME type or return the
         * [DEFAULT_MIME_TYPE] if it can't guess.
         *
         * @param filePath path of the file to guess its MIME type.
         * @return MIME type guessed from file extension or [DEFAULT_MIME_TYPE].
         */
        fun guessMimeType(filePath: String): String {
            val mimeType = URLConnection.guessContentTypeFromName(filePath)
            return mimeType ?: DEFAULT_MIME_TYPE
        }
    }
}
