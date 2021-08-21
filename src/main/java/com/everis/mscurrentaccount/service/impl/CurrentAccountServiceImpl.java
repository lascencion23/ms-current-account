package com.everis.mscurrentaccount.service.impl;

import com.everis.mscurrentaccount.entity.CreditCard;
import com.everis.mscurrentaccount.entity.CurrentAccount;
import com.everis.mscurrentaccount.entity.Customer;
import com.everis.mscurrentaccount.repository.CurrentAccountRepository;
import com.everis.mscurrentaccount.service.CurrentAccountService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreaker;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class CurrentAccountServiceImpl implements CurrentAccountService {

	private final WebClient webClient;
	private final ReactiveCircuitBreaker reactiveCircuitBreaker;

    String uri = "http://gateway:8090/api";
    
	public CurrentAccountServiceImpl(ReactiveResilience4JCircuitBreakerFactory circuitBreakerFactory) {
		this.webClient = WebClient.builder().baseUrl(this.uri).build();
		this.reactiveCircuitBreaker = circuitBreakerFactory.create("customerCredit");
	}

	@Autowired
	CurrentAccountRepository currentAccountRepository;

	// Plan A CUSTOMER
	@Override
	public Mono<Customer> findCustomerById(String id) {
		return reactiveCircuitBreaker.run(webClient.get().uri(this.uri + "/ms-customer/customer/find/{id}", id)
				.accept(MediaType.APPLICATION_JSON).retrieve().bodyToMono(Customer.class), throwable -> {
					return this.getDefaultCustomer();
				});
	}

	// Plan B CUSTOMER
	public Mono<Customer> getDefaultCustomer() {
		Mono<Customer> customer = Mono.just(new Customer("0", null, null, null, null, null, null, null));
		return customer;
	}

	// Plan A - CREDITCARD
	@Override
	public Flux<CreditCard> findCreditCardByCustomerId(String id) {
		return reactiveCircuitBreaker.run(webClient.get().uri(this.uri + "/ms-creditcard/creditcard/find/{id}", id)
				.accept(MediaType.APPLICATION_JSON).retrieve().bodyToFlux(CreditCard.class), throwable -> {
					return this.getDefaultCreditCard();
				});
	}

	@Override
	public Mono<Long> creditExpiredById(String id) {
		return webClient.get().uri(this.uri + "/ms-credit-charge/creditCharge/creditExpiredById/{id}", id)
				.accept(MediaType.APPLICATION_JSON).retrieve().bodyToMono(Long.class);
	}

	// Plan B - CREDITCARD
	public Flux<CreditCard> getDefaultCreditCard() {
		Flux<CreditCard> creditCard = Flux.just(new CreditCard("0", null, null, null, null, null, null));
		return creditCard;
	}

	@Override
	public Mono<CurrentAccount> create(CurrentAccount t) {
		return currentAccountRepository.save(t);
	}

	@Override
	public Flux<CurrentAccount> findAll() {
		return currentAccountRepository.findAll();
	}

	@Override
	public Mono<CurrentAccount> findById(String id) {
		return currentAccountRepository.findById(id);
	}

	@Override
	public Mono<CurrentAccount> update(CurrentAccount t) {
		return currentAccountRepository.save(t);
	}

	@Override
	public Mono<Boolean> delete(String t) {
		return currentAccountRepository.findById(t)
				.flatMap(ca -> currentAccountRepository.delete(ca).then(Mono.just(Boolean.TRUE)))
				.defaultIfEmpty(Boolean.FALSE);
	}

	@Override
	public Mono<Long> countCustomerAccountBank(String id) {
		return currentAccountRepository.findByCustomerId(id).count();
	}

	@Override
	public Flux<CurrentAccount> customerAccountBank(String id) {
		return currentAccountRepository.findByCustomerId(id);
	}

	@Override
	public Mono<CurrentAccount> findByAccountNumber(String number) {
		return currentAccountRepository.findByAccountNumber(number);
	}

}
