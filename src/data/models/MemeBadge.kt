package com.samarth.models

import com.samarth.models.enums.MemeBadgeType

data class MemeBadge(
    val memeBadgeType: MemeBadgeType,
    val time:Long
)
