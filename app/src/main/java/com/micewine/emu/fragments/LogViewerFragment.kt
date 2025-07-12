package com.micewine.emu.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.google.android.material.button.MaterialButton
import com.micewine.emu.R
import com.micewine.emu.activities.EmulationActivity.Companion.sharedLogs
import com.micewine.emu.adapters.AdapterGame.Companion.selectedGameName
import java.io.File
import java.nio.charset.StandardCharsets

class LogViewerFragment : Fragment() {
    private var logTextView: TextView? = null
    private var scrollView: ScrollView? = null
    private var exportLogButton: MaterialButton? = null
    private var logsObserver: Observer<String>? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_log_viewer, container, false)

        logTextView = rootView.findViewById(R.id.logsTextView)
        scrollView = rootView.findViewById(R.id.scrollView)
        exportLogButton = rootView.findViewById(R.id.exportLogButton)

        logsObserver = Observer { out: String? ->
            if (out != null) {
                logTextView?.text = out
                scrollView?.post { scrollView?.fullScroll(ScrollView.FOCUS_DOWN) }
            }
        }

        sharedLogs?.logsTextHead?.observe(requireActivity(), logsObserver!!)
        scrollView?.fullScroll(ScrollView.FOCUS_DOWN)

        exportLogButton?.setOnClickListener {
            val logContent = sharedLogs?.getLogsContent() ?: logTextView?.text?.toString() ?: ""
            
            if (logContent.isNotEmpty()) {
                try {
                    // Create a temporary file to share
                    val fileName = "Windroid-$selectedGameName-Log-${System.currentTimeMillis() / 1000}.txt"
                    val logFile = File(requireContext().cacheDir, fileName)
                    logFile.writeText(logContent, StandardCharsets.UTF_8)
                    
                    // Verify file was created and has content
                    if (!logFile.exists() || logFile.length() == 0L) {
                        Toast.makeText(context, "Erro: Arquivo de log não foi criado corretamente", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }
                    
                    // Log debug information
                    android.util.Log.d("LogViewer", "Log file created: ${logFile.absolutePath}, size: ${logFile.length()} bytes")
                    
                    // Create sharing intent
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_SUBJECT, "Windroid Log - $selectedGameName")
                        
                        // Attach only the file, not the text content to avoid duplication
                        val fileUri = FileProvider.getUriForFile(
                            requireContext(),
                            "${requireContext().packageName}.fileprovider",
                            logFile
                        )
                        putExtra(Intent.EXTRA_STREAM, fileUri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        
                        // Log debug information
                        android.util.Log.d("LogViewer", "File URI: $fileUri")
                    }
                    
                    // Start sharing activity
                    startActivity(Intent.createChooser(shareIntent, getString(R.string.share_log)))
                } catch (e: Exception) {
                    Toast.makeText(context, "Erro ao compartilhar log: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, getString(R.string.no_log_content), Toast.LENGTH_SHORT).show()
            }
        }

        return rootView
    }

    override fun onDestroyView() {
        super.onDestroyView()
        logsObserver?.let { observer ->
            sharedLogs?.logsTextHead?.removeObserver(observer)
        }
        logsObserver = null
    }
}
