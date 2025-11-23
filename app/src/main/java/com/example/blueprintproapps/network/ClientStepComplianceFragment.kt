package com.example.blueprintproapps.network

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.blueprintproapps.R
import com.example.blueprintproapps.api.ApiClient
import com.example.blueprintproapps.models.ClientCompliance

class ClientStepComplianceFragment : Fragment() {

    private lateinit var zoningContainer: LinearLayout
    private lateinit var otherContainer: LinearLayout

    companion object {
        fun newInstance(compliance: ClientCompliance?): ClientStepComplianceFragment {
            val fragment = ClientStepComplianceFragment()
            val args = Bundle()
            args.putParcelable("compliance", compliance)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(R.layout.fragment_client_step_compliance, container, false)

        zoningContainer = view.findViewById(R.id.zoningFilesContainer)
        otherContainer = view.findViewById(R.id.otherFilesContainer)

        // Get compliance data
        val compliance = arguments?.getParcelable<ClientCompliance>("compliance")

        // Clear containers
        zoningContainer.removeAllViews()
        otherContainer.removeAllViews()

        // Add zoning file link (if exists)
        compliance?.compliance_Zoning?.let {
            addFileLink("Zoning File", it, zoningContainer)
        }

        // Add other file link (if exists)
        compliance?.compliance_Others?.let {
            addFileLink("Other File", it, otherContainer)
        }

        return view
    }

    private fun addFileLink(label: String, fileName: String, parent: LinearLayout) {
        val tv = TextView(requireContext())
        tv.text = label
        tv.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary))

        tv.setOnClickListener {
            val fileUrl =
                if (fileName.startsWith("http")) fileName
                else "${ApiClient.getBaseUrl()}uploads/compliance/$fileName"

            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(fileUrl)))
        }

        parent.addView(tv)
    }
}
