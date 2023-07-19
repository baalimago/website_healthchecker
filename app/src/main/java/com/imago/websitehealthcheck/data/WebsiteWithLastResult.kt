package com.imago.websitehealthcheck.data

import androidx.room.Embedded
import androidx.room.Relation

data class WebsiteWithLastResult(
    @Embedded val website: Website,
    @Relation(
        parentColumn = "id",
        entityColumn = "websiteId",
        entity = CheckResult::class
    )
    val lastResult: CheckResult?
)