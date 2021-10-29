package msdk.kotlin.sample.types

interface MessageStatusType {
    companion object {
        const val CREATED = "MS-101"
        const val SENT = "MS-102"
        const val PENDING = "MS-103"
        const val ACCEPTED = "MS-104"
        const val REJECTED = "MS-105"
        const val REVIEWED = "MS-106"
    }
}