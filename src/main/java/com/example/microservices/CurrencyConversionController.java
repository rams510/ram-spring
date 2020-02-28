package com.example.microservices;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class CurrencyConversionController {

	Logger ZUUL_LOGG = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private CurrencyExchangeServiceProxy proxy;

	// case-1: hardcoding the values
	// http://localhost:8100/currency-conversion/from/USD/to/INR/quantity/1000
	@GetMapping("/currency-conversion/from/{from}/to/{to}/quantity/{quantity}")
	public CurrencyConversion convertCurrency1(@PathVariable("from") String from, @PathVariable("to") String to,
			@PathVariable BigDecimal quantity) {

		return new CurrencyConversion(1L, from, to, BigDecimal.ONE, quantity, quantity, 0);
	}

	// case 2: consuming the rest service through RestClient
	// http://localhost:8100/currency-conversion-rest/from/US/to/INR/quantity/1000
	@GetMapping("/currency-conversion-rest/from/{from}/to/{to}/quantity/{quantity}")
	public CurrencyConversion convertCurrency2(@PathVariable("from") String from, @PathVariable("to") String to,
			@PathVariable BigDecimal quantity) {

		// problem 1 : we need to write more lines of code so, to avoid this problem
		// Feign came into the picture
		Map<String, String> uriVariables = new HashMap();
		uriVariables.put("from", from);
		uriVariables.put("to", to);

		ResponseEntity<CurrencyConversion> responseEntity = new RestTemplate().getForEntity(
				"http://localhost:8000/currency-exchange-jpa/from/{from}/to/{to}", CurrencyConversion.class,
				uriVariables);

		CurrencyConversion response = responseEntity.getBody();

		return new CurrencyConversion(response.getId(), from, to, response.getCurrencyMultiple(), quantity,
				quantity.multiply(response.getCurrencyMultiple()), response.getPort());
	}

	// case 3 : Feign is also a another Rest client to call the restful services
	// http://localhost:8100/currency-conversion-feign/from/US/to/INR/quantity/1000
	// Way of calling the micro service through ZUUL Api :
	// http://localhost:8765/currency-conversion-service/currency-conversion-feign/from/USD/to/INR/quantity/1000
	@GetMapping("/currency-conversion-feign/from/{from}/to/{to}/quantity/{quantity}")
	public CurrencyConversion convertConversion(@PathVariable("from") String from, @PathVariable("to") String to,
			@PathVariable("quantity") BigDecimal quantity) {

		CurrencyConversion response = proxy.retriveExchangeValue(from, to);
		
		ZUUL_LOGG.info("{}", response);
		
		
		return new CurrencyConversion(response.getId(), from, to, response.getCurrencyMultiple(), quantity,
				quantity.multiply(response.getCurrencyMultiple()), response.getPort());
	}

}
