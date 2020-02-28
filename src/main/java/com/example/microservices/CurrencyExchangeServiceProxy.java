package com.example.microservices;

import org.springframework.cloud.netflix.ribbon.RibbonClient;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/*
 * By using @FeignClient we are able to consume any one of the instance in any environment.
 * And also it is integrated with ribbon to distribute the load  calls between the micro services
 * Ribbon can make use of the feign to call the multiple instances by using property file
 */
// case 3: FeignClient
//@FeignClient(name="currency-exchange-service",url="localhost:8000")
//case 4 : enable the ribbon client on proxy level
//@FeignClient(name = "currency-exchange-service")
// To intercept all the microservice calls communicate through the ZUUL api we just need to pass an application name to the feign
@FeignClient(name = "netflix-zuul-api-gateway")
@RibbonClient(name = "currency-exchange-service")
public interface CurrencyExchangeServiceProxy {

	// lets assume currency-exchange-service offering 20 resources to consumer, so
	// the thing is we can declare and call all the 20 resources in a single place
	// that is called proxy.
	// @GetMapping("/currency-exchange-jpa/from/{from}/to/{to}")
	@GetMapping("/currency-exchange-service/currency-exchange-jpa/from/{from}/to/{to}")
	public CurrencyConversion retriveExchangeValue(@PathVariable String from, @PathVariable String to);

}
