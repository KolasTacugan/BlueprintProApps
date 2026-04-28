package com.example.blueprintproapps.network

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.example.blueprintproapps.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class CredentialReminderBottomSheet : BottomSheetDialogFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = true
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_credential_reminder, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<Button>(R.id.btnGoToProfile).setOnClickListener {
            dismiss()
            startActivity(Intent(requireContext(), ProfileActivity::class.java))
        }

        view.findViewById<Button>(R.id.btnMaybeLater).setOnClickListener {
            dismiss()
        }
    }
}
