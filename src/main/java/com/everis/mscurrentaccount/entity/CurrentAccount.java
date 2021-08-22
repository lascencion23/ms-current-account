package com.everis.mscurrentaccount.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@Document("CurrentAccount")
@AllArgsConstructor
@NoArgsConstructor
public class CurrentAccount {
	
    private String id;
    
    private String accountNumber;
    
    private Customer customer;

    private List<Person> holders;

    private List<Person> signers;

    private Integer freeTransactions;

    private Double commissionTransactions;

    private Double commissionMaintenance;

    private Double balance;

    private LocalDateTime date;

    public static String generateAccountNumber() {
        final String ACCOUNT_PREFIX = "300-";
        Random random = new Random();
        return ACCOUNT_PREFIX + random.nextInt(999999999);
    }
}
