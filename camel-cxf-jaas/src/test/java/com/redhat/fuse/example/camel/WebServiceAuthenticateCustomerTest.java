package com.redhat.fuse.example.camel;

import com.redhat.fuse.example.*;
import org.apache.camel.CamelContext;
import org.apache.camel.test.junit4.CamelSpringTestSupport;
import org.apache.cxf.interceptor.Interceptor;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.message.Message;
import org.apache.cxf.ws.security.wss4j.WSS4JOutInterceptor;
import org.junit.Test;
import org.springframework.context.support.AbstractXmlApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WebServiceAuthenticateCustomerTest extends CamelSpringTestSupport {

    // should be the same address as we have in our route
    private static final String URL = "http://localhost:9090/training/WebService";

    protected CamelContext camel;

    @Override
    protected AbstractXmlApplicationContext createApplicationContext() {
        return new ClassPathXmlApplicationContext("/META-INF/spring/CamelContext1.xml");
    }

    protected static CustomerService createCXFClient(String url) {

        List<Interceptor<? extends Message>> outInterceptors = new ArrayList<Interceptor<? extends Message>>();

        // Define WSS4j properties for flow outgoing
        Map<String, Object> outProps = new HashMap<String, Object>();
        outProps.put("action", "UsernameToken Timestamp");

        /* CONFIG USING PASSWORD DIGEST
        outProps.put("passwordType", "PasswordDigest");
        outProps.put("user", "jim");
        outProps.put("passwordCallbackClass", "com.redhat.fuse.example.camel.UTPasswordCallback");
         */

        // CONFIG WITH CLEAR PASSWORD
        outProps.put("passwordType", "PasswordText");
        outProps.put("user", "charles");
        outProps.put("passwordCallbackClass", "com.redhat.fuse.example.camel.UTPasswordCallback");

        WSS4JOutInterceptor wss4j = new WSS4JOutInterceptor(outProps);

        // Add LoggingOutInterceptor
        LoggingOutInterceptor loggingOutInterceptor = new LoggingOutInterceptor();

        outInterceptors.add(wss4j);
        outInterceptors.add(loggingOutInterceptor);

        // we use CXF to create a client for us as its easier than JAXWS and works
        JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
        factory.setOutInterceptors(outInterceptors);
        factory.setServiceClass(CustomerService.class);
        factory.setAddress(url);
        return (CustomerService) factory.create();
    }

    @Test
    public void testGetAllCustomers() throws Exception {

        String client = "Fuse";

        // Create Get Customer By Name
        GetCustomerByName req = new GetCustomerByName();
        req.setName(client);

        // create the webservice client and send the request
        String url = context.resolvePropertyPlaceholders(URL);
        CustomerService customerService = createCXFClient(url);

        GetCustomerByNameResponse result  = customerService.getCustomerByName(req);
        System.out.println(">>> Response : " + result);

        // Assert get Fuse customer
        assertEquals("Fuse", result.getReturn().get(0).getName());
        assertEquals("FuseSource Office",result.getReturn().get(0).getAddress().get(0));
        assertEquals(CustomerType.BUSINESS,result.getReturn().get(0).getType());

    }
}
