package art.shittim.secure

const val PArticleModify : Long = 0b1000
const val PAccountModify : Long = 0b0100

val perms = listOf(PArticleModify, PAccountModify)
val allPerm = perms.reduce { acc, num -> acc withPerm num }.toLong()

infix fun Long.hasPerm(perm: Long)
    = this and perm > 0

infix fun Long.withPerm(perm: Long)
    = this or perm
