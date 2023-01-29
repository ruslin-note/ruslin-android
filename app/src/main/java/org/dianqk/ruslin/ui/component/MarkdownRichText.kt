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
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import com.google.accompanist.web.AccompanistWebViewClient
import com.google.accompanist.web.WebView
import com.google.accompanist.web.rememberWebViewStateWithHTMLData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.dianqk.ruslin.BuildConfig
import org.dianqk.ruslin.data.NotesRepository
import org.dianqk.ruslin.ui.component.LocalContentWebViewClient.Companion.FILES_SCHEME
import org.dianqk.ruslin.ui.component.LocalContentWebViewClient.Companion.NOTES_SCHEME
import org.dianqk.ruslin.ui.ext.getCacheSharedDir
import java.io.*
import java.net.URLConnection
import java.util.zip.GZIPInputStream
import javax.inject.Inject

const val TAG = "MarkdownRichText"

data class LocalResource(
    val file: File,
    val mime: String,
    val title: String,
)

@HiltViewModel
class MarkdownRichTextViewModel @Inject constructor(
    private val notesRepository: NotesRepository,
) : ViewModel() {

    fun loadLocalResource(id: String): Result<LocalResource> {
        return notesRepository.loadResource(id = id)
            .map { resource ->
                val file = notesRepository.resourceDir.resolve(buildString {
                    append(resource.id)
                    if (resource.fileExtension.isNotEmpty()) {
                        append(".")
                        append(resource.fileExtension)
                    }
                })
                LocalResource(file = file, mime = resource.mime, title = resource.title)
            }
    }

}

@Composable
fun MarkdownRichText(
    modifier: Modifier = Modifier,
    viewModel: MarkdownRichTextViewModel = hiltViewModel(),
    htmlBodyText: String,
    navigateToNote: (String) -> Unit,
) {
    val webViewState = rememberWebViewStateWithHTMLData(
        data = htmlBodyText,
    )
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val client = remember {
        LocalContentWebViewClient(
            context = context,
            getResource = viewModel::loadLocalResource,
            handleUrl = { url ->
                if (url.scheme == FILES_SCHEME) {
                    scope.launch {
                        url.path?.let {
                            try {
                                val resourceId = AssetHelper.removeLeadingSlash(it)
                                val localResource =
                                    viewModel.loadLocalResource(resourceId).getOrThrow()
                                val shareIntent: Intent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    type = localResource.mime
                                    val sharedFile =
                                        context.getCacheSharedDir().resolve(localResource.title)
                                    localResource.file.copyTo(sharedFile, true)
                                    val shareUri = FileProvider.getUriForFile(
                                        context,
                                        "${BuildConfig.APPLICATION_ID}.fileprovider",
                                        sharedFile
                                    )
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    putExtra(Intent.EXTRA_STREAM, shareUri)
                                    putExtra(Intent.EXTRA_TITLE, localResource.title)
                                }
                                context.startActivity(
                                    Intent.createChooser(
                                        shareIntent,
                                        localResource.title
                                    )
                                )
                            } catch (e: Exception) {
                                Toast.makeText(
                                    context,
                                    e.localizedMessage ?: e.toString(),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                } else if (url.scheme == NOTES_SCHEME) {
                    url.path?.let {
                        val noteId = AssetHelper.removeLeadingSlash(it)
                        navigateToNote(noteId)
                    }
                } else {
                    try {
                        context.startActivity(Intent(Intent.ACTION_VIEW, url))
                    } catch (e: ActivityNotFoundException) {
                        Toast.makeText(
                            context,
                            e.localizedMessage ?: e.toString(),
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                }
                true
            })
    }

    WebView(
        state = webViewState,
        modifier = modifier
            .clickable(false) {}
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        captureBackPresses = false,
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
    getResource: (String) -> Result<LocalResource>,
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
        const val ASSETS_SCHEME = "ruslin-assets"
        const val FILES_SCHEME = "ruslin-files"
        const val NOTES_SCHEME = "ruslin-notes"
        const val CORRUPT_FILES_SCHEME = "ruslin-corrupt-files"
    }
}

private class LocalContentPathHandler(
    context: Context,
    private val getResource: (String) -> Result<LocalResource>
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
            val localResource = getResource(resourceId).getOrThrow()
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
