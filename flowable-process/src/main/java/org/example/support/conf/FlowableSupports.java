package org.example.support.conf;

import io.netty.channel.ChannelOption;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.handler.timeout.ReadTimeoutHandler;
import liquibase.pro.packaged.B;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.example.support.loadbalancer.ReactorLoadBalancerExchangeFilterFunction;
import org.flowable.engine.cfg.HttpClientConfig;
import org.flowable.http.common.api.client.FlowableHttpClient;
import org.flowable.http.common.impl.apache.client5.ApacheHttpComponents5FlowableHttpClient;
import org.flowable.http.common.impl.spring.reactive.SpringWebClientFlowableHttpClient;
import org.flowable.spring.boot.FlowableHttpProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.groovy.template.GroovyTemplateAutoConfiguration;
import org.springframework.boot.autoconfigure.web.reactive.function.client.WebClientAutoConfiguration;
import org.springframework.boot.web.reactive.function.client.WebClientCustomizer;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.reactive.LoadBalancedExchangeFilterFunction;
import org.springframework.cloud.client.loadbalancer.reactive.LoadBalancerClientRequestTransformer;
import org.springframework.cloud.client.loadbalancer.reactive.ReactiveLoadBalancer;
import org.springframework.cloud.client.loadbalancer.reactive.ReactorLoadBalancerClientAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.http.client.HttpClientSecurityUtils;
import reactor.netty.resources.ConnectionProvider;
import reactor.netty.tcp.SslProvider;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.lang.Math.toIntExact;

/**
 * @author renc
 */
@Configuration(proxyBeanMethods = false)
public class FlowableSupports {

    private static final Logger LOGGER = LoggerFactory.getLogger(FlowableSupports.class);

    @Bean
    @ConditionalOnProperty(value = "spring.cloud.loadbalancer.retry.enabled", havingValue = "false",
            matchIfMissing = true)
    public ReactorLoadBalancerExchangeFilterFunction loadBalancerExchangeFilterFunction(
            ReactiveLoadBalancer.Factory<ServiceInstance> loadBalancerFactory,
            ObjectProvider<List<LoadBalancerClientRequestTransformer>> transformers) {
        return new ReactorLoadBalancerExchangeFilterFunction(loadBalancerFactory,
                transformers.getIfAvailable(Collections::emptyList));
    }

    @Bean
    public WebClientCustomizer webClientCustomizer(FlowableHttpProperties httpProperties,
                                                   LoadBalancedExchangeFilterFunction lbFunction) {
        return webClientBuilder -> {

            HttpClient httpClient = HttpClient.create(ConnectionProvider
                            .builder("flowableHttpClient")
                            .maxConnections(500)
                            .build())
                    .responseTimeout(httpProperties.getSocketTimeout())
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS,
                            toIntExact(httpProperties.getConnectTimeout().toMillis()))
                    .secure()
                    .compress(true);

            if (httpProperties.isDisableCertVerify()) {
                try {
                    SslContext sslContext = SslContextBuilder
                            .forClient()
                            .trustManager(InsecureTrustManagerFactory.INSTANCE)
                            .build();

                    httpClient = httpClient.secure(spec -> spec.sslContext(sslContext));
                } catch (Exception e) {
                    LOGGER.error("Could not configure HTTP Client SSL self signed strategy", e);
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
