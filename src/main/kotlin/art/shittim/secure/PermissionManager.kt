package art.shittim.secure

/*const val PArticleModify : Long = 0b1000
const val PAccountModify : Long = 0b0100*/
const val PArticleWrite: Long =  0b000001
const val PArticleDelete: Long = 0b000010
const val PArticleHidden: Long = 0b000100 // introduce line hidden to avoid arius's attack
const val PArticleAccess: Long = 0b000111

//const val PAccountCreate: Long = 0b000100
const val PAccountDelete: Long = 0b001000
const val PAccountModify: Long = 0b010000
const val PAccountList: Long =   0b100000
const val PAccountAccess: Long = 0b111000

val allPerm = listOf(PArticleAccess, PAccountAccess).reduce { acc, num -> acc withPerm num }.toLong()

infix fun Long.hasPerm(perm: Long)
    = this and perm > 0

infix fun Long.withPerm(perm: Long)
    = this or perm
