package com.example.ui.components

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.webkit.JavascriptInterface
import android.webkit.ValueCallback
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.ExtractionRule
import com.example.ui.theme.BluePrimary
import com.example.ui.theme.TealAccent

enum class VisualPickerMode {
    NONE,
    PICK_ID_INPUT,
    PICK_BIRTH_YEAR_INPUT,
    PICK_SUBMIT_BUTTON,
    PICK_EXTRACTION_FIELD
}

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun InteractiveWebScraperView(
    url: String,
    pickerMode: VisualPickerMode,
    selectedIdSelector: String,
    selectedBirthYearSelector: String,
    selectedSubmitSelector: String,
    extractionRules: List<ExtractionRule>,
    onSelectorPicked: (mode: VisualPickerMode, selector: String, textSnippet: String) -> Unit,
    onPageLoaded: (title: String) -> Unit,
    onWebViewCreated: (WebView) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var pageTitle by remember { mutableStateOf("") }

    // Inject Inspection JavaScript when pickerMode changes
    LaunchedEffect(pickerMode, webViewRef) {
        val wv = webViewRef ?: return@LaunchedEffect
        if (pickerMode != VisualPickerMode.NONE) {
            val script = getPickerInjectionScript(pickerMode)
            wv.evaluateJavascript(script, null)
        } else {
            // Remove highlight overlays
            wv.evaluateJavascript("if (window.cleanScraperPicker) window.cleanScraperPicker();", null)
        }
    }

    // Highlight existing selected elements when WebView is loaded
    fun highlightSelectedElements() {
        val wv = webViewRef ?: return
        val js = """
            (function() {
                // Clear past highlights
                document.querySelectorAll('.scraper-highlighted').forEach(e => {
                    e.style.outline = '';
                    e.style.boxShadow = '';
                });
                
                function applyOutline(sel, color, label) {
                    if (!sel) return;
                    try {
                        let el = document.querySelector(sel);
                        if (el) {
                            el.style.outline = '3px solid ' + color;
                            el.style.boxShadow = '0 0 10px ' + color;
                        }
                    } catch(e) {}
                }
                
                applyOutline('$selectedIdSelector', '#2563EB', 'ID Input');
                applyOutline('$selectedBirthYearSelector', '#0D9488', 'Birth Year');
                applyOutline('$selectedSubmitSelector', '#D97706', 'Submit Button');
            })();
        """.trimIndent()
        wv.evaluateJavascript(js, null)
    }

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                WebView(ctx).apply {
                    webViewRef = this
                    onWebViewCreated(this)

                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        useWideViewPort = true
                        loadWithOverviewMode = true
                        mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                        userAgentString = "Mozilla/5.0 (Linux; Android 13; Pixel 7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/116.0.0.0 Mobile Safari/537.36"
                    }

                    // Native Javascript Interface bridge
                    addJavascriptInterface(object {
                        @JavascriptInterface
                        fun onElementSelected(selector: String, textSnippet: String, tag: String) {
                            // Run on UI thread
                            post {
                                onSelectorPicked(pickerMode, selector, textSnippet)
                                highlightSelectedElements()
                            }
                        }
                    }, "AndroidScraperBridge")

                    webViewClient = object : WebViewClient() {
                        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                            super.onPageStarted(view, url, favicon)
                            isLoading = true
                        }

                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            isLoading = false
                            pageTitle = view?.title ?: ""
                            onPageLoaded(pageTitle)

                            if (pickerMode != VisualPickerMode.NONE) {
                                view?.evaluateJavascript(getPickerInjectionScript(pickerMode), null)
                            }
                            highlightSelectedElements()
                        }
                    }

                    if (url.isNotBlank()) {
                        loadUrl(url)
                    }
                }
            },
            update = { wv ->
                if (url.isNotBlank() && wv.url != url && !url.endsWith("about:blank")) {
                    wv.loadUrl(url)
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Loading Overlay Bar
        if (isLoading) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter),
                color = TealAccent
            )
        }

        // Active Selector Mode Banner
        if (pickerMode != VisualPickerMode.NONE) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
                    .align(Alignment.TopCenter),
                shape = RoundedCornerShape(12.dp),
                color = when (pickerMode) {
                    VisualPickerMode.PICK_ID_INPUT -> BluePrimary
                    VisualPickerMode.PICK_BIRTH_YEAR_INPUT -> TealAccent
                    VisualPickerMode.PICK_SUBMIT_BUTTON -> Color(0xFFD97706)
                    VisualPickerMode.PICK_EXTRACTION_FIELD -> Color(0xFF059669)
                    else -> MaterialTheme.colorScheme.primary
                },
                contentColor = Color.White,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AdsClick,
                            contentDescription = null,
                            tint = Color.White
                        )
                        Column {
                            Text(
                                text = when (pickerMode) {
                                    VisualPickerMode.PICK_ID_INPUT -> "انقر على مربع ادخال رقم الهوية في الصفحة"
                                    VisualPickerMode.PICK_BIRTH_YEAR_INPUT -> "انقر على مربع ادخال سنة الميلاد في الصفحة"
                                    VisualPickerMode.PICK_SUBMIT_BUTTON -> "انقر على زر الاستعلام في الصفحة"
                                    VisualPickerMode.PICK_EXTRACTION_FIELD -> "انقر على العنصر المراد استخراج قيمته"
                                    VisualPickerMode.NONE -> ""
                                },
                                style = MaterialTheme.typography.titleSmall,
                                color = Color.White
                            )
                            Text(
                                text = "سيتم تحديد السيلكتور تلقائياً بمجرد اللمس",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }

                    IconButton(
                        onClick = { onSelectorPicked(VisualPickerMode.NONE, "", "") }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "إلغاء",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}

/**
 * Returns JavaScript code that hooks click events on WebView elements
 * and calculates precise CSS Selectors for them.
 */
private fun getPickerInjectionScript(mode: VisualPickerMode): String {
    return """
        (function() {
            if (window.__scraperPickerActive) return;
            window.__scraperPickerActive = true;

            var hoverEl = null;

            function getCssSelector(el) {
                if (!el || el.nodeType !== Node.ELEMENT_NODE) return '';
                if (el.id) {
                    return '#' + CSS.escape(el.id);
                }
                if (el.getAttribute('name')) {
                    return el.tagName.toLowerCase() + '[name="' + el.getAttribute('name') + '"]';
                }
                if (el.getAttribute('placeholder')) {
                    return el.tagName.toLowerCase() + '[placeholder="' + el.getAttribute('placeholder') + '"]';
                }
                
                var path = [];
                while (el && el.nodeType === Node.ELEMENT_NODE) {
                    var selector = el.tagName.toLowerCase();
                    if (el.id) {
                        selector += '#' + CSS.escape(el.id);
                        path.unshift(selector);
                        break;
                    } else {
                        var sibling = el;
                        var nth = 1;
                        while (sibling = sibling.previousElementSibling) {
                            if (sibling.tagName.toLowerCase() == selector) nth++;
                        }
                        if (nth != 1) selector += ":nth-of-type(" + nth + ")";
                    }
                    path.unshift(selector);
                    el = el.parentElement;
                }
                return path.join(" > ");
            }

            function handleMouseOver(e) {
                e.stopPropagation();
                if (hoverEl) {
                    hoverEl.style.outline = hoverEl.__origOutline || '';
                }
                hoverEl = e.target;
                hoverEl.__origOutline = hoverEl.style.outline;
                hoverEl.style.outline = '3px dashed #E11D48';
            }

            function handleClick(e) {
                e.preventDefault();
                e.stopPropagation();

                var target = e.target;
                var selector = getCssSelector(target);
                var textSnippet = (target.innerText || target.value || target.placeholder || target.getAttribute('alt') || '').trim();

                if (window.AndroidScraperBridge) {
                    window.AndroidScraperBridge.onElementSelected(selector, textSnippet, '${mode.name}');
                }

                // Cleanup hover
                if (hoverEl) {
                    hoverEl.style.outline = hoverEl.__origOutline || '';
                    hoverEl = null;
                }

                return false;
            }

            window.cleanScraperPicker = function() {
                document.removeEventListener('mouseover', handleMouseOver, true);
                document.removeEventListener('click', handleClick, true);
                if (hoverEl) {
                    hoverEl.style.outline = hoverEl.__origOutline || '';
                }
                window.__scraperPickerActive = false;
            };

            document.addEventListener('mouseover', handleMouseOver, true);
            document.addEventListener('click', handleClick, true);
        })();
    """.trimIndent()
}
