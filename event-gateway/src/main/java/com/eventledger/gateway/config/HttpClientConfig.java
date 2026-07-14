package com.eventledger.gateway.config;
import org.springframework.beans.factory.annotation.Value; import org.springframework.context.annotation.*; import org.springframework.http.client.JdkClientHttpRequestFactory; import org.springframework.web.client.RestClient;
import java.net.http.HttpClient; import java.time.Duration;
@Configuration
public class HttpClientConfig {
 @Bean RestClient accountRestClient(@Value("${account-service.base-url}") String baseUrl){
  var http=HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(1)).build(); var factory=new JdkClientHttpRequestFactory(http); factory.setReadTimeout(Duration.ofSeconds(2));
  return RestClient.builder().baseUrl(baseUrl).requestFactory(factory).build();
 }
}
