package art.shittim.secure

const val PArticleModify = 0b1000
const val PAccountModify = 0b0100

val perms = listOf(PAccountModify, PAccountModify)
val allPerm = perms.reduce { acc, num -> acc withPerm num }

infix fun Int.hasPerm(perm: Int)
    = this and perm > 0

infix fun Int.withPerm(perm: Int)
    = this or perm