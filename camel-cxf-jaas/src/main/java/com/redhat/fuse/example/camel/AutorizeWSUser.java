package com.redhat.fuse.example.camel;

import com.redhat.fuse.example.NotAuthorizedUser;
import com.redhat.fuse.example.NotAuthorizedUserFault;
import org.apache.camel.*;
import org.apache.camel.builder.RouteBuilder;

public class AutorizeWSUser extends RouteBuilder {

    @Override
    public void configure() throws Exception {

        onException(CamelAuthorizationException.class)
        .log(">> User not authorized")
        .handled(true)
                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        String user = "jim";
                        NotAuthorizedUser notAuthorizedUser = new NotAuthorizedUser();
                        notAuthorizedUser.setUser(user);
                        NotAuthorizedUserFault notAuthorizedUserFault = new NotAuthorizedUserFault("User Not authorized to call this camel route !", notAuthorizedUser);

                        exchange.getOut().setFault(true);
                        exchange.getOut().setBody(notAuthorizedUserFault);
                    }
                });

        from("cxf:bean:WS").id("cxf-to-client-pojo")

          .beanRef("enrich", "convertToAuthenticationWithRealm")

          .policy("admin")
            .choice()
               .when().simple("${in.header.SOAPAction} contains 'getCustomerByName'")
                    .log(">>> We will search a Customer")
                    .beanRef("enrich", "getCustomerByName2")

               .when().simple("${in.header.SOAPAction} contains 'saveCustomer'")
                    .log(">>> We will save a Customer")
                    .beanRef("enrich", "saveCustomer")

               .when().simple("${in.header.SOAPAction} contains 'getAllCustomers'")
                    .log(">>> We will get all Customers")
                    .beanRef("enrich", "getCustomers")
            ;


    }
}
