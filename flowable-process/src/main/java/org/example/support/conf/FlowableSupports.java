package org.example.support.conf;

import io.netty.channel.ChannelOption;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.handler.timeout.ReadTimeoutHandler;
import liquibase.pro.packaged.B;
import org.flowable.engine.cfg.HttpClientConfig;
import org.flowable.http.common.api.client.FlowableHttpClient;
import org.flowable.http.common.impl.apache.client5.ApacheHttpComponents5FlowableHttpClient;
import org.flowable.http.common.impl.spring.reactive.SpringWebClientFlowableHttpClient;
import org.flowable.spring.boot.FlowableHttpProperties;
import org.springframework.boot.autoconfigure.web.reactive.function.client.WebClientAutoConfiguration;
import org.springframework.boot.web.reactive.function.client.WebClientCustomizer;
import org.springframework.cloud.client.loadbalancer.reactive.LoadBalancedExchangeFilterFunction;
import org.springframework.cloud.client.loadbalancer.reactive.ReactorLoadBalancerClientAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static java.lang.Math.toIntExact;

/**
 * @author renc
 */
@Configuration(proxyBeanMethods = false)
public class FlowableSupports {

    @Bean
    public WebClientCustomizer webClientCustomizer(FlowableHttpProperties httpProperties,
                                                   LoadBalancedExchangeFilterFunction lbFunction) {
        return webClientBuilder -> {
            HttpClientConfig config = new HttpClientConfig();
            config.setUseSystemProperties(httpProperties.isUseSystemProperties());
            config.setConnectionRequestTimeout(httpProperties.getConnectionRequestTimeout());
            config.setRequestRetryLimit(httpProperties.getRequestRetryLimit());

            HttpClient httpClient = HttpClient.create(ConnectionProvider
                            .builder("flowableHttpClient")
                            .maxConnections(500)
                            .build())
                    .disableRetry(httpProperties.getRequestRetryLimit() == 0)
                    .responseTimeout(httpProperties.getSocketTimeout())
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS,
                            toIntExact(httpProperties.getConnectTimeout().toMillis()))
                    .compress(true);

            if (httpProperties.isDisableCertVerify()) {
                try {
                    SslContext sslContext = SslContextBuilder
                            .forClient()
                            .trustManager(InsecureTrustManagerFactory.INSTANCE)
                            .build();

                    httpClient = httpClient.secure(spec -> spec.sslContext(sslContext));
                } catch (Exception e) {
                    // logger.error("Could not configure HTTP Client SSL self signed strategy", e);
                }
            }

            webClientBuilder.filter(lbFunction)
                    .clientConnector(new ReactorClientHttpConnector(httpClient));
        };
    }

    /**
     * @see WebClientAutoConfiguration
     * @see ReactorLoadBalancerClientAutoConfiguration
     */
    @Bean
    public FlowableHttpClient loadBalancedFlowableHttpClient(WebClient.Builder webClientBuilder) {
        return new SpringWebClientFlowableHttpClient(webClientBuilder);
    }

}
