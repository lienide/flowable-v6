package org.example.support.loadbalancer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.*;
import org.springframework.cloud.client.loadbalancer.reactive.LoadBalancedExchangeFilterFunction;
import org.springframework.cloud.client.loadbalancer.reactive.LoadBalancerClientRequestTransformer;
import org.springframework.cloud.client.loadbalancer.reactive.ReactiveLoadBalancer;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;
import java.util.Set;

import static org.example.support.loadbalancer.ExchangeFilterFunctionUtils.*;

/**
 * @author renc
 */
public class ReactorLoadBalancerExchangeFilterFunction implements LoadBalancedExchangeFilterFunction {

    private static final Logger LOG = LoggerFactory.getLogger(ReactorLoadBalancerExchangeFilterFunction.class);

    private final ReactiveLoadBalancer.Factory<ServiceInstance> loadBalancerFactory;

    private final List<LoadBalancerClientRequestTransformer> transformers;

    public ReactorLoadBalancerExchangeFilterFunction(ReactiveLoadBalancer.Factory<ServiceInstance> loadBalancerFactory,
                                                     List<LoadBalancerClientRequestTransformer> transformers) {
        this.loadBalancerFactory = loadBalancerFactory;
        this.transformers = transformers;
    }

    @Override
    public Mono<ClientResponse> filter(ClientRequest clientRequest, ExchangeFunction next) {
        URI originalUrl = clientRequest.url();

        if (!"lb".equalsIgnoreCase(originalUrl.getScheme())) { // no-balancer
            return next.exchange(clientRequest);
        }

        String serviceId = originalUrl.getHost();
        if (serviceId == null) {
            String message = String.format("Request URI does not contain a valid hostname: %s", originalUrl);
            if (LOG.isWarnEnabled()) {
                LOG.warn(message);
            }
            return Mono.just(ClientResponse.create(HttpStatus.BAD_REQUEST).body(message).build());
        }

        Set<LoadBalancerLifecycle> supportedLifecycleProcessors = LoadBalancerLifecycleValidator
                .getSupportedLifecycleProcessors(
                        loadBalancerFactory.getInstances(serviceId, LoadBalancerLifecycle.class),
                        RequestDataContext.class, ResponseData.class, ServiceInstance.class);
        String hint = getHint(serviceId, loadBalancerFactory.getProperties(serviceId).getHint());
        RequestData requestData = new RequestData(clientRequest);
        DefaultRequest<RequestDataContext> lbRequest = new DefaultRequest<>(new RequestDataContext(requestData, hint));
        supportedLifecycleProcessors.forEach(lifecycle -> lifecycle.onStart(lbRequest));

        return choose(serviceId, lbRequest).flatMap(lbResponse -> {
            ServiceInstance instance = lbResponse.getServer();
            if (instance == null) {
                String message = serviceInstanceUnavailableMessage(serviceId);
                if (LOG.isWarnEnabled()) {
                    LOG.warn(message);
                }
                supportedLifecycleProcessors.forEach(lifecycle -> lifecycle
                        .onComplete(new CompletionContext<>(CompletionContext.Status.DISCARD, lbRequest, lbResponse)));
                return Mono.just(ClientResponse.create(HttpStatus.SERVICE_UNAVAILABLE)
                        .body(serviceInstanceUnavailableMessage(serviceId)).build());
            }

            if (LOG.isDebugEnabled()) {
                LOG.debug(String.format("LoadBalancer has retrieved the instance for service %s: %s", serviceId,
                        instance.getUri()));
            }
            LoadBalancerProperties properties = loadBalancerFactory.getProperties(serviceId);
            LoadBalancerProperties.StickySession stickySessionProperties = properties.getStickySession();
            ClientRequest newRequest = buildClientRequest(clientRequest, instance,
                    stickySessionProperties.getInstanceIdCookieName(),
                    stickySessionProperties.isAddServiceInstanceCookie(), transformers);
            supportedLifecycleProcessors.forEach(lifecycle -> lifecycle.onStartRequest(lbRequest, lbResponse));
            return next.exchange(newRequest)
                    .doOnError(throwable -> supportedLifecycleProcessors.forEach(lifecycle -> lifecycle
                            .onComplete(new CompletionContext<ResponseData, ServiceInstance, RequestDataContext>(
                                    CompletionContext.Status.FAILED, throwable, lbRequest, lbResponse))))
                    .doOnSuccess(clientResponse -> supportedLifecycleProcessors.forEach(
                            lifecycle -> lifecycle.onComplete(new CompletionContext<>(CompletionContext.Status.SUCCESS,
                                    lbRequest, lbResponse, buildResponseData(requestData, clientResponse,
                                    properties.isUseRawStatusCodeInResponseData())))));
        });
    }


    private ResponseData buildResponseData(RequestData requestData, ClientResponse clientResponse,
                                           boolean useRawStatusCodes) {
        if (useRawStatusCodes) {
            return new ResponseData(requestData, clientResponse);
        }
        return new ResponseData(clientResponse, requestData);
    }

    protected Mono<Response<ServiceInstance>> choose(String serviceId, Request<RequestDataContext> request) {
        ReactiveLoadBalancer<ServiceInstance> loadBalancer = loadBalancerFactory.getInstance(serviceId);
        if (loadBalancer == null) {
            return Mono.just(new EmptyResponse());
        }
        return Mono.from(loadBalancer.choose(request));
    }
}
