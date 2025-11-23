package com.example.blueprintproapps.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.blueprintproapps.network.ClientStepReviewFragment
import com.example.blueprintproapps.network.ClientStepComplianceFragment
import com.example.blueprintproapps.network.ClientStepFinalizationFragment
import com.example.blueprintproapps.models.ClientProjectTrackerResponse

class ClientProjectTrackerAdapter(
    activity: FragmentActivity,
    private val trackerData: ClientProjectTrackerResponse
) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {

            // REVIEW STEP
            0 -> ClientStepReviewFragment.newInstance(
                trackerData.currentFileName ?: "",
                trackerData.currentFilePath ?: "",
                trackerData.currentRevision ?: 0,           // works now
                trackerData.revisionHistory ?: emptyList()
            )

            // COMPLIANCE STEP
            1 -> ClientStepComplianceFragment.newInstance(
                trackerData.compliance
            )

            // FINALIZATION STEP
            2 -> ClientStepFinalizationFragment.newInstance(
                trackerData.finalizationNotes ?: "",
                trackerData.projectStatus ?: "",
                trackerData.isRated,
                trackerData.project_Id ?:"",
                trackerData.currentFilePath
            )


            else -> throw IllegalStateException("Invalid page index")
        }
    }
    
}
