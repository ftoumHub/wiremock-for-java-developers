package libs.wiremock;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.VerificationException;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import com.github.tomakehurst.wiremock.verification.NearMiss;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.List;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

public class WireMockExtension extends WireMockServer implements BeforeAllCallback, AfterAllCallback {

    private final int port;

    public WireMockExtension(int port) {
        super(wireMockConfig()
                .port(port)
                .notifier(new ConsoleNotifier(true))
                .extensions(new ResponseTemplateTransformer(true)));
        this.port = port;
    }

    @Override
    public void beforeAll(ExtensionContext context) {
        start();
        WireMock.configureFor("localhost", port());
    }

    @Override
    public void afterAll(ExtensionContext context) {
        checkForUnmatchedRequests();
        stop();
    }

    private void checkForUnmatchedRequests() {
        List<LoggedRequest> unmatchedRequests = findAllUnmatchedRequests();
        if (!unmatchedRequests.isEmpty()) {
            List<NearMiss> nearMisses = findNearMissesForAllUnmatchedRequests();
            if (nearMisses.isEmpty()) {
                throw VerificationException.forUnmatchedRequests(unmatchedRequests);
            } else {
                throw VerificationException.forUnmatchedNearMisses(nearMisses);
            }
        }
    }
}