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

class LogViewerFragment : Fragment() {
    private var logTextView: TextView? = null
    private var scrollView: ScrollView? = null
    private var exportLogButton: MaterialButton? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_log_viewer, container, false)

        logTextView = rootView.findViewById(R.id.logsTextView)
        scrollView = rootView.findViewById(R.id.scrollView)
        exportLogButton = rootView.findViewById(R.id.exportLogButton)

        val observer = Observer { out: String? ->
            if (out != null) {
                logTextView?.append("$out")
                scrollView?.post { scrollView?.fullScroll(ScrollView.FOCUS_DOWN) }
            }
        }

        sharedLogs?.logsTextHead?.observe(requireActivity(), observer)
        scrollView?.fullScroll(ScrollView.FOCUS_DOWN)

        exportLogButton?.setOnClickListener {
            val logContent = logTextView?.text.toString()
            
            if (logContent.isNotEmpty()) {
                // Create a temporary file to share
                val logFile = File(requireContext().cacheDir, "Windroid-$selectedGameName-Log-${System.currentTimeMillis() / 1000}.txt")
                logFile.writeText(logContent)
                
                // Create sharing intent
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_SUBJECT, "Windroid Log - $selectedGameName")
                    putExtra(Intent.EXTRA_TEXT, logContent)
                    
                    // Also attach the file
                    val fileUri = FileProvider.getUriForFile(
                        requireContext(),
                        "${requireContext().packageName}.fileprovider",
                        logFile
                    )
                    putExtra(Intent.EXTRA_STREAM, fileUri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                
                // Start sharing activity
                startActivity(Intent.createChooser(shareIntent, getString(R.string.share_log)))
            } else {
                Toast.makeText(context, getString(R.string.no_log_content), Toast.LENGTH_SHORT).show()
            }
        }

        return rootView
    }
}
