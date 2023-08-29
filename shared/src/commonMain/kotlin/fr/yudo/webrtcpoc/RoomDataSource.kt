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
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

class RoomDataSource {

    private val firestore by lazy { Firebase.firestore }
    private val roomsRef by lazy { firestore.collection("rooms") }

    fun createRoom(): String {
        return roomsRef.document.id.also {
            Logger.log(Severity.Error, "Room", message = "New room $it", throwable = null)
        }
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

    suspend fun getAnswer(roomId: String): SessionDescription = roomsRef
        .document(roomId)
        .snapshots
        .filter { it.exists && it.contains("answer") }
        .filterNotNull()
        .map {
            val answer = it.get<String>("answer")
            SessionDescription(SessionDescriptionType.Answer, answer)
        }.last()

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
                        Logger.log(Severity.Error, "Room", message = "New doc ${documentChange.document.data<String>()}", throwable = null)
                        IceCandidate(
                            sdpMid = documentChange.document.data<Map<String, Any>>()["sdpMid"] as String,
                            sdpMLineIndex = (documentChange.document.data<Map<String, Any>>()["sdpMid"] as Long).toInt(),
                            candidate = documentChange.document.data<Map<String, Any>>()["candidate"] as String
                        )
                    }
            }.flatMapConcat {
                it.asFlow()
            }

}

internal const val FIRESTORE_DOCUMENT_TTL_SECONDS = 60 * 60 * 5 // 5 hours