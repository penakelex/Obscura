package org.penakelex.obscura.grpc

import com.google.protobuf.ByteString
import io.grpc.CallOptions
import io.grpc.Channel
import io.grpc.ClientCall
import io.grpc.ClientInterceptors
import io.grpc.ForwardingClientCall
import io.grpc.ManagedChannel
import io.grpc.Metadata
import io.grpc.MethodDescriptor
import io.grpc.ServerInterceptors
import io.grpc.Status
import io.grpc.StatusException
import io.grpc.inprocess.InProcessChannelBuilder
import io.grpc.inprocess.InProcessServerBuilder
import io.grpc.testing.GrpcCleanupRule
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.penakelex.obscura.config.ServerConfig
import org.penakelex.obscura.crypto.CipherType
import org.penakelex.obscura.db.repository.NoteRepository
import org.penakelex.obscura.db.repository.SessionRepository
import org.penakelex.obscura.db.tables.Notes
import org.penakelex.obscura.db.tables.Sessions
import org.penakelex.obscura.db.tables.Users
import org.penakelex.obscura.proto.ClientSyncPayload
import org.penakelex.obscura.proto.NoteProto
import org.penakelex.obscura.proto.SecureNotesSyncGrpcKt
import org.penakelex.obscura.proto.ServerSyncPayload
import org.penakelex.obscura.proto.SyncStatus
import org.testcontainers.containers.PostgreSQLContainer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class NotesSyncServiceTest {
    companion object {
        private val postgres =
            PostgreSQLContainer("postgres:16-alpine").apply {
                withDatabaseName("obscura_grpc_test")
                withUsername("test")
                withPassword("test")
                start()
            }
    }

    @get:Rule
    val grpcCleanup = GrpcCleanupRule()

    private lateinit var channel: ManagedChannel
    private lateinit var sessionRepository: SessionRepository
    private lateinit var noteRepository: NoteRepository
    private val testUserId: Uuid =
        Uuid.parse("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
    private var rawToken: String = ""

    @Before
    fun setUp() {
        Database.connect(
            url = postgres.jdbcUrl,
            driver = "org.postgresql.Driver",
            user = postgres.username,
            password = postgres.password
        )
        transaction {
            SchemaUtils.drop(Notes, Sessions, Users)
            SchemaUtils.create(Users, Sessions, Notes)
            exec(
                """
                INSERT INTO users (id, email, password_hash) 
                VALUES ('$testUserId', 'grpc-test@example.com', 'hash')
                """.trimIndent()
            )
        }

        val serverConfig = ServerConfig()

        sessionRepository =
            SessionRepository(serverConfig.security.session)
        noteRepository = NoteRepository(serverConfig.validation)

        val session = kotlinx.coroutines.runBlocking {
            sessionRepository.create(testUserId, "test-device")
        }
        rawToken = session.rawToken

        val serverName = InProcessServerBuilder.generateName()
        val syncService = NotesSyncService(
            noteRepository = noteRepository,
            sessionRepository = sessionRepository,
            validationConfig = serverConfig.validation,
        )
        val authInterceptor = GrpcAuthInterceptor()

        grpcCleanup.register(
            InProcessServerBuilder.forName(serverName)
                .directExecutor()
                .addService(
                    ServerInterceptors.intercept(
                        syncService,
                        authInterceptor
                    )
                )
                .build()
                .start()
        )

        channel = grpcCleanup.register(
            InProcessChannelBuilder.forName(serverName)
                .directExecutor()
                .build()
        )
    }

    @After
    fun tearDown() {
        transaction {
            SchemaUtils.drop(Notes, Sessions, Users)
        }
    }

    private fun createAuthenticatedStub(
        token: String = rawToken
    ): SecureNotesSyncGrpcKt.SecureNotesSyncCoroutineStub {
        val interceptedChannel = ClientInterceptors.intercept(
            channel, bearerInterceptor(token)
        )
        return SecureNotesSyncGrpcKt.SecureNotesSyncCoroutineStub(
            interceptedChannel
        )
    }

    private fun bearerInterceptor(token: String): io.grpc.ClientInterceptor =
        object : io.grpc.ClientInterceptor {
            override fun <ReqT, RespT> interceptCall(
                method: MethodDescriptor<ReqT, RespT>,
                callOptions: CallOptions,
                next: Channel
            ): ClientCall<ReqT, RespT> {
                val call = next.newCall(method, callOptions)
                return object : ForwardingClientCall
                .SimpleForwardingClientCall<ReqT, RespT>(call) {
                    override fun start(
                        responseListener: Listener<RespT>,
                        headers: Metadata
                    ) {
                        headers.put(
                            Metadata.Key.of(
                                "authorization",
                                Metadata.ASCII_STRING_MARSHALLER
                            ),
                            "Bearer $token"
                        )
                        super.start(responseListener, headers)
                    }
                }
            }
        }

    private suspend fun syncFlow(
        stub: SecureNotesSyncGrpcKt.SecureNotesSyncCoroutineStub,
        payloads: List<ClientSyncPayload>
    ): List<ServerSyncPayload> {
        val requestFlow: Flow<ClientSyncPayload> = flow {
            payloads.forEach { emit(it) }
        }
        return stub.syncNotes(requestFlow).toList()
    }

    private fun noteProto(
        id: String,
        data: String = "encrypted-data",
        cipherType: Int = CipherType.DEFAULT.id,
        updatedAt: Long = System.currentTimeMillis(),
        isDeleted: Boolean = false
    ): NoteProto = NoteProto.newBuilder()
        .setId(id)
        .setEncryptedData(ByteString.copyFromUtf8(data))
        .setCipherType(cipherType)
        .setUpdatedAt(updatedAt)
        .setIsDeleted(isDeleted)
        .build()

    private fun payload(
        changes: List<NoteProto> = emptyList(),
        lastSync: Long = 0L
    ): ClientSyncPayload = ClientSyncPayload.newBuilder()
        .addAllClientChanges(changes)
        .setLastSyncTimestamp(lastSync)
        .build()

    @Test
    fun `missing auth header returns UNAUTHENTICATED`() = runTest {
        val unauthStub =
            SecureNotesSyncGrpcKt.SecureNotesSyncCoroutineStub(channel)
        val exception =
            assertFailsWith<StatusException> {
                syncFlow(unauthStub, listOf(payload()))
            }
        assertEquals(
            Status.Code.UNAUTHENTICATED,
            exception.status.code
        )
    }

    @Test
    fun `invalid token returns UNAUTHENTICATED`() = runTest {
        val badStub = createAuthenticatedStub("invalid_token_hex")
        val exception =
            assertFailsWith<StatusException> {
                syncFlow(badStub, listOf(payload()))
            }
        assertEquals(
            Status.Code.UNAUTHENTICATED,
            exception.status.code
        )
    }

    @Test
    fun `revoked token returns UNAUTHENTICATED`() = runTest {
        sessionRepository.revokeAllByUserId(testUserId)

        val stub = createAuthenticatedStub()
        val exception =
            assertFailsWith<StatusException> {
                syncFlow(stub, listOf(payload()))
            }
        assertEquals(
            Status.Code.UNAUTHENTICATED,
            exception.status.code
        )
    }

    @Test
    fun `sync empty changes returns SUCCESS with empty delta`() =
        runTest {
            val stub = createAuthenticatedStub()
            val responses = syncFlow(stub, listOf(payload()))

            assertEquals(1, responses.size)
            assertEquals(SyncStatus.SUCCESS, responses[0].status)
            assertTrue(responses[0].serverChangesList.isEmpty())
            assertTrue(responses[0].newSyncTimestamp > 0)
        }

    @Test
    fun `sync new notes saves them and returns in delta`() = runTest {
        val stub = createAuthenticatedStub()
        val noteId = "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"
        val note = noteProto(noteId, "secret-data", updatedAt = 1000L)

        val responses =
            syncFlow(stub, listOf(payload(changes = listOf(note))))

        assertEquals(1, responses.size)
        assertEquals(SyncStatus.SUCCESS, responses[0].status)
        assertTrue(responses[0].serverChangesList.any { it.id == noteId })
    }

    @Test
    fun `second sync with newer lastSync returns only newer notes`() =
        runTest {
            val stub = createAuthenticatedStub()
            val note1 = noteProto(
                "11111111-1111-1111-1111-111111111111",
                "data-1", updatedAt = 1000L
            )
            val note2 = noteProto(
                "22222222-2222-2222-2222-222222222222",
                "data-2", updatedAt = 2000L
            )

            syncFlow(
                stub,
                listOf(payload(changes = listOf(note1, note2)))
            )

            val note3 = noteProto(
                "33333333-3333-3333-3333-333333333333",
                "data-3", updatedAt = 3000L
            )
            val responses = syncFlow(
                stub,
                listOf(
                    payload(
                        changes = listOf(note3),
                        lastSync = 2500L
                    )
                )
            )

            assertEquals(1, responses.size)
            val serverChanges = responses[0].serverChangesList
            assertEquals(1, serverChanges.size)
            assertEquals(note3.id, serverChanges[0].id)
        }

    @Test
    fun `LWW - server newer than client ignores client change`() =
        runTest {
            val stub = createAuthenticatedStub()
            val noteId = "cccccccc-cccc-cccc-cccc-cccccccccccc"

            val serverNote =
                noteProto(noteId, "server-data", updatedAt = 5000L)
            syncFlow(
                stub,
                listOf(payload(changes = listOf(serverNote)))
            )

            val olderClientNote = noteProto(
                noteId, "client-data", updatedAt = 3000L
            )
            syncFlow(
                stub,
                listOf(
                    payload(
                        changes = listOf(olderClientNote),
                        lastSync = 6000L
                    )
                )
            )

            val delta = syncFlow(stub, listOf(payload(lastSync = 0L)))
            val stored =
                delta[0].serverChangesList.first { it.id == noteId }

            assertEquals(5000L, stored.updatedAt)
            assertEquals(
                "server-data",
                stored.encryptedData.toStringUtf8()
            )
        }

    @Test
    fun `LWW - client newer than server overwrites server`() =
        runTest {
            val stub = createAuthenticatedStub()
            val noteId = "dddddddd-dddd-dddd-dddd-dddddddddddd"

            val serverNote =
                noteProto(noteId, "server-data", updatedAt = 1000L)
            syncFlow(
                stub,
                listOf(payload(changes = listOf(serverNote)))
            )

            val newerClientNote = noteProto(
                noteId, "client-data", updatedAt = 5000L
            )
            syncFlow(
                stub,
                listOf(
                    payload(
                        changes = listOf(newerClientNote),
                        lastSync = 6000L
                    )
                )
            )

            val delta = syncFlow(stub, listOf(payload(lastSync = 0L)))
            val stored =
                delta[0].serverChangesList.first { it.id == noteId }

            assertEquals(5000L, stored.updatedAt)
            assertEquals(
                "client-data",
                stored.encryptedData.toStringUtf8()
            )
        }

    @Test
    fun `soft-delete marks note as deleted`() = runTest {
        val stub = createAuthenticatedStub()
        val noteId = "eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee"

        val note = noteProto(
            noteId, "data", updatedAt = 1000L, isDeleted = false
        )
        syncFlow(stub, listOf(payload(changes = listOf(note))))

        val deleted = noteProto(
            noteId, "data", updatedAt = 2000L, isDeleted = true
        )
        syncFlow(
            stub,
            listOf(
                payload(
                    changes = listOf(deleted),
                    lastSync = 1500L
                )
            )
        )

        val delta = syncFlow(stub, listOf(payload(lastSync = 0L)))
        val stored =
            delta[0].serverChangesList.first { it.id == noteId }

        assertTrue(stored.isDeleted)
    }

    @Test
    fun `multiple payloads in one stream processed independently`() =
        runTest {
            val stub = createAuthenticatedStub()
            val note1 = noteProto(
                "f1f1f1f1-f1f1-f1f1-f1f1-f1f1f1f1f1f1",
                "data-1", updatedAt = 1000L
            )
            val note2 = noteProto(
                "f2f2f2f2-f2f2-f2f2-f2f2-f2f2f2f2f2f2",
                "data-2", updatedAt = 2000L
            )

            val responses = syncFlow(
                stub,
                listOf(
                    payload(changes = listOf(note1)),
                    payload(changes = listOf(note2))
                )
            )

            assertEquals(2, responses.size)
            assertTrue(responses.all { it.status == SyncStatus.SUCCESS })
        }

    @Test
    fun `payload exceeding max size returns PARTIAL`() = runTest {
        val stub = createAuthenticatedStub()
        val oversizedChanges = (1..501).map { i ->
            noteProto(
                "%08d-0000-0000-0000-%012d".format(i, i),
                "data-$i", updatedAt = i.toLong()
            )
        }

        val responses = syncFlow(
            stub, listOf(payload(changes = oversizedChanges))
        )

        assertEquals(1, responses.size)
        assertEquals(SyncStatus.PARTIAL, responses[0].status)
    }

    @Test
    fun `notes are isolated per user`() = runTest {
        val stub1 = createAuthenticatedStub()

        val note = noteProto(
            "abababab-abab-abab-abab-abababababab",
            "secret", updatedAt = 1000L
        )
        syncFlow(stub1, listOf(payload(changes = listOf(note))))

        // Создаём второго пользователя
        val otherUserId =
            Uuid.parse("99999999-9999-9999-9999-999999999999")
        transaction {
            exec(
                """
                INSERT INTO users (id, email, password_hash) 
                VALUES ('$otherUserId', 'other@example.com', 'hash')
                """.trimIndent()
            )
        }
        val otherSession =
            sessionRepository.create(otherUserId, "other-device")

        val stub2 = createAuthenticatedStub(otherSession.rawToken)
        val otherResponses = syncFlow(stub2, listOf(payload()))

        assertTrue(otherResponses[0].serverChangesList.isEmpty())
    }
}