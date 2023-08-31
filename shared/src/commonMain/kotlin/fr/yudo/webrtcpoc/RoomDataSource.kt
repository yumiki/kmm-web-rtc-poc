package fr.yudo.webrtcpoc

import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import com.shepeliev.webrtckmp.IceCandidate
import com.shepeliev.webrtckmp.SessionDescription
import com.shepeliev.webrtckmp.SessionDescriptionType
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.ChangeType
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlin.random.Random

class RoomDataSource {

    private val firestore by lazy { Firebase.firestore }
    private val roomsRef by lazy { firestore.collection("rooms") }

    private
    fun generateRoomId(): String {
        val characters = "abcdefghijklmnopqrstuvwxyz"
        val random = Random(Clock.System.now().toEpochMilliseconds())

        val code = buildString {
            repeat(3) {
                append(characters[random.nextInt(characters.length)])
            }
            append("-")
            repeat(3) {
                append(characters[random.nextInt(characters.length)])
            }
        }

        return code
    }


    fun createRoom(): String {
        val id = generateRoomId()
        roomsRef.document(id) // TODO check if there is already a existing document
        return id
    }

    suspend fun insertOffer(roomId: String, description: SessionDescription) {
        roomsRef.document(roomId).set(
            mapOf(
                "offer" to description.sdp,
                "expireAt" to getExpireAtTime()
            )
        )
    }

    suspend fun insertAnswer(roomId: String, description: SessionDescription) {
        roomsRef.document(roomId).update(mapOf("answer" to description.sdp))
    }

    suspend fun insertIceCandidate(roomId: String, peerName: String, candidate: IceCandidate) {
        roomsRef.document(roomId)
            .collection(peerName)
            .add(
                mapOf(
                    "candidate" to candidate.candidate,
                    "sdpMLineIndex" to candidate.sdpMLineIndex,
                    "sdpMid" to candidate.sdpMid,
                    "expireAt" to getExpireAtTime(),
                )
            )
    }

    private fun getExpireAtTime(): Instant {
        val now = Clock.System.now().toEpochMilliseconds()
        val expireAt = now + FIRESTORE_DOCUMENT_TTL_SECONDS * 1000
        return Instant.fromEpochMilliseconds(expireAt)
    }

    suspend fun getOffer(roomId: String): SessionDescription? {
        val snapshot = roomsRef.document(roomId).get()
        val offerSdp = snapshot.takeIf { it.exists }?.get<String>("offer")
        return offerSdp?.let { SessionDescription(SessionDescriptionType.Offer, it) }
    }

    fun getAnswer(roomId: String): Flow<SessionDescription> = roomsRef
        .document(roomId)
        .snapshots
        .filter {
            Logger.log(Severity.Error, "Room", message = "getAnswer: ${it.data<Map<String, String>>()}", throwable = null)
            it.exists
        }.map {
            it.data<Map<String, String>>()["answer"]
        }
        .filterNotNull()
        .map { answer ->
            SessionDescription(SessionDescriptionType.Answer, answer).also {
                sessionDescription ->
                Logger.log(Severity.Error, "Room", message = "getAnswer sdp: $sessionDescription", throwable = null)
            }
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun observeIceCandidates(roomId: String, peerName: String): Flow<IceCandidate> =
        roomsRef
            .document(roomId)
            .collection(peerName)
            .snapshots
            .map {
                it.documentChanges
            }
            .map { documents ->
                documents
                    .filter { it.type == ChangeType.ADDED }
                    .map { documentChange ->
                        //Logger.log(Severity.Error, "Room", message = "New doc ${documentChange.document.data<fr.yudo.webrtcpoc.IceCandidate>()}", throwable = null)
                        //documentChange.document.data<fr.yudo.webrtcpoc.IceCandidate>()
                        documentChange.document.data(strategy = fr.yudo.webrtcpoc.IceCandidate.serializer()).toIceCandidate()
                        /*IceCandidate(
                            sdpMid = documentChange.document.data<Map<String, Any>>()["sdpMid"] as String,
                            sdpMLineIndex = (documentChange.document.data<Map<String, Any>>()["sdpMid"] as Long).toInt(),
                            candidate = documentChange.document.data<Map<String, Any>>()["candidate"] as String
                        )*/
                    }
            }.flatMapConcat {
                it.asFlow()
            }

}

@Serializable
data class IceCandidate(val sdpMid: String, val sdpMLineIndex: Int, val candidate: String) {
    fun toIceCandidate(): IceCandidate = IceCandidate(sdpMid, sdpMLineIndex, candidate)
}

internal const val FIRESTORE_DOCUMENT_TTL_SECONDS = 60 * 60 * 5 // 5 hours