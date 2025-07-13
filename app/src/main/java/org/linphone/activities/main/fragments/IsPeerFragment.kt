/*
 * Copyright (c) 2010-2020 Belledonne Communications SARL.
 *
 * This file is part of linphone-android
 * (see https://www.linphone.org).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.linphone.activities.main.fragments

import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.transition.MaterialSharedAxis
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.linphone.R
import org.linphone.activities.main.dialer.fragments.DialerFragment
import org.linphone.activities.main.dialer.viewmodels.DialerViewModel
import org.linphone.utils.Constants
import org.linphone.utils.SharedPrefsManager

class IsPeerFragment : Fragment() {
    private lateinit var viewModel: DialerViewModel

    private var progress: Progress? = null
    private var isLoaded: Boolean = false
    private lateinit var webView: WebView
    var APP_URL = ""
    var number: String = ""
    val mainLooper = Looper.getMainLooper()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        reenterTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)

        val view = inflater!!.inflate(R.layout.fragment_ispeer, container, false)

        viewModel = ViewModelProvider(this)[DialerViewModel::class.java]
//        binding.viewModel = viewModel

        val username = SharedPrefsManager.getInstance().getString(Constants.username)
        val password = SharedPrefsManager.getInstance().getString(Constants.password)
        Log.e("values", username + " " + password)
        APP_URL = "https://ismypeerssip.callmyway.com/Welcome/isMyPeersSipLogin?username=" + username + "&password=" + password

        webView = view.findViewById(R.id.webView)
        webView.settings.javaScriptEnabled = true

        loadWebView()

        webView.run {
            settings.javaScriptEnabled = true
            addJavascriptInterface(MyJsInterface(), "AndroidListener")
            webViewClient = object : WebViewClient() {
                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    super.onPageStarted(view, url, favicon)
                    evaluateJavascript(
                        "window.webkit = { messageHandlers: { placeCall: window.AndroidListener} }"
                    ) {}
                }
            }
        }

//        webView.loadUrl(APP_URL)

        return view
    }

    inner class MyJsInterface() {
        @JavascriptInterface
        fun postMessage(value: String) {
            number = value
            GlobalScope.launch {
                Log.e("pre thread", "Pre thread")
                Handler(mainLooper).post {
                    Log.e("post thread", "post thread")
                    webView.stopLoading()
                    webView.pauseTimers()
                    DialerFragment.startWebViewCall(number)
                }
            }
            Log.e("deep_recievedNumber", number)
        }
    }

//    user2 = 8649015
//    passw2 = 96815035260

    private fun loadWebView() {
        webView.loadUrl(APP_URL)
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                val url = request?.url.toString()
                view?.loadUrl(url)
                return super.shouldOverrideUrlLoading(view, request)
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                setProgressDialogVisibility(true)
                super.onPageStarted(view, url, favicon)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                isLoaded = true
                setProgressDialogVisibility(false)

                super.onPageFinished(view, url)
            }

            override fun onReceivedError(
                view: WebView,
                request: WebResourceRequest,
                error: WebResourceError
            ) {
                isLoaded = false
                val errorMessage = "Got Error! $error"
                showToast(errorMessage)

                setProgressDialogVisibility(false)
                super.onReceivedError(view, request, error)
            }
        }
    }

    private fun setProgressDialogVisibility(visible: Boolean) {
        if (visible) progress = Progress(activity, R.string.please_wait, cancelable = true)
        progress?.apply { if (visible) show() else dismiss() }
    }

    private fun showToast(message: String) {
//        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
    }

    override fun onPause() {
        super.onPause()
        callHiddenWebViewMethod("onPause")
    }

    override fun onResume() {
        super.onResume()
        callHiddenWebViewMethod("onResume")
    }

    private fun callHiddenWebViewMethod(name: String) {
        if (webView != null) {
            try {
                val method: Method = WebView::class.java.getMethod(name)
                method.invoke(webView)
                Log.e("Success: $name", name)
            } catch (e: NoSuchMethodException) {
                Log.e("No such method: $name", e.toString())
            } catch (e: IllegalAccessException) {
                Log.e("Illegal Access: $name", e.toString())
            } catch (e: InvocationTargetException) {
                Log.e("Invocation Target Exception: $name", e.toString())
            }
        }
    }
}
