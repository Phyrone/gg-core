package de.phyrone.gg.common.mvn

import org.apache.maven.repository.internal.MavenRepositorySystemUtils
import org.eclipse.aether.RepositorySystem
import org.eclipse.aether.artifact.Artifact
import org.eclipse.aether.artifact.DefaultArtifact
import org.eclipse.aether.collection.CollectRequest
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory
import org.eclipse.aether.graph.Dependency
import org.eclipse.aether.repository.LocalRepository
import org.eclipse.aether.repository.RemoteRepository
import org.eclipse.aether.resolution.ArtifactRequest
import org.eclipse.aether.resolution.DependencyRequest
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory
import org.eclipse.aether.spi.connector.transport.TransporterFactory
import org.eclipse.aether.transport.file.FileTransporterFactory
import org.eclipse.aether.transport.http.HttpTransporterFactory
import org.eclipse.aether.util.filter.ScopeDependencyFilter
import java.io.File
import java.util.*

class MvnResolver(
    repos: List<String> = listOf(MAVEN_CENTRAL_REPO, JCENTER_REPO, JITPACK_REPO),
    localRepoFile: File
) {

    private val localRepo = LocalRepository(localRepoFile)
    private val repos =
        repos.map { repo -> RemoteRepository.Builder(UUID.randomUUID().toString(), "default", repo).build() }

    private val session = MavenRepositorySystemUtils.newSession().also { session ->
        session.localRepositoryManager = system.newLocalRepositoryManager(session, localRepo)

    }

    private fun createArtifactRequest(artifact: Artifact): ArtifactRequest {
        val request = ArtifactRequest()
        request.artifact = artifact
        repos.forEach { repo -> request.addRepository(repo) }
        return request
    }

    private fun createDependencyRequest(artifact: Artifact): DependencyRequest {
        val dependency = Dependency(artifact, "compile", false)
        val collectRequest = CollectRequest(dependency, repos)
        return DependencyRequest(collectRequest, ScopeDependencyFilter("provided", "test", "import", "system"))
    }

    fun resolveArtifact(artifact: Artifact): File? {
        val request = createArtifactRequest(artifact)
        return system.resolveArtifact(session, request).artifact.file
    }

    fun resolveArtifactReclusive(artifact: Artifact): List<File> {
        val request = createDependencyRequest(artifact)
        return system.resolveDependencies(
            session,
            request
        ).artifactResults.mapNotNull { artifactResult -> artifactResult.artifact.file }
    }

    companion object Static {
        const val MAVEN_CENTRAL_REPO = "https://repo.maven.apache.org/maven2/"
        const val JCENTER_REPO = "https://jcenter.bintray.com/"
        const val JITPACK_REPO = "https://jitpack.io/"

        fun toArtifact(coords: String) = DefaultArtifact(coords)

        fun toArtifact(group: String, artifact: String, version: String) =
            DefaultArtifact(group, artifact, "jar", version)

        private val locator by lazy {
            MavenRepositorySystemUtils.newServiceLocator().apply {
                addService(RepositoryConnectorFactory::class.java, BasicRepositoryConnectorFactory::class.java)
                addService(TransporterFactory::class.java, FileTransporterFactory::class.java)
                addService(TransporterFactory::class.java, HttpTransporterFactory::class.java)
            }
        }
        private val system by lazy { locator.getService(RepositorySystem::class.java) }
    }
}