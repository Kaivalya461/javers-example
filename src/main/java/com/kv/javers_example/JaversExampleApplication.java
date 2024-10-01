package com.kv.javers_example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class JaversExampleApplication {

	public static void main(String[] args) {
		SpringApplication.run(JaversExampleApplication.class, args);
	}

	/*
	*
//added
//removed
//2nd element entirely updated to new one
//2nd element's non "ID" fields updated
------------------------------------------

-----------
Scenario 1:
===== Meth Added
Emp 1 ->
Emp Name: null to Frodo
Favorite Drugs:
	Added New Drug:
		NdcCode = 68308-115-01
		Drug name = Meth
		Drug Amount = 69

------------------------------------------

-----------
Scenario 2:
===== Meth Removed
Emp 1 ->
Favorite Drugs:
	Removed Drug:
		NdcCode = 68308-115-01
		Drug name = Meth
		Drug Amount = 69
------------------------------------------

-----------
Scenario 3:
===== 2nd element entirely updated to new one,
===== Already has Meth and Cocaine, where Cocaine was changed to Marijuana
Emp 1 ->
Favorite Drugs:
	Changed Drug:
		7360
		Marijuana
		55.5
------------------------------------------
*/
}
