package com.cardioconnect.domain.model

enum class EcgLead(val displayName: String, val channelIndex: Int) {
    LEAD_I("Lead I", 0),
    LEAD_II("Lead II", 1),
    LEAD_III("Lead III", 2),
    AVR("aVR", 3),
    AVL("aVL", 4),
    AVF("aVF", 5),
    V1("V1", 6),
    V2("V2", 7),
    V3("V3", 8),
    V4("V4", 9),
    V5("V5", 10),
    V6("V6", 11);

    companion object {
        fun fromChannel(channel: Int): EcgLead {
            return entries.firstOrNull { it.channelIndex == channel } ?: LEAD_II
        }
    }
}
