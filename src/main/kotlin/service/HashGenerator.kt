package org.example.service

class HashGenerator {
    fun getHashes(): Map<String, String> {
        return mapOf(
            "0" to "8c348dc7cf2ca7a26fbca7d924c3617d78620f18",
            "1" to "59eb68ea39325cd3346fff29291e9d64516bab8c",
            "2" to "7374438bc311a369dd8092cb5ccf1c15678ba13c",
            "3" to "ba320084152da55f5f5272ebef64a3759f959c16",
            "4" to "06def764a9957c2b91b02c9fe4abcde48a4886c4",
            "5" to "63e27314c677e60ecee18b809102fd2f74dbdaca",
            "6" to "7a69ca8888bf1cb0f032f290589351337253c8cb",
            "7" to "58dc795192d5982e43fce5e151962baad57aaa23",
            "8" to "5d2467f373ba0b82916b466ceb3b4ced8ff8bd3c",
            "9" to "f61cbae9db225cf89805bb58d276648740c50bd7"
        )
    }
}

//import java.security.MessageDigest
//import java.util.UUID
//
//class HashGenerator {
//    // 해시값을 저장할 프로퍼티
//    val hashes: List<String>
//
//    init {
//        // 클래스 초기화 시 10개의 해시값을 생성
//        hashes = generateHashes(10)
//    }
//
//    // 해시값 생성 메서드
//    private fun generateHashes(count: Int): List<String> {
//        val hashes = mutableListOf<String>()
//        val digest = MessageDigest.getInstance("SHA-256")
//
//        repeat(count) {
//            val uuid = UUID.randomUUID().toString()
//            val hashBytes = digest.digest(uuid.toByteArray())
//            val hashString = hashBytes.joinToString("") { byte -> "%02x".format(byte) }
//            hashes.add(hashString)
//        }
//
//        return hashes
//    }
//}