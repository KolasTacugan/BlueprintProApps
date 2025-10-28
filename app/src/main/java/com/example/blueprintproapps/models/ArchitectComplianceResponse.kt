package com.example.blueprintproapps.models

data class ArchitectComplianceResponse(
    val compliance_Id: Int,
    val zoningFile: String?,   // ✅ match backend JSON key
    val othersFile: String?    // ✅ match backend JSON key
)
