package com.shadowengineer.api.github;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Service
public class GitHubPreviewService {

    private final RestClient restClient = RestClient.builder()
            .baseUrl("https://api.github.com")
            .defaultHeader("Accept", "application/vnd.github+json")
            .build();

    public GitHubSnapshot preview(String username) {
        String normalizedUsername = normalize(username);
        GitHubProfile profile = getProfile(normalizedUsername);
        List<GitHubRepository> repositories = getRepositories(normalizedUsername).stream()
                .filter(repository -> !repository.fork())
                .toList();

        Map<String, Integer> languageCounts = new HashMap<>();
        for (GitHubRepository repository : repositories) {
            if (repository.language() != null && !repository.language().isBlank()) {
                languageCounts.merge(repository.language(), 1, Integer::sum);
            }
        }
        int languageTotal = languageCounts.values().stream().mapToInt(Integer::intValue).sum();
        List<LanguageSignal> languages = languageCounts.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .limit(5)
                .map(entry -> new LanguageSignal(entry.getKey(), entry.getValue(), languageTotal == 0 ? 0 : Math.round(entry.getValue() * 100f / languageTotal)))
                .toList();
        int activeRepositories = (int) repositories.stream()
                .filter(repository -> repository.updatedAt() != null && repository.updatedAt().isAfter(OffsetDateTime.now(ZoneOffset.UTC).minusMonths(6)))
                .count();
        int stars = repositories.stream().mapToInt(GitHubRepository::stars).sum();

        return new GitHubSnapshot(profile, repositories, languages, stars, activeRepositories, OffsetDateTime.now(ZoneOffset.UTC));
    }

    private GitHubProfile getProfile(String username) {
        try {
            GitHubProfile profile = restClient.get().uri("/users/{username}", username).retrieve().body(GitHubProfile.class);
            if (profile == null) throw new GitHubSourceException(HttpStatus.BAD_GATEWAY, "GitHub returned an empty profile.");
            return profile;
        } catch (RestClientResponseException exception) {
            throw sourceException(exception, "GitHub profile lookup failed.");
        }
    }

    private List<GitHubRepository> getRepositories(String username) {
        try {
            List<GitHubRepository> repositories = restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/users/{username}/repos").queryParam("per_page", 100).queryParam("sort", "updated").build(username))
                    .retrieve()
                    .body(new org.springframework.core.ParameterizedTypeReference<>() {});
            return repositories == null ? new ArrayList<>() : repositories;
        } catch (RestClientResponseException exception) {
            throw sourceException(exception, "GitHub repository lookup failed.");
        }
    }

    private GitHubSourceException sourceException(RestClientResponseException exception, String fallback) {
        HttpStatus status = exception.getStatusCode().value() == 404 ? HttpStatus.NOT_FOUND
                : exception.getStatusCode().value() == 403 ? HttpStatus.TOO_MANY_REQUESTS : HttpStatus.BAD_GATEWAY;
        String message = status == HttpStatus.NOT_FOUND ? "That GitHub profile could not be found."
                : status == HttpStatus.TOO_MANY_REQUESTS ? "GitHub is rate limiting requests. Try again in a few minutes." : fallback;
        return new GitHubSourceException(status, message);
    }

    private String normalize(String username) {
        String normalized = username == null ? "" : username.trim().replaceFirst("^@", "");
        if (!normalized.matches("[A-Za-z0-9-]{1,39}")) throw new GitHubSourceException(HttpStatus.BAD_REQUEST, "Enter a valid GitHub username.");
        return normalized;
    }

    public record GitHubSnapshot(
            GitHubProfile profile,
            List<GitHubRepository> repositories,
            List<LanguageSignal> languages,
            int stars,
            int activeRepositories,
            OffsetDateTime observedAt
    ) {
    }

    public record GitHubProfile(
            long id,
            String login,
            String name,
            @JsonProperty("avatar_url") String avatarUrl,
            @JsonProperty("public_repos") int publicRepos,
            int followers,
            @JsonProperty("html_url") String htmlUrl
    ) {
    }

    public record GitHubRepository(
            long id,
            String name,
            @JsonProperty("html_url") String htmlUrl,
            String language,
            @JsonProperty("stargazers_count") int stars,
            @JsonProperty("forks_count") int forks,
            int size,
            @JsonProperty("updated_at") OffsetDateTime updatedAt,
            boolean fork
    ) {
    }

    public record LanguageSignal(String name, int count, int percentage) {
    }
}
