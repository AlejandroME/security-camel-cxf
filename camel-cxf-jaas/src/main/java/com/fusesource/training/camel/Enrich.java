package com.fusesource.training.camel;

import com.fusesource.training.*;
import org.apache.camel.Body;
import org.apache.camel.Exchange;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.common.security.UsernameToken;
import org.apache.cxf.message.MessageContentsList;
import org.apache.ws.security.WSUsernameTokenPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import javax.security.auth.Subject;
import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigDecimal;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Enrich {

    private static Logger log = LoggerFactory.getLogger(Enrich.class);
    static List<Customer> customers = new ArrayList<Customer>();
    Random randomGenerator = new Random();

    private void generateCustomer() {
        Customer customer = new Customer();
        customer.setName("Fuse");
        customer.setNumOrders(randomGenerator.nextInt(100));
        customer.setRevenue(randomGenerator.nextInt(10000));
        customer.setType(CustomerType.BUSINESS);
        customer.setTest(BigDecimal.valueOf(100.00));
        customer.getAddress().add("FuseSource Office");
        customers.add(customer);
    }

    public SaveCustomer createCustomer(@Body String name) {
        Customer customer = new Customer();
        customer.setName(name);
        customer.setNumOrders(randomGenerator.nextInt(100));
        customer.setRevenue(randomGenerator.nextInt(10000));
        customer.setType(CustomerType.BUSINESS);
        customer.setTest(BigDecimal.valueOf(100.00));
        customer.getAddress().add("FuseSource Office");

        SaveCustomer result = new SaveCustomer();
        result.setCustomer(customer);
        return result;
    }

    public GetAllCustomersResponse getCustomers() {

        GetAllCustomersResponse response = new GetAllCustomersResponse();
        response.getReturn().addAll(customers);
        return response;
    }

    public GetCustomerByNameResponse getCustomerByName(@Body GetCustomerByName cSearched) {

        List<Customer> result = new ArrayList<Customer>();
        // Search for Customer using name as key
        for(Customer c : customers) {
            if (c.getName().equals(cSearched.getName())) {
               result.add(c);
               log.info(">> Customer find !");
               break;
            }
        }

        GetCustomerByNameResponse response = new GetCustomerByNameResponse();
        response.getReturn().addAll(result);

        return response;

    }

    public GetCustomerByNameResponse getCustomerByName2(Exchange ex) {

        MessageContentsList payload = (MessageContentsList) ex.getIn().getBody();
        // String toSearch = (String) payload.get(0);
        GetCustomerByName customerByName = (GetCustomerByName) payload.get(0);
        String cSearch = customerByName.getName();

        List<Customer> result = new ArrayList<Customer>();
        // Search for Customer using name as key
        for(Customer c : customers) {
            if (c.getName().equals(cSearch)) {
                result.add(c);
                log.info(">> Customer find !");
                break;
            }
        }

        GetCustomerByNameResponse response = new GetCustomerByNameResponse();
        response.getReturn().addAll(result);

        return response;

    }

    public Customer saveCustomer(@Body SaveCustomer c) {

        String address = (c.getCustomer().getAddress().get(0) != null) ?  c.getCustomer().getAddress().get(0) : "Unknown address";
        XMLGregorianCalendar birthDate = c.getCustomer().getBirthDate();

        // enrich the customer received from backend data
        Customer customer = new Customer();
        customer.setName(c.getCustomer().getName());
        customer.getAddress().add(address);
        customer.setBirthDate(birthDate);
        customer.setNumOrders(randomGenerator.nextInt(100));
        customer.setRevenue(randomGenerator.nextInt(10000));
        customer.setType(CustomerType.PRIVATE);
        customer.setTest(BigDecimal.valueOf(100.00));
        customers.add(customer);

        log.info(">> Customer created and added in the array.");

        return customer;
    }

    public void checkBody(Exchange ex) {

       Object payload = ex.getIn().getBody();
       log.info(">> Response prepared : " + payload);
    }

    public void convertToAuthenticationWithRealm(Exchange exchange) {

        Subject newSubject = null;

        /* As Client has already been authenticated by camel cxf using wss4j and we still would like to get Username Token, then
           we have to retrieve this information from the CxfMessage
         */
        SoapMessage soapMessage = (SoapMessage) exchange.getIn().getHeader("CamelCxfMessage");
        UsernameToken securityToken = (UsernameToken) soapMessage.get(org.apache.cxf.common.security.SecurityToken.class);
        Authentication answer = new UsernamePasswordAuthenticationToken(securityToken.getName(), securityToken.getPassword());

        // Create a new Subject + Authentication Principal
        if (answer != null) {
            newSubject = new Subject();
            newSubject.getPrincipals().add(answer);
        }

        // Override Exchange.AUTHENTICATION
        exchange.getIn().setHeader(Exchange.AUTHENTICATION,newSubject);
    }

    public void convertToAuthentication(Exchange exchange) {

        Subject newSubject = null;

        /* IF WE USE JAASLoginInterceptor, Exchange.AUTHENTICATION contains javax.security.auth.Subject with
           a principal which is a RolePrincipal (e.g. admin by example) and not longer anymore the TokenPrincipal
         So we cannot use this code anymore
         */

        Subject subject = (Subject) exchange.getIn().getHeader(Exchange.AUTHENTICATION);
        WSUsernameTokenPrincipal ut = null;

        Authentication answer = null;
        for (Principal principal : subject.getPrincipals()) {
            if (principal instanceof WSUsernameTokenPrincipal) {
                ut = (WSUsernameTokenPrincipal) principal;
                answer = new UsernamePasswordAuthenticationToken(ut.getName(), ut.getPassword());
                break;
            }
        }

        // Create a new Subject + Authentication Principal
        if (ut != null) {
            newSubject = new Subject();
            newSubject.getPrincipals().add(answer);
        }

        // Override Exchange.AUTHENTICATION
        exchange.getIn().setHeader(Exchange.AUTHENTICATION,newSubject);

    }


}
