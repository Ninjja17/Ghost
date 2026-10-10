package com.shadowengineer.api.github;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import java.util.List;

import com.shadowengineer.api.auth.AuthTokenFilter;
import com.shadowengineer.api.auth.InvalidCredentialsException;
import com.shadowengineer.api.evidence.EvidenceExtractionService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/sources/github")
public class GitHubPreviewController {

    private final GitHubPreviewService service;
    private final GitHubConnectionRepository connectionRepository;
    private final GitHubRepositoryRecordRepository repositoryRecordRepository;
    private final EvidenceExtractionService evidenceExtractionService;

    public GitHubPreviewController(GitHubPreviewService service,
                                   GitHubConnectionRepository connectionRepository,
                                   GitHubRepositoryRecordRepository repositoryRecordRepository,
                                   EvidenceExtractionService evidenceExtractionService) {
        this.service = service;
        this.connectionRepository = connectionRepository;
        this.repositoryRecordRepository = repositoryRecordRepository;
        this.evidenceExtractionService = evidenceExtractionService;
    }

    @GetMapping("/preview")
    public GitHubPreviewService.GitHubSnapshot preview(@RequestParam String username) {
        return service.preview(username);
    }

    @PostMapping("/connect")
    @ResponseStatus(HttpStatus.CREATED)
    public GitHubPreviewService.GitHubSnapshot connect(@RequestParam String username, HttpServletRequest request) {
        UUID userId = authenticatedUserId(request);
        GitHubPreviewService.GitHubSnapshot snapshot = service.preview(username);
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        GitHubConnection connection = connectionRepository.save(new GitHubConnection(UUID.randomUUID(), userId, snapshot.profile().id(),
                snapshot.profile().login(), now, snapshot.observedAt()));
        repositoryRecordRepository.replaceSnapshot(connection.id(), snapshot.repositories().stream()
            .map(repository -> new GitHubRepositoryRecord(UUID.randomUUID(), connection.id(), String.valueOf(repository.id()),
                repository.name(), repository.htmlUrl(), repository.language(), repository.stars(), repository.forks(),
                repository.size(), repository.fork(), repository.updatedAt(), snapshot.observedAt()))
            .toList());
        evidenceExtractionService.rebuild(connection);
        return snapshot;
    }

    @GetMapping("/connection")
    public GitHubConnection connection(HttpServletRequest request) {
        return connectionRepository.findByUserId(authenticatedUserId(request))
                .orElseThrow(() -> new GitHubSourceException(HttpStatus.NOT_FOUND, "No GitHub source is connected."));
    }

    @DeleteMapping("/connection")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void disconnect(HttpServletRequest request) {
        connectionRepository.findByUserId(authenticatedUserId(request)).ifPresent(connection -> {
            repositoryRecordRepository.replaceSnapshot(connection.id(), List.of());
            connectionRepository.deleteByUserId(connection.userId());
        });
    }

    @GetMapping("/repositories")
    public List<GitHubRepositoryRecord> repositories(HttpServletRequest request) {
        GitHubConnection connection = connectionRepository.findByUserId(authenticatedUserId(request))
                .orElseThrow(() -> new GitHubSourceException(HttpStatus.NOT_FOUND, "No GitHub source is connected."));
        return repositoryRecordRepository.findByConnectionId(connection.id());
    }

    private UUID authenticatedUserId(HttpServletRequest request) {
        Object userId = request.getAttribute(AuthTokenFilter.AUTH_USER_ID_ATTRIBUTE);
        if (userId == null) throw new InvalidCredentialsException();
        return (UUID) userId;
    }
}
