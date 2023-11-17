package org.example.support.conf;

import org.flowable.engine.cfg.HttpClientConfig;
import org.flowable.http.common.api.client.FlowableHttpClient;
import org.flowable.http.common.impl.apache.client5.ApacheHttpComponents5FlowableHttpClient;
import org.flowable.http.common.impl.spring.reactive.SpringWebClientFlowableHttpClient;
import org.flowable.spring.boot.FlowableHttpProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author renc
 */
@Configuration(proxyBeanMethods = false)
public class FlowableSupports {

    // TODO @see org.flowable.http.common.impl.HttpClientConfig.determineHttpClient
    @Bean
    public FlowableHttpClient flowableHttpClient(FlowableHttpProperties httpProperties) {
        HttpClientConfig config = new HttpClientConfig();
        config.setUseSystemProperties(httpProperties.isUseSystemProperties());
        config.setConnectionRequestTimeout(httpProperties.getConnectionRequestTimeout());
        config.setConnectTimeout(httpProperties.getConnectTimeout());
        config.setDisableCertVerify(httpProperties.isDisableCertVerify());
        config.setRequestRetryLimit(httpProperties.getRequestRetryLimit());
        config.setSocketTimeout(httpProperties.getSocketTimeout());
        return new SpringWebClientFlowableHttpClient(config);
    }
}
